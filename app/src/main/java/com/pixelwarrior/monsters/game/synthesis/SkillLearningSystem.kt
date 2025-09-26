package com.pixelwarrior.monsters.game.synthesis

import com.pixelwarrior.monsters.data.model.*
import com.pixelwarrior.monsters.data.database.SkillEntity
import com.pixelwarrior.monsters.data.repository.GameRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random

/**
 * Skill Learning System - Phase 3 Implementation
 * Handles item-based skill learning, skill compatibility, and mastery progression
 */
class SkillLearningSystem(
    private val gameRepository: GameRepository
) {
    
    private val _learnableSkills = MutableStateFlow<Map<String, List<LearnableSkill>>>(emptyMap())
    val learnableSkills: StateFlow<Map<String, List<LearnableSkill>>> = _learnableSkills.asStateFlow()
    
    private val skillLearningDatabase = createSkillLearningDatabase()
    
    /**
     * Initialize skill learning system
     */
    suspend fun initializeSkillLearning() {
        val allSkills = gameRepository.getAllSkills()
        _learnableSkills.value = organizeSkillsByCategory(allSkills)
    }
    
    /**
     * Attempt to teach a skill to a monster using an item
     */
    suspend fun learnSkillFromItem(
        monster: Monster,
        skillItem: SkillItem,
        playerInventory: Map<String, Int>
    ): SkillLearningResult {
        
        // Check if player has the required item
        if ((playerInventory[skillItem.itemId] ?: 0) < skillItem.requiredQuantity) {
            return SkillLearningResult.Failure("Not enough ${skillItem.itemName} (need ${skillItem.requiredQuantity})")
        }
        
        // Check if monster already knows this skill
        if (monster.skills.contains(skillItem.skillId)) {
            return SkillLearningResult.Failure("${monster.name} already knows ${skillItem.skillName}")
        }
        
        // Check skill compatibility
        val compatibility = checkSkillCompatibility(monster, skillItem.skillId)
        if (!compatibility.canLearn) {
            return SkillLearningResult.Failure(compatibility.reason)
        }
        
        // Check if monster has room for more skills
        if (monster.skills.size >= getMaxSkills(monster)) {
            return SkillLearningResult.NeedsSkillReplacement(monster.skills)
        }
        
        // Calculate learning success rate
        val successRate = calculateLearningSuccessRate(monster, skillItem)
        
        // Roll for success
        val isSuccess = Random.nextFloat() < successRate
        
        if (!isSuccess) {
            return SkillLearningResult.Failure("${monster.name} failed to learn ${skillItem.skillName}")
        }
        
        // Successfully learned the skill
        val updatedSkills = monster.skills + skillItem.skillId
        val updatedMonster = monster.copy(skills = updatedSkills)
        
        return SkillLearningResult.Success(
            updatedMonster = updatedMonster,
            learnedSkill = skillItem.skillName,
            itemsConsumed = mapOf(skillItem.itemId to skillItem.requiredQuantity)
        )
    }
    
    /**
     * Replace an existing skill with a new one
     */
    suspend fun replaceSkill(
        monster: Monster,
        oldSkillId: String,
        newSkillItem: SkillItem
    ): SkillLearningResult {
        
        if (!monster.skills.contains(oldSkillId)) {
            return SkillLearningResult.Failure("Monster doesn't know the skill to replace")
        }
        
        // Replace skill in the list
        val updatedSkills = monster.skills.map { 
            if (it == oldSkillId) newSkillItem.skillId else it 
        }
        
        val updatedMonster = monster.copy(skills = updatedSkills)
        val oldSkillName = getSkillName(oldSkillId)
        
        return SkillLearningResult.SkillReplaced(
            updatedMonster = updatedMonster,
            learnedSkill = newSkillItem.skillName,
            replacedSkill = oldSkillName,
            itemsConsumed = mapOf(newSkillItem.itemId to newSkillItem.requiredQuantity)
        )
    }
    
    /**
     * Get skills that a monster can potentially learn
     */
    suspend fun getLearnableSkillsForMonster(monster: Monster): List<LearnableSkill> {
        val allSkills = gameRepository.getAllSkills()
        val learnableSkills = mutableListOf<LearnableSkill>()
        
        for (skill in allSkills) {
            // Skip if monster already knows this skill
            if (monster.skills.contains(skill.skillId)) continue
            
            // Check if skill is compatible with monster type/family
            val compatibility = checkSkillCompatibility(monster, skill.skillId)
            if (!compatibility.canLearn) continue
            
            // Find skill learning items for this skill
            val skillItems = getSkillItemsForSkill(skill.skillId)
            
            if (skillItems.isNotEmpty()) {
                learnableSkills.add(
                    LearnableSkill(
                        skillId = skill.skillId,
                        skillName = skill.name,
                        description = skill.description,
                        learningItems = skillItems,
                        compatibility = compatibility,
                        requiredLevel = getMinimumLevelForSkill(skill.skillId, monster)
                    )
                )
            }
        }
        
        return learnableSkills.sortedBy { it.requiredLevel }
    }
    
    /**
     * Increase skill mastery through usage
     */
    fun increaseSkillMastery(monster: Monster, skillId: String): Monster {
        if (!monster.skills.contains(skillId)) return monster
        
        // Track skill mastery in monster traits (simplified)
        val masteryKey = "${skillId}_mastery"
        val currentMastery = monster.traits.find { it.startsWith(masteryKey) }?.let {
            it.split("_").last().toIntOrNull() ?: 0
        } ?: 0
        
        val newMastery = minOf(currentMastery + 1, 100)
        val updatedTraits = monster.traits.filter { !it.startsWith(masteryKey) } + "${masteryKey}_$newMastery"
        
        return monster.copy(traits = updatedTraits)
    }
    
    /**
     * Get skill mastery level for a monster
     */
    fun getSkillMastery(monster: Monster, skillId: String): Int {
        val masteryKey = "${skillId}_mastery"
        return monster.traits.find { it.startsWith(masteryKey) }?.let {
            it.split("_").last().toIntOrNull() ?: 0
        } ?: 0
    }
    
    private suspend fun checkSkillCompatibility(monster: Monster, skillId: String): SkillCompatibility {
        val skill = gameRepository.getSkillById(skillId)
            ?: return SkillCompatibility(false, "Skill not found")
        
        // Check level requirement
        val minLevel = getMinimumLevelForSkill(skillId, monster)
        if (monster.level < minLevel) {
            return SkillCompatibility(false, "Monster must be level $minLevel to learn this skill")
        }
        
        // Check type compatibility
        val isTypeCompatible = when (skill.element) {
            "FIRE" -> monster.type1 == MonsterType.FIRE || monster.type2 == MonsterType.FIRE
            "WATER" -> monster.type1 == MonsterType.WATER || monster.type2 == MonsterType.WATER
            "GRASS" -> monster.type1 == MonsterType.GRASS || monster.type2 == MonsterType.GRASS
            "ELECTRIC" -> monster.type1 == MonsterType.ELECTRIC || monster.type2 == MonsterType.ELECTRIC
            "ICE" -> monster.type1 == MonsterType.ICE || monster.type2 == MonsterType.ICE
            "NORMAL" -> true // Normal skills can be learned by anyone
            else -> true // Other elements are generally learnable
        }
        
        if (!isTypeCompatible && skill.element != "NORMAL") {
            return SkillCompatibility(false, "Monster type is incompatible with ${skill.element} skills")
        }
        
        // Check family restrictions
        val familyCompatible = when (skill.skillType) {
            "HEALING" -> monster.family in listOf(MonsterFamily.PLANT, MonsterFamily.SLIME)
            "DARK" -> monster.family in listOf(MonsterFamily.UNDEAD, MonsterFamily.DEMON)
            else -> true
        }
        
        if (!familyCompatible) {
            return SkillCompatibility(false, "Monster family cannot learn this type of skill")
        }
        
        return SkillCompatibility(true, "Compatible")
    }
    
    private fun calculateLearningSuccessRate(monster: Monster, skillItem: SkillItem): Float {
        val baseRate = 0.8f
        val levelBonus = minOf(monster.level * 0.01f, 0.15f)
        val friendshipBonus = 0.0f // Could add friendship system later
        val itemQualityBonus = when (skillItem.rarity) {
            SkillItemRarity.COMMON -> 0.0f
            SkillItemRarity.UNCOMMON -> 0.05f
            SkillItemRarity.RARE -> 0.1f
            SkillItemRarity.LEGENDARY -> 0.2f
        }
        
        return (baseRate + levelBonus + friendshipBonus + itemQualityBonus).coerceIn(0.5f, 0.95f)
    }
    
    private fun getMaxSkills(monster: Monster): Int {
        return when {
            monster.level >= 50 -> 8
            monster.level >= 30 -> 6
            monster.level >= 15 -> 4
            else -> 2
        }
    }
    
    private fun getMinimumLevelForSkill(skillId: String, monster: Monster): Int {
        return when (skillId) {
            "heal" -> 5
            "fireball", "ice_shard", "thunder" -> 10
            "guard", "sleep" -> 3
            else -> 1
        }
    }
    
    private suspend fun getSkillName(skillId: String): String {
        return gameRepository.getSkillById(skillId)?.name ?: "Unknown Skill"
    }
    
    private fun getSkillItemsForSkill(skillId: String): List<SkillItem> {
        return skillLearningDatabase[skillId] ?: emptyList()
    }
    
    private fun organizeSkillsByCategory(skills: List<SkillEntity>): Map<String, List<LearnableSkill>> {
        return skills.groupBy { it.skillType }.mapValues { (_, skillsInType) ->
            skillsInType.map { skill ->
                LearnableSkill(
                    skillId = skill.skillId,
                    skillName = skill.name,
                    description = skill.description,
                    learningItems = getSkillItemsForSkill(skill.skillId),
                    compatibility = SkillCompatibility(true, "General availability"),
                    requiredLevel = 1
                )
            }
        }
    }
    
    private fun createSkillLearningDatabase(): Map<String, List<SkillItem>> {
        return mapOf(
            "heal" to listOf(
                SkillItem("healing_herb", "Healing Herb", "heal", "Heal", 3, SkillItemRarity.COMMON),
                SkillItem("restoration_potion", "Restoration Potion", "heal", "Heal", 1, SkillItemRarity.UNCOMMON)
            ),
            "fireball" to listOf(
                SkillItem("fire_stone", "Fire Stone", "fireball", "Fireball", 2, SkillItemRarity.UNCOMMON),
                SkillItem("flame_crystal", "Flame Crystal", "fireball", "Fireball", 1, SkillItemRarity.RARE)
            ),
            "ice_shard" to listOf(
                SkillItem("ice_crystal", "Ice Crystal", "ice_shard", "Ice Shard", 2, SkillItemRarity.UNCOMMON),
                SkillItem("frozen_core", "Frozen Core", "ice_shard", "Ice Shard", 1, SkillItemRarity.RARE)
            ),
            "thunder" to listOf(
                SkillItem("thunder_gem", "Thunder Gem", "thunder", "Thunder", 2, SkillItemRarity.UNCOMMON),
                SkillItem("storm_essence", "Storm Essence", "thunder", "Thunder", 1, SkillItemRarity.RARE)
            ),
            "guard" to listOf(
                SkillItem("defense_manual", "Defense Manual", "guard", "Guard", 1, SkillItemRarity.COMMON),
                SkillItem("shield_tome", "Shield Tome", "guard", "Guard", 1, SkillItemRarity.UNCOMMON)
            ),
            "sleep" to listOf(
                SkillItem("sleep_powder", "Sleep Powder", "sleep", "Sleep", 3, SkillItemRarity.COMMON),
                SkillItem("dream_dust", "Dream Dust", "sleep", "Sleep", 2, SkillItemRarity.UNCOMMON)
            ),
            "poison_sting" to listOf(
                SkillItem("poison_sac", "Poison Sac", "poison_sting", "Poison Sting", 2, SkillItemRarity.UNCOMMON),
                SkillItem("venom_extract", "Venom Extract", "poison_sting", "Poison Sting", 1, SkillItemRarity.RARE)
            )
        )
    }
}

/**
 * Data classes for skill learning system
 */
data class SkillItem(
    val itemId: String,
    val itemName: String,
    val skillId: String,
    val skillName: String,
    val requiredQuantity: Int,
    val rarity: SkillItemRarity
)

data class LearnableSkill(
    val skillId: String,
    val skillName: String,
    val description: String,
    val learningItems: List<SkillItem>,
    val compatibility: SkillCompatibility,
    val requiredLevel: Int
)

data class SkillCompatibility(
    val canLearn: Boolean,
    val reason: String
)

enum class SkillItemRarity {
    COMMON, UNCOMMON, RARE, LEGENDARY
}

sealed class SkillLearningResult {
    data class Success(
        val updatedMonster: Monster,
        val learnedSkill: String,
        val itemsConsumed: Map<String, Int>
    ) : SkillLearningResult()
    
    data class SkillReplaced(
        val updatedMonster: Monster,
        val learnedSkill: String,
        val replacedSkill: String,
        val itemsConsumed: Map<String, Int>
    ) : SkillLearningResult()
    
    data class NeedsSkillReplacement(
        val currentSkills: List<String>
    ) : SkillLearningResult()
    
    data class Failure(
        val reason: String
    ) : SkillLearningResult()
}