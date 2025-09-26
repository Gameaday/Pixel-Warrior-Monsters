package com.pixelwarrior.monsters.game.exploration

import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.random.Random

/**
 * Advanced exploration system with gate keys, weather, day/night cycle, and monster nests
 */
class ExplorationSystem {
    
    // Gate Keys System
    enum class GateKey(val displayName: String, val description: String) {
        FOREST_KEY("Forest Gate Key", "Opens the Ancient Forest sanctuary"),
        FLAME_KEY("Flame Gate Key", "Unlocks volcanic chambers deep underground"),
        ICE_KEY("Ice Gate Key", "Opens frozen passages in crystal caverns"),
        STONE_KEY("Stone Gate Key", "Grants access to forgotten ruins"),
        TIDE_KEY("Tide Gate Key", "Opens underwater grottos and deep passages"),
        WIND_KEY("Wind Gate Key", "Unlocks sky towers floating in the clouds"),
        SAND_KEY("Sand Gate Key", "Opens sealed tomb chambers in the desert"),
        CRYSTAL_KEY("Crystal Gate Key", "Unlocks the deepest crystal cave sanctuaries")
    }
    
    // Weather System
    enum class WeatherType(val displayName: String, val encounterModifier: Double, val battleEffect: String) {
        SUNNY("Sunny", 1.0, "No special effects"),
        RAINY("Rainy", 1.2, "Water attacks deal 20% more damage"),
        STORMY("Stormy", 1.5, "Electric attacks deal 30% more damage, accuracy reduced"),
        FOGGY("Foggy", 0.8, "Accuracy reduced by 25%"),
        SNOWY("Snowy", 1.1, "Ice attacks deal 20% more damage, agility reduced"),
        WINDY("Windy", 1.3, "Flying monsters appear more often")
    }
    
    // Monster Nests
    data class MonsterNest(
        val name: String,
        val location: String,
        val specialBonus: String,
        val requiredKey: GateKey?,
        val breedingTimeReduction: Double,
        val rareMonsterChance: Double
    )
    
    // Time and Weather State
    data class ExplorationState(
        val currentWeather: WeatherType,
        val timeOfDay: LocalTime,
        val gateKeysCollected: Set<GateKey>,
        val activeNests: List<MonsterNest>,
        val hiddenPassagesFound: Set<String>,
        val weatherChangeTime: LocalDateTime
    )
    
    private var currentState = ExplorationState(
        currentWeather = WeatherType.SUNNY,
        timeOfDay = LocalTime.now(),
        gateKeysCollected = emptySet(),
        activeNests = emptyList(),
        hiddenPassagesFound = emptySet(),
        weatherChangeTime = LocalDateTime.now().plusHours(2)
    )
    
    // Predefined Monster Nests
    private val allNests = listOf(
        MonsterNest("Whispering Grove", "Forest depths", "Grass-type breeding bonus +25%", GateKey.FOREST_KEY, 0.8, 0.15),
        MonsterNest("Ember Sanctuary", "Volcanic core", "Fire-type stat boost +20%", GateKey.FLAME_KEY, 0.7, 0.20),
        MonsterNest("Frost Hollow", "Ice caverns", "Ice-type health boost +30%", GateKey.ICE_KEY, 0.75, 0.18),
        MonsterNest("Stone Circle", "Ancient ruins", "Rock-type defense boost +25%", GateKey.STONE_KEY, 0.85, 0.12),
        MonsterNest("Coral Gardens", "Underwater grotto", "Water-type breeding success +30%", GateKey.TIDE_KEY, 0.6, 0.25),
        MonsterNest("Cloud Roost", "Sky tower peak", "Flying-type agility boost +35%", GateKey.WIND_KEY, 0.9, 0.10),
        MonsterNest("Oasis Haven", "Desert sanctuary", "Ground-type stamina boost +40%", GateKey.SAND_KEY, 0.8, 0.15),
        MonsterNest("Crystal Nexus", "Cave heart", "All types stat boost +15%", GateKey.CRYSTAL_KEY, 0.5, 0.30),
        MonsterNest("Mystic Pool", "Hidden spring", "Magic-type MP boost +50%", null, 0.9, 0.08),
        MonsterNest("Shadow Nook", "Dark corner", "Dark-type breeding variety +40%", null, 0.85, 0.12)
    )
    
    // Day/Night Cycle Effects
    fun isNightTime(): Boolean {
        val hour = currentState.timeOfDay.hour
        return hour < 6 || hour >= 18
    }
    
    fun getDayNightModifier(): Double {
        return if (isNightTime()) 1.3 else 1.0 // Night increases encounter rates
    }
    
    // Weather System
    suspend fun updateWeather() {
        val now = LocalDateTime.now()
        if (now.isAfter(currentState.weatherChangeTime)) {
            val newWeather = WeatherType.values().random()
            val nextChangeTime = now.plusHours(Random.nextLong(1, 5))
            
            currentState = currentState.copy(
                currentWeather = newWeather,
                weatherChangeTime = nextChangeTime
            )
        }
        
        // Update time of day
        currentState = currentState.copy(timeOfDay = LocalTime.now())
    }
    
    // Gate Key Collection
    fun collectGateKey(key: GateKey): Boolean {
        if (key in currentState.gateKeysCollected) return false
        
        currentState = currentState.copy(
            gateKeysCollected = currentState.gateKeysCollected + key
        )
        
        // Unlock any nests that require this key
        val newNests = allNests.filter { nest ->
            nest.requiredKey == key && nest !in currentState.activeNests
        }
        
        if (newNests.isNotEmpty()) {
            currentState = currentState.copy(
                activeNests = currentState.activeNests + newNests
            )
        }
        
        return true
    }
    
    // Hidden Passage Discovery
    fun discoverHiddenPassage(passageName: String, puzzleSolved: Boolean): Boolean {
        if (!puzzleSolved || passageName in currentState.hiddenPassagesFound) {
            return false
        }
        
        currentState = currentState.copy(
            hiddenPassagesFound = currentState.hiddenPassagesFound + passageName
        )
        return true
    }
    
    // Time-Based Events
    fun checkTimeBasedEvents(): List<String> {
        val events = mutableListOf<String>()
        val hour = currentState.timeOfDay.hour
        
        when (hour) {
            6 -> events.add("Dawn breaks - Wild monsters are more active!")
            12 -> events.add("Noon sun - Fire-type monsters appear more frequently")
            18 -> events.add("Dusk approaches - Ghost-type monsters begin to emerge")
            0 -> events.add("Midnight hour - Rare nocturnal monsters can be found")
        }
        
        // Weather-based events
        when (currentState.currentWeather) {
            WeatherType.STORMY -> {
                if (Random.nextFloat() < 0.3f) {
                    events.add("Lightning illuminates a hidden treasure!")
                }
            }
            WeatherType.RAINY -> {
                if (Random.nextFloat() < 0.2f) {
                    events.add("Rainbow appears - rare Water-type monster spotted!")
                }
            }
            WeatherType.FOGGY -> {
                if (Random.nextFloat() < 0.25f) {
                    events.add("Mysterious figure vanishes in the mist...")
                }
            }
            else -> { /* No special events */ }
        }
        
        return events
    }
    
    // Monster Nest Benefits
    fun getNestBreedingBonus(nestName: String): Double {
        return currentState.activeNests
            .find { it.name == nestName }
            ?.breedingTimeReduction ?: 1.0
    }
    
    fun getNestRareChance(nestName: String): Double {
        return currentState.activeNests
            .find { it.name == nestName }
            ?.rareMonsterChance ?: 0.0
    }
    
    // Encounter Rate Calculation
    fun calculateEncounterRate(baseRate: Double): Double {
        val weatherModifier = currentState.currentWeather.encounterModifier
        val timeModifier = getDayNightModifier()
        return baseRate * weatherModifier * timeModifier
    }
    
    // Current State Access
    fun getCurrentState(): ExplorationState = currentState.copy()
    
    fun getAvailableKeys(): List<GateKey> = GateKey.values().toList()
    
    fun hasGateKey(key: GateKey): Boolean = key in currentState.gateKeysCollected
    
    fun getActiveNests(): List<MonsterNest> = currentState.activeNests.toList()
    
    fun getCurrentWeather(): WeatherType = currentState.currentWeather
    
    fun getHiddenPassages(): Set<String> = currentState.hiddenPassagesFound.toSet()
    
    // Special Area Access
    fun canAccessSpecialArea(requiredKey: GateKey): Boolean {
        return hasGateKey(requiredKey)
    }
    
    fun canAccessHiddenPassage(passageName: String): Boolean {
        return passageName in currentState.hiddenPassagesFound
    }
    
    // Weather Forecast (for UI)
    fun getWeatherForecast(): List<Pair<String, WeatherType>> {
        val forecast = mutableListOf<Pair<String, WeatherType>>()
        var time = currentState.weatherChangeTime
        var weather = WeatherType.values().random()
        
        for (i in 1..5) {
            val timeStr = "${time.hour}:${String.format("%02d", time.minute)}"
            forecast.add(timeStr to weather)
            time = time.plusHours(Random.nextLong(1, 4))
            weather = WeatherType.values().random()
        }
        
        return forecast
    }
}