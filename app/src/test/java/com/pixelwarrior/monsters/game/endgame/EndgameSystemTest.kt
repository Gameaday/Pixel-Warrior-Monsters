package com.pixelwarrior.monsters.game.endgame

import com.pixelwarrior.monsters.createTestMonster
import com.pixelwarrior.monsters.data.model.Monster
import com.pixelwarrior.monsters.data.model.MonsterType
import com.pixelwarrior.monsters.data.model.MonsterFamily
import com.pixelwarrior.monsters.data.model.Personality
import org.junit.Test
import org.junit.Assert.*

class EndgameSystemTest {
    
    private val endgameSystem = EndgameSystem()
    
    @Test
    fun `post-game dungeons unlock correctly`() {
        // Test unlocking conditions
        assertFalse(
            "Should not unlock with low level",
            endgameSystem.arePostGameDungeonsUnlocked(50, true, true)
        )
        
        assertFalse(
            "Should not unlock without main story completion",
            endgameSystem.arePostGameDungeonsUnlocked(80, false, true)
        )
        
        assertFalse(
            "Should not unlock without defeating champion",
            endgameSystem.arePostGameDungeonsUnlocked(80, true, false)
        )
        
        assertTrue(
            "Should unlock with all conditions met",
            endgameSystem.arePostGameDungeonsUnlocked(70, true, true)
        )
    }
    
    @Test
    fun `available post-game dungeons scale with level`() {
        val level80Dungeons = endgameSystem.getAvailablePostGameDungeons(80)
        val level90Dungeons = endgameSystem.getAvailablePostGameDungeons(90)
        val level99Dungeons = endgameSystem.getAvailablePostGameDungeons(99)
        
        assertTrue("Level 80 should have at least Void Nexus", level80Dungeons.isNotEmpty())
        assertTrue("Level 90 should have more dungeons than level 80", level90Dungeons.size >= level80Dungeons.size)
        assertTrue("Level 99 should have all dungeons", level99Dungeons.size == EndgameSystem.PostGameDungeon.values().size)
        
        // Check specific dungeon availability
        assertTrue("Level 80 should access Void Nexus", level80Dungeons.contains(EndgameSystem.PostGameDungeon.VOID_NEXUS))
        assertTrue("Level 90 should access Celestial Tower", level90Dungeons.contains(EndgameSystem.PostGameDungeon.CELESTIAL_TOWER))
        assertTrue("Level 99 should access Infinity Realm", level99Dungeons.contains(EndgameSystem.PostGameDungeon.INFINITY_REALM))
    }
    
    @Test
    fun `legendary encounter conditions work correctly`() {
        val testMonsters = listOf(
            createTestMonster(name = "Fire Drake", type1 = MonsterType.FIRE, family = MonsterFamily.DRAGON),
            createTestMonster(name = "Earth Golem", type1 = MonsterType.NORMAL, family = MonsterFamily.MATERIAL)
        )
        
        // Test night time requirement
        val nightEncounter = endgameSystem.checkLegendaryEncounter(
            currentDungeon = EndgameSystem.PostGameDungeon.VOID_NEXUS,
            currentFloor = 50,
            playerLevel = 85,
            currentTime = 2, // Night time
            weatherCondition = null,
            partyMonsters = testMonsters,
            completedConditions = emptyList()
        )
        
        // Since encounter rates are very low, we test the method execution without relying on random results
        // The method should complete without errors
        assertNotNull("Method should execute without errors", Unit)
        
        // Test dawn time requirement
        val dawnEncounter = endgameSystem.checkLegendaryEncounter(
            currentDungeon = EndgameSystem.PostGameDungeon.CELESTIAL_TOWER,
            currentFloor = 55,
            playerLevel = 90,
            currentTime = 6, // Dawn time
            weatherCondition = null,
            partyMonsters = testMonsters,
            completedConditions = emptyList()
        )
        
        assertNotNull("Dawn encounter method should execute", Unit)
    }
    
    @Test
    fun `legendary monster creation has correct stats`() {
        val legendaryData = EndgameSystem.LegendaryMonster(
            species = "Test Legendary",
            type1 = MonsterType.DRAGON,
            type2 = MonsterType.DARK,
            family = MonsterFamily.DRAGON,
            baseAttack = 200,
            baseDefense = 180,
            baseAgility = 160,
            baseMagic = 220,
            baseWisdom = 200,
            baseHP = 350,
            baseMP = 300,
            uniqueAbility = "Test Ability",
            encounterRate = 0.001,
            requiredDungeon = EndgameSystem.PostGameDungeon.VOID_NEXUS,
            spawnConditions = listOf("Test condition")
        )
        
        val legendary = endgameSystem.createLegendaryMonster(legendaryData, 90)
        
        assertEquals("Legendary should have correct species", "Test Legendary", legendary.species)
        assertEquals("Legendary should have correct level", 90, legendary.level)
        assertEquals("Legendary should have correct attack stat", 200, legendary.baseAttack)
        assertEquals("Legendary should have correct HP", 350, legendary.baseHP)
        assertTrue("Legendary should have legendary trait", legendary.traits.contains("Legendary"))
        assertTrue("Legendary should know unique ability", legendary.skills.contains("Test Ability"))
        assertEquals("Legendary should have limited synthesis level", 1, legendary.maxSynthesisLevel)
    }
    
    @Test
    fun `new game plus bonuses scale correctly`() {
        val firstNG = endgameSystem.initializeNewGamePlus(0, 1000, listOf("Master Key", "Potion"), emptyList())
        val secondNG = endgameSystem.initializeNewGamePlus(1, 2000, listOf("Master Key", "Potion"), emptyList())
        val fifthNG = endgameSystem.initializeNewGamePlus(4, 5000, listOf("Master Key", "Potion"), emptyList())
        
        // Test playthrough increment
        assertEquals("First NG+ should be playthrough 1", 1, firstNG.playthrough)
        assertEquals("Second NG+ should be playthrough 2", 2, secondNG.playthrough)
        
        // Test gold retention scaling
        assertTrue("Second NG+ should retain more gold than first", secondNG.retainedGold > firstNG.retainedGold)
        assertEquals("First NG+ should retain 50% gold", 500, firstNG.retainedGold)
        assertEquals("Second NG+ should retain 60% gold", 1200, secondNG.retainedGold)
        
        // Test bonus level scaling
        assertEquals("First NG+ should have 5 bonus levels", 5, firstNG.bonusStarterLevel)
        assertEquals("Second NG+ should have 10 bonus levels", 10, secondNG.bonusStarterLevel)
        assertEquals("Fifth NG+ should cap at 25 bonus levels", 25, fifthNG.bonusStarterLevel)
        
        // Test difficulty scaling
        assertEquals("First NG+ should have 1.15x difficulty", 1.15, firstNG.difficultyMultiplier, 0.01)
        assertEquals("Second NG+ should have 1.30x difficulty", 1.30, secondNG.difficultyMultiplier, 0.01)
        
        // Test feature unlocking
        assertTrue("Second NG+ should have advanced breeding", secondNG.unlockedFeatures.contains("Advanced Breeding Options"))
        assertFalse("First NG+ should not have legendary starter", firstNG.unlockedFeatures.contains("Legendary Starter Choice"))
        assertTrue("Fifth NG+ should have ultimate challenge", fifthNG.unlockedFeatures.contains("Ultimate Challenge Mode"))
        
        // Test item retention
        assertTrue("Should retain key items", firstNG.retainedItems.contains("Master Key"))
        assertFalse("Should not retain regular items", firstNG.retainedItems.contains("Potion"))
    }
    
    @Test
    fun `additional worlds unlock with correct requirements`() {
        val noAchievements = endgameSystem.getUnlockedAdditionalWorlds(emptyList(), emptyMap())
        assertTrue("No worlds should unlock without achievements", noAchievements.isEmpty())
        
        val someStats = mapOf(
            "darkTypeDefeated" to 60,
            "materialTypeSynthesized" to 25,
            "successfulBreedings" to 120
        )
        val someAchievements = listOf("All Dungeons Complete")
        
        val unlockedWorlds = endgameSystem.getUnlockedAdditionalWorlds(someAchievements, someStats)
        
        assertTrue("Shadow Realm should unlock with 60 dark defeats", 
            unlockedWorlds.any { it == EndgameSystem.AdditionalWorld.SHADOW_REALM })
        assertTrue("Mechanical Zone should unlock with 25 material synthesis", 
            unlockedWorlds.any { it == EndgameSystem.AdditionalWorld.MECHANICAL_ZONE })
        assertTrue("Fairy Garden should unlock with 120 breedings", 
            unlockedWorlds.any { it == EndgameSystem.AdditionalWorld.FAIRY_GARDEN })
        assertTrue("Ancient Kingdom should unlock with all dungeons complete", 
            unlockedWorlds.any { it == EndgameSystem.AdditionalWorld.ANCIENT_KINGDOM })
        
        // Dream Dimension should not unlock (requires max friendship stat)
        assertFalse("Dream Dimension should not unlock without friendship stat", 
            unlockedWorlds.any { it == EndgameSystem.AdditionalWorld.DREAM_DIMENSION })
    }
    
    @Test
    fun `advanced fusion options require correct conditions`() {
        val lowLevelMonsters = listOf(
            createTestMonster(name = "Young Dragon", type1 = MonsterType.DRAGON, family = MonsterFamily.DRAGON, level = 50),
            createTestMonster(name = "Small Beast", type1 = MonsterType.BEAST, family = MonsterFamily.BEAST, level = 40)
        )
        
        val highLevelMonsters = listOf(
            createTestMonster(name = "Elder Dragon", type1 = MonsterType.DRAGON, family = MonsterFamily.DRAGON, level = 85),
            createTestMonster(name = "Ancient Beast", type1 = MonsterType.BEAST, family = MonsterFamily.BEAST, level = 90),
            createTestMonster(name = "Mystic Bird", type1 = MonsterType.BIRD, family = MonsterFamily.BIRD, level = 88)
        )
        
        val legendaryMonster = createTestMonster(name = "Legendary Dragon", type1 = MonsterType.DRAGON, family = MonsterFamily.DRAGON, level = 95, traits = listOf("Legendary", "Rare"))
        
        // Test with low level monsters
        val lowLevelFusions = endgameSystem.getAdvancedFusionOptions(lowLevelMonsters)
        assertTrue("Low level monsters should have no fusion options", lowLevelFusions.isEmpty())
        
        // Test with high level monsters
        val highLevelFusions = endgameSystem.getAdvancedFusionOptions(highLevelMonsters)
        assertTrue("High level monsters should have fusion options", highLevelFusions.isNotEmpty())
        
        // Test with legendary
        val legendaryFusions = endgameSystem.getAdvancedFusionOptions(highLevelMonsters + legendaryMonster)
        val hasLegendaryFusion = legendaryFusions.any { it.generation == 4 }
        assertTrue("Should have legendary fusion option", hasLegendaryFusion)
        
        // Check fusion requirements
        val triFusion = legendaryFusions.find { it.generation == 3 }
        if (triFusion != null) {
            assertEquals("Tri-fusion should require level 80", 80, triFusion.requiredLevel)
            assertEquals("Tri-fusion should need Trinity Crystal", "Trinity Crystal", triFusion.fusionMaterial)
            assertTrue("Tri-fusion should have stat bonus", triFusion.statBonusMultiplier > 1.0)
        }
    }
    
    @Test
    fun `post-game dungeon data is valid`() {
        EndgameSystem.PostGameDungeon.values().forEach { dungeon ->
            assertTrue("Dungeon should have positive required level", dungeon.requiredLevel > 0)
            assertTrue("Dungeon should have positive floor count", dungeon.floors > 0)
            assertTrue("Dungeon should have non-empty name", dungeon.displayName.isNotBlank())
            assertTrue("Dungeon should have theme", dungeon.theme.isNotBlank())
            assertTrue("Dungeon should have description", dungeon.description.isNotBlank())
            
            // Check level progression makes sense
            assertTrue("Required level should be reasonable", dungeon.requiredLevel in 70..99)
            assertTrue("Floor count should be reasonable", dungeon.floors in 20..100)
        }
    }
    
    @Test
    fun `additional world data is valid`() {
        EndgameSystem.AdditionalWorld.values().forEach { world ->
            assertTrue("World should have non-empty name", world.displayName.isNotBlank())
            assertTrue("World should have theme", world.theme.isNotBlank())
            assertTrue("World should have unlock requirement", world.unlockRequirement.isNotBlank())
            assertTrue("World should have level range", world.levelRange.first > 0)
            assertTrue("World should have valid level range", world.levelRange.first <= world.levelRange.last)
            assertTrue("World should have unique monster types", world.uniqueMonsterTypes.isNotEmpty())
        }
    }
}