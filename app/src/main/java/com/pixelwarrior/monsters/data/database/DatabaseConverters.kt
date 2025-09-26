package com.pixelwarrior.monsters.data.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.pixelwarrior.monsters.data.model.*

/**
 * Room database type converters for complex data types
 */
class DatabaseConverters {
    private val gson = Gson()
    
    @TypeConverter
    fun fromStringMap(value: Map<String, String>?): String {
        return gson.toJson(value)
    }
    
    @TypeConverter
    fun toStringMap(value: String): Map<String, String> {
        return try {
            gson.fromJson(value, object : TypeToken<Map<String, String>>() {}.type) ?: emptyMap<String, String>()
        } catch (e: Exception) {
            emptyMap<String, String>()
        }
    }
    
    @TypeConverter
    fun fromIntMap(value: Map<String, Int>?): String {
        return gson.toJson(value)
    }
    
    @TypeConverter
    fun toIntMap(value: String): Map<String, Int> {
        return try {
            gson.fromJson(value, object : TypeToken<Map<String, Int>>() {}.type) ?: emptyMap<String, Int>()
        } catch (e: Exception) {
            emptyMap<String, Int>()
        }
    }
    
    @TypeConverter
    fun fromBooleanMap(value: Map<String, Boolean>?): String {
        return gson.toJson(value)
    }
    
    @TypeConverter
    fun toBooleanMap(value: String): Map<String, Boolean> {
        return try {
            gson.fromJson(value, object : TypeToken<Map<String, Boolean>>() {}.type) ?: emptyMap<String, Boolean>()
        } catch (e: Exception) {
            emptyMap<String, Boolean>()
        }
    }
    
    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return gson.toJson(value)
    }
    
    @TypeConverter
    fun toStringList(value: String): List<String> {
        return try {
            gson.fromJson(value, object : TypeToken<List<String>>() {}.type) ?: emptyList<String>()
        } catch (e: Exception) {
            emptyList<String>()
        }
    }
    
    @TypeConverter
    fun fromMonsterStats(stats: MonsterStats?): String {
        return gson.toJson(stats)
    }
    
    @TypeConverter
    fun toMonsterStats(value: String): MonsterStats? {
        return try {
            gson.fromJson(value, MonsterStats::class.java)
        } catch (e: Exception) {
            null
        }
    }
}

/**
 * Extension functions to convert between domain models and database entities
 */
fun GameSave.toEntity(): GameSaveEntity {
    return GameSaveEntity(
        saveId = playerId,
        playerName = playerName,
        currentLevel = 1, // Map from level string to int
        currentArea = currentLevel,
        playtimeMinutes = playtimeMinutes.toInt(),
        goldAmount = gold.toInt(),
        storyProgress = Gson().toJson(storyProgress),
        inventory = Gson().toJson(inventory),
        partyMonsters = Gson().toJson(partyMonsters.map { it.id }),
        allMonsters = Gson().toJson((partyMonsters + farmMonsters).map { it.id }),
        lastSaved = lastSaved,
        gameVersion = saveVersion.toString()
    )
}

fun GameSaveEntity.toDomain(allMonsters: List<Monster>): GameSave {
    val gson = Gson()
    val partyIds: List<String> = try {
        gson.fromJson(partyMonsters, object : TypeToken<List<String>>() {}.type) ?: emptyList<String>()
    } catch (e: Exception) { emptyList<String>() }
    
    val allMonsterIds: List<String> = try {
        gson.fromJson(this.allMonsters, object : TypeToken<List<String>>() {}.type) ?: emptyList<String>()
    } catch (e: Exception) { emptyList<String>() }
    
    val partyMonstersList = allMonsters.filter { it.id in partyIds }
    val ownedMonsters = allMonsters.filter { it.id in allMonsterIds }
    val farmMonsters = ownedMonsters - partyMonstersList
    
    return GameSave(
        playerId = saveId,
        playerName = playerName,
        currentLevel = currentArea,
        position = Position(0f, 0f),
        partyMonsters = partyMonstersList,
        farmMonsters = farmMonsters,
        playtimeMinutes = playtimeMinutes.toLong(),
        gold = goldAmount.toLong(),
        storyProgress = try {
            gson.fromJson(storyProgress, object : TypeToken<Map<String, Boolean>>() {}.type) ?: emptyMap<String, Boolean>()
        } catch (e: Exception) { emptyMap<String, Boolean>() },
        inventory = try {
            gson.fromJson(inventory, object : TypeToken<Map<String, Int>>() {}.type) ?: emptyMap<String, Int>()
        } catch (e: Exception) { emptyMap<String, Int>() },
        lastSaved = lastSaved,
        saveVersion = try { gameVersion.toInt() } catch (e: Exception) { 1 }
    )
}

fun Monster.toEntity(saveId: String): MonsterEntity {
    val gson = Gson()
    return MonsterEntity(
        monsterId = id,
        saveId = saveId,
        speciesId = speciesId,
        name = name,
        level = level,
        experience = experience,
        currentHp = currentHp,
        currentMp = currentMp,
        type1 = type1.name,
        type2 = type2?.name,
        family = family.name,
        personality = "NORMAL", // Default personality since Monster model doesn't have this
        baseStats = gson.toJson(baseStats),
        skills = gson.toJson(skills),
        traits = gson.toJson(traits),
        plusLevel = 0, // Default plus level since Monster model doesn't have this
        synthesisParents = null // Default since Monster model doesn't have this
    )
}


fun MonsterEntity.toDomain(): Monster {
    val gson = Gson()
    val stats = try {
        gson.fromJson(baseStats, MonsterStats::class.java) ?: MonsterStats(0, 0, 0, 0, 0, 0, 0)
    } catch (e: Exception) { MonsterStats(0, 0, 0, 0, 0, 0, 0) }
    
    val skillList = try {
        gson.fromJson(skills, object : TypeToken<List<String>>() {}.type) ?: emptyList<String>()
    } catch (e: Exception) { emptyList<String>() }
    
    val traitList = try {
        gson.fromJson(traits, object : TypeToken<List<String>>() {}.type) ?: emptyList<String>()
    } catch (e: Exception) { emptyList<String>() }
    
    return Monster(
        id = monsterId,
        speciesId = speciesId,
        name = name,
        type1 = MonsterType.valueOf(type1),
        type2 = type2?.let { MonsterType.valueOf(it) },
        family = MonsterFamily.valueOf(family),
        level = level,
        currentHp = currentHp,
        currentMp = currentMp,
        experience = experience,
        experienceToNext = 1000L, // Default value since not stored in entity
        baseStats = stats,
        currentStats = stats, // Use same stats for now
        skills = skillList,
        traits = traitList,
        isWild = false,
        captureRate = 100,
        growthRate = GrowthRate.MEDIUM_FAST
    )
}