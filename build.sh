#!/bin/bash
set -e

echo "🚀 Building UPI Voice Alert APK..."

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check prerequisites
echo -e "${YELLOW}Checking prerequisites...${NC}"

if ! command -v node &> /dev/null; then
    echo -e "${RED}Node.js is required but not installed.${NC}"
    exit 1
fi

if ! command -v gradle &> /dev/null && ! [ -f "android/gradlew" ]; then
    echo -e "${RED}Gradle is required.${NC}"
    exit 1
fi

# Install dependencies
echo -e "${YELLOW}Installing dependencies...${NC}"
npm ci

# Build web assets
echo -e "${YELLOW}Building web assets...${NC}"
npm run build:prod

# Sync Capacitor
echo -e "${YELLOW}Syncing Capacitor...${NC}"
npx cap sync android

# Check for keystore
if [ ! -f "keystore.jks" ]; then
    echo -e "${YELLOW}Keystore not found. Building debug APK...${NC}"
    cd android
    ./gradlew assembleDebug
    echo -e "${GREEN}✅ Debug APK built successfully!${NC}"
    echo -e "${GREEN}APK Path: android/app/build/outputs/apk/debug/app-debug.apk${NC}"
else
    echo -e "${YELLOW}Keystore found. Building release APK...${NC}"
    
    # Set environment variables if not already set
    if [ -z "$KEYSTORE_ALIAS" ]; then
        read -p "Enter keystore alias (default: release): " KEYSTORE_ALIAS
        KEYSTORE_ALIAS=${KEYSTORE_ALIAS:-release}
    fi
    
    if [ -z "$KEYSTORE_PASSWORD" ]; then
        read -sp "Enter keystore password: " KEYSTORE_PASSWORD
        echo
    fi
    
    if [ -z "$KEYSTORE_ALIAS_PASSWORD" ]; then
        read -sp "Enter key alias password: " KEYSTORE_ALIAS_PASSWORD
        echo
    fi
    
    export KEYSTORE_ALIAS
    export KEYSTORE_PASSWORD
    export KEYSTORE_ALIAS_PASSWORD
    export KEYSTORE_PATH=keystore.jks
    
    cd android
    ./gradlew assembleRelease bundleRelease
    
    echo -e "${GREEN}✅ Release APK built successfully!${NC}"
    echo -e "${GREEN}APK Path: android/app/build/outputs/apk/release/app-release.apk${NC}"
    echo -e "${GREEN}AAB Path: android/app/build/outputs/bundle/release/app-release.aab${NC}"
fi

echo -e "${GREEN}✅ Build complete!${NC}"
