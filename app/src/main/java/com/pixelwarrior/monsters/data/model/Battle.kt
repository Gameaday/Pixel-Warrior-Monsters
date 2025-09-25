package com.pixelwarrior.monsters.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Battle action types
 */
enum class BattleAction {
    ATTACK, SKILL, DEFEND, RUN
}

/**
 * Battle result outcomes
 */
enum class BattleResult {
    VICTORY, DEFEAT, ESCAPE, CAPTURE
}

/**
 * Skill targeting types
 */
enum class SkillTarget {
    SELF, SINGLE_ENEMY, ALL_ENEMIES, SINGLE_ALLY, ALL_ALLIES, ALL
}

/**
 * Skill effects and damage types
 */
enum class SkillType {
    PHYSICAL, MAGICAL, STATUS, HEALING, SUPPORT
}

/**
 * Individual skill definition
 */
@Parcelize
data class Skill(
    val id: String,
    val name: String,
    val description: String,
    val type: SkillType,
    val target: SkillTarget,
    val mpCost: Int,
    val power: Int = 0,
    val accuracy: Int = 100,
    val statusEffects: List<StatusEffect> = emptyList(),
    val priority: Int = 0
) : Parcelable

/**
 * Status effects that can be applied in battle
 */
@Parcelize
data class StatusEffect(
    val id: String,
    val name: String,
    val duration: Int,
    val statChanges: Map<String, Int> = emptyMap(),
    val damagePerTurn: Int = 0,
    val healingPerTurn: Int = 0,
    val preventActions: Boolean = false
) : Parcelable

/**
 * Battle state management
 */
@Parcelize
data class BattleState(
    val playerMonsters: List<Monster>,
    val enemyMonsters: List<Monster>,
    val currentPlayerMonster: Int = 0,
    val currentEnemyMonster: Int = 0,
    val turn: Int = 1,
    val battlePhase: BattlePhase = BattlePhase.SELECTION,
    val lastAction: String = "",
    val isWildBattle: Boolean = false,
    val canEscape: Boolean = true,
    val canCapture: Boolean = false
) : Parcelable

/**
 * Current phase of battle
 */
enum class BattlePhase {
    SELECTION,    // Player selecting action
    ANIMATION,    // Playing battle animations
    RESOLUTION,   // Resolving effects
    VICTORY,      // Battle won
    DEFEAT,       // Battle lost
    CAPTURE       // Monster capture attempt
}

/**
 * Battle action with all necessary data
 */
@Parcelize
data class BattleActionData(
    val action: BattleAction,
    val skillId: String? = null,
    val targetIndex: Int = 0,
    val actingMonster: Monster,
    val priority: Int = 0
) : Parcelable