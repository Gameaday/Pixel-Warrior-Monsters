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
            gson.fromJson(value, object : TypeToken<Map<String, String>>() {}.type) ?: emptyMap()
        } catch (e: Exception) {
            emptyMap()
        }
    }
    
    @TypeConverter
    fun fromIntMap(value: Map<String, Int>?): String {
        return gson.toJson(value)
    }
    
    @TypeConverter
    fun toIntMap(value: String): Map<String, Int> {
        return try {
            gson.fromJson(value, object : TypeToken<Map<String, Int>>() {}.type) ?: emptyMap()
        } catch (e: Exception) {
            emptyMap()
        }
    }
    
    @TypeConverter
    fun fromBooleanMap(value: Map<String, Boolean>?): String {
        return gson.toJson(value)
    }
    
    @TypeConverter
    fun toBooleanMap(value: String): Map<String, Boolean> {
        return try {
            gson.fromJson(value, object : TypeToken<Map<String, Boolean>>() {}.type) ?: emptyMap()
        } catch (e: Exception) {
            emptyMap()
        }
    }
    
    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return gson.toJson(value)
    }
    
    @TypeConverter
    fun toStringList(value: String): List<String> {
        return try {
            gson.fromJson(value, object : TypeToken<List<String>>() {}.type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
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
        saveId = saveId,
        playerName = playerName,
        currentLevel = currentLevel,
        currentArea = currentArea,
        playtimeMinutes = playtimeMinutes,
        goldAmount = goldAmount,
        storyProgress = Gson().toJson(storyProgress),
        inventory = Gson().toJson(inventory),
        partyMonsters = Gson().toJson(partyMonsters.map { it.id }),
        allMonsters = Gson().toJson(allMonsters.map { it.id }),
        lastSaved = lastSaved,
        gameVersion = gameVersion
    )
}

fun GameSaveEntity.toDomain(allMonsters: List<Monster>): GameSave {
    val gson = Gson()
    val partyIds: List<String> = try {
        gson.fromJson(partyMonsters, object : TypeToken<List<String>>() {}.type) ?: emptyList()
    } catch (e: Exception) { emptyList() }
    
    val allMonsterIds: List<String> = try {
        gson.fromJson(allMonsters, object : TypeToken<List<String>>() {}.type) ?: emptyList()
    } catch (e: Exception) { emptyList() }
    
    val partyMonsters = allMonsters.filter { it.id in partyIds }
    val ownedMonsters = allMonsters.filter { it.id in allMonsterIds }
    
    return GameSave(
        saveId = saveId,
        playerName = playerName,
        currentLevel = currentLevel,
        currentArea = currentArea,
        playtimeMinutes = playtimeMinutes,
        goldAmount = goldAmount,
        storyProgress = try {
            gson.fromJson(storyProgress, object : TypeToken<Map<String, Boolean>>() {}.type) ?: emptyMap()
        } catch (e: Exception) { emptyMap() },
        inventory = try {
            gson.fromJson(inventory, object : TypeToken<Map<String, Int>>() {}.type) ?: emptyMap()
        } catch (e: Exception) { emptyMap() },
        partyMonsters = partyMonsters,
        allMonsters = ownedMonsters,
        lastSaved = lastSaved,
        gameVersion = gameVersion
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
        personality = personality.name,
        baseStats = gson.toJson(baseStats),
        skills = gson.toJson(skills),
        traits = gson.toJson(traits),
        plusLevel = plusLevel,
        synthesisParents = synthesisParents?.let { gson.toJson(it) }
    )
}

/**
 * Simple data classes for the missing models
 */
data class Skill(
    val id: String,
    val name: String,
    val description: String,
    val power: Int,
    val accuracy: Int,
    val mpCost: Int,
    val element: String,
    val targetType: String
)

data class Personality(
    val name: String,
    val displayName: String,
    val growthModifiers: Map<String, Float>
) {
    companion object {
        fun valueOf(name: String): Personality {
            return Personality(name, name, emptyMap())
        }
    }
}
    val gson = Gson()
    val stats = try {
        gson.fromJson(baseStats, MonsterStats::class.java) ?: MonsterStats(0, 0, 0, 0, 0, 0, 0)
    } catch (e: Exception) { MonsterStats(0, 0, 0, 0, 0, 0, 0) }
    
    val skillList = try {
        gson.fromJson(skills, object : TypeToken<List<String>>() {}.type) ?: emptyList()
    } catch (e: Exception) { emptyList() }
    
    val traitList = try {
        gson.fromJson(traits, object : TypeToken<List<String>>() {}.type) ?: emptyList()
    } catch (e: Exception) { emptyList() }
    
    val parentsList = synthesisParents?.let { 
        try {
            gson.fromJson(it, object : TypeToken<List<String>>() {}.type)
        } catch (e: Exception) { null }
    }
    
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
        baseStats = stats,
        skills = skillList,
        traits = traitList,
        personality = Personality.valueOf(personality),
        plusLevel = plusLevel,
        synthesisParents = parentsList
    )
}