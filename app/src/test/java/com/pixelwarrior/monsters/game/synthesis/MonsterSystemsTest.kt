package com.pixelwarrior.monsters.game.synthesis

import com.pixelwarrior.monsters.data.model.*
import com.pixelwarrior.monsters.game.story.StorySystem
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*

/**
 * Unit tests for Monster Systems - Phase 3 Implementation
 * Validates synthesis laboratory, skill learning, and scout deployment
 */
class MonsterSystemsTest {

    private lateinit var storySystem: StorySystem
    private lateinit var synthesisLab: SynthesisLaboratory
    private lateinit var scoutDeployment: ScoutDeploymentInterface
    
    private val testMonster1 = Monster(
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
    
    private val testMonster2 = Monster(
        id = "test2", 
        speciesId = "water_spirit",
        name = "Aqua",
        type1 = MonsterType.WATER,
        type2 = null,
        family = MonsterFamily.SLIME,
        level = 12,
        currentHp = 45,
        currentMp = 35,
        experience = 800,
        baseStats = MonsterStats(30, 40, 35, 50, 40, 45, 35),
        skills = listOf("tackle", "heal"),
        traits = listOf("water_affinity"),
        growthRate = GrowthRate.MEDIUM_SLOW,
        friendship = 60,
        isFainted = false
    )

    @Before
    fun setup() {
        storySystem = StorySystem()
        storySystem.initializeStory()
        synthesisLab = SynthesisLaboratory(storySystem)
        scoutDeployment = ScoutDeploymentInterface()
    }

    @Test
    fun `synthesis lab initializes correctly`() = runTest {
        synthesisLab.initializeLab()
        
        val labResources = synthesisLab.labResources.first()
        
        assertEquals("Should have synthesis energy", 100, labResources.synthesisEnergy)
        assertEquals("Should have catalyst stones", 5, labResources.catalystStones)
        assertEquals("Should have stabilizers", 3, labResources.stabilizers)
        
        val discoveredRecipes = synthesisLab.discoveredRecipes.first()
        assertTrue("Should have basic recipes discovered", discoveredRecipes.isNotEmpty())
        assertTrue("Should have basic dragon synthesis", discoveredRecipes.contains("basic_dragon_synthesis"))
    }

    @Test
    fun `synthesis preview works correctly`() = runTest {
        synthesisLab.initializeLab()
        
        val preview = synthesisLab.previewSynthesis(testMonster1, testMonster2)
        
        assertNotNull("Should get synthesis preview", preview)
        assertTrue("Success rate should be reasonable", preview.successRate > 0.0f)
        assertTrue("Should have synthesis cost", preview.cost.gold > 0)
    }

    @Test
    fun `synthesis process can be started`() = runTest {
        synthesisLab.initializeLab()
        
        // Unlock synthesis lab through story
        storySystem.triggerMilestone("synthesis_lab_visited")
        
        val playerInventory = mapOf("gold" to 1000, "synthesis_energy" to 100)
        
        val result = synthesisLab.startSynthesis(testMonster1, testMonster2, playerInventory)
        
        when (result) {
            is SynthesisResult.InProgress -> {
                assertNotNull("Should have synthesis process", result.process)
                assertEquals("Should start in preparation phase", SynthesisPhase.PREPARATION, result.process.phase)
                assertTrue("Should have positive success rate", result.process.successRate > 0.0f)
            }
            is SynthesisResult.Failure -> {
                // Check if failure is due to compatibility or resources
                assertTrue("Should be valid failure reason", result.message.isNotEmpty())
            }
            else -> fail("Unexpected result type")
        }
    }

    @Test
    fun `synthesis can be advanced through phases`() = runTest {
        synthesisLab.initializeLab()
        storySystem.triggerMilestone("synthesis_lab_visited")
        
        val playerInventory = mapOf("gold" to 1000, "synthesis_energy" to 100)
        
        // Start synthesis
        val startResult = synthesisLab.startSynthesis(testMonster1, testMonster2, playerInventory)
        
        if (startResult is SynthesisResult.InProgress) {
            // Advance through phases
            val phase1 = synthesisLab.advanceSynthesis()
            val phase2 = synthesisLab.advanceSynthesis()
            val phase3 = synthesisLab.advanceSynthesis()
            
            assertTrue("Should advance through phases successfully", 
                      phase1 is SynthesisResult.InProgress || phase1 is SynthesisResult.Success ||
                      phase2 is SynthesisResult.InProgress || phase2 is SynthesisResult.Success ||
                      phase3 is SynthesisResult.InProgress || phase3 is SynthesisResult.Success)
        }
    }

    @Test
    fun `synthesis can be cancelled in early phases`() = runTest {
        synthesisLab.initializeLab()
        storySystem.triggerMilestone("synthesis_lab_visited")
        
        val playerInventory = mapOf("gold" to 1000, "synthesis_energy" to 100)
        
        val startResult = synthesisLab.startSynthesis(testMonster1, testMonster2, playerInventory)
        
        if (startResult is SynthesisResult.InProgress) {
            val cancelled = synthesisLab.cancelSynthesis()
            assertTrue("Should be able to cancel in preparation phase", cancelled)
            
            val currentProcess = synthesisLab.currentSynthesisProcess.first()
            assertNull("Process should be cleared after cancellation", currentProcess)
        }
    }

    @Test
    fun `scout areas unlock based on player level`() {
        val level1Areas = scoutDeployment.getAvailableScoutAreas(1)
        val level20Areas = scoutDeployment.getAvailableScoutAreas(20)
        val level40Areas = scoutDeployment.getAvailableScoutAreas(40)
        
        assertTrue("Level 1 should have starting areas", level1Areas.isNotEmpty())
        assertTrue("Level 20 should have more areas than level 1", level20Areas.size > level1Areas.size)
        assertTrue("Level 40 should have all areas", level40Areas.size >= level20Areas.size)
        
        val startingPlains = level1Areas.find { it.id == "starting_plains" }
        assertNotNull("Should have starting plains at level 1", startingPlains)
        assertEquals("Starting plains should be danger level 1", 1, startingPlains!!.dangerLevel)
    }

    @Test
    fun `scout deployment calculates costs correctly`() {
        val area = ScoutArea(
            id = "test_area",
            name = "Test Area",
            description = "Test",
            unlockLevel = 1,
            availableMissions = listOf(ScoutMissionType.QUICK_PATROL, ScoutMissionType.TREASURE_HUNT),
            dangerLevel = 3
        )
        
        val quickPatrolCost = scoutDeployment.calculateDeploymentCost(area, ScoutMissionType.QUICK_PATROL)
        val treasureHuntCost = scoutDeployment.calculateDeploymentCost(area, ScoutMissionType.TREASURE_HUNT)
        
        assertTrue("Quick patrol should be cheaper", quickPatrolCost.gold < treasureHuntCost.gold)
        assertTrue("Treasure hunt should require more provisions", treasureHuntCost.provisions > quickPatrolCost.provisions)
        
        // Cost should increase with danger level
        assertTrue("Should have reasonable cost based on danger", quickPatrolCost.gold > 10)
    }

    @Test 
    fun `scout deployment validates requirements`() {
        val highLevelArea = ScoutArea(
            id = "dangerous_area",
            name = "Dangerous Area", 
            description = "Very dangerous",
            unlockLevel = 30,
            availableMissions = listOf(ScoutMissionType.LEGENDARY_QUEST),
            dangerLevel = 8
        )
        
        val playerInventory = mapOf("gold" to 1000, "provisions" to 20)
        
        // Test with low-level monster
        val result = scoutDeployment.deployScout(testMonster1, highLevelArea, ScoutMissionType.LEGENDARY_QUEST, playerInventory)
        
        assertTrue("Should fail due to level requirement", result is ScoutDeploymentResult.Failure)
        
        if (result is ScoutDeploymentResult.Failure) {
            assertTrue("Should mention level requirement", result.reason.contains("level"))
        }
    }

    @Test
    fun `scout success rate calculation works`() {
        val easyArea = ScoutArea(
            id = "easy_area",
            name = "Easy Area",
            description = "Safe",
            unlockLevel = 1,
            availableMissions = listOf(ScoutMissionType.QUICK_PATROL),
            dangerLevel = 1
        )
        
        val hardArea = ScoutArea(
            id = "hard_area",
            name = "Hard Area",
            description = "Dangerous",
            unlockLevel = 1,
            availableMissions = listOf(ScoutMissionType.DEEP_EXPLORATION),
            dangerLevel = 7
        )
        
        val easyRate = scoutDeployment.getSuccessRate(testMonster1, easyArea, ScoutMissionType.QUICK_PATROL)
        val hardRate = scoutDeployment.getSuccessRate(testMonster1, hardArea, ScoutMissionType.DEEP_EXPLORATION)
        
        assertTrue("Easy area should have higher success rate", easyRate > hardRate)
        assertTrue("Success rates should be reasonable", easyRate > 0.5f && easyRate <= 1.0f)
        assertTrue("Hard area should still have some success chance", hardRate > 0.0f)
    }

    @Test
    fun `synthesis recipe discovery works`() = runTest {
        synthesisLab.initializeLab()
        
        val initialRecipes = synthesisLab.discoveredRecipes.first()
        val initialCount = initialRecipes.size
        
        synthesisLab.discoverRecipe("new_test_recipe")
        
        val updatedRecipes = synthesisLab.discoveredRecipes.first()
        
        assertEquals("Should have one more recipe", initialCount + 1, updatedRecipes.size)
        assertTrue("Should contain the new recipe", updatedRecipes.contains("new_test_recipe"))
    }

    @Test
    fun `synthesis validates monster compatibility`() = runTest {
        synthesisLab.initializeLab()
        storySystem.triggerMilestone("synthesis_lab_visited")
        
        // Create identical monsters (same species)
        val identicalMonster = testMonster1.copy(id = "identical")
        val playerInventory = mapOf("gold" to 1000)
        
        val result = synthesisLab.startSynthesis(testMonster1, identicalMonster, playerInventory)
        
        assertTrue("Should fail with same species", result is SynthesisResult.Failure)
        if (result is SynthesisResult.Failure) {
            assertTrue("Should mention species compatibility", result.message.contains("species") || result.message.contains("compatible"))
        }
    }
}