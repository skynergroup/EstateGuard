#!/bin/bash

# Firebase Deployment Script for EstateGuard
# This script deploys all Firebase components for the EstateGuard app

set -e  # Exit on any error

echo "🔥 Starting Firebase deployment for EstateGuard..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
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

# Check if Firebase CLI is installed
if ! command -v firebase &> /dev/null; then
    print_error "Firebase CLI is not installed. Please install it first:"
    echo "npm install -g firebase-tools"
    exit 1
fi

# Check if logged in to Firebase
if ! firebase projects:list &> /dev/null; then
    print_error "Not logged in to Firebase. Please login first:"
    echo "firebase login"
    exit 1
fi

print_status "Checking Firebase project..."

# Check if firebase.json exists
if [ ! -f "firebase.json" ]; then
    print_error "firebase.json not found. Please run this script from the project root."
    exit 1
fi

# Step 1: Deploy Firestore Security Rules
print_status "Deploying Firestore security rules..."
if firebase deploy --only firestore:rules; then
    print_success "Firestore security rules deployed successfully"
else
    print_error "Failed to deploy Firestore security rules"
    exit 1
fi

# Step 2: Deploy Firestore Indexes
print_status "Deploying Firestore indexes..."
if firebase deploy --only firestore:indexes; then
    print_success "Firestore indexes deployed successfully"
else
    print_error "Failed to deploy Firestore indexes"
    exit 1
fi

# Step 3: Deploy Storage Rules
print_status "Deploying Storage security rules..."
if firebase deploy --only storage; then
    print_success "Storage security rules deployed successfully"
else
    print_error "Failed to deploy Storage security rules"
    exit 1
fi

# Step 4: Deploy Cloud Functions
print_status "Installing Cloud Functions dependencies..."
if [ -d "firebase/functions" ]; then
    cd firebase/functions
    if npm install; then
        print_success "Cloud Functions dependencies installed"
    else
        print_error "Failed to install Cloud Functions dependencies"
        exit 1
    fi
    cd ../..
    
    print_status "Deploying Cloud Functions..."
    if firebase deploy --only functions; then
        print_success "Cloud Functions deployed successfully"
    else
        print_error "Failed to deploy Cloud Functions"
        exit 1
    fi
else
    print_warning "Cloud Functions directory not found, skipping..."
fi

# Step 5: Initialize Database (optional)
read -p "Do you want to initialize the database with sample data? (y/N): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    print_status "Initializing database with sample data..."
    
    # Check if service account key exists
    if [ ! -f "firebase/service-account-key.json" ]; then
        print_error "Service account key not found at firebase/service-account-key.json"
        print_warning "Please download it from Firebase Console > Project Settings > Service Accounts"
        print_warning "Skipping database initialization..."
    else
        cd firebase
        if node firestore-init.js; then
            print_success "Database initialized with sample data"
        else
            print_error "Failed to initialize database"
        fi
        cd ..
    fi
fi

# Step 6: Verify deployment
print_status "Verifying deployment..."

# Check if rules are active
print_status "Checking Firestore rules..."
if firebase firestore:rules:list &> /dev/null; then
    print_success "Firestore rules are active"
else
    print_warning "Could not verify Firestore rules"
fi

# Check if functions are deployed
print_status "Checking Cloud Functions..."
if firebase functions:list &> /dev/null; then
    print_success "Cloud Functions are active"
else
    print_warning "Could not verify Cloud Functions"
fi

# Step 7: Display important information
echo
echo "🎉 Firebase deployment completed!"
echo
echo "📋 Next Steps:"
echo "1. Test your Android app with the deployed Firebase backend"
echo "2. Verify authentication works correctly"
echo "3. Test Firestore read/write operations"
echo "4. Test file upload to Storage"
echo "5. Test push notifications"
echo
echo "🔧 Useful Commands:"
echo "• View logs: firebase functions:log"
echo "• Test locally: firebase emulators:start"
echo "• Monitor: firebase console"
echo
echo "📱 Android App Configuration:"
echo "• Ensure google-services.json is in app/ directory"
echo "• Verify Firebase dependencies in build.gradle.kts"
echo "• Test permission flows and data operations"
echo

# Step 8: Optional - Open Firebase Console
read -p "Do you want to open Firebase Console? (y/N): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    firebase open
fi

print_success "Firebase deployment script completed successfully!"
echo "🚀 Your EstateGuard Firebase backend is ready!"
