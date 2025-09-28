package com.pixelwarrior.monsters.game.endgame

import com.pixelwarrior.monsters.data.model.Monster
import com.pixelwarrior.monsters.data.model.MonsterType
import com.pixelwarrior.monsters.data.model.MonsterFamily
import com.pixelwarrior.monsters.data.model.MonsterStats
import com.pixelwarrior.monsters.data.model.GrowthRate
import kotlin.random.Random

/**
 * Endgame content system providing post-game dungeons, legendary monsters,
 * New Game Plus mode, and advanced progression systems
 */
class EndgameSystem {
    
    // Post-game dungeons with ultra-high difficulty
    enum class PostGameDungeon(
        val displayName: String,
        val requiredLevel: Int,
        val floors: Int,
        val theme: String,
        val description: String
    ) {
        VOID_NEXUS("Void Nexus", 80, 50, "Cosmic", "A realm beyond reality where the strongest monsters dwell"),
        PRIMAL_DEPTHS("Primal Depths", 85, 40, "Primordial", "Ancient caves where the first monsters were born"),
        CELESTIAL_TOWER("Celestial Tower", 90, 60, "Divine", "A tower reaching into the heavens itself"),
        TEMPORAL_MAZE("Temporal Maze", 95, 30, "Time", "A dungeon that exists across multiple timelines"),
        INFINITY_REALM("Infinity Realm", 99, 100, "Ultimate", "The final challenge for true masters")
    }
    
    // Legendary monsters with unique abilities
    data class LegendaryMonster(
        val species: String,
        val type1: MonsterType,
        val type2: MonsterType?,
        val family: MonsterFamily,
        val baseAttack: Int,
        val baseDefense: Int,
        val baseAgility: Int,
        val baseMagic: Int,
        val baseWisdom: Int,
        val baseHP: Int,
        val baseMP: Int,
        val uniqueAbility: String,
        val encounterRate: Double, // Very low encounter rates
        val requiredDungeon: PostGameDungeon,
        val spawnConditions: List<String>
    )
    
    // New Game Plus bonuses and challenges
    data class NewGamePlusData(
        val playthrough: Int,
        val retainedGold: Int,
        val retainedItems: List<String>,
        val bonusStarterLevel: Int,
        val difficultyMultiplier: Double,
        val unlockedFeatures: List<String>
    )
    
    // Additional worlds with unique themes
    enum class AdditionalWorld(
        val displayName: String,
        val theme: String,
        val levelRange: IntRange,
        val uniqueMonsterTypes: List<MonsterType>,
        val unlockRequirement: String
    ) {
        SHADOW_REALM("Shadow Realm", "Dark", 70..85, listOf(MonsterType.DARK), "Defeat 50 dark-type monsters"),
        MECHANICAL_ZONE("Mechanical Zone", "Tech", 75..90, listOf(MonsterType.STEEL, MonsterType.ELECTRIC), "Synthesize 20 material-type monsters"),
        FAIRY_GARDEN("Fairy Garden", "Magic", 65..80, listOf(MonsterType.GRASS), "Breed 100 monsters successfully"),
        ANCIENT_KINGDOM("Ancient Kingdom", "Historic", 80..95, listOf(MonsterType.DRAGON), "Complete all 8 main dungeons"),
        DREAM_DIMENSION("Dream Dimension", "Psychic", 85..99, listOf(MonsterType.PSYCHIC), "Reach maximum friendship with 10 monsters")
    }
    
    // Legendary monsters definitions
    private val legendaryMonsters = listOf(
        LegendaryMonster(
            species = "Void Dragon",
            type1 = MonsterType.DRAGON,
            type2 = MonsterType.DARK,
            family = MonsterFamily.DRAGON,
            baseAttack = 180,
            baseDefense = 160,
            baseAgility = 140,
            baseMagic = 200,
            baseWisdom = 180,
            baseHP = 300,
            baseMP = 250,
            uniqueAbility = "Void Breath - Ignores type resistances",
            encounterRate = 0.001, // 0.1% chance
            requiredDungeon = PostGameDungeon.VOID_NEXUS,
            spawnConditions = listOf("Floor 45+", "Player level 80+", "Night time only")
        ),
        LegendaryMonster(
            species = "Primal Behemoth",
            type1 = MonsterType.NORMAL,
            type2 = MonsterType.GROUND,
            family = MonsterFamily.BEAST,
            baseAttack = 220,
            baseDefense = 200,
            baseAgility = 100,
            baseMagic = 120,
            baseWisdom = 140,
            baseHP = 400,
            baseMP = 180,
            uniqueAbility = "Earthquake - Damages all enemies",
            encounterRate = 0.002,
            requiredDungeon = PostGameDungeon.PRIMAL_DEPTHS,
            spawnConditions = listOf("Floor 30+", "Earthquake weather", "Party has earth-type monster")
        ),
        LegendaryMonster(
            species = "Celestial Phoenix",
            type1 = MonsterType.FLYING,
            type2 = MonsterType.FIRE,
            family = MonsterFamily.BIRD,
            baseAttack = 160,
            baseDefense = 140,
            baseAgility = 200,
            baseMagic = 190,
            baseWisdom = 170,
            baseHP = 280,
            baseMP = 300,
            uniqueAbility = "Rebirth - Revives with 50% HP when defeated",
            encounterRate = 0.0015,
            requiredDungeon = PostGameDungeon.CELESTIAL_TOWER,
            spawnConditions = listOf("Floor 50+", "Dawn time only", "Player has fire-type starter")
        ),
        LegendaryMonster(
            species = "Time Serpent",
            type1 = MonsterType.DRAGON,
            type2 = MonsterType.PSYCHIC,
            family = MonsterFamily.DRAGON,
            baseAttack = 170,
            baseDefense = 150,
            baseAgility = 180,
            baseMagic = 210,
            baseWisdom = 200,
            baseHP = 320,
            baseMP = 280,
            uniqueAbility = "Time Warp - Can act twice per turn",
            encounterRate = 0.001,
            requiredDungeon = PostGameDungeon.TEMPORAL_MAZE,
            spawnConditions = listOf("Any floor", "Specific time: 12:00 or 00:00", "Temporal distortion event")
        ),
        LegendaryMonster(
            species = "Omega Destroyer",
            type1 = MonsterType.STEEL,
            type2 = MonsterType.DARK,
            family = MonsterFamily.MATERIAL,
            baseAttack = 250,
            baseDefense = 220,
            baseAgility = 160,
            baseMagic = 180,
            baseWisdom = 160,
            baseHP = 500,
            baseMP = 200,
            uniqueAbility = "Omega Beam - Ultimate attack with 50% critical rate",
            encounterRate = 0.0005, // Rarest legendary
            requiredDungeon = PostGameDungeon.INFINITY_REALM,
            spawnConditions = listOf("Floor 90+", "All other legendaries captured", "Perfect victory streak of 20+")
        )
    )
    
    /**
     * Check if post-game dungeons are unlocked
     */
    fun arePostGameDungeonsUnlocked(
        playerLevel: Int,
        completedMainStory: Boolean,
        defeatedChampion: Boolean
    ): Boolean {
        return playerLevel >= 70 && completedMainStory && defeatedChampion
    }
    
    /**
     * Get available post-game dungeons based on player progress
     */
    fun getAvailablePostGameDungeons(playerLevel: Int): List<PostGameDungeon> {
        return PostGameDungeon.values().filter { dungeon ->
            playerLevel >= dungeon.requiredLevel
        }
    }
    
    /**
     * Check for legendary monster encounters
     */
    fun checkLegendaryEncounter(
        currentDungeon: PostGameDungeon,
        currentFloor: Int,
        playerLevel: Int,
        currentTime: Int, // Hour of day (0-23)
        weatherCondition: String?,
        partyMonsters: List<Monster>,
        completedConditions: List<String>
    ): LegendaryMonster? {
        val availableLegendaries = legendaryMonsters.filter { legendary ->
            legendary.requiredDungeon == currentDungeon
        }
        
        for (legendary in availableLegendaries) {
            if (Random.nextDouble() < legendary.encounterRate) {
                // Check spawn conditions
                val conditionsMet = legendary.spawnConditions.all { condition ->
                    when {
                        condition.startsWith("Floor") -> {
                            val requiredFloor = condition.substringAfter("Floor ").removeSuffix("+").toInt()
                            currentFloor >= requiredFloor
                        }
                        condition.startsWith("Player level") -> {
                            val requiredLevel = condition.substringAfter("Player level ").removeSuffix("+").toInt()
                            playerLevel >= requiredLevel
                        }
                        condition.contains("time only") -> {
                            val timeRequirement = condition.substringBefore(" time only")
                            checkTimeRequirement(timeRequirement, currentTime)
                        }
                        condition.contains("weather") -> {
                            val requiredWeather = condition.substringBefore(" weather")
                            weatherCondition == requiredWeather
                        }
                        condition.contains("Party has") -> {
                            val requiredType = condition.substringAfter("Party has ").substringBefore("-type")
                            partyMonsters.any { it.type1.name.lowercase() == requiredType.lowercase() }
                        }
                        else -> completedConditions.contains(condition)
                    }
                }
                
                if (conditionsMet) {
                    return legendary
                }
            }
        }
        
        return null
    }
    
    private fun checkTimeRequirement(requirement: String, currentTime: Int): Boolean {
        return when (requirement.lowercase()) {
            "night" -> currentTime < 6 || currentTime >= 18
            "dawn" -> currentTime in 5..7
            "day" -> currentTime in 6..17
            "dusk" -> currentTime in 17..19
            else -> true
        }
    }
    
    /**
     * Create a legendary monster instance
     */
    fun createLegendaryMonster(legendary: LegendaryMonster, level: Int = 85): Monster {
        return Monster(
            id = java.util.UUID.randomUUID().toString(),
            speciesId = legendary.species,
            name = legendary.species,
            type1 = legendary.type1,
            type2 = legendary.type2,
            family = legendary.family,
            level = level,
            currentHp = legendary.baseHP,
            currentMp = legendary.baseMP,
            experience = 0L,
            experienceToNext = 1000L,
            baseStats = MonsterStats(
                attack = legendary.baseAttack,
                defense = legendary.baseDefense,
                agility = legendary.baseAgility,
                magic = legendary.baseMagic,
                wisdom = legendary.baseWisdom,
                maxHp = legendary.baseHP,
                maxMp = legendary.baseMP
            ),
            currentStats = MonsterStats(
                attack = legendary.baseAttack,
                defense = legendary.baseDefense,
                agility = legendary.baseAgility,
                magic = legendary.baseMagic,
                wisdom = legendary.baseWisdom,
                maxHp = legendary.baseHP,
                maxMp = legendary.baseMP
            ),
            skills = emptyList(),
            traits = emptyList(),
            isWild = true,
            captureRate = 3, // Very hard to capture
            growthRate = GrowthRate.SLOW
        )
    }
    
    /**
     * Initialize New Game Plus
     */
    fun initializeNewGamePlus(
        currentPlaythrough: Int,
        playerGold: Int,
        playerItems: List<String>,
        achievements: List<String>
    ): NewGamePlusData {
        val retainedGoldPercentage = minOf(50 + (currentPlaythrough * 10), 90)
        val retainedGold = (playerGold * retainedGoldPercentage) / 100
        
        val bonusLevel = minOf(currentPlaythrough * 5, 25)
        val difficultyMultiplier = 1.0 + (currentPlaythrough * 0.15)
        
        val unlockedFeatures = mutableListOf<String>()
        if (currentPlaythrough >= 1) unlockedFeatures.add("Advanced Breeding Options")
        if (currentPlaythrough >= 2) unlockedFeatures.add("Legendary Starter Choice")
        if (currentPlaythrough >= 3) unlockedFeatures.add("Master Difficulty Mode")
        if (currentPlaythrough >= 5) unlockedFeatures.add("Ultimate Challenge Mode")
        
        // Retain key items and special tools
        val retainedItems = playerItems.filter { item ->
            item.contains("Key") || item.contains("Tool") || item.contains("Medal")
        }
        
        return NewGamePlusData(
            playthrough = currentPlaythrough + 1,
            retainedGold = retainedGold,
            retainedItems = retainedItems,
            bonusStarterLevel = bonusLevel,
            difficultyMultiplier = difficultyMultiplier,
            unlockedFeatures = unlockedFeatures
        )
    }
    
    /**
     * Get additional world unlock status
     */
    fun getUnlockedAdditionalWorlds(achievements: List<String>, statistics: Map<String, Int>): List<AdditionalWorld> {
        return AdditionalWorld.values().filter { world ->
            when (world.unlockRequirement) {
                "Defeat 50 dark-type monsters" -> (statistics["darkTypeDefeated"] ?: 0) >= 50
                "Synthesize 20 material-type monsters" -> (statistics["materialTypeSynthesized"] ?: 0) >= 20
                "Breed 100 monsters successfully" -> (statistics["successfulBreedings"] ?: 0) >= 100
                "Complete all 8 main dungeons" -> achievements.contains("All Dungeons Complete")
                "Reach maximum friendship with 10 monsters" -> (statistics["maxFriendshipMonsters"] ?: 0) >= 10
                else -> false
            }
        }
    }
    
    /**
     * Advanced monster fusion system for endgame
     */
    data class FusionTree(
        val generation: Int,
        val parents: List<Monster>,
        val requiredLevel: Int,
        val fusionMaterial: String,
        val resultSpecies: String,
        val statBonusMultiplier: Double
    )
    
    /**
     * Create advanced fusion combinations
     */
    fun getAdvancedFusionOptions(monsters: List<Monster>): List<FusionTree> {
        val fusionOptions = mutableListOf<FusionTree>()
        
        // Triple fusion - requires 3 monsters of different families
        val familyGroups = monsters.groupBy { it.family }
        if (familyGroups.size >= 3) {
            val eligibleMonsters = familyGroups.values.flatten().filter { it.level >= 80 }
            if (eligibleMonsters.size >= 3) {
                fusionOptions.add(
                    FusionTree(
                        generation = 3,
                        parents = eligibleMonsters.take(3),
                        requiredLevel = 80,
                        fusionMaterial = "Trinity Crystal",
                        resultSpecies = "Tri-Fusion Beast",
                        statBonusMultiplier = 1.8
                    )
                )
            }
        }
        
        // Legendary fusion - requires legendary + regular monster
        val legendaries = monsters.filter { it.traits.contains("Legendary") }
        val regulars = monsters.filter { !it.traits.contains("Legendary") && it.level >= 90 }
        
        for (legendary in legendaries) {
            for (regular in regulars) {
                if (legendary.family == regular.family) {
                    fusionOptions.add(
                        FusionTree(
                            generation = 4,
                            parents = listOf(legendary, regular),
                            requiredLevel = 95,
                            fusionMaterial = "Legendary Essence",
                            resultSpecies = "Ascended ${legendary.name}",
                            statBonusMultiplier = 2.2
                        )
                    )
                }
            }
        }
        
        return fusionOptions
    }
}