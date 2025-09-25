# Pixel Warrior Monsters

An open source reimagining of the original Dragon Warrior Monsters (DQM) utilizing pixel-based graphics, animation, and written to run on Android. This project aims to recreate all the core systems and features of the original game while avoiding copyright infringement through original assets and generic monster designs.

## Project Goals

- **Core Gameplay**: Recreate the turn-based battle system, monster breeding mechanics, and exploration features
- **Platform**: Primary target is Android, with plans for eventual Steam release
- **Graphics**: Pixel art aesthetic with smooth animations optimized for mobile
- **Expansion**: Add new monsters, levels, and quality-of-life improvements for modern mobile gaming

## Features Implemented

### Core Systems
- ✅ **Monster Management**: Complete data model for monsters with stats, types, families, and traits
- ✅ **Battle System**: Turn-based combat with damage calculation, type effectiveness, and skill usage
- ✅ **Breeding System**: Monster genetics with family compatibility and stat inheritance  
- ✅ **World Exploration**: Random encounters, area progression, and item discovery
- ✅ **Experience System**: Level progression with multiple growth rate curves
- ✅ **Save System**: Complete game state persistence with settings and progress tracking

### Technical Architecture
- ✅ **Android Project**: Properly configured Gradle build with Jetpack Compose UI
- ✅ **MVVM Architecture**: Clean separation of data models, repositories, and UI layers
- ✅ **Data Models**: Comprehensive type-safe data classes with Parcelable support
- ✅ **Game Logic**: Modular systems for battle, breeding, world exploration, and utilities
- ✅ **Testing**: Unit tests covering core game mechanics and calculations

### User Interface
- ✅ **Compose UI**: Modern Android UI with pixel art theming and responsive design
- ✅ **Navigation**: Screen management for menu, world map, battles, and monster management
- ✅ **Theming**: Pixel art color palette and typography optimized for retro aesthetic
- ✅ **Mobile Optimization**: Landscape orientation and touch-friendly controls

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
