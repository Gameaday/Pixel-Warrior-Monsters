# Quick Start - Getting the Alpha Build

## For Alpha Testers

### Download from GitHub Actions (Recommended)

1. **Visit GitHub Actions**: https://github.com/Gameaday/Pixel-Warrior-Monsters/actions

2. **Find Latest Build**:
   - Click on the latest "CI/CD Pipeline - Testing and Build" workflow
   - Look for the green checkmark (✓) indicating success

3. **Download APK**:
   - Scroll to the bottom of the page
   - Find "Artifacts" section
   - Click on `app-debug-apk` to download
   - Extract the ZIP file to get `app-debug.apk`

4. **Install on Android Device**:
   - Transfer APK to your phone
   - Enable "Install from Unknown Sources" in Settings
   - Tap the APK file to install
   - Launch "Pixel Warrior Monsters"

### Build Locally

If you prefer to build the APK yourself:

```bash
# Clone the repository
git clone https://github.com/Gameaday/Pixel-Warrior-Monsters.git
cd Pixel-Warrior-Monsters

# Build alpha APK
./build.sh alpha

# APK will be at: app/build/outputs/apk/debug/app-debug.apk
```

## For Developers

### Quick Commands

```bash
# Build alpha/debug APK
./build.sh alpha

# Build production release
./build.sh release

# Run tests
./build.sh test

# Run lint checks
./build.sh lint

# Build everything
./build.sh all

# Clean builds
./build.sh clean
```

### Manual Gradle Commands

```bash
# Alpha build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease bundleRelease

# Run tests
./gradlew test

# Run lint
./gradlew lint
```

## System Requirements

### For Installation
- Android 7.0 (API 24) or higher
- ~50MB free storage
- No internet connection required

### For Development
- Android Studio or IntelliJ IDEA
- JDK 17 or higher
- Android SDK 24-35
- Kotlin 2.1.0+

## Getting Help

- **Testing Guide**: See [ALPHA_TESTING.md](ALPHA_TESTING.md)
- **Build Details**: See [releases/README.md](releases/README.md)
- **Issues**: https://github.com/Gameaday/Pixel-Warrior-Monsters/issues
- **Documentation**: See main [README.md](README.md)

## Current Version

**Version**: 1.0.0-alpha  
**Build**: versionCode 1  
**Target**: Android 14 (API 34)  
**Minimum**: Android 7.0 (API 24)

## What's Included

✅ All game features fully implemented:
- Turn-based battle system
- Monster breeding and genetics
- 8 themed dungeons (160+ floors)
- Hub world with NPCs
- Monster synthesis system
- Plus enhancement system
- Scout missions
- Save/load system
- 8-bit chiptune audio
- Complete UI with 13+ screens

## Known Limitations

- Debug build is larger (~23MB vs ~3MB release)
- Some lint warnings for icons (non-critical)
- Signed with debug keystore (not for production)

---

**Quick Link**: [Download Latest Build](https://github.com/Gameaday/Pixel-Warrior-Monsters/actions)
