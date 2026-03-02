#!/bin/bash

# UPI Announcer - Complete Setup Script
# This script installs all dependencies and sets up the project

set -e  # Exit on error

echo "========================================="
echo "🚀 UPI Announcer - Complete Setup Script"
echo "========================================="
echo ""

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_message() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if running on macOS or Linux
check_os() {
    print_message "Checking operating system..."
    if [[ "$OSTYPE" == "darwin"* ]]; then
        OS="macos"
        print_success "Detected macOS"
    elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
        OS="linux"
        print_success "Detected Linux"
    else
        OS="windows"
        print_warning "Detected Windows. Some commands may need to be run manually."
    fi
}

# Check and install Homebrew (for macOS)
install_homebrew() {
    if [[ "$OS" == "macos" ]]; then
        if ! command -v brew &> /dev/null; then
            print_message "Installing Homebrew..."
            /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
            print_success "Homebrew installed"
        else
            print_success "Homebrew already installed"
        fi
    fi
}

# Install Node.js and npm
install_node() {
    print_message "Checking Node.js installation..."
    
    if ! command -v node &> /dev/null; then
        print_message "Installing Node.js..."
        if [[ "$OS" == "macos" ]]; then
            brew install node@20
        elif [[ "$OS" == "linux" ]]; then
            curl -fsSL https://deb.nodesource.com/setup_20.x | sudo -E bash -
            sudo apt-get install -y nodejs
        fi
        print_success "Node.js installed"
    else
        NODE_VERSION=$(node -v)
        print_success "Node.js already installed: $NODE_VERSION"
    fi
    
    # Check if npm is installed
    if ! command -v npm &> /dev/null; then
        print_error "npm not found. Please install npm manually."
        exit 1
    fi
    
    # Update npm to latest
    print_message "Updating npm to latest version..."
    npm install -g npm@latest
    print_success "npm updated"
}

# Install Java (required for Android)
install_java() {
    print_message "Checking Java installation..."
    
    if ! command -v java &> /dev/null; then
        print_message "Installing Java 17..."
        if [[ "$OS" == "macos" ]]; then
            brew install openjdk@17
        elif [[ "$OS" == "linux" ]]; then
            sudo apt-get update
            sudo apt-get install -y openjdk-17-jdk
        fi
        print_success "Java installed"
    else
        JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2)
        print_success "Java already installed: $JAVA_VERSION"
    fi
    
    # Set JAVA_HOME
    if [[ "$OS" == "macos" ]]; then
        export JAVA_HOME=$(/usr/libexec/java_home -v 17)
    elif [[ "$OS" == "linux" ]]; then
        export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
    fi
    
    echo "export JAVA_HOME=$JAVA_HOME" >> ~/.bashrc
    print_success "JAVA_HOME set to $JAVA_HOME"
}

# Install Android SDK
install_android_sdk() {
    print_message "Checking Android SDK..."
    
    if [[ -z "$ANDROID_HOME" ]]; then
        if [[ "$OS" == "macos" ]]; then
            ANDROID_HOME="$HOME/Library/Android/sdk"
        elif [[ "$OS" == "linux" ]]; then
            ANDROID_HOME="$HOME/Android/Sdk"
        fi
    fi
    
    if [[ ! -d "$ANDROID_HOME" ]]; then
        print_message "Installing Android SDK Command Line Tools..."
        
        # Create Android SDK directory
        mkdir -p "$ANDROID_HOME/cmdline-tools"
        
        # Download command line tools
        if [[ "$OS" == "macos" ]]; then
            curl -o cmdline-tools.zip https://dl.google.com/android/repository/commandlinetools-mac-9477386_latest.zip
        elif [[ "$OS" == "linux" ]]; then
            curl -o cmdline-tools.zip https://dl.google.com/android/repository/commandlinetools-linux-9477386_latest.zip
        fi
        
        # Extract
        unzip cmdline-tools.zip -d "$ANDROID_HOME/cmdline-tools"
        mv "$ANDROID_HOME/cmdline-tools/cmdline-tools" "$ANDROID_HOME/cmdline-tools/latest"
        rm cmdline-tools.zip
        
        # Add to PATH
        export PATH="$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools"
        echo "export ANDROID_HOME=$ANDROID_HOME" >> ~/.bashrc
        echo 'export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools' >> ~/.bashrc
        
        print_success "Android SDK installed at $ANDROID_HOME"
    else
        print_success "Android SDK already installed at $ANDROID_HOME"
    fi
    
    # Install required Android components
    print_message "Installing required Android components..."
    yes | sdkmanager --licenses
    sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0"
    print_success "Android components installed"
}

# Install project dependencies
install_project_deps() {
    print_message "Installing project dependencies..."
    
    # Install dependencies from package.json
    npm install
    
    # Install global packages
    print_message "Installing global packages..."
    npm install -g @capacitor/cli @ionic/cli
    
    print_success "Project dependencies installed"
}

# Add Android platform
add_android_platform() {
    print_message "Adding Android platform..."
    
    # Build web app first
    npm run build
    npm run build:prod
    
    # Add Android platform
    npx cap add android
    
    print_success "Android platform added"
}

# Create necessary configuration files if they don't exist
create_config_files() {
    print_message "Creating configuration files if needed..."
    
    # Create capacitor.config.js if it doesn't exist
    if [[ ! -f "capacitor.config.js" ]] && [[ ! -f "capacitor.config.ts" ]]; then
        cat > capacitor.config.js << 'EOF'
const config = {
  appId: 'com.yourname.upiannouncer',
  appName: 'UPI Announcer',
  webDir: 'dist',
  bundledWebRuntime: false,
  plugins: {
    TextToSpeech: {
      displayName: 'Text to Speech',
      description: 'Announce UPI transactions'
    },
    Notification: {
      displayName: 'Notifications',
      description: 'Listen for UPI transaction notifications'
    }
  },
  server: {
    androidScheme: 'https',
    cleartext: true,
    allowNavigation: ['*']
  },
  android: {
    allowMixedContent: true,
    captureInput: true,
    webContentsDebuggingEnabled: true
  }
};

module.exports = config;
EOF
        print_success "capacitor.config.js created"
    fi
    
    # Create .env file
    if [[ ! -f ".env" ]]; then
        cat > .env << 'EOF'
# App Configuration
APP_ID=com.yourname.upiannouncer
APP_NAME=UPI Announcer

# Build Configuration
BUILD_TYPE=debug
EOF
        print_success ".env file created"
    fi
}

# Setup Git hooks
setup_git_hooks() {
    print_message "Setting up Git hooks..."
    
    # Create .husky directory
    mkdir -p .husky
    
    # Pre-commit hook
    cat > .husky/pre-commit << 'EOF'
#!/bin/sh
. "$(dirname "$0")/_/husky.sh"

npm run lint
npm run format
EOF
    
    # Make hooks executable
    chmod +x .husky/pre-commit
    
    print_success "Git hooks set up"
}

# Verify installation
verify_installation() {
    print_message "Verifying installation..."
    
    errors=0
    
    # Check Node.js
    if command -v node &> /dev/null; then
        print_success "✓ Node.js: $(node -v)"
    else
        print_error "✗ Node.js not found"
        errors=$((errors+1))
    fi
    
    # Check npm
    if command -v npm &> /dev/null; then
        print_success "✓ npm: $(npm -v)"
    else
        print_error "✗ npm not found"
        errors=$((errors+1))
    fi
    
    # Check Java
    if command -v java &> /dev/null; then
        print_success "✓ Java: $(java -version 2>&1 | head -n 1)"
    else
        print_error "✗ Java not found"
        errors=$((errors+1))
    fi
    
    # Check Android SDK
    if [[ -d "$ANDROID_HOME" ]]; then
        print_success "✓ Android SDK: $ANDROID_HOME"
    else
        print_warning "✗ Android SDK not found"
        errors=$((errors+1))
    fi
    
    # Check Capacitor
    if command -v cap &> /dev/null; then
        print_success "✓ Capacitor CLI installed"
    else
        print_error "✗ Capacitor CLI not found"
        errors=$((errors+1))
    fi
    
    # Check Android platform
    if [[ -d "android" ]]; then
        print_success "✓ Android platform added"
    else
        print_warning "✗ Android platform not added"
    fi
    
    if [[ $errors -eq 0 ]]; then
        print_success "✅ All dependencies installed successfully!"
    else
        print_warning "⚠️  Some dependencies are missing ($errors errors)"
    fi
}

# Main setup function
main() {
    print_message "Starting UPI Announcer setup..."
    echo ""
    
    # Check OS
    check_os
    
    # Install everything
    install_homebrew
    install_node
    install_java
    install_android_sdk
    install_project_deps
    create_config_files
    add_android_platform
    setup_git_hooks
    
    echo ""
    print_success "✨ Setup completed successfully!"
    echo ""
    
    # Verify
    verify_installation
    
    echo ""
    echo "========================================="
    echo "📱 Next Steps:"
    echo "========================================="
    echo ""
    echo "1. To run in development:"
    echo "   npm start"
    echo ""
    echo "2. To build for Android:"
    echo "   npm run build"
    echo "   npm run build:prod"
    echo "   npx cap sync android"
    echo ""
    echo "3. To open in Android Studio:"
    echo "   npx cap open android"
    echo ""
    echo "4. To build APK:"
    echo "   cd android && ./gradlew assembleDebug"
    echo ""
    echo "========================================="
}

# Run main function
main