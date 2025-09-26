package com.pixelwarrior.monsters.game.world

import com.pixelwarrior.monsters.data.model.*
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for the Hub World System
 */
class HubWorldSystemTest {
    
    private val hubWorldSystem = HubWorldSystem()
    
    @Test
    fun testMainHallAlwaysUnlocked() {
        val emptyGameSave = createTestGameSave()
        
        assertTrue("Main Hall should always be unlocked", 
            hubWorldSystem.isAreaUnlocked(HubWorldSystem.HubArea.MAIN_HALL, emptyGameSave))
    }
    
    @Test
    fun testStoryProgressUnlocks() {
        val gameSave = createTestGameSave()
        
        // Monster Library should not be unlocked initially
        assertFalse("Monster Library should be locked initially",
            hubWorldSystem.isAreaUnlocked(HubWorldSystem.HubArea.MONSTER_LIBRARY, gameSave))
        
        // Add first capture story progress
        val updatedSave = gameSave.copy(
            storyProgress = gameSave.storyProgress + ("first_capture" to true)
        )
        
        // Monster Library should now be unlocked
        assertTrue("Monster Library should be unlocked after first capture",
            hubWorldSystem.isAreaUnlocked(HubWorldSystem.HubArea.MONSTER_LIBRARY, updatedSave))
    }
    
    @Test
    fun testKeyItemUnlocks() {
        val gameSave = createTestGameSave()
        
        // Breeding Lab should not be unlocked initially
        assertFalse("Breeding Lab should be locked initially",
            hubWorldSystem.isAreaUnlocked(HubWorldSystem.HubArea.BREEDING_LAB, gameSave))
        
        // Add breeder license key item
        val updatedSave = gameSave.copy(
            inventory = gameSave.inventory + ("breeder_license" to 1)
        )
        
        // Breeding Lab should now be unlocked
        assertTrue("Breeding Lab should be unlocked with breeder license",
            hubWorldSystem.isAreaUnlocked(HubWorldSystem.HubArea.BREEDING_LAB, updatedSave))
    }
    
    @Test
    fun testGetUnlockedAreas() {
        val gameSave = createTestGameSave().copy(
            storyProgress = mapOf(
                "first_capture" to true,
                "earned_gold" to true
            ),
            inventory = mapOf(
                "breeder_license" to 1
            )
        )
        
        val unlockedAreas = hubWorldSystem.getUnlockedAreas(gameSave)
        
        assertTrue("Main Hall should be unlocked", 
            unlockedAreas.contains(HubWorldSystem.HubArea.MAIN_HALL))
        assertTrue("Monster Library should be unlocked", 
            unlockedAreas.contains(HubWorldSystem.HubArea.MONSTER_LIBRARY))
        assertTrue("Breeding Lab should be unlocked", 
            unlockedAreas.contains(HubWorldSystem.HubArea.BREEDING_LAB))
        assertTrue("Item Shop should be unlocked", 
            unlockedAreas.contains(HubWorldSystem.HubArea.ITEM_SHOP))
        
        assertFalse("Battle Arena should still be locked", 
            unlockedAreas.contains(HubWorldSystem.HubArea.BATTLE_ARENA))
    }
    
    @Test
    fun testGetAvailableNPCs() {
        val gameSave = createTestGameSave().copy(
            storyProgress = mapOf("first_capture" to true),
            inventory = mapOf("breeder_license" to 1)
        )
        
        val availableNPCs = hubWorldSystem.getAvailableNPCs(gameSave)
        
        // Should include NPCs from unlocked areas
        assertTrue("Master should be available", 
            availableNPCs.any { it.id == "master" })
        assertTrue("Librarian should be available", 
            availableNPCs.any { it.id == "librarian" })
        assertTrue("Stable Keeper should be available", 
            availableNPCs.any { it.id == "keeper" })
        
        // Should not include NPCs from locked areas
        assertFalse("Arena Master should not be available", 
            availableNPCs.any { it.id == "arena_master" })
    }
    
    @Test
    fun testAwardKeyItem() {
        val gameSave = createTestGameSave()
        val keyItem = HubWorldSystem.KeyItem.NOVICE_BADGE
        
        val updatedSave = hubWorldSystem.awardKeyItem(keyItem, gameSave)
        
        assertTrue("Key item should be added to inventory",
            updatedSave.inventory.containsKey(keyItem.id))
        assertEquals("Key item count should be 1",
            1, updatedSave.inventory[keyItem.id])
    }
    
    @Test
    fun testCompleteStoryMilestone() {
        val gameSave = createTestGameSave()
        val milestone = HubWorldSystem.StoryMilestone.FIRST_CAPTURE
        
        val updatedSave = hubWorldSystem.completeStoryMilestone(milestone, gameSave)
        
        assertTrue("Milestone should be marked as completed",
            updatedSave.storyProgress.containsKey(milestone.id))
        assertEquals("Milestone should be true",
            true, updatedSave.storyProgress[milestone.id])
    }
    
    @Test
    fun testGetAvailableFacilities() {
        val gameSave = createTestGameSave().copy(
            storyProgress = mapOf("first_capture" to true),
            inventory = mapOf("breeder_license" to 1)
        )
        
        val facilities = hubWorldSystem.getAvailableFacilities(gameSave)
        
        assertTrue("Should have Main Hall facility",
            facilities.any { it.area == HubWorldSystem.HubArea.MAIN_HALL })
        assertTrue("Should have Monster Library facility",
            facilities.any { it.area == HubWorldSystem.HubArea.MONSTER_LIBRARY })
        assertTrue("Should have Breeding Lab facility",
            facilities.any { it.area == HubWorldSystem.HubArea.BREEDING_LAB })
        
        // Check facility functions
        val mainHallFacility = facilities.first { it.area == HubWorldSystem.HubArea.MAIN_HALL }
        assertTrue("Main Hall should have story dialogue function",
            mainHallFacility.functions.contains(HubWorldSystem.FacilityFunction.STORY_DIALOGUE))
        assertTrue("Main Hall should have monster healing function",
            mainHallFacility.functions.contains(HubWorldSystem.FacilityFunction.MONSTER_HEALING))
    }
    
    @Test
    fun testGetNextObjective() {
        // Test empty save
        val emptySave = createTestGameSave()
        val objective1 = hubWorldSystem.getNextObjective(emptySave)
        assertTrue("Should guide player to first capture",
            objective1.contains("first wild monster"))
        
        // Test with first capture completed
        val saveWithCapture = emptySave.copy(
            storyProgress = mapOf("first_capture" to true)
        )
        val objective2 = hubWorldSystem.getNextObjective(saveWithCapture)
        assertTrue("Should guide player to earn gold",
            objective2.contains("earn gold"))
        
        // Test with multiple milestones
        val advancedSave = emptySave.copy(
            storyProgress = mapOf(
                "first_capture" to true,
                "earned_gold" to true,
                "completed_first_dungeon" to true
            ),
            inventory = mapOf(
                "breeder_license" to 1,
                "novice_badge" to 1
            )
        )
        val objective3 = hubWorldSystem.getNextObjective(advancedSave)
        assertTrue("Should guide to next milestone",
            objective3.contains("Explorer's Compass") || objective3.contains("other worlds"))
    }
    
    @Test
    fun testKeyItemUnlocksList() {
        val breederLicense = HubWorldSystem.KeyItem.BREEDER_LICENSE
        assertTrue("Breeder license should unlock breeding lab",
            breederLicense.unlocks.contains(HubWorldSystem.HubArea.BREEDING_LAB))
        
        val noviceBadge = HubWorldSystem.KeyItem.NOVICE_BADGE
        assertTrue("Novice badge should unlock battle arena",
            noviceBadge.unlocks.contains(HubWorldSystem.HubArea.BATTLE_ARENA))
    }
    
    @Test
    fun testStoryMilestoneUnlocksList() {
        val firstCapture = HubWorldSystem.StoryMilestone.FIRST_CAPTURE
        assertTrue("First capture should unlock monster library",
            firstCapture.unlocks.contains(HubWorldSystem.HubArea.MONSTER_LIBRARY))
        
        val earnedGold = HubWorldSystem.StoryMilestone.EARNED_GOLD
        assertTrue("Earned gold should unlock item shop",
            earnedGold.unlocks.contains(HubWorldSystem.HubArea.ITEM_SHOP))
    }
    
    @Test
    fun testNPCLocations() {
        assertEquals("Master should be in Main Hall",
            HubWorldSystem.HubArea.MAIN_HALL, 
            HubWorldSystem.HubNPC.MASTER.location)
        
        assertEquals("Librarian should be in Monster Library",
            HubWorldSystem.HubArea.MONSTER_LIBRARY, 
            HubWorldSystem.HubNPC.LIBRARIAN.location)
        
        assertEquals("Arena Master should be in Battle Arena",
            HubWorldSystem.HubArea.BATTLE_ARENA, 
            HubWorldSystem.HubNPC.ARENA_MASTER.location)
    }
    
    @Test
    fun testNPCDialogue() {
        val master = HubWorldSystem.HubNPC.MASTER
        assertTrue("Master should have dialogue", master.dialogue.isNotEmpty())
        assertTrue("Master should have multiple dialogue lines", master.dialogue.size > 1)
        
        val librarian = HubWorldSystem.HubNPC.LIBRARIAN
        assertTrue("Librarian should have relevant dialogue",
            librarian.dialogue.any { it.contains("knowledge") || it.contains("monster") })
    }
    
    @Test
    fun testProgressiveUnlocking() {
        var gameSave = createTestGameSave()
        
        // Initially only Main Hall should be unlocked
        var unlockedAreas = hubWorldSystem.getUnlockedAreas(gameSave)
        assertEquals("Only Main Hall should be unlocked initially", 1, unlockedAreas.size)
        
        // Complete first capture
        gameSave = hubWorldSystem.completeStoryMilestone(
            HubWorldSystem.StoryMilestone.FIRST_CAPTURE, gameSave)
        unlockedAreas = hubWorldSystem.getUnlockedAreas(gameSave)
        assertTrue("Monster Library should now be unlocked",
            unlockedAreas.contains(HubWorldSystem.HubArea.MONSTER_LIBRARY))
        
        // Add key items
        gameSave = hubWorldSystem.awardKeyItem(HubWorldSystem.KeyItem.BREEDER_LICENSE, gameSave)
        gameSave = hubWorldSystem.awardKeyItem(HubWorldSystem.KeyItem.NOVICE_BADGE, gameSave)
        
        unlockedAreas = hubWorldSystem.getUnlockedAreas(gameSave)
        assertTrue("Breeding Lab should now be unlocked",
            unlockedAreas.contains(HubWorldSystem.HubArea.BREEDING_LAB))
        assertTrue("Battle Arena should now be unlocked",
            unlockedAreas.contains(HubWorldSystem.HubArea.BATTLE_ARENA))
    }
    
    private fun createTestGameSave(): GameSave {
        return GameSave(
            playerId = "test",
            playerName = "Test Player",
            currentLevel = "test_area",
            position = Position(0f, 0f),
            partyMonsters = emptyList(),
            farmMonsters = emptyList(),
            inventory = emptyMap(),
            gold = 0,
            playtimeMinutes = 0,
            storyProgress = emptyMap(),
            unlockedGates = emptyList()
        )
    }
}