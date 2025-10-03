# Alpha Testing Guide - Pixel Warrior Monsters

Welcome to the alpha testing phase of Pixel Warrior Monsters! This guide will help you get started with testing the game on your Android device.

## Download & Installation

### Requirements
- Android device running Android 7.0 (API 24) or higher
- ~50MB free storage space
- Allow installation from unknown sources

### Installation Steps

1. **Download the Alpha APK**
   - Go to [GitHub Actions](https://github.com/Gameaday/Pixel-Warrior-Monsters/actions)
   - Click on the latest successful "Android CI" workflow
   - Scroll to "Artifacts" and download `app-debug-apk`
   - Extract the ZIP file to get `app-debug.apk`

2. **Enable Unknown Sources** (if not already enabled)
   - Open Settings on your Android device
   - Go to Security or Privacy settings
   - Enable "Install unknown apps" or "Unknown sources" for your browser/file manager

3. **Install the APK**
   - Locate the downloaded `app-debug.apk` file
   - Tap on it to begin installation
   - Follow the on-screen prompts
   - Tap "Install" when prompted

4. **Launch the Game**
   - Find "Pixel Warrior Monsters" in your app drawer
   - Tap to launch and start playing!

## What to Test

### Core Gameplay Features

#### 1. Monster Management ✅
- [ ] Create and view monsters in your party
- [ ] Check monster stats, types, and families
- [ ] View personality traits and how they affect growth
- [ ] Test level progression and experience gains

#### 2. Battle System ✅
- [ ] Enter turn-based battles
- [ ] Test attack, defend, skill, and run actions
- [ ] Verify type effectiveness (fire vs grass, water vs fire, etc.)
- [ ] Check damage calculations and critical hits
- [ ] Test status effects and buffs/debuffs

#### 3. Breeding System ✅
- [ ] Breed two monsters together
- [ ] Verify family compatibility checks
- [ ] Check offspring stat inheritance
- [ ] Test breeding with different monster types

#### 4. World Exploration ✅
- [ ] Navigate through different areas
- [ ] Encounter wild monsters
- [ ] Find and collect items
- [ ] Progress through the gate system

#### 5. Dungeon System ✅
- [ ] Enter the 8 themed dungeons
- [ ] Explore multiple floors
- [ ] Face boss encounters on boss floors
- [ ] Experience special wandering events
- [ ] Test floor progression and unlocking

#### 6. Hub World System ✅
- [ ] Navigate the Master's Sanctuary
- [ ] Interact with NPCs (Master, Librarian, etc.)
- [ ] Unlock new areas with key items
- [ ] Access facilities (Library, Arena, Breeding Lab, etc.)

#### 7. Monster Synthesis ✅
- [ ] Combine compatible monsters
- [ ] Create hybrid forms
- [ ] Test synthesis requirements

#### 8. Plus Enhancement System ✅
- [ ] Enhance monsters to +1, +2, +3, +4, +5
- [ ] Verify stat improvements
- [ ] Test enhancement item requirements

#### 9. Scout Missions ✅
- [ ] Deploy monsters on missions
- [ ] Complete different mission types
- [ ] Collect mission rewards
- [ ] Test mission success rates

#### 10. Audio System ✅
- [ ] Listen to background music
- [ ] Verify music changes with different screens
- [ ] Test sound effects (battles, menus, etc.)
- [ ] Check 8-bit audio synthesis quality
- [ ] Test audio settings (enable/disable, volume)

#### 11. Save/Load System ✅
- [ ] Save your game progress
- [ ] Close and reopen the app
- [ ] Verify saved data loads correctly
- [ ] Test settings persistence

### User Interface Testing

- [ ] Navigate all game screens
- [ ] Test touch controls and gestures
- [ ] Verify landscape orientation works
- [ ] Check text readability
- [ ] Test menu navigation
- [ ] Verify animations play smoothly

### Performance Testing

- [ ] Monitor battery usage during gameplay
- [ ] Check for lag or stuttering
- [ ] Test on low-end devices if possible
- [ ] Verify app doesn't crash or freeze
- [ ] Test memory usage (shouldn't exceed 512MB)

### Edge Cases & Bugs

Please test unusual scenarios:
- [ ] What happens with full monster party?
- [ ] Can you breed incompatible monsters?
- [ ] What happens at max level?
- [ ] Test saving/loading multiple times
- [ ] Try rapid screen navigation
- [ ] Test with airplane mode (no internet required)

## Reporting Issues

### How to Report Bugs

When you find a bug, please open an issue on GitHub with:

1. **Title**: Brief description of the issue
2. **Description**: Detailed explanation of what happened
3. **Steps to Reproduce**: Exact steps to recreate the bug
4. **Expected Behavior**: What should have happened
5. **Actual Behavior**: What actually happened
6. **Device Info**: 
   - Device model (e.g., Samsung Galaxy S21)
   - Android version (e.g., Android 12)
   - App version (currently 1.0.0)
7. **Screenshots**: If applicable

### Feedback & Suggestions

We also welcome feedback on:
- Gameplay balance and difficulty
- User interface improvements
- Feature requests
- Performance observations
- General impressions

Please share your feedback by opening an issue labeled "feedback" or "enhancement".

## Known Issues

Current known issues:
- Debug build is larger than optimized release (~23MB vs ~3MB)
- Some lint warnings for icons (non-critical)
- ProGuard configuration needs tuning for release builds

## Testing Timeline

**Alpha Phase Goals:**
- Test all implemented features thoroughly
- Identify critical bugs and crashes
- Gather feedback on gameplay and UX
- Performance validation on various devices

**Duration**: 1-2 weeks

After alpha testing is complete and critical issues are resolved, we'll move to beta testing with a wider audience through Google Play Console.

## Contact & Support

- **GitHub Issues**: https://github.com/Gameaday/Pixel-Warrior-Monsters/issues
- **Repository**: https://github.com/Gameaday/Pixel-Warrior-Monsters

Thank you for participating in alpha testing! Your feedback is invaluable in making Pixel Warrior Monsters the best it can be.

## Quick Reference

**Current Version**: 1.0.0-alpha
**Min Android**: 7.0 (API 24)
**Target Android**: 14 (API 34)
**APK Size**: ~23MB (debug) / ~3MB (release)
**Install Size**: ~50MB
