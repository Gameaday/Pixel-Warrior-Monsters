package com.pixelwarrior.monsters.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Player's save data and progression
 */
@Parcelize
data class GameSave(
    val playerId: String,
    val playerName: String,
    val currentLevel: String,
    val position: Position,
    val partyMonsters: List<Monster>,
    val farmMonsters: List<Monster> = emptyList(),
    val inventory: Map<String, Int> = emptyMap(),
    val gold: Long = 0,
    val playtimeMinutes: Long = 0,
    val storyProgress: Map<String, Boolean> = emptyMap(),
    val unlockedGates: List<String> = emptyList(),
    val gameSettings: GameSettings = GameSettings(),
    val cookingSkill: CookingSkill = CookingSkill(), // New field for cooking progression
    val saveVersion: Int = 1,
    val lastSaved: Long = System.currentTimeMillis()
) : Parcelable

/**
 * 2D position in the game world
 */
@Parcelize
data class Position(
    val x: Float,
    val y: Float,
    val facing: Direction = Direction.DOWN
) : Parcelable

/**
 * Cardinal directions for movement and facing
 */
enum class Direction {
    UP, DOWN, LEFT, RIGHT
}

/**
 * Game configuration and user preferences
 */
@Parcelize
data class GameSettings(
    val musicEnabled: Boolean = true,
    val soundEffectsEnabled: Boolean = true,
    val musicVolume: Float = 1.0f,
    val soundVolume: Float = 1.0f,
    val textSpeed: TextSpeed = TextSpeed.MEDIUM,
    val battleAnimations: Boolean = true,
    val autoSave: Boolean = true,
    val difficulty: Difficulty = Difficulty.NORMAL
) : Parcelable

/**
 * Text display speed options
 */
enum class TextSpeed {
    SLOW, MEDIUM, FAST, INSTANT
}

/**
 * Game difficulty settings
 */
enum class Difficulty {
    EASY, NORMAL, HARD, EXPERT
}

/**
 * Item types in the game
 */
enum class ItemType {
    HEALING, FOOD, TOOL, KEY_ITEM, CAPTURE, BATTLE_ITEM, BREEDING_ITEM,
    BASIC_TREAT, QUALITY_TREAT, PREMIUM_TREAT, COOKING_INGREDIENT
}

/**
 * Individual items that can be collected and used
 */
@Parcelize
data class Item(
    val id: String,
    val name: String,
    val description: String,
    val type: ItemType,
    val value: Int = 0,
    val stackable: Boolean = true,
    val usableInBattle: Boolean = false,
    val usableInField: Boolean = false,
    val effect: String = ""
) : Parcelable

/**
 * Game world location data
 */
@Parcelize
data class GameLevel(
    val id: String,
    val name: String,
    val description: String,
    val encounterRate: Float = 0.1f,
    val possibleEncounters: List<String> = emptyList(),
    val requiredItem: String? = null,
    val isCompleted: Boolean = false
) : Parcelable

/**
 * Treat quality levels that affect monster affection and joining chances
 */
enum class TreatQuality(val displayName: String, val affectionBonus: Int, val joinChanceBonus: Float) {
    BASIC("Basic Treat", 5, 0.05f),
    QUALITY("Quality Treat", 15, 0.15f),  
    PREMIUM("Premium Treat", 30, 0.25f)
}

/**
 * Individual treat item with quality and type preferences
 */
@Parcelize
data class Treat(
    val id: String,
    val name: String,
    val quality: TreatQuality,
    val description: String,
    val preferredTypes: List<MonsterType> = emptyList(), // Some treats work better on certain types
    val cookingTime: Int = 300, // Time in seconds to cook
    val requiredSkillLevel: Int = 1 // Cooking skill required
) : Parcelable

/**
 * Cooking recipe for creating treats
 */
@Parcelize
data class CookingRecipe(
    val id: String,
    val name: String,
    val resultItem: String,
    val ingredients: Map<String, Int>, // ingredient id -> quantity needed
    val cookingTime: Int,
    val requiredLevel: Int = 1,
    val description: String = ""
) : Parcelable

/**
 * Player's cooking skill progression
 */
@Parcelize
data class CookingSkill(
    val level: Int = 1,
    val experience: Int = 0,
    val knownRecipes: List<String> = emptyList()
) : Parcelable

/**
 * Save/Load screen modes
 */
enum class SaveLoadMode {
    SAVE, LOAD
}

/**
 * Individual save slot data
 */
@Parcelize
data class SaveSlot(
    val id: String,
    val playerName: String,
    val playtimeMinutes: Long,
    val currentLevel: String,
    val partySize: Int,
    val lastSaved: Long,
    val previewImagePath: String? = null
) : Parcelable

/**
 * Voice character for audio system
 */
@Parcelize
data class VoiceCharacter(
    val id: String,
    val name: String,
    val type: CharacterType
) : Parcelable

/**
 * Character type classification
 */
enum class CharacterType {
    MASTER, LIBRARIAN, SYNTHESIS_EXPERT, ARENA_MASTER, 
    MERCHANT, RIVAL, NPC, NARRATOR, MAIN_CHARACTER, SHOPKEEPER
}
