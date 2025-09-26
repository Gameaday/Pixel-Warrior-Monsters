package com.pixelwarrior.monsters.data.database

import androidx.room.*
import androidx.room.Database
import com.pixelwarrior.monsters.data.model.*

/**
 * Room database for persistent game data storage
 * Replaces TODO stubs in GameRepository with actual persistence
 */
@Database(
    entities = [
        GameSaveEntity::class,
        MonsterEntity::class,
        SkillEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(DatabaseConverters::class)
abstract class GameDatabase : RoomDatabase() {
    abstract fun gameSaveDao(): GameSaveDao
    abstract fun monsterDao(): MonsterDao
    abstract fun skillDao(): SkillDao
    
    companion object {
        const val DATABASE_NAME = "pixel_warrior_monsters_db"
    }
}

/**
 * Entity representing a saved game state
 */
@Entity(tableName = "game_saves")
data class GameSaveEntity(
    @PrimaryKey val saveId: String,
    val playerName: String,
    val currentLevel: Int,
    val currentArea: String,
    val playtimeMinutes: Int,
    val goldAmount: Int,
    val storyProgress: String, // JSON string of progress map
    val inventory: String, // JSON string of inventory map
    val partyMonsters: String, // JSON string of monster IDs
    val allMonsters: String, // JSON string of all owned monsters
    val lastSaved: Long,
    val gameVersion: String = "1.0",
    val previewImagePath: String? = null
)

/**
 * Entity for individual monsters in database
 */
@Entity(tableName = "monsters")
data class MonsterEntity(
    @PrimaryKey val monsterId: String,
    val saveId: String, // Foreign key to game save
    val speciesId: String,
    val name: String,
    val level: Int,
    val experience: Long,
    val currentHp: Int,
    val currentMp: Int,
    val type1: String,
    val type2: String?,
    val family: String,
    val personality: String,
    val baseStats: String, // JSON string of MonsterStats
    val skills: String, // JSON string of skill list
    val traits: String, // JSON string of trait list
    val plusLevel: Int = 0,
    val synthesisParents: String? = null, // JSON string of parent monster IDs
    val captureDate: Long = System.currentTimeMillis()
)

/**
 * Entity for skills database
 */
@Entity(tableName = "skills")
data class SkillEntity(
    @PrimaryKey val skillId: String,
    val name: String,
    val description: String,
    val skillType: String,
    val element: String,
    val power: Int,
    val accuracy: Int,
    val mpCost: Int,
    val targetType: String,
    val effectDescription: String,
    val learningRequirement: String? = null // Item or level requirement
)

/**
 * Data Access Object for game saves
 */
@Dao
interface GameSaveDao {
    @Query("SELECT * FROM game_saves ORDER BY lastSaved DESC")
    suspend fun getAllSaves(): List<GameSaveEntity>
    
    @Query("SELECT * FROM game_saves WHERE saveId = :saveId")
    suspend fun getSaveById(saveId: String): GameSaveEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSave(save: GameSaveEntity)
    
    @Update
    suspend fun updateSave(save: GameSaveEntity)
    
    @Delete
    suspend fun deleteSave(save: GameSaveEntity)
    
    @Query("DELETE FROM game_saves WHERE saveId = :saveId")
    suspend fun deleteSaveById(saveId: String)
}

/**
 * Data Access Object for monsters
 */
@Dao
interface MonsterDao {
    @Query("SELECT * FROM monsters WHERE saveId = :saveId")
    suspend fun getMonstersForSave(saveId: String): List<MonsterEntity>
    
    @Query("SELECT * FROM monsters WHERE monsterId = :monsterId")
    suspend fun getMonsterById(monsterId: String): MonsterEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMonster(monster: MonsterEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMonsters(monsters: List<MonsterEntity>)
    
    @Update
    suspend fun updateMonster(monster: MonsterEntity)
    
    @Delete
    suspend fun deleteMonster(monster: MonsterEntity)
    
    @Query("DELETE FROM monsters WHERE saveId = :saveId")
    suspend fun deleteMonstersForSave(saveId: String)
}

/**
 * Data Access Object for skills
 */
@Dao
interface SkillDao {
    @Query("SELECT * FROM skills")
    suspend fun getAllSkills(): List<SkillEntity>
    
    @Query("SELECT * FROM skills WHERE skillId = :skillId")
    suspend fun getSkillById(skillId: String): SkillEntity?
    
    @Query("SELECT * FROM skills WHERE element = :element")
    suspend fun getSkillsByElement(element: String): List<SkillEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSkill(skill: SkillEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSkills(skills: List<SkillEntity>)
    
    @Update
    suspend fun updateSkill(skill: SkillEntity)
    
    @Query("DELETE FROM skills")
    suspend fun deleteAllSkills()
}