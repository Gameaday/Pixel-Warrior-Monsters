#!/bin/bash

# Pixel Warrior Monsters - Build Script
# This script helps build different versions of the APK for testing and release

set -e  # Exit on error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Functions
print_header() {
    echo -e "\n${GREEN}========================================${NC}"
    echo -e "${GREEN}$1${NC}"
    echo -e "${GREEN}========================================${NC}\n"
}

print_info() {
    echo -e "${YELLOW}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if gradlew exists
if [ ! -f "./gradlew" ]; then
    print_error "gradlew not found. Please run this script from the project root."
    exit 1
fi

# Make gradlew executable
chmod +x gradlew

# Parse command line arguments
BUILD_TYPE=${1:-help}

case $BUILD_TYPE in
    alpha|debug)
        print_header "Building Alpha/Debug APK"
        print_info "This build is for testing purposes only"
        print_info "Signed with debug keystore"
        print_info "Not optimized (larger size)"
        
        ./gradlew clean assembleDebug
        
        APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
        
        if [ -f "$APK_PATH" ]; then
            SIZE=$(du -h "$APK_PATH" | cut -f1)
            print_success "Alpha APK built successfully!"
            print_info "Location: $APK_PATH"
            print_info "Size: $SIZE"
            print_info "\nTo install on device:"
            print_info "  adb install -r $APK_PATH"
        else
            print_error "Build failed - APK not found"
            exit 1
        fi
        ;;
        
    release|production)
        print_header "Building Production Release"
        print_info "This build requires a release keystore"
        print_info "Optimized with ProGuard/R8"
        print_info "Creates both APK and AAB"
        
        ./gradlew clean assembleRelease bundleRelease
        
        APK_PATH="app/build/outputs/apk/release/app-release.apk"
        AAB_PATH="app/build/outputs/bundle/release/app-release.aab"
        
        if [ -f "$APK_PATH" ] && [ -f "$AAB_PATH" ]; then
            APK_SIZE=$(du -h "$APK_PATH" | cut -f1)
            AAB_SIZE=$(du -h "$AAB_PATH" | cut -f1)
            print_success "Release builds created successfully!"
            print_info "\nAPK (for sideloading):"
            print_info "  Location: $APK_PATH"
            print_info "  Size: $APK_SIZE"
            print_info "\nAAB (for Play Store):"
            print_info "  Location: $AAB_PATH"
            print_info "  Size: $AAB_SIZE"
        else
            print_error "Build failed - APK/AAB not found"
            exit 1
        fi
        ;;
        
    test)
        print_header "Running Tests"
        print_info "Executing unit tests and generating coverage report"
        
        ./gradlew clean test jacocoTestReport
        
        print_success "Tests completed!"
        print_info "Coverage report: app/build/reports/jacoco/test/html/index.html"
        print_info "Test results: app/build/reports/tests/testDebugUnitTest/index.html"
        ;;
        
    lint)
        print_header "Running Lint Checks"
        print_info "Analyzing code quality and potential issues"
        
        ./gradlew lint
        
        print_success "Lint checks completed!"
        print_info "Report: app/build/reports/lint-results.html"
        ;;
        
    all)
        print_header "Building All Variants"
        print_info "This will build both debug and release versions"
        
        ./gradlew clean assembleDebug assembleRelease bundleRelease
        
        print_success "All builds completed!"
        print_info "\nDebug APK: app/build/outputs/apk/debug/app-debug.apk"
        print_info "Release APK: app/build/outputs/apk/release/app-release.apk"
        print_info "Release AAB: app/build/outputs/bundle/release/app-release.aab"
        ;;
        
    clean)
        print_header "Cleaning Build Outputs"
        ./gradlew clean
        print_success "Clean completed!"
        ;;
        
    help|*)
        print_header "Pixel Warrior Monsters - Build Script"
        echo "Usage: ./build.sh [command]"
        echo ""
        echo "Commands:"
        echo "  alpha       Build debug APK for alpha testing (default)"
        echo "  debug       Alias for 'alpha'"
        echo "  release     Build production APK and AAB"
        echo "  production  Alias for 'release'"
        echo "  test        Run unit tests with coverage report"
        echo "  lint        Run code quality checks"
        echo "  all         Build all variants (debug + release)"
        echo "  clean       Clean all build outputs"
        echo "  help        Show this help message"
        echo ""
        echo "Examples:"
        echo "  ./build.sh alpha       # Build alpha APK for testing"
        echo "  ./build.sh release     # Build production APK and AAB"
        echo "  ./build.sh test        # Run all tests"
        echo ""
        echo "For more information, see:"
        echo "  - README.md"
        echo "  - ALPHA_TESTING.md"
        echo "  - releases/README.md"
        ;;
esac
