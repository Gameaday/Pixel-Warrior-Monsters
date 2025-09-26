package com.pixelwarrior.monsters.game.exploration

import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.Assert.*
import java.time.LocalTime

/**
 * Comprehensive unit tests for advanced exploration features
 */
class ExplorationSystemTest {
    
    @Test
    fun testGateKeyCollection() {
        val explorationSystem = ExplorationSystem()
        
        // Initially no keys collected
        assertEquals(0, explorationSystem.getCurrentState().gateKeysCollected.size)
        assertFalse(explorationSystem.hasGateKey(ExplorationSystem.GateKey.FOREST_KEY))
        
        // Collect a key
        assertTrue(explorationSystem.collectGateKey(ExplorationSystem.GateKey.FOREST_KEY))
        assertTrue(explorationSystem.hasGateKey(ExplorationSystem.GateKey.FOREST_KEY))
        
        // Cannot collect same key twice
        assertFalse(explorationSystem.collectGateKey(ExplorationSystem.GateKey.FOREST_KEY))
        
        // Collect more keys
        assertTrue(explorationSystem.collectGateKey(ExplorationSystem.GateKey.FLAME_KEY))
        assertEquals(2, explorationSystem.getCurrentState().gateKeysCollected.size)
    }
    
    @Test
    fun testGateKeyUnlocksNests() {
        val explorationSystem = ExplorationSystem()
        
        // Initially no active nests
        assertEquals(0, explorationSystem.getActiveNests().size)
        
        // Collecting forest key should unlock forest nest
        explorationSystem.collectGateKey(ExplorationSystem.GateKey.FOREST_KEY)
        val activeNests = explorationSystem.getActiveNests()
        assertTrue(activeNests.any { it.name == "Whispering Grove" })
        
        // Collect another key
        explorationSystem.collectGateKey(ExplorationSystem.GateKey.FLAME_KEY)
        val updatedNests = explorationSystem.getActiveNests()
        assertTrue(updatedNests.any { it.name == "Ember Sanctuary" })
        assertTrue(updatedNests.size >= 2)
    }
    
    @Test
    fun testWeatherSystem() = runTest {
        val explorationSystem = ExplorationSystem()
        
        // Initial weather
        val initialWeather = explorationSystem.getCurrentWeather()
        assertNotNull(initialWeather)
        
        // Weather affects encounter rates
        val baseRate = 1.0
        val modifiedRate = explorationSystem.calculateEncounterRate(baseRate)
        assertTrue(modifiedRate > 0)
        
        // Update weather
        explorationSystem.updateWeather()
        // Weather might be same or different after update, but should still be valid
        val currentWeather = explorationSystem.getCurrentWeather()
        assertNotNull(currentWeather)
    }
    
    @Test
    fun testDayNightCycle() {
        val explorationSystem = ExplorationSystem()
        
        // Test day/night detection (depends on current system time)
        val isNight = explorationSystem.isNightTime()
        val modifier = explorationSystem.getDayNightModifier()
        
        // Night should have higher encounter rates
        if (isNight) {
            assertEquals(1.3, modifier, 0.01)
        } else {
            assertEquals(1.0, modifier, 0.01)
        }
        
        // Time-based events
        val events = explorationSystem.checkTimeBasedEvents()
        assertTrue(events.size >= 0) // May be empty or have events
    }
    
    @Test
    fun testHiddenPassageDiscovery() {
        val explorationSystem = ExplorationSystem()
        
        // Initially no hidden passages
        assertEquals(0, explorationSystem.getHiddenPassages().size)
        assertFalse(explorationSystem.canAccessHiddenPassage("Secret Cave"))
        
        // Cannot discover without solving puzzle
        assertFalse(explorationSystem.discoverHiddenPassage("Secret Cave", false))
        
        // Discover with solved puzzle
        assertTrue(explorationSystem.discoverHiddenPassage("Secret Cave", true))
        assertTrue(explorationSystem.canAccessHiddenPassage("Secret Cave"))
        
        // Cannot discover same passage twice
        assertFalse(explorationSystem.discoverHiddenPassage("Secret Cave", true))
        
        // Discover more passages
        assertTrue(explorationSystem.discoverHiddenPassage("Ancient Tunnel", true))
        assertEquals(2, explorationSystem.getHiddenPassages().size)
    }
    
    @Test
    fun testMonsterNestBonuses() {
        val explorationSystem = ExplorationSystem()
        
        // Unlock a nest
        explorationSystem.collectGateKey(ExplorationSystem.GateKey.FOREST_KEY)
        
        // Check nest bonuses
        val breedingBonus = explorationSystem.getNestBreedingBonus("Whispering Grove")
        assertTrue(breedingBonus < 1.0) // Should reduce breeding time
        
        val rareChance = explorationSystem.getNestRareChance("Whispering Grove")
        assertTrue(rareChance > 0.0) // Should have some rare chance
        
        // Non-existent nest should return defaults
        assertEquals(1.0, explorationSystem.getNestBreedingBonus("Non-existent Nest"), 0.01)
        assertEquals(0.0, explorationSystem.getNestRareChance("Non-existent Nest"), 0.01)
    }
    
    @Test
    fun testSpecialAreaAccess() {
        val explorationSystem = ExplorationSystem()
        
        // Cannot access without key
        assertFalse(explorationSystem.canAccessSpecialArea(ExplorationSystem.GateKey.CRYSTAL_KEY))
        
        // Can access with key
        explorationSystem.collectGateKey(ExplorationSystem.GateKey.CRYSTAL_KEY)
        assertTrue(explorationSystem.canAccessSpecialArea(ExplorationSystem.GateKey.CRYSTAL_KEY))
    }
    
    @Test
    fun testEncounterRateCalculation() {
        val explorationSystem = ExplorationSystem()
        
        val baseRate = 0.5
        val calculatedRate = explorationSystem.calculateEncounterRate(baseRate)
        
        // Rate should be modified by weather and time
        assertTrue(calculatedRate > 0)
        
        // Should be affected by current conditions
        val currentWeather = explorationSystem.getCurrentWeather()
        val expectedMinRate = baseRate * currentWeather.encounterModifier * 1.0 // Day minimum
        val expectedMaxRate = baseRate * currentWeather.encounterModifier * 1.3 // Night maximum
        
        assertTrue(calculatedRate >= expectedMinRate - 0.01)
        assertTrue(calculatedRate <= expectedMaxRate + 0.01)
    }
    
    @Test
    fun testWeatherForecast() {
        val explorationSystem = ExplorationSystem()
        
        val forecast = explorationSystem.getWeatherForecast()
        
        // Should have 5 forecast entries
        assertEquals(5, forecast.size)
        
        // Each entry should have time and weather
        forecast.forEach { (time, weather) ->
            assertTrue(time.contains(":"))
            assertNotNull(weather)
        }
    }
    
    @Test
    fun testWeatherEffects() {
        // Test each weather type has different encounter modifiers
        val weatherTypes = ExplorationSystem.WeatherType.values()
        
        assertTrue(weatherTypes.isNotEmpty())
        
        weatherTypes.forEach { weather ->
            assertTrue(weather.encounterModifier > 0)
            assertTrue(weather.displayName.isNotEmpty())
            assertTrue(weather.battleEffect.isNotEmpty())
        }
        
        // Stormy should have highest encounter rate
        val stormy = ExplorationSystem.WeatherType.STORMY
        assertEquals(1.5, stormy.encounterModifier, 0.01)
        
        // Sunny should be neutral
        val sunny = ExplorationSystem.WeatherType.SUNNY
        assertEquals(1.0, sunny.encounterModifier, 0.01)
    }
    
    @Test
    fun testAllGateKeysHaveValidData() {
        val allKeys = ExplorationSystem.GateKey.values()
        
        assertEquals(8, allKeys.size)
        
        allKeys.forEach { key ->
            assertTrue(key.displayName.isNotEmpty())
            assertTrue(key.description.isNotEmpty())
            assertTrue(key.displayName.contains("Key"))
        }
    }
    
    @Test
    fun testExplorationStateIntegrity() {
        val explorationSystem = ExplorationSystem()
        val initialState = explorationSystem.getCurrentState()
        
        // State should be properly initialized
        assertNotNull(initialState.currentWeather)
        assertNotNull(initialState.timeOfDay)
        assertNotNull(initialState.gateKeysCollected)
        assertNotNull(initialState.activeNests)
        assertNotNull(initialState.hiddenPassagesFound)
        assertNotNull(initialState.weatherChangeTime)
        
        // Make changes
        explorationSystem.collectGateKey(ExplorationSystem.GateKey.FOREST_KEY)
        explorationSystem.discoverHiddenPassage("Test Passage", true)
        
        val updatedState = explorationSystem.getCurrentState()
        
        // State should reflect changes
        assertTrue(updatedState.gateKeysCollected.contains(ExplorationSystem.GateKey.FOREST_KEY))
        assertTrue(updatedState.hiddenPassagesFound.contains("Test Passage"))
        assertTrue(updatedState.activeNests.isNotEmpty())
    }
}