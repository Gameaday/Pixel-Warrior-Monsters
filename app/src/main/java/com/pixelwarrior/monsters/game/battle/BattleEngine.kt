package com.pixelwarrior.monsters.game.battle

import com.pixelwarrior.monsters.data.model.*
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * Core battle system that handles turn-based combat mechanics
 * Implements damage calculation, status effects, and battle flow
 */
class BattleEngine {
    
    /**
     * Calculate damage from an attack
     */
    fun calculateDamage(
        attacker: Monster,
        defender: Monster,
        skill: Skill,
        isPhysical: Boolean = true
    ): Int {
        val attackStat = if (isPhysical) attacker.currentStats.attack else attacker.currentStats.magic
        val defenseStat = if (isPhysical) defender.currentStats.defense else defender.currentStats.wisdom
        
        // Base damage calculation
        val baseDamage = (attackStat * skill.power) / 100
        
        // Level difference modifier
        val levelDiff = attacker.level - defender.level
        val levelModifier = 1.0f + (levelDiff * 0.05f)
        
        // Type effectiveness (simplified)
        val typeModifier = calculateTypeEffectiveness(attacker.type1, defender.type1)
        
        // Critical hit chance (5% base)
        val isCritical = Random.nextFloat() < 0.05f
        val criticalModifier = if (isCritical) 1.5f else 1.0f
        
        // Random variance (85%-115%)
        val randomModifier = Random.nextFloat() * 0.3f + 0.85f
        
        val finalDamage = (baseDamage * levelModifier * typeModifier * criticalModifier * randomModifier).toInt()
        
        return maxOf(1, finalDamage) // Minimum 1 damage
    }
    
    /**
     * Simple type effectiveness system
     */
    private fun calculateTypeEffectiveness(attackerType: MonsterType, defenderType: MonsterType): Float {
        return when {
            // Super effective combinations
            (attackerType == MonsterType.FIRE && defenderType == MonsterType.GRASS) ||
            (attackerType == MonsterType.WATER && defenderType == MonsterType.FIRE) ||
            (attackerType == MonsterType.GRASS && defenderType == MonsterType.WATER) ||
            (attackerType == MonsterType.ELECTRIC && defenderType == MonsterType.FLYING) ||
            (attackerType == MonsterType.FIGHTING && defenderType == MonsterType.NORMAL) -> 1.5f
            
            // Not very effective combinations
            (attackerType == MonsterType.FIRE && defenderType == MonsterType.WATER) ||
            (attackerType == MonsterType.WATER && defenderType == MonsterType.GRASS) ||
            (attackerType == MonsterType.GRASS && defenderType == MonsterType.FIRE) ||
            (attackerType == MonsterType.ELECTRIC && defenderType == MonsterType.GROUND) -> 0.5f
            
            // Normal effectiveness
            else -> 1.0f
        }
    }
    
    /**
     * Execute a battle turn and return the updated battle state
     */
    suspend fun executeBattleTurn(
        battleState: BattleState,
        playerAction: BattleActionData,
        enemyAction: BattleActionData
    ): BattleState {
        var newState = battleState.copy(battlePhase = BattlePhase.ANIMATION)
        
        // Determine turn order based on agility/priority
        val actions = listOf(playerAction, enemyAction).sortedByDescending { 
            it.priority * 1000 + it.actingMonster.currentStats.agility
        }
        
        // Execute actions in order
        for (action in actions) {
            newState = executeAction(newState, action)
            
            // Add delay for animation timing
            delay(500)
            
            // Check if battle should end
            if (isBattleOver(newState)) {
                newState = newState.copy(
                    battlePhase = if (isPlayerVictorious(newState)) BattlePhase.VICTORY else BattlePhase.DEFEAT
                )
                break
            }
        }
        
        return if (newState.battlePhase != BattlePhase.VICTORY && newState.battlePhase != BattlePhase.DEFEAT) {
            newState.copy(
                battlePhase = BattlePhase.SELECTION,
                turn = newState.turn + 1
            )
        } else {
            newState
        }
    }
    
    /**
     * Execute a single battle action
     */
    private fun executeAction(battleState: BattleState, action: BattleActionData): BattleState {
        return when (action.action) {
            BattleAction.ATTACK -> executeAttack(battleState, action)
            BattleAction.SKILL -> executeSkill(battleState, action)
            BattleAction.DEFEND -> executeDefend(battleState, action)
            BattleAction.RUN -> executeRun(battleState, action)
        }
    }
    
    /**
     * Execute basic attack
     */
    private fun executeAttack(battleState: BattleState, action: BattleActionData): BattleState {
        // Simple attack using a basic attack skill
        val basicAttack = Skill(
            id = "basic_attack",
            name = "Attack",
            description = "Basic physical attack",
            type = SkillType.PHYSICAL,
            target = SkillTarget.SINGLE_ENEMY,
            mpCost = 0,
            power = 50,
            accuracy = 95
        )
        
        return executeSkillDamage(battleState, action, basicAttack)
    }
    
    /**
     * Execute skill attack
     */
    private fun executeSkill(battleState: BattleState, action: BattleActionData): BattleState {
        val skillId = action.skillId ?: return battleState
        
        // Load skill from skill database
        val skill = loadSkillFromDatabase(skillId) ?: return battleState
        val skill = getSkillById(skillId) ?: return battleState
        
        // Check MP cost
        if (action.actingMonster.currentMp < skill.mpCost) {
            return battleState // Not enough MP
        }
        
        return executeSkillDamage(battleState, action, skill)
    }
    
    /**
     * Execute defend action
     */
    private fun executeDefend(battleState: BattleState, action: BattleActionData): BattleState {
        // TODO: Implement defense boost for next turn
        return battleState
    }
    
    /**
     * Execute run action
     */
    private fun executeRun(battleState: BattleState, action: BattleActionData): BattleState {
        if (!battleState.canEscape) return battleState
        
        // Calculate escape chance based on agility difference
        val playerAgility = battleState.playerMonsters[battleState.currentPlayerMonster].currentStats.agility
        val enemyAgility = battleState.enemyMonsters[battleState.currentEnemyMonster].currentStats.agility
        
        val escapeChance = 0.5f + (playerAgility - enemyAgility) * 0.01f
        
        return if (Random.nextFloat() < escapeChance.coerceIn(0.1f, 0.9f)) {
            battleState.copy(battlePhase = BattlePhase.DEFEAT) // Escaped
        } else {
            battleState // Failed to escape
        }
    }
    
    /**
     * Execute skill damage calculation and apply to target
     */
    private fun executeSkillDamage(
        battleState: BattleState,
        action: BattleActionData,
        skill: Skill
    ): BattleState {
        val isPlayerAction = battleState.playerMonsters.contains(action.actingMonster)
        val targetMonsters = if (isPlayerAction) battleState.enemyMonsters else battleState.playerMonsters
        val targetIndex = if (isPlayerAction) battleState.currentEnemyMonster else battleState.currentPlayerMonster
        
        if (targetIndex >= targetMonsters.size) return battleState
        
        val target = targetMonsters[targetIndex]
        val damage = calculateDamage(action.actingMonster, target, skill, skill.type == SkillType.PHYSICAL)
        
        val newHp = (target.currentHp - damage).coerceAtLeast(0)
        val updatedTarget = target.copy(currentHp = newHp)
        
        // Update MP cost for attacker
        val newMp = (action.actingMonster.currentMp - skill.mpCost).coerceAtLeast(0)
        val updatedAttacker = action.actingMonster.copy(currentMp = newMp)
        
        return if (isPlayerAction) {
            val updatedEnemies = battleState.enemyMonsters.toMutableList()
            updatedEnemies[targetIndex] = updatedTarget
            
            val updatedPlayers = battleState.playerMonsters.toMutableList()
            val playerIndex = updatedPlayers.indexOf(action.actingMonster)
            if (playerIndex >= 0) updatedPlayers[playerIndex] = updatedAttacker
            
            battleState.copy(
                playerMonsters = updatedPlayers,
                enemyMonsters = updatedEnemies,
                lastAction = "${action.actingMonster.name} used ${skill.name}! Dealt $damage damage!"
            )
        } else {
            val updatedPlayers = battleState.playerMonsters.toMutableList()
            updatedPlayers[targetIndex] = updatedTarget
            
            val updatedEnemies = battleState.enemyMonsters.toMutableList()
            val enemyIndex = updatedEnemies.indexOf(action.actingMonster)
            if (enemyIndex >= 0) updatedEnemies[enemyIndex] = updatedAttacker
            
            battleState.copy(
                playerMonsters = updatedPlayers,
                enemyMonsters = updatedEnemies,
                lastAction = "${action.actingMonster.name} used ${skill.name}! Dealt $damage damage!"
            )
        }
    }
    
    /**
     * Check if the battle is over
     */
    private fun isBattleOver(battleState: BattleState): Boolean {
        val playerAlive = battleState.playerMonsters.any { it.currentHp > 0 }
        val enemyAlive = battleState.enemyMonsters.any { it.currentHp > 0 }
        
        return !playerAlive || !enemyAlive
    }
    
    /**
     * Check if player won
     */
    private fun isPlayerVictorious(battleState: BattleState): Boolean {
        return battleState.enemyMonsters.all { it.currentHp <= 0 }
    }
    
    /**
     * Get skill by ID (placeholder - would load from database)
     */
    private fun getSkillById(skillId: String): Skill? {
        return when (skillId) {
            "fireball" -> Skill(
                id = "fireball",
                name = "Fireball",
                description = "A ball of fire that burns the enemy",
                type = SkillType.MAGICAL,
                target = SkillTarget.SINGLE_ENEMY,
                mpCost = 8,
                power = 75,
                accuracy = 90
            )
            "heal" -> Skill(
                id = "heal",
                name = "Heal",
                description = "Restores HP to the target",
                type = SkillType.HEALING,
                target = SkillTarget.SELF,
                mpCost = 6,
                power = 50,
                accuracy = 100
            )
            else -> null
        }
    }
}