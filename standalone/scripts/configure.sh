#!/bin/bash

# Shop Manager - Interactive Configuration Wizard
# This script helps clients configure Shop Manager with their business details

set -e

# Colors for better UX
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# Functions
print_header() {
    echo -e "${BLUE}============================================${NC}"
    echo -e "${BLUE}  $1${NC}"
    echo -e "${BLUE}============================================${NC}"
    echo
}

print_error() {
    echo -e "${RED}❌ Error: $1${NC}"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_info() {
    echo -e "${YELLOW}ℹ️  $1${NC}"
}

validate_email() {
    local email="$1"
    if [[ "$email" =~ ^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$ ]]; then
        return 0
    else
        return 1
    fi
}

validate_password() {
    local password="$1"
    local length=${#password}

    if [ $length -lt 8 ]; then
        print_error "Password must be at least 8 characters long"
        return 1
    fi

    if ! [[ "$password" =~ [A-Z] ]]; then
        print_error "Password must contain at least one uppercase letter"
        return 1
    fi

    if ! [[ "$password" =~ [a-z] ]]; then
        print_error "Password must contain at least one lowercase letter"
        return 1
    fi

    if ! [[ "$password" =~ [0-9] ]]; then
        print_error "Password must contain at least one digit"
        return 1
    fi

    return 0
}

read_input() {
    local prompt="$1"
    local default="$2"
    local result

    if [ -n "$default" ]; then
        read -p "$prompt [$default]: " result
        echo "${result:-$default}"
    else
        read -p "$prompt: " result
        echo "$result"
    fi
}

read_password() {
    local prompt="$1"
    local password

    read -s -p "$prompt: " password
    echo
    echo "$password"
}

# Main Configuration Wizard
main() {
    clear
    print_header "Shop Manager - Configuration Wizard"

    echo "This wizard will help you configure Shop Manager"
    echo "with your business details."
    echo
    read -p "Press Enter to continue..."

    # Step 1: Company Information
    clear
    print_header "Step 1: Company Information"

    while true; do
        COMPANY_NAME=$(read_input "Company Name")
        if [ -n "$COMPANY_NAME" ]; then
            break
        fi
        print_error "Company name cannot be empty"
    done

    PLATFORM_NAME=$(read_input "Platform Name" "${COMPANY_NAME} Retail Manager")

    # Step 2: Administrator Account
    echo
    print_header "Step 2: Administrator Account"

    while true; do
        ADMIN_EMAIL=$(read_input "Administrator Email")
        if validate_email "$ADMIN_EMAIL"; then
            break
        fi
        print_error "Invalid email format"
    done

    echo
    print_info "Password Requirements:"
    echo "  - Minimum 8 characters"
    echo "  - At least one uppercase letter"
    echo "  - At least one lowercase letter"
    echo "  - At least one digit"
    echo

    while true; do
        ADMIN_PASSWORD=$(read_password "Administrator Password")

        if ! validate_password "$ADMIN_PASSWORD"; then
            continue
        fi

        ADMIN_PASSWORD_CONFIRM=$(read_password "Confirm Password")

        if [ "$ADMIN_PASSWORD" == "$ADMIN_PASSWORD_CONFIRM" ]; then
            break
        fi
        print_error "Passwords do not match"
    done

    # Step 3: Business Settings
    echo
    print_header "Step 3: Business Settings"

    echo "Select your business currency:"
    echo "1) USD - US Dollar"
    echo "2) EUR - Euro"
    echo "3) GBP - British Pound"
    echo "4) NGN - Nigerian Naira"
    echo

    while true; do
        CURRENCY_CHOICE=$(read_input "Select currency (1-4)")

        case $CURRENCY_CHOICE in
            1)
                CURRENCY="USD"
                CURRENCY_NAME="US Dollar"
                break
                ;;
            2)
                CURRENCY="EUR"
                CURRENCY_NAME="Euro"
                break
                ;;
            3)
                CURRENCY="GBP"
                CURRENCY_NAME="British Pound"
                break
                ;;
            4)
                CURRENCY="NGN"
                CURRENCY_NAME="Nigerian Naira"
                break
                ;;
            *)
                print_error "Invalid choice"
                ;;
        esac
    done

    echo
    CUSTOM_DOMAIN=$(read_input "Custom domain" "localhost")

    # Configuration Summary
    echo
    print_header "Configuration Summary"

    echo "Company Name:     $COMPANY_NAME"
    echo "Platform Name:    $PLATFORM_NAME"
    echo "Admin Email:      $ADMIN_EMAIL"
    echo "Currency:         $CURRENCY_NAME ($CURRENCY)"
    echo "Domain:           $CUSTOM_DOMAIN"
    echo
    echo "============================================"
    echo

    read -p "Is this information correct? (Y/N): " CONFIRM
    if [[ ! "$CONFIRM" =~ ^[Yy]$ ]]; then
        echo
        print_error "Configuration cancelled. Please run this script again."
        exit 1
    fi

    # Generate Configuration
    echo
    print_info "Generating configuration files..."
    echo

    # Check if Python is available
    if ! command -v python3 &> /dev/null; then
        print_error "Python 3 is required but not found"
        echo
        echo "Please install Python 3:"
        if [[ "$OSTYPE" == "darwin"* ]]; then
            echo "  brew install python3"
        elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
            echo "  sudo apt-get install python3  # Debian/Ubuntu"
            echo "  sudo dnf install python3      # Fedora"
        fi
        exit 1
    fi

    # Run Python config generator
    if python3 generate-client-config.py \
        --company "$COMPANY_NAME" \
        --platform "$PLATFORM_NAME" \
        --email "$ADMIN_EMAIL" \
        --password "$ADMIN_PASSWORD" \
        --currency "$CURRENCY" \
        --domain "$CUSTOM_DOMAIN"; then

        echo
        print_header "Configuration Complete!"
        echo
        echo "Your Shop Manager is now configured with:"
        echo "  Company: $COMPANY_NAME"
        echo "  Email:   $ADMIN_EMAIL"
        echo
        echo "Next step: Run install.sh to install Shop Manager"
        echo

        # Ask if user wants to install now
        read -p "Would you like to install Shop Manager now? (Y/N): " INSTALL_NOW
        if [[ "$INSTALL_NOW" =~ ^[Yy]$ ]]; then
            echo
            print_info "Starting installation..."
            cd "$SCRIPT_DIR/.."
            ./install.sh
        else
            echo
            print_info "To install later, run: ./install.sh"
            echo
        fi
    else
        echo
        print_error "Configuration generation failed"
        exit 1
    fi
}

# Run main function
main
