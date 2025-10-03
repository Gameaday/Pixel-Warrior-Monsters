package com.pixelwarrior.monsters.game.synthesis

import com.pixelwarrior.monsters.data.model.*
import kotlin.random.Random

/**
 * Monster Synthesis System - Advanced feature from Phase 2 of roadmap
 * Combines two monsters into a more powerful form with enhanced abilities
 */

/**
 * Enhancement level for Plus System
 */
enum class PlusLevel(val displayName: String, val statMultiplier: Float) {
    NORMAL("", 1.0f),
    PLUS_1("+1", 1.1f),
    PLUS_2("+2", 1.25f),
    PLUS_3("+3", 1.4f),
    PLUS_4("+4", 1.6f),
    PLUS_5("+5", 1.8f)
}

/**
 * Monster personality types affecting growth patterns and AI behavior
 */
enum class MonsterPersonality(val displayName: String, val growthBonus: Map<String, Float>) {
    HARDY("Hardy", mapOf("attack" to 1.1f, "defense" to 1.1f)),
    BRAVE("Brave", mapOf("attack" to 1.2f, "agility" to 0.9f)),
    TIMID("Timid", mapOf("agility" to 1.2f, "attack" to 0.9f)),
    MODEST("Modest", mapOf("magic" to 1.2f, "attack" to 0.9f)),
    BOLD("Bold", mapOf("defense" to 1.2f, "wisdom" to 0.9f)),
    CALM("Calm", mapOf("wisdom" to 1.2f, "defense" to 0.9f)),
    GENTLE("Gentle", mapOf("wisdom" to 1.1f, "magic" to 1.1f)),
    CAREFUL("Careful", mapOf("defense" to 1.1f, "wisdom" to 1.1f)),
    QUIRKY("Quirky", mapOf("agility" to 1.1f, "magic" to 1.1f)),
    SASSY("Sassy", mapOf("magic" to 1.1f, "agility" to 1.1f)),
    IMPISH("Impish", mapOf("agility" to 1.1f, "defense" to 1.1f)),
    RELAXED("Relaxed", mapOf("defense" to 1.1f, "agility" to 0.9f)),
    LONELY("Lonely", mapOf("attack" to 1.2f, "defense" to 0.9f)),
    ADAMANT("Adamant", mapOf("attack" to 1.2f, "magic" to 0.9f)),
    NAUGHTY("Naughty", mapOf("attack" to 1.2f, "wisdom" to 0.9f)),
    SERIOUS("Serious", mapOf()) // Balanced growth
}

/**
 * Synthesis recipe defining how monsters can be combined
 */
data class SynthesisRecipe(
    val parent1Family: MonsterFamily,
    val parent2Family: MonsterFamily,
    val resultSpeciesId: String,
    val minimumLevel: Int = 10,
    val successRate: Float = 0.8f,
    val requiredItem: String? = null
)

/**
 * Scout monster data for exploration missions
 */
data class ScoutMonster(
    val monsterId: String,
    val dungeonId: String,
    val floor: Int,
    val missionStartTime: Long,
    val missionDuration: Long, // in milliseconds
    val expectedRewards: List<String>
)

/**
 * Enhanced monster stats with synthesis and personality data
 */
data class EnhancedMonster(
    val baseMonster: Monster,
    val plusLevel: PlusLevel = PlusLevel.NORMAL,
    val personality: MonsterPersonality = MonsterPersonality.HARDY,
    val synthesisParent1: String? = null,
    val synthesisParent2: String? = null,
    val learnedSkills: List<String> = emptyList(),
    val maxSynthesisLevel: Int = 0
) {
    /**
     * Calculate enhanced stats based on plus level and personality
     */
    fun getEnhancedStats(): MonsterStats {
        val base = baseMonster.currentStats
        val plusMultiplier = plusLevel.statMultiplier
        val personalityBonus = personality.growthBonus
        
        return MonsterStats(
            attack = (base.attack * plusMultiplier * (personalityBonus["attack"] ?: 1.0f)).toInt(),
            defense = (base.defense * plusMultiplier * (personalityBonus["defense"] ?: 1.0f)).toInt(),
            agility = (base.agility * plusMultiplier * (personalityBonus["agility"] ?: 1.0f)).toInt(),
            magic = (base.magic * plusMultiplier * (personalityBonus["magic"] ?: 1.0f)).toInt(),
            wisdom = (base.wisdom * plusMultiplier * (personalityBonus["wisdom"] ?: 1.0f)).toInt(),
            maxHp = (base.maxHp * plusMultiplier).toInt(),
            maxMp = (base.maxMp * plusMultiplier).toInt()
        )
    }
}

/**
 * Monster Synthesis System implementation
 */
class MonsterSynthesis(private val random: Random = Random.Default) {
    
    private val synthesisRecipes = listOf(
        // Beast family combinations
        SynthesisRecipe(MonsterFamily.BEAST, MonsterFamily.BEAST, "dire_wolf", 15, 0.9f),
        SynthesisRecipe(MonsterFamily.BEAST, MonsterFamily.DRAGON, "drake_hound", 20, 0.7f),
        SynthesisRecipe(MonsterFamily.BEAST, MonsterFamily.BIRD, "griffin_cub", 18, 0.75f),
        
        // Slime family combinations
        SynthesisRecipe(MonsterFamily.SLIME, MonsterFamily.SLIME, "king_slime", 12, 0.85f),
        SynthesisRecipe(MonsterFamily.SLIME, MonsterFamily.MATERIAL, "metal_slime", 25, 0.6f),
        SynthesisRecipe(MonsterFamily.SLIME, MonsterFamily.PLANT, "moss_slime", 10, 0.9f),
        SynthesisRecipe(MonsterFamily.SLIME, MonsterFamily.DRAGON, "dragon_slime", 20, 0.7f),
        
        // Dragon family combinations
        SynthesisRecipe(MonsterFamily.DRAGON, MonsterFamily.DRAGON, "elder_dragon", 30, 0.5f),
        SynthesisRecipe(MonsterFamily.DRAGON, MonsterFamily.DEMON, "shadow_dragon", 28, 0.55f),
        SynthesisRecipe(MonsterFamily.DRAGON, MonsterFamily.MATERIAL, "crystal_dragon", 25, 0.6f),
        
        // Plant family combinations
        SynthesisRecipe(MonsterFamily.PLANT, MonsterFamily.PLANT, "ancient_treant", 20, 0.8f),
        SynthesisRecipe(MonsterFamily.PLANT, MonsterFamily.UNDEAD, "thorn_wraith", 22, 0.7f),
        
        // Special combinations requiring items
        SynthesisRecipe(MonsterFamily.MATERIAL, MonsterFamily.MATERIAL, "golem_lord", 25, 0.65f, "Ancient Core"),
        SynthesisRecipe(MonsterFamily.DEMON, MonsterFamily.DEMON, "arch_demon", 35, 0.4f, "Demon Seal"),
        SynthesisRecipe(MonsterFamily.UNDEAD, MonsterFamily.UNDEAD, "lich_king", 30, 0.5f, "Soul Crystal")
    )
    
    /**
     * Check if two monsters can be synthesized
     */
    fun canSynthesize(monster1: EnhancedMonster, monster2: EnhancedMonster): Boolean {
        val recipe = findSynthesisRecipe(monster1, monster2) ?: return false
        return monster1.baseMonster.level >= recipe.minimumLevel && 
               monster2.baseMonster.level >= recipe.minimumLevel &&
               monster1.maxSynthesisLevel < 3 && // Limit synthesis chains
               monster2.maxSynthesisLevel < 3
    }
    
    /**
     * Find synthesis recipe for two monsters
     */
    private fun findSynthesisRecipe(monster1: EnhancedMonster, monster2: EnhancedMonster): SynthesisRecipe? {
        return synthesisRecipes.find { recipe ->
            (recipe.parent1Family == monster1.baseMonster.family && recipe.parent2Family == monster2.baseMonster.family) ||
            (recipe.parent1Family == monster2.baseMonster.family && recipe.parent2Family == monster1.baseMonster.family)
        }
    }
    
    /**
     * Perform monster synthesis
     */
    fun synthesizeMonsters(
        monster1: EnhancedMonster, 
        monster2: EnhancedMonster,
        availableItems: List<String> = emptyList()
    ): SynthesisResult {
        val recipe = findSynthesisRecipe(monster1, monster2) 
            ?: return SynthesisResult.Failure("No valid synthesis recipe found")
        
        if (!canSynthesize(monster1, monster2)) {
            return SynthesisResult.Failure("Monsters do not meet synthesis requirements")
        }
        
        // Check required item
        recipe.requiredItem?.let { requiredItem ->
            if (!availableItems.contains(requiredItem)) {
                return SynthesisResult.Failure("Required item '$requiredItem' not available")
            }
        }
        
        // Calculate success chance
        val baseChance = recipe.successRate
        val levelBonus = ((monster1.baseMonster.level + monster2.baseMonster.level) / 100f).coerceAtMost(0.2f)
        val finalChance = (baseChance + levelBonus).coerceAtMost(0.95f)
        
        if (random.nextFloat() > finalChance) {
            return SynthesisResult.Failure("Synthesis failed - monsters were not compatible")
        }
        
        // Create synthesized monster
        val avgLevel = (monster1.baseMonster.level + monster2.baseMonster.level) / 2
        val newMonster = createSynthesizedMonster(
            recipe.resultSpeciesId,
            avgLevel,
            monster1,
            monster2,
            recipe
        )
        
        return SynthesisResult.Success(newMonster, recipe.requiredItem)
    }
    
    /**
     * Create a new synthesized monster
     */
    private fun createSynthesizedMonster(
        speciesId: String,
        level: Int,
        parent1: EnhancedMonster,
        parent2: EnhancedMonster,
        recipe: SynthesisRecipe
    ): EnhancedMonster {
        // Determine new monster properties based on species
        val (type1, type2, family) = getSpeciesInfo(speciesId)
        
        // Average and enhance stats
        val parent1Stats = parent1.getEnhancedStats()
        val parent2Stats = parent2.getEnhancedStats()
        val avgStats = MonsterStats(
            attack = (parent1Stats.attack + parent2Stats.attack) / 2 + random.nextInt(-5, 6),
            defense = (parent1Stats.defense + parent2Stats.defense) / 2 + random.nextInt(-5, 6),
            agility = (parent1Stats.agility + parent2Stats.agility) / 2 + random.nextInt(-5, 6),
            magic = (parent1Stats.magic + parent2Stats.magic) / 2 + random.nextInt(-5, 6),
            wisdom = (parent1Stats.wisdom + parent2Stats.wisdom) / 2 + random.nextInt(-5, 6),
            maxHp = (parent1Stats.maxHp + parent2Stats.maxHp) / 2 + random.nextInt(-10, 11),
            maxMp = (parent1Stats.maxMp + parent2Stats.maxMp) / 2 + random.nextInt(-10, 11)
        )
        
        // Create base monster
        val baseMonster = Monster(
            id = "synth_${System.currentTimeMillis()}_${random.nextInt(1000)}",
            speciesId = speciesId,
            name = generateSynthesisName(parent1.baseMonster.name, parent2.baseMonster.name),
            type1 = type1,
            type2 = type2,
            family = family,
            level = level,
            currentHp = avgStats.maxHp,
            currentMp = avgStats.maxMp,
            experience = 0L,
            experienceToNext = calculateExperienceToNext(level),
            baseStats = avgStats,
            currentStats = avgStats,
            skills = (parent1.baseMonster.skills + parent2.baseMonster.skills).distinct().take(6),
            traits = (parent1.baseMonster.traits + parent2.baseMonster.traits).distinct().take(3),
            growthRate = parent1.baseMonster.growthRate // Inherit from stronger parent
        )
        
        // Create enhanced monster with synthesis data
        return EnhancedMonster(
            baseMonster = baseMonster,
            plusLevel = PlusLevel.PLUS_1, // Synthesized monsters start at +1
            personality = listOf(parent1.personality, parent2.personality, MonsterPersonality.values().random(random)).random(random),
            synthesisParent1 = parent1.baseMonster.id,
            synthesisParent2 = parent2.baseMonster.id,
            learnedSkills = (parent1.learnedSkills + parent2.learnedSkills).distinct(),
            maxSynthesisLevel = maxOf(parent1.maxSynthesisLevel, parent2.maxSynthesisLevel) + 1
        )
    }
    
    /**
     * Get species information for a given species ID
     */
    private fun getSpeciesInfo(speciesId: String): Triple<MonsterType, MonsterType?, MonsterFamily> {
        return when (speciesId) {
            "dire_wolf" -> Triple(MonsterType.NORMAL, MonsterType.DARK, MonsterFamily.BEAST)
            "drake_hound" -> Triple(MonsterType.DRAGON, MonsterType.FIRE, MonsterFamily.DRAGON)
            "griffin_cub" -> Triple(MonsterType.FLYING, MonsterType.NORMAL, MonsterFamily.BIRD)
            "king_slime" -> Triple(MonsterType.NORMAL, null, MonsterFamily.SLIME)
            "metal_slime" -> Triple(MonsterType.STEEL, null, MonsterFamily.MATERIAL)
            "moss_slime" -> Triple(MonsterType.GRASS, MonsterType.WATER, MonsterFamily.PLANT)
            "dragon_slime" -> Triple(MonsterType.DRAGON, MonsterType.WATER, MonsterFamily.SLIME)
            "elder_dragon" -> Triple(MonsterType.DRAGON, MonsterType.PSYCHIC, MonsterFamily.DRAGON)
            "shadow_dragon" -> Triple(MonsterType.DRAGON, MonsterType.GHOST, MonsterFamily.DEMON)
            "crystal_dragon" -> Triple(MonsterType.DRAGON, MonsterType.ROCK, MonsterFamily.MATERIAL)
            "ancient_treant" -> Triple(MonsterType.GRASS, MonsterType.GROUND, MonsterFamily.PLANT)
            "thorn_wraith" -> Triple(MonsterType.GRASS, MonsterType.GHOST, MonsterFamily.UNDEAD)
            "golem_lord" -> Triple(MonsterType.ROCK, MonsterType.STEEL, MonsterFamily.MATERIAL)
            "arch_demon" -> Triple(MonsterType.DARK, MonsterType.FIRE, MonsterFamily.DEMON)
            "lich_king" -> Triple(MonsterType.GHOST, MonsterType.PSYCHIC, MonsterFamily.UNDEAD)
            else -> Triple(MonsterType.NORMAL, null, MonsterFamily.BEAST)
        }
    }
    
    /**
     * Calculate experience needed for next level based on growth rate
     */
    private fun calculateExperienceToNext(level: Int, growthRate: GrowthRate = GrowthRate.MEDIUM_FAST): Long {
        return when (growthRate) {
            GrowthRate.FAST -> (level * level * level * 0.8).toLong()
            GrowthRate.MEDIUM_FAST -> (level * level * level).toLong()
            GrowthRate.MEDIUM_SLOW -> (level * level * level * 1.2).toLong()
            GrowthRate.SLOW -> (level * level * level * 1.5).toLong()
            GrowthRate.VERY_FAST -> (level * level * level * 0.6).toLong()
        }
    }
    
    /**
     * Generate a name for synthesized monster
     */
    private fun generateSynthesisName(parent1Name: String, parent2Name: String): String {
        val prefixes = listOf("Fused", "Hybrid", "Enhanced", "Prime", "Evolved")
        val combinations = listOf(
            "${parent1Name.take(3)}${parent2Name.takeLast(3)}",
            "${parent2Name.take(3)}${parent1Name.takeLast(3)}",
            "${prefixes.random(random)} ${listOf(parent1Name, parent2Name).random(random)}"
        )
        return combinations.random(random)
    }
    
    /**
     * Get all possible synthesis results for a monster
     */
    fun getPossibleSyntheses(monster: EnhancedMonster, availableMonsters: List<EnhancedMonster>): List<SynthesisPreview> {
        return availableMonsters.mapNotNull { partner ->
            val recipe = findSynthesisRecipe(monster, partner)
            recipe?.let {
                val successRate = calculateSuccessRate(monster, partner, it)
                SynthesisPreview(
                    isCompatible = true,
                    possibleOffspring = listOf(it.resultSpeciesId),
                    successRate = successRate,
                    cost = SynthesisCost(gold = (monster.baseMonster.level + partner.baseMonster.level) * 100)
                )
            }
        }
    }
    
    private fun calculateSuccessRate(monster1: EnhancedMonster, monster2: EnhancedMonster, recipe: SynthesisRecipe): Float {
        val baseChance = recipe.successRate
        val levelBonus = ((monster1.baseMonster.level + monster2.baseMonster.level) / 100f).coerceAtMost(0.2f)
        return (baseChance + levelBonus).coerceAtMost(0.95f)
    }
    
    /**
     * Check if two monsters can be synthesized
     */
    fun checkCompatibility(parent1: EnhancedMonster, parent2: EnhancedMonster): CompatibilityResult {
        // Cannot synthesize same species
        if (parent1.baseMonster.speciesId == parent2.baseMonster.speciesId) {
            return CompatibilityResult.SameSpecies
        }
        
        // Check level requirements
        val minLevel = 10 // Minimum level for synthesis
        if (parent1.baseMonster.level < minLevel || parent2.baseMonster.level < minLevel) {
            return CompatibilityResult.LevelTooLow(minLevel)
        }
        
        // Check if families are compatible
        val recipe = findSynthesisRecipe(parent1, parent2)
        if (recipe == null) {
            return CompatibilityResult.IncompatibleFamily
        }
        
        return CompatibilityResult.Compatible(recipe)
    }
    
    /**
     * Get possible synthesis results for two compatible monsters
     */
    fun getPossibleSynthesisResults(parent1: EnhancedMonster, parent2: EnhancedMonster): List<String> {
        val recipe = findSynthesisRecipe(parent1, parent2) ?: return emptyList()
        return listOf(recipe.resultSpeciesId)
    }
    
    /**
     * Get all available recipes
     */
    fun getAllRecipes(): List<SynthesisRecipe> {
        return synthesisRecipes
    }
}

/**
 * Result of compatibility check between two monsters
 */
sealed class CompatibilityResult {
    data class Compatible(val recipe: SynthesisRecipe) : CompatibilityResult()
    object IncompatibleFamily : CompatibilityResult()
    data class LevelTooLow(val minimumLevel: Int) : CompatibilityResult()
    object SameSpecies : CompatibilityResult()
}

/**
 * Result of synthesis attempt
 */
sealed class SynthesisResult {
    data class Success(val synthesizedMonster: EnhancedMonster, val usedItem: String?) : SynthesisResult()
    data class Failure(val reason: String) : SynthesisResult()
    data class InProgress(val process: SynthesisProcess) : SynthesisResult()
}

/**
 * Plus System for enhancing existing monsters
 */
class PlusSystem {
    
    /**
     * Enhance a monster to the next plus level
     */
    fun enhanceMonster(monster: EnhancedMonster, enhancementItems: List<String>): PlusResult {
        if (monster.plusLevel == PlusLevel.PLUS_5) {
            return PlusResult.Failure("Monster is already at maximum enhancement level")
        }
        
        val nextLevel = getNextPlusLevel(monster.plusLevel)
        val requiredItems = getRequiredEnhancementItems(nextLevel)
        
        if (!hasRequiredItems(enhancementItems, requiredItems)) {
            return PlusResult.Failure("Missing required enhancement items: ${requiredItems.joinToString()}")
        }
        
        val enhancedMonster = monster.copy(plusLevel = nextLevel)
        return PlusResult.Success(enhancedMonster, requiredItems)
    }
    
    private fun getNextPlusLevel(current: PlusLevel): PlusLevel {
        return when (current) {
            PlusLevel.NORMAL -> PlusLevel.PLUS_1
            PlusLevel.PLUS_1 -> PlusLevel.PLUS_2
            PlusLevel.PLUS_2 -> PlusLevel.PLUS_3
            PlusLevel.PLUS_3 -> PlusLevel.PLUS_4
            PlusLevel.PLUS_4 -> PlusLevel.PLUS_5
            PlusLevel.PLUS_5 -> PlusLevel.PLUS_5 // Max level
        }
    }
    
    private fun getRequiredEnhancementItems(level: PlusLevel): List<String> {
        return when (level) {
            PlusLevel.PLUS_1 -> listOf("Enhancement Stone")
            PlusLevel.PLUS_2 -> listOf("Enhancement Stone", "Power Crystal")
            PlusLevel.PLUS_3 -> listOf("Enhancement Stone", "Power Crystal", "Rare Gem")
            PlusLevel.PLUS_4 -> listOf("Enhancement Stone", "Power Crystal", "Rare Gem", "Ancient Essence")
            PlusLevel.PLUS_5 -> listOf("Enhancement Stone", "Power Crystal", "Rare Gem", "Ancient Essence", "Divine Shard")
            else -> emptyList()
        }
    }
    
    private fun hasRequiredItems(available: List<String>, required: List<String>): Boolean {
        return required.all { item -> available.contains(item) }
    }
}

/**
 * Result of plus enhancement
 */
sealed class PlusResult {
    data class Success(val enhancedMonster: EnhancedMonster, val usedItems: List<String>) : PlusResult()
    data class Failure(val reason: String) : PlusResult()
}