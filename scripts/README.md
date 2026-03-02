# CI/CD Configuration File

This directory contains helper scripts for setting up CI/CD signing keys.

## Files

- `generate-keystore.sh` - Generate a new signing keystore
- `encode-keystore.sh` - Encode keystore to Base64 for GitHub secrets

## Quick Start

### 1. Generate Keystore (Do this ONCE)

```bash
chmod +x scripts/generate-keystore.sh
./scripts/generate-keystore.sh
```

This will prompt you for:
- Keystore Alias (e.g., `upi-announcer`)
- Keystore Password
- Key Password

### 2. Encode to Base64

```bash
chmod +x scripts/encode-keystore.sh
./scripts/encode-keystore.sh release.keystore
```

This outputs the Base64-encoded keystore that you'll use for GitHub secrets.

### 3. Add to GitHub Secrets

Go to your GitHub repository:
1. Settings → Secrets and variables → Actions
2. Create new secrets:
   - **SIGNING_KEY** - Paste the Base64 output from step 2
   - **SIGNING_KEY_ALIAS** - Your alias (e.g., `upi-announcer`)
   - **SIGNING_KEY_PASSWORD** - Your key password
   - **SIGNING_KEY_STORE_PASSWORD** - Your keystore password

## Security Notes

⚠️ **IMPORTANT:**
- Never commit `release.keystore` or `keystore.b64` to version control
- Add them to `.gitignore` (already configured)
- Keep passwords in a secure location
- Rotate keys periodically for production apps
- Use strong passwords

## Verify Keystore

To verify your keystore contents:

```bash
keytool -list -v -keystore release.keystore
```

You'll be prompted for the keystore password.
