# Pixel Warrior Monsters

An open source reimagining of the original Dragon Warrior Monsters (DQM) utilizing pixel-based graphics, animation, and written to run on Android. This project aims to recreate all the core systems and features of the original game while avoiding copyright infringement through original assets and generic monster designs.

## Project Goals

- **Core Gameplay**: Recreate the turn-based battle system, monster breeding mechanics, and exploration features
- **Platform**: Primary target is Android, with plans for eventual Steam release
- **Graphics**: Pixel art aesthetic with smooth animations optimized for mobile
- **Expansion**: Add new monsters, levels, and quality-of-life improvements for modern mobile gaming

## Features Implemented

### Core Systems
- ✅ **Monster Management**: Complete data model with 17 types, 8 families, and advanced personality system
- ✅ **Battle System**: Turn-based combat with damage calculation, type effectiveness, and skill usage
- ✅ **Breeding System**: Monster genetics with family compatibility and stat inheritance  
- ✅ **World Exploration**: Random encounters, area progression, and item discovery
- ✅ **Advanced Dungeon System**: 8 themed dungeons with multiple floors, boss encounters, and special events
- ✅ **Hub World System**: Progressive story-driven Master's Sanctuary with 9 unlockable areas and NPCs
- ✅ **Monster Synthesis**: Combine compatible monsters to create 14 powerful new hybrid forms
- ✅ **Plus Enhancement**: Upgrade monsters through +5 levels with special enhancement items
- ✅ **Scout Missions**: Deploy monsters on 6 types of exploration missions for rewards
- ✅ **Personality System**: 16 distinct personalities affecting growth, AI, and mission success
- ✅ **Experience System**: Level progression with multiple growth rate curves
- ✅ **Save System**: Complete game state persistence with settings and progress tracking
- ✅ **Chiptune Audio System**: 8-bit style music and sound effects with 7 background tracks and 11 SFX

### Technical Architecture
- ✅ **Android Project**: Properly configured Gradle build with Jetpack Compose UI
- ✅ **MVVM Architecture**: Clean separation of data models, repositories, and UI layers
- ✅ **Data Models**: Comprehensive type-safe data classes with Parcelable support
- ✅ **Game Logic**: Modular systems for battle, breeding, world exploration, and utilities
- ✅ **Testing**: 200+ unit tests covering all game mechanics, advanced systems, and calculations

### User Interface
- ✅ **Compose UI**: Modern Android UI with pixel art theming and responsive design
- ✅ **Navigation**: Screen management for menu, world map, battles, and monster management
- ✅ **Theming**: Pixel art color palette and typography optimized for retro aesthetic
- ✅ **Mobile Optimization**: Landscape orientation and touch-friendly controls
- ✅ **Complete Game Screens**: 13+ screens including battles, breeding, dungeon exploration, synthesis lab, settings
- ✅ **Audio Integration**: Chiptune music that changes contextually with screen navigation

### Advanced Dungeon System

The game features an extensive dungeon exploration system inspired by the original Dragon Warrior Monsters:

#### 8 Themed Dungeons
- 🌲 **Whispering Woods** (16 floors) - Forest theme with nature monsters
- 🌋 **Molten Core Depths** (20 floors) - Volcanic theme with fire monsters  
- ❄️ **Eternal Ice Palace** (18 floors) - Ice theme with frost monsters
- 🏺 **Ancient Lost Ruins** (24 floors) - Archaeological theme with ancient monsters
- 🌊 **Abyssal Ocean Depths** (22 floors) - Underwater theme with sea monsters
- ☁️ **Celestial Sky Tower** (30 floors) - Sky theme with flying monsters
- 🏜️ **Endless Mirage Desert** (20 floors) - Desert theme with sand monsters
- 💎 **Rainbow Crystal Caverns** (32 floors) - Crystal theme with gem monsters

#### Floor Types
- **Regular Floors**: Standard exploration with encounters and wandering events
- **Boss Floors**: Every 8th floor features powerful guardian monsters with guaranteed encounters
- **Event Floors**: Special areas with unique mechanics like monster villages, treasure vaults, trials

#### Wandering Events
Random events occur as you explore, themed to each dungeon:
- **Forest**: Fairy rings (healing), ancient trees (experience), mushroom circles
- **Volcanic**: Lava pools (damage), fire crystals (fire boost), magical forges  
- **Ice**: Frozen fountains (MP restore), ice storms, crystal mazes
- **Ruins**: Ancient inscriptions (stat boost), treasure chambers, spirit councils
- **Underwater**: Air pockets, coral gardens, sunken ships with pirate treasure
- **Sky**: Wind currents (agility boost), cloud shrines, storm centers
- **Desert**: Hidden oases (full healing), sandstorms, pyramid chambers
- **Crystal**: Crystal resonance (magic boost), gem veins, rainbow portals

#### Progressive Difficulty
- Encounter rates increase with floor depth
- Monster levels scale with dungeon progression  
- Special key items required to unlock advanced dungeons
- Each dungeon unlocks through story progression

## Project Structure

```
app/src/main/java/com/pixelwarrior/monsters/
├── data/
│   ├── model/          # Core data classes (Monster, Battle, GameData)
│   └── repository/     # Data persistence and game state management
├── game/
│   ├── battle/         # Turn-based combat system
│   ├── breeding/       # Monster genetics and breeding
│   └── world/          # Exploration and encounter system
├── ui/
│   ├── screens/        # Compose UI screens and navigation
│   ├── components/     # Reusable UI components
│   └── theme/          # Pixel art styling and colors
└── utils/              # Game calculations and utilities
```

## Key Features

### Monster System
- **17 Monster Types**: Fire, Water, Grass, Electric, Flying, and more with type effectiveness
- **8 Monster Families**: Beast, Bird, Plant, Slime, Undead, Material, Demon, Dragon
- **Breeding Compatibility**: Family-based breeding system with stat inheritance
- **Growth Rates**: 5 different experience curves for varied progression
- **Traits System**: Personality traits affecting monster behavior and stats

### Battle System
- **Turn-based Combat**: Strategic battles with attack, skill, defend, and run options
- **Damage Calculation**: Complex formula considering stats, level, type, and critical hits
- **Skill System**: MP-based special abilities with various targeting options
- **Status Effects**: Buffs, debuffs, and damage-over-time effects
- **AI Opponents**: Smart enemy behavior based on HP and available options

### World Exploration
- **Multiple Areas**: 5+ unique locations with different monster encounters
- **Random Encounters**: Area-specific encounter rates and monster lists  
- **Item Discovery**: Find healing items, capture tools, and special treasures
- **Gate System**: Unlock new areas with key items and story progression

### Hub World System
- **Master's Sanctuary**: Central hub area with 9 progressive unlockable facilities
- **Story-Driven Progression**: Areas unlock through key items and story milestones
- **Interactive NPCs**: 7 unique characters including Master, Librarian, Arena Manager, and more
- **Hub Facilities**: 
  - Monster Library (bestiary and breeding info)
  - Breeding Laboratory (advanced monster genetics)
  - Battle Arena (tournaments and competitions)
  - Synthesis Lab (monster combination research)
  - Item Shop (purchase tools and supplies)
  - Gate Chamber (portal access to different worlds)
  - Master's Quarters and Secret Vault (end-game content)
- **Progressive Unlocks**: Each area requires specific achievements or key items
- **Dialogue System**: Multi-line conversations with story and gameplay hints

### Quality of Life Features
- **Auto-save**: Automatic game state preservation
- **Fast Text**: Configurable text speed including instant mode
- **Battle Animations**: Toggle-able combat animations for faster battles
- **Difficulty Settings**: Adjustable challenge levels for different players

## Copyright Compliance

This project is designed to avoid copyright infringement:
- **Original Assets**: All graphics, sounds, and names are original creations
- **Generic Designs**: Monster types and families use common fantasy tropes
- **Inspired Mechanics**: Game systems are inspired by but not copied from DQM
- **Clean Implementation**: No reverse-engineered code or extracted assets

## Development Status

The project foundation is complete with all core systems implemented and tested. The codebase follows Android best practices and is ready for further development in areas such as:

- Enhanced UI screens for monster management and breeding
- Advanced battle animations and visual effects  
- Expanded monster roster and skill library
- Story mode and quest system implementation
- Audio system with music and sound effects
- Additional quality-of-life features

## Building the Project

This is a standard Android project using Gradle:

1. Clone the repository
2. Open in Android Studio
3. Sync Gradle files
4. Build and run on device or emulator

**Requirements:**
- Android SDK 24+ (Android 7.0)
- Java 17 or higher
- Kotlin support
- Jetpack Compose

## Testing

Run the test suite to verify core game mechanics:

```bash
./gradlew test
```

The test suite covers:
- Monster creation and stat calculations
- Battle system damage formulas
- Breeding compatibility and offspring generation
- Experience and level progression
- Utility functions and validations

## License

This project is open source and available under the MIT License. All original assets and code may be freely used and modified while maintaining attribution to the original creators.
