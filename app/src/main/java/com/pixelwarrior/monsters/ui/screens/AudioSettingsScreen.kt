package com.pixelwarrior.monsters.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
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
import com.pixelwarrior.monsters.ui.theme.*

/**
 * Audio settings screen for configuring music and sound effects
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioSettingsScreen(
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val audioViewModel: AudioViewModel = viewModel(
        factory = AudioViewModelFactory(context)
    )
    
    val isMusicEnabled by audioViewModel.isMusicEnabled.collectAsState()
    val isSoundEnabled by audioViewModel.isSoundEnabled.collectAsState()
    val musicVolume by audioViewModel.musicVolume.collectAsState()
    val soundVolume by audioViewModel.soundVolume.collectAsState()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PixelBlack)
            .padding(16.dp)
    ) {
        // Header
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
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = "Audio Settings",
                        tint = ExpYellow,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Audio Settings",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Button(
                    onClick = {
                        audioViewModel.playMenuBackSound()
                        onBackPressed()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PixelBlue)
                ) {
                    Text("Back", color = Color.White)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Music Settings
        AudioSettingSection(
            title = "Background Music",
            icon = Icons.Default.MusicNote
        ) {
            // Music Enable/Disable
            AudioToggleRow(
                label = "Enable Music",
                isEnabled = isMusicEnabled,
                onToggle = { enabled ->
                    audioViewModel.setMusicEnabled(enabled)
                    if (enabled) {
                        audioViewModel.playTitleMusic()
                    } else {
                        audioViewModel.stopMusic()
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Music Volume Slider
            AudioVolumeSlider(
                label = "Music Volume",
                volume = musicVolume,
                enabled = isMusicEnabled,
                onVolumeChange = { volume ->
                    audioViewModel.setMusicVolume(volume)
                }
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Sound Effects Settings
        AudioSettingSection(
            title = "Sound Effects",
            icon = Icons.Default.VolumeUp
        ) {
            // Sound Enable/Disable
            AudioToggleRow(
                label = "Enable Sound Effects",
                isEnabled = isSoundEnabled,
                onToggle = { enabled ->
                    audioViewModel.setSoundEnabled(enabled)
                    if (enabled) {
                        audioViewModel.playMenuSelectSound()
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Sound Volume Slider
            AudioVolumeSlider(
                label = "Sound Volume",
                volume = soundVolume,
                enabled = isSoundEnabled,
                onVolumeChange = { volume ->
                    audioViewModel.setSoundVolume(volume)
                    audioViewModel.playMenuSelectSound()
                }
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Audio Test Buttons
        AudioTestSection(audioViewModel = audioViewModel)
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Reset to Defaults
        Button(
            onClick = {
                audioViewModel.setMusicEnabled(true)
                audioViewModel.setSoundEnabled(true)
                audioViewModel.setMusicVolume(0.7f)
                audioViewModel.setSoundVolume(0.8f)
                audioViewModel.playMenuSelectSound()
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = PixelGreen)
        ) {
            Text(
                text = "Reset to Defaults",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun AudioSettingSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = PixelDarkGray),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
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
}

@Composable
private fun AudioToggleRow(
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
private fun AudioVolumeSlider(
    label: String,
    volume: Float,
    enabled: Boolean,
    onVolumeChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = if (enabled) Color.White else PixelGray
            )
            Text(
                text = "${(volume * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = if (enabled) ExpYellow else PixelGray,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Slider(
            value = volume,
            onValueChange = onVolumeChange,
            enabled = enabled,
            colors = SliderDefaults.colors(
                thumbColor = ExpYellow,
                activeTrackColor = PixelBlue,
                inactiveTrackColor = PixelGray,
                disabledThumbColor = PixelGray,
                disabledActiveTrackColor = PixelDarkGray
            )
        )
    }
}

@Composable
private fun AudioTestSection(audioViewModel: AudioViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = PixelDarkGray),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Test Audio",
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            // Music test buttons
            Text(
                text = "Music Tracks:",
                style = MaterialTheme.typography.bodySmall,
                color = PixelLightGray,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AudioTestButton(
                    text = "Title",
                    onClick = { audioViewModel.playTitleMusic() },
                    modifier = Modifier.weight(1f)
                )
                AudioTestButton(
                    text = "World",
                    onClick = { audioViewModel.playWorldMapMusic() },
                    modifier = Modifier.weight(1f)
                )
                AudioTestButton(
                    text = "Battle",
                    onClick = { audioViewModel.playBattleMusic() },
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Sound effect test buttons
            Text(
                text = "Sound Effects:",
                style = MaterialTheme.typography.bodySmall,
                color = PixelLightGray,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AudioTestButton(
                    text = "Hit",
                    onClick = { audioViewModel.playBattleHitSound() },
                    modifier = Modifier.weight(1f)
                )
                AudioTestButton(
                    text = "Heal",
                    onClick = { audioViewModel.playHealingSound() },
                    modifier = Modifier.weight(1f)
                )
                AudioTestButton(
                    text = "Level Up",
                    onClick = { audioViewModel.playLevelUpSound() },
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = { audioViewModel.stopMusic() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = HpRed)
            ) {
                Icon(
                    imageVector = Icons.Default.VolumeOff,
                    contentDescription = "Stop Music",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Stop Music", color = Color.White)
            }
        }
    }
}

@Composable
private fun AudioTestButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(containerColor = PixelBlue),
        contentPadding = PaddingValues(8.dp)
    ) {
        Text(
            text = text,
            color = Color.White,
            style = MaterialTheme.typography.bodySmall
        )
    }
}