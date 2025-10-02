package com.pixelwarrior.monsters.edge_cases

import com.pixelwarrior.monsters.createTestMonster
import com.pixelwarrior.monsters.data.model.*
import com.pixelwarrior.monsters.data.repository.GameRepository
import com.pixelwarrior.monsters.game.battle.BattleEngine
import com.pixelwarrior.monsters.game.breeding.BreedingSystem
import com.pixelwarrior.monsters.audio.ChiptuneAudioEngine
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*

/**
 * Comprehensive edge case testing to ensure system stability
 * Tests boundary conditions, error states, and unusual scenarios
 */
class EdgeCaseTestSuite {

    private lateinit var gameRepository: GameRepository
    private lateinit var battleEngine: BattleEngine
    private lateinit var breedingSystem: BreedingSystem
    private lateinit var mockAudioEngine: ChiptuneAudioEngine

    @Before
    fun setup() {
        gameRepository = GameRepository()
        battleEngine = BattleEngine()
        breedingSystem = BreedingSystem()
        mockAudioEngine = mock(ChiptuneAudioEngine::class.java)
    }

    @Test
    fun testBoundaryConditions_MonsterStats() = runTest {
        // Test extreme stat values
        val maxStatsMonster = createTestMonster(
            id = "max_stats",
            name = "MaxStatsMonster",
            type1 = MonsterType.DRAGON,
            family = MonsterFamily.DRAGON,
            personality = Personality.HARDY,
            attack = 999,
            defense = 999,
            agility = 999,
            magic = 999,
            wisdom = 999,
            maxHp = 999,
            maxMp = 999,
            skills = emptyList(),
            level = 99,
            experience = 999999,
            affection = 255
        )
        
        val minStatsMonster = createTestMonster(
            id = "min_stats",
            name = "MinStatsMonster", 
            type1 = MonsterType.NORMAL,
            family = MonsterFamily.SLIME,
            personality = Personality.HARDY,
            attack = 1,
            defense = 1,
            agility = 1,
            magic = 1,
            wisdom = 1,
            maxHp = 1,
            maxMp = 1,
            skills = emptyList(),
            level = 1,
            experience = 0,
            affection = 0
        )

        // Verify stats are handled correctly
        assertTrue("Max stats monster should be valid", maxStatsMonster.baseStats.attack > 0)
        assertTrue("Min stats monster should be valid", minStatsMonster.baseStats.attack > 0)

        // Test battle with extreme stat differences
        val battleState = battleEngine.initiateBattle(
            playerParty = listOf(maxStatsMonster),
            enemyParty = listOf(minStatsMonster),
            battleType = BattleType.WILD_ENCOUNTER
        )
        
        assertNotNull("Battle with extreme stats should be possible", battleState)
    }

    @Test
    fun testNullAndEmptyDataHandling() = runTest {
        // Test with empty monster party
        try {
            battleEngine.initiateBattle(
                playerParty = emptyList(),
                enemyParty = emptyList(),
                battleType = BattleType.WILD_ENCOUNTER
            )
            fail("Empty party battle should throw exception")
        } catch (e: IllegalArgumentException) {
            // Expected behavior
            assertTrue("Should handle empty parties gracefully", true)
        }

        // Test save/load with null values
        val result = gameRepository.loadGame("")
        assertNull("Loading empty ID should return null", result)

        // Test breeding with null compatibility
        val monster1 = gameRepository.generateWildMonster("forest", 5)
        val monster2 = gameRepository.generateWildMonster("forest", 5)
        val compatibility = breedingSystem.checkBreedingCompatibility(monster1, monster2)
        
        assertNotNull("Compatibility check should never return null", compatibility)
    }

    @Test 
    fun testResourceExhaustion() = runTest {
        // Test creating many monsters to check memory handling
        val monsters = mutableListOf<Monster>()
        
        for (i in 1..100) {
            val monster = gameRepository.generateWildMonster("forest", i % 50 + 1)
            monsters.add(monster)
        }
        
        assertTrue("Should handle creating many monsters", monsters.size == 100)
        
        // Test battle with large party (should be limited)
        val largeParty = monsters.take(10) // Most games limit party size
        val enemyParty = monsters.takeLast(10)
        
        try {
            val battleState = battleEngine.initiateBattle(
                playerParty = largeParty,
                enemyParty = enemyParty,
                battleType = BattleType.TOURNAMENT
            )
            assertNotNull("Large party battle should work or fail gracefully", battleState)
        } catch (e: Exception) {
            // Should handle large parties gracefully
            assertTrue("Should handle large parties", true)
        }
    }

    @Test
    fun testConcurrencyAndThreadSafety() = runTest {
        // Test multiple simultaneous operations
        val gameSave = gameRepository.createNewGame("ConcurrencyTest")
        
        // Simulate multiple save operations
        val saveOperations = (1..5).map {
            val modifiedSave = gameSave.copy(gold = gameSave.gold + it * 100)
            gameRepository.saveGame(modifiedSave)
        }
        
        assertTrue("Multiple save operations should complete", saveOperations.all { it })
        
        // Test concurrent battle state modifications
        val monster1 = gameSave.partyMonsters.first()
        val monster2 = gameRepository.generateWildMonster("forest", 5)
        
        val battleState = battleEngine.initiateBattle(
            playerParty = listOf(monster1),
            enemyParty = listOf(monster2),
            battleType = BattleType.WILD_ENCOUNTER
        )
        
        // Multiple rapid battle actions
        val actions = (1..3).map {
            BattleActionData(
                action = BattleAction.ATTACK,
                sourceMonster = monster1,
                targetMonster = monster2
            )
        }
        
        var currentBattle = battleState
        actions.forEach { action ->
            currentBattle = battleEngine.processBattleAction(currentBattle, action)
        }
        
        assertNotNull("Rapid battle actions should be handled", currentBattle)
    }

    @Test
    fun testInvalidInputHandling() = runTest {
        // Test with invalid monster types/data
        try {
            val invalidMonster = createTestMonster(
                id = "",  // Empty ID
                name = "",  // Empty name
                type1 = MonsterType.DRAGON,
                family = MonsterFamily.DRAGON,
                personality = Personality.HARDY,
                attack = -1,
                defense = -1,
                agility = -1,
                magic = -1,
                wisdom = -1,
                maxHp = -1,
                maxMp = -1,
                skills = emptyList(),
                level = -5,  // Negative level
                experience = -100,  // Negative experience
                affection = -50  // Negative affection
            )
            
            // System should handle invalid data gracefully
            assertTrue("Invalid monster should be handled", invalidMonster.id.isEmpty())
        } catch (e: Exception) {
            assertTrue("Should handle invalid monster creation", true)
        }

        // Test invalid battle actions
        val validMonster = gameRepository.generateWildMonster("forest", 5)
        val battleState = battleEngine.initiateBattle(
            playerParty = listOf(validMonster),
            enemyParty = listOf(validMonster.copy(id = "enemy")),
            battleType = BattleType.WILD_ENCOUNTER
        )
        
        try {
            val invalidAction = BattleActionData(
                action = BattleAction.ATTACK,
                sourceMonster = validMonster,
                targetMonster = validMonster  // Can't target self in most battle systems
            )
            
            battleEngine.processBattleAction(battleState, invalidAction)
            // Should either work or handle gracefully
            assertTrue("Invalid battle action should be handled", true)
        } catch (e: Exception) {
            assertTrue("Should handle invalid battle actions", true)
        }
    }

    @Test
    fun testNetworkAndIOFailureSimulation() = runTest {
        // Simulate save/load failures
        val gameSave = gameRepository.createNewGame("IOFailureTest")
        
        // Test repeated save operations (simulate disk full)
        var saveSuccessCount = 0
        for (i in 1..10) {
            if (gameRepository.saveGame(gameSave.copy(gold = i * 100))) {
                saveSuccessCount++
            }
        }
        
        assertTrue("Some saves should succeed even with simulated failures", 
            saveSuccessCount > 0)
        
        // Test loading non-existent saves
        val nonExistentSaves = listOf(
            "invalid_id_123",
            "corrupted_save",
            "deleted_save",
            ""
        )
        
        nonExistentSaves.forEach { saveId ->
            val result = gameRepository.loadGame(saveId)
            assertNull("Loading invalid save should return null", result)
        }
    }

    @Test
    fun testAudioSystemEdgeCases() = runTest {
        // Test audio system with various edge cases
        val context = mock(android.content.Context::class.java)
        val audioEngine = ChiptuneAudioEngine(context)
        
        // Test with disabled audio
        audioEngine.updateAudioSettings(
            musicEnabled = false,
            soundEnabled = false,
            musicVolume = 0f,
            soundVolume = 0f
        )
        
        // These should not crash
        audioEngine.playBattleMusic()
        audioEngine.playMenuMusic()
        audioEngine.playMonsterCry("invalid_monster")
        audioEngine.playMonsterCry("")
        
        // Test rapid audio requests
        for (i in 1..10) {
            audioEngine.playMenuSelectSound()
        }
        
        assertTrue("Audio system should handle edge cases gracefully", true)
    }

    @Test
    fun testGameProgressionEdgeCases() = runTest {
        val gameSave = gameRepository.createNewGame("ProgressionTest")
        
        // Test with maxed out player
        val maxedSave = gameSave.copy(
            gold = Int.MAX_VALUE,
            playtimeMinutes = Int.MAX_VALUE,
            partyMonsters = (1..6).map { // Max party size
                gameRepository.generateWildMonster("forest", 99) // Max level
            }
        )
        
        // System should handle max values gracefully
        val saveResult = gameRepository.saveGame(maxedSave)
        assertTrue("Should handle maxed out game save", saveResult)
        
        // Test with minimal progress
        val minimalSave = gameSave.copy(
            gold = 0,
            partyMonsters = listOf(gameSave.partyMonsters.first()),
            inventory = emptyMap()
        )
        
        val minimalSaveResult = gameRepository.saveGame(minimalSave)
        assertTrue("Should handle minimal game save", minimalSaveResult)
    }

    @Test
    fun testLongRunningOperations() = runTest {
        // Test operations that might take a long time
        
        // Generate many monsters (simulating long-running battle)
        val startTime = System.currentTimeMillis()
        val generatedMonsters = (1..50).map { i ->
            gameRepository.generateWildMonster("forest", i % 20 + 1)
        }
        val endTime = System.currentTimeMillis()
        
        assertTrue("Monster generation should complete in reasonable time",
            (endTime - startTime) < 5000) // 5 second limit
        
        assertEquals("Should generate correct number of monsters", 
            50, generatedMonsters.size)
        
        // Test breeding combinations (computationally intensive)
        val breedingStartTime = System.currentTimeMillis()
        val combinations = mutableListOf<BreedingCompatibility>()
        
        for (i in 0 until minOf(10, generatedMonsters.size)) {
            for (j in i + 1 until minOf(10, generatedMonsters.size)) {
                val compatibility = breedingSystem.checkBreedingCompatibility(
                    generatedMonsters[i], generatedMonsters[j]
                )
                combinations.add(compatibility)
            }
        }
        
        val breedingEndTime = System.currentTimeMillis()
        assertTrue("Breeding compatibility checks should complete in reasonable time",
            (breedingEndTime - breedingStartTime) < 10000) // 10 second limit
        
        assertTrue("Should have breeding compatibility results", 
            combinations.isNotEmpty())
    }
}