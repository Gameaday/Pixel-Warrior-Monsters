package com.pixelwarrior.monsters.game.breeding

import com.pixelwarrior.monsters.data.model.*
import kotlin.random.Random
import java.util.UUID

/**
 * Monster breeding system that handles genetics, compatibility, and offspring generation
 * This system creates unique offspring based on parent characteristics
 */
class BreedingSystem {
    
    /**
     * Check if two monsters can breed together
     */
    fun canBreed(parent1: Monster, parent2: Monster): Boolean {
        // Different species requirement
        if (parent1.speciesId == parent2.speciesId) return false
        
        // Both must be at least level 10
        if (parent1.level < 10 || parent2.level < 10) return false
        
        // Must have compatible families
        return isCompatibleFamily(parent1.family, parent2.family)
    }
    
    /**
     * Check if two monster families are compatible for breeding
     */
    private fun isCompatibleFamily(family1: MonsterFamily, family2: MonsterFamily): Boolean {
        val compatibilityMap = mapOf(
            MonsterFamily.BEAST to listOf(MonsterFamily.BEAST, MonsterFamily.BIRD, MonsterFamily.DRAGON),
            MonsterFamily.BIRD to listOf(MonsterFamily.BIRD, MonsterFamily.BEAST, MonsterFamily.DRAGON),
            MonsterFamily.PLANT to listOf(MonsterFamily.PLANT, MonsterFamily.SLIME),
            MonsterFamily.SLIME to listOf(MonsterFamily.SLIME, MonsterFamily.PLANT, MonsterFamily.MATERIAL),
            MonsterFamily.UNDEAD to listOf(MonsterFamily.UNDEAD, MonsterFamily.DEMON),
            MonsterFamily.MATERIAL to listOf(MonsterFamily.MATERIAL, MonsterFamily.SLIME),
            MonsterFamily.DEMON to listOf(MonsterFamily.DEMON, MonsterFamily.UNDEAD, MonsterFamily.DRAGON),
            MonsterFamily.DRAGON to listOf(MonsterFamily.DRAGON, MonsterFamily.BEAST, MonsterFamily.BIRD, MonsterFamily.DEMON)
        )
        
        return compatibilityMap[family1]?.contains(family2) == true
    }
    
    /**
     * Generate offspring from two parent monsters
     */
    fun breedMonsters(parent1: Monster, parent2: Monster): Monster? {
        if (!canBreed(parent1, parent2)) return null
        
        // Determine offspring species (simplified - random between parents or potential hybrid)
        val offspringSpecies = determineOffspringSpecies(parent1, parent2)
        
        // Calculate inherited stats
        val inheritedStats = calculateInheritedStats(parent1, parent2)
        
        // Determine types (can inherit from either parent)
        val type1 = if (Random.nextBoolean()) parent1.type1 else parent2.type1
        val type2 = when {
            Random.nextFloat() < 0.3f -> parent1.type2
            Random.nextFloat() < 0.3f -> parent2.type2
            else -> null
        }
        
        // Determine family (usually from stronger parent)
        val family = if (parent1.level >= parent2.level) parent1.family else parent2.family
        
        // Inherit some skills from both parents
        val inheritedSkills = inheritSkills(parent1, parent2)
        
        // Generate unique traits
        val inheritedTraits = inheritTraits(parent1, parent2)
        
        // Calculate starting HP/MP based on new stats
        val startingHp = (inheritedStats.maxHp * 0.8f).toInt()
        val startingMp = (inheritedStats.maxMp * 0.8f).toInt()
        
        return Monster(
            id = UUID.randomUUID().toString(),
            speciesId = offspringSpecies,
            name = generateOffspringName(parent1, parent2),
            type1 = type1,
            type2 = type2,
            family = family,
            level = 1,
            currentHp = startingHp,
            currentMp = startingMp,
            experience = 0,
            experienceToNext = 100,
            baseStats = inheritedStats,
            currentStats = inheritedStats,
            skills = inheritedSkills,
            traits = inheritedTraits,
            isWild = false,
            captureRate = 100,
            growthRate = if (Random.nextBoolean()) parent1.growthRate else parent2.growthRate
        )
    }
    
    /**
     * Determine the species of the offspring
     */
    private fun determineOffspringSpecies(parent1: Monster, parent2: Monster): String {
        // Simplified: create hybrid species ID
        return "hybrid_${parent1.speciesId}_${parent2.speciesId}"
    }
    
    /**
     * Calculate inherited stats with some randomness
     */
    private fun calculateInheritedStats(parent1: Monster, parent2: Monster): MonsterStats {
        fun averageWithVariation(stat1: Int, stat2: Int): Int {
            val average = (stat1 + stat2) / 2
            val variation = (Random.nextFloat() - 0.5f) * 0.4f // ±20% variation
            return (average * (1.0f + variation)).toInt().coerceAtLeast(1)
        }
        
        return MonsterStats(
            attack = averageWithVariation(parent1.baseStats.attack, parent2.baseStats.attack),
            defense = averageWithVariation(parent1.baseStats.defense, parent2.baseStats.defense),
            agility = averageWithVariation(parent1.baseStats.agility, parent2.baseStats.agility),
            magic = averageWithVariation(parent1.baseStats.magic, parent2.baseStats.magic),
            wisdom = averageWithVariation(parent1.baseStats.wisdom, parent2.baseStats.wisdom),
            maxHp = averageWithVariation(parent1.baseStats.maxHp, parent2.baseStats.maxHp),
            maxMp = averageWithVariation(parent1.baseStats.maxMp, parent2.baseStats.maxMp)
        )
    }
    
    /**
     * Inherit skills from both parents
     */
    private fun inheritSkills(parent1: Monster, parent2: Monster): List<String> {
        val inheritedSkills = mutableSetOf<String>()
        
        // 50% chance to inherit each skill from each parent
        parent1.skills.forEach { skill ->
            if (Random.nextFloat() < 0.5f) {
                inheritedSkills.add(skill)
            }
        }
        
        parent2.skills.forEach { skill ->
            if (Random.nextFloat() < 0.5f) {
                inheritedSkills.add(skill)
            }
        }
        
        // Ensure at least one skill is inherited
        if (inheritedSkills.isEmpty()) {
            val allParentSkills = parent1.skills + parent2.skills
            if (allParentSkills.isNotEmpty()) {
                inheritedSkills.add(allParentSkills.random())
            }
        }
        
        return inheritedSkills.toList().take(4) // Maximum 4 starting skills
    }
    
    /**
     * Inherit traits from parents with potential for new traits
     */
    private fun inheritTraits(parent1: Monster, parent2: Monster): List<String> {
        val inheritedTraits = mutableSetOf<String>()
        
        // 30% chance to inherit each trait from each parent
        parent1.traits.forEach { trait ->
            if (Random.nextFloat() < 0.3f) {
                inheritedTraits.add(trait)
            }
        }
        
        parent2.traits.forEach { trait ->
            if (Random.nextFloat() < 0.3f) {
                inheritedTraits.add(trait)
            }
        }
        
        // 10% chance for a completely new trait
        if (Random.nextFloat() < 0.1f) {
            val newTraits = listOf(
                "Hardy", "Brave", "Gentle", "Fierce", "Calm", 
                "Swift", "Sturdy", "Clever", "Lucky", "Focused"
            )
            inheritedTraits.add(newTraits.random())
        }
        
        return inheritedTraits.toList().take(2) // Maximum 2 traits
    }
    
    /**
     * Generate a name for the offspring based on parents
     */
    private fun generateOffspringName(parent1: Monster, parent2: Monster): String {
        val prefixes = listOf("Little", "Young", "Baby", "Mini")
        val suffixes = listOf("Jr", "II", "Child", "Pup")
        
        return when (Random.nextInt(3)) {
            0 -> "${prefixes.random()} ${parent1.name}"
            1 -> "${parent2.name} ${suffixes.random()}"
            else -> {
                val name1Parts = parent1.name.split(" ")
                val name2Parts = parent2.name.split(" ")
                val part1 = name1Parts.first().take(3)
                val part2 = name2Parts.last().takeLast(3)
                part1 + part2
            }
        }
    }
    
    /**
     * Get breeding time in minutes (would be used for breeding timer)
     */
    fun getBreedingTime(parent1: Monster, parent2: Monster): Int {
        val averageLevel = (parent1.level + parent2.level) / 2
        return (60 + averageLevel * 5).coerceAtMost(300) // 1-5 hours max
    }
    
    /**
     * Calculate breeding success rate
     */
    fun calculateBreedingSuccessRate(parent1: Monster, parent2: Monster): Float {
        if (!canBreed(parent1, parent2)) return 0f
        
        val levelSum = parent1.level + parent2.level
        val baseRate = 0.7f
        val levelBonus = (levelSum - 20) * 0.01f // Bonus for higher level parents
        return (baseRate + levelBonus).coerceIn(0.1f, 0.95f)
    }
    /**
     * Check breeding compatibility between two monsters
     */
    fun checkBreedingCompatibility(monster1: Monster, monster2: Monster): BreedingCompatibility {
        val canBreed = canBreed(monster1, monster2)
        val successRate = calculateBreedingSuccessRate(monster1, monster2)
        
        return BreedingCompatibility(
            canBreed = canBreed,
            successRate = successRate,
            reason = if (!canBreed) "Monsters cannot breed" else "Compatible for breeding"
        )
    }
    
    /**
     * Generate offspring from two parent monsters
     */
    fun generateOffspring(parent1: Monster, parent2: Monster): Monster {
        val offspringLevel = maxOf(1, (parent1.level + parent2.level) / 2 - 5)
        val offspringFamily = if (kotlin.random.Random.nextFloat() < 0.5f) parent1.family else parent2.family
        
        return Monster(
            id = java.util.UUID.randomUUID().toString(),
            speciesId = "${parent1.speciesId}_${parent2.speciesId}_offspring",
            name = "Offspring",
            type1 = parent1.type1,
            type2 = parent2.type1,
            family = offspringFamily,
            level = offspringLevel,
            currentHp = 100,
            currentMp = 50,
            experience = 0,
            experienceToNext = (offspringLevel * offspringLevel * offspringLevel).toLong(),
            baseStats = MonsterStats(
                attack = (parent1.baseStats.attack + parent2.baseStats.attack) / 2,
                defense = (parent1.baseStats.defense + parent2.baseStats.defense) / 2,
                agility = (parent1.baseStats.agility + parent2.baseStats.agility) / 2,
                magic = (parent1.baseStats.magic + parent2.baseStats.magic) / 2,
                wisdom = (parent1.baseStats.wisdom + parent2.baseStats.wisdom) / 2,
                maxHp = (parent1.baseStats.maxHp + parent2.baseStats.maxHp) / 2,
                maxMp = (parent1.baseStats.maxMp + parent2.baseStats.maxMp) / 2
            ),
            currentStats = MonsterStats(
                attack = (parent1.baseStats.attack + parent2.baseStats.attack) / 2,
                defense = (parent1.baseStats.defense + parent2.baseStats.defense) / 2,
                agility = (parent1.baseStats.agility + parent2.baseStats.agility) / 2,
                magic = (parent1.baseStats.magic + parent2.baseStats.magic) / 2,
                wisdom = (parent1.baseStats.wisdom + parent2.baseStats.wisdom) / 2,
                maxHp = (parent1.baseStats.maxHp + parent2.baseStats.maxHp) / 2,
                maxMp = (parent1.baseStats.maxMp + parent2.baseStats.maxMp) / 2
            ),
            skills = listOf("Inherited Skill"),
            traits = parent1.traits.take(1) + parent2.traits.take(1),
            personality = if (kotlin.random.Random.nextFloat() < 0.5f) parent1.personality else parent2.personality
        )
    }
}

/**
 * Breeding compatibility result
 */
data class BreedingCompatibility(
    val canBreed: Boolean,
    val successRate: Float,
    val reason: String
)
