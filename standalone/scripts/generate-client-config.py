#!/usr/bin/env python3
"""
Generate Shop Manager configuration from command-line arguments.
Used by interactive installation scripts to create config.yaml.
"""

import argparse
import sys
import yaml
from pathlib import Path

def generate_config(args):
    """Generate config.yaml from command-line arguments."""

    # Load base config template
    base_config_path = Path(__file__).parent.parent / 'config.yaml'

    if not base_config_path.exists():
        print(f"Error: Base config not found at {base_config_path}")
        sys.exit(1)

    with open(base_config_path, 'r') as f:
        config = yaml.safe_load(f)

    # Update with client-provided values
    if args.company:
        config['branding']['companyName'] = args.company

    if args.platform:
        config['branding']['platformName'] = args.platform
    elif args.company:
        # Default platform name if not provided
        config['branding']['platformName'] = f"{args.company} Retail Manager"

    if args.email:
        # Update admin user email
        if 'testUsers' in config and config['testUsers']['enabled']:
            for user in config['testUsers']['users']:
                if user['role'] == 'TENANT_ADMIN':
                    user['username'] = args.email
                    user['email'] = args.email
                    break

    if args.password:
        # Update admin password
        if 'keycloak' in config:
            config['keycloak']['admin']['password'] = args.password

        # Also update test user password
        if 'testUsers' in config and config['testUsers']['enabled']:
            for user in config['testUsers']['users']:
                if user['role'] == 'TENANT_ADMIN':
                    user['password'] = args.password
                    break

    if args.currency:
        config['business']['defaultCurrency'] = args.currency.upper()

    if args.domain:
        config['global']['domain'] = args.domain
        if 'certificates' in config:
            config['certificates']['commonName'] = args.domain

    if args.timezone:
        config['business']['timezone'] = args.timezone

    if args.locale:
        config['business']['locale'] = args.locale

    # Write updated config
    output_path = base_config_path
    with open(output_path, 'w') as f:
        yaml.dump(config, f, default_flow_style=False, sort_keys=False)

    print(f"✅ Configuration saved to {output_path}")
    print("\nConfiguration Summary:")
    print(f"  Company: {config['branding']['companyName']}")
    print(f"  Platform: {config['branding']['platformName']}")
    if 'testUsers' in config and config['testUsers']['enabled']:
        admin_user = next((u for u in config['testUsers']['users'] if u['role'] == 'TENANT_ADMIN'), None)
        if admin_user:
            print(f"  Admin Email: {admin_user['email']}")
    print(f"  Currency: {config['business']['defaultCurrency']}")
    print(f"  Domain: {config['global']['domain']}")

    return True

def validate_password(password):
    """Validate password meets minimum requirements."""
    if len(password) < 8:
        return False, "Password must be at least 8 characters long"
    if not any(c.isupper() for c in password):
        return False, "Password must contain at least one uppercase letter"
    if not any(c.islower() for c in password):
        return False, "Password must contain at least one lowercase letter"
    if not any(c.isdigit() for c in password):
        return False, "Password must contain at least one digit"
    return True, "Password is valid"

def main():
    parser = argparse.ArgumentParser(
        description='Generate Shop Manager configuration from command-line arguments',
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog='''
Examples:
  # Basic configuration
  python generate-client-config.py --company "ACME Corp" --email "admin@acme.com" --password "SecurePass123"

  # Full configuration
  python generate-client-config.py \\
    --company "ACME Retail" \\
    --platform "ACME POS System" \\
    --email "admin@acme.com" \\
    --password "SecurePass123!" \\
    --currency USD \\
    --domain "retail.acme.local" \\
    --timezone "America/New_York"
        '''
    )

    parser.add_argument(
        '--company',
        help='Company name (e.g., "ACME Corporation")',
        required=True
    )

    parser.add_argument(
        '--platform',
        help='Platform/application name (e.g., "ACME Retail Manager"). Defaults to "{company} Retail Manager"',
        default=None
    )

    parser.add_argument(
        '--email',
        help='Admin email address',
        required=True
    )

    parser.add_argument(
        '--password',
        help='Admin password (min 8 characters, must include uppercase, lowercase, and digit)',
        required=True
    )

    parser.add_argument(
        '--currency',
        help='Default currency code (USD, EUR, GBP, NGN)',
        choices=['USD', 'EUR', 'GBP', 'NGN'],
        default='USD'
    )

    parser.add_argument(
        '--domain',
        help='Domain name (e.g., "shop.company.com"). Defaults to "localhost"',
        default='localhost'
    )

    parser.add_argument(
        '--timezone',
        help='Timezone (e.g., "America/New_York", "Europe/London")',
        default='UTC'
    )

    parser.add_argument(
        '--locale',
        help='Locale (e.g., "en_US", "en_GB")',
        default='en_US'
    )

    parser.add_argument(
        '--validate-only',
        action='store_true',
        help='Only validate inputs without generating config'
    )

    args = parser.parse_args()

    # Validate password
    is_valid, message = validate_password(args.password)
    if not is_valid:
        print(f"❌ Error: {message}")
        sys.exit(1)

    if args.validate_only:
        print("✅ All inputs are valid")
        return 0

    # Generate configuration
    try:
        success = generate_config(args)
        return 0 if success else 1
    except Exception as e:
        print(f"❌ Error generating configuration: {e}")
        import traceback
        traceback.print_exc()
        return 1

if __name__ == '__main__':
    sys.exit(main())
