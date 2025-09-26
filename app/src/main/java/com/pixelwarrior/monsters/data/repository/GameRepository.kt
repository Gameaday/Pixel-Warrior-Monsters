package com.pixelwarrior.monsters.data.repository

import com.pixelwarrior.monsters.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/**
 * Repository for managing game state and data persistence
 * In a real implementation, this would interface with local database and cloud storage
 */
class GameRepository {
    
    private val _currentGameSave = MutableStateFlow<GameSave?>(null)
    val currentGameSave: Flow<GameSave?> = _currentGameSave.asStateFlow()
    
    private val _monsterSpeciesDatabase = MutableStateFlow(generateDefaultSpecies())
    val monsterSpeciesDatabase: Flow<List<MonsterSpecies>> = _monsterSpeciesDatabase.asStateFlow()
    
    private val _skillsDatabase = MutableStateFlow(generateDefaultSkills())
    val skillsDatabase: Flow<List<Skill>> = _skillsDatabase.asStateFlow()
    
    /**
     * Create a new game save
     */
    suspend fun createNewGame(playerName: String): GameSave {
        val startingMonster = generateStartingMonster()
        
        val newSave = GameSave(
            playerId = UUID.randomUUID().toString(),
            playerName = playerName,
            currentLevel = "starting_area",
            position = Position(0f, 0f, Direction.DOWN),
            partyMonsters = listOf(startingMonster),
            farmMonsters = emptyList(),
            inventory = mapOf("basic_food" to 5, "healing_herb" to 3),
            gold = 100,
            playtimeMinutes = 0,
            storyProgress = mapOf("tutorial_complete" to false),
            unlockedGates = listOf("gate_1"),
            gameSettings = GameSettings(),
            saveVersion = 1,
            lastSaved = System.currentTimeMillis()
        )
        
        _currentGameSave.value = newSave
        return newSave
    }
    
    /**
     * Save the current game state to database
     */
    suspend fun saveGame(gameSave: GameSave): Boolean {
        return try {
            val updatedSave = gameSave.copy(lastSaved = System.currentTimeMillis())
            
            // Save to database
            val saveDao = database.gameSaveDao()
            val monsterDao = database.monsterDao()
            
            // Save game state
            saveDao.insertSave(updatedSave.toEntity())
            
            // Save all monsters
            val monsterEntities = updatedSave.allMonsters.map { it.toEntity(updatedSave.saveId) }
            monsterDao.insertMonsters(monsterEntities)
            
            // Update in-memory state
            _currentGameSave.value = updatedSave
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Load a saved game from database
     */
    suspend fun loadGame(saveId: String): GameSave? {
        return try {
            val saveDao = database.gameSaveDao()
            val monsterDao = database.monsterDao()
            
            val saveEntity = saveDao.getSaveById(saveId) ?: return null
            val monsterEntities = monsterDao.getMonstersForSave(saveId)
            val monsters = monsterEntities.map { it.toDomain() }
            
            val gameSave = saveEntity.toDomain(monsters)
            _currentGameSave.value = gameSave
            gameSave
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Get all saved games
     */
    suspend fun getAllSaves(): List<GameSave> {
        return try {
            val saveDao = database.gameSaveDao()
            val monsterDao = database.monsterDao()
            
            val saveEntities = saveDao.getAllSaves()
            saveEntities.map { saveEntity ->
                val monsters = monsterDao.getMonstersForSave(saveEntity.saveId).map { it.toDomain() }
                saveEntity.toDomain(monsters)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Delete a saved game
     */
    suspend fun deleteSave(saveId: String): Boolean {
        return try {
            val saveDao = database.gameSaveDao()
            val monsterDao = database.monsterDao()
            
            // Delete monsters first (foreign key relationship)
            monsterDao.deleteMonstersForSave(saveId)
            // Delete save
            saveDao.deleteSaveById(saveId)
            
            // Clear current save if it was the deleted one
            if (_currentGameSave.value?.saveId == saveId) {
                _currentGameSave.value = null
            }
            
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Create a new game save
     */
    suspend fun createNewSave(playerName: String): GameSave? {
        return try {
            val newSave = GameSave(
                saveId = UUID.randomUUID().toString(),
                playerName = playerName,
                currentLevel = 1,
                currentArea = "starting_area",
                playtimeMinutes = 0,
                goldAmount = 500,
                storyProgress = mapOf("game_started" to true),
                inventory = mapOf("herb" to 5, "monster_treat" to 3),
                partyMonsters = emptyList(),
                allMonsters = emptyList(),
                lastSaved = System.currentTimeMillis(),
                gameVersion = "1.0"
            )
            
            if (saveGame(newSave)) {
                newSave
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Get all skills from database
     */
    suspend fun getAllSkills(): List<SkillEntity> {
        return database.skillDao().getAllSkills()
    }
    
    /**
     * Get skill by ID from database
     */
    suspend fun getSkillById(skillId: String): SkillEntity? {
        return database.skillDao().getSkillById(skillId)
    }
    
    /**
     * Update player's party monsters
     */
    suspend fun updatePartyMonsters(monsters: List<Monster>) {
        _currentGameSave.value?.let { save ->
            val updatedSave = save.copy(partyMonsters = monsters)
            _currentGameSave.value = updatedSave
            saveGame(updatedSave)
        }
    }
    
    /**
     * Create default skills for the database
     */
    private fun createDefaultSkills(): List<SkillEntity> {
        return listOf(
            SkillEntity("tackle", "Tackle", "A physical ramming attack", "PHYSICAL", "NORMAL", 40, 100, 0, "SINGLE", "Basic physical attack"),
            SkillEntity("heal", "Heal", "Restores HP to target", "HEALING", "NORMAL", 40, 100, 6, "SINGLE", "Heals 30-50 HP"),
            SkillEntity("fireball", "Fireball", "Launches a ball of fire", "MAGICAL", "FIRE", 60, 90, 8, "SINGLE", "Fire-based magic attack"),
            SkillEntity("ice_shard", "Ice Shard", "Fires sharp ice crystals", "MAGICAL", "ICE", 55, 95, 7, "SINGLE", "Ice-based magic attack"),
            SkillEntity("thunder", "Thunder", "Strikes with lightning", "MAGICAL", "ELECTRIC", 65, 85, 9, "SINGLE", "Electric-based magic attack"),
            SkillEntity("gust", "Gust", "Creates a powerful wind", "MAGICAL", "AIR", 45, 95, 5, "SINGLE", "Wind-based magic attack"),
            SkillEntity("bite", "Bite", "Bites the target with fangs", "PHYSICAL", "NORMAL", 50, 95, 3, "SINGLE", "Physical bite attack"),
            SkillEntity("guard", "Guard", "Raises defense for next turn", "SUPPORT", "NORMAL", 0, 100, 4, "SELF", "Increases defense by 50%"),
            SkillEntity("sleep", "Sleep", "Puts target to sleep", "STATUS", "NORMAL", 0, 75, 6, "SINGLE", "Inflicts sleep status"),
            SkillEntity("poison_sting", "Poison Sting", "Stings with poison", "PHYSICAL", "POISON", 35, 90, 4, "SINGLE", "May inflict poison")
        )
    }
    
    /**
     * Add monster to farm
     */
    suspend fun addToFarm(monster: Monster) {
        _currentGameSave.value?.let { save ->
            val updatedFarm = save.farmMonsters + monster
            _currentGameSave.value = save.copy(farmMonsters = updatedFarm)
        }
    }
    
    /**
     * Get monster species by ID
     */
    suspend fun getMonsterSpecies(speciesId: String): MonsterSpecies? {
        return _monsterSpeciesDatabase.value.find { it.id == speciesId }
    }
    
    /**
     * Get skill by ID
     */
    suspend fun getSkill(skillId: String): Skill? {
        return _skillsDatabase.value.find { it.id == skillId }
    }
    
    /**
     * Generate a starting monster for new players
     */
    private fun generateStartingMonster(): Monster {
        val species = _monsterSpeciesDatabase.value.first { it.id == "starter_slime" }
        
        return Monster(
            id = UUID.randomUUID().toString(),
            speciesId = species.id,
            name = "Buddy",
            type1 = species.type1,
            type2 = species.type2,
            family = species.family,
            level = 5,
            currentHp = species.baseStats.maxHp,
            currentMp = species.baseStats.maxMp,
            experience = 0,
            experienceToNext = 150,
            baseStats = species.baseStats,
            currentStats = species.baseStats,
            skills = listOf("tackle", "heal"),
            traits = listOf("Friendly"),
            isWild = false,
            captureRate = 100,
            growthRate = species.growthRate
        )
    }
    
    /**
     * Generate default monster species for the game
     */
    private fun generateDefaultSpecies(): List<MonsterSpecies> {
        return listOf(
            MonsterSpecies(
                id = "starter_slime",
                name = "Gel Slime",
                type1 = MonsterType.NORMAL,
                type2 = null,
                family = MonsterFamily.SLIME,
                baseStats = MonsterStats(
                    attack = 30,
                    defense = 25,
                    agility = 35,
                    magic = 20,
                    wisdom = 30,
                    maxHp = 120,
                    maxMp = 40
                ),
                skillsLearnedByLevel = mapOf(
                    1 to listOf("tackle"),
                    3 to listOf("heal"),
                    7 to listOf("bounce"),
                    12 to listOf("regenerate")
                ),
                possibleTraits = listOf("Friendly", "Hardy", "Gentle"),
                captureRate = 200,
                growthRate = GrowthRate.MEDIUM_FAST,
                description = "A friendly slime that makes an excellent companion for new adventurers."
            ),
            MonsterSpecies(
                id = "fire_sprite",
                name = "Flame Sprite",
                type1 = MonsterType.FIRE,
                type2 = null,
                family = MonsterFamily.MATERIAL,
                baseStats = MonsterStats(
                    attack = 45,
                    defense = 20,
                    agility = 50,
                    magic = 60,
                    wisdom = 40,
                    maxHp = 90,
                    maxMp = 80
                ),
                skillsLearnedByLevel = mapOf(
                    1 to listOf("spark"),
                    5 to listOf("fireball"),
                    10 to listOf("flame_burst"),
                    15 to listOf("inferno")
                ),
                possibleTraits = listOf("Fierce", "Hot-headed", "Brave"),
                captureRate = 150,
                growthRate = GrowthRate.FAST,
                description = "A small sprite made of living flame. Quick and magical but fragile."
            ),
            MonsterSpecies(
                id = "forest_beast",
                name = "Moss Wolf",
                type1 = MonsterType.GRASS,
                type2 = MonsterType.NORMAL,
                family = MonsterFamily.BEAST,
                baseStats = MonsterStats(
                    attack = 55,
                    defense = 45,
                    agility = 40,
                    magic = 25,
                    wisdom = 35,
                    maxHp = 140,
                    maxMp = 50
                ),
                skillsLearnedByLevel = mapOf(
                    1 to listOf("bite"),
                    4 to listOf("vine_whip"),
                    8 to listOf("howl"),
                    14 to listOf("forest_charge")
                ),
                possibleTraits = listOf("Loyal", "Wild", "Swift"),
                captureRate = 120,
                growthRate = GrowthRate.MEDIUM_SLOW,
                description = "A wolf-like creature that has adapted to forest life, growing moss on its back."
            ),
            MonsterSpecies(
                id = "sky_bird",
                name = "Wind Falcon",
                type1 = MonsterType.FLYING,
                type2 = MonsterType.NORMAL,
                family = MonsterFamily.BIRD,
                baseStats = MonsterStats(
                    attack = 40,
                    defense = 30,
                    agility = 70,
                    magic = 35,
                    wisdom = 45,
                    maxHp = 100,
                    maxMp = 60
                ),
                skillsLearnedByLevel = mapOf(
                    1 to listOf("peck"),
                    6 to listOf("gust"),
                    11 to listOf("dive_bomb"),
                    16 to listOf("tornado")
                ),
                possibleTraits = listOf("Swift", "Keen", "Free"),
                captureRate = 100,
                growthRate = GrowthRate.FAST,
                description = "A majestic falcon that rides the wind currents with incredible speed."
            )
        )
    }
    
    /**
     * Generate default skills for the game
     */
    private fun generateDefaultSkills(): List<Skill> {
        return listOf(
            Skill(
                id = "tackle",
                name = "Tackle",
                description = "A basic physical attack",
                type = SkillType.PHYSICAL,
                target = SkillTarget.SINGLE_ENEMY,
                mpCost = 0,
                power = 40,
                accuracy = 100
            ),
            Skill(
                id = "heal",
                name = "Heal",
                description = "Restores HP to self",
                type = SkillType.HEALING,
                target = SkillTarget.SELF,
                mpCost = 6,
                power = 40,
                accuracy = 100
            ),
            Skill(
                id = "fireball",
                name = "Fireball",
                description = "Hurls a ball of fire at the enemy",
                type = SkillType.MAGICAL,
                target = SkillTarget.SINGLE_ENEMY,
                mpCost = 8,
                power = 60,
                accuracy = 90
            ),
            Skill(
                id = "gust",
                name = "Gust",
                description = "Creates a powerful wind attack",
                type = SkillType.MAGICAL,
                target = SkillTarget.SINGLE_ENEMY,
                mpCost = 5,
                power = 45,
                accuracy = 95
            ),
            Skill(
                id = "bite",
                name = "Bite",
                description = "Bites the enemy with sharp fangs",
                type = SkillType.PHYSICAL,
                target = SkillTarget.SINGLE_ENEMY,
                mpCost = 3,
                power = 50,
                accuracy = 95
            )
        )
    }
}