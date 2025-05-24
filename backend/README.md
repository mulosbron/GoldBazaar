# GoldBazaar API - Ubuntu 22.04 Staging Deployment Guide

This document includes complete steps to deploy the GoldBazaar API to a staging environment on an Ubuntu 22.04 server using .NET 8, MongoDB, Docker, Nginx, and HTTPS (Let's Encrypt).

---

## 1. Prepare the Ubuntu Server

```bash
sudo apt update && sudo apt upgrade -y
sudo apt install -y curl wget unzip nano git
```

---

## 2. Install MongoDB 7.0

```bash
curl -fsSL https://pgp.mongodb.com/server-7.0.asc | \
  sudo gpg -o /usr/share/keyrings/mongodb-server-7.0.gpg --dearmor

echo "deb [ arch=amd64,arm64 signed-by=/usr/share/keyrings/mongodb-server-7.0.gpg ] \
  https://repo.mongodb.org/apt/ubuntu jammy/mongodb-org/7.0 multiverse" | \
  sudo tee /etc/apt/sources.list.d/mongodb-org-7.0.list

sudo apt update && sudo apt install -y mongodb-org
sudo systemctl enable --now mongod
```

> Edit the `/etc/mongod.conf` file to set `bindIp: 0.0.0.0`.

```bash
sudo nano /etc/mongod.conf
# bindIp: 0.0.0.0
sudo systemctl restart mongod
```

---

## 3. Install .NET 8 Runtime + SDK

```bash
wget https://packages.microsoft.com/config/ubuntu/22.04/packages-microsoft-prod.deb -O packages.deb
sudo dpkg -i packages.deb && sudo apt update
sudo apt install -y dotnet-sdk-8.0 dotnet-runtime-8.0 aspnetcore-runtime-8.0
```

---

## 4. Install Docker + Docker Compose Plugin

```bash
sudo apt install -y docker.io docker-compose-plugin
sudo systemctl enable --now docker
sudo usermod -aG docker $USER
```

---

## 5. Transfer the Application from Local to Server

**From Windows:**

```powershell
scp -r <path> root@YOUR_IP:<path>
```

---

## 6. Sample Dockerfile

```dockerfile
FROM mcr.microsoft.com/dotnet/aspnet:8.0 AS base
WORKDIR /app
EXPOSE 5000
ENV ASPNETCORE_URLS=http://+:5000
ENV ASPNETCORE_ENVIRONMENT=Staging
ENV TZ=Europe/Istanbul

FROM base AS final
COPY . .
ENTRYPOINT ["dotnet", "GoldBazaarAPI.dll"]
```

---

## 7. `appsettings.Staging.json` Configuration

```json
{
  "MongoDbSettings": {
    "ConnectionString": "mongodb://172.17.0.1:27017",
    "MainDatabaseName": "gold_prices",
    "NewsDatabaseName": "gold_news"
  },
  "AllowedHosts": "*"
}
```

---

## 8. Run the Application with Docker

```bash
cd <api-path>
docker build -t goldbazaarapi .
docker run -d -p 5000:5000 --name goldapi goldbazaarapi
```

---

## 9. Set Up NGINX Reverse Proxy

```bash
sudo apt install -y nginx certbot python3-certbot-nginx
```

**/etc/nginx/sites-available/goldbazaar**:

```nginx
server {
    listen 80;
    server_name staging-api.goldmarketcap.xyz;

    location / {
        proxy_pass         http://127.0.0.1:5000;
        proxy_http_version 1.1;
        proxy_set_header   Upgrade $http_upgrade;
        proxy_set_header   Connection keep-alive;
        proxy_set_header   Host $host;
        proxy_set_header   X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header   X-Forwarded-Proto $scheme;
    }
}
```

```bash
sudo ln -s /etc/nginx/sites-available/goldbazaar /etc/nginx/sites-enabled/
sudo nginx -t && sudo systemctl reload nginx
```

---

## 10. Domain DNS A Record

| Type | Host        | Value (IP) | TTL  |
|------|-------------|------------|------|
| A    | staging-api | <ip>       | Auto |

---

## 11. Obtain HTTPS Certificate

```bash
sudo certbot --nginx -d staging-api.goldmarketcap.xyz --redirect
```

---

## 12. Test It

```bash
curl http://localhost:5000/api/gold-prices/latest
```

or:

```bash
https://staging-api.goldmarketcap.xyz/swagger
```