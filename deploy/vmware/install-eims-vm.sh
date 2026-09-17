#!/bin/bash
# EIMS VMware 演示环境一键安装脚本
# 适用：Ubuntu 22.04 + Host-Only 网络（192.168.56.10）
# 用法：sudo bash install-eims-vm.sh /path/to/project

set -e

PROJECT_DIR="${1:-}"
EIMS_HOME="/opt/eims"
EIMS_IP="192.168.56.10"
DB_NAME="eims"
DB_USER="eims"
DB_PASS="eims123"
ADMIN_PASS="Admin@123456"

if [ "$(id -u)" -ne 0 ]; then
  echo "请使用 root 执行: sudo bash install-eims-vm.sh [项目目录]"
  exit 1
fi

if [ -z "$PROJECT_DIR" ] || [ ! -d "$PROJECT_DIR/eims-backend" ]; then
  echo "用法: sudo bash install-eims-vm.sh /path/to/project"
  echo "示例: sudo bash install-eims-vm.sh /mnt/hgfs/project"
  exit 1
fi

echo "=========================================="
echo " EIMS VMware 演示环境安装"
echo " 项目目录: $PROJECT_DIR"
echo " 访问地址: http://${EIMS_IP}"
echo "=========================================="

# ---------- 1. 系统依赖 ----------
echo "[1/8] 安装 JDK、Nginx、MySQL..."
export DEBIAN_FRONTEND=noninteractive
apt-get update -qq
apt-get install -y -qq openjdk-8-jdk nginx mysql-server curl

# # ---------- 2. 固定 IP（Host-Only）----------
# echo "[2/8] 配置网络 ${EIMS_IP}..."
# IFACE=$(ip -o link show | awk -F': ' '{print $2}' | grep -E '^ens|^eth' | head -1)
# if [ -n "$IFACE" ]; then
#   cat > /etc/netplan/99-eims.yaml << EOF
# network:
#   version: 2
#   ethernets:
#     ${IFACE}:
#       dhcp4: no
#       addresses:
#         - ${EIMS_IP}/24
# EOF
#   netplan apply 2>/dev/null || true
#   echo "  网卡 ${IFACE} 已配置为 ${EIMS_IP}"
# else
#   echo "  警告: 未检测到网卡，请手动配置 IP 为 ${EIMS_IP}"
# fi

# ---------- 3. MySQL ----------
echo "[3/8] 初始化数据库..."
systemctl enable mysql
systemctl start mysql

mysql -e "CREATE DATABASE IF NOT EXISTS ${DB_NAME} CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
mysql -e "CREATE USER IF NOT EXISTS '${DB_USER}'@'localhost' IDENTIFIED BY '${DB_PASS}';"
mysql -e "GRANT ALL PRIVILEGES ON ${DB_NAME}.* TO '${DB_USER}'@'localhost';"
mysql -e "FLUSH PRIVILEGES;"

SCHEMA="${PROJECT_DIR}/eims-backend/src/main/resources/db/schema.sql"
DATA="${PROJECT_DIR}/eims-backend/src/main/resources/db/data.sql"
if [ -f "$SCHEMA" ]; then
  mysql -u"${DB_USER}" -p"${DB_PASS}" "${DB_NAME}" < "$SCHEMA"
  echo "  已导入 schema.sql"
fi
if [ -f "$DATA" ]; then
  mysql -u"${DB_USER}" -p"${DB_PASS}" "${DB_NAME}" < "$DATA"
  echo "  已导入 data.sql"
fi

# 演示账号默认密码 Admin@123456（data.sql 占位哈希在 prod 下不会自动修正）
ADMIN_HASH='$2a$10$9uiLTq5BOYb4p9cYeaVYBOZ0MtXYUFKVAWoi6iCVNniH9EQG4kbqe'
mysql -u"${DB_USER}" -p"${DB_PASS}" "${DB_NAME}" -e \
  "UPDATE sys_user SET password='${ADMIN_HASH}' WHERE user_name IN ('admin','purchaser','sales','warehouse');"
echo "  已重置演示账号密码"

# ---------- 4. 构建后端（若未打包则尝试 mvn）----------
echo "[4/8] 部署后端..."
JAR="${PROJECT_DIR}/eims-backend/target/eims-backend-1.0.0.jar"
if [ ! -f "$JAR" ]; then
  if command -v mvn &>/dev/null; then
    echo "  未找到 JAR，正在 mvn package..."
    (cd "${PROJECT_DIR}/eims-backend" && mvn -q clean package -DskipTests)
  else
    echo "  错误: 未找到 ${JAR}"
    echo "  请在 Windows 上执行 mvn package 后重试，或安装 maven"
    exit 1
  fi
fi

mkdir -p "${EIMS_HOME}/frontend"
cp "$JAR" "${EIMS_HOME}/eims-backend.jar"

# ---------- 5. 部署前端 ----------
echo "[5/8] 部署前端..."
DIST="${PROJECT_DIR}/eims-frontend/dist"
if [ ! -d "$DIST" ] || [ -z "$(ls -A "$DIST" 2>/dev/null)" ]; then
  if command -v npm &>/dev/null; then
    echo "  未找到 dist，正在 npm build..."
    (cd "${PROJECT_DIR}/eims-frontend" && npm install -q && npm run build)
    DIST="${PROJECT_DIR}/eims-frontend/dist"
  else
    echo "  错误: 未找到前端 dist 目录"
    echo "  请在 Windows 上执行 npm run build 后重试"
    exit 1
  fi
fi
rm -rf "${EIMS_HOME}/frontend"/*
cp -r "${DIST}"/* "${EIMS_HOME}/frontend/"

# ---------- 6. 环境变量 ----------
echo "[6/8] 写入配置..."
cat > "${EIMS_HOME}/eims.env" << EOF
SPRING_PROFILES_ACTIVE=prod
DB_URL=jdbc:mysql://127.0.0.1:3306/${DB_NAME}?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
DB_USERNAME=${DB_USER}
DB_PASSWORD=${DB_PASS}
JWT_SECRET=EIMS-VMware-Demo-Secret-Key-32chars!!
CORS_ALLOWED_ORIGINS=http://${EIMS_IP}
SERVER_PORT=8080
EOF
chmod 600 "${EIMS_HOME}/eims.env"

# ---------- 7. systemd ----------
echo "[7/8] 配置 systemd..."
cat > /etc/systemd/system/eims.service << EOF
[Unit]
Description=EIMS Backend
After=network.target mysql.service

[Service]
Type=simple
WorkingDirectory=${EIMS_HOME}
EnvironmentFile=${EIMS_HOME}/eims.env
ExecStart=/usr/bin/java -Xms512m -Xmx1024m -jar ${EIMS_HOME}/eims-backend.jar
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
EOF

systemctl daemon-reload
systemctl enable eims mysql nginx
systemctl restart mysql
systemctl restart eims

# ---------- 8. Nginx ----------
echo "[8/8] 配置 Nginx..."
cat > /etc/nginx/sites-available/eims << 'NGINX'
server {
    listen 80;
    server_name _;

    root /opt/eims/frontend;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://127.0.0.1:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
}
NGINX

ln -sf /etc/nginx/sites-available/eims /etc/nginx/sites-enabled/eims
rm -f /etc/nginx/sites-enabled/default
nginx -t
systemctl reload nginx

# ---------- 等待后端就绪 ----------
echo "等待后端启动..."
for i in $(seq 1 30); do
  if curl -sf -X POST "http://127.0.0.1:8080/api/auth/login" \
    -H "Content-Type: application/json" \
    -d "{\"username\":\"admin\",\"password\":\"${ADMIN_PASS}\"}" | grep -q '"code":200'; then
    echo ""
    echo "=========================================="
    echo " 安装成功！"
    echo " 浏览器访问: http://${EIMS_IP}"
    echo " 账号: admin"
    echo " 密码: ${ADMIN_PASS}"
    echo "=========================================="
    exit 0
  fi
  sleep 2
done

echo "安装完成，但后端尚未就绪，请检查: journalctl -u eims -n 50"
exit 0
