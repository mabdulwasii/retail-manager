const fs = require('fs');
const path = require('path');

const template = fs.readFileSync(path.join(__dirname, '../public/config.js.template'), 'utf8');

const config = template
  .replace('${VITE_API_BASE_URL}', process.env.VITE_API_BASE_URL || 'https://api.retailhq.app/api')
  .replace('${VITE_KEYCLOAK_URL}', process.env.VITE_KEYCLOAK_URL || '')
  .replace('${VITE_KEYCLOAK_REALM}', process.env.VITE_KEYCLOAK_REALM || '')
  .replace('${VITE_KEYCLOAK_CLIENT_ID}', process.env.VITE_KEYCLOAK_CLIENT_ID || '')
  .replace('${VITE_APP_VERSION}', process.env.VITE_APP_VERSION || '1.0.0')
  .replace('${VITE_APP_ENV}', process.env.VITE_APP_ENV || 'production')
  .replace('${VITE_AUTH_MODE}', process.env.VITE_AUTH_MODE || 'embedded');

fs.writeFileSync(path.join(__dirname, '../public/config.js'), config);
console.log('Generated public/config.js');
console.log('  API_BASE_URL:', process.env.VITE_API_BASE_URL || 'https://api.retailhq.app/api (default)');
console.log('  APP_ENV:', process.env.VITE_APP_ENV || 'production (default)');
