package com.pixelwarrior.monsters.game.synthesis

import com.pixelwarrior.monsters.data.model.Monster
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * Scout System - Send monsters on exploration missions
 * Based on Phase 2 of the roadmap implementation
 */

/**
 * Scout mission types with different rewards and durations
 */
enum class ScoutMissionType(
    val displayName: String, 
    val duration: Long, // in milliseconds
    val goldReward: IntRange,
    val itemRewards: List<String>,
    val experienceReward: IntRange
) {
    QUICK_PATROL("Quick Patrol", 30_000L, 10..25, listOf("Herb", "Antidote"), 5..15),
    TREASURE_HUNT("Treasure Hunt", 120_000L, 25..75, listOf("Enhancement Stone", "Rare Gem", "Ancient Coin"), 15..40),
    DEEP_EXPLORATION("Deep Exploration", 300_000L, 50..150, listOf("Power Crystal", "Ancient Core", "Dragon Scale"), 25..60),
    LEGENDARY_QUEST("Legendary Quest", 600_000L, 100..300, listOf("Divine Shard", "Soul Crystal", "Demon Seal"), 50..120),
    MATERIAL_GATHERING("Material Gathering", 180_000L, 15..50, listOf("Iron Ore", "Magic Wood", "Crystal Shard"), 10..30),
    MONSTER_RESCUE("Monster Rescue", 240_000L, 30..80, listOf("Friendship Potion", "Revival Herb"), 20..50)
}

/**
 * Scout mission status
 */
enum class ScoutStatus {
    AVAILABLE, IN_PROGRESS, COMPLETED, FAILED
}

/**
 * Scout mission data
 */
data class ScoutMission(
    val id: String,
    val type: ScoutMissionType,
    val scoutMonsterId: String,
    val startTime: Long,
    val status: ScoutStatus,
    val rewards: ScoutRewards? = null,
    val failureReason: String? = null
) {
    fun getRemainingTime(): Long {
        if (status != ScoutStatus.IN_PROGRESS) return 0L
        val elapsed = System.currentTimeMillis() - startTime
        return maxOf(0L, type.duration - elapsed)
    }
    
    fun isCompleted(): Boolean = getRemainingTime() <= 0L && status == ScoutStatus.IN_PROGRESS
}

/**
 * Scout mission rewards
 */
data class ScoutRewards(
    val gold: Int,
    val items: List<String>,
    val experience: Int,
    val discoveredMonsters: List<String> = emptyList(),
    val specialReward: String? = null
)

/**
 * Scout System implementation
 */
class ScoutSystem {
    
    private val activeMissions = mutableMapOf<String, ScoutMission>()
    
    /**
     * Start a scout mission with a monster
     */
    fun startScoutMission(monster: EnhancedMonster, missionType: ScoutMissionType): ScoutMissionResult {
        // Check if monster is already on a mission
        if (isMonsterOnMission(monster.baseMonster.id)) {
            return ScoutMissionResult.Failure("Monster is already on a scout mission")
        }
        
        // Check monster requirements for mission
        val requiredLevel = getRequiredLevel(missionType)
        if (monster.baseMonster.level < requiredLevel) {
            return ScoutMissionResult.Failure("Monster must be level $requiredLevel or higher for this mission")
        }
        
        // Create mission
        val mission = ScoutMission(
            id = "mission_${System.currentTimeMillis()}_${Random.nextInt(1000)}",
            type = missionType,
            scoutMonsterId = monster.baseMonster.id,
            startTime = System.currentTimeMillis(),
            status = ScoutStatus.IN_PROGRESS
        )
        
        activeMissions[mission.id] = mission
        
        return ScoutMissionResult.Success(mission)
    }
    
    /**
     * Complete a scout mission and get rewards
     */
    fun completeMission(missionId: String, scout: EnhancedMonster): CompletionResult {
        val mission = activeMissions[missionId] 
            ?: return CompletionResult.Failure("Mission not found")
        
        if (mission.status != ScoutStatus.IN_PROGRESS) {
            return CompletionResult.Failure("Mission is not in progress")
        }
        
        if (!mission.isCompleted()) {
            return CompletionResult.Failure("Mission is not yet completed")
        }
        
        // Calculate mission success
        val success = calculateMissionSuccess(scout, mission.type)
        
        if (!success) {
            val failedMission = mission.copy(
                status = ScoutStatus.FAILED,
                failureReason = generateFailureReason(mission.type)
            )
            activeMissions[missionId] = failedMission
            return CompletionResult.Failure(failedMission.failureReason ?: "Mission failed")
        }
        
        // Generate rewards
        val rewards = generateRewards(scout, mission.type)
        val completedMission = mission.copy(
            status = ScoutStatus.COMPLETED,
            rewards = rewards
        )
        
        activeMissions[missionId] = completedMission
        
        return CompletionResult.Success(rewards, scout.baseMonster.id)
    }
    
    /**
     * Get all active scout missions
     */
    fun getActiveMissions(): List<ScoutMission> {
        return activeMissions.values.toList()
    }
    
    /**
     * Get missions for a specific monster
     */
    fun getMissionsForMonster(monsterId: String): List<ScoutMission> {
        return activeMissions.values.filter { it.scoutMonsterId == monsterId }
    }
    
    /**
     * Check if monster is on a mission
     */
    fun isMonsterOnMission(monsterId: String): Boolean {
        return activeMissions.values.any { 
            it.scoutMonsterId == monsterId && it.status == ScoutStatus.IN_PROGRESS 
        }
    }
    
    /**
     * Get available mission types for a monster
     */
    fun getAvailableMissions(monster: EnhancedMonster): List<ScoutMissionType> {
        return ScoutMissionType.values().filter { missionType ->
            monster.baseMonster.level >= getRequiredLevel(missionType)
        }
    }
    
    /**
     * Cancel an active mission (with penalties)
     */
    fun cancelMission(missionId: String): Boolean {
        val mission = activeMissions[missionId] ?: return false
        
        if (mission.status != ScoutStatus.IN_PROGRESS) return false
        
        val cancelledMission = mission.copy(
            status = ScoutStatus.FAILED,
            failureReason = "Mission cancelled by trainer"
        )
        
        activeMissions[missionId] = cancelledMission
        return true
    }
    
    /**
     * Monitor mission progress
     */
    fun monitorMission(missionId: String): Flow<ScoutMission> = flow {
        while (true) {
            val mission = activeMissions[missionId]
            if (mission != null) {
                emit(mission)
                if (mission.status != ScoutStatus.IN_PROGRESS) break
            }
            delay(5000) // Update every 5 seconds
        }
    }
    
    private fun getRequiredLevel(missionType: ScoutMissionType): Int {
        return when (missionType) {
            ScoutMissionType.QUICK_PATROL -> 5
            ScoutMissionType.MATERIAL_GATHERING -> 10
            ScoutMissionType.TREASURE_HUNT -> 15
            ScoutMissionType.MONSTER_RESCUE -> 20
            ScoutMissionType.DEEP_EXPLORATION -> 25
            ScoutMissionType.LEGENDARY_QUEST -> 35
        }
    }
    
    private fun calculateMissionSuccess(scout: EnhancedMonster, missionType: ScoutMissionType): Boolean {
        val baseSuccessRate = when (missionType) {
            ScoutMissionType.QUICK_PATROL -> 0.95f
            ScoutMissionType.MATERIAL_GATHERING -> 0.85f
            ScoutMissionType.TREASURE_HUNT -> 0.75f
            ScoutMissionType.MONSTER_RESCUE -> 0.70f
            ScoutMissionType.DEEP_EXPLORATION -> 0.60f
            ScoutMissionType.LEGENDARY_QUEST -> 0.45f
        }
        
        // Modify success rate based on scout stats
        val stats = scout.getEnhancedStats()
        val levelBonus = (scout.baseMonster.level / 100f).coerceAtMost(0.2f)
        val agilityBonus = (stats.agility / 200f).coerceAtMost(0.15f)
        val personalityBonus = getPersonalityMissionBonus(scout.personality, missionType)
        
        val finalSuccessRate = (baseSuccessRate + levelBonus + agilityBonus + personalityBonus).coerceAtMost(0.95f)
        
        return Random.nextFloat() < finalSuccessRate
    }
    
    private fun getPersonalityMissionBonus(personality: MonsterPersonality, missionType: ScoutMissionType): Float {
        return when (missionType) {
            ScoutMissionType.QUICK_PATROL -> if (personality == MonsterPersonality.TIMID) 0.1f else 0f
            ScoutMissionType.TREASURE_HUNT -> if (personality == MonsterPersonality.CAREFUL) 0.1f else 0f
            ScoutMissionType.DEEP_EXPLORATION -> if (personality == MonsterPersonality.BRAVE) 0.1f else 0f
            ScoutMissionType.LEGENDARY_QUEST -> if (personality == MonsterPersonality.ADAMANT) 0.1f else 0f
            ScoutMissionType.MATERIAL_GATHERING -> if (personality == MonsterPersonality.HARDY) 0.1f else 0f
            ScoutMissionType.MONSTER_RESCUE -> if (personality == MonsterPersonality.GENTLE) 0.1f else 0f
        }
    }
    
    private fun generateRewards(scout: EnhancedMonster, missionType: ScoutMissionType): ScoutRewards {
        val stats = scout.getEnhancedStats()
        
        // Base rewards from mission type
        val gold = missionType.goldReward.random()
        val experience = missionType.experienceReward.random()
        
        // Select random items from mission rewards
        val numItems = Random.nextInt(1, minOf(4, missionType.itemRewards.size + 1))
        val items = missionType.itemRewards.shuffled().take(numItems)
        
        // Bonus rewards based on scout stats and personality
        val bonusGold = (gold * (stats.agility / 200f)).toInt()
        val bonusExp = (experience * (stats.wisdom / 200f)).toInt()
        
        // Special personality-based rewards
        val specialReward = generateSpecialReward(scout.personality, missionType)
        
        // Rare chance to discover new monsters
        val discoveredMonsters = if (Random.nextFloat() < 0.1f) {
            listOf(generateDiscoveredMonster(missionType))
        } else emptyList()
        
        return ScoutRewards(
            gold = gold + bonusGold,
            items = items,
            experience = experience + bonusExp,
            discoveredMonsters = discoveredMonsters,
            specialReward = specialReward
        )
    }
    
    private fun generateSpecialReward(personality: MonsterPersonality, missionType: ScoutMissionType): String? {
        val chance = when (personality) {
            MonsterPersonality.CAREFUL, MonsterPersonality.SERIOUS -> 0.15f
            MonsterPersonality.BRAVE, MonsterPersonality.ADAMANT -> 0.12f
            else -> 0.08f
        }
        
        if (Random.nextFloat() > chance) return null
        
        return when (missionType) {
            ScoutMissionType.LEGENDARY_QUEST -> "Ancient Artifact"
            ScoutMissionType.DEEP_EXPLORATION -> "Rare Mineral"
            ScoutMissionType.TREASURE_HUNT -> "Golden Statue"
            ScoutMissionType.MONSTER_RESCUE -> "Friendship Medal"
            ScoutMissionType.MATERIAL_GATHERING -> "Pure Extract"
            ScoutMissionType.QUICK_PATROL -> "Scout Badge"
        }
    }
    
    private fun generateDiscoveredMonster(missionType: ScoutMissionType): String {
        return when (missionType) {
            ScoutMissionType.LEGENDARY_QUEST -> listOf("shadow_sprite", "crystal_golem", "ancient_phoenix").random()
            ScoutMissionType.DEEP_EXPLORATION -> listOf("cave_bear", "crystal_bat", "underground_worm").random()
            ScoutMissionType.TREASURE_HUNT -> listOf("treasure_mimic", "golden_slime", "coin_sprite").random()
            ScoutMissionType.MONSTER_RESCUE -> listOf("lost_cub", "injured_bird", "scared_rabbit").random()
            ScoutMissionType.MATERIAL_GATHERING -> listOf("rock_golem", "iron_beetle", "gem_crawler").random()
            ScoutMissionType.QUICK_PATROL -> listOf("patrol_dog", "watch_owl", "guard_cat").random()
        }
    }
    
    private fun generateFailureReason(missionType: ScoutMissionType): String {
        val reasons = when (missionType) {
            ScoutMissionType.QUICK_PATROL -> listOf("Got lost in familiar territory", "Distracted by interesting scents")
            ScoutMissionType.TREASURE_HUNT -> listOf("Treasure was already taken", "Trap prevented access")
            ScoutMissionType.DEEP_EXPLORATION -> listOf("Path was blocked", "Encountered too-strong monsters")
            ScoutMissionType.LEGENDARY_QUEST -> listOf("Legend was false", "Guardian was too powerful")
            ScoutMissionType.MATERIAL_GATHERING -> listOf("Materials were depleted", "Other scouts got there first")
            ScoutMissionType.MONSTER_RESCUE -> listOf("Could not find the lost monster", "Rescue attempt was too dangerous")
        }
        return reasons.random()
    }
}

/**
 * Result of starting a scout mission
 */
sealed class ScoutMissionResult {
    data class Success(val mission: ScoutMission) : ScoutMissionResult()
    data class Failure(val reason: String) : ScoutMissionResult()
}

/**
 * Result of completing a mission
 */
sealed class CompletionResult {
    data class Success(val rewards: ScoutRewards, val scoutId: String) : CompletionResult()
    data class Failure(val reason: String) : CompletionResult()
}

/**
 * Scout Deployment Interface - Phase 3 Enhancement
 * Provides user-friendly interface for deploying monsters on scout missions
 */
class ScoutDeploymentInterface(
    private val scoutSystem: ScoutSystem = ScoutSystem()
) {
    
    /**
     * Get available areas for scouting based on player progress
     */
    fun getAvailableScoutAreas(playerLevel: Int): List<ScoutArea> {
        return listOf(
            ScoutArea(
                id = "starting_plains",
                name = "Starting Plains",
                description = "Safe grasslands perfect for beginner scouts",
                unlockLevel = 1,
                availableMissions = listOf(ScoutMissionType.QUICK_PATROL, ScoutMissionType.MATERIAL_GATHERING),
                dangerLevel = 1
            ),
            ScoutArea(
                id = "mystic_forest", 
                name = "Mystic Forest",
                description = "Ancient woods filled with valuable resources",
                unlockLevel = 10,
                availableMissions = listOf(ScoutMissionType.TREASURE_HUNT, ScoutMissionType.MATERIAL_GATHERING),
                dangerLevel = 3
            ),
            ScoutArea(
                id = "crystal_caves",
                name = "Crystal Caves",
                description = "Deep caverns containing rare crystals",
                unlockLevel = 20,
                availableMissions = listOf(ScoutMissionType.DEEP_EXPLORATION, ScoutMissionType.TREASURE_HUNT),
                dangerLevel = 5
            ),
            ScoutArea(
                id = "dragon_peaks",
                name = "Dragon Peaks",
                description = "Treacherous mountains where legends are born",
                unlockLevel = 40,
                availableMissions = listOf(ScoutMissionType.LEGENDARY_QUEST, ScoutMissionType.MONSTER_RESCUE),
                dangerLevel = 8
            )
        ).filter { it.unlockLevel <= playerLevel }
    }
    
    /**
     * Deploy scout with user-friendly interface
     */
    fun deployScout(
        monster: Monster,
        area: ScoutArea,
        missionType: ScoutMissionType,
        playerInventory: Map<String, Int>
    ): ScoutDeploymentResult {
        
        // Check if monster is suitable for scouting
        if (monster.currentHp <= 0) {
            return ScoutDeploymentResult.Failure("Monster must be healthy to scout")
        }
        
        if (monster.level < area.unlockLevel) {
            return ScoutDeploymentResult.Failure("Monster level too low for this area")
        }
        
        // Check if mission type is available in this area
        if (missionType !in area.availableMissions) {
            return ScoutDeploymentResult.Failure("Mission type not available in this area")
        }
        
        // Check deployment costs
        val deploymentCost = calculateDeploymentCost(area, missionType)
        if (!canAffordDeployment(deploymentCost, playerInventory)) {
            return ScoutDeploymentResult.InsufficientResources(deploymentCost)
        }
        
        // Deploy through scout system
        val enhancedMonster = EnhancedMonster(monster)
        val result = scoutSystem.startScoutMission(enhancedMonster, missionType)
        
        return when (result) {
            is ScoutMissionResult.Success -> {
                ScoutDeploymentResult.Success(
                    mission = result.mission,
                    estimatedReturn = System.currentTimeMillis() + result.mission.type.duration,
                    deploymentCost = deploymentCost
                )
            }
            is ScoutMissionResult.Failure -> {
                ScoutDeploymentResult.Failure(result.reason)
            }
        }
    }
    
    /**
     * Get deployment cost for a mission
     */
    fun calculateDeploymentCost(area: ScoutArea, missionType: ScoutMissionType): DeploymentCost {
        val baseCost = when (missionType) {
            ScoutMissionType.QUICK_PATROL -> 10
            ScoutMissionType.MATERIAL_GATHERING -> 25
            ScoutMissionType.TREASURE_HUNT -> 50
            ScoutMissionType.DEEP_EXPLORATION -> 100
            ScoutMissionType.MONSTER_RESCUE -> 75
            ScoutMissionType.LEGENDARY_QUEST -> 200
        }
        
        val areaDifficultyMultiplier = 1.0f + (area.dangerLevel * 0.1f)
        val finalCost = (baseCost * areaDifficultyMultiplier).toInt()
        
        return DeploymentCost(
            gold = finalCost,
            provisions = when (missionType) {
                ScoutMissionType.QUICK_PATROL -> 1
                ScoutMissionType.MATERIAL_GATHERING -> 2
                ScoutMissionType.TREASURE_HUNT -> 3
                ScoutMissionType.DEEP_EXPLORATION -> 5
                ScoutMissionType.MONSTER_RESCUE -> 4
                ScoutMissionType.LEGENDARY_QUEST -> 10
            }
        )
    }
    
    private fun canAffordDeployment(cost: DeploymentCost, inventory: Map<String, Int>): Boolean {
        return (inventory["gold"] ?: 0) >= cost.gold &&
               (inventory["provisions"] ?: 0) >= cost.provisions
    }
}

/**
 * Additional data classes for scout deployment
 */
data class ScoutArea(
    val id: String,
    val name: String,
    val description: String,
    val unlockLevel: Int,
    val availableMissions: List<ScoutMissionType>,
    val dangerLevel: Int
)

data class DeploymentCost(
    val gold: Int,
    val provisions: Int
)

sealed class ScoutDeploymentResult {
    data class Success(
        val mission: ScoutMission,
        val estimatedReturn: Long,
        val deploymentCost: DeploymentCost
    ) : ScoutDeploymentResult()
    
    data class InsufficientResources(
        val requiredCost: DeploymentCost
    ) : ScoutDeploymentResult()
    
    data class Failure(
        val reason: String
    ) : ScoutDeploymentResult()
}