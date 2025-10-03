# Production Bundle Preparation - Google Play Store

This document describes how to prepare the production bundle for Google Play Store submission.

## Overview

The production bundle includes:
1. **Android App Bundle (AAB)** - Primary format for Play Store
2. **Release APK** - For sideloading and testing
3. **Store Assets** - Icons, screenshots, graphics
4. **Compliance Documents** - Privacy policy, content rating
5. **Metadata** - App description, keywords, release notes

## Current Status

### ✅ Ready for Production
- Build system configured for release builds
- ProGuard/R8 optimization enabled
- Debug symbols preserved for crash reporting
- AAB and APK generation working
- CI/CD pipeline for automated builds

### ⏳ Pending Configuration
- Release keystore generation
- Play Console service account
- Store assets creation
- Privacy policy drafting
- Content rating questionnaire

## Building Production Bundle

### Prerequisites

1. **Release Keystore**
   ```bash
   # Generate a new keystore (only once)
   keytool -genkey -v -keystore release-key.jks \
     -keyalg RSA -keysize 2048 -validity 10000 \
     -alias pixelwarrior-key
   ```

2. **Keystore Configuration**
   Create `keystore.properties` in project root:
   ```properties
   storeFile=/path/to/release-key.jks
   storePassword=YOUR_STORE_PASSWORD
   keyAlias=pixelwarrior-key
   keyPassword=YOUR_KEY_PASSWORD
   ```

3. **Update build.gradle**
   Add signing configuration (already configured, just needs keystore):
   ```gradle
   android {
       signingConfigs {
           release {
               if (project.file('keystore.properties').exists()) {
                   // Load keystore
                   def keystorePropertiesFile = rootProject.file("keystore.properties")
                   def keystoreProperties = new Properties()
                   keystoreProperties.load(new FileInputStream(keystorePropertiesFile))
                   
                   storeFile file(keystoreProperties['storeFile'])
                   storePassword keystoreProperties['storePassword']
                   keyAlias keystoreProperties['keyAlias']
                   keyPassword keystoreProperties['keyPassword']
               }
           }
       }
   }
   ```

### Building Release Bundle

**Using build script:**
```bash
./build.sh release
```

**Manual build:**
```bash
./gradlew clean bundleRelease assembleRelease
```

**Output files:**
- AAB: `app/build/outputs/bundle/release/app-release.aab`
- APK: `app/build/outputs/apk/release/app-release.apk`

### Automated Release via GitHub Actions

Push a version tag to trigger automated release:
```bash
# Tag the release
git tag -a v1.0.0 -m "Release version 1.0.0"
git push origin v1.0.0
```

This automatically:
1. Runs all tests
2. Performs security scans
3. Builds signed AAB and APK
4. Creates GitHub Release with artifacts
5. (Future) Uploads to Play Console

## Store Assets Required

### App Icon
- **Size:** 512x512 pixels
- **Format:** 32-bit PNG (with alpha)
- **Location:** Create in `app/src/main/res/mipmap-xxxhdpi/ic_launcher.png`

### Feature Graphic
- **Size:** 1024x500 pixels
- **Format:** JPEG or 24-bit PNG
- **Content:** Showcase game screenshot or key art

### Screenshots (Required)
Minimum 2 screenshots per device type:
- **Phone:** 1080x1920 (portrait) or 1920x1080 (landscape)
- **7-inch Tablet:** 1200x1920 or 1920x1200
- **10-inch Tablet:** 1600x2560 or 2560x1600

**Recommended:** 8 screenshots showing:
1. Title/Menu screen
2. Battle system
3. Monster collection
4. Breeding mechanics
5. World exploration
6. Dungeon system
7. Hub world
8. Settings/Features

### Promotional Graphics (Optional)
- **Promo Graphic:** 180x120 pixels
- **TV Banner:** 1280x720 pixels (if targeting Android TV)

## Store Listing Content

### Short Description (80 characters max)
```
Classic monster collection RPG with breeding, battles, and exploration!
```

### Full Description (4000 characters max)
```
Pixel Warrior Monsters is a love letter to classic monster-collecting RPGs, 
featuring turn-based battles, strategic breeding mechanics, and dungeon exploration.

🎮 CORE FEATURES
• Collect and train monsters across 17 types and 8 families
• Strategic turn-based combat with type effectiveness
• Breed monsters to create powerful hybrids
• Explore 8 themed dungeons with 160+ floors
• Progressive hub world with story-driven unlocks

⚔️ BATTLE SYSTEM
• Classic turn-based combat with modern polish
• Type advantage system for strategic depth
• Skill-based special attacks
• Smart AI opponents

🧬 BREEDING & GENETICS
• Compatible family breeding system
• Stat inheritance from parents
• Create unique monster combinations
• Plus enhancement system (+1 to +5)

🌍 EXPLORATION
• 8 massive themed dungeons
• Boss battles every 8 floors
• Random events and treasure
• Progressive difficulty scaling

🎨 RETRO AESTHETIC
• Authentic pixel art graphics
• 8-bit chiptune audio
• Landscape-optimized UI
• Smooth animations

📱 MOBILE OPTIMIZED
• Touch-friendly controls
• Battery efficient
• No internet required
• Save anywhere

Perfect for fans of Dragon Warrior Monsters, Pokémon, and classic JRPGs!
```

### Keywords/Tags
- Monster collecting
- RPG
- Breeding
- Turn-based
- Retro
- Pixel art
- Dragon Quest
- JRPG
- Strategy
- Dungeon crawler

## Privacy Policy

**Required for Play Store submission**

Create `PRIVACY_POLICY.md` with:

```markdown
# Privacy Policy - Pixel Warrior Monsters

Last updated: [DATE]

## Data Collection
This app does NOT collect, store, or transmit any personal information.

## Local Data Storage
- Game saves stored locally on device
- Settings stored in app preferences
- No cloud backup or sync
- Data never leaves your device

## Third-Party Services
This app does NOT use:
- Analytics services
- Advertising networks
- Social media integration
- Cloud storage services

## Permissions
This app requests NO special permissions beyond basic storage for game saves.

## Children's Privacy
This app is safe for children and complies with COPPA. No data collection occurs.

## Changes to Policy
Any updates will be posted in this document and the app store listing.

## Contact
For questions: [Your contact email or GitHub]
```

Host this at: `https://gameaday.github.io/Pixel-Warrior-Monsters/privacy-policy.html`

## Content Rating

Google Play requires content rating questionnaire:

**Expected Rating:** E (Everyone) or E10+ (Everyone 10+)

**Content:**
- ✅ Fantasy violence (monster battles)
- ✅ No realistic violence
- ✅ No blood/gore
- ✅ No profanity
- ✅ No sexual content
- ✅ No gambling
- ✅ No drugs/alcohol

## Version Management

### Version Code
- Increment by 1 for each release
- Current: 1
- Next release: 2

### Version Name
- Semantic versioning: MAJOR.MINOR.PATCH
- Current: 1.0.0
- Bug fixes: 1.0.1
- New features: 1.1.0
- Major changes: 2.0.0

Update in `app/build.gradle`:
```gradle
defaultConfig {
    versionCode 1
    versionName "1.0.0"
}
```

## Release Checklist

### Pre-Release
- [ ] All tests passing (200+ unit tests)
- [ ] No lint errors or warnings
- [ ] ProGuard rules tested
- [ ] Release keystore configured
- [ ] Version code and name updated
- [ ] Release notes written

### Build
- [ ] Clean build performed
- [ ] AAB generated and signed
- [ ] APK generated for verification
- [ ] File sizes acceptable (<50MB)
- [ ] Test on multiple devices

### Store Assets
- [ ] 512x512 app icon created
- [ ] Feature graphic created
- [ ] 8+ screenshots captured
- [ ] All graphics meet specifications

### Compliance
- [ ] Privacy policy published
- [ ] Content rating completed
- [ ] App description written
- [ ] Keywords selected
- [ ] Release notes prepared

### Play Console
- [ ] Account created and verified
- [ ] App created in console
- [ ] AAB uploaded to internal track
- [ ] Store listing completed
- [ ] Internal testing validated

### Launch
- [ ] Internal testing successful
- [ ] Beta testing (optional)
- [ ] Production release submitted
- [ ] Marketing materials ready

## Post-Release

### Monitoring
- Set up Play Console monitoring
- Watch for crashes and ANRs
- Monitor user reviews
- Track download metrics

### Updates
- Regular bug fixes
- Feature additions
- Performance improvements
- Content updates

## File Structure

```
releases/
├── production/
│   ├── v1.0.0/
│   │   ├── app-release.aab      # Play Store bundle
│   │   ├── app-release.apk      # Sideload APK
│   │   ├── mapping.txt          # ProGuard mapping
│   │   └── release-notes.md     # Version notes
│   └── assets/
│       ├── icon-512x512.png
│       ├── feature-graphic.png
│       ├── screenshots/
│       └── privacy-policy.md
└── README.md
```

## Additional Resources

- [Play Console Help](https://support.google.com/googleplay/android-developer)
- [App Bundle Documentation](https://developer.android.com/guide/app-bundle)
- [Play Store Launch Checklist](https://developer.android.com/distribute/best-practices/launch/launch-checklist)
- [PLAY_STORE_PREPARATION.md](PLAY_STORE_PREPARATION.md) - Detailed checklist
- [DEPLOYMENT_STATUS.md](DEPLOYMENT_STATUS.md) - Current status

## Timeline to Launch

Assuming all requirements are met:

1. **Week 1:** Alpha testing and bug fixes
2. **Week 2:** Create store assets and compliance docs
3. **Week 3:** Play Console setup and internal testing
4. **Week 4:** Beta testing and production launch

**Target Launch Date:** 4 weeks from alpha testing start

## Support

For questions about the production bundle:
- Open an issue on GitHub
- See existing documentation in repo
- Contact project maintainers
