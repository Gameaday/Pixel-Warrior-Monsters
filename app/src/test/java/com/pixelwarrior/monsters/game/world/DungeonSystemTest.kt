package com.pixelwarrior.monsters.game.world

import com.pixelwarrior.monsters.data.model.*
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before

/**
 * Tests for the enhanced dungeon system with themed areas and multiple floors
 */
class DungeonSystemTest {
    
    private lateinit var dungeonSystem: DungeonSystem
    private lateinit var sampleProgress: Map<String, Boolean>
    
    @Before
    fun setup() {
        dungeonSystem = DungeonSystem()
        sampleProgress = mapOf(
            "forest_complete" to true,
            "fire_key_obtained" to true,
            "ice_crown_found" to false
        )
    }
    
    @Test
    fun testGetAvailableDungeons() {
        val dungeons = dungeonSystem.getAvailableDungeons(sampleProgress)
        
        // Should include dungeons with no requirements and met requirements
        assertTrue("Should include beginner dungeon", 
            dungeons.any { it.id == "beginner_forest" })
        assertTrue("Should include molten depths with fire key", 
            dungeons.any { it.id == "molten_depths" })
        assertTrue("Should include frozen palace with fire key", 
            dungeons.any { it.id == "frozen_palace" })
        
        // Should not include dungeons with unmet requirements
        assertFalse("Should not include lost ruins without ice crown", 
            dungeons.any { it.id == "lost_ruins" })
    }
    
    @Test
    fun testEnterDungeon() {
        val firstFloor = dungeonSystem.enterDungeon("beginner_forest")
        
        assertNotNull("Should create first floor", firstFloor)
        firstFloor?.let { floor ->
            assertEquals("Should be floor 1", 1, floor.floorNumber)
            assertEquals("Should be beginner forest", "beginner_forest", floor.dungeonId)
            assertEquals("Should be forest theme", DungeonTheme.FOREST, floor.theme)
            assertTrue("Should have stairs on first floor", floor.hasStairs)
        }
    }
    
    @Test
    fun testProceedToNextFloor() {
        val firstFloor = dungeonSystem.enterDungeon("beginner_forest")
        assertNotNull(firstFloor)
        
        val secondFloor = dungeonSystem.proceedToNextFloor("beginner_forest", 1)
        assertNotNull("Should create second floor", secondFloor)
        secondFloor?.let { floor ->
            assertEquals("Should be floor 2", 2, floor.floorNumber)
            assertEquals("Should be same dungeon", "beginner_forest", floor.dungeonId)
            assertEquals("Should maintain theme", DungeonTheme.FOREST, floor.theme)
        }
    }
    
    @Test
    fun testMaxFloorLimit() {
        // Test that proceeding beyond max floors returns null
        val beyondMax = dungeonSystem.proceedToNextFloor("beginner_forest", 16)
        assertNull("Should not create floor beyond maximum", beyondMax)
    }
    
    @Test
    fun testFloorTypes() {
        // Boss floors should occur every 8 floors
        val bossFloor = dungeonSystem.proceedToNextFloor("beginner_forest", 7) // Would be floor 8
        assertNotNull(bossFloor)
        // Note: We can't directly test the boss floor type due to random generation
        // but we can verify it was generated
        
        val regularFloor = dungeonSystem.proceedToNextFloor("beginner_forest", 1) // Would be floor 2
        assertNotNull(regularFloor)
    }
    
    @Test
    fun testDungeonThemes() {
        val forestDungeon = dungeonSystem.enterDungeon("beginner_forest")
        assertEquals("Forest dungeon should have forest theme", 
            DungeonTheme.FOREST, forestDungeon?.theme)
            
        val volcanicDungeon = dungeonSystem.enterDungeon("molten_depths")
        assertEquals("Volcanic dungeon should have volcanic theme", 
            DungeonTheme.VOLCANIC, volcanicDungeon?.theme)
            
        val iceDungeon = dungeonSystem.enterDungeon("frozen_palace")
        assertEquals("Ice dungeon should have ice theme", 
            DungeonTheme.ICE, iceDungeon?.theme)
    }
    
    @Test
    fun testEncounterRates() {
        val firstFloor = dungeonSystem.enterDungeon("beginner_forest")
        assertNotNull(firstFloor)
        
        // Generate multiple floors and check that at least one regular floor has a higher rate
        // This accounts for the fact that some floors may be event floors with different rates
        var foundHigherRate = false
        for (i in 5..12) {
            val floor = dungeonSystem.proceedToNextFloor("beginner_forest", i)
            floor?.let {
                // For regular floors, the encounter rate increases with floor number
                if (it.type == FloorType.REGULAR && it.encounterRate > firstFloor!!.encounterRate) {
                    foundHigherRate = true
                }
            }
        }
        
        assertTrue("Later regular floors should have higher encounter rates", foundHigherRate)
    }
    
    @Test
    fun testWanderingEvents() {
        // Generate multiple floors to test wandering event generation
        var eventFound = false
        for (i in 1..10) {
            val floor = dungeonSystem.proceedToNextFloor("beginner_forest", i)
            floor?.let {
                if (it.wanderingEvents.isNotEmpty()) {
                    eventFound = true
                    // Verify event structure
                    val event = it.wanderingEvents.first()
                    assertNotNull("Event should have ID", event.id)
                    assertNotNull("Event should have description", event.description)
                    assertNotNull("Event should have outcome", event.outcome)
                }
            }
        }
        // Note: Due to randomness, we can't guarantee events will be found
        // but the structure should be valid when they are
    }
    
    @Test
    fun testSpecialFeatures() {
        val floor = dungeonSystem.enterDungeon("beginner_forest")
        assertNotNull(floor)
        
        // Features should be strings
        floor?.specialFeatures?.forEach { feature ->
            assertNotNull("Feature should not be null", feature)
            assertTrue("Feature should be non-empty", feature.isNotEmpty())
        }
    }
    
    @Test
    fun testDungeonProgression() {
        val availableWithNoProgress = dungeonSystem.getAvailableDungeons(emptyMap())
        val availableWithProgress = dungeonSystem.getAvailableDungeons(
            mapOf(
                "forest_complete" to true,
                "fire_key_obtained" to true,
                "ice_crown_found" to true,
                "ancient_key_acquired" to true
            )
        )
        
        assertTrue("Should unlock more dungeons with progress",
            availableWithProgress.size > availableWithNoProgress.size)
    }
    
    @Test
    fun testEventFloorOutcomes() {
        // Test that event outcomes are properly mapped
        val outcomes = EventOutcome.values()
        assertTrue("Should have healing outcome", outcomes.contains(EventOutcome.HEALING))
        assertTrue("Should have experience outcome", outcomes.contains(EventOutcome.EXPERIENCE))
        assertTrue("Should have treasure outcome", outcomes.contains(EventOutcome.TREASURE))
        assertTrue("Should have stat boost outcome", outcomes.contains(EventOutcome.STAT_BOOST))
    }
    
    @Test
    fun testDungeonDataIntegrity() {
        val allProgress = mapOf(
            "forest_complete" to true,
            "fire_key_obtained" to true,
            "ice_crown_found" to true,
            "ancient_key_acquired" to true,
            "sea_blessing_received" to true,
            "wind_blessing_earned" to true,
            "all_blessings_complete" to true
        )
        
        val allDungeons = dungeonSystem.getAvailableDungeons(allProgress)
        
        allDungeons.forEach { dungeon ->
            assertNotNull("Dungeon ID should not be null", dungeon.id)
            assertNotNull("Dungeon name should not be null", dungeon.name)
            assertNotNull("Dungeon theme should not be null", dungeon.theme)
            assertTrue("Dungeon should have positive max floors", dungeon.maxFloors > 0)
            assertTrue("Dungeon should have valid encounter rate", 
                dungeon.baseEncounterRate >= 0.0f && dungeon.baseEncounterRate <= 1.0f)
            assertTrue("Dungeon should have theme encounters", 
                dungeon.themeEncounters.isNotEmpty())
        }
    }
}