# Docker Lite - Custom Domain Configuration Guide

## Overview

Docker Lite supports custom domains for professional URLs and better user experience. By default, the application runs on `shopmanager.local`, but you can configure any custom domain you prefer.

---

## Quick Start

### 1. Configure Your Custom Domain

Edit the `.env` file and set your preferred domain:

```bash
# Domain Configuration
CUSTOM_DOMAIN=shopmanager.local    # Change this to your preferred domain
SHOP_NAME=My Shop                  # Your shop name
```

**Examples:**
- `shopmanager.local` - Local network access (recommended for development)
- `shop.mystore.com` - Production domain with DNS
- `localhost` - Local-only access

### 2. Add Domain to Hosts File (Local Domains Only)

For `.local` domains or testing, you need to add the domain to your system's hosts file.

#### macOS / Linux

1. Edit hosts file:
   ```bash
   sudo nano /etc/hosts
   ```

2. Add this line:
   ```
   127.0.0.1   shopmanager.local
   ```

3. Save and exit (Ctrl+X, then Y, then Enter)

4. Flush DNS cache (macOS):
   ```bash
   sudo dscacheutil -flushcache
   sudo killall -HUP mDNSResponder
   ```

#### Windows

1. Open Notepad as Administrator

2. Open file: `C:\Windows\System32\drivers\etc\hosts`

3. Add this line:
   ```
   127.0.0.1   shopmanager.local
   ```

4. Save the file

5. Flush DNS cache:
   ```cmd
   ipconfig /flushdns
   ```

### 3. Restart Docker Lite

Restart the containers to apply the new domain:

```bash
docker-compose -f docker-compose-lite.yml down
docker-compose -f docker-compose-lite.yml up -d
```

### 4. Access Your Application

Open your browser and navigate to:
- **Frontend**: http://shopmanager.local/
- **Backend API**: http://shopmanager.local/api
- **Health Check**: http://shopmanager.local/actuator/health

---

## Advanced Configuration

### Using a Production Domain

If you have a registered domain (e.g., `shop.mystore.com`), follow these steps:

1. **DNS Configuration**: Point your domain to your server's IP address
   ```
   A Record: shop.mystore.com → YOUR_SERVER_IP
   ```

2. **Update .env file**:
   ```bash
   CUSTOM_DOMAIN=shop.mystore.com
   APP_PORT=80  # or 443 for HTTPS
   ```

3. **SSL/HTTPS (Recommended)**:
   - Use a reverse proxy like Caddy (automatic HTTPS) or Certbot (Let's Encrypt)
   - Update nginx-lite.conf to redirect HTTP to HTTPS
   - Mount SSL certificates into the nginx container

### Using Multiple Domains

To support multiple domains (e.g., `shopmanager.local` and `shop.example.com`), edit `nginx-lite.conf`:

```nginx
server {
    listen 80;
    server_name shopmanager.local shop.example.com localhost;
    # ... rest of configuration
}
```

Then rebuild and restart:
```bash
docker-compose -f docker-compose-lite.yml build nginx
docker-compose -f docker-compose-lite.yml restart nginx
```

### Network Access (Other Devices on LAN)

To access from other devices on your local network:

1. Find your server's IP address:
   ```bash
   # macOS/Linux
   ifconfig | grep "inet " | grep -v 127.0.0.1

   # Windows
   ipconfig
   ```

2. On client devices, add to hosts file:
   ```
   192.168.1.100   shopmanager.local  # Replace with your server's IP
   ```

3. Access from client: http://shopmanager.local/

---

## Troubleshooting

### Issue: "This site can't be reached"

**Solution:**
1. Verify domain is in hosts file:
   ```bash
   # macOS/Linux
   cat /etc/hosts | grep shopmanager

   # Windows
   type C:\Windows\System32\drivers\etc\hosts | findstr shopmanager
   ```

2. Verify nginx is running:
   ```bash
   docker ps | grep nginx-lite
   ```

3. Check nginx logs:
   ```bash
   docker logs shop-manager-nginx-lite
   ```

### Issue: "ERR_CONNECTION_REFUSED"

**Solution:**
1. Ensure all containers are healthy:
   ```bash
   docker-compose -f docker-compose-lite.yml ps
   ```

2. Check if port 80 is in use:
   ```bash
   # macOS/Linux
   sudo lsof -i :80

   # Windows
   netstat -ano | findstr :80
   ```

3. Change `APP_PORT` in `.env` if port 80 is occupied:
   ```bash
   APP_PORT=8080  # Use different port
   ```

   Then access via: http://shopmanager.local:8080/

### Issue: 404 or API Not Found

**Solution:**
1. Verify nginx configuration was updated:
   ```bash
   docker exec shop-manager-nginx-lite cat /etc/nginx/conf.d/default.conf | grep server_name
   ```

   Should show: `server_name shopmanager.local localhost;`

2. If not updated, rebuild nginx:
   ```bash
   docker-compose -f docker-compose-lite.yml down
   docker-compose -f docker-compose-lite.yml up -d --force-recreate nginx
   ```

### Issue: Custom domain shows default nginx page

**Solution:**
1. Clear browser cache (Ctrl+Shift+Delete or Cmd+Shift+Delete)

2. Hard refresh the page (Ctrl+F5 or Cmd+Shift+R)

3. Verify frontend is serving files:
   ```bash
   docker exec shop-manager-frontend-lite ls /usr/share/nginx/html
   ```

---

## Security Best Practices

1. **Use HTTPS in Production**: Never use HTTP for sensitive data
   - Use Caddy for automatic HTTPS with Let's Encrypt
   - Or use Certbot to generate SSL certificates manually

2. **Change Default Credentials**: See `DOCKER_LITE_DEFAULT_CREDENTIALS.md`
   - Default superadmin: `superadmin` / `changeme`
   - Default admin: `admin` / `admin123`

3. **Firewall Configuration**: Only expose necessary ports
   ```bash
   # Allow only port 80/443
   sudo ufw allow 80/tcp
   sudo ufw allow 443/tcp
   ```

4. **Secure JWT Secret**: Already generated in `.env` - keep it secret!

---

## Environment Variables Reference

| Variable | Default | Description |
|----------|---------|-------------|
| `CUSTOM_DOMAIN` | `shopmanager.local` | Custom domain for the application |
| `APP_PORT` | `80` | Port to expose nginx reverse proxy |
| `SHOP_NAME` | `Shop Manager` | Display name for your shop |
| `JWT_SECRET` | (generated) | Secret key for JWT token signing |
| `POSTGRES_PASSWORD` | `shopmanager` | Database password |

---

## Examples

### Example 1: Local Development
```bash
# .env
CUSTOM_DOMAIN=localhost
APP_PORT=80
```
Access: http://localhost/

### Example 2: Network Sharing
```bash
# .env
CUSTOM_DOMAIN=shopmanager.local
APP_PORT=80
```
- Add `192.168.1.100 shopmanager.local` to all client hosts files
- Access: http://shopmanager.local/ from any device on LAN

### Example 3: Production Domain
```bash
# .env
CUSTOM_DOMAIN=shop.mystore.com
APP_PORT=443  # HTTPS
```
- Configure DNS A record: shop.mystore.com → YOUR_SERVER_IP
- Set up SSL certificates
- Access: https://shop.mystore.com/

---

## Additional Resources

- **Main README**: [README.md](./README.md)
- **Docker Lite Setup**: [DEPLOYMENT_GUIDE.md](./DEPLOYMENT_GUIDE.md)
- **Default Credentials**: [DOCKER_LITE_DEFAULT_CREDENTIALS.md](./DOCKER_LITE_DEFAULT_CREDENTIALS.md)
- **Developer Guide**: [DEVELOPER_GUIDE.md](./DEVELOPER_GUIDE.md)

---

## Support

For issues or questions:
1. Check troubleshooting section above
2. Review nginx logs: `docker logs shop-manager-nginx-lite`
3. Review backend logs: `docker logs retailhq-backend-lite`
4. Create an issue on GitHub with logs and configuration details
