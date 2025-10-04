# Artifact Repository - CI/CD Build Artifacts

This document describes the automated artifact generation system for Pixel Warrior Monsters.

## Overview

Every push to the `main` or `develop` branch automatically triggers the CI/CD pipeline, which:
1. Runs all 200+ unit tests
2. Performs lint checks and code quality scans
3. Builds both development and production artifacts
4. Uploads artifacts to GitHub Actions for download

## Available Artifacts

### 1. Development APK (`development-apk`)

**Purpose**: Alpha testing and development

**Contents**:
- `app-debug.apk` - Debug build signed with debug keystore

**Details**:
- **Size**: ~23MB (includes debug symbols)
- **Signing**: Debug keystore (auto-generated)
- **Optimization**: None (easier debugging)
- **Target**: Android 7.0+ (API 24)
- **Retention**: 90 days

**Use Cases**:
- Alpha testing on physical devices
- Internal development testing
- Feature validation
- Bug reproduction

**Download**:
1. Go to [GitHub Actions](https://github.com/Gameaday/Pixel-Warrior-Monsters/actions)
2. Click latest "CI/CD Pipeline - Testing and Build" workflow
3. Scroll to "Artifacts" section
4. Download `development-apk`
5. Extract ZIP and install the APK

### 2. Production Bundle (`production-bundle`)

**Purpose**: Google Play Store submission and production testing

**Contents**:
- `app-release.apk` - Release APK for sideloading (~3MB)
- `app-release.aab` - Android App Bundle for Play Store (~4MB)
- `mapping.txt` - ProGuard/R8 mapping file (for crash reports)
- `README.md` - Installation and submission instructions

**Details**:
- **Size**: 3-4MB (ProGuard/R8 optimized)
- **Signing**: Release keystore (when configured)
- **Optimization**: Full ProGuard/R8 optimization
- **Target**: Android 7.0+ (API 24)
- **Retention**: 180 days

**Use Cases**:
- Google Play Store submission
- Production release testing
- Beta/Alpha track distribution
- Final QA validation

**Download**:
1. Go to [GitHub Actions](https://github.com/Gameaday/Pixel-Warrior-Monsters/actions)
2. Click latest "CI/CD Pipeline - Testing and Build" workflow
3. Scroll to "Artifacts" section
4. Download `production-bundle`
5. Extract ZIP to access all files

## CI/CD Status Badges

The following badges show the current status of the CI/CD pipeline:

[![CI/CD Pipeline](https://github.com/Gameaday/Pixel-Warrior-Monsters/actions/workflows/main.yml/badge.svg)](https://github.com/Gameaday/Pixel-Warrior-Monsters/actions/workflows/main.yml)
[![Code Quality](https://github.com/Gameaday/Pixel-Warrior-Monsters/actions/workflows/security.yml/badge.svg)](https://github.com/Gameaday/Pixel-Warrior-Monsters/actions/workflows/security.yml)
[![Release Pipeline](https://github.com/Gameaday/Pixel-Warrior-Monsters/actions/workflows/release.yml/badge.svg)](https://github.com/Gameaday/Pixel-Warrior-Monsters/actions/workflows/release.yml)

## Workflow Configuration

The artifact generation is configured in `.github/workflows/main.yml`:

```yaml
- name: Build debug APK
  run: ./gradlew assembleDebug

- name: Build release APK and AAB
  run: ./gradlew assembleRelease bundleRelease

- name: Upload debug APK (Development Build)
  uses: actions/upload-artifact@v4
  with:
    name: development-apk
    path: app/build/outputs/apk/debug/*.apk
    retention-days: 90

- name: Prepare production bundle
  run: |
    mkdir -p production-bundle
    cp app/build/outputs/apk/release/*.apk production-bundle/
    cp app/build/outputs/bundle/release/*.aab production-bundle/
    if [ -f app/build/outputs/mapping/release/mapping.txt ]; then
      cp app/build/outputs/mapping/release/mapping.txt production-bundle/
    fi
    # Generate README...

- name: Upload production bundle (Play Store Ready)
  uses: actions/upload-artifact@v4
  with:
    name: production-bundle
    path: production-bundle/
    retention-days: 180
```

## Local Build Instructions

### Development Build
```bash
./gradlew assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk
```

### Production Build
```bash
./gradlew assembleRelease bundleRelease
# Output: 
# - app/build/outputs/apk/release/app-release.apk
# - app/build/outputs/bundle/release/app-release.aab
```

## Artifact Retention Policy

| Artifact | Retention Period | Reason |
|----------|------------------|--------|
| development-apk | 90 days | Active alpha testing phase |
| production-bundle | 180 days | Production releases and Play Store submissions |

After the retention period, artifacts are automatically deleted from GitHub Actions. Tagged releases are preserved indefinitely.

## Related Documentation

- [README.md](README.md) - Main project documentation
- [GITHUB_ACCESS.md](GITHUB_ACCESS.md) - Complete GitHub access guide
- [ALPHA_TESTING.md](ALPHA_TESTING.md) - Alpha testing instructions
- [DEPLOYMENT_STATUS.md](DEPLOYMENT_STATUS.md) - Deployment roadmap
- [PRODUCTION_BUNDLE.md](PRODUCTION_BUNDLE.md) - Play Store preparation guide
- [releases/README.md](releases/README.md) - Release distribution info

## Support

For issues with artifacts or build failures:
1. Check [GitHub Actions](https://github.com/Gameaday/Pixel-Warrior-Monsters/actions) for build logs
2. Review workflow status badges on [README.md](README.md)
3. Open an issue: https://github.com/Gameaday/Pixel-Warrior-Monsters/issues

---

**Last Updated**: October 2024  
**Version**: 1.0.0-alpha
