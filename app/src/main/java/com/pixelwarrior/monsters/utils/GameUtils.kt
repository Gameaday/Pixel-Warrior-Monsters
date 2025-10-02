package com.pixelwarrior.monsters.utils

import com.pixelwarrior.monsters.data.model.*
import kotlin.random.Random

/**
 * Utility functions for game calculations and operations
 */
object GameUtils {
    
    /**
     * Calculate experience required for a specific level
     */
    fun calculateExpForLevel(level: Int, growthRate: GrowthRate): Long {
        val baseExp = when (growthRate) {
            GrowthRate.SLOW -> level * 125L + level * level * level * 6L / 5L
            GrowthRate.MEDIUM_SLOW -> level * 100L + level * level * level * 4L / 5L
            GrowthRate.MEDIUM_FAST -> level * 100L + level * level * level * 3L / 5L
            GrowthRate.FAST -> level * 80L + level * level * level * 4L / 5L
            GrowthRate.VERY_FAST -> level * 60L + level * level * level * 3L / 5L
        }
        return baseExp.coerceAtLeast(1)
    }
    
    /**
     * Calculate total experience needed to reach a level
     */
    fun calculateTotalExpForLevel(level: Int, growthRate: GrowthRate): Long {
        var totalExp = 0L
        for (i in 1 until level) {
            totalExp += calculateExpForLevel(i, growthRate)
        }
        return totalExp
    }
    
    /**
     * Level up a monster and return the updated monster with new stats
     */
    fun levelUpMonster(monster: Monster): Monster {
        val newLevel = monster.level + 1
        val newStats = calculateStatsForLevel(monster.baseStats, newLevel)
        val expToNext = calculateExpForLevel(newLevel + 1, monster.growthRate)
        
        // Heal monster on level up
        val newHp = (monster.currentHp + (newStats.maxHp - monster.currentStats.maxHp)).coerceAtMost(newStats.maxHp)
        val newMp = (monster.currentMp + (newStats.maxMp - monster.currentStats.maxMp)).coerceAtMost(newStats.maxMp)
        
        return monster.copy(
            level = newLevel,
            currentHp = newHp,
            currentMp = newMp,
            experienceToNext = expToNext,
            currentStats = newStats
        )
    }
    
    /**
     * Calculate stats for a specific level
     */
    fun calculateStatsForLevel(baseStats: MonsterStats, level: Int): MonsterStats {
        val growthMultiplier = 1.0f + (level - 1) * 0.08f
        
        return MonsterStats(
            attack = (baseStats.attack * growthMultiplier).toInt(),
            defense = (baseStats.defense * growthMultiplier).toInt(),
            agility = (baseStats.agility * growthMultiplier).toInt(),
            magic = (baseStats.magic * growthMultiplier).toInt(),
            wisdom = (baseStats.wisdom * growthMultiplier).toInt(),
            maxHp = (baseStats.maxHp * growthMultiplier).toInt(),
            maxMp = (baseStats.maxMp * growthMultiplier).toInt()
        )
    }
    
    /**
     * Calculate capture probability based on target monster's condition
     */
    fun calculateCaptureRate(monster: Monster, captureItem: String = "basic_capture"): Float {
        val hpRatio = monster.currentHp.toFloat() / monster.currentStats.maxHp
        val baseRate = monster.captureRate / 255.0f
        
        // Lower HP increases capture rate
        val hpModifier = (1.0f - hpRatio) * 0.5f + 0.5f
        
        // Item modifier
        val itemModifier = when (captureItem) {
            "basic_capture" -> 1.0f
            "great_capture" -> 1.5f
            "ultra_capture" -> 2.0f
            "master_capture" -> 3.0f
            else -> 1.0f
        }
        
        return (baseRate * hpModifier * itemModifier).coerceAtMost(0.95f)
    }
    
    /**
     * Generate a random nature/trait for a monster
     */
    fun generateRandomNature(): String {
        val natures = listOf(
            "Hardy", "Lonely", "Brave", "Adamant", "Naughty",
            "Bold", "Docile", "Relaxed", "Impish", "Lax",
            "Timid", "Hasty", "Serious", "Jolly", "Naive",
            "Modest", "Mild", "Quiet", "Bashful", "Rash",
            "Calm", "Gentle", "Sassy", "Careful", "Quirky"
        )
        return natures.random()
    }
    
    /**
     * Calculate AI battle action for wild monsters
     */
    fun calculateAIAction(monster: Monster, playerMonster: Monster): BattleActionData {
        val availableSkills = monster.skills.filter { skillId ->
            getSkillMpCost(skillId) <= monster.currentMp
        }
        
        // AI strategy based on HP percentage
        val hpPercentage = monster.currentHp.toFloat() / monster.currentStats.maxHp
        
        return when {
            // Low HP - try to heal if possible
            hpPercentage < 0.3f && availableSkills.contains("heal") -> {
                BattleActionData(
                    action = BattleAction.SKILL,
                    skillId = "heal",
                    targetIndex = 0,
                    actingMonster = monster,
                    priority = 1
                )
            }
            // Medium HP - use powerful attacks
            hpPercentage < 0.7f && availableSkills.isNotEmpty() -> {
                val bestSkill = availableSkills.maxByOrNull { getSkillPower(it) } ?: availableSkills.random()
                BattleActionData(
                    action = BattleAction.SKILL,
                    skillId = bestSkill,
                    targetIndex = 0,
                    actingMonster = monster,
                    priority = 0
                )
            }
            // Default to basic attack
            else -> {
                BattleActionData(
                    action = BattleAction.ATTACK,
                    skillId = null,
                    targetIndex = 0,
                    actingMonster = monster,
                    priority = 0
                )
            }
        }
    }
    
    /**
     * Validate monster name (basic validation)
     */
    fun isValidMonsterName(name: String): Boolean {
        return name.isNotBlank() && 
               name.length <= 12 && 
               name.all { it.isLetterOrDigit() || it.isWhitespace() }
    }
    
    /**
     * Format experience display
     */
    fun formatExperience(exp: Long): String {
        return when {
            exp >= 1000000 -> "${exp / 1000000}M"
            exp >= 1000 -> "${exp / 1000}K"
            else -> exp.toString()
        }
    }
    
    /**
     * Calculate friendship level based on interactions
     */
    fun calculateFriendshipLevel(interactions: Int): Int {
        return when {
            interactions < 10 -> 1
            interactions < 25 -> 2
            interactions < 50 -> 3
            interactions < 100 -> 4
            else -> 5
        }
    }
    
    /**
     * Generate unique monster nickname suggestions
     */
    fun generateNicknameSuggestions(species: String): List<String> {
        val prefixes = listOf("Little", "Brave", "Swift", "Mighty", "Gentle", "Wild", "Magic")
        val suffixes = listOf("paw", "wing", "tail", "fang", "claw", "eye", "heart")
        val adjectives = listOf("Sunny", "Shadow", "Storm", "Star", "Moon", "Fire", "Ice")
        
        return listOf(
            "${prefixes.random()} ${species.split("_").last().replaceFirstChar { it.uppercase() }}",
            "${adjectives.random()}${suffixes.random().replaceFirstChar { it.uppercase() }}",
            species.split("_").joinToString("") { it.replaceFirstChar { char -> char.uppercase() } }
        )
    }
    
    // Helper functions for AI calculations
    private fun getSkillMpCost(skillId: String): Int {
        return when (skillId) {
            "heal" -> 6
            "fireball" -> 8
            "gust" -> 5
            "bite" -> 3
            else -> 0
        }
    }
    
    private fun getSkillPower(skillId: String): Int {
        return when (skillId) {
            "heal" -> 40
            "fireball" -> 60
            "gust" -> 45
            "bite" -> 50
            "tackle" -> 40
            else -> 0
        }
    }
}

/**
 * Extension functions for common operations
 */

/**
 * Check if monster can level up
 */
fun Monster.canLevelUp(): Boolean {
    return this.experience >= GameUtils.calculateTotalExpForLevel(this.level + 1, this.growthRate)
}

/**
 * Get monster's MP percentage
 */
fun Monster.getMpPercentage(): Float {
    return if (currentStats.maxMp > 0) currentMp.toFloat() / currentStats.maxMp else 0f
}