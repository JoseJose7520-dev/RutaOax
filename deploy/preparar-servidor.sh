#!/usr/bin/env bash
# Prepara el VPS para RutaOax (Ubuntu/Debian). Se puede ejecutar varias veces sin romper nada.
# Uso:  bash preparar-servidor.sh
#       CERTBOT_EMAIL=tucorreo@ejemplo.com bash preparar-servidor.sh   (además pide el certificado HTTPS)
set -euo pipefail

DOMINIO="rutaoax.devs8.tech"
IP_ESPERADA="179.197.84.34"
PUERTO_API="9250"
BASE="/opt/rutaoax"

[ "$(id -u)" -eq 0 ] || { echo "ERROR: ejecuta este script como root."; exit 1; }
. /etc/os-release
echo "Sistema: ${PRETTY_NAME}"
case "${ID}" in ubuntu|debian) ;; *) echo "AVISO: este script se probó pensando en Ubuntu/Debian.";; esac

echo; echo "== 1/7 Paquetes (Java 21, MySQL, Nginx, Certbot) =="
export DEBIAN_FRONTEND=noninteractive
apt-get update -y
apt-get install -y openjdk-21-jre-headless mysql-server nginx certbot python3-certbot-nginx ufw openssl curl
java -version 2>&1 | head -1
mysql --version
if mysql --version | grep -qi mariadb; then
  echo "ERROR: se instaló MariaDB y el proyecto exige MySQL 8. Detente y avísale a tu equipo."; exit 1
fi
systemctl enable --now mysql nginx

echo; echo "== 2/7 Firewall =="
PUERTO_SSH="$(ss -tlnp 2>/dev/null | awk '/sshd/ {n=split($4,a,":"); print a[n]; exit}')"
PUERTO_SSH="${PUERTO_SSH:-22}"
echo "SSH detectado en el puerto ${PUERTO_SSH}"
ufw allow "${PUERTO_SSH}/tcp"
ufw allow 'Nginx Full'
ufw allow 25/tcp
ufw --force enable
ufw status | head -12

echo; echo "== 3/7 Base de datos =="
ARCHIVO_CLAVE="/root/.rutaoax_db_pass"
if [ ! -f "${ARCHIVO_CLAVE}" ]; then
  openssl rand -base64 24 | tr -d '/+=\n' > "${ARCHIVO_CLAVE}"
  chmod 600 "${ARCHIVO_CLAVE}"
fi
CLAVE_DB="$(cat "${ARCHIVO_CLAVE}")"
mysql <<SQL
CREATE DATABASE IF NOT EXISTS rutaoax CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'rutaoax_app'@'localhost' IDENTIFIED BY '${CLAVE_DB}';
ALTER USER 'rutaoax_app'@'localhost' IDENTIFIED BY '${CLAVE_DB}';
GRANT ALL PRIVILEGES ON rutaoax.* TO 'rutaoax_app'@'localhost';
FLUSH PRIVILEGES;
SQL
mysql -u rutaoax_app -p"${CLAVE_DB}" rutaoax -e "SELECT 'base de datos lista' AS estado;"

echo; echo "== 4/7 Usuario y carpetas =="
id rutaoax >/dev/null 2>&1 || useradd -r -s /usr/sbin/nologin rutaoax
mkdir -p "${BASE}/backend" "${BASE}/config" /var/www/rutaoax

echo; echo "== 5/7 Configuración del backend =="
ARCHIVO_PROP="${BASE}/config/application.properties"
if [ ! -f "${ARCHIVO_PROP}" ]; then
  SECRETO_JWT="$(openssl rand -base64 48 | tr -d '\n')"
  cat > "${ARCHIVO_PROP}" <<PROP
server.port=${PUERTO_API}

spring.datasource.url=jdbc:mysql://localhost:3306/rutaoax?useSSL=false&serverTimezone=America/Mexico_City&allowPublicKeyRetrieval=true
spring.datasource.username=rutaoax_app
spring.datasource.password=${CLAVE_DB}

spring.jpa.hibernate.ddl-auto=none
spring.jpa.open-in-view=false
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration

app.jwt.secret=${SECRETO_JWT}
app.jwt.expiration-ms=28800000
app.cors.allowed-origins=https://${DOMINIO}
app.frontend-url=https://${DOMINIO}

spring.mail.host=localhost
spring.mail.port=25
spring.mail.properties.mail.smtp.connectiontimeout=5000
spring.mail.properties.mail.smtp.timeout=5000
spring.mail.properties.mail.smtp.writetimeout=5000
app.mail.from=no-reply@devs8.tech
app.dev.log-reset-link=false

app.twilio.account-sid=CAMBIAR
app.twilio.auth-token=CAMBIAR
app.twilio.sms-from=CAMBIAR
app.twilio.whatsapp-from=whatsapp:+14155238886
app.notificaciones.pais-prefijo=+52
PROP
  echo "Se creó ${ARCHIVO_PROP} con claves generadas automáticamente."
else
  echo "Ya existía ${ARCHIVO_PROP}; no se modificó."
fi
chown -R rutaoax:rutaoax "${BASE}"
chmod 600 "${ARCHIVO_PROP}"

echo; echo "== 6/7 Nginx y servicio =="
cat > /etc/nginx/sites-available/rutaoax <<NGINX
server {
    listen 80;
    server_name ${DOMINIO};
    root /var/www/rutaoax;
    index index.html;

    location /api/ {
        proxy_pass http://127.0.0.1:${PUERTO_API};
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
    }

    location / {
        try_files \$uri /index.html;
    }
}
NGINX
ln -sf /etc/nginx/sites-available/rutaoax /etc/nginx/sites-enabled/rutaoax
nginx -t
systemctl reload nginx

cat > /etc/systemd/system/rutaoax.service <<UNIT
[Unit]
Description=RutaOax API
After=network.target mysql.service

[Service]
User=rutaoax
WorkingDirectory=${BASE}/backend
ExecStart=/usr/bin/java -jar ${BASE}/backend/app.jar --spring.config.additional-location=file:${BASE}/config/
Restart=always
RestartSec=5

[Install]
WantedBy=multi-user.target
UNIT
systemctl daemon-reload
systemctl enable rutaoax

echo; echo "== 7/7 DNS y HTTPS =="
DNS_IP="$(getent ahostsv4 "${DOMINIO}" | awk '{print $1; exit}' || true)"
echo "${DOMINIO} apunta a: ${DNS_IP:-(sin resolver)}   | IP esperada: ${IP_ESPERADA}"
if [ "${DNS_IP:-}" != "${IP_ESPERADA}" ]; then
  echo "AVISO: el DNS todavía no apunta a este servidor. Crea el registro A  rutaoax -> ${IP_ESPERADA}  y espera unos minutos."
  echo "Cuando resuelva, vuelve a ejecutar este script con CERTBOT_EMAIL=tucorreo@ejemplo.com"
elif [ -n "${CERTBOT_EMAIL:-}" ]; then
  certbot --nginx -d "${DOMINIO}" --non-interactive --agree-tos -m "${CERTBOT_EMAIL}" --redirect
  echo "Certificado HTTPS instalado."
else
  echo "El DNS está bien. Para activar HTTPS ejecuta:  certbot --nginx -d ${DOMINIO}"
  echo "o repite este script con CERTBOT_EMAIL=tucorreo@ejemplo.com"
fi

echo; echo "LISTO. Servidor preparado. Falta subir la aplicación con deploy.ps1 desde tu PC."
