# Pixel Warrior Monsters - Known Limitations & Missing Features

**Last Updated:** February 9, 2026  
**Version:** 1.0.0-alpha

This document transparently lists features that are incomplete, missing, or differ from documentation claims.

---

## ❌ Major Missing Features

### 1. Online Multiplayer/PvP (Claimed but Not Implemented)
**Documentation Claim:** "✅ Online Multiplayer: PvP battles with other players' monster teams"  
**Reality:** Not implemented at all. No networking code, no multiplayer infrastructure.  
**Impact:** Cannot battle other players online  
**Planned:** v2.0 (6-8 months post-launch)

### 2. Cross-Platform Features (Phase 7)
**Documentation Claim:** Steam version, cloud saves, mod support, social features  
**Reality:** Only stub implementations exist with no functionality  
**Impact:** Cannot use on Steam, no cloud sync, no mods, no friend system  
**Planned:** Long-term (12+ months post-launch)

---

## ⚠️ Incomplete Features

### 1. Story/Quest System
**What Exists:**
- Story milestone data structures
- Key item definitions
- Hub world progression framework

**What's Missing:**
- Quest tracking and state management
- Narrative progression mechanics
- Quest completion rewards
- Branching storylines

**Impact:** Limited story-driven gameplay  
**Workaround:** Game is fully playable without story, focus on monster collection/battles  
**Planned Fix:** May simplify to linear progression for v1.0

### 2. Interactive Dialogue System
**What Exists:**
- Static dialogue arrays for NPCs
- Basic NPC interaction

**What's Missing:**
- Dialogue tree parsing
- Conversation branching
- Player choice consequences
- Relationship tracking

**Impact:** NPCs always say same things  
**Workaround:** Dialogue is still readable and provides context  
**Planned Fix:** v1.1 or v1.2 update

### 3. Item-Based Skill Learning
**What Exists:**
- Skills are defined and functional
- Monsters have skills from creation

**What's Missing:**
- Skill learning items
- Skill database system
- Learning progression
- Skill mastery mechanics

**Impact:** Cannot teach monsters new skills after creation  
**Workaround:** Breed/synthesize monsters with desired skills  
**Planned Fix:** v1.1 update

### 4. Synthesis Laboratory Process
**What Exists:**
- Synthesis recipes and data
- Synthesis calculation logic

**What's Missing:**
- Laboratory interaction UI/UX
- Synthesis animation/effects
- Recipe discovery system
- Failure feedback

**Impact:** Synthesis might feel basic/unpolished  
**Workaround:** Synthesis still functions correctly  
**Planned Fix:** Polish in alpha testing phase

---

## 🔧 Technical Limitations

### 1. Audio System
**Current Implementation:** Procedural 8-bit audio synthesis using coroutine delays  
**Limitation:** Not actual audio file playback, simulation only  
**Impact:** Audio works but is not true chiptune music  
**Performance:** May have slight timing variations  
**Planned Fix:** Real audio synthesis with AudioTrack (v1.0 before launch)

### 2. Animation System
**Current Implementation:** Framework exists, not fully integrated  
**Limitation:** Limited visual feedback during battles/events  
**Impact:** Less visual polish than optimal  
**Workaround:** Game is fully playable without animations  
**Planned Fix:** Progressive integration in updates

### 3. Performance Testing
**Current Status:** Tested in emulators and CI/CD only  
**Limitation:** Unknown performance on older physical devices  
**Impact:** May have performance issues on Android 7.0-8.0 devices  
**Workaround:** Target Android 9.0+ for best experience  
**Planned Fix:** Device testing during alpha phase

---

## 📱 Platform Limitations

### 1. Android Only
**Current Support:** Android 7.0+ (API 24+)  
**Limitation:** No iOS, no desktop, no web  
**Impact:** Can only play on Android devices  
**Planned:** Steam/desktop in Phase 7 (long-term)

### 2. Offline Only
**Current Support:** Single-player offline gameplay  
**Limitation:** No online features, no cloud saves, no multiplayer  
**Impact:** Progress is device-local only  
**Planned:** Online features in v2.0

### 3. No Monetization
**Current Status:** Completely free, no ads, no IAP  
**Limitation:** No revenue model  
**Impact:** Development is volunteer/donation-based  
**Future:** May add optional premium features in v2.0

---

## 📊 Data Persistence Limitations

### 1. Local Storage Only
**Current Implementation:** Room database on device  
**Limitation:** No cloud backup, no cross-device sync  
**Impact:** Losing device = losing progress  
**Workaround:** Manual export/import may be added  
**Planned:** Cloud sync in v2.0

### 2. Save Corruption Risk
**Current Status:** Save validation exists but untested at scale  
**Limitation:** Edge cases may cause save issues  
**Impact:** Rare chance of progress loss  
**Mitigation:** Backup system planned for v1.0  
**Planned Fix:** Extensive testing during alpha

---

## 🎮 Gameplay Limitations

### 1. No Monster Trading
**Current Status:** Not implemented  
**Limitation:** Cannot trade with other players  
**Impact:** Must obtain all monsters yourself  
**Planned:** v2.0 with multiplayer

### 2. Limited Monster Slots
**Current Status:** Finite party/stable size  
**Limitation:** Cannot keep every monster indefinitely  
**Impact:** Must choose which monsters to keep  
**Workaround:** Synthesis reduces monster count  
**Future:** May expand storage in updates

### 3. No Difficulty Scaling
**Current Status:** Fixed difficulty curve  
**Limitation:** May be too easy or too hard for some players  
**Impact:** Cannot adjust challenge level  
**Planned:** Difficulty options in v1.1

---

## 🐛 Known Bugs & Issues

### 1. Build Warnings (Non-Critical)
**Issue:** 85 lint warnings related to icons  
**Impact:** Icons may not display optimally on all devices  
**Severity:** Low  
**Planned Fix:** Asset cleanup before launch

### 2. Gradle Deprecations
**Issue:** Some Gradle features deprecated for Gradle 10  
**Impact:** Future build system updates needed  
**Severity:** Low  
**Planned Fix:** Update in v1.1

### 3. Emulator Boot Timeout (CI/CD)
**Issue:** Integration tests may timeout on slow runners  
**Impact:** CI/CD may show intermittent failures  
**Severity:** Low (doesn't affect game)  
**Mitigation:** `continue-on-error` enabled

---

## ✅ What Works Well

Despite limitations, these features are fully functional:

- ✅ Complete battle system with 17 types
- ✅ Breeding with genetic inheritance
- ✅ 8 dungeons with 182 floors total
- ✅ Tournament system with 12 rivals
- ✅ Weather and day/night cycle
- ✅ Scout missions and synthesis
- ✅ Save/load system
- ✅ 5 ultra dungeons and legendary monsters
- ✅ Achievement system
- ✅ 195+ tests passing

---

## 📋 Comparison: Roadmap vs Reality

| Feature | Roadmap Says | Reality | Gap |
|---------|-------------|---------|-----|
| Hub World | ✅ Complete | ⚠️ 75% | Story/quest system incomplete |
| Synthesis | ✅ Complete | ⚠️ 85% | UI polish needed |
| Tournaments | ✅ Complete | ⚠️ 70% | Missing multiplayer |
| **Multiplayer** | **✅ Complete** | **❌ 0%** | **NOT IMPLEMENTED** |
| Exploration | ✅ Complete | ✅ 95% | Fully functional |
| Endgame | ✅ Complete | ✅ 95% | Fully functional |
| Audio | ✅ Complete | ⚠️ 70% | Procedural only |
| Cross-Platform | Stub | ❌ 10% | Future plans |

---

## 🎯 Recommendations for Users

### For Alpha Testers
- Focus on single-player experience
- Test battle, breeding, dungeon features
- Report bugs via GitHub Issues
- Expect some unfinished features

### For v1.0 Launch
- Single-player game will be complete
- Do not expect multiplayer in v1.0
- Cloud saves not available yet
- Performance may vary on older devices

### For Future Updates
- v1.1: QoL improvements, bug fixes
- v1.2: Story/quest completion, skill learning
- v2.0: Multiplayer infrastructure
- v3.0+: Cross-platform support

---

## 📞 Reporting Issues

**Found a bug or limitation not listed here?**
- Open an issue on [GitHub Issues](https://github.com/Gameaday/Pixel-Warrior-Monsters/issues)
- Use template: "Bug Report" or "Feature Request"
- Include device info, Android version, steps to reproduce

**Want to contribute?**
- Check [IMPLEMENTATION_PLAN.md](IMPLEMENTATION_PLAN.md) for development priorities
- See [PROJECT_ASSESSMENT.md](PROJECT_ASSESSMENT.md) for technical details
- Follow contribution guidelines (coming soon)

---

**Transparency Note:** This document exists to set accurate expectations. The project is high-quality and functional, but some documentation was aspirational rather than descriptive. We're committed to honesty about what exists vs. what's planned.

**Next Review:** March 2026 (after alpha testing phase)
