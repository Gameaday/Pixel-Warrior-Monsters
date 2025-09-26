package com.pixelwarrior.monsters.game.ui

import com.pixelwarrior.monsters.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.delay

/**
 * UI Polish and Quality of Life System
 * Handles enhanced user experience features including animations, fast travel, and auto-save
 */
class UIPolishSystem {
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _currentNotification = MutableStateFlow<UINotification?>(null)
    val currentNotification: StateFlow<UINotification?> = _currentNotification.asStateFlow()
    
    private val _fastTravelDestinations = MutableStateFlow<List<FastTravelDestination>>(emptyList())
    val fastTravelDestinations: StateFlow<List<FastTravelDestination>> = _fastTravelDestinations.asStateFlow()
    
    private val _autoSaveSettings = MutableStateFlow(AutoSaveSettings())
    val autoSaveSettings: StateFlow<AutoSaveSettings> = _autoSaveSettings.asStateFlow()
    
    private val _userPreferences = MutableStateFlow(UserPreferences())
    val userPreferences: StateFlow<UserPreferences> = _userPreferences.asStateFlow()
    
    // Monster Animation System
    private val _currentAnimations = MutableStateFlow<Map<String, MonsterAnimation>>(emptyMap())
    val currentAnimations: StateFlow<Map<String, MonsterAnimation>> = _currentAnimations.asStateFlow()
    
    /**
     * Monster Animation Management
     */
    fun playMonsterAnimation(monsterId: String, animationType: AnimationType): MonsterAnimation {
        val animation = MonsterAnimation(
            monsterId = monsterId,
            type = animationType,
            duration = getAnimationDuration(animationType),
            isLooping = animationType == AnimationType.IDLE,
            startTime = System.currentTimeMillis()
        )
        
        val currentAnimations = _currentAnimations.value.toMutableMap()
        currentAnimations[monsterId] = animation
        _currentAnimations.value = currentAnimations
        
        return animation
    }
    
    private fun getAnimationDuration(type: AnimationType): Long = when (type) {
        AnimationType.IDLE -> 2000
        AnimationType.ATTACK -> 800
        AnimationType.SKILL -> 1200
        AnimationType.VICTORY -> 1500
        AnimationType.DEFEAT -> 2000
    }
    
    fun stopMonsterAnimation(monsterId: String) {
        val currentAnimations = _currentAnimations.value.toMutableMap()
        currentAnimations.remove(monsterId)
        _currentAnimations.value = currentAnimations
    }
    
    /**
     * Fast Travel System
     */
    fun initializeFastTravel(playerSave: GameSave) {
        val destinations = HubArea.values().mapNotNull { area ->
            FastTravelDestination(
                area = area,
                displayName = area.displayName,
                description = if (isAreaUnlocked(area, playerSave)) area.description else "Locked",
                travelTime = calculateTravelTime(area),
                isUnlocked = isAreaUnlocked(area, playerSave)
            )
        }
        _fastTravelDestinations.value = destinations
    }
    
    private fun isAreaUnlocked(area: HubArea, playerSave: GameSave): Boolean {
        area.requiredKeyItem?.let { keyItem ->
            if (!playerSave.inventory.containsKey(keyItem)) return false
        }
        area.requiredStoryProgress?.let { storyFlag ->
            if (playerSave.storyProgress[storyFlag] != true) return false
        }
        return true
    }
    
    private fun calculateTravelTime(area: HubArea): Int = when (area) {
        HubArea.MAIN_HALL -> 0
        HubArea.MONSTER_LIBRARY, HubArea.ITEM_SHOP -> 5
        HubArea.BREEDING_LAB, HubArea.BATTLE_ARENA -> 8
        HubArea.SYNTHESIS_LAB, HubArea.GATE_CHAMBER -> 12
        HubArea.MASTER_QUARTERS, HubArea.SECRET_VAULT -> 15
    }
    
    suspend fun fastTravelTo(destination: HubArea): FastTravelResult {
        val dest = _fastTravelDestinations.value.find { it.area == destination }
            ?: return FastTravelResult.FAILED("Destination not found")
        
        if (!dest.isUnlocked) {
            return FastTravelResult.FAILED("Destination is locked")
        }
        
        _isLoading.value = true
        delay(dest.travelTime * 100L) // Simulate travel time
        _isLoading.value = false
        
        showNotification(UINotification.SUCCESS("Arrived at ${dest.displayName}"))
        return FastTravelResult.SUCCESS(dest.displayName)
    }
    
    /**
     * Auto-Save System
     */
    fun configureAutoSave(settings: AutoSaveSettings) {
        _autoSaveSettings.value = settings
    }
    
    fun shouldAutoSave(event: GameEvent, timeSinceLastSave: Long): Boolean {
        val settings = _autoSaveSettings.value
        if (!settings.enabled) return false
        
        return when (event) {
            GameEvent.BATTLE_COMPLETED -> settings.saveOnBattleComplete
            GameEvent.MONSTER_SYNTHESIZED -> settings.saveOnSynthesis
            GameEvent.QUEST_COMPLETED -> settings.saveOnQuestProgress
            GameEvent.AREA_UNLOCKED -> settings.saveOnAreaUnlock
            GameEvent.PERIODIC -> timeSinceLastSave >= settings.intervalMinutes * 60 * 1000
        }
    }
    
    suspend fun performAutoSave(): AutoSaveResult {
        _isLoading.value = true
        return try {
            delay(500) // Simulate save operation
            _isLoading.value = false
            showNotification(UINotification.INFO("Game saved automatically"))
            AutoSaveResult.SUCCESS
        } catch (e: Exception) {
            _isLoading.value = false
            showNotification(UINotification.ERROR("Auto-save failed: ${e.message}"))
            AutoSaveResult.FAILED(e.message ?: "Unknown error")
        }
    }
    
    /**
     * UI Feedback System
     */
    fun showNotification(notification: UINotification) {
        _currentNotification.value = notification
    }
    
    fun dismissNotification() {
        _currentNotification.value = null
    }
    
    suspend fun showLoadingState(operation: suspend () -> Unit) {
        _isLoading.value = true
        try {
            operation()
        } finally {
            _isLoading.value = false
        }
    }
    
    /**
     * User Preferences Management
     */
    fun updateUserPreferences(preferences: UserPreferences) {
        _userPreferences.value = preferences
    }
    
    fun getAnimationQuality(): AnimationQuality = _userPreferences.value.animationQuality
    
    fun isReducedAnimationsEnabled(): Boolean = _userPreferences.value.reduceAnimations
    
    fun getFastTravelPreferences(): FastTravelPreferences = _userPreferences.value.fastTravelPreferences
}

/**
 * Data classes for UI Polish System
 */
data class MonsterAnimation(
    val monsterId: String,
    val type: AnimationType,
    val duration: Long,
    val isLooping: Boolean,
    val startTime: Long
)

enum class AnimationType {
    IDLE, ATTACK, SKILL, VICTORY, DEFEAT
}

data class FastTravelDestination(
    val area: HubArea,
    val displayName: String,
    val description: String,
    val travelTime: Int, // seconds
    val isUnlocked: Boolean
)

sealed class FastTravelResult {
    data class SUCCESS(val destinationName: String) : FastTravelResult()
    data class FAILED(val reason: String) : FastTravelResult()
}

data class AutoSaveSettings(
    val enabled: Boolean = true,
    val intervalMinutes: Int = 10,
    val saveOnBattleComplete: Boolean = true,
    val saveOnSynthesis: Boolean = true,
    val saveOnQuestProgress: Boolean = true,
    val saveOnAreaUnlock: Boolean = true,
    val maxAutoSaves: Int = 5
)

enum class GameEvent {
    BATTLE_COMPLETED, MONSTER_SYNTHESIZED, QUEST_COMPLETED, AREA_UNLOCKED, PERIODIC
}

sealed class AutoSaveResult {
    object SUCCESS : AutoSaveResult()
    data class FAILED(val reason: String) : AutoSaveResult()
}

sealed class UINotification {
    data class SUCCESS(val message: String) : UINotification()
    data class ERROR(val message: String) : UINotification()
    data class INFO(val message: String) : UINotification()
    data class WARNING(val message: String) : UINotification()
}

data class UserPreferences(
    val animationQuality: AnimationQuality = AnimationQuality.HIGH,
    val reduceAnimations: Boolean = false,
    val fastTravelPreferences: FastTravelPreferences = FastTravelPreferences(),
    val autoSaveEnabled: Boolean = true,
    val showTutorialHints: Boolean = true,
    val uiTheme: UITheme = UITheme.AUTO
)

enum class AnimationQuality {
    HIGH, MEDIUM, LOW, OFF
}

data class FastTravelPreferences(
    val showTravelTime: Boolean = true,
    val confirmTravel: Boolean = false,
    val favoriteDestinations: List<HubArea> = emptyList()
)

enum class UITheme {
    LIGHT, DARK, AUTO
}

// Hub Area enum (should match existing HubWorldSystem)
enum class HubArea(
    val id: String,
    val displayName: String,
    val description: String,
    val requiredKeyItem: String? = null,
    val requiredStoryProgress: String? = null
) {
    MAIN_HALL("main_hall", "Main Hall", "Central meeting area with the Master"),
    MONSTER_LIBRARY("library", "Monster Library", "Study monster information and breeding compatibility", requiredStoryProgress = "first_capture"),
    BREEDING_LAB("breeding_lab", "Breeding Laboratory", "Advanced breeding facilities", requiredKeyItem = "breeder_license"),
    BATTLE_ARENA("arena", "Battle Arena", "Tournament battles and training", requiredStoryProgress = "first_tournament"),
    SYNTHESIS_LAB("synthesis", "Synthesis Laboratory", "Monster combination research", requiredKeyItem = "synthesis_permit"),
    ITEM_SHOP("shop", "Item Shop", "Purchase tools and healing items"),
    GATE_CHAMBER("gates", "Gate Chamber", "Access to different worlds", requiredKeyItem = "explorer_compass"),
    MASTER_QUARTERS("quarters", "Master's Quarters", "Private chambers", requiredKeyItem = "master_key"),
    SECRET_VAULT("vault", "Secret Vault", "Hidden treasures and artifacts", requiredKeyItem = "ancient_relic")
}