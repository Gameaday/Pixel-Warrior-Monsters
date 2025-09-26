package com.pixelwarrior.monsters.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pixelwarrior.monsters.data.model.GameSave
import com.pixelwarrior.monsters.game.world.HubWorldSystem
import com.pixelwarrior.monsters.ui.theme.*

/**
 * Hub world screen showing the Master's Sanctuary with progressive unlocks
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HubWorldScreen(
    gameSave: GameSave,
    hubWorldSystem: HubWorldSystem,
    onAreaSelected: (HubWorldSystem.HubArea) -> Unit,
    onNPCInteract: (HubWorldSystem.HubNPC) -> Unit,
    onBackPressed: () -> Unit
) {
    var selectedArea by remember { mutableStateOf<HubWorldSystem.HubArea?>(null) }
    var showDialogue by remember { mutableStateOf(false) }
    var currentNPC by remember { mutableStateOf<HubWorldSystem.HubNPC?>(null) }
    
    val unlockedAreas = hubWorldSystem.getUnlockedAreas(gameSave)
    val availableNPCs = hubWorldSystem.getAvailableNPCs(gameSave)
    val nextObjective = hubWorldSystem.getNextObjective(gameSave)
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PixelDarkGreen)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = PixelLightGray),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🏰 Master's Sanctuary",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = PixelBlack
                    )
                    Button(
                        onClick = onBackPressed,
                        colors = ButtonDefaults.buttonColors(containerColor = PixelRed)
                    ) {
                        Text("Back", color = PixelWhite)
                    }
                }
                
                Text(
                    text = nextObjective,
                    fontSize = 14.sp,
                    color = PixelBlue,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
        
        // Hub Areas Grid
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = PixelLightGray),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Available Areas",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = PixelBlack
                )
                
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(HubWorldSystem.HubArea.values().toList()) { area ->
                        val isUnlocked = unlockedAreas.contains(area)
                        val areaIcon = getAreaIcon(area)
                        
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = isUnlocked) {
                                    if (isUnlocked) {
                                        selectedArea = area
                                        onAreaSelected(area)
                                    }
                                }
                                .padding(4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isUnlocked) PixelGreen else PixelGray
                            ),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = areaIcon,
                                    contentDescription = null,
                                    tint = if (isUnlocked) PixelWhite else PixelDarkGray,
                                    modifier = Modifier.size(24.dp)
                                )
                                
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = 12.dp)
                                ) {
                                    Text(
                                        text = area.displayName,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isUnlocked) PixelWhite else PixelDarkGray
                                    )
                                    Text(
                                        text = area.description,
                                        fontSize = 12.sp,
                                        color = if (isUnlocked) PixelLightGray else PixelDarkGray
                                    )
                                }
                                
                                if (!isUnlocked) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Locked",
                                        tint = PixelDarkGray,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // NPCs Section
        if (availableNPCs.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = PixelLightGray),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "NPCs Available",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = PixelBlack
                    )
                    
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(availableNPCs) { npc ->
                            Card(
                                modifier = Modifier
                                    .width(120.dp)
                                    .clickable {
                                        currentNPC = npc
                                        showDialogue = true
                                        onNPCInteract(npc)
                                    },
                                colors = CardDefaults.cardColors(containerColor = PixelBlue),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = PixelWhite,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Text(
                                        text = npc.name,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PixelWhite,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = npc.title,
                                        fontSize = 10.sp,
                                        color = PixelLightGray,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Progress Information
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = PixelYellow),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Progress Summary",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = PixelBlack
                )
                
                Text(
                    text = "Areas Unlocked: ${unlockedAreas.size}/${HubWorldSystem.HubArea.values().size}",
                    fontSize = 14.sp,
                    color = PixelBlack
                )
                
                Text(
                    text = "Key Items: ${gameSave.inventory.size}",
                    fontSize = 14.sp,
                    color = PixelBlack
                )
                
                Text(
                    text = "Story Progress: ${gameSave.storyProgress.size} milestones",
                    fontSize = 14.sp,
                    color = PixelBlack
                )
            }
        }
    }
    
    // NPC Dialogue Dialog
    if (showDialogue && currentNPC != null) {
        NPCDialogueDialog(
            npc = currentNPC!!,
            onDismiss = { 
                showDialogue = false
                currentNPC = null 
            }
        )
    }
}

/**
 * Get appropriate icon for each hub area
 */
@Composable
private fun getAreaIcon(area: HubWorldSystem.HubArea): ImageVector {
    return when (area) {
        HubWorldSystem.HubArea.MAIN_HALL -> Icons.Default.Home
        HubWorldSystem.HubArea.MONSTER_LIBRARY -> Icons.Default.MenuBook
        HubWorldSystem.HubArea.BREEDING_LAB -> Icons.Default.Science
        HubWorldSystem.HubArea.BATTLE_ARENA -> Icons.Default.Sports
        HubWorldSystem.HubArea.SYNTHESIS_LAB -> Icons.Default.Build
        HubWorldSystem.HubArea.ITEM_SHOP -> Icons.Default.Store
        HubWorldSystem.HubArea.GATE_CHAMBER -> Icons.Default.Flight
        HubWorldSystem.HubArea.MASTER_QUARTERS -> Icons.Default.VpnKey
        HubWorldSystem.HubArea.SECRET_VAULT -> Icons.Default.Lock
    }
}

/**
 * NPC Dialogue Dialog
 */
@Composable
private fun NPCDialogueDialog(
    npc: HubWorldSystem.HubNPC,
    onDismiss: () -> Unit
) {
    var currentDialogueIndex by remember { mutableStateOf(0) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = npc.name,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = npc.title,
                    fontSize = 14.sp,
                    color = PixelBlue
                )
            }
        },
        text = {
            Card(
                colors = CardDefaults.cardColors(containerColor = PixelLightGray)
            ) {
                Text(
                    text = npc.dialogue[currentDialogueIndex],
                    modifier = Modifier.padding(16.dp),
                    fontSize = 14.sp,
                    color = PixelBlack
                )
            }
        },
        confirmButton = {
            Row {
                if (currentDialogueIndex > 0) {
                    TextButton(onClick = { 
                        currentDialogueIndex-- 
                    }) {
                        Text("Previous", color = PixelBlue)
                    }
                }
                
                if (currentDialogueIndex < npc.dialogue.size - 1) {
                    TextButton(onClick = { 
                        currentDialogueIndex++ 
                    }) {
                        Text("Next", color = PixelBlue)
                    }
                } else {
                    TextButton(onClick = onDismiss) {
                        Text("Close", color = PixelRed)
                    }
                }
            }
        },
        containerColor = PixelWhite
    )
}