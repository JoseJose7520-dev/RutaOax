$ErrorActionPreference = "Stop"
$VPS = "root@179.197.84.34"
$raiz = Split-Path -Parent $PSScriptRoot
Set-Location $raiz

Write-Host "1/4 Compilando el backend..."
Push-Location backend
mvn -q -DskipTests package
if ($LASTEXITCODE -ne 0) { throw "Falló la compilación del backend" }
Pop-Location

Write-Host "2/4 Compilando el frontend..."
Push-Location frontend
npm run build
if ($LASTEXITCODE -ne 0) { throw "Falló la compilación del frontend" }
Pop-Location

$jar = Get-ChildItem backend/target/*.jar | Where-Object { $_.Name -notlike "*plain*" } | Select-Object -First 1
if (-not $jar) { throw "No se encontró el JAR en backend/target" }
$archivos = Get-ChildItem frontend/dist | ForEach-Object { $_.FullName }

Write-Host "3/4 Subiendo archivos al servidor..."
scp $jar.FullName "${VPS}:/opt/rutaoax/backend/app.jar"
if ($LASTEXITCODE -ne 0) { throw "Falló la subida del JAR" }
ssh $VPS "rm -rf /var/www/rutaoax/*"
scp -r @archivos "${VPS}:/var/www/rutaoax/"
if ($LASTEXITCODE -ne 0) { throw "Falló la subida del frontend" }

Write-Host "4/4 Reiniciando el servicio..."
ssh $VPS "chown -R rutaoax:rutaoax /opt/rutaoax/backend && systemctl restart rutaoax && sleep 15 && systemctl is-active rutaoax"
if ($LASTEXITCODE -ne 0) { throw "El servicio no quedó activo: revisa  ssh $VPS 'journalctl -u rutaoax -n 80 --no-pager'" }

Write-Host "Listo: https://rutaoax.devs8.tech"
