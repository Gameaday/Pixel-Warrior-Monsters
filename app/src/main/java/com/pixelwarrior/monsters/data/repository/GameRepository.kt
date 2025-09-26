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
     * Save the current game state
     */
    suspend fun saveGame(gameSave: GameSave): Boolean {
        return try {
            val updatedSave = gameSave.copy(lastSaved = System.currentTimeMillis())
            _currentGameSave.value = updatedSave
            // TODO: Persist to local storage/database
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Load a saved game
     */
    suspend fun loadGame(saveId: String): GameSave? {
        // TODO: Load from local storage/database
        return _currentGameSave.value
    }
    
    /**
     * Update player's party
     */
    suspend fun updatePartyMonsters(monsters: List<Monster>) {
        _currentGameSave.value?.let { save ->
            _currentGameSave.value = save.copy(partyMonsters = monsters)
        }
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