#!/usr/bin/env python3
"""
Shop Manager Configuration Generator

This script reads config.yaml and generates all necessary configuration files
for Docker Compose deployment, including:
- .env file with environment variables
- Keycloak realm JSON from template
- Nginx configuration
- SSL certificates
- Docker Compose overrides

Usage:
    python3 generate-config.py
    python3 generate-config.py --config custom-config.yaml
    python3 generate-config.py --validate-only
"""

import argparse
import base64
import json
import os
import sys
from pathlib import Path
from typing import Any, Dict

import yaml
from jinja2 import Environment, FileSystemLoader, Template


class ConfigGenerator:
    """Generate configuration files from config.yaml"""

    def __init__(self, config_path: str = "config.yaml"):
        self.config_path = Path(config_path)
        self.root_dir = self.config_path.parent
        self.templates_dir = self.root_dir / "templates"
        self.output_dir = self.root_dir / "generated"
        self.config: Dict[str, Any] = {}

        # Ensure output directory exists
        self.output_dir.mkdir(exist_ok=True)

    def load_config(self) -> None:
        """Load and validate configuration file"""
        print(f"📖 Loading configuration from {self.config_path}")

        if not self.config_path.exists():
            print(f"❌ Configuration file not found: {self.config_path}")
            sys.exit(1)

        with open(self.config_path, 'r') as f:
            self.config = yaml.safe_load(f)

        print("✅ Configuration loaded successfully")

    def validate_config(self) -> bool:
        """Validate configuration structure and required fields"""
        print("🔍 Validating configuration...")

        required_sections = [
            'global', 'branding', 'keycloak', 'database',
            'business', 'security', 'features'
        ]

        missing_sections = []
        for section in required_sections:
            if section not in self.config:
                missing_sections.append(section)

        if missing_sections:
            print(f"❌ Missing required sections: {', '.join(missing_sections)}")
            return False

        # Validate credentials (warn if using defaults)
        self._check_default_passwords()

        print("✅ Configuration is valid")
        return True

    def _check_default_passwords(self) -> None:
        """Check if default passwords are being used"""
        warnings = []

        # Check database passwords
        db_config = self.config.get('database', {}).get('postgres', {})
        if 'P@ssw0rd!2024#Shop' in str(db_config):
            warnings.append("Database password appears to be default")

        # Check Keycloak admin password
        kc_admin = self.config.get('keycloak', {}).get('admin', {})
        if 'Adm1n!SecureP@ss2024' in str(kc_admin.get('password', '')):
            warnings.append("Keycloak admin password appears to be default")

        # Check MinIO password
        minio_config = self.config.get('minio', {})
        if 'Min1o!SecureK3y2024#' in str(minio_config.get('rootPassword', '')):
            warnings.append("MinIO password appears to be default")

        if warnings:
            print("\n⚠️  Security Warnings:")
            for warning in warnings:
                print(f"   - {warning}")
            print("   Please change default passwords in production!\n")

    def generate_env_file(self) -> None:
        """Generate .env file with all environment variables"""
        print("📝 Generating .env file...")

        env_vars = self._build_env_vars()

        env_file_path = self.output_dir / ".env"
        with open(env_file_path, 'w') as f:
            f.write("# ============================================================================\n")
            f.write("# Shop Manager Environment Variables\n")
            f.write("# Auto-generated from config.yaml - DO NOT EDIT MANUALLY\n")
            f.write("# ============================================================================\n\n")

            for section, vars_dict in env_vars.items():
                f.write(f"# {section}\n")
                f.write("# " + "-" * 76 + "\n")
                for key, value in vars_dict.items():
                    f.write(f"{key}={value}\n")
                f.write("\n")

        print(f"✅ .env file generated: {env_file_path}")

    def _generate_urls(self, app_name: str, domain: str) -> Dict[str, str]:
        """Generate URLs based on appName and domain"""
        # Determine if using custom domain or localhost
        if domain == "localhost":
            # localhost configuration - use ports
            return {
                'frontend': 'http://localhost:3001',
                'backend': 'http://localhost:8081',
                'keycloak': 'http://localhost:8080',
                'api': 'http://localhost:8081/api',
            }
        else:
            # Custom domain configuration - use subdomains with HTTPS
            protocol = 'https'  # Always use HTTPS for custom domains

            # Check if appName is already part of domain
            if app_name in domain:
                # Domain already includes app name (e.g., myshop.local)
                frontend_domain = domain
                api_domain = f"api.{domain}"
                auth_domain = f"auth.{domain}"
            else:
                # Build full domain with app name
                frontend_domain = f"{app_name}.{domain}"
                api_domain = f"api.{app_name}.{domain}"
                auth_domain = f"auth.{app_name}.{domain}"

            return {
                'frontend': f"{protocol}://{frontend_domain}",
                'backend': f"{protocol}://{api_domain}",
                'keycloak': f"{protocol}://{auth_domain}",
                'api': f"{protocol}://{api_domain}/api",
            }

    def _build_env_vars(self) -> Dict[str, Dict[str, str]]:
        """Build environment variables dictionary from config"""
        env_vars = {}

        # Generate URLs from appName and domain
        app_name = self.config['global']['appName']
        domain = self.config['global']['domain']
        urls = self._generate_urls(app_name, domain)

        # Global settings
        env_vars['Global'] = {
            'APP_NAME': app_name,
            'DOMAIN': domain,
            'ENVIRONMENT': self.config['global']['environment'],
        }

        # Auto-generated URLs
        env_vars['URLs'] = {
            'FRONTEND_URL': urls['frontend'],
            'BACKEND_URL': urls['backend'],
            'KEYCLOAK_URL': urls['keycloak'],
            'API_BASE_URL': urls['api'],
        }

        # Branding
        branding = self.config['branding']
        env_vars['Branding'] = {
            'PLATFORM_NAME': branding['platformName'],
            'COMPANY_NAME': branding['companyName'],
            'PLATFORM_DESCRIPTION': branding['platformDescription'],
            'PRIMARY_COLOR': branding['colors']['primary'],
            'SECONDARY_COLOR': branding['colors']['secondary'],
        }

        # Database
        db = self.config['database']['postgres']
        env_vars['Database'] = {
            'POSTGRES_DB': db['app']['database'],
            'POSTGRES_USER': db['app']['username'],
            'POSTGRES_PASSWORD': db['app']['password'],
            'POSTGRES_ADMIN_PASSWORD': db['admin']['password'],
            'KC_DB_DATABASE': db['keycloak']['database'],
            'KC_DB_USERNAME': db['keycloak']['username'],
            'KC_DB_PASSWORD': db['keycloak']['password'],
        }

        # Keycloak
        kc = self.config['keycloak']
        env_vars['Keycloak'] = {
            'KEYCLOAK_REALM': kc['realm'],
            'KEYCLOAK_ADMIN': kc['admin']['username'],
            'KEYCLOAK_ADMIN_PASSWORD': kc['admin']['password'],
            'KEYCLOAK_CLIENT_ID': kc['client']['clientId'],
        }

        # MinIO
        if self.config.get('minio', {}).get('enabled', True):
            minio = self.config['minio']
            env_vars['MinIO'] = {
                'MINIO_ROOT_USER': minio['rootUser'],
                'MINIO_ROOT_PASSWORD': minio['rootPassword'],
            }

        # Kafka
        if self.config.get('kafka', {}).get('enabled', True):
            kafka = self.config['kafka']
            env_vars['Kafka'] = {
                'KAFKA_CLUSTER_ID': kafka['clusterId'],
            }

        # Business settings
        business = self.config['business']
        env_vars['Business'] = {
            'DEFAULT_CURRENCY': business['defaultCurrency'],
            'DEFAULT_TAX_RATE': business['defaultTaxRate'],
            'TIMEZONE': business['timezone'],
            'LOCALE': business['locale'],
        }

        # Feature flags
        features = self.config['features']
        env_vars['Features'] = {
            'FEATURE_INVESTMENT': str(features['investment']).lower(),
            'FEATURE_ANALYTICS': str(features['analytics']).lower(),
            'FEATURE_FRAUD': str(features['fraud']).lower(),
            'FEATURE_MULTI_CURRENCY': str(features['multiCurrency']).lower(),
            'FEATURE_BARCODE_SCANNING': str(features['barcodeScanning']).lower(),
            'FEATURE_LOYALTY_PROGRAM': str(features['loyaltyProgram']).lower(),
        }

        return env_vars

    def generate_keycloak_realm(self) -> None:
        """Generate Keycloak realm JSON from template"""
        print("🔐 Generating Keycloak realm configuration...")

        template_path = self.templates_dir / "keycloak-realm.json.j2"
        if not template_path.exists():
            print(f"⚠️  Template not found: {template_path}, skipping...")
            return

        # Load template
        env = Environment(loader=FileSystemLoader(self.templates_dir))
        template = env.get_template("keycloak-realm.json.j2")

        # Render template
        realm_json = template.render(config=self.config)

        # Save rendered realm
        output_path = self.output_dir / "keycloak-realm.json"
        with open(output_path, 'w') as f:
            # Pretty print JSON
            realm_data = json.loads(realm_json)
            json.dump(realm_data, f, indent=2)

        print(f"✅ Keycloak realm generated: {output_path}")

    def generate_docker_compose_override(self) -> None:
        """Generate docker-compose.override.yml with customizations"""
        print("🐳 Generating Docker Compose override...")

        override_config = {
            'version': '3.8',
            'services': {}
        }

        # Add service configurations based on config
        if not self.config.get('kafka', {}).get('enabled', True):
            override_config['services']['kafka'] = {
                'profiles': ['with-kafka']  # Disable by default
            }

        if not self.config.get('minio', {}).get('enabled', True):
            override_config['services']['minio'] = {
                'profiles': ['with-minio']  # Disable by default
            }

        # Write override file
        output_path = self.output_dir / "docker-compose.override.yml"
        with open(output_path, 'w') as f:
            yaml.dump(override_config, f, default_flow_style=False, sort_keys=False)

        print(f"✅ Docker Compose override generated: {output_path}")

    def generate_readme(self) -> None:
        """Generate README with deployment instructions"""
        print("📚 Generating README...")

        readme_content = f"""# Shop Manager - Generated Configuration

This directory contains auto-generated configuration files for your Shop Manager installation.

## Generated Files

- `.env` - Environment variables for Docker Compose
- `keycloak-realm.json` - Keycloak realm configuration
- `docker-compose.override.yml` - Docker Compose customizations

## Configuration Summary

**Company:** {self.config['branding']['companyName']}
**Platform:** {self.config['branding']['platformName']}
**Domain:** {self.config['global']['domain']}
**Environment:** {self.config['global']['environment']}

**Test Users:** {'Enabled' if self.config.get('testUsers', {}).get('enabled', False) else 'Disabled'}
**Kafka:** {'Enabled' if self.config.get('kafka', {}).get('enabled', True) else 'Disabled'}
**MinIO:** {'Enabled' if self.config.get('minio', {}).get('enabled', True) else 'Disabled'}

## Deployment

1. **Copy generated files to project root:**
   ```bash
   cp generated/.env ../
   cp generated/keycloak-realm.json ../docker/
   cp generated/docker-compose.override.yml ../
   ```

2. **Start services:**
   ```bash
   docker compose up -d
   ```

3. **Access application:**
   - Frontend: http://{self.config['global']['domain']}:3001
   - Backend: http://{self.config['global']['domain']}:8081
   - Keycloak: http://{self.config['global']['domain']}:8080

## Security Warnings

⚠️  **IMPORTANT:** Before deploying to production:

1. Change all default passwords in `config.yaml`
2. Enable HTTPS/TLS certificates
3. Disable test users (`testUsers.enabled: false`)
4. Review and update security settings

## Support

For issues or questions, see: https://github.com/yourorg/shop-manager

---
Generated: {self._get_timestamp()}
Config version: {self.config.get('_version', 'unknown')}
"""

        output_path = self.output_dir / "README.md"
        with open(output_path, 'w') as f:
            f.write(readme_content)

        print(f"✅ README generated: {output_path}")

    def _get_timestamp(self) -> str:
        """Get current timestamp for documentation"""
        from datetime import datetime
        return datetime.now().strftime("%Y-%m-%d %H:%M:%S")

    def generate_all(self) -> None:
        """Generate all configuration files"""
        print("\n" + "="*80)
        print("  Shop Manager Configuration Generator")
        print("="*80 + "\n")

        self.load_config()

        if not self.validate_config():
            print("\n❌ Configuration validation failed. Please fix errors and try again.")
            sys.exit(1)

        self.generate_env_file()
        self.generate_keycloak_realm()
        self.generate_docker_compose_override()
        self.generate_readme()

        print("\n" + "="*80)
        print("  ✅ Configuration Generation Complete!")
        print("="*80)
        print(f"\nGenerated files are in: {self.output_dir}")
        print("\nNext steps:")
        print("  1. Review generated files")
        print("  2. Copy files to project root")
        print("  3. Run: docker compose up -d")
        print("\n")


def main():
    """Main entry point"""
    parser = argparse.ArgumentParser(
        description="Generate Shop Manager configuration files"
    )
    parser.add_argument(
        '--config',
        default='config.yaml',
        help='Path to config.yaml file (default: config.yaml)'
    )
    parser.add_argument(
        '--validate-only',
        action='store_true',
        help='Only validate configuration without generating files'
    )

    args = parser.parse_args()

    generator = ConfigGenerator(args.config)
    generator.load_config()

    if args.validate_only:
        if generator.validate_config():
            print("✅ Configuration is valid")
            sys.exit(0)
        else:
            print("❌ Configuration has errors")
            sys.exit(1)
    else:
        generator.generate_all()


if __name__ == '__main__':
    main()
