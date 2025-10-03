# What's Available on GitHub

This document lists everything accessible on GitHub for alpha testing and development.

## 🎮 Alpha Testing APK

### How to Download

**Via GitHub Actions (Recommended)**
1. Visit: https://github.com/Gameaday/Pixel-Warrior-Monsters/actions
2. Click on the latest "CI/CD Pipeline - Testing and Build" workflow
3. Scroll to "Artifacts" section
4. Download `app-debug-apk` (contains app-debug.apk)

**File Details:**
- **Name**: app-debug.apk
- **Size**: ~23MB
- **Signing**: Debug keystore (for testing only)
- **Min Android**: 7.0 (API 24)
- **Target Android**: 14 (API 34)

### Installation
1. Transfer APK to Android device
2. Enable "Install from Unknown Sources"
3. Tap APK to install
4. Launch "Pixel Warrior Monsters"

See [QUICK_START.md](QUICK_START.md) or [ALPHA_TESTING.md](ALPHA_TESTING.md) for detailed instructions.

## 📂 Repository Contents

### Documentation (Immediately Accessible)

All documentation is viewable directly on GitHub:

**Getting Started:**
- [README.md](https://github.com/Gameaday/Pixel-Warrior-Monsters/blob/main/README.md) - Main documentation
- [QUICK_START.md](https://github.com/Gameaday/Pixel-Warrior-Monsters/blob/main/QUICK_START.md) - Fast installation guide
- [ALPHA_RELEASE_SUMMARY.md](https://github.com/Gameaday/Pixel-Warrior-Monsters/blob/main/ALPHA_RELEASE_SUMMARY.md) - Release overview
- [DOCUMENTATION_INDEX.md](https://github.com/Gameaday/Pixel-Warrior-Monsters/blob/main/DOCUMENTATION_INDEX.md) - Complete index

**Testing:**
- [ALPHA_TESTING.md](https://github.com/Gameaday/Pixel-Warrior-Monsters/blob/main/ALPHA_TESTING.md) - Testing guide with checklist
- [releases/README.md](https://github.com/Gameaday/Pixel-Warrior-Monsters/blob/main/releases/README.md) - Build distribution info

**Development:**
- [build.sh](https://github.com/Gameaday/Pixel-Warrior-Monsters/blob/main/build.sh) - Build script
- [ROADMAP.md](https://github.com/Gameaday/Pixel-Warrior-Monsters/blob/main/ROADMAP.md) - Feature roadmap
- [IMPLEMENTATION_PLAN.md](https://github.com/Gameaday/Pixel-Warrior-Monsters/blob/main/IMPLEMENTATION_PLAN.md) - Implementation details

**Deployment:**
- [DEPLOYMENT_STATUS.md](https://github.com/Gameaday/Pixel-Warrior-Monsters/blob/main/DEPLOYMENT_STATUS.md) - Deployment roadmap
- [PRODUCTION_BUNDLE.md](https://github.com/Gameaday/Pixel-Warrior-Monsters/blob/main/PRODUCTION_BUNDLE.md) - Play Store guide
- [PLAY_STORE_PREPARATION.md](https://github.com/Gameaday/Pixel-Warrior-Monsters/blob/main/PLAY_STORE_PREPARATION.md) - Store checklist

### Source Code

Full source code is available in the repository:
- **Android app**: `app/src/main/java/com/pixelwarrior/monsters/`
- **Unit tests**: `app/src/test/java/com/pixelwarrior/monsters/`
- **Resources**: `app/src/main/res/`

### Build System

- **Gradle build files**: `build.gradle`, `app/build.gradle`, `settings.gradle`
- **CI/CD workflows**: `.github/workflows/`
- **Build script**: `build.sh`

## 🔄 Automated Builds

### Every Push to Main/Develop
GitHub Actions automatically:
1. Runs all 200+ unit tests
2. Performs lint checks
3. Builds debug APK
4. Builds release APK
5. Uploads artifacts

**Access artifacts**: Go to any workflow run → Scroll to "Artifacts"

### On Version Tags
When a version tag is pushed (e.g., `v1.0.0`):
1. Security scans
2. Performance testing
3. Release builds (APK + AAB)
4. GitHub Release creation

## 📊 Test Results

Test results are available as artifacts in GitHub Actions:
- **Test Reports**: HTML reports showing all test results
- **Coverage Reports**: Code coverage analysis

Access via workflow runs → Artifacts → `test-results`

## 🛠️ Building Locally

Clone and build yourself:

```bash
# Clone repository
git clone https://github.com/Gameaday/Pixel-Warrior-Monsters.git
cd Pixel-Warrior-Monsters

# Build alpha APK
./build.sh alpha

# Build everything
./build.sh all

# Run tests
./build.sh test
```

## 📈 Repository Statistics

**Current Status:**
- ✅ All tests passing (200+ unit tests)
- ✅ CI/CD pipeline operational
- ✅ Debug builds available as artifacts
- ✅ Release builds configured
- ✅ Complete documentation

**Version:** 1.0.0-alpha (versionCode: 1)

## 🔗 Important Links

### Repository
- **Main**: https://github.com/Gameaday/Pixel-Warrior-Monsters
- **Actions**: https://github.com/Gameaday/Pixel-Warrior-Monsters/actions
- **Issues**: https://github.com/Gameaday/Pixel-Warrior-Monsters/issues
- **Discussions**: https://github.com/Gameaday/Pixel-Warrior-Monsters/discussions

### Latest Builds
- **Alpha APK**: https://github.com/Gameaday/Pixel-Warrior-Monsters/actions (latest workflow → artifacts)
- **Releases**: https://github.com/Gameaday/Pixel-Warrior-Monsters/releases (when available)

### Documentation
- **README**: https://github.com/Gameaday/Pixel-Warrior-Monsters/blob/main/README.md
- **Quick Start**: https://github.com/Gameaday/Pixel-Warrior-Monsters/blob/main/QUICK_START.md
- **Alpha Testing**: https://github.com/Gameaday/Pixel-Warrior-Monsters/blob/main/ALPHA_TESTING.md
- **All Docs**: https://github.com/Gameaday/Pixel-Warrior-Monsters/blob/main/DOCUMENTATION_INDEX.md

## ❓ FAQ

### Q: Where can I download the game?
**A:** Go to [GitHub Actions](https://github.com/Gameaday/Pixel-Warrior-Monsters/actions), click latest workflow, download `app-debug-apk` artifact.

### Q: Is there a Play Store release?
**A:** Not yet. Currently in alpha testing phase. Play Store launch is planned after alpha feedback.

### Q: How do I report bugs?
**A:** Open an issue: https://github.com/Gameaday/Pixel-Warrior-Monsters/issues

### Q: Can I contribute?
**A:** Yes! Fork the repository, make changes, and submit a pull request.

### Q: Where is the source code?
**A:** All source code is in the repository under `app/src/`

### Q: How do I build from source?
**A:** Clone the repo and run `./build.sh alpha` or see [QUICK_START.md](QUICK_START.md)

## 📞 Support

- **Issues**: https://github.com/Gameaday/Pixel-Warrior-Monsters/issues
- **Discussions**: https://github.com/Gameaday/Pixel-Warrior-Monsters/discussions
- **Repository**: https://github.com/Gameaday/Pixel-Warrior-Monsters

## 🎯 Quick Access

**I want to...**
- **Download the game** → [GitHub Actions artifacts](https://github.com/Gameaday/Pixel-Warrior-Monsters/actions)
- **Read docs** → [Documentation Index](DOCUMENTATION_INDEX.md)
- **Report bugs** → [Open an issue](https://github.com/Gameaday/Pixel-Warrior-Monsters/issues)
- **See features** → [README Features](https://github.com/Gameaday/Pixel-Warrior-Monsters#features-implemented)
- **Build locally** → [Quick Start Guide](QUICK_START.md)

---

**Everything is accessible on GitHub - no external hosting required!**
