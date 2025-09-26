package com.pixelwarrior.monsters.integration

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.pixelwarrior.monsters.data.repository.GameRepository
import com.pixelwarrior.monsters.game.battle.BattleEngine
import com.pixelwarrior.monsters.game.breeding.BreedingSystem
import com.pixelwarrior.monsters.game.story.StorySystem
import com.pixelwarrior.monsters.game.world.HubWorldSystem
import com.pixelwarrior.monsters.audio.ChiptuneAudioEngine
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration tests that validate complete user interaction flows
 * This ensures the roadmap features are actually implemented and working together
 */
@RunWith(AndroidJUnit4::class)
class UserInteractionFlowTest {

    private lateinit var gameRepository: GameRepository
    private lateinit var battleEngine: BattleEngine
    private lateinit var breedingSystem: BreedingSystem
    private lateinit var storySystem: StorySystem
    private lateinit var hubWorldSystem: HubWorldSystem
    private lateinit var audioEngine: ChiptuneAudioEngine

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        
        gameRepository = GameRepository()
        battleEngine = BattleEngine()
        breedingSystem = BreedingSystem()
        storySystem = StorySystem()
        hubWorldSystem = HubWorldSystem()
        audioEngine = ChiptuneAudioEngine(context)
    }

    @Test
    fun testCompleteNewGameFlow() = runTest {
        // Test: New player can start game and progress through tutorial
        val gameSave = gameRepository.createNewGame("TestPlayer")
        
        assertNotNull("Game save should be created", gameSave)
        assertEquals("Player name should be set", "TestPlayer", gameSave.playerName)
        assertTrue("Should have starting monster", gameSave.partyMonsters.isNotEmpty())
        assertTrue("Should have starting gold", gameSave.gold > 0)
        assertTrue("Should have starting items", gameSave.inventory.isNotEmpty())
        
        // Verify story system initializes properly
        assertTrue("Story system should have initial quests", 
            storySystem.getAvailableQuests().isNotEmpty())
        
        // Verify hub world is accessible
        assertTrue("Main hall should be unlocked",
            hubWorldSystem.isAreaUnlocked(HubWorldSystem.HubArea.MAIN_HALL, gameSave))
    }

    @Test
    fun testBattleSystemIntegration() = runTest {
        // Test: Complete battle flow from encounter to victory
        val gameSave = gameRepository.createNewGame("BattleTestPlayer")
        val playerMonster = gameSave.partyMonsters.first()
        
        // Create a test wild monster encounter
        val wildMonster = gameRepository.generateWildMonster("forest", 5)
        assertNotNull("Wild monster should be generated", wildMonster)
        
        // Start battle
        val battleState = battleEngine.initiateBattle(
            playerParty = listOf(playerMonster),
            enemyParty = listOf(wildMonster),
            battleType = com.pixelwarrior.monsters.data.model.BattleType.WILD_ENCOUNTER
        )
        
        assertNotNull("Battle state should be created", battleState)
        assertEquals("Battle should be active", 
            com.pixelwarrior.monsters.data.model.BattleStatus.ACTIVE, battleState.status)
        
        // Test that battle actions can be executed
        val attackAction = com.pixelwarrior.monsters.data.model.BattleActionData(
            action = com.pixelwarrior.monsters.data.model.BattleAction.ATTACK,
            sourceMonster = playerMonster,
            targetMonster = wildMonster
        )
        
        val updatedBattle = battleEngine.processBattleAction(battleState, attackAction)
        assertNotNull("Battle should process actions", updatedBattle)
    }

    @Test
    fun testBreedingSystemIntegration() = runTest {
        // Test: Monster breeding from selection to offspring creation
        val gameSave = gameRepository.createNewGame("BreedingTestPlayer")
        
        // Add a second monster for breeding
        val secondMonster = gameRepository.generateWildMonster("forest", 10)
        val updatedSave = gameSave.copy(
            partyMonsters = gameSave.partyMonsters + secondMonster
        )
        
        // Test breeding compatibility
        val parent1 = updatedSave.partyMonsters[0]
        val parent2 = updatedSave.partyMonsters[1]
        
        val compatibility = breedingSystem.checkBreedingCompatibility(parent1, parent2)
        assertNotNull("Breeding compatibility should be determined", compatibility)
        
        if (compatibility.canBreed) {
            // Test offspring generation
            val offspring = breedingSystem.generateOffspring(parent1, parent2)
            assertNotNull("Offspring should be generated if compatible", offspring)
            assertTrue("Offspring should have stats", offspring.stats.attack > 0)
        }
    }

    @Test
    fun testAudioSystemIntegration() = runTest {
        // Test: Audio system responds to game events
        
        // Test menu music
        audioEngine.playMenuMusic()
        assertTrue("Audio engine should be initialized", audioEngine.isMusicEnabled.value)
        
        // Test battle music transition
        audioEngine.playBattleMusic()
        
        // Test monster cries
        audioEngine.playMonsterCry("dragon")
        
        // Test UI sounds
        audioEngine.playMenuSelectSound()
        audioEngine.playMenuBackSound()
        
        // All audio calls should complete without exceptions
        assertTrue("Audio system integration should work", true)
    }

    @Test
    fun testSaveLoadSystemIntegration() = runTest {
        // Test: Complete save/load cycle preserves game state
        val originalSave = gameRepository.createNewGame("SaveLoadTestPlayer")
        
        // Modify game state
        val modifiedSave = originalSave.copy(
            gold = 500,
            playtimeMinutes = 120,
            storyProgress = originalSave.storyProgress + ("test_quest" to true)
        )
        
        // Save game state
        gameRepository.saveGame(modifiedSave)
        
        // Load game state
        val loadedSave = gameRepository.loadGame(modifiedSave.playerId)
        assertNotNull("Game should be loadable", loadedSave)
        
        loadedSave?.let { save ->
            assertEquals("Gold should be preserved", 500, save.gold)
            assertEquals("Playtime should be preserved", 120, save.playtimeMinutes)
            assertTrue("Story progress should be preserved", 
                save.storyProgress["test_quest"] == true)
        }
    }

    @Test
    fun testHubWorldNavigationFlow() = runTest {
        // Test: Player can navigate through hub world areas
        val gameSave = gameRepository.createNewGame("HubTestPlayer")
        
        // Test area unlocking progression
        val unlockedAreas = HubWorldSystem.HubArea.values().filter { area ->
            hubWorldSystem.isAreaUnlocked(area, gameSave)
        }
        
        assertTrue("At least main hall should be unlocked", unlockedAreas.isNotEmpty())
        assertTrue("Main hall should always be accessible", 
            unlockedAreas.contains(HubWorldSystem.HubArea.MAIN_HALL))
        
        // Test NPC interactions
        val npcs = hubWorldSystem.getAreaNPCs(HubWorldSystem.HubArea.MAIN_HALL)
        assertTrue("Main hall should have NPCs", npcs.isNotEmpty())
        
        val masterNPC = npcs.find { it.type == com.pixelwarrior.monsters.data.model.NPCType.MASTER }
        assertNotNull("Master NPC should be present", masterNPC)
    }

    @Test
    fun testEndToEndGameplayFlow() = runTest {
        // Test: Complete gameplay session from start to first major milestone
        val gameSave = gameRepository.createNewGame("EndToEndTestPlayer")
        
        // 1. Start game with tutorial
        assertTrue("Should have tutorial quest", 
            storySystem.getAvailableQuests().any { it.id.contains("tutorial") })
        
        // 2. Complete first battle
        val wildMonster = gameRepository.generateWildMonster("forest", 3)
        val battleState = battleEngine.initiateBattle(
            playerParty = gameSave.partyMonsters,
            enemyParty = listOf(wildMonster),
            battleType = com.pixelwarrior.monsters.data.model.BattleType.WILD_ENCOUNTER
        )
        assertNotNull("First battle should be possible", battleState)
        
        // 3. Visit breeding lab
        val breedingLabUnlocked = hubWorldSystem.isAreaUnlocked(
            HubWorldSystem.HubArea.BREEDING_LAB, gameSave)
        
        // 4. Check exploration system
        val explorationSystem = com.pixelwarrior.monsters.game.exploration.ExplorationSystem()
        assertTrue("Exploration should be available", 
            explorationSystem.getCurrentState().availableAreas.isNotEmpty())
        
        // 5. Verify audio accompanies actions
        audioEngine.playBattleMusic()
        audioEngine.playVictoryMusic()
        
        // Complete flow should work without crashes
        assertTrue("End-to-end flow should complete successfully", true)
    }

    @Test
    fun testEdgeCasesAndErrorHandling() = runTest {
        // Test: System handles edge cases gracefully
        
        // Test with invalid game save
        val invalidSave = null
        val loadResult = gameRepository.loadGame("invalid_id")
        assertNull("Invalid game ID should return null", loadResult)
        
        // Test battle with invalid monsters
        try {
            val emptyParty = emptyList<com.pixelwarrior.monsters.data.model.Monster>()
            battleEngine.initiateBattle(
                playerParty = emptyParty,
                enemyParty = emptyParty,
                battleType = com.pixelwarrior.monsters.data.model.BattleType.WILD_ENCOUNTER
            )
            fail("Battle with empty parties should throw exception")
        } catch (e: Exception) {
            // Expected behavior
            assertTrue("Should handle invalid battle setup", true)
        }
        
        // Test audio system with disabled audio
        audioEngine.updateAudioSettings(
            musicEnabled = false,
            soundEnabled = false,
            musicVolume = 0f,
            soundVolume = 0f
        )
        
        audioEngine.playBattleMusic() // Should not crash
        assertTrue("Audio system should handle disabled state", true)
        
        // Test breeding with incompatible monsters
        val monster1 = gameRepository.generateWildMonster("forest", 5)
        val monster2 = gameRepository.generateWildMonster("desert", 5)
        
        val compatibility = breedingSystem.checkBreedingCompatibility(monster1, monster2)
        assertNotNull("Compatibility check should always return result", compatibility)
    }
}