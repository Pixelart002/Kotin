#!/bin/bash

set -e

echo "🔎 Checking for package.json..."

if [ ! -f "package.json" ]; then
  echo "❌ package.json not found in current directory."
  echo "Run this script from your project root."
  exit 1
fi

echo "📦 Installing dependencies and generating package-lock.json..."
npm install

if [ ! -f "package-lock.json" ]; then
  echo "❌ Failed to generate package-lock.json"
  exit 1
fi

echo "📝 Adding package-lock.json to git..."
git add package-lock.json

echo "💾 Committing changes..."
git commit -m "Add package-lock.json for GitHub Actions CI"

echo "🚀 Pushing to main branch..."
git push origin main

echo "✅ Done. Lock file generated and pushed successfully."