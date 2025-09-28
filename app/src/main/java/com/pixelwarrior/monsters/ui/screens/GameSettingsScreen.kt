package com.pixelwarrior.monsters.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Games
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pixelwarrior.monsters.audio.AudioViewModel
import com.pixelwarrior.monsters.audio.AudioViewModelFactory
import com.pixelwarrior.monsters.data.model.*
import com.pixelwarrior.monsters.ui.theme.*

/**
 * Comprehensive game settings screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameSettingsScreen(
    gameSettings: GameSettings,
    onSettingsChanged: (GameSettings) -> Unit,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val audioViewModel: AudioViewModel = viewModel(factory = AudioViewModelFactory(context))
    val scrollState = rememberScrollState()
    
    var currentSettings by remember { mutableStateOf(gameSettings) }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PixelBlack)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Header
        SettingsHeader(
            onBackPressed = {
                audioViewModel.playMenuBackSound()
                onSettingsChanged(currentSettings)
                onBackPressed()
            },
            onResetDefaults = {
                audioViewModel.playMenuSelectSound()
                currentSettings = GameSettings()
                onSettingsChanged(currentSettings)
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Audio Settings Section
        SettingsSection(
            title = "Audio Settings",
            icon = Icons.Default.Settings
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = PixelDarkGray),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    SettingsToggleRow(
                        label = "Background Music",
                        isEnabled = currentSettings.musicEnabled,
                        onToggle = { enabled ->
                            audioViewModel.playMenuSelectSound()
                            currentSettings = currentSettings.copy(musicEnabled = enabled)
                            audioViewModel.setMusicEnabled(enabled)
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    SettingsToggleRow(
                        label = "Sound Effects",
                        isEnabled = currentSettings.soundEffectsEnabled,
                        onToggle = { enabled ->
                            audioViewModel.playMenuSelectSound()
                            currentSettings = currentSettings.copy(soundEffectsEnabled = enabled)
                            audioViewModel.setSoundEnabled(enabled)
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    SettingsSlider(
                        label = "Music Volume",
                        value = currentSettings.musicVolume,
                        onValueChange = { volume ->
                            currentSettings = currentSettings.copy(musicVolume = volume)
                            audioViewModel.setMusicVolume(volume)
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    SettingsSlider(
                        label = "Sound Volume",
                        value = currentSettings.soundVolume,
                        onValueChange = { volume ->
                            currentSettings = currentSettings.copy(soundVolume = volume)
                            audioViewModel.setSoundVolume(volume)
                            audioViewModel.playMenuSelectSound()
                        }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Gameplay Settings Section
        SettingsSection(
            title = "Gameplay Settings",
            icon = Icons.Default.Games
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = PixelDarkGray),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    SettingsDropdown(
                        label = "Text Speed",
                        currentValue = currentSettings.textSpeed,
                        options = TextSpeed.values().toList(),
                        onValueSelected = { speed ->
                            audioViewModel.playMenuSelectSound()
                            currentSettings = currentSettings.copy(textSpeed = speed)
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    SettingsDropdown(
                        label = "Difficulty",
                        currentValue = currentSettings.difficulty,
                        options = Difficulty.values().toList(),
                        onValueSelected = { difficulty ->
                            audioViewModel.playMenuSelectSound()
                            currentSettings = currentSettings.copy(difficulty = difficulty)
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    SettingsToggleRow(
                        label = "Battle Animations",
                        isEnabled = currentSettings.battleAnimations,
                        onToggle = { enabled ->
                            audioViewModel.playMenuSelectSound()
                            currentSettings = currentSettings.copy(battleAnimations = enabled)
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    SettingsToggleRow(
                        label = "Auto-Save",
                        isEnabled = currentSettings.autoSave,
                        onToggle = { enabled ->
                            audioViewModel.playMenuSelectSound()
                            currentSettings = currentSettings.copy(autoSave = enabled)
                        }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Game Information Section
        SettingsSection(
            title = "Game Information",
            icon = Icons.Default.Palette
        ) {
            GameInfoCard()
        }
    }
}

@Composable
private fun SettingsHeader(
    onBackPressed: () -> Unit,
    onResetDefaults: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = PixelDarkGray),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = ExpYellow,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Game Settings",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onResetDefaults,
                    colors = ButtonDefaults.buttonColors(containerColor = PixelGreen),
                    contentPadding = PaddingValues(12.dp)
                ) {
                    Text("Reset", color = Color.White, style = MaterialTheme.typography.bodySmall)
                }
                
                Button(
                    onClick = onBackPressed,
                    colors = ButtonDefaults.buttonColors(containerColor = PixelBlue),
                    contentPadding = PaddingValues(12.dp)
                ) {
                    Text("Done", color = Color.White, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = ExpYellow,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
        
        content()
    }
}

@Composable
private fun SettingsToggleRow(
    label: String,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White
        )
        
        Switch(
            checked = isEnabled,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = ExpYellow,
                checkedTrackColor = PixelGreen,
                uncheckedThumbColor = PixelGray,
                uncheckedTrackColor = PixelDarkGray
            )
        )
    }
}

@Composable
private fun SettingsSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White
            )
            Text(
                text = "${(value * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = ExpYellow,
                fontWeight = FontWeight.Bold
            )
        }
        
        Slider(
            value = value,
            onValueChange = onValueChange,
            colors = SliderDefaults.colors(
                thumbColor = ExpYellow,
                activeTrackColor = PixelBlue,
                inactiveTrackColor = PixelGray
            )
        )
    }
}

@Composable
private fun <T> SettingsDropdown(
    label: String,
    currentValue: T,
    options: List<T>,
    onValueSelected: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = currentValue.toString().replaceFirstChar { it.uppercase() },
                onValueChange = { },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PixelBlue,
                    unfocusedBorderColor = PixelGray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )
            
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = option.toString().replaceFirstChar { it.uppercase() },
                                color = if (option == currentValue) ExpYellow else Color.White
                            )
                        },
                        onClick = {
                            onValueSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun GameInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = PixelDarkGray),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            InfoRow("Game Version", "1.0.0")
            InfoRow("Build", "Alpha")
            InfoRow("Engine", "Jetpack Compose")
            InfoRow("Audio", "ChiptuneEngine v1.0")
            InfoRow("Platform", "Android")
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Pixel Warrior Monsters is an open-source reimagining of classic monster collecting RPGs. All content is original and copyright-safe.",
                style = MaterialTheme.typography.bodySmall,
                color = PixelLightGray,
                lineHeight = MaterialTheme.typography.bodySmall.lineHeight
            )
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodySmall,
            color = PixelLightGray
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}