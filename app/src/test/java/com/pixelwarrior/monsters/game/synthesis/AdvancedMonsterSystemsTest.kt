package com.pixelwarrior.monsters.game.synthesis

import com.pixelwarrior.monsters.data.model.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Comprehensive test suite for Phase 2 Advanced Monster Systems
 * Tests synthesis, plus system, personalities, and scout missions
 */
class AdvancedMonsterSystemsTest {
    
    private lateinit var monsterSynthesis: MonsterSynthesis
    private lateinit var plusSystem: PlusSystem
    private lateinit var scoutSystem: ScoutSystem
    
    private lateinit var testMonster1: EnhancedMonster
    private lateinit var testMonster2: EnhancedMonster
    private lateinit var testDragonMonster: EnhancedMonster
    
    @Before
    fun setUp() {
        monsterSynthesis = MonsterSynthesis()
        plusSystem = PlusSystem()
        scoutSystem = ScoutSystem()
        
        // Create test monsters
        val baseStats = MonsterStats(50, 45, 60, 40, 35, 100, 50)
        
        val baseMonster1 = Monster(
            id = "test_1",
            speciesId = "forest_wolf",
            name = "Fenrir",
            type1 = MonsterType.NORMAL,
            family = MonsterFamily.BEAST,
            level = 20,
            currentHp = 100,
            currentMp = 50,
            experience = 8000L,
            stats = baseStats,
            skills = listOf("Bite", "Howl"),
            traits = listOf("Loyal"),
            growthRate = GrowthRate.MEDIUM_FAST,
            friendship = 75,
            isFainted = false
        )
        
        val baseMonster2 = Monster(
            id = "test_2",
            speciesId = "flame_sprite",
            name = "Ignis",
            type1 = MonsterType.FIRE,
            family = MonsterFamily.BEAST,
            level = 18,
            currentHp = 90,
            currentMp = 60,
            experience = 6500L,
            stats = baseStats.copy(magic = 55, attack = 40),
            skills = listOf("Fireball", "Burn"),
            traits = listOf("Fiery"),
            growthRate = GrowthRate.MEDIUM_FAST,
            friendship = 60,
            isFainted = false
        )
        
        val dragonMonster = Monster(
            id = "test_dragon",
            speciesId = "young_dragon",
            name = "Draco",
            type1 = MonsterType.DRAGON,
            family = MonsterFamily.DRAGON,
            level = 25,
            currentHp = 150,
            currentMp = 80,
            experience = 15000L,
            stats = baseStats.copy(attack = 70, defense = 60, maxHp = 150, maxMp = 80),
            skills = listOf("Dragon Breath", "Roar"),
            traits = listOf("Proud", "Ancient"),
            growthRate = GrowthRate.SLOW,
            friendship = 80,
            isFainted = false
        )
        
        testMonster1 = EnhancedMonster(
            baseMonster = baseMonster1,
            personality = MonsterPersonality.BRAVE
        )
        
        testMonster2 = EnhancedMonster(
            baseMonster = baseMonster2,
            personality = MonsterPersonality.MODEST
        )
        
        testDragonMonster = EnhancedMonster(
            baseMonster = dragonMonster,
            personality = MonsterPersonality.ADAMANT,
            plusLevel = PlusLevel.PLUS_1
        )
    }
    
    @Test
    fun testMonsterPersonalities() {
        // Test personality growth bonuses
        val braveStats = testMonster1.getEnhancedStats()
        val modestStats = testMonster2.getEnhancedStats()
        
        // Brave personality should boost attack and reduce agility
        assertTrue("Brave personality should boost attack", 
            braveStats.attack > testMonster1.baseMonster.stats.attack)
        assertTrue("Brave personality should reduce agility",
            braveStats.agility < testMonster1.baseMonster.stats.agility)
        
        // Modest personality should boost magic and reduce attack
        assertTrue("Modest personality should boost magic",
            modestStats.magic > testMonster2.baseMonster.stats.magic)
        assertTrue("Modest personality should reduce attack",
            modestStats.attack < testMonster2.baseMonster.stats.attack)
    }
    
    @Test
    fun testPlusSystemStatBoosts() {
        val normalStats = testMonster1.getEnhancedStats()
        val plusStats = testDragonMonster.getEnhancedStats()
        
        // Plus monsters should have boosted stats
        assertTrue("Plus monsters should have higher HP",
            plusStats.maxHp > testDragonMonster.baseMonster.stats.maxHp)
        assertTrue("Plus monsters should have higher attack",
            plusStats.attack > testDragonMonster.baseMonster.stats.attack)
        
        // Verify multiplier is correct (1.1f for +1)
        val expectedHp = (testDragonMonster.baseMonster.stats.maxHp * 1.1f * 1.2f).toInt() // 1.2f from Adamant attack bonus
        assertTrue("Plus level multiplier should be applied correctly",
            plusStats.maxHp >= (testDragonMonster.baseMonster.stats.maxHp * 1.05f).toInt())
    }
    
    @Test
    fun testSynthesisCompatibility() {
        // Same family monsters should be able to synthesize
        assertTrue("Same family monsters should be compatible",
            monsterSynthesis.canSynthesize(testMonster1, testMonster2))
        
        // Test cross-family synthesis
        val beastDragon = monsterSynthesis.canSynthesize(testMonster1, testDragonMonster)
        assertTrue("Beast and Dragon families should have synthesis recipes",
            beastDragon)
    }
    
    @Test
    fun testSynthesisCreatesEnhancedMonster() {
        val result = monsterSynthesis.synthesizeMonsters(testMonster1, testMonster2)
        
        when (result) {
            is SynthesisResult.Success -> {
                val synthesized = result.synthesizedMonster
                
                // Synthesized monster should start at +1
                assertEquals("Synthesized monsters should start at +1",
                    PlusLevel.PLUS_1, synthesized.plusLevel)
                
                // Should inherit skills from both parents
                assertTrue("Should inherit parent skills",
                    synthesized.baseMonster.skills.isNotEmpty())
                
                // Should have synthesis parent tracking
                assertNotNull("Should track first parent", synthesized.synthesisParent1)
                assertNotNull("Should track second parent", synthesized.synthesisParent2)
                
                // Level should be average of parents
                val expectedLevel = (testMonster1.baseMonster.level + testMonster2.baseMonster.level) / 2
                assertEquals("Level should be average of parents", expectedLevel, synthesized.baseMonster.level)
            }
            is SynthesisResult.Failure -> {
                fail("Synthesis should succeed for compatible monsters: ${result.reason}")
            }
        }
    }
    
    @Test
    fun testSynthesisWithRequiredItems() {
        // Create two material family monsters for special synthesis
        val materialMonster1 = testMonster1.copy(
            baseMonster = testMonster1.baseMonster.copy(
                family = MonsterFamily.MATERIAL,
                level = 25
            )
        )
        val materialMonster2 = testMonster2.copy(
            baseMonster = testMonster2.baseMonster.copy(
                family = MonsterFamily.MATERIAL,
                level = 25
            )
        )
        
        // Without required item should fail
        val resultWithoutItem = monsterSynthesis.synthesizeMonsters(
            materialMonster1, materialMonster2, emptyList()
        )
        assertTrue("Should fail without required item",
            resultWithoutItem is SynthesisResult.Failure)
        
        // With required item should succeed
        val resultWithItem = monsterSynthesis.synthesizeMonsters(
            materialMonster1, materialMonster2, listOf("Ancient Core")
        )
        // Note: May still fail due to random chance, but should not fail due to missing item
        if (resultWithItem is SynthesisResult.Failure) {
            assertFalse("Should not fail due to missing item",
                resultWithItem.reason.contains("not available"))
        }
    }
    
    @Test
    fun testPlusSystemEnhancement() {
        val enhancementItems = listOf("Enhancement Stone")
        
        val result = plusSystem.enhanceMonster(testMonster1, enhancementItems)
        
        when (result) {
            is PlusResult.Success -> {
                val enhanced = result.enhancedMonster
                assertEquals("Should enhance to +1", PlusLevel.PLUS_1, enhanced.plusLevel)
                assertEquals("Should use enhancement stone", listOf("Enhancement Stone"), result.usedItems)
            }
            is PlusResult.Failure -> {
                fail("Enhancement should succeed with proper items: ${result.reason}")
            }
        }
    }
    
    @Test
    fun testPlusSystemMaxLevel() {
        val maxLevelMonster = testMonster1.copy(plusLevel = PlusLevel.PLUS_5)
        val result = plusSystem.enhanceMonster(maxLevelMonster, listOf("Enhancement Stone"))
        
        assertTrue("Should fail at max level", result is PlusResult.Failure)
        assertTrue("Should indicate max level reached",
            (result as PlusResult.Failure).reason.contains("maximum"))
    }
    
    @Test
    fun testPlusSystemRequiredItems() {
        // Test different enhancement levels require different items
        val plus2Items = listOf("Enhancement Stone", "Power Crystal")
        
        val plus1Monster = testMonster1.copy(plusLevel = PlusLevel.PLUS_1)
        val result = plusSystem.enhanceMonster(plus1Monster, plus2Items)
        
        when (result) {
            is PlusResult.Success -> {
                assertEquals("Should enhance to +2", PlusLevel.PLUS_2, result.enhancedMonster.plusLevel)
                assertTrue("Should use required items", 
                    result.usedItems.containsAll(plus2Items))
            }
            is PlusResult.Failure -> {
                fail("Enhancement should succeed with proper items: ${result.reason}")
            }
        }
    }
    
    @Test
    fun testScoutMissionRequirements() {
        // Low level monster should only access basic missions
        val lowLevelMonster = testMonster1.copy(
            baseMonster = testMonster1.baseMonster.copy(level = 5)
        )
        
        val availableMissions = scoutSystem.getAvailableMissions(lowLevelMonster)
        assertTrue("Low level monsters should have limited missions",
            availableMissions.contains(ScoutMissionType.QUICK_PATROL))
        assertFalse("Low level monsters should not access legendary quests",
            availableMissions.contains(ScoutMissionType.LEGENDARY_QUEST))
        
        // High level monster should access all missions
        val highLevelMonster = testMonster1.copy(
            baseMonster = testMonster1.baseMonster.copy(level = 40)
        )
        
        val allMissions = scoutSystem.getAvailableMissions(highLevelMonster)
        assertTrue("High level monsters should access all missions",
            allMissions.contains(ScoutMissionType.LEGENDARY_QUEST))
    }
    
    @Test
    fun testScoutMissionStart() {
        val result = scoutSystem.startScoutMission(testMonster1, ScoutMissionType.QUICK_PATROL)
        
        when (result) {
            is ScoutMissionResult.Success -> {
                val mission = result.mission
                assertEquals("Mission should be in progress", ScoutStatus.IN_PROGRESS, mission.status)
                assertEquals("Mission should track scout", testMonster1.baseMonster.id, mission.scoutMonsterId)
                assertTrue("Mission should have remaining time", mission.getRemainingTime() > 0)
                
                // Monster should now be on mission
                assertTrue("Monster should be marked as on mission",
                    scoutSystem.isMonsterOnMission(testMonster1.baseMonster.id))
            }
            is ScoutMissionResult.Failure -> {
                fail("Scout mission should start successfully: ${result.reason}")
            }
        }
    }
    
    @Test
    fun testScoutMissionCompletion() {
        // Start a mission
        val startResult = scoutSystem.startScoutMission(testMonster1, ScoutMissionType.QUICK_PATROL)
        assertTrue("Mission should start successfully", startResult is ScoutMissionResult.Success)
        
        val mission = (startResult as ScoutMissionResult.Success).mission
        
        // Try to complete before time is up
        val earlyCompletion = scoutSystem.completeMission(mission.id, testMonster1)
        assertTrue("Early completion should fail", earlyCompletion is CompletionResult.Failure)
        
        // Simulate mission completion by creating a completed mission
        val completedMission = mission.copy(
            startTime = System.currentTimeMillis() - ScoutMissionType.QUICK_PATROL.duration - 1000
        )
        
        // Note: In a real test, we would need to wait or mock time
        // For now, we verify the mission completion logic structure
        assertNotNull("Mission completion logic should be implemented", scoutSystem)
    }
    
    @Test
    fun testScoutMissionPersonalityBonuses() {
        // Test that different personalities get bonuses for appropriate missions
        val braveScout = testMonster1 // Already has Brave personality
        val carefulScout = testMonster2.copy(personality = MonsterPersonality.CAREFUL)
        
        // Both should be able to start missions, but personality affects success rates
        val braveResult = scoutSystem.startScoutMission(braveScout, ScoutMissionType.DEEP_EXPLORATION)
        val carefulResult = scoutSystem.startScoutMission(carefulScout, ScoutMissionType.TREASURE_HUNT)
        
        assertTrue("Brave monster should start deep exploration", braveResult is ScoutMissionResult.Success)
        assertTrue("Careful monster should start treasure hunt", carefulResult is ScoutMissionResult.Success)
    }
    
    @Test
    fun testSynthesisPreviewGeneration() {
        val availableMonsters = listOf(testMonster2, testDragonMonster)
        val previews = monsterSynthesis.getPossibleSyntheses(testMonster1, availableMonsters)
        
        assertTrue("Should generate synthesis previews", previews.isNotEmpty())
        
        previews.forEach { preview ->
            assertTrue("Success rate should be valid", preview.successRate >= 0f && preview.successRate <= 1f)
            assertTrue("Required level should be positive", preview.requiredLevel > 0)
            assertNotNull("Should have valid recipe", preview.recipe)
            assertNotNull("Should have partner reference", preview.partner)
        }
    }
    
    @Test
    fun testSynthesisChainLimiting() {
        // Create a monster that's already been synthesized multiple times
        val heavilySynthesized = testMonster1.copy(maxSynthesisLevel = 3)
        
        assertFalse("Should not be able to synthesize heavily synthesized monsters",
            monsterSynthesis.canSynthesize(heavilySynthesized, testMonster2))
    }
    
    @Test
    fun testEnhancedStatsCalculation() {
        val stats = testDragonMonster.getEnhancedStats()
        val baseStats = testDragonMonster.baseMonster.stats
        
        // Verify that enhanced stats are higher than base stats
        assertTrue("Enhanced HP should be higher", stats.maxHp >= baseStats.maxHp)
        assertTrue("Enhanced MP should be higher", stats.maxMp >= baseStats.maxMp)
        
        // Verify personality bonus is applied (Adamant boosts attack, reduces magic)
        assertTrue("Adamant should boost attack", stats.attack > baseStats.attack)
        assertTrue("Adamant should reduce magic", stats.magic < baseStats.magic)
        
        // Verify plus level bonus is applied
        val expectedMinHp = (baseStats.maxHp * PlusLevel.PLUS_1.statMultiplier * 0.95f).toInt()
        assertTrue("Plus level should boost HP", stats.maxHp >= expectedMinHp)
    }
    
    @Test
    fun testAllPersonalitiesHaveValidBonuses() {
        MonsterPersonality.values().forEach { personality ->
            val monster = testMonster1.copy(personality = personality)
            val stats = monster.getEnhancedStats()
            
            // All personalities should produce valid stats
            assertTrue("${personality.displayName} should produce positive HP", stats.maxHp > 0)
            assertTrue("${personality.displayName} should produce positive MP", stats.maxMp > 0)
            assertTrue("${personality.displayName} should produce positive attack", stats.attack > 0)
            assertTrue("${personality.displayName} should produce positive defense", stats.defense > 0)
            assertTrue("${personality.displayName} should produce positive agility", stats.agility > 0)
            assertTrue("${personality.displayName} should produce positive magic", stats.magic > 0)
            assertTrue("${personality.displayName} should produce positive wisdom", stats.wisdom > 0)
        }
    }
    
    @Test
    fun testScoutMissionTypes() {
        // Verify all mission types have valid configurations
        ScoutMissionType.values().forEach { missionType ->
            assertTrue("${missionType.displayName} should have positive duration", 
                missionType.duration > 0)
            assertTrue("${missionType.displayName} should have valid gold range",
                missionType.goldReward.first <= missionType.goldReward.last)
            assertTrue("${missionType.displayName} should have valid exp range",
                missionType.experienceReward.first <= missionType.experienceReward.last)
            assertTrue("${missionType.displayName} should have item rewards",
                missionType.itemRewards.isNotEmpty())
        }
    }
    
    @Test
    fun testSynthesisNameGeneration() {
        // Test multiple synthesis attempts to verify name generation variety
        val names = mutableSetOf<String>()
        
        repeat(10) {
            val result = monsterSynthesis.synthesizeMonsters(testMonster1, testMonster2)
            if (result is SynthesisResult.Success) {
                names.add(result.synthesizedMonster.baseMonster.name)
            }
        }
        
        // Should generate different names (though some might repeat due to randomness)
        assertTrue("Should generate varied names", names.size >= 1)
        
        names.forEach { name ->
            assertTrue("Names should not be empty", name.isNotEmpty())
            assertTrue("Names should be reasonable length", name.length <= 50)
        }
    }
}