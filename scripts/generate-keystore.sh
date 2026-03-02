#!/bin/bash

# Script to generate keystore for signing APKs
# Run this locally ONCE to create your signing key

KEYSTORE_FILE="release.keystore"
VALIDITY_DAYS=10000

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}=== Android Keystore Generation ===${NC}"
echo ""
echo "This script will generate a keystore for signing your Android APK."
echo "You will be asked for several details."
echo ""

# Check if keystore already exists
if [ -f "$KEYSTORE_FILE" ]; then
    echo -e "${YELLOW}Warning: $KEYSTORE_FILE already exists!${NC}"
    read -p "Do you want to overwrite it? (y/n) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo "Aborted."
        exit 1
    fi
fi

# Prompt for keystore details
read -p "Enter Keystore Alias (e.g., upi-announcer): " ALIAS
read -sp "Enter Keystore Password: " KEYSTORE_PASSWORD
echo
read -sp "Re-enter Keystore Password: " KEYSTORE_PASSWORD_CONFIRM
echo

if [ "$KEYSTORE_PASSWORD" != "$KEYSTORE_PASSWORD_CONFIRM" ]; then
    echo -e "${YELLOW}Error: Passwords do not match!${NC}"
    exit 1
fi

read -sp "Enter Key Password (usually same as keystore password): " KEY_PASSWORD
echo

# Generate keystore
keytool -genkey -v \
    -keystore "$KEYSTORE_FILE" \
    -keyalg RSA \
    -keysize 2048 \
    -validity $VALIDITY_DAYS \
    -alias "$ALIAS" \
    -storepass "$KEYSTORE_PASSWORD" \
    -keypass "$KEY_PASSWORD"

if [ $? -eq 0 ]; then
    echo ""
    echo -e "${GREEN}✓ Keystore generated successfully!${NC}"
    echo ""
    echo "Next steps:"
    echo "1. Create a Base64 encoded version of the keystore:"
    echo "   base64 -w 0 $KEYSTORE_FILE > keystore.b64"
    echo ""
    echo "2. Add the following GitHub secrets to your repository:"
    echo "   - SIGNING_KEY: (contents of keystore.b64)"
    echo "   - SIGNING_KEY_ALIAS: $ALIAS"
    echo "   - SIGNING_KEY_PASSWORD: $KEY_PASSWORD"
    echo "   - SIGNING_KEY_STORE_PASSWORD: $KEYSTORE_PASSWORD"
    echo ""
    echo "3. Keep $KEYSTORE_FILE in a SAFE LOCATION"
    echo "   DO NOT commit to version control!"
    echo ""
    echo -e "${YELLOW}Important: Save these credentials securely!${NC}"
else
    echo -e "${YELLOW}Error: Failed to generate keystore${NC}"
    exit 1
fi
