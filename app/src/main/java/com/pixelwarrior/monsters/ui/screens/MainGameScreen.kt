package com.pixelwarrior.monsters.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pixelwarrior.monsters.R
import com.pixelwarrior.monsters.audio.AudioViewModel
import com.pixelwarrior.monsters.audio.AudioViewModelFactory
import com.pixelwarrior.monsters.data.model.*
import com.pixelwarrior.monsters.game.synthesis.EnhancedMonster
import com.pixelwarrior.monsters.game.world.HubWorldSystem
import com.pixelwarrior.monsters.ui.theme.PixelBlack
import com.pixelwarrior.monsters.ui.theme.PixelBlue

/**
 * Main game screen that handles navigation between different game states
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainGameScreen() {
    var currentScreen by remember { mutableStateOf(GameScreen.MAIN_MENU) }
    var gameViewModel: GameViewModel = viewModel()
    
    val context = LocalContext.current
    val audioViewModel: AudioViewModel = viewModel(
        factory = AudioViewModelFactory(context)
    )
    
    // Play appropriate music based on current screen
    LaunchedEffect(currentScreen) {
        when (currentScreen) {
            GameScreen.MAIN_MENU -> audioViewModel.playTitleMusic()
            GameScreen.WORLD_MAP -> audioViewModel.playWorldMapMusic()
            GameScreen.BATTLE -> audioViewModel.playBattleMusic()
            GameScreen.BREEDING -> audioViewModel.playBreedingMusic()
            else -> { /* Keep current music */ }
        }
    }
    
    when (currentScreen) {
        GameScreen.MAIN_MENU -> MainMenuScreen(
            onStartGame = { 
                audioViewModel.playMenuSelectSound()
                gameViewModel.startNewGame("Player")
                currentScreen = GameScreen.WORLD_MAP 
            },
            onLoadGame = { 
                audioViewModel.playMenuSelectSound()
                currentScreen = GameScreen.LOAD_GAME
            },
            onSettings = { 
                audioViewModel.playMenuSelectSound()
                currentScreen = GameScreen.AUDIO_SETTINGS
            },
            onCredits = { 
                audioViewModel.playMenuSelectSound()
                currentScreen = GameScreen.CREDITS
            }
        )
        GameScreen.WORLD_MAP -> WorldMapScreen(
            onBackToMenu = { 
                audioViewModel.playMenuBackSound()
                currentScreen = GameScreen.MAIN_MENU 
            },
            onBattle = { 
                audioViewModel.playMenuSelectSound()
                gameViewModel.startWildBattle()
                currentScreen = GameScreen.BATTLE 
            },
            onMonsterManagement = { 
                audioViewModel.playMenuSelectSound()
                currentScreen = GameScreen.MONSTER_MANAGEMENT 
            },
            onBreeding = { 
                audioViewModel.playMenuSelectSound()
                currentScreen = GameScreen.BREEDING 
            },
            onSaveGame = {
                audioViewModel.playMenuSelectSound()
                currentScreen = GameScreen.SAVE_GAME
            },
            onSettings = {
                audioViewModel.playMenuSelectSound()
                currentScreen = GameScreen.GAME_SETTINGS
            },
            onCodex = {
                audioViewModel.playMenuSelectSound()
                currentScreen = GameScreen.MONSTER_CODEX
            },
            onDungeonExploration = {
                audioViewModel.playMenuSelectSound()
                currentScreen = GameScreen.DUNGEON_EXPLORATION
            },
            onHubWorld = {
                audioViewModel.playMenuSelectSound()
                currentScreen = GameScreen.HUB_WORLD
            },
            audioViewModel = audioViewModel
        )
        
        GameScreen.HUB_WORLD -> {
            val currentSave by gameViewModel.gameSave.collectAsState()
            currentSave?.let { save ->
                HubWorldScreen(
                    gameSave = save,
                    hubWorldSystem = gameViewModel.getHubWorldSystem(),
                    onAreaSelected = { area ->
                        audioViewModel.playMenuSelectSound()
                        // Navigate to appropriate screen based on area
                        currentScreen = when (area) {
                            HubWorldSystem.HubArea.BATTLE_ARENA -> GameScreen.ARENA
                            HubWorldSystem.HubArea.BREEDING_LAB -> GameScreen.BREEDING
                            HubWorldSystem.HubArea.MONSTER_LIBRARY -> GameScreen.MONSTER_CODEX
                            HubWorldSystem.HubArea.SYNTHESIS_LAB -> GameScreen.SYNTHESIS_LAB
                            HubWorldSystem.HubArea.GATE_CHAMBER -> GameScreen.DUNGEON_EXPLORATION
                            else -> GameScreen.WORLD_MAP
                        }
                    },
                    onNPCInteract = { npc ->
                        audioViewModel.playMenuSelectSound()
                        gameViewModel.interactWithNPC(npc)
                    },
                    onBackPressed = {
                        audioViewModel.playMenuBackSound()
                        currentScreen = GameScreen.WORLD_MAP
                    }
                )
            }
        }
        
        GameScreen.BATTLE -> {
            val battleState by gameViewModel.battleState.collectAsState()
            battleState?.let { state ->
                BattleScreen(
                    battleState = state,
                    onBattleAction = { action ->
                        gameViewModel.executeBattleAction(action)
                        // Play appropriate sound effect
                        when (action.action) {
                            BattleAction.ATTACK -> audioViewModel.playBattleHitSound()
                            BattleAction.SKILL -> audioViewModel.playSkillUseSound()
                            BattleAction.DEFEND -> audioViewModel.playMenuSelectSound()
                            BattleAction.RUN -> audioViewModel.playMenuBackSound()
                            BattleAction.TREAT -> audioViewModel.playMenuSelectSound() // Use menu sound for treats
                        }
                    },
                    onBattleEnd = {
                        when (state.battlePhase) {
                            BattlePhase.VICTORY -> audioViewModel.playVictoryMusic()
                            BattlePhase.DEFEAT -> audioViewModel.playGameOverMusic()
                            BattlePhase.MONSTER_JOINED -> audioViewModel.playMonsterCaptureSound() // Reuse the same sound for joining
                            else -> { /* No special music */ }
                        }
                        gameViewModel.endBattle()
                        currentScreen = GameScreen.WORLD_MAP
                    },
                    audioViewModel = audioViewModel
                )
            }
        }
        GameScreen.MONSTER_MANAGEMENT -> {
            val gameSave by gameViewModel.gameSave.collectAsState()
            gameSave?.let { save ->
                MonsterManagementScreen(
                    partyMonsters = save.partyMonsters,
                    farmMonsters = save.farmMonsters,
                    onMonsterSelected = { 
                        audioViewModel.playMenuSelectSound()
                        /* TODO: Handle monster selection */ 
                    },
                    onPartyChanged = { newParty ->
                        audioViewModel.playMenuSelectSound()
                        gameViewModel.updateParty(newParty)
                    },
                    onBackPressed = { 
                        audioViewModel.playMenuBackSound()
                        currentScreen = GameScreen.WORLD_MAP 
                    }
                )
            }
        }
        GameScreen.BREEDING -> {
            val gameSave by gameViewModel.gameSave.collectAsState()
            gameSave?.let { save ->
                val availableMonsters = save.partyMonsters + save.farmMonsters
                BreedingScreen(
                    availableMonsters = availableMonsters,
                    onBreed = { parent1, parent2 ->
                        audioViewModel.playBreedingSuccessSound()
                        gameViewModel.breedMonsters(parent1, parent2)
                    },
                    onBackPressed = { 
                        audioViewModel.playMenuBackSound()
                        currentScreen = GameScreen.WORLD_MAP 
                    }
                )
            }
        }
        GameScreen.COOKING -> {
            val gameSave by gameViewModel.gameSave.collectAsState()
            gameSave?.let { save ->
                CookingScreen(
                    playerInventory = save.inventory,
                    cookingSkill = save.cookingSkill,
                    onCook = { recipe ->
                        // This would normally trigger cooking in the game system
                        gameViewModel.addGameMessage("Cooking ${recipe.name}...")
                    },
                    onBack = {
                        audioViewModel.playMenuBackSound()
                        currentScreen = GameScreen.WORLD_MAP
                    }
                )
            }
        }
        GameScreen.MONSTER_DETAIL -> {
            val gameSave by gameViewModel.gameSave.collectAsState()
            gameSave?.let { save ->
                if (save.partyMonsters.isNotEmpty()) {
                    MonsterDetailScreen(
                        monster = save.partyMonsters.first(), // Show first party monster as example
                        onBack = {
                            audioViewModel.playMenuBackSound()
                            currentScreen = GameScreen.MONSTER_MANAGEMENT
                        },
                        onRename = { newName ->
                            gameViewModel.addGameMessage("${save.partyMonsters.first().name} renamed to $newName!")
                        },
                        onHeal = {
                            gameViewModel.addGameMessage("${save.partyMonsters.first().name} was healed!")
                        },
                        onGiveTreat = { treatType ->
                            gameViewModel.addGameMessage("Gave ${treatType.replace("_", " ")} to ${save.partyMonsters.first().name}!")
                        }
                    )
                } else {
                    // Fallback if no monsters
                    currentScreen = GameScreen.MONSTER_MANAGEMENT
                }
            }
        }
        GameScreen.SYNTHESIS_LAB -> {
            val gameSave by gameViewModel.gameSave.collectAsState()
            gameSave?.let { save ->
                SynthesisLabScreen(
                    availableMonsters = save.farmMonsters.map { EnhancedMonster(it) },
                    availableItems = save.inventory.keys.toList(),
                    onSynthesizeMonsters = { parent1, parent2 ->
                        gameViewModel.addGameMessage("Synthesizing ${parent1.baseMonster.name} and ${parent2.baseMonster.name}...")
                    },
                    onEnhanceMonster = { monster ->
                        gameViewModel.addGameMessage("Enhancing ${monster.baseMonster.name}...")
                    },
                    onStartScoutMission = { monster, missionType ->
                        gameViewModel.addGameMessage("${monster.baseMonster.name} started a ${missionType.name.lowercase().replace("_", " ")} mission!")
                    },
                    onBack = {
                        audioViewModel.playMenuBackSound()
                        currentScreen = GameScreen.WORLD_MAP
                    }
                )
            }
        }
        GameScreen.ARENA -> {
            val gameSave by gameViewModel.gameSave.collectAsState()
            gameSave?.let { save ->
                ArenaScreen(
                    tournamentSystem = gameViewModel.getTournamentSystem(),
                    playerName = save.playerName,
                    playerGold = save.gold.toInt(),
                    playtime = save.playtimeMinutes,
                    playerParty = save.partyMonsters,
                    onBattleRival = { rival ->
                        gameViewModel.addGameMessage("Challenging ${rival.name} to battle!")
                    },
                    onBack = {
                        audioViewModel.playMenuBackSound()
                        currentScreen = GameScreen.WORLD_MAP
                    }
                )
            }
        }
        GameScreen.AUDIO_SETTINGS -> {
            AudioSettingsScreen(
                onBackPressed = { 
                    audioViewModel.playMenuBackSound()
                    currentScreen = GameScreen.MAIN_MENU 
                }
            )
        }
        GameScreen.GAME_SETTINGS -> {
            val gameSave by gameViewModel.gameSave.collectAsState()
            gameSave?.let { save ->
                GameSettingsScreen(
                    gameSettings = save.gameSettings,
                    onSettingsChanged = { newSettings ->
                        gameViewModel.updateGameSettings(newSettings)
                    },
                    onBackPressed = { 
                        audioViewModel.playMenuBackSound()
                        currentScreen = GameScreen.MAIN_MENU 
                    }
                )
            }
        }
        GameScreen.MONSTER_CODEX -> {
            val gameSave by gameViewModel.gameSave.collectAsState()
            gameSave?.let { save ->
                MonsterCodexScreen(
                    discoveredMonsters = emptyList(), // Stub - would normally track discovered species
                    allSpecies = emptyList(), // Stub - would normally get from repository
                    onBackPressed = { 
                        audioViewModel.playMenuBackSound()
                        currentScreen = GameScreen.WORLD_MAP 
                    }
                )
            }
        }
        GameScreen.SAVE_GAME -> {
            SaveLoadScreen(
                mode = SaveLoadMode.SAVE,
                onSaveSelected = { saveId ->
                    gameViewModel.saveGame()
                    audioViewModel.playCoinCollectSound()
                    currentScreen = GameScreen.WORLD_MAP
                },
                onLoadSelected = { /* Not used in save mode */ },
                onBackPressed = {
                    audioViewModel.playMenuBackSound()
                    currentScreen = GameScreen.WORLD_MAP
                }
            )
        }
        GameScreen.LOAD_GAME -> {
            SaveLoadScreen(
                mode = SaveLoadMode.LOAD,
                onSaveSelected = { /* Not used in load mode */ },
                onLoadSelected = { saveId ->
                    gameViewModel.loadGame(saveId)
                    audioViewModel.playCoinCollectSound()
                    currentScreen = GameScreen.WORLD_MAP
                },
                onBackPressed = {
                    audioViewModel.playMenuBackSound()
                    currentScreen = GameScreen.MAIN_MENU
                }
            )
        }
        GameScreen.DUNGEON_EXPLORATION -> {
            DungeonExplorationScreen(
                gameViewModel = gameViewModel,
                onNavigateBack = {
                    audioViewModel.playMenuBackSound()
                    currentScreen = GameScreen.WORLD_MAP
                },
                onBattleStart = { wildMonster ->
                    gameViewModel.startBattleWithWildMonster(wildMonster)
                    currentScreen = GameScreen.BATTLE
                }
            )
        }
        GameScreen.ENDGAME_CONTENT -> {
            val gameSave by gameViewModel.gameSave.collectAsState()
            gameSave?.let { save ->
                EndgameContentScreen(
                    playerLevel = save.partyMonsters.firstOrNull()?.level ?: 1,
                    completedMainStory = save.storyProgress["main_story_complete"] == true,
                    defeatedChampion = save.storyProgress["champion_defeated"] == true,
                    achievements = save.storyProgress.filterValues { it == true }.keys.toList(),
                    statistics = emptyMap(), // Stub - no statistics field in current GameSave
                    currentPlaythrough = 1, // Stub - no playthrough tracking in current GameSave
                    onNavigateToPostGameDungeon = { dungeon ->
                        audioViewModel.playMenuSelectSound()
                        // Navigate to specific post-game dungeon
                        currentScreen = GameScreen.DUNGEON_EXPLORATION
                    },
                    onNavigateToAdditionalWorld = { world ->
                        audioViewModel.playMenuSelectSound()
                        // Navigate to additional world
                        currentScreen = GameScreen.WORLD_MAP
                    },
                    onStartNewGamePlus = {
                        audioViewModel.playMenuSelectSound()
                        gameViewModel.startNewGamePlus()
                        currentScreen = GameScreen.WORLD_MAP
                    },
                    onNavigateBack = { 
                        audioViewModel.playMenuBackSound()
                        currentScreen = GameScreen.WORLD_MAP
                    }
                )
            }
        }
        GameScreen.QUALITY_OF_LIFE -> {
            QualityOfLifeScreen(
                qolSystem = gameViewModel.getQoLSystem(),
                onBackPress = { 
                    audioViewModel.playMenuBackSound()
                    currentScreen = GameScreen.WORLD_MAP
                }
            )
        }
        GameScreen.EXPLORATION_HUB -> {
            ExplorationHubScreen(
                explorationSystem = gameViewModel.getExplorationSystem(),
                onBack = { 
                    audioViewModel.playMenuBackSound()
                    currentScreen = GameScreen.WORLD_MAP
                }
            )
        }
        GameScreen.CREDITS -> {
            CreditsScreen(
                onBackPressed = { 
                    audioViewModel.playMenuBackSound()
                    currentScreen = GameScreen.MAIN_MENU 
                }
            )
        }
    }
}

/**
 * Different screens in the game
 */
enum class GameScreen {
    MAIN_MENU,
    WORLD_MAP,
    HUB_WORLD,
    BATTLE,
    MONSTER_MANAGEMENT,
    MONSTER_DETAIL,
    BREEDING,
    COOKING,
    SYNTHESIS_LAB,
    ARENA,
    AUDIO_SETTINGS,
    GAME_SETTINGS,
    MONSTER_CODEX,
    CREDITS,
    SAVE_GAME,
    LOAD_GAME,
    DUNGEON_EXPLORATION,
    ENDGAME_CONTENT,
    QUALITY_OF_LIFE,
    EXPLORATION_HUB
}

/**
 * Main menu with pixel art styling
 */
@Composable
fun MainMenuScreen(
    onStartGame: () -> Unit,
    onLoadGame: () -> Unit,
    onSettings: () -> Unit,
    onCredits: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PixelBlack),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Game title
            Text(
                text = stringResource(R.string.game_title),
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Menu buttons
            PixelButton(
                text = stringResource(R.string.start_game),
                onClick = onStartGame
            )
            
            PixelButton(
                text = stringResource(R.string.load_game),
                onClick = onLoadGame
            )
            
            PixelButton(
                text = "Audio Settings",
                onClick = onSettings
            )
            
            PixelButton(
                text = "Game Settings", 
                onClick = onSettings // Use the same callback as other settings
            )
            
            PixelButton(
                text = stringResource(R.string.credits),
                onClick = onCredits
            )
        }
    }
}

/**
 * Styled button component for pixel art aesthetic
 */
@Composable
fun PixelButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier.width(200.dp),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = PixelBlue,
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Enhanced world map screen with navigation options
 */
@Composable
fun WorldMapScreen(
    onBackToMenu: () -> Unit,
    onBattle: () -> Unit,
    onMonsterManagement: () -> Unit,
    onBreeding: () -> Unit,
    onSaveGame: () -> Unit,
    onSettings: () -> Unit,
    onCodex: () -> Unit,
    onDungeonExploration: () -> Unit,
    onHubWorld: () -> Unit,
    audioViewModel: AudioViewModel
) {
    val gameViewModel: GameViewModel = viewModel()
    val gameSave by gameViewModel.gameSave.collectAsState()
    val gameMessage by gameViewModel.gameMessage.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PixelBlack)
    ) {
        // Game info header
        gameSave?.let { save ->
            com.pixelwarrior.monsters.ui.components.GameInfoPanel(
                playerName = save.playerName,
                gold = save.gold,
                playtime = save.playtimeMinutes,
                partySize = save.partyMonsters.size,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )
        }
        
        // Main content
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Adventure Hub",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
                
                Text(
                    text = "Choose your next adventure!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
                
                // Adventure options
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PixelButton(
                        text = "Wild Battle",
                        onClick = onBattle,
                        modifier = Modifier.weight(1f)
                    )
                    PixelButton(
                        text = "Dungeons",
                        onClick = onDungeonExploration,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PixelButton(
                        text = "Monsters",
                        onClick = onMonsterManagement,
                        modifier = Modifier.weight(1f)
                    )
                    PixelButton(
                        text = "Hub World",
                        onClick = onHubWorld,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PixelButton(
                        text = "Breeding",
                        onClick = onBreeding,
                        modifier = Modifier.weight(1f)
                    )
                    PixelButton(
                        text = "Codex",
                        onClick = onCodex,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PixelButton(
                        text = "Save Game",
                        onClick = onSaveGame,
                        modifier = Modifier.weight(1f)
                    )
                    PixelButton(
                        text = "Settings",
                        onClick = onSettings,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                PixelButton(
                    text = "Main Menu",
                    onClick = onBackToMenu
                )
            }
        }
    }
    
    // Show game messages
    com.pixelwarrior.monsters.ui.components.GameMessageDialog(
        message = gameMessage,
        onDismiss = { /* Messages auto-dismiss */ }
    )
}

