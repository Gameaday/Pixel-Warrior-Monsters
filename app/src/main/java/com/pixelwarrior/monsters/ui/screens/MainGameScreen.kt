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
                /* TODO: Implement load game */ 
            },
            onSettings = { 
                audioViewModel.playMenuSelectSound()
                currentScreen = GameScreen.AUDIO_SETTINGS
            },
            onCredits = { 
                audioViewModel.playMenuSelectSound()
                /* TODO: Implement credits */ 
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
            audioViewModel = audioViewModel
        )
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
                        }
                    },
                    onBattleEnd = {
                        when (state.battlePhase) {
                            BattlePhase.VICTORY -> audioViewModel.playVictoryMusic()
                            BattlePhase.DEFEAT -> audioViewModel.playGameOverMusic()
                            BattlePhase.CAPTURE -> audioViewModel.playMonsterCaptureSound()
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
        GameScreen.AUDIO_SETTINGS -> {
            AudioSettingsScreen(
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
    BATTLE,
    MONSTER_MANAGEMENT,
    BREEDING,
    AUDIO_SETTINGS
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
                        text = "Monsters",
                        onClick = onMonsterManagement,
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
                        text = "Explore",
                        onClick = { 
                            audioViewModel.playErrorSound()
                            /* TODO: Implement exploration */ 
                        },
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