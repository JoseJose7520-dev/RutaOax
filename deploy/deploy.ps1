$VPS = "root@179.197.84.34"
Set-Location backend; mvn -q -DskipTests package; Set-Location ..
Set-Location frontend; npm run build; Set-Location ..
$jar = (Get-ChildItem backend/target/*.jar | Where-Object { $_.Name -notlike "*plain*" } | Select-Object -First 1).FullName
scp $jar "${VPS}:/opt/rutaoax/backend/app.jar"
scp -r frontend/dist/. "${VPS}:/var/www/rutaoax/"
ssh $VPS "chown -R rutaoax:rutaoax /opt/rutaoax/backend && systemctl restart rutaoax && sleep 8 && systemctl is-active rutaoax"
