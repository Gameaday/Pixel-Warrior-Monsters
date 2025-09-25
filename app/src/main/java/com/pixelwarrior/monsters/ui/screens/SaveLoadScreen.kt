package com.pixelwarrior.monsters.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pixelwarrior.monsters.audio.AudioViewModel
import com.pixelwarrior.monsters.audio.AudioViewModelFactory
import com.pixelwarrior.monsters.data.model.*
import com.pixelwarrior.monsters.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Save/Load game screen for managing game saves
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaveLoadScreen(
    mode: SaveLoadMode,
    onSaveSelected: (String) -> Unit,
    onLoadSelected: (String) -> Unit,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val audioViewModel: AudioViewModel = viewModel(factory = AudioViewModelFactory(context))
    val gameViewModel: GameViewModel = viewModel()
    
    // Mock save data for demonstration
    val availableSaves = remember {
        listOf(
            SaveSlot(
                id = "save1",
                playerName = "Player",
                level = 5,
                playtime = 120,
                partySize = 3,
                lastSaved = System.currentTimeMillis() - 86400000 // 1 day ago
            ),
            SaveSlot(
                id = "save2", 
                playerName = "Hero",
                level = 12,
                playtime = 300,
                partySize = 4,
                lastSaved = System.currentTimeMillis() - 3600000 // 1 hour ago
            ),
            SaveSlot(
                id = "save3",
                playerName = "Warrior",
                level = 8,
                playtime = 180,
                partySize = 2,
                lastSaved = System.currentTimeMillis() - 172800000 // 2 days ago
            )
        )
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PixelBlack)
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (mode == SaveLoadMode.SAVE) "Save Game" else "Load Game",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )
            
            IconButton(onClick = onBackPressed) {
                Text("Back", color = Color.White)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Save slots
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(availableSaves) { saveSlot ->
                SaveSlotCard(
                    saveSlot = saveSlot,
                    mode = mode,
                    onSelect = {
                        audioViewModel.playMenuSelectSound()
                        when (mode) {
                            SaveLoadMode.SAVE -> onSaveSelected(saveSlot.id)
                            SaveLoadMode.LOAD -> onLoadSelected(saveSlot.id)
                        }
                    },
                    onDelete = {
                        audioViewModel.playMenuBackSound()
                        // TODO: Implement save deletion
                    }
                )
            }
            
            // Empty slot for new save
            if (mode == SaveLoadMode.SAVE) {
                item {
                    EmptySaveSlotCard(
                        onSelect = {
                            audioViewModel.playMenuSelectSound()
                            onSaveSelected("new_save_${System.currentTimeMillis()}")
                        }
                    )
                }
            }
        }
    }
}

/**
 * Card displaying a save slot
 */
@Composable
fun SaveSlotCard(
    saveSlot: SaveSlot,
    mode: SaveLoadMode,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = PixelGray),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = saveSlot.playerName,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Level ${saveSlot.level} • Party: ${saveSlot.partySize}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PixelBlue
                )
                Text(
                    text = "Playtime: ${saveSlot.playtime / 60}h ${saveSlot.playtime % 60}m",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Text(
                    text = "Saved: ${dateFormat.format(Date(saveSlot.lastSaved))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onSelect,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (mode == SaveLoadMode.LOAD) PixelGreen else PixelBlue
                    )
                ) {
                    Icon(
                        imageVector = if (mode == SaveLoadMode.LOAD) Icons.Default.Save else Icons.Default.Save,
                        contentDescription = if (mode == SaveLoadMode.LOAD) "Load" else "Save"
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (mode == SaveLoadMode.LOAD) "Load" else "Save")
                }
                
                if (mode == SaveLoadMode.LOAD) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = PixelRed
                        )
                    }
                }
            }
        }
    }
}

/**
 * Card for empty save slot
 */
@Composable
fun EmptySaveSlotCard(
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, PixelGray, RoundedCornerShape(8.dp))
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = onSelect,
                colors = ButtonDefaults.buttonColors(containerColor = PixelGreen)
            ) {
                Icon(imageVector = Icons.Default.Save, contentDescription = "New Save")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create New Save")
            }
        }
    }
}

/**
 * Data class for save slot information
 */
data class SaveSlot(
    val id: String,
    val playerName: String,
    val level: Int,
    val playtime: Int, // in minutes
    val partySize: Int,
    val lastSaved: Long
)

/**
 * Mode for save/load screen
 */
enum class SaveLoadMode {
    SAVE,
    LOAD
}