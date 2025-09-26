package com.pixelwarrior.monsters.game.ui

import com.pixelwarrior.monsters.game.ui.*
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * Test suite for UI Polish System
 */
class UIPolishSystemTest {
    
    private lateinit var uiPolishSystem: UIPolishSystem
    
    @Before
    fun setup() {
        uiPolishSystem = UIPolishSystem()
    }
    
    @Test
    fun `monster animation system works correctly`() {
        // Test playing animation
        val animation = uiPolishSystem.playMonsterAnimation("test_monster", AnimationType.ATTACK)
        
        assertNotNull(animation)
        assertEquals("test_monster", animation.monsterId)
        assertEquals(AnimationType.ATTACK, animation.type)
        assertEquals(800, animation.duration)
        assertFalse(animation.isLooping)
        
        // Test idle animation is looping
        val idleAnimation = uiPolishSystem.playMonsterAnimation("test_monster", AnimationType.IDLE)
        assertTrue(idleAnimation.isLooping)
        assertEquals(2000, idleAnimation.duration)
        
        // Test stopping animation
        uiPolishSystem.stopMonsterAnimation("test_monster")
        val currentAnimations = uiPolishSystem.currentAnimations.value
        assertFalse(currentAnimations.containsKey("test_monster"))
    }
    
    @Test
    fun `fast travel system works correctly`() = runTest {
        // Mock game save data
        val mockGameSave = createMockGameSave()
        
        // Initialize fast travel
        uiPolishSystem.initializeFastTravel(mockGameSave)
        
        val destinations = uiPolishSystem.fastTravelDestinations.value
        assertTrue(destinations.isNotEmpty())
        
        // Test unlocked destination
        val mainHall = destinations.find { it.area == HubArea.MAIN_HALL }
        assertNotNull(mainHall)
        assertTrue(mainHall!!.isUnlocked)
        assertEquals(0, mainHall.travelTime)
        
        // Test fast travel to unlocked destination
        val result = uiPolishSystem.fastTravelTo(HubArea.MAIN_HALL)
        assertTrue(result is FastTravelResult.SUCCESS)
        assertEquals("Main Hall", (result as FastTravelResult.SUCCESS).destinationName)
    }
    
    @Test
    fun `auto-save system works correctly`() = runTest {
        // Configure auto-save settings
        val settings = AutoSaveSettings(
            enabled = true,
            intervalMinutes = 5,
            saveOnBattleComplete = true,
            saveOnSynthesis = false
        )
        
        uiPolishSystem.configureAutoSave(settings)
        
        // Test should auto-save logic
        assertTrue(uiPolishSystem.shouldAutoSave(GameEvent.BATTLE_COMPLETED, 0))
        assertFalse(uiPolishSystem.shouldAutoSave(GameEvent.MONSTER_SYNTHESIZED, 0))
        
        // Test periodic auto-save
        val fiveMinutesInMs = 5 * 60 * 1000L
        assertTrue(uiPolishSystem.shouldAutoSave(GameEvent.PERIODIC, fiveMinutesInMs + 1000))
        assertFalse(uiPolishSystem.shouldAutoSave(GameEvent.PERIODIC, fiveMinutesInMs - 1000))
        
        // Test performing auto-save
        val result = uiPolishSystem.performAutoSave()
        assertTrue(result is AutoSaveResult.SUCCESS)
    }
    
    @Test
    fun `notification system works correctly`() {
        // Test showing notification
        val notification = UINotification.SUCCESS("Test message")
        uiPolishSystem.showNotification(notification)
        
        val currentNotification = uiPolishSystem.currentNotification.value
        assertNotNull(currentNotification)
        assertTrue(currentNotification is UINotification.SUCCESS)
        assertEquals("Test message", (currentNotification as UINotification.SUCCESS).message)
        
        // Test dismissing notification
        uiPolishSystem.dismissNotification()
        assertEquals(null, uiPolishSystem.currentNotification.value)
    }
    
    @Test
    fun `user preferences system works correctly`() {
        // Test updating preferences
        val preferences = UserPreferences(
            animationQuality = AnimationQuality.LOW,
            reduceAnimations = true,
            autoSaveEnabled = false,
            uiTheme = UITheme.DARK
        )
        
        uiPolishSystem.updateUserPreferences(preferences)
        
        assertEquals(AnimationQuality.LOW, uiPolishSystem.getAnimationQuality())
        assertTrue(uiPolishSystem.isReducedAnimationsEnabled())
        
        val storedPreferences = uiPolishSystem.userPreferences.value
        assertEquals(AnimationQuality.LOW, storedPreferences.animationQuality)
        assertTrue(storedPreferences.reduceAnimations)
        assertFalse(storedPreferences.autoSaveEnabled)
        assertEquals(UITheme.DARK, storedPreferences.uiTheme)
    }
    
    @Test
    fun `loading state management works correctly`() = runTest {
        assertFalse(uiPolishSystem.isLoading.value)
        
        uiPolishSystem.showLoadingState {
            // Simulate some operation
            assertTrue(uiPolishSystem.isLoading.value)
        }
        
        // Should be false after operation completes
        assertFalse(uiPolishSystem.isLoading.value)
    }
    
    private fun createMockGameSave(): com.pixelwarrior.monsters.data.model.GameSave {
        return com.pixelwarrior.monsters.data.model.GameSave(
            id = "test_save",
            playerName = "Test Player",
            currentLevel = 10,
            currentArea = "main_hall",
            monsters = emptyList(),
            inventory = mapOf("breeder_license" to 1),
            storyProgress = mapOf("first_capture" to true),
            lastSaved = System.currentTimeMillis()
        )
    }
}