EIMS 企业综合管理平台 - VMware 演示镜像说明
============================================

环境要求：
  - VMware Workstation Player 16+ 或 Workstation Pro
  - 电脑内存 >= 8GB（虚拟机分配 4GB）
  - 硬盘空余 >= 20GB

使用步骤：
  1. 用 VMware 导入 EIMS-Demo.ova
  2. 启动虚拟机，等待 1～2 分钟
  3. 浏览器打开 http://192.168.56.10
  4. 登录账号：admin
     密码：Admin@123456

若无法访问：
  - 确认虚拟机网络为「仅主机」(Host-Only)
  - 在虚拟机终端执行 ip a 查看 IP 地址
  - 检查服务：sudo systemctl status eims

制作镜像详见：演示镜像使用说明.md

仅用于演示/教学，请勿用于生产环境。
