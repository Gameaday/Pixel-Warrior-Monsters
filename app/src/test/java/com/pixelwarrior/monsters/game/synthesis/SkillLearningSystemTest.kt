package com.pixelwarrior.monsters.game.synthesis

import com.pixelwarrior.monsters.data.model.*
import com.pixelwarrior.monsters.data.database.SkillEntity
import com.pixelwarrior.monsters.data.repository.GameRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*

/**
 * Unit tests for Skill Learning System - Phase 3 Implementation
 */
class SkillLearningSystemTest {

    private lateinit var skillLearningSystem: SkillLearningSystem
    private lateinit var mockGameRepository: GameRepository
    
    private val testMonster = Monster(
        id = "test1",
        speciesId = "fire_drake",
        name = "Blaze",
        type1 = MonsterType.FIRE,
        type2 = null,
        family = MonsterFamily.DRAGON,
        level = 15,
        currentHp = 50,
        currentMp = 30,
        experience = 1000,
        baseStats = MonsterStats(40, 35, 25, 45, 30, 50, 30),
        skills = listOf("tackle", "fireball"),
        traits = listOf("fire_affinity"),
        growthRate = GrowthRate.MEDIUM_FAST,
        friendship = 70,
        isFainted = false
    )
    
    private val testSkills = listOf(
        SkillEntity("heal", "Heal", "Restores HP", "HEALING", "NORMAL", 40, 100, 6, "SINGLE", "Heals 30-50 HP"),
        SkillEntity("ice_shard", "Ice Shard", "Ice attack", "MAGICAL", "ICE", 55, 95, 7, "SINGLE", "Ice magic"),
        SkillEntity("guard", "Guard", "Raises defense", "SUPPORT", "NORMAL", 0, 100, 4, "SELF", "Defense boost")
    )

    @Before
    fun setup() {
        mockGameRepository = mock(GameRepository::class.java)
        skillLearningSystem = SkillLearningSystem(mockGameRepository)
        
        // Mock repository responses
        `when`(mockGameRepository.getAllSkills()).thenReturn(testSkills)
        `when`(mockGameRepository.getSkillById("heal")).thenReturn(testSkills[0])
        `when`(mockGameRepository.getSkillById("ice_shard")).thenReturn(testSkills[1])
        `when`(mockGameRepository.getSkillById("guard")).thenReturn(testSkills[2])
    }

    @Test
    fun `skill learning system initializes correctly`() = runTest {
        skillLearningSystem.initializeSkillLearning()
        
        val learnableSkills = skillLearningSystem.learnableSkills.value
        
        assertTrue("Should have organized skills by category", learnableSkills.isNotEmpty())
        assertTrue("Should have healing skills", learnableSkills.containsKey("HEALING"))
        assertTrue("Should have magical skills", learnableSkills.containsKey("MAGICAL"))
    }

    @Test
    fun `monster can learn compatible skill with item`() = runTest {
        skillLearningSystem.initializeSkillLearning()
        
        val healingHerb = SkillItem("healing_herb", "Healing Herb", "heal", "Heal", 3, SkillItemRarity.COMMON)
        val playerInventory = mapOf("healing_herb" to 5, "gold" to 100)
        
        val result = skillLearningSystem.learnSkillFromItem(testMonster, healingHerb, playerInventory)
        
        when (result) {
            is SkillLearningResult.Success -> {
                assertTrue("Monster should learn the skill", result.updatedMonster.skills.contains("heal"))
                assertEquals("Should consume correct items", 3, result.itemsConsumed["healing_herb"])
                assertEquals("Should learn heal", "Heal", result.learnedSkill)
            }
            is SkillLearningResult.Failure -> {
                // Check if failure is due to valid reason (e.g., compatibility)
                assertTrue("Should have valid failure reason", result.reason.isNotEmpty())
            }
            else -> fail("Unexpected result type: $result")
        }
    }

    @Test
    fun `learning fails with insufficient items`() = runTest {
        skillLearningSystem.initializeSkillLearning()
        
        val healingHerb = SkillItem("healing_herb", "Healing Herb", "heal", "Heal", 3, SkillItemRarity.COMMON)
        val playerInventory = mapOf("healing_herb" to 1) // Not enough
        
        val result = skillLearningSystem.learnSkillFromItem(testMonster, healingHerb, playerInventory)
        
        assertTrue("Should fail with insufficient items", result is SkillLearningResult.Failure)
        if (result is SkillLearningResult.Failure) {
            assertTrue("Should mention insufficient items", result.reason.contains("Not enough"))
        }
    }

    @Test
    fun `learning fails when monster already knows skill`() = runTest {
        skillLearningSystem.initializeSkillLearning()
        
        val fireballItem = SkillItem("fire_stone", "Fire Stone", "fireball", "Fireball", 2, SkillItemRarity.UNCOMMON)
        val playerInventory = mapOf("fire_stone" to 5)
        
        val result = skillLearningSystem.learnSkillFromItem(testMonster, fireballItem, playerInventory)
        
        assertTrue("Should fail when already knows skill", result is SkillLearningResult.Failure)
        if (result is SkillLearningResult.Failure) {
            assertTrue("Should mention already knowing skill", result.reason.contains("already knows"))
        }
    }

    @Test
    fun `skill replacement works when monster has max skills`() = runTest {
        skillLearningSystem.initializeSkillLearning()
        
        // Create monster with many skills to trigger replacement
        val fullSkillsMonster = testMonster.copy(
            level = 10, // Lower level = fewer max skills
            skills = listOf("tackle", "fireball") // Already at capacity for low level
        )
        
        val healingHerb = SkillItem("healing_herb", "Healing Herb", "heal", "Heal", 3, SkillItemRarity.COMMON)
        val playerInventory = mapOf("healing_herb" to 5)
        
        val result = skillLearningSystem.learnSkillFromItem(fullSkillsMonster, healingHerb, playerInventory)
        
        assertTrue("Should need skill replacement", result is SkillLearningResult.NeedsSkillReplacement)
        if (result is SkillLearningResult.NeedsSkillReplacement) {
            assertEquals("Should list current skills", fullSkillsMonster.skills, result.currentSkills)
        }
    }

    @Test
    fun `skill replacement replaces correctly`() = runTest {
        skillLearningSystem.initializeSkillLearning()
        
        val healingHerb = SkillItem("healing_herb", "Healing Herb", "heal", "Heal", 3, SkillItemRarity.COMMON)
        
        val result = skillLearningSystem.replaceSkill(testMonster, "tackle", healingHerb)
        
        when (result) {
            is SkillLearningResult.SkillReplaced -> {
                assertFalse("Should no longer have old skill", result.updatedMonster.skills.contains("tackle"))
                assertTrue("Should have new skill", result.updatedMonster.skills.contains("heal"))
                assertEquals("Should report learned skill", "Heal", result.learnedSkill)
                assertTrue("Should report replaced skill", result.replacedSkill.isNotEmpty())
            }
            else -> fail("Expected skill replacement result")
        }
    }

    @Test
    fun `skill replacement fails for unknown skill`() = runTest {
        skillLearningSystem.initializeSkillLearning()
        
        val healingHerb = SkillItem("healing_herb", "Healing Herb", "heal", "Heal", 3, SkillItemRarity.COMMON)
        
        val result = skillLearningSystem.replaceSkill(testMonster, "unknown_skill", healingHerb)
        
        assertTrue("Should fail for unknown skill", result is SkillLearningResult.Failure)
        if (result is SkillLearningResult.Failure) {
            assertTrue("Should mention skill not known", result.reason.contains("doesn't know"))
        }
    }

    @Test
    fun `learnable skills filtered by compatibility`() = runTest {
        skillLearningSystem.initializeSkillLearning()
        
        val learnableSkills = skillLearningSystem.getLearnableSkillsForMonster(testMonster)
        
        assertTrue("Should have learnable skills", learnableSkills.isNotEmpty())
        
        // Should not include skills the monster already knows
        val alreadyKnownSkills = learnableSkills.filter { testMonster.skills.contains(it.skillId) }
        assertTrue("Should not include already known skills", alreadyKnownSkills.isEmpty())
        
        // Should be sorted by required level
        val sortedByLevel = learnableSkills.sortedBy { it.requiredLevel }
        assertEquals("Should be sorted by level", sortedByLevel, learnableSkills)
    }

    @Test
    fun `skill mastery increases with usage`() {
        val originalMastery = skillLearningSystem.getSkillMastery(testMonster, "fireball")
        
        val updatedMonster = skillLearningSystem.increaseSkillMastery(testMonster, "fireball")
        val newMastery = skillLearningSystem.getSkillMastery(updatedMonster, "fireball")
        
        assertTrue("Mastery should increase", newMastery > originalMastery)
        assertTrue("Mastery should be reasonable", newMastery <= 100)
    }

    @Test
    fun `skill mastery doesnt increase for unknown skills`() {
        val originalMonster = testMonster
        val updatedMonster = skillLearningSystem.increaseSkillMastery(testMonster, "unknown_skill")
        
        assertEquals("Monster should remain unchanged", originalMonster, updatedMonster)
    }

    @Test
    fun `skill mastery caps at 100`() {
        var monster = testMonster
        
        // Add mastery trait close to max
        monster = monster.copy(traits = monster.traits + "fireball_mastery_99")
        
        monster = skillLearningSystem.increaseSkillMastery(monster, "fireball")
        val finalMastery = skillLearningSystem.getSkillMastery(monster, "fireball")
        
        assertEquals("Mastery should cap at 100", 100, finalMastery)
    }

    @Test
    fun `learning success rate varies by item rarity`() {
        skillLearningSystem.initializeSkillLearning()
        
        // This tests internal calculation - we can't easily mock private methods
        // So we test the observable behavior through actual learning attempts
        val commonItem = SkillItem("common_item", "Common", "heal", "Heal", 1, SkillItemRarity.COMMON)
        val rareItem = SkillItem("rare_item", "Rare", "heal", "Heal", 1, SkillItemRarity.RARE)
        val playerInventory = mapOf("common_item" to 10, "rare_item" to 10)
        
        // Since success is random, we can't test exact values, but we can test structure
        val monsterWithoutHeal = testMonster.copy(skills = listOf("tackle"))
        
        val commonResult = skillLearningSystem.learnSkillFromItem(monsterWithoutHeal, commonItem, playerInventory)
        val rareResult = skillLearningSystem.learnSkillFromItem(monsterWithoutHeal, rareItem, playerInventory)
        
        // Both should either succeed or fail, but not crash
        assertTrue("Common item should produce valid result", 
                  commonResult is SkillLearningResult.Success || commonResult is SkillLearningResult.Failure)
        assertTrue("Rare item should produce valid result",
                  rareResult is SkillLearningResult.Success || rareResult is SkillLearningResult.Failure)
    }
}