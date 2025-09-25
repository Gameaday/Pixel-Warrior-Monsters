package com.pixelwarrior.monsters.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pixelwarrior.monsters.data.model.*
import com.pixelwarrior.monsters.data.repository.GameRepository
import com.pixelwarrior.monsters.game.battle.BattleEngine
import com.pixelwarrior.monsters.game.breeding.BreedingSystem
import com.pixelwarrior.monsters.game.world.WorldExplorer
import com.pixelwarrior.monsters.utils.GameUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

/**
 * ViewModel for managing game state across all screens
 */
class GameViewModel : ViewModel() {
    
    private val gameRepository = GameRepository()
    private val battleEngine = BattleEngine()
    private val breedingSystem = BreedingSystem()
    private val worldExplorer = WorldExplorer()
    
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
                val newSave = gameRepository.createNewGame(playerName)
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
                val save = gameRepository.loadGame(saveId)
                if (save != null) {
                    _gameSave.value = save
                    _gameMessage.value = "Game loaded successfully!"
                } else {
                    _gameMessage.value = "No saved game found"
                }
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
                val success = gameRepository.saveGame(save)
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
                    canCapture = true
                )
                _gameMessage.value = "Battle started against ${wildMonster.name}!"
            } else {
                _gameMessage.value = "No wild monsters encountered"
            }
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
                        BattlePhase.CAPTURE -> {
                            handleMonsterCapture(newState)
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
                gameRepository.saveGame(updatedSave)
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
                        gameRepository.saveGame(updatedSave)
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
            addDiscoveredSpecies(monster.species)
            
            viewModelScope.launch {
                gameRepository.saveGame(updatedSave)
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
                gameRepository.saveGame(updatedSave)
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
                gameRepository.saveGame(updatedSave)
            }
        }
    }
    
    /**
     * Handle monster capture
     */
    private fun handleMonsterCapture(battleState: BattleState) {
        val capturedMonster = battleState.enemyMonsters.first().copy(
            isWild = false,
            currentHp = (battleState.enemyMonsters.first().currentStats.maxHp * 0.8f).toInt()
        )
        
        addMonsterToFarm(capturedMonster)
        _gameMessage.value = "${capturedMonster.name} was captured!"
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
    
    override fun onCleared() {
        super.onCleared()
        // Save game when ViewModel is destroyed
        _gameSave.value?.let { save ->
            viewModelScope.launch {
                gameRepository.saveGame(save)
            }
        }
    }
}