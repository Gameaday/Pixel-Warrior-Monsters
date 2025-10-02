package com.pixelwarrior.monsters.game.qol

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random

/**
 * Quality of Life System for enhanced user experience
 * Includes advanced AI, animations, achievements, statistics, and voice acting
 */
class QualityOfLifeSystem {

    private val _achievements = MutableStateFlow(emptyList<Achievement>())
    val achievements: StateFlow<List<Achievement>> = _achievements.asStateFlow()

    private val _statistics = MutableStateFlow(GameStatistics())
    val statistics: StateFlow<GameStatistics> = _statistics.asStateFlow()

    private val _voiceSettings = MutableStateFlow(VoiceSettings())
    val voiceSettings: StateFlow<VoiceSettings> = _voiceSettings.asStateFlow()

    private val _animationSettings = MutableStateFlow(AnimationSettings())
    val animationSettings: StateFlow<AnimationSettings> = _animationSettings.asStateFlow()

    private val availableAchievements = createAchievements()
    private val advancedAI = AdvancedAI()

    // Achievement System
    fun checkAchievement(trigger: AchievementTrigger, value: Int = 1) {
        val currentAchievements = _achievements.value.toMutableList()
        val updatedAchievements = mutableListOf<Achievement>()
        var hasChanges = false

        availableAchievements.forEach { achievement ->
            if (achievement.trigger == trigger) {
                val existingAchievement = currentAchievements.find { it.id == achievement.id }
                
                if (existingAchievement != null) {
                    // Update existing achievement progress
                    if (!existingAchievement.isUnlocked) {
                        val newProgress = existingAchievement.progress + value
                        val updatedAchievement = existingAchievement.copy(
                            progress = minOf(newProgress, existingAchievement.requirement),
                            isUnlocked = newProgress >= existingAchievement.requirement
                        )
                        updatedAchievements.add(updatedAchievement)
                        hasChanges = true
                    } else {
                        updatedAchievements.add(existingAchievement)
                    }
                } else {
                    // Add new achievement with progress
                    val newProgress = achievement.progress + value
                    val newAchievement = achievement.copy(
                        progress = minOf(newProgress, achievement.requirement),
                        isUnlocked = newProgress >= achievement.requirement
                    )
                    updatedAchievements.add(newAchievement)
                    hasChanges = true
                }
            } else {
                // Keep non-matching achievements as-is
                currentAchievements.find { it.id == achievement.id }?.let { updatedAchievements.add(it) }
            }
        }

        if (hasChanges) {
            _achievements.value = updatedAchievements
        }
    }

    // Statistics Tracking
    fun updateStatistic(type: StatisticType, value: Int) {
        val currentStats = _statistics.value
        val updatedStats = when (type) {
            StatisticType.BATTLES_WON -> currentStats.copy(battlesWon = currentStats.battlesWon + value)
            StatisticType.BATTLES_LOST -> currentStats.copy(battlesLost = currentStats.battlesLost + value)
            StatisticType.MONSTERS_CAUGHT -> currentStats.copy(monstersCaught = currentStats.monstersCaught + value)
            StatisticType.MONSTERS_BRED -> currentStats.copy(monstersBred = currentStats.monstersBred + value)
            StatisticType.DUNGEONS_CLEARED -> currentStats.copy(dungeonsCleared = currentStats.dungeonsCleared + value)
            StatisticType.TOURNAMENTS_WON -> currentStats.copy(tournamentsWon = currentStats.tournamentsWon + value)
            StatisticType.SYNTHESIS_PERFORMED -> currentStats.copy(synthesisPerformed = currentStats.synthesisPerformed + value)
            StatisticType.PLAYTIME_MINUTES -> currentStats.copy(playtimeMinutes = currentStats.playtimeMinutes + value)
            StatisticType.GOLD_EARNED -> currentStats.copy(goldEarned = currentStats.goldEarned + value)
            StatisticType.EXPERIENCE_GAINED -> currentStats.copy(experienceGained = currentStats.experienceGained + value)
        }
        _statistics.value = updatedStats
        
        // Check for statistic-based achievements
        checkStatisticAchievements(type, updatedStats)
    }

    // Advanced AI Strategy Selection
    fun getAdvancedAIStrategy(aiPersonality: AIPersonality, playerParty: List<Any>, difficulty: Int): AIStrategy {
        return advancedAI.selectStrategy(aiPersonality, playerParty, difficulty)
    }

    // Animation Management
    fun playMonsterAnimation(monsterId: String, animationType: AnimationType): AnimationData? {
        if (!_animationSettings.value.enableAnimations) return null
        
        return when (animationType) {
            AnimationType.IDLE -> AnimationData("idle_${monsterId}", 2000, true)
            AnimationType.ATTACK -> AnimationData("attack_${monsterId}", 800, false)
            AnimationType.SKILL -> AnimationData("skill_${monsterId}", 1200, false)
            AnimationType.VICTORY -> AnimationData("victory_${monsterId}", 1500, false)
            AnimationType.FAINT -> AnimationData("faint_${monsterId}", 1000, false)
            AnimationType.ENTRY -> AnimationData("entry_${monsterId}", 600, false)
        }
    }

    // Voice Acting System
    fun playVoiceLine(character: VoiceCharacter, lineType: VoiceLineType): VoiceData? {
        if (!_voiceSettings.value.enableVoiceActing) return null
        
        val voiceId = "${character.name.lowercase()}_${lineType.name.lowercase()}"
        return VoiceData(voiceId, character, lineType, _voiceSettings.value.voiceVolume)
    }

    // Monster Cry System
    fun playMonsterCry(monsterType: String): SoundData? {
        if (!_voiceSettings.value.enableMonsterCries) return null
        
        return SoundData("cry_${monsterType.lowercase()}", _voiceSettings.value.effectsVolume)
    }

    // Settings Management
    fun updateVoiceSettings(settings: VoiceSettings) {
        _voiceSettings.value = settings
    }

    fun updateAnimationSettings(settings: AnimationSettings) {
        _animationSettings.value = settings
    }

    private fun checkStatisticAchievements(type: StatisticType, stats: GameStatistics) {
        when (type) {
            StatisticType.BATTLES_WON -> {
                if (stats.battlesWon >= 100) checkAchievement(AchievementTrigger.BATTLES_WON_100, stats.battlesWon)
                if (stats.battlesWon >= 500) checkAchievement(AchievementTrigger.BATTLES_WON_500, stats.battlesWon)
                if (stats.battlesWon >= 1000) checkAchievement(AchievementTrigger.BATTLES_WON_1000, stats.battlesWon)
            }
            StatisticType.MONSTERS_CAUGHT -> {
                if (stats.monstersCaught >= 50) checkAchievement(AchievementTrigger.MONSTERS_CAUGHT_50, stats.monstersCaught)
                if (stats.monstersCaught >= 150) checkAchievement(AchievementTrigger.MONSTERS_CAUGHT_150, stats.monstersCaught)
            }
            StatisticType.MONSTERS_BRED -> {
                if (stats.monstersBred >= 25) checkAchievement(AchievementTrigger.MONSTERS_BRED_25, stats.monstersBred)
                if (stats.monstersBred >= 100) checkAchievement(AchievementTrigger.MONSTERS_BRED_100, stats.monstersBred)
            }
            else -> { /* Other statistic triggers */ }
        }
    }

    private fun createAchievements(): List<Achievement> = listOf(
        Achievement(
            id = "first_victory",
            title = "First Victory",
            description = "Win your first battle",
            trigger = AchievementTrigger.FIRST_BATTLE_WIN,
            requirement = 1,
            reward = AchievementReward.GOLD(500)
        ),
        Achievement(
            id = "monster_master",
            title = "Monster Master",
            description = "Catch 50 different monster types",
            trigger = AchievementTrigger.MONSTERS_CAUGHT_50,
            requirement = 50,
            reward = AchievementReward.ITEM("Master Ball")
        ),
        Achievement(
            id = "breeding_expert",
            title = "Breeding Expert",
            description = "Successfully breed 25 monsters",
            trigger = AchievementTrigger.MONSTERS_BRED_25,
            requirement = 25,
            reward = AchievementReward.TITLE("Breeder")
        ),
        Achievement(
            id = "tournament_champion",
            title = "Tournament Champion",
            description = "Win 10 tournament battles",
            trigger = AchievementTrigger.TOURNAMENTS_WON_10,
            requirement = 10,
            reward = AchievementReward.GOLD(2000)
        ),
        Achievement(
            id = "synthesis_master",
            title = "Synthesis Master",
            description = "Perform 20 monster synthesis combinations",
            trigger = AchievementTrigger.SYNTHESIS_PERFORMED_20,
            requirement = 20,
            reward = AchievementReward.ITEM("Synthesis Booster")
        ),
        Achievement(
            id = "dungeon_explorer",
            title = "Dungeon Explorer",
            description = "Clear 5 different dungeons",
            trigger = AchievementTrigger.DUNGEONS_CLEARED_5,
            requirement = 5,
            reward = AchievementReward.TITLE("Explorer")
        ),
        Achievement(
            id = "battle_veteran",
            title = "Battle Veteran",
            description = "Win 100 battles",
            trigger = AchievementTrigger.BATTLES_WON_100,
            requirement = 100,
            reward = AchievementReward.ITEM("Veteran Badge")
        ),
        Achievement(
            id = "legendary_trainer",
            title = "Legendary Trainer",
            description = "Win 500 battles",
            trigger = AchievementTrigger.BATTLES_WON_500,
            requirement = 500,
            reward = AchievementReward.TITLE("Legend")
        ),
        Achievement(
            id = "ultimate_master",
            title = "Ultimate Master",
            description = "Win 1000 battles",
            trigger = AchievementTrigger.BATTLES_WON_1000,
            requirement = 1000,
            reward = AchievementReward.SPECIAL("Ultimate Crown")
        ),
        Achievement(
            id = "collector_supreme",
            title = "Collector Supreme",
            description = "Catch 150 different monsters",
            trigger = AchievementTrigger.MONSTERS_CAUGHT_150,
            requirement = 150,
            reward = AchievementReward.TITLE("Supreme Collector")
        )
    )
    
    /**
     * Get all achievements
     */
    fun getAllAchievements(): List<Achievement> {
        return availableAchievements
    }
    
    /**
     * Get player statistics
     */
    fun getPlayerStatistics(): GameStatistics {
        return _statistics.value
    }
    
    /**
     * Get auto-battle options
     */
    fun getAutoBattleOptions(): Map<String, Boolean> {
        return mapOf(
            "auto_attack" to true,
            "auto_skill" to true,
            "auto_item" to true,
            "fast_forward" to true,
            "skip_animations" to true
        )
    }
    
    /**
     * Get UI enhancements
     */
    fun getUIEnhancements(): Map<String, Boolean> {
        return mapOf(
            "quick_menus" to true,
            "hotkeys" to true,
            "custom_themes" to true,
            "advanced_filters" to true,
            "batch_operations" to true
        )
    }
}

// Advanced AI System
class AdvancedAI {
    fun selectStrategy(personality: AIPersonality, playerParty: List<Any>, difficulty: Int): AIStrategy {
        val baseStrategy = when (personality) {
            AIPersonality.AGGRESSIVE -> AIStrategy.OFFENSIVE
            AIPersonality.DEFENSIVE -> AIStrategy.TANK
            AIPersonality.SWIFT -> AIStrategy.SPEED_CONTROL
            AIPersonality.STURDY -> AIStrategy.WALL
            AIPersonality.ENERGETIC -> AIStrategy.PRESSURE
            AIPersonality.CALM -> AIStrategy.BALANCED
            AIPersonality.CALCULATING -> AIStrategy.SETUP_SWEEP
            AIPersonality.MYSTERIOUS -> AIStrategy.UNPREDICTABLE
            AIPersonality.NOBLE -> AIStrategy.HONOR_DUEL
            AIPersonality.METHODICAL -> AIStrategy.SYSTEMATIC
            AIPersonality.PRECISE -> AIStrategy.TECHNICAL
            AIPersonality.LEGENDARY -> AIStrategy.ADAPTIVE
        }

        // Modify strategy based on difficulty and player party composition
        return enhanceStrategyForDifficulty(baseStrategy, difficulty, playerParty.size)
    }

    private fun enhanceStrategyForDifficulty(baseStrategy: AIStrategy, difficulty: Int, partySize: Int): AIStrategy {
        return when {
            difficulty >= 8 -> AIStrategy.LEGENDARY_MASTER
            difficulty >= 6 -> AIStrategy.EXPERT_TACTICAL
            difficulty >= 4 -> AIStrategy.ADVANCED_COMBO
            else -> baseStrategy
        }
    }
}

// Data Classes
data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val trigger: AchievementTrigger,
    val requirement: Int,
    val reward: AchievementReward,
    var progress: Int = 0,
    var isUnlocked: Boolean = false,
    val icon: String = "achievement_${id}",
    val rarity: AchievementRarity = AchievementRarity.COMMON
)

data class GameStatistics(
    val battlesWon: Int = 0,
    val battlesLost: Int = 0,
    val monstersCaught: Int = 0,
    val monstersBred: Int = 0,
    val dungeonsCleared: Int = 0,
    val tournamentsWon: Int = 0,
    val synthesisPerformed: Int = 0,
    val playtimeMinutes: Int = 0,
    val goldEarned: Int = 0,
    val experienceGained: Int = 0,
    val perfectBattles: Int = 0,
    val criticalHits: Int = 0,
    val itemsUsed: Int = 0,
    val distanceTraveled: Int = 0
) {
    val winRate: Double get() = if (battlesWon + battlesLost > 0) battlesWon.toDouble() / (battlesWon + battlesLost) else 0.0
    val averageBattleTime: Double get() = if (battlesWon > 0) playtimeMinutes.toDouble() / battlesWon else 0.0
}

data class VoiceSettings(
    val enableVoiceActing: Boolean = true,
    val enableMonsterCries: Boolean = true,
    val voiceVolume: Float = 0.8f,
    val effectsVolume: Float = 0.7f,
    val voiceLanguage: VoiceLanguage = VoiceLanguage.ENGLISH
)

data class AnimationSettings(
    val enableAnimations: Boolean = true,
    val animationSpeed: Float = 1.0f,
    val enableBattleAnimations: Boolean = true,
    val enableUIAnimations: Boolean = true,
    val enableParticleEffects: Boolean = true,
    val animationQuality: AnimationQuality = AnimationQuality.HIGH
)

data class AnimationData(
    val animationId: String,
    val duration: Int,
    val isLooping: Boolean,
    val priority: Int = 0
)

data class VoiceData(
    val voiceId: String,
    val character: VoiceCharacter,
    val lineType: VoiceLineType,
    val volume: Float
)

data class SoundData(
    val soundId: String,
    val volume: Float,
    val pitch: Float = 1.0f
)

data class VoiceCharacter(
    val name: String,
    val voiceActor: String,
    val characterType: CharacterType
)

// Enums
enum class AchievementTrigger {
    FIRST_BATTLE_WIN,
    BATTLES_WON_100,
    BATTLES_WON_500,
    BATTLES_WON_1000,
    MONSTERS_CAUGHT_50,
    MONSTERS_CAUGHT_150,
    MONSTERS_BRED_25,
    MONSTERS_BRED_100,
    TOURNAMENTS_WON_10,
    SYNTHESIS_PERFORMED_20,
    DUNGEONS_CLEARED_5,
    PERFECT_BATTLE,
    LEGENDARY_MONSTER_CAUGHT,
    MAX_LEVEL_REACHED,
    ALL_TYPES_CAUGHT
}

enum class AchievementRarity {
    COMMON, RARE, EPIC, LEGENDARY, MYTHIC
}

sealed class AchievementReward {
    data class GOLD(val amount: Int) : AchievementReward()
    data class ITEM(val itemName: String) : AchievementReward()
    data class TITLE(val titleName: String) : AchievementReward()
    data class SPECIAL(val specialReward: String) : AchievementReward()
}

enum class StatisticType {
    BATTLES_WON, BATTLES_LOST, MONSTERS_CAUGHT, MONSTERS_BRED,
    DUNGEONS_CLEARED, TOURNAMENTS_WON, SYNTHESIS_PERFORMED,
    PLAYTIME_MINUTES, GOLD_EARNED, EXPERIENCE_GAINED
}

enum class AIPersonality {
    AGGRESSIVE, DEFENSIVE, SWIFT, STURDY, ENERGETIC, CALM,
    CALCULATING, MYSTERIOUS, NOBLE, METHODICAL, PRECISE, LEGENDARY
}

enum class AIStrategy {
    OFFENSIVE, TANK, SPEED_CONTROL, WALL, PRESSURE, BALANCED,
    SETUP_SWEEP, UNPREDICTABLE, HONOR_DUEL, SYSTEMATIC, TECHNICAL,
    ADAPTIVE, LEGENDARY_MASTER, EXPERT_TACTICAL, ADVANCED_COMBO
}

enum class AnimationType {
    IDLE, ATTACK, SKILL, VICTORY, FAINT, ENTRY
}

enum class VoiceLineType {
    GREETING, BATTLE_START, VICTORY, DEFEAT, SKILL_USE,
    ITEM_USE, LEVEL_UP, EVOLUTION, CRITICAL_HIT, STORY
}

enum class VoiceLanguage {
    ENGLISH, JAPANESE, SPANISH, FRENCH, GERMAN
}

enum class AnimationQuality {
    LOW, MEDIUM, HIGH, ULTRA
}

enum class CharacterType {
    MAIN_CHARACTER, NPC, RIVAL, MASTER, SHOPKEEPER, NARRATOR
}