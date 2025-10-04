# Pixel Warrior Monsters - Release Builds

This directory contains release builds for different deployment stages.

## Directory Structure

### `/alpha` - Alpha Testing Builds
Development builds for internal alpha testing. These APKs are:
- Signed with debug keystore for easy installation
- Include debug symbols for crash reporting
- Not optimized with ProGuard/R8
- Suitable for testing all features on physical devices

**To download the latest alpha build:**
1. Go to the [GitHub Actions](https://github.com/Gameaday/Pixel-Warrior-Monsters/actions) page
2. Click on the latest successful "CI/CD Pipeline - Testing and Build" workflow run
3. Scroll down to "Artifacts" section
4. Download `development-apk`

### `/production` - Production Release Builds
Production builds ready for Google Play Store submission. These include:
- Signed release AAB (Android App Bundle)
- Signed release APK
- ProGuard/R8 optimized and minified
- All Play Store compliance requirements met

**To download production builds:**
1. Go to the [GitHub Actions](https://github.com/Gameaday/Pixel-Warrior-Monsters/actions) page
2. Click on the latest successful "CI/CD Pipeline - Testing and Build" workflow run
3. Scroll down to "Artifacts" section
4. Download `production-bundle`

Or go to the [Releases](https://github.com/Gameaday/Pixel-Warrior-Monsters/releases) page for tagged versions.

## Building Locally

### Development/Alpha APK
```bash
./gradlew assembleDebug
```
Output: `app/build/outputs/apk/debug/app-debug.apk`

### Production Release (requires signing configuration)
```bash
./gradlew assembleRelease bundleRelease
```
Output: 
- APK: `app/build/outputs/apk/release/app-release.apk`
- AAB: `app/build/outputs/bundle/release/app-release.aab`

## Installation Instructions

### For Alpha Testers (Android Device)
1. Download the `app-debug.apk` file
2. Enable "Install from Unknown Sources" in your device settings
3. Tap the APK file to install
4. Launch "Pixel Warrior Monsters" from your app drawer

### For Play Store Beta Testing
Production builds will be distributed through Google Play Console's internal/beta testing tracks once configured.

## Current Build Status

✅ **CI/CD Pipeline**: Fully automated with GitHub Actions
✅ **Debug Builds**: Available for alpha testing
✅ **Release Builds**: Generated but require production keystore for Play Store
✅ **Tests**: All unit tests passing (200+ tests)
✅ **Code Quality**: Lint checks and security scanning enabled

## Version Information

Current version: `1.0.0` (versionCode: 1)

See [DEPLOYMENT_STATUS.md](../DEPLOYMENT_STATUS.md) for detailed release roadmap.
