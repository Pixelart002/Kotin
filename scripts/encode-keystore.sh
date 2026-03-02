#!/bin/bash

# Script to encode keystore to Base64 for GitHub secrets
# Run this after generate-keystore.sh

KEYSTORE_FILE="${1:-release.keystore}"

if [ ! -f "$KEYSTORE_FILE" ]; then
    echo "Error: Keystore file '$KEYSTORE_FILE' not found!"
    echo "Usage: $0 [keystore-file]"
    exit 1
fi

echo "Encoding $KEYSTORE_FILE to Base64..."
echo ""

# Encode to Base64
base64 -w 0 "$KEYSTORE_FILE" > keystore.b64

if [ $? -eq 0 ]; then
    echo "✓ Encoded successfully!"
    echo ""
    echo "Keystore Base64 (for SIGNING_KEY secret):"
    echo "---"
    cat keystore.b64
    echo ""
    echo "---"
    echo ""
    echo "Also saved to: keystore.b64"
    echo ""
    echo "Step: Add this to your GitHub repository secrets as 'SIGNING_KEY'"
else
    echo "Error: Failed to encode keystore"
    exit 1
fi
