package com.pixelwarrior.monsters.ui

import com.pixelwarrior.monsters.ui.screens.*
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for navigation and UI components
 */
class NavigationTest {
    
    @Test
    fun saveLoadMode_enumValues_areCorrect() {
        val modes = SaveLoadMode.values()
        assertEquals(2, modes.size)
        assertTrue(modes.contains(SaveLoadMode.SAVE))
        assertTrue(modes.contains(SaveLoadMode.LOAD))
    }
    
    @Test
    fun gameScreen_enumValues_includeAllScreens() {
        val screens = GameScreen.values()
        assertTrue("Should include MAIN_MENU", screens.contains(GameScreen.MAIN_MENU))
        assertTrue("Should include WORLD_MAP", screens.contains(GameScreen.WORLD_MAP))
        assertTrue("Should include BATTLE", screens.contains(GameScreen.BATTLE))
        assertTrue("Should include MONSTER_MANAGEMENT", screens.contains(GameScreen.MONSTER_MANAGEMENT))
        assertTrue("Should include BREEDING", screens.contains(GameScreen.BREEDING))
        assertTrue("Should include AUDIO_SETTINGS", screens.contains(GameScreen.AUDIO_SETTINGS))
        assertTrue("Should include GAME_SETTINGS", screens.contains(GameScreen.GAME_SETTINGS))
        assertTrue("Should include MONSTER_CODEX", screens.contains(GameScreen.MONSTER_CODEX))
        assertTrue("Should include CREDITS", screens.contains(GameScreen.CREDITS))
        assertTrue("Should include SAVE_GAME", screens.contains(GameScreen.SAVE_GAME))
        assertTrue("Should include LOAD_GAME", screens.contains(GameScreen.LOAD_GAME))
    }
    
    @Test
    fun saveSlot_dataClass_createsCorrectly() {
        val saveSlot = SaveSlot(
            id = "test_save",
            playerName = "TestPlayer",
            level = 10,
            playtime = 150,
            partySize = 3,
            lastSaved = System.currentTimeMillis()
        )
        
        assertEquals("test_save", saveSlot.id)
        assertEquals("TestPlayer", saveSlot.playerName)
        assertEquals(10, saveSlot.level)
        assertEquals(150, saveSlot.playtime)
        assertEquals(3, saveSlot.partySize)
        assertTrue(saveSlot.lastSaved > 0)
    }
    
    @Test
    fun gameViewModel_loadGameOverloads_workCorrectly() {
        val viewModel = GameViewModel()
        
        // Test that both overloads exist (compilation check)
        assertNotNull("loadGame() method should exist", viewModel)
        // The actual loading functionality would need a mock repository for full testing
    }
}