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
                // Create a stub game save for development
                val newSave = GameSave(
                    playerId = "player_${System.currentTimeMillis()}",
                    playerName = playerName,
                    currentLevel = "tutorial",
                    position = Position(0f, 0f),
                    partyMonsters = emptyList(),
                    farmMonsters = emptyList(),
                    inventory = mapOf("basic_capture" to 10),
                    gold = 100L,
                    playtimeMinutes = 0L,
                    storyProgress = mapOf("tutorial" to true),
                    unlockedGates = emptyList(),
                    gameSettings = GameSettings(),
                    cookingSkill = CookingSkill(),
                    saveVersion = 1,
                    lastSaved = System.currentTimeMillis()
                )
                _gameSave.value = newSave
                _gameMessage.value = "Welcome to Pixel Warrior Monsters, $playerName!"
                clearMessageAfterDelay()
            } catch (e: Exception) {
                _gameMessage.value = "Failed to start new game: ${e.message}"
                clearMessageAfterDelay()
            }
        }
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