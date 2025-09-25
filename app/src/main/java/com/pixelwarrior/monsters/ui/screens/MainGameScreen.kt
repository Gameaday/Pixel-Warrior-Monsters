package com.pixelwarrior.monsters.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pixelwarrior.monsters.R
import com.pixelwarrior.monsters.ui.theme.PixelBlack
import com.pixelwarrior.monsters.ui.theme.PixelBlue

/**
 * Main game screen that handles navigation between different game states
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainGameScreen() {
    var currentScreen by remember { mutableStateOf(GameScreen.MAIN_MENU) }
    
    when (currentScreen) {
        GameScreen.MAIN_MENU -> MainMenuScreen(
            onStartGame = { currentScreen = GameScreen.WORLD_MAP },
            onLoadGame = { /* TODO: Implement load game */ },
            onSettings = { /* TODO: Implement settings */ },
            onCredits = { /* TODO: Implement credits */ }
        )
        GameScreen.WORLD_MAP -> WorldMapScreen(
            onBackToMenu = { currentScreen = GameScreen.MAIN_MENU }
        )
        GameScreen.BATTLE -> {
            // TODO: Implement battle screen
        }
        GameScreen.MONSTER_MANAGEMENT -> {
            // TODO: Implement monster management screen
        }
        GameScreen.BREEDING -> {
            // TODO: Implement breeding screen
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
    BREEDING
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
                text = stringResource(R.string.settings),
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
 * Simple world map screen placeholder
 */
@Composable
fun WorldMapScreen(
    onBackToMenu: () -> Unit
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
            Text(
                text = "World Map",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )
            
            Text(
                text = "Coming soon! This is where you'll explore different areas and encounter monsters.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )
            
            PixelButton(
                text = "Back to Menu",
                onClick = onBackToMenu
            )
        }
    }
}