package com.pixelwarrior.monsters.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Represents the different types of monsters in the game
 * These are generic types, not specific to avoid copyright issues
 */
enum class MonsterType {
    NORMAL, FIRE, WATER, GRASS, ELECTRIC, ICE, FIGHTING, POISON,
    GROUND, FLYING, PSYCHIC, BUG, ROCK, GHOST, DRAGON, DARK, STEEL
}

/**
 * Monster family categories for breeding compatibility
 */
enum class MonsterFamily {
    BEAST, BIRD, PLANT, SLIME, UNDEAD, MATERIAL, DEMON, DRAGON
}

/**
 * Core monster statistics
 */
@Parcelize
data class MonsterStats(
    val attack: Int,
    val defense: Int,
    val agility: Int,
    val magic: Int,
    val wisdom: Int,
    val maxHp: Int,
    val maxMp: Int
) : Parcelable

/**
 * Individual monster instance with unique characteristics
 */
@Parcelize
data class Monster(
    val id: String,
    val speciesId: String,
    val name: String,
    val type1: MonsterType,
    val type2: MonsterType? = null,
    val family: MonsterFamily,
    val level: Int,
    val currentHp: Int,
    val currentMp: Int,
    val experience: Long,
    val experienceToNext: Long,
    val baseStats: MonsterStats,
    val currentStats: MonsterStats,
    val skills: List<String> = emptyList(),
    val traits: List<String> = emptyList(),
    val isWild: Boolean = false,
    val captureRate: Int = 100,
    val growthRate: GrowthRate = GrowthRate.MEDIUM_FAST,
    val affection: Int = 0, // New field for monster affection/friendship (0-100)
    val personality: Personality = Personality.NONE // Monster personality type
) : Parcelable {
    /**
     * Get HP percentage (0.0 to 1.0)
     */
    fun getHpPercentage(): Float = if (currentStats.maxHp > 0) currentHp.toFloat() / currentStats.maxHp else 0f
    
    /**
     * Check if monster is fainted
     */
    fun isFainted(): Boolean = currentHp <= 0
    
    /**
     * Get display name (same as name for now)
     */
    fun getDisplayName(): String = name
}

/**
 * Monster growth rates determine experience required for leveling
 */
enum class GrowthRate {
    SLOW, MEDIUM_SLOW, MEDIUM_FAST, FAST, VERY_FAST
}

/**
 * Monster species template for generating individual monsters
 */
@Parcelize
data class MonsterSpecies(
    val id: String,
    val name: String,
    val type1: MonsterType,
    val type2: MonsterType? = null,
    val family: MonsterFamily,
    val baseStats: MonsterStats,
    val skillsLearnedByLevel: Map<Int, List<String>> = emptyMap(),
    val possibleTraits: List<String> = emptyList(),
    val captureRate: Int,
    val growthRate: GrowthRate,
    val breedingCompatibility: List<MonsterFamily> = emptyList(),
    val description: String = ""
) : Parcelable
/**
 * Monster personality types that affect behavior in battle
 */
enum class Personality {
    NONE, AGGRESSIVE, DEFENSIVE, CAUTIOUS, BRAVE, TIMID, 
    HARDY, GENTLE, RECKLESS, CALM, ENERGETIC, LAZY
}
