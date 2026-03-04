package com.pixelwarrior.monsters.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pixelwarrior.monsters.data.model.*
import com.pixelwarrior.monsters.data.repository.GameRepository
import com.pixelwarrior.monsters.data.database.GameDatabase
import com.pixelwarrior.monsters.game.battle.BattleEngine
import com.pixelwarrior.monsters.game.breeding.BreedingSystem
import com.pixelwarrior.monsters.game.tournament.TournamentSystem
import com.pixelwarrior.monsters.game.world.WorldExplorer
import com.pixelwarrior.monsters.game.world.HubWorldSystem
import com.pixelwarrior.monsters.game.story.StorySystem
import com.pixelwarrior.monsters.utils.GameUtils
import com.pixelwarrior.monsters.utils.canLevelUp // Extension function import
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

/**
 * ViewModel for managing game state across all screens  
 */
class GameViewModel : ViewModel() {
    
    // For development/stub purposes - normally these would be dependency injected
    private val battleEngine = BattleEngine()
    private val breedingSystem = BreedingSystem()
    private val worldExplorer = WorldExplorer()
    
    // These will be initialized lazily when needed  
    private var storySystem: StorySystem? = null
    private var hubWorldSystem: HubWorldSystem? = null
    
    // Game state flows
    private val _gameSave = MutableStateFlow<GameSave?>(null)
    val gameSave: StateFlow<GameSave?> = _gameSave.asStateFlow()
    
    private val _battleState = MutableStateFlow<BattleState?>(null)
    val battleState: StateFlow<BattleState?> = _battleState.asStateFlow()
    
    private val _gameMessage = MutableStateFlow<String?>(null)
    val gameMessage: StateFlow<String?> = _gameMessage.asStateFlow()
    
    /**
     * Start a new game with the given player name
     */
    fun startNewGame(playerName: String) {
        viewModelScope.launch {
            try {
                val starterMonster = generateStarterMonster()
                val newSave = GameSave(
                    playerId = "player_${System.currentTimeMillis()}",
                    playerName = playerName,
                    currentLevel = "starting_meadow",
                    position = Position(0f, 0f),
                    partyMonsters = listOf(starterMonster),
                    farmMonsters = emptyList(),
                    inventory = mapOf(
                        "basic_treat" to 5,
                        "healing_herb" to 3,
                        "monster_food" to 3
                    ),
                    gold = 200L,
                    playtimeMinutes = 0L,
                    storyProgress = mapOf("game_started" to true),
                    unlockedGates = emptyList(),
                    gameSettings = GameSettings(),
                    cookingSkill = CookingSkill(),
                    saveVersion = 1,
                    lastSaved = System.currentTimeMillis()
                )
                _gameSave.value = newSave
                _gameMessage.value = "Welcome, $playerName! ${starterMonster.name} joins your party!"
                clearMessageAfterDelay()
            } catch (e: Exception) {
                _gameMessage.value = "Failed to start new game: ${e.message}"
                clearMessageAfterDelay()
            }
        }
    }
    
    /**
     * Generate a starter monster for new players
     */
    private fun generateStarterMonster(): Monster {
        val personalities = Personality.values().filter { it != Personality.NONE }
        val baseStats = MonsterStats(
            attack = 30, defense = 25, agility = 35,
            magic = 20, wisdom = 30, maxHp = 120, maxMp = 40
        )
        return Monster(
            id = java.util.UUID.randomUUID().toString(),
            speciesId = "starter_slime",
            name = "Buddy",
            type1 = MonsterType.NORMAL,
            type2 = null,
            family = MonsterFamily.SLIME,
            level = 5,
            currentHp = baseStats.maxHp,
            currentMp = baseStats.maxMp,
            experience = 0,
            experienceToNext = 150,
            baseStats = baseStats,
            currentStats = GameUtils.calculateStatsForLevel(baseStats, 5),
            skills = listOf("tackle", "heal"),
            traits = listOf("Friendly"),
            isWild = false,
            captureRate = 100,
            growthRate = GrowthRate.MEDIUM_FAST,
            personality = personalities.random()
        )
    }
    
    /**
     * Load an existing game save
     */
    fun loadGame(saveId: String = "default") {
        viewModelScope.launch {
            try {
                // Stub implementation - would normally load from database
                _gameMessage.value = "Load game not implemented in stub version"
                clearMessageAfterDelay()
            } catch (e: Exception) {
                _gameMessage.value = "Error loading game: ${e.message}"
                clearMessageAfterDelay()
            }
        }
    }
    
    /**
     * Load the default/most recent game save
     */
    fun loadGame() {
        loadGame("default")
    }
    
    /**
     * Save the current game
     */
    fun saveGame() {
        viewModelScope.launch {
            _gameSave.value?.let { save ->
                // Stub implementation - would normally save to database  
                val success = true // gameRepository.saveGame(save)
                _gameMessage.value = if (success) "Game saved!" else "Failed to save game"
                clearMessageAfterDelay()
            }
        }
    }
    
    /**
     * Start a battle with a wild monster
     */
    fun startWildBattle() {
        _gameSave.value?.let { save ->
            if (save.partyMonsters.isEmpty() || save.partyMonsters.all { it.currentHp <= 0 }) {
                _gameMessage.value = "No healthy monsters available for battle!"
                clearMessageAfterDelay()
                return
            }
            
            // Generate a wild encounter
            val wildMonster = worldExplorer.attemptRandomEncounter(
                save.currentLevel,
                save.partyMonsters.maxOfOrNull { it.level } ?: 1
            )
            
            if (wildMonster != null) {
                startBattleWithWildMonster(wildMonster)
            } else {
                _gameMessage.value = "No wild monsters found in this area."
                clearMessageAfterDelay()
            }
        }
    }
    
    /**
     * Start a battle with a specific wild monster
     */
    fun startBattleWithWildMonster(wildMonster: Monster) {
        _gameSave.value?.let { save ->
            if (save.partyMonsters.isEmpty() || save.partyMonsters.all { it.currentHp <= 0 }) {
                _gameMessage.value = "No healthy monsters available for battle!"
                clearMessageAfterDelay()
                return
            }
            
            val healthyMonsters = save.partyMonsters.filter { it.currentHp > 0 }
            _battleState.value = BattleState(
                playerMonsters = healthyMonsters,
                enemyMonsters = listOf(wildMonster),
                currentPlayerMonster = 0,
                currentEnemyMonster = 0,
                turn = 1,
                battlePhase = BattlePhase.SELECTION,
                lastAction = "A wild ${wildMonster.name} appeared!",
                isWildBattle = true,
                canEscape = true,
                canTreat = true
            )
            _gameMessage.value = "Battle started against ${wildMonster.name}!"
            clearMessageAfterDelay()
        }
    }
    
    /**
     * Execute a battle action
     */
    fun executeBattleAction(action: BattleActionData) {
        _battleState.value?.let { currentState ->
            if (currentState.battlePhase != BattlePhase.SELECTION) return
            
            viewModelScope.launch {
                try {
                    // Generate AI action for enemy
                    val enemyMonster = currentState.enemyMonsters[currentState.currentEnemyMonster]
                    val playerMonster = currentState.playerMonsters[currentState.currentPlayerMonster]
                    val enemyAction = GameUtils.calculateAIAction(enemyMonster, playerMonster)
                    
                    // Execute battle turn
                    val newState = battleEngine.executeBattleTurn(currentState, action, enemyAction)
                    _battleState.value = newState
                    
                    // Handle battle end conditions
                    when (newState.battlePhase) {
                        BattlePhase.VICTORY -> {
                            handleBattleVictory(newState)
                        }
                        BattlePhase.DEFEAT -> {
                            handleBattleDefeat(newState)
                        }
                        BattlePhase.MONSTER_JOINED -> {
                            handleMonsterJoining(newState)
                        }
                        else -> { /* Battle continues */ }
                    }
                } catch (e: Exception) {
                    _gameMessage.value = "Battle error: ${e.message}"
                    clearMessageAfterDelay()
                }
            }
        }
    }
    
    /**
     * End the current battle
     */
    fun endBattle() {
        _battleState.value = null
    }
    
    /**
     * Update the player's party
     */
    fun updateParty(newParty: List<Monster>) {
        _gameSave.value?.let { save ->
            val updatedSave = save.copy(partyMonsters = newParty)
            _gameSave.value = updatedSave
            
            viewModelScope.launch {
                // gameRepository.saveGame(updatedSave)
            }
        }
    }
    
    /**
     * Breed two monsters
     */
    fun breedMonsters(parent1: Monster, parent2: Monster) {
        viewModelScope.launch {
            try {
                val offspring = breedingSystem.breedMonsters(parent1, parent2)
                if (offspring != null) {
                    _gameSave.value?.let { save ->
                        val updatedFarm = save.farmMonsters + offspring
                        val updatedSave = save.copy(farmMonsters = updatedFarm)
                        _gameSave.value = updatedSave
                        // gameRepository.saveGame(updatedSave)
                    }
                    _gameMessage.value = "Breeding successful! ${offspring.name} was born!"
                } else {
                    _gameMessage.value = "Breeding failed. Parents are not compatible."
                }
                clearMessageAfterDelay()
            } catch (e: Exception) {
                _gameMessage.value = "Breeding error: ${e.message}"
                clearMessageAfterDelay()
            }
        }
    }
    
    /**
     * Add monster to farm (usually from captures)
     */
    fun addMonsterToFarm(monster: Monster) {
        _gameSave.value?.let { save ->
            val updatedFarm = save.farmMonsters + monster
            val updatedSave = save.copy(farmMonsters = updatedFarm)
            _gameSave.value = updatedSave
            
            // Add species to discovered list
            addDiscoveredSpecies(monster.speciesId)
            
            viewModelScope.launch {
                // gameRepository.saveGame(updatedSave)
            }
        }
    }
    
    /**
     * Level up monsters that have gained enough experience
     */
    private fun checkForLevelUps(monsters: List<Monster>): List<Monster> {
        return monsters.map { monster ->
            if (monster.canLevelUp()) {
                val leveledUp = GameUtils.levelUpMonster(monster)
                _gameMessage.value = "${monster.name} leveled up to ${leveledUp.level}!"
                leveledUp
            } else {
                monster
            }
        }
    }
    
    /**
     * Handle battle victory
     */
    private fun handleBattleVictory(battleState: BattleState) {
        _gameSave.value?.let { save ->
            // Award experience to participating monsters
            val updatedParty = save.partyMonsters.map { monster ->
                if (battleState.playerMonsters.contains(monster)) {
                    val expGain = 50 + (battleState.enemyMonsters.first().level * 10)
                    monster.copy(experience = monster.experience + expGain)
                } else {
                    monster
                }
            }
            
            // Check for level ups
            val leveledUpParty = checkForLevelUps(updatedParty)
            
            val updatedSave = save.copy(
                partyMonsters = leveledUpParty,
                gold = save.gold + (25 + battleState.enemyMonsters.first().level * 5)
            )
            _gameSave.value = updatedSave
            
            viewModelScope.launch {
                // gameRepository.saveGame(updatedSave)
            }
        }
    }
    
    /**
     * Handle battle defeat
     */
    private fun handleBattleDefeat(battleState: BattleState) {
        _gameSave.value?.let { save ->
            // Heal all monsters to 1 HP and reduce gold
            val healedParty = save.partyMonsters.map { monster ->
                monster.copy(currentHp = 1)
            }
            
            val updatedSave = save.copy(
                partyMonsters = healedParty,
                gold = (save.gold * 0.9f).toLong() // Lose 10% gold
            )
            _gameSave.value = updatedSave
            
            viewModelScope.launch {
                // gameRepository.saveGame(updatedSave)
            }
        }
    }
    
    /**
     * Handle monster joining the party after battle
     */
    private fun handleMonsterJoining(battleState: BattleState) {
        val joiningMonster = battleState.enemyMonsters.first().copy(
            isWild = false,
            currentHp = (battleState.enemyMonsters.first().currentStats.maxHp * 0.9f).toInt(),
            affection = (battleState.enemyMonsters.first().affection + 20).coerceAtMost(100) // Higher base affection for joining monsters
        )
        
        addMonsterToFarm(joiningMonster)
        _gameMessage.value = "${joiningMonster.name} wants to join your party!"
        clearMessageAfterDelay()
    }
    
    /**
     * Clear game message after a delay
     */
    private fun clearMessageAfterDelay() {
        viewModelScope.launch {
            delay(3000)
            _gameMessage.value = null
        }
    }
    
    /**
     * Add a game message for display
     */
    fun addGameMessage(message: String) {
        _gameMessage.value = message
        clearMessageAfterDelay()
    }
    
    /**
     * Update game settings
     */
    fun updateGameSettings(newSettings: GameSettings) {
        _gameSave.value?.let { save ->
            val updatedSave = save.copy(gameSettings = newSettings)
            _gameSave.value = updatedSave
            // In a real app, this would persist to the repository
        }
    }
    
    /**
     * Get tournament system instance (stub)
     */
    fun getTournamentSystem(): TournamentSystem {
        return TournamentSystem() // Create new instance each time for simplicity
    }
    
    // Hub World System Functions
    
    /**
     * Get the hub world system instance
     */
    fun getHubWorldSystem(): HubWorldSystem {
        if (hubWorldSystem == null) {
            // Initialize lazily with stub StorySystem
            hubWorldSystem = HubWorldSystem(storySystem ?: StorySystem())
        }
        return hubWorldSystem!!
    }
    
    /**
     * Award a key item to the player
     */
    fun awardKeyItem(keyItem: HubWorldSystem.KeyItem) {
        viewModelScope.launch {
            _gameSave.value?.let { save ->
                val updatedSave = getHubWorldSystem().awardKeyItem(keyItem, save)
                _gameSave.value = updatedSave
                _gameMessage.value = "Received ${keyItem.displayName}!"
                clearMessageAfterDelay()
                
                // Auto-save
                // gameRepository.saveGame(updatedSave)
            }
        }
    }
    
    /**
     * Complete a story milestone
     */
    fun completeStoryMilestone(milestone: HubWorldSystem.StoryMilestone) {
        viewModelScope.launch {
            _gameSave.value?.let { save ->
                val updatedSave = getHubWorldSystem().completeStoryMilestone(milestone, save)
                _gameSave.value = updatedSave
                _gameMessage.value = "Achievement unlocked: ${milestone.displayName}"
                clearMessageAfterDelay()
                
                // Auto-save
                // gameRepository.saveGame(updatedSave)
            }
        }
    }
    
    /**
     * Handle NPC interaction for story progression
     */
    fun interactWithNPC(npc: HubWorldSystem.HubNPC) {
        viewModelScope.launch {
            _gameMessage.value = "${npc.displayName}: ${npc.dialogue.random()}"
            clearMessageAfterDelay()
            
            // Check for quest completion or item awards based on NPC
            when (npc) {
                HubWorldSystem.HubNPC.MASTER -> {
                    // Master might award first key items
                    _gameSave.value?.let { save ->
                        if (!save.inventory.containsKey("starter_guidance")) {
                            val updatedInventory = save.inventory.toMutableMap()
                            updatedInventory["starter_guidance"] = 1
                            _gameSave.value = save.copy(inventory = updatedInventory)
                        }
                    }
                }
                HubWorldSystem.HubNPC.STABLE_KEEPER -> {
                    // Stable keeper might award breeder license after first breeding
                    _gameSave.value?.let { save ->
                        if (save.storyProgress.containsKey("first_breeding") && 
                            !save.inventory.containsKey("breeder_license")) {
                            awardKeyItem(HubWorldSystem.KeyItem.BREEDER_LICENSE)
                        }
                    }
                }
                else -> { /* Other NPCs handled as needed */ }
            }
        }
    }
    
    /**
     * Add discovered species (stub implementation)
     */
    private fun addDiscoveredSpecies(speciesId: String) {
        // Stub implementation - would normally track discovered species
        // For now, just store in a simple list or use inventory/storyProgress
        _gameSave.value?.let { save ->
            val updatedProgress = save.storyProgress.toMutableMap()
            updatedProgress["discovered_$speciesId"] = true
            val updatedSave = save.copy(storyProgress = updatedProgress)
            _gameSave.value = updatedSave
        }
    }
    
    /**
     * Get all monster species for the codex
     */
    fun getAllSpecies(): List<MonsterSpecies> {
        return listOf(
            MonsterSpecies(
                id = "starter_slime", name = "Gel Slime",
                type1 = MonsterType.NORMAL, type2 = null, family = MonsterFamily.SLIME,
                baseStats = MonsterStats(30, 25, 35, 20, 30, 120, 40),
                captureRate = 200, growthRate = GrowthRate.MEDIUM_FAST,
                description = "A friendly slime that makes an excellent companion for new adventurers."
            ),
            MonsterSpecies(
                id = "fire_sprite", name = "Flame Sprite",
                type1 = MonsterType.FIRE, type2 = null, family = MonsterFamily.MATERIAL,
                baseStats = MonsterStats(45, 20, 50, 60, 40, 90, 80),
                captureRate = 150, growthRate = GrowthRate.FAST,
                description = "A small sprite made of living flame. Quick and magical but fragile."
            ),
            MonsterSpecies(
                id = "forest_beast", name = "Moss Wolf",
                type1 = MonsterType.GRASS, type2 = MonsterType.NORMAL, family = MonsterFamily.BEAST,
                baseStats = MonsterStats(55, 45, 40, 25, 35, 140, 50),
                captureRate = 120, growthRate = GrowthRate.MEDIUM_SLOW,
                description = "A wolf-like creature that has adapted to forest life, growing moss on its back."
            ),
            MonsterSpecies(
                id = "sky_bird", name = "Wind Falcon",
                type1 = MonsterType.FLYING, type2 = MonsterType.NORMAL, family = MonsterFamily.BIRD,
                baseStats = MonsterStats(40, 30, 70, 35, 45, 100, 60),
                captureRate = 100, growthRate = GrowthRate.FAST,
                description = "A majestic falcon that rides the wind currents with incredible speed."
            ),
            MonsterSpecies(
                id = "grass_bug", name = "Leaf Beetle",
                type1 = MonsterType.BUG, type2 = MonsterType.GRASS, family = MonsterFamily.PLANT,
                baseStats = MonsterStats(35, 40, 30, 15, 20, 100, 30),
                captureRate = 220, growthRate = GrowthRate.FAST,
                description = "A beetle with leaf-like wings found in meadows."
            ),
            MonsterSpecies(
                id = "tiny_bird", name = "Sparrow",
                type1 = MonsterType.FLYING, type2 = null, family = MonsterFamily.BIRD,
                baseStats = MonsterStats(25, 20, 45, 15, 25, 80, 30),
                captureRate = 240, growthRate = GrowthRate.FAST,
                description = "A common bird often seen in peaceful areas."
            ),
            MonsterSpecies(
                id = "tree_spirit", name = "Dryad Sprite",
                type1 = MonsterType.GRASS, type2 = MonsterType.PSYCHIC, family = MonsterFamily.PLANT,
                baseStats = MonsterStats(20, 30, 35, 55, 50, 90, 70),
                captureRate = 100, growthRate = GrowthRate.MEDIUM_SLOW,
                description = "A mystical spirit that inhabits ancient trees in enchanted forests."
            ),
            MonsterSpecies(
                id = "lava_worm", name = "Magma Worm",
                type1 = MonsterType.FIRE, type2 = MonsterType.GROUND, family = MonsterFamily.BEAST,
                baseStats = MonsterStats(50, 55, 20, 40, 30, 130, 40),
                captureRate = 90, growthRate = GrowthRate.SLOW,
                description = "A worm-like creature that thrives in volcanic caves."
            ),
            MonsterSpecies(
                id = "crystal_golem", name = "Crystal Golem",
                type1 = MonsterType.ROCK, type2 = MonsterType.ICE, family = MonsterFamily.MATERIAL,
                baseStats = MonsterStats(60, 70, 15, 30, 35, 160, 30),
                captureRate = 70, growthRate = GrowthRate.SLOW,
                description = "A massive golem formed from crystallized minerals deep underground."
            ),
            MonsterSpecies(
                id = "stone_guardian", name = "Stone Guardian",
                type1 = MonsterType.ROCK, type2 = MonsterType.FIGHTING, family = MonsterFamily.MATERIAL,
                baseStats = MonsterStats(65, 75, 25, 20, 40, 150, 35),
                captureRate = 60, growthRate = GrowthRate.SLOW,
                description = "An ancient guardian that protects the ruins from intruders."
            ),
            MonsterSpecies(
                id = "ghost_knight", name = "Phantom Knight",
                type1 = MonsterType.GHOST, type2 = MonsterType.DARK, family = MonsterFamily.UNDEAD,
                baseStats = MonsterStats(55, 50, 45, 50, 45, 110, 70),
                captureRate = 50, growthRate = GrowthRate.MEDIUM_SLOW,
                description = "The spirit of a fallen warrior, bound to the ancient ruins."
            ),
            MonsterSpecies(
                id = "ancient_dragon", name = "Ancient Dragon",
                type1 = MonsterType.DRAGON, type2 = MonsterType.FIRE, family = MonsterFamily.DRAGON,
                baseStats = MonsterStats(80, 70, 60, 75, 65, 180, 100),
                captureRate = 20, growthRate = GrowthRate.SLOW,
                description = "A legendary dragon of immense power that guards the deepest ruins."
            )
        )
    }
    
    /**
     * Get discovered species based on game progress
     */
    fun getDiscoveredSpecies(): List<MonsterSpecies> {
        val save = _gameSave.value ?: return emptyList()
        val discoveredIds = save.storyProgress.keys
            .filter { it.startsWith("discovered_") }
            .map { it.removePrefix("discovered_") }
            .toSet()
        
        // Always include starter slime + any encountered species
        val allIds = discoveredIds + save.partyMonsters.map { it.speciesId } + save.farmMonsters.map { it.speciesId }
        return getAllSpecies().filter { it.id in allIds }
    }
    fun getQoLSystem(): com.pixelwarrior.monsters.game.qol.QualityOfLifeSystem {
        // Stub implementation - return a basic QoL system
        return com.pixelwarrior.monsters.game.qol.QualityOfLifeSystem()
    }
    
    /**
     * Get Exploration system for UI
     */
    fun getExplorationSystem(): com.pixelwarrior.monsters.game.exploration.ExplorationSystem {
        // Stub implementation - return a basic exploration system
        return com.pixelwarrior.monsters.game.exploration.ExplorationSystem()
    }
    
    /**
     * Start new game plus with enhanced features
     */
    fun startNewGamePlus() {
        _gameSave.value?.let { currentSave ->
            val newGamePlusSave = GameSave(
                playerId = currentSave.playerId,
                playerName = currentSave.playerName,
                currentLevel = "tutorial_forest", // Start from beginning
                position = Position(0f, 0f), // Reset position
                partyMonsters = emptyList(), // Fresh start with monsters
                farmMonsters = emptyList(),
                inventory = currentSave.inventory.filterKeys { it.startsWith("key_") }, // Keep key items
                gold = currentSave.gold / 2, // Keep half gold
                playtimeMinutes = 0L, // Reset playtime
                storyProgress = mapOf("new_game_plus" to true),
                unlockedGates = emptyList(), // Reset progress
                gameSettings = currentSave.gameSettings,
                cookingSkill = currentSave.cookingSkill, // Keep cooking progress
                saveVersion = currentSave.saveVersion,
                lastSaved = System.currentTimeMillis()
            )
            _gameSave.value = newGamePlusSave
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        // Save game when ViewModel is destroyed
        _gameSave.value?.let { save ->
            viewModelScope.launch {
                // gameRepository.saveGame(save)
            }
        }
    }
}