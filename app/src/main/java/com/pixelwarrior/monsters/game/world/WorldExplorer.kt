package com.pixelwarrior.monsters.game.world

import com.pixelwarrior.monsters.data.model.*
import kotlin.random.Random

/**
 * World exploration system that handles random encounters, movement, and area management
 */
class WorldExplorer {
    
    private val areas = generateGameAreas()
    
    /**
     * Get available areas for exploration
     */
    fun getAvailableAreas(unlockedGates: List<String>): List<GameLevel> {
        return areas.filter { area ->
            area.requiredItem == null || unlockedGates.contains(area.requiredItem)
        }
    }
    
    /**
     * Attempt a random encounter in the specified area
     */
    fun attemptRandomEncounter(areaId: String, playerLevel: Int): Monster? {
        val area = areas.find { it.id == areaId } ?: return null
        
        // Check if encounter should happen
        if (Random.nextFloat() > area.encounterRate) return null
        
        // Select random monster from area's encounter list
        val encounterOptions = area.possibleEncounters
        if (encounterOptions.isEmpty()) return null
        
        val selectedSpeciesId = encounterOptions.random()
        return generateWildMonster(selectedSpeciesId, playerLevel)
    }
    
    /**
     * Generate a wild monster with level scaling
     */
    private fun generateWildMonster(speciesId: String, playerLevel: Int): Monster {
        // Level should be close to player level with some variation
        val wildLevel = (playerLevel + Random.nextInt(-2, 3)).coerceAtLeast(1)
        
        // Get base stats (would normally come from species database)
        val baseStats = getBaseStatsForSpecies(speciesId)
        
        // Scale stats based on level
        val scaledStats = scaleStatsForLevel(baseStats, wildLevel)
        
        return Monster(
            id = java.util.UUID.randomUUID().toString(),
            speciesId = speciesId,
            name = getSpeciesName(speciesId),
            type1 = getSpeciesType1(speciesId),
            type2 = getSpeciesType2(speciesId),
            family = getSpeciesFamily(speciesId),
            level = wildLevel,
            currentHp = scaledStats.maxHp,
            currentMp = scaledStats.maxMp,
            experience = 0,
            experienceToNext = calculateExpToNextLevel(wildLevel),
            baseStats = baseStats,
            currentStats = scaledStats,
            skills = getSkillsForLevel(speciesId, wildLevel),
            traits = generateRandomTraits(),
            isWild = true,
            captureRate = getBaseCaptureRate(speciesId),
            growthRate = getGrowthRate(speciesId)
        )
    }
    
    /**
     * Calculate movement through an area and return any events
     */
    fun exploreArea(areaId: String, currentPosition: Position, direction: Direction): ExplorationResult {
        val newPosition = moveInDirection(currentPosition, direction)
        
        // Check for encounters
        val encounter = attemptRandomEncounter(areaId, 10) // TODO: Use actual player level
        
        // Check for items or special locations
        val itemFound = if (Random.nextFloat() < 0.05f) generateRandomItem() else null
        
        return ExplorationResult(
            newPosition = newPosition,
            encounter = encounter,
            itemFound = itemFound,
            specialEvent = null
        )
    }
    
    /**
     * Move position in specified direction
     */
    private fun moveInDirection(position: Position, direction: Direction): Position {
        val moveDistance = 1.0f
        return when (direction) {
            Direction.UP -> position.copy(y = position.y - moveDistance, facing = direction)
            Direction.DOWN -> position.copy(y = position.y + moveDistance, facing = direction)
            Direction.LEFT -> position.copy(x = position.x - moveDistance, facing = direction)
            Direction.RIGHT -> position.copy(x = position.x + moveDistance, facing = direction)
        }
    }
    
    /**
     * Generate random item for exploration
     */
    private fun generateRandomItem(): Item? {
        val items = listOf(
            Item("healing_herb", "Healing Herb", "Restores 50 HP", ItemType.HEALING, 10, usableInField = true),
            Item("magic_water", "Magic Water", "Restores 30 MP", ItemType.HEALING, 15, usableInField = true),
            Item("monster_food", "Monster Food", "Increases friendship", ItemType.FOOD, 5),
            
            // Cooking ingredients  
            Item("berries", "Fresh Berries", "Sweet berries for cooking treats", ItemType.COOKING_INGREDIENT, 2),
            Item("grain", "Wild Grain", "Nutritious grain for monster food", ItemType.COOKING_INGREDIENT, 1),
            Item("herbs", "Aromatic Herbs", "Fragrant herbs that monsters love", ItemType.COOKING_INGREDIENT, 3),
            Item("honey", "Pure Honey", "Sweet honey for quality treats", ItemType.COOKING_INGREDIENT, 8),
            Item("spicy_peppers", "Spicy Peppers", "Hot peppers for fire treats", ItemType.COOKING_INGREDIENT, 5),
            Item("fresh_leaves", "Fresh Leaves", "Tender leaves for grass treats", ItemType.COOKING_INGREDIENT, 2),
            
            // Pre-made treats
            Item("basic_treat", "Basic Monster Treat", "A simple treat that monsters enjoy", ItemType.BASIC_TREAT, 15, usableInBattle = true),
            Item("quality_treat", "Quality Monster Treat", "A delicious treat that increases affection significantly", ItemType.QUALITY_TREAT, 35, usableInBattle = true)
        )
        return items.randomOrNull()
    }
    
    /**
     * Generate default game areas
     */
    private fun generateGameAreas(): List<GameLevel> {
        return listOf(
            GameLevel(
                id = "starting_meadow",
                name = "Peaceful Meadow",
                description = "A gentle grassland perfect for beginners",
                encounterRate = 0.15f,
                possibleEncounters = listOf("starter_slime", "grass_bug", "tiny_bird"),
                requiredItem = null
            ),
            GameLevel(
                id = "enchanted_forest",
                name = "Enchanted Forest",
                description = "A mysterious forest full of magical creatures",
                encounterRate = 0.25f,
                possibleEncounters = listOf("forest_beast", "tree_spirit", "fairy_fly"),
                requiredItem = "forest_key"
            ),
            GameLevel(
                id = "volcanic_caves",
                name = "Volcanic Caves",
                description = "Hot caves where fire monsters make their home",
                encounterRate = 0.30f,
                possibleEncounters = listOf("fire_sprite", "lava_worm", "crystal_golem"),
                requiredItem = "heat_protection"
            ),
            GameLevel(
                id = "sky_islands",
                name = "Floating Islands",
                description = "Magical islands floating in the sky",
                encounterRate = 0.20f,
                possibleEncounters = listOf("sky_bird", "wind_elemental", "cloud_sheep"),
                requiredItem = "wind_charm"
            ),
            GameLevel(
                id = "ancient_ruins",
                name = "Ancient Ruins",
                description = "Mysterious ruins filled with powerful monsters",
                encounterRate = 0.35f,
                possibleEncounters = listOf("stone_guardian", "ghost_knight", "ancient_dragon"),
                requiredItem = "archaeologist_pass"
            )
        )
    }
    
    // Helper functions (these would normally query a database)
    private fun getBaseStatsForSpecies(speciesId: String): MonsterStats {
        return when (speciesId) {
            "starter_slime" -> MonsterStats(30, 25, 35, 20, 30, 120, 40)
            "fire_sprite" -> MonsterStats(45, 20, 50, 60, 40, 90, 80)
            "forest_beast" -> MonsterStats(55, 45, 40, 25, 35, 140, 50)
            "sky_bird" -> MonsterStats(40, 30, 70, 35, 45, 100, 60)
            else -> MonsterStats(35, 30, 35, 30, 30, 100, 50)
        }
    }
    
    private fun scaleStatsForLevel(baseStats: MonsterStats, level: Int): MonsterStats {
        val multiplier = 1.0f + (level - 1) * 0.1f
        return MonsterStats(
            attack = (baseStats.attack * multiplier).toInt(),
            defense = (baseStats.defense * multiplier).toInt(),
            agility = (baseStats.agility * multiplier).toInt(),
            magic = (baseStats.magic * multiplier).toInt(),
            wisdom = (baseStats.wisdom * multiplier).toInt(),
            maxHp = (baseStats.maxHp * multiplier).toInt(),
            maxMp = (baseStats.maxMp * multiplier).toInt()
        )
    }
    
    private fun getSpeciesName(speciesId: String): String {
        return when (speciesId) {
            "starter_slime" -> "Gel Slime"
            "fire_sprite" -> "Flame Sprite"
            "forest_beast" -> "Moss Wolf"
            "sky_bird" -> "Wind Falcon"
            else -> "Wild Monster"
        }
    }
    
    private fun getSpeciesType1(speciesId: String): MonsterType {
        return when (speciesId) {
            "starter_slime" -> MonsterType.NORMAL
            "fire_sprite" -> MonsterType.FIRE
            "forest_beast" -> MonsterType.GRASS
            "sky_bird" -> MonsterType.FLYING
            else -> MonsterType.NORMAL
        }
    }
    
    private fun getSpeciesType2(speciesId: String): MonsterType? {
        return when (speciesId) {
            "forest_beast" -> MonsterType.NORMAL
            "sky_bird" -> MonsterType.NORMAL
            else -> null
        }
    }
    
    private fun getSpeciesFamily(speciesId: String): MonsterFamily {
        return when (speciesId) {
            "starter_slime" -> MonsterFamily.SLIME
            "fire_sprite" -> MonsterFamily.MATERIAL
            "forest_beast" -> MonsterFamily.BEAST
            "sky_bird" -> MonsterFamily.BIRD
            else -> MonsterFamily.BEAST
        }
    }
    
    private fun getSkillsForLevel(speciesId: String, level: Int): List<String> {
        val allSkills = when (speciesId) {
            "starter_slime" -> mapOf(1 to "tackle", 3 to "heal", 7 to "bounce")
            "fire_sprite" -> mapOf(1 to "spark", 5 to "fireball", 10 to "flame_burst")
            "forest_beast" -> mapOf(1 to "bite", 4 to "vine_whip", 8 to "howl")
            "sky_bird" -> mapOf(1 to "peck", 6 to "gust", 11 to "dive_bomb")
            else -> mapOf(1 to "tackle")
        }
        
        return allSkills.filter { it.key <= level }.values.toList()
    }
    
    private fun generateRandomTraits(): List<String> {
        val traits = listOf("Hardy", "Brave", "Gentle", "Fierce", "Calm", "Swift", "Sturdy", "Clever")
        return if (Random.nextFloat() < 0.3f) listOf(traits.random()) else emptyList()
    }
    
    private fun getBaseCaptureRate(speciesId: String): Int {
        return when (speciesId) {
            "starter_slime" -> 200
            "fire_sprite" -> 150
            "forest_beast" -> 120
            "sky_bird" -> 100
            else -> 100
        }
    }
    
    private fun getGrowthRate(speciesId: String): GrowthRate {
        return when (speciesId) {
            "starter_slime" -> GrowthRate.MEDIUM_FAST
            "fire_sprite" -> GrowthRate.FAST
            "forest_beast" -> GrowthRate.MEDIUM_SLOW
            "sky_bird" -> GrowthRate.FAST
            else -> GrowthRate.MEDIUM_FAST
        }
    }
    
    private fun calculateExpToNextLevel(level: Int): Long {
        return (level * 100 + level * level * 5).toLong()
    }
}

/**
 * Result of exploring an area
 */
data class ExplorationResult(
    val newPosition: Position,
    val encounter: Monster?,
    val itemFound: Item?,
    val specialEvent: String?
)