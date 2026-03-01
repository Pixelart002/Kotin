#!/bin/bash

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

echo -e "${GREEN}═══════════════════════════════════════${NC}"
echo -e "${GREEN}  UPI Voice Alert - Setup Guide${NC}"
echo -e "${GREEN}═══════════════════════════════════════${NC}"
echo ""

# Create keystore
echo -e "${YELLOW}1. Generating keystore...${NC}"
read -p "Enter keystore alias name (default: release): " KEYSTORE_ALIAS
KEYSTORE_ALIAS=${KEYSTORE_ALIAS:-release}

read -sp "Enter keystore password: " KEYSTORE_PASSWORD
echo ""
read -sp "Re-enter keystore password: " KEYSTORE_PASSWORD_CONFIRM
echo ""

if [ "$KEYSTORE_PASSWORD" != "$KEYSTORE_PASSWORD_CONFIRM" ]; then
    echo -e "${RED}Passwords do not match!${NC}"
    exit 1
fi

read -sp "Enter key alias password (can be same as keystore): " KEYSTORE_ALIAS_PASSWORD
echo ""

# Generate keystore
keytool -genkey -v \
    -keystore keystore.jks \
    -keyalg RSA \
    -keysize 2048 \
    -validity 36500 \
    -alias "$KEYSTORE_ALIAS" \
    -storepass "$KEYSTORE_PASSWORD" \
    -keypass "$KEYSTORE_ALIAS_PASSWORD" \
    -dname "CN=UPI Voice Alert,O=Personal,L=India,ST=IN,C=IN"

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✅ Keystore created successfully!${NC}"
else
    echo -e "${RED}Failed to create keystore${NC}"
    exit 1
fi

# Save to .env
echo -e "${YELLOW}2. Creating .env file...${NC}"
cat > .env << EOF
KEYSTORE_ALIAS=$KEYSTORE_ALIAS
KEYSTORE_PASSWORD=$KEYSTORE_PASSWORD
KEYSTORE_ALIAS_PASSWORD=$KEYSTORE_ALIAS_PASSWORD
KEYSTORE_PATH=keystore.jks
EOF

echo -e "${GREEN}✅ .env file created${NC}"

# Encode for GitHub
echo -e "${YELLOW}3. Encoding keystore for GitHub...${NC}"
base64 keystore.jks | tr -d '\n' > keystore_base64.txt

echo -e "${GREEN}✅ GitHub keystore encoded${NC}"
echo -e "${YELLOW}Your base64-encoded keystore is saved in: ${GREEN}keystore_base64.txt${NC}"
echo ""
echo -e "${YELLOW}To set GitHub Secrets:${NC}"
echo "1. Go to: https://github.com/Pixelart002/Kotin/settings/secrets/actions"
echo "2. Create New Secret 'KEYSTORE_CONTENT'"
echo "3. Copy content from ${GREEN}keystore_base64.txt${NC}"
echo "4. Create Secret 'KEYSTORE_ALIAS' = $KEYSTORE_ALIAS"
echo "5. Create Secret 'KEYSTORE_PASSWORD' = (your password)"
echo "6. Create Secret 'KEYSTORE_ALIAS_PASSWORD' = (your password)"
echo ""

echo -e "${YELLOW}4. Installing dependencies...${NC}"
npm install

echo -e "${GREEN}✅ Setup complete!${NC}"
echo ""
echo -e "${YELLOW}Next steps:${NC}"
echo "1. Set up GitHub Secrets with the values above"
echo "2. Run: npx cap sync android"
echo "3. Run: npm run build:prod && cd android && ./gradlew assembleRelease"
echo ""
echo -e "${GREEN}Or use the build script:${NC}"
echo "bash build.sh"
