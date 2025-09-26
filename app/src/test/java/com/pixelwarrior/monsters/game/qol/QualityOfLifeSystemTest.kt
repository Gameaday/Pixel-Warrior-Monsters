package com.pixelwarrior.monsters.game.qol

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.Assert.*

/**
 * Comprehensive test suite for Quality of Life System
 * Tests achievements, statistics tracking, AI strategies, and settings management
 */
class QualityOfLifeSystemTest {

    @Test
    fun `achievement system tracks progress correctly`() = runTest {
        val qolSystem = QualityOfLifeSystem()
        
        // Initially no achievements
        assertEquals(0, qolSystem.achievements.first().size)
        
        // Trigger first battle win
        qolSystem.checkAchievement(AchievementTrigger.FIRST_BATTLE_WIN)
        
        val achievements = qolSystem.achievements.first()
        assertTrue(achievements.any { it.id == "first_victory" && it.isUnlocked })
    }

    @Test
    fun `achievement progress accumulates correctly`() = runTest {
        val qolSystem = QualityOfLifeSystem()
        
        // Check multiple battles won
        repeat(50) {
            qolSystem.checkAchievement(AchievementTrigger.BATTLES_WON_100)
        }
        
        val achievements = qolSystem.achievements.first()
        val battleAchievement = achievements.find { it.id == "battle_veteran" }
        
        assertEquals(50, battleAchievement?.progress ?: 0)
        assertFalse(battleAchievement?.isUnlocked ?: true)
        
        // Complete the achievement
        repeat(50) {
            qolSystem.checkAchievement(AchievementTrigger.BATTLES_WON_100)
        }
        
        val updatedAchievements = qolSystem.achievements.first()
        val completedAchievement = updatedAchievements.find { it.id == "battle_veteran" }
        
        assertEquals(100, completedAchievement?.progress ?: 0)
        assertTrue(completedAchievement?.isUnlocked ?: false)
    }

    @Test
    fun `statistics tracking works correctly`() = runTest {
        val qolSystem = QualityOfLifeSystem()
        
        // Update various statistics
        qolSystem.updateStatistic(StatisticType.BATTLES_WON, 5)
        qolSystem.updateStatistic(StatisticType.BATTLES_LOST, 2)
        qolSystem.updateStatistic(StatisticType.MONSTERS_CAUGHT, 10)
        qolSystem.updateStatistic(StatisticType.GOLD_EARNED, 1000)
        
        val stats = qolSystem.statistics.first()
        
        assertEquals(5, stats.battlesWon)
        assertEquals(2, stats.battlesLost)
        assertEquals(10, stats.monstersCaught)
        assertEquals(1000, stats.goldEarned)
        
        // Test calculated fields
        assertEquals(5.0 / 7.0, stats.winRate, 0.01)
    }

    @Test
    fun `statistics trigger achievements automatically`() = runTest {
        val qolSystem = QualityOfLifeSystem()
        
        // Update battles won to trigger achievement
        qolSystem.updateStatistic(StatisticType.BATTLES_WON, 100)
        
        val achievements = qolSystem.achievements.first()
        assertTrue(achievements.any { it.trigger == AchievementTrigger.BATTLES_WON_100 && it.isUnlocked })
    }

    @Test
    fun `advanced AI selects appropriate strategies`() {
        val qolSystem = QualityOfLifeSystem()
        val playerParty = listOf("Monster1", "Monster2", "Monster3")
        
        // Test different personalities
        val aggressiveStrategy = qolSystem.getAdvancedAIStrategy(
            AIPersonality.AGGRESSIVE, 
            playerParty, 
            difficulty = 3
        )
        assertEquals(AIStrategy.OFFENSIVE, aggressiveStrategy)
        
        val defensiveStrategy = qolSystem.getAdvancedAIStrategy(
            AIPersonality.DEFENSIVE, 
            playerParty, 
            difficulty = 3
        )
        assertEquals(AIStrategy.TANK, defensiveStrategy)
        
        // Test difficulty scaling
        val legendaryStrategy = qolSystem.getAdvancedAIStrategy(
            AIPersonality.LEGENDARY, 
            playerParty, 
            difficulty = 10
        )
        assertEquals(AIStrategy.LEGENDARY_MASTER, legendaryStrategy)
    }

    @Test
    fun `animation system respects settings`() {
        val qolSystem = QualityOfLifeSystem()
        
        // Test with animations enabled
        val animationData = qolSystem.playMonsterAnimation("slime", AnimationType.ATTACK)
        assertNotNull(animationData)
        assertEquals("attack_slime", animationData?.animationId)
        assertEquals(800, animationData?.duration)
        assertFalse(animationData?.isLooping ?: true)
        
        // Test with animations disabled
        qolSystem.updateAnimationSettings(
            AnimationSettings(enableAnimations = false)
        )
        
        val noAnimationData = qolSystem.playMonsterAnimation("slime", AnimationType.ATTACK)
        assertNull(noAnimationData)
    }

    @Test
    fun `voice system respects settings`() {
        val qolSystem = QualityOfLifeSystem()
        val character = VoiceCharacter("Master", "Voice Actor", CharacterType.MASTER)
        
        // Test with voice enabled
        val voiceData = qolSystem.playVoiceLine(character, VoiceLineType.GREETING)
        assertNotNull(voiceData)
        assertEquals("master_greeting", voiceData?.voiceId)
        
        // Test with voice disabled
        qolSystem.updateVoiceSettings(
            VoiceSettings(enableVoiceActing = false)
        )
        
        val noVoiceData = qolSystem.playVoiceLine(character, VoiceLineType.GREETING)
        assertNull(noVoiceData)
    }

    @Test
    fun `monster cry system works correctly`() {
        val qolSystem = QualityOfLifeSystem()
        
        // Test with monster cries enabled
        val cryData = qolSystem.playMonsterCry("Dragon")
        assertNotNull(cryData)
        assertEquals("cry_dragon", cryData?.soundId)
        
        // Test with monster cries disabled
        qolSystem.updateVoiceSettings(
            VoiceSettings(enableMonsterCries = false)
        )
        
        val noCryData = qolSystem.playMonsterCry("Dragon")
        assertNull(noCryData)
    }

    @Test
    fun `achievement rewards are properly configured`() {
        val qolSystem = QualityOfLifeSystem()
        
        // Create test achievements and verify rewards
        val achievements = listOf(
            Achievement(
                id = "test_gold",
                title = "Gold Test",
                description = "Test",
                trigger = AchievementTrigger.FIRST_BATTLE_WIN,
                requirement = 1,
                reward = AchievementReward.GOLD(500)
            ),
            Achievement(
                id = "test_item",
                title = "Item Test", 
                description = "Test",
                trigger = AchievementTrigger.MONSTERS_CAUGHT_50,
                requirement = 50,
                reward = AchievementReward.ITEM("Test Item")
            ),
            Achievement(
                id = "test_title",
                title = "Title Test",
                description = "Test", 
                trigger = AchievementTrigger.TOURNAMENTS_WON_10,
                requirement = 10,
                reward = AchievementReward.TITLE("Test Title")
            )
        )
        
        achievements.forEach { achievement ->
            when (achievement.reward) {
                is AchievementReward.GOLD -> {
                    assertTrue((achievement.reward as AchievementReward.GOLD).amount > 0)
                }
                is AchievementReward.ITEM -> {
                    assertTrue((achievement.reward as AchievementReward.ITEM).itemName.isNotEmpty())
                }
                is AchievementReward.TITLE -> {
                    assertTrue((achievement.reward as AchievementReward.TITLE).titleName.isNotEmpty())
                }
                is AchievementReward.SPECIAL -> {
                    assertTrue((achievement.reward as AchievementReward.SPECIAL).specialReward.isNotEmpty())
                }
            }
        }
    }

    @Test
    fun `animation data properties are correct`() {
        val qolSystem = QualityOfLifeSystem()
        
        // Test different animation types
        val idleAnimation = qolSystem.playMonsterAnimation("test", AnimationType.IDLE)
        assertTrue(idleAnimation?.isLooping ?: false)
        assertEquals(2000, idleAnimation?.duration)
        
        val attackAnimation = qolSystem.playMonsterAnimation("test", AnimationType.ATTACK)
        assertFalse(attackAnimation?.isLooping ?: true)
        assertEquals(800, attackAnimation?.duration)
        
        val skillAnimation = qolSystem.playMonsterAnimation("test", AnimationType.SKILL)
        assertFalse(skillAnimation?.isLooping ?: true)
        assertEquals(1200, skillAnimation?.duration)
    }

    @Test
    fun `settings updates are properly applied`() = runTest {
        val qolSystem = QualityOfLifeSystem()
        
        // Test voice settings update
        val newVoiceSettings = VoiceSettings(
            enableVoiceActing = false,
            enableMonsterCries = true,
            voiceVolume = 0.5f,
            effectsVolume = 0.8f,
            voiceLanguage = VoiceLanguage.JAPANESE
        )
        
        qolSystem.updateVoiceSettings(newVoiceSettings)
        val updatedVoiceSettings = qolSystem.voiceSettings.first()
        
        assertEquals(newVoiceSettings, updatedVoiceSettings)
        
        // Test animation settings update
        val newAnimationSettings = AnimationSettings(
            enableAnimations = true,
            animationSpeed = 1.5f,
            enableBattleAnimations = false,
            enableUIAnimations = true,
            enableParticleEffects = false,
            animationQuality = AnimationQuality.MEDIUM
        )
        
        qolSystem.updateAnimationSettings(newAnimationSettings)
        val updatedAnimationSettings = qolSystem.animationSettings.first()
        
        assertEquals(newAnimationSettings, updatedAnimationSettings)
    }

    @Test
    fun `ai difficulty scaling works correctly`() {
        val advancedAI = AdvancedAI()
        val playerParty = listOf("Monster1", "Monster2")
        
        // Test different difficulty levels
        val easyStrategy = advancedAI.selectStrategy(
            AIPersonality.CALM, 
            playerParty, 
            difficulty = 1
        )
        assertEquals(AIStrategy.BALANCED, easyStrategy)
        
        val mediumStrategy = advancedAI.selectStrategy(
            AIPersonality.CALM, 
            playerParty, 
            difficulty = 5
        )
        assertEquals(AIStrategy.ADVANCED_COMBO, mediumStrategy)
        
        val hardStrategy = advancedAI.selectStrategy(
            AIPersonality.CALM, 
            playerParty, 
            difficulty = 7
        )
        assertEquals(AIStrategy.EXPERT_TACTICAL, hardStrategy)
        
        val legendaryStrategy = advancedAI.selectStrategy(
            AIPersonality.CALM, 
            playerParty, 
            difficulty = 9
        )
        assertEquals(AIStrategy.LEGENDARY_MASTER, legendaryStrategy)
    }

    @Test
    fun `game statistics calculated fields work correctly`() {
        val stats = GameStatistics(
            battlesWon = 75,
            battlesLost = 25,
            playtimeMinutes = 300
        )
        
        assertEquals(0.75, stats.winRate, 0.01)
        assertEquals(4.0, stats.averageBattleTime, 0.01)
        
        // Test edge cases
        val emptyStats = GameStatistics()
        assertEquals(0.0, emptyStats.winRate, 0.01)
        assertEquals(0.0, emptyStats.averageBattleTime, 0.01)
    }

    @Test
    fun `achievement rarity affects display`() {
        val achievements = listOf(
            Achievement("common", "Common", "Test", AchievementTrigger.FIRST_BATTLE_WIN, 1, 
                      AchievementReward.GOLD(100), rarity = AchievementRarity.COMMON),
            Achievement("rare", "Rare", "Test", AchievementTrigger.BATTLES_WON_100, 100, 
                      AchievementReward.GOLD(500), rarity = AchievementRarity.RARE),
            Achievement("legendary", "Legendary", "Test", AchievementTrigger.BATTLES_WON_1000, 1000, 
                      AchievementReward.SPECIAL("Crown"), rarity = AchievementRarity.LEGENDARY)
        )
        
        // Verify rarities are properly set
        assertEquals(AchievementRarity.COMMON, achievements[0].rarity)
        assertEquals(AchievementRarity.RARE, achievements[1].rarity)
        assertEquals(AchievementRarity.LEGENDARY, achievements[2].rarity)
        
        // Legendary achievements should have special rewards
        assertTrue(achievements[2].reward is AchievementReward.SPECIAL)
    }

    @Test
    fun `voice character data is properly structured`() {
        val characters = listOf(
            VoiceCharacter("Hero", "Main VA", CharacterType.MAIN_CHARACTER),
            VoiceCharacter("Master", "Wise VA", CharacterType.MASTER),
            VoiceCharacter("Rival", "Young VA", CharacterType.RIVAL),
            VoiceCharacter("Shopkeeper", "Friendly VA", CharacterType.SHOPKEEPER)
        )
        
        characters.forEach { character ->
            assertTrue(character.name.isNotEmpty())
            assertTrue(character.voiceActor.isNotEmpty())
            assertNotNull(character.characterType)
        }
    }

    @Test
    fun `multiple achievements can be unlocked simultaneously`() = runTest {
        val qolSystem = QualityOfLifeSystem()
        
        // Update statistics that should trigger multiple achievements
        qolSystem.updateStatistic(StatisticType.BATTLES_WON, 100)
        qolSystem.updateStatistic(StatisticType.MONSTERS_CAUGHT, 50)
        
        val achievements = qolSystem.achievements.first()
        
        // Should have unlocked both battle and monster achievements
        assertTrue(achievements.any { it.trigger == AchievementTrigger.BATTLES_WON_100 && it.isUnlocked })
        assertTrue(achievements.any { it.trigger == AchievementTrigger.MONSTERS_CAUGHT_50 && it.isUnlocked })
    }
}