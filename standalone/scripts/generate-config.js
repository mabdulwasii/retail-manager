/**
 * Shop Manager Configuration Generator (JavaScript version)
 *
 * Generates all necessary configuration files for Docker Compose deployment:
 * - .env file with environment variables
 * - Keycloak realm JSON from template
 * - Docker Compose overrides
 * - README with deployment instructions
 *
 * Usage:
 *   As a module: const generateConfig = require('./generate-config.js');
 *                await generateConfig('/path/to/config.yaml');
 *
 *   As CLI:      node generate-config.js
 *                node generate-config.js --config custom-config.yaml
 */

const fs = require('fs').promises;
const fsSync = require('fs');
const path = require('path');
const yaml = require('yaml');

class ConfigGenerator {
  constructor(configPath = 'config.yaml') {
    this.configPath = path.resolve(configPath);
    this.rootDir = path.dirname(this.configPath);
    this.templatesDir = path.join(this.rootDir, 'templates');
    this.outputDir = path.join(this.rootDir, 'generated');
    this.config = {};
  }

  /**
   * Load and parse configuration file
   */
  async loadConfig() {
    console.log(`📖 Loading configuration from ${this.configPath}`);

    try {
      const configContent = await fs.readFile(this.configPath, 'utf8');
      this.config = yaml.parse(configContent);
      console.log('✅ Configuration loaded successfully');
      return true;
    } catch (error) {
      console.error(`❌ Failed to load configuration: ${error.message}`);
      throw new Error(`Configuration file error: ${error.message}`);
    }
  }

  /**
   * Validate configuration structure
   */
  validateConfig() {
    console.log('🔍 Validating configuration...');

    const requiredSections = [
      'global', 'branding', 'keycloak', 'database',
      'business', 'security', 'features'
    ];

    const missingSections = requiredSections.filter(
      section => !this.config[section]
    );

    if (missingSections.length > 0) {
      console.error(`❌ Missing required sections: ${missingSections.join(', ')}`);
      return false;
    }

    // Check for default passwords
    this.checkDefaultPasswords();

    console.log('✅ Configuration is valid');
    return true;
  }

  /**
   * Check if default passwords are being used
   */
  checkDefaultPasswords() {
    const warnings = [];
    const configStr = JSON.stringify(this.config);

    if (configStr.includes('P@ssw0rd!2024#Shop')) {
      warnings.push('Database password appears to be default');
    }

    if (configStr.includes('Adm1n!SecureP@ss2024')) {
      warnings.push('Keycloak admin password appears to be default');
    }

    if (configStr.includes('Min1o!SecureK3y2024#')) {
      warnings.push('MinIO password appears to be default');
    }

    if (warnings.length > 0) {
      console.log('\n⚠️  Security Warnings:');
      warnings.forEach(warning => console.log(`   - ${warning}`));
      console.log('   Please change default passwords in production!\n');
    }
  }

  /**
   * Generate URLs based on appName and domain
   */
  generateUrls(appName, domain) {
    if (domain === 'localhost') {
      // localhost configuration - use ports
      return {
        frontend: 'http://localhost:3001',
        backend: 'http://localhost:8081',
        keycloak: 'http://localhost:8080',
        api: 'http://localhost:8081/api'
      };
    } else {
      // Custom domain configuration - use subdomains with HTTPS
      const protocol = 'https';

      let frontendDomain, apiDomain, authDomain;

      if (domain.includes(appName)) {
        // Domain already includes app name
        frontendDomain = domain;
        apiDomain = `api.${domain}`;
        authDomain = `auth.${domain}`;
      } else {
        // Build full domain with app name
        frontendDomain = `${appName}.${domain}`;
        apiDomain = `api.${appName}.${domain}`;
        authDomain = `auth.${appName}.${domain}`;
      }

      return {
        frontend: `${protocol}://${frontendDomain}`,
        backend: `${protocol}://${apiDomain}`,
        keycloak: `${protocol}://${authDomain}`,
        api: `${protocol}://${apiDomain}/api`
      };
    }
  }

  /**
   * Build environment variables dictionary
   */
  buildEnvVars() {
    const envVars = {};
    const appName = this.config.global.appName;
    const domain = this.config.global.domain;
    const urls = this.generateUrls(appName, domain);

    // Global settings
    envVars['Global'] = {
      'APP_NAME': appName,
      'DOMAIN': domain,
      'ENVIRONMENT': this.config.global.environment
    };

    // Auto-generated URLs
    envVars['URLs'] = {
      'FRONTEND_URL': urls.frontend,
      'BACKEND_URL': urls.backend,
      'KEYCLOAK_URL': urls.keycloak,
      'API_BASE_URL': urls.api
    };

    // Branding
    const branding = this.config.branding;
    envVars['Branding'] = {
      'PLATFORM_NAME': branding.platformName,
      'COMPANY_NAME': branding.companyName,
      'PLATFORM_DESCRIPTION': branding.platformDescription,
      'PRIMARY_COLOR': branding.colors.primary,
      'SECONDARY_COLOR': branding.colors.secondary
    };

    // Database
    const db = this.config.database.postgres;
    envVars['Database'] = {
      'POSTGRES_DB': db.app.database,
      'POSTGRES_USER': db.app.username,
      'POSTGRES_PASSWORD': db.app.password,
      'POSTGRES_ADMIN_PASSWORD': db.admin.password,
      'KC_DB_DATABASE': db.keycloak.database,
      'KC_DB_USERNAME': db.keycloak.username,
      'KC_DB_PASSWORD': db.keycloak.password
    };

    // Keycloak
    const kc = this.config.keycloak;
    envVars['Keycloak'] = {
      'KEYCLOAK_REALM': kc.realm,
      'KEYCLOAK_ADMIN': kc.admin.username,
      'KEYCLOAK_ADMIN_PASSWORD': kc.admin.password,
      'KEYCLOAK_CLIENT_ID': kc.client.clientId
    };

    // MinIO (if enabled)
    if (this.config.minio?.enabled !== false) {
      const minio = this.config.minio || {};
      envVars['MinIO'] = {
        'MINIO_ROOT_USER': minio.rootUser || 'shopmanager',
        'MINIO_ROOT_PASSWORD': minio.rootPassword || 'Min1o!SecureK3y2024#'
      };
    }

    // Kafka (if enabled)
    if (this.config.kafka?.enabled !== false) {
      const kafka = this.config.kafka || {};
      envVars['Kafka'] = {
        'KAFKA_CLUSTER_ID': kafka.clusterId || 'MkU3OEVBNTcwNTJENDM2Qk'
      };
    }

    // Business settings
    const business = this.config.business;
    envVars['Business'] = {
      'DEFAULT_CURRENCY': business.defaultCurrency,
      'DEFAULT_TAX_RATE': business.defaultTaxRate,
      'TIMEZONE': business.timezone,
      'LOCALE': business.locale
    };

    // Feature flags
    const features = this.config.features;
    envVars['Features'] = {
      'FEATURE_INVESTMENT': String(features.investment).toLowerCase(),
      'FEATURE_ANALYTICS': String(features.analytics).toLowerCase(),
      'FEATURE_FRAUD': String(features.fraud).toLowerCase(),
      'FEATURE_MULTI_CURRENCY': String(features.multiCurrency).toLowerCase(),
      'FEATURE_BARCODE_SCANNING': String(features.barcodeScanning).toLowerCase(),
      'FEATURE_LOYALTY_PROGRAM': String(features.loyaltyProgram).toLowerCase()
    };

    return envVars;
  }

  /**
   * Generate .env file
   */
  async generateEnvFile() {
    console.log('📝 Generating .env file...');

    const envVars = this.buildEnvVars();
    let content = '';

    content += '# ============================================================================\n';
    content += '# Shop Manager Environment Variables\n';
    content += '# Auto-generated from config.yaml - DO NOT EDIT MANUALLY\n';
    content += '# ============================================================================\n\n';

    for (const [section, vars] of Object.entries(envVars)) {
      content += `# ${section}\n`;
      content += '# ' + '-'.repeat(76) + '\n';
      for (const [key, value] of Object.entries(vars)) {
        content += `${key}=${value}\n`;
      }
      content += '\n';
    }

    const outputPath = path.join(this.outputDir, '.env');
    await fs.writeFile(outputPath, content, 'utf8');

    console.log(`✅ .env file generated: ${outputPath}`);
  }

  /**
   * Generate Keycloak realm JSON
   */
  async generateKeycloakRealm() {
    console.log('🔐 Generating Keycloak realm configuration...');

    const templatePath = path.join(this.templatesDir, 'keycloak-realm.json.j2');

    // Check if template exists
    if (!fsSync.existsSync(templatePath)) {
      console.log(`⚠️  Template not found: ${templatePath}, skipping...`);
      return;
    }

    try {
      const templateContent = await fs.readFile(templatePath, 'utf8');

      // Simple template variable replacement (Jinja2-like)
      let realmJson = templateContent;

      // Replace config variables
      const replacements = {
        'config.keycloak.realm': this.config.keycloak.realm,
        'config.branding.platformName': this.config.branding.platformName,
        'config.branding.companyName': this.config.branding.companyName,
        'config.keycloak.client.clientId': this.config.keycloak.client.clientId
      };

      for (const [key, value] of Object.entries(replacements)) {
        const regex = new RegExp(`{{\\s*${key}\\s*}}`, 'g');
        realmJson = realmJson.replace(regex, value);
      }

      // Parse and pretty-print JSON
      const realmData = JSON.parse(realmJson);
      const outputPath = path.join(this.outputDir, 'keycloak-realm.json');
      await fs.writeFile(outputPath, JSON.stringify(realmData, null, 2), 'utf8');

      console.log(`✅ Keycloak realm generated: ${outputPath}`);
    } catch (error) {
      console.log(`⚠️  Failed to generate Keycloak realm: ${error.message}`);
    }
  }

  /**
   * Generate Docker Compose override
   */
  async generateDockerComposeOverride() {
    console.log('🐳 Generating Docker Compose override...');

    const override = {
      version: '3.8',
      services: {}
    };

    // Disable services based on config
    if (this.config.kafka?.enabled === false) {
      override.services.kafka = {
        profiles: ['with-kafka']
      };
    }

    if (this.config.minio?.enabled === false) {
      override.services.minio = {
        profiles: ['with-minio']
      };
    }

    const outputPath = path.join(this.outputDir, 'docker-compose.override.yml');
    const yamlContent = yaml.stringify(override);

    await fs.writeFile(outputPath, yamlContent, 'utf8');
    console.log(`✅ Docker Compose override generated: ${outputPath}`);
  }

  /**
   * Generate README
   */
  async generateReadme() {
    console.log('📚 Generating README...');

    const timestamp = new Date().toISOString().replace('T', ' ').substring(0, 19);
    const domain = this.config.global.domain;

    const content = `# Shop Manager - Generated Configuration

This directory contains auto-generated configuration files for your Shop Manager installation.

## Generated Files

- \`.env\` - Environment variables for Docker Compose
- \`keycloak-realm.json\` - Keycloak realm configuration
- \`docker-compose.override.yml\` - Docker Compose customizations

## Configuration Summary

**Company:** ${this.config.branding.companyName}
**Platform:** ${this.config.branding.platformName}
**Domain:** ${domain}
**Environment:** ${this.config.global.environment}

**Test Users:** ${this.config.testUsers?.enabled ? 'Enabled' : 'Disabled'}
**Kafka:** ${this.config.kafka?.enabled !== false ? 'Enabled' : 'Disabled'}
**MinIO:** ${this.config.minio?.enabled !== false ? 'Enabled' : 'Disabled'}

## Deployment

1. **Copy generated files to project root:**
   \`\`\`bash
   cp generated/.env ../
   cp generated/keycloak-realm.json ../docker/
   cp generated/docker-compose.override.yml ../
   \`\`\`

2. **Start services:**
   \`\`\`bash
   docker compose up -d
   \`\`\`

3. **Access application:**
   - Frontend: http://${domain}:3001
   - Backend: http://${domain}:8081
   - Keycloak: http://${domain}:8080

## Security Warnings

⚠️  **IMPORTANT:** Before deploying to production:

1. Change all default passwords in \`config.yaml\`
2. Enable HTTPS/TLS certificates
3. Disable test users (\`testUsers.enabled: false\`)
4. Review and update security settings

## Support

For issues or questions, see: https://github.com/mabdulwasii/retail-manager

---
Generated: ${timestamp}
Config version: ${this.config._version || 'unknown'}
`;

    const outputPath = path.join(this.outputDir, 'README.md');
    await fs.writeFile(outputPath, content, 'utf8');
    console.log(`✅ README generated: ${outputPath}`);
  }

  /**
   * Ensure output directory exists
   */
  async ensureOutputDir() {
    try {
      await fs.access(this.outputDir);
    } catch {
      await fs.mkdir(this.outputDir, { recursive: true });
    }
  }

  /**
   * Generate all configuration files
   */
  async generateAll() {
    console.log('\n' + '='.repeat(80));
    console.log('  Shop Manager Configuration Generator (JavaScript)');
    console.log('='.repeat(80) + '\n');

    try {
      await this.loadConfig();

      if (!this.validateConfig()) {
        throw new Error('Configuration validation failed');
      }

      await this.ensureOutputDir();
      await this.generateEnvFile();
      await this.generateKeycloakRealm();
      await this.generateDockerComposeOverride();
      await this.generateReadme();

      console.log('\n' + '='.repeat(80));
      console.log('  ✅ Configuration Generation Complete!');
      console.log('='.repeat(80));
      console.log(`\nGenerated files are in: ${this.outputDir}`);
      console.log('\nNext steps:');
      console.log('  1. Review generated files');
      console.log('  2. Copy files to project root');
      console.log('  3. Run: docker compose up -d');
      console.log('\n');

      return {
        success: true,
        outputDir: this.outputDir
      };
    } catch (error) {
      console.error(`\n❌ Configuration generation failed: ${error.message}\n`);
      throw error;
    }
  }
}

/**
 * Module export for programmatic use
 */
async function generateConfig(configPath = 'config.yaml') {
  const generator = new ConfigGenerator(configPath);
  return await generator.generateAll();
}

/**
 * CLI entry point
 */
async function main() {
  const args = process.argv.slice(2);
  let configPath = 'config.yaml';
  let validateOnly = false;

  // Parse arguments
  for (let i = 0; i < args.length; i++) {
    if (args[i] === '--config' && args[i + 1]) {
      configPath = args[i + 1];
      i++;
    } else if (args[i] === '--validate-only') {
      validateOnly = true;
    }
  }

  const generator = new ConfigGenerator(configPath);

  try {
    await generator.loadConfig();

    if (validateOnly) {
      const isValid = generator.validateConfig();
      process.exit(isValid ? 0 : 1);
    } else {
      await generator.generateAll();
      process.exit(0);
    }
  } catch (error) {
    console.error(`Error: ${error.message}`);
    process.exit(1);
  }
}

// Run as CLI if called directly
if (require.main === module) {
  main();
}

// Export for use as module
module.exports = generateConfig;
module.exports.ConfigGenerator = ConfigGenerator;
