package com.pixelwarrior.monsters.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pixelwarrior.monsters.data.model.*
import com.pixelwarrior.monsters.ui.theme.*
import com.pixelwarrior.monsters.utils.*

/**
 * Detailed monster view screen for examining individual monster stats, skills, and traits
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonsterDetailScreen(
    monster: Monster,
    onBack: () -> Unit,
    onRename: (String) -> Unit = {},
    onHeal: () -> Unit = {},
    onGiveTreat: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showRenameDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf(monster.name) }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PixelBlack)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header with monster basic info
        MonsterDetailHeader(
            monster = monster,
            onBack = onBack,
            onRename = { showRenameDialog = true }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Stats section
        MonsterStatsCard(monster = monster)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Skills section
        MonsterSkillsCard(monster = monster)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Traits section
        MonsterTraitsCard(monster = monster)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Actions section
        MonsterActionsCard(
            monster = monster,
            onHeal = onHeal,
            onGiveTreat = onGiveTreat
        )
    }
    
    // Rename dialog
    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename Monster", color = Color.White) },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("New Name") },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRename(newName)
                        showRenameDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PixelGreen)
                ) {
                    Text("Rename", color = Color.White)
                }
            },
            dismissButton = {
                Button(
                    onClick = { showRenameDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = PixelRed)
                ) {
                    Text("Cancel", color = Color.White)
                }
            },
            containerColor = PixelDarkGray
        )
    }
}

@Composable
private fun MonsterDetailHeader(
    monster: Monster,
    onBack: () -> Unit,
    onRename: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = PixelDarkBlue)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(containerColor = PixelBrown)
            ) {
                Text("Back", color = Color.White)
            }
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = monster.name,
                    style = MaterialTheme.typography.headlineSmall,
                    color = PixelYellow,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "Level ${monster.level}",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TypeChip(type = monster.type1)
                    monster.type2?.let { TypeChip(type = it) }
                }
            }
            
            Button(
                onClick = onRename,
                colors = ButtonDefaults.buttonColors(containerColor = PixelPurple)
            ) {
                Text("Rename", color = Color.White)
            }
        }
        
        // HP and MP bars
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            HPBar(
                currentHp = monster.currentHp,
                maxHp = monster.currentStats.maxHp,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            MPBar(
                currentMp = monster.currentMp,
                maxMp = monster.currentStats.maxMp,
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        // Affection meter
        if (monster.affection > 0) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Affection",
                    tint = PixelRed,
                    modifier = Modifier.size(16.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                LinearProgressIndicator(
                    progress = monster.affection / 100f,
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp),
                    color = PixelRed,
                    trackColor = PixelDarkGray
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "${monster.affection}/100",
                    color = Color.White,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun MonsterStatsCard(monster: Monster) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = PixelDarkGreen)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "📊 Stats",
                style = MaterialTheme.typography.titleLarge,
                color = PixelYellow,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            val stats = listOf(
                "Attack" to monster.currentStats.attack,
                "Defense" to monster.currentStats.defense,
                "Agility" to monster.currentStats.agility,
                "Magic" to monster.currentStats.magic,
                "Wisdom" to monster.currentStats.wisdom
            )
            
            stats.forEach { (statName, value) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = statName,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = value.toString(),
                        color = PixelLightBlue,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Experience progress
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "EXP:",
                    color = Color.White,
                    style = MaterialTheme.typography.bodySmall
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                LinearProgressIndicator(
                    progress = (monster.experience.toFloat() / monster.experienceToNext.toFloat()).coerceIn(0f, 1f),
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp),
                    color = PixelYellow,
                    trackColor = PixelDarkGray
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "${monster.experience}/${monster.experienceToNext}",
                    color = PixelYellow,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun MonsterSkillsCard(monster: Monster) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = PixelDarkBlue)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "⚔️ Skills",
                style = MaterialTheme.typography.titleLarge,
                color = PixelYellow,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (monster.skills.isEmpty()) {
                Text(
                    text = "No skills learned yet",
                    color = PixelLightGray,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                monster.skills.forEach { skill ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        colors = CardDefaults.cardColors(containerColor = PixelBlack.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = skill.replace("_", " ").lowercase()
                                .replaceFirstChar { it.uppercase() },
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MonsterTraitsCard(monster: Monster) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = PixelDarkGray)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "✨ Traits",
                style = MaterialTheme.typography.titleLarge,
                color = PixelYellow,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (monster.traits.isEmpty()) {
                Text(
                    text = "No special traits",
                    color = PixelLightGray,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                monster.traits.forEach { trait ->
                    Row(
                        modifier = Modifier.padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Trait",
                            tint = PixelGold,
                            modifier = Modifier.size(16.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = trait.replace("_", " ").lowercase()
                                .replaceFirstChar { it.uppercase() },
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MonsterActionsCard(
    monster: Monster,
    onHeal: () -> Unit,
    onGiveTreat: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = PixelDarkBlue)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "🎯 Actions",
                style = MaterialTheme.typography.titleLarge,
                color = PixelYellow,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onHeal,
                    enabled = monster.currentHp < monster.currentStats.maxHp,
                    colors = ButtonDefaults.buttonColors(containerColor = PixelGreen),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("💊 Heal", color = Color.White)
                }
                
                Button(
                    onClick = { onGiveTreat("basic_treat") },
                    colors = ButtonDefaults.buttonColors(containerColor = PixelYellow),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("🍖 Treat", color = Color.Black)
                }
            }
        }
    }
}

@Composable
private fun TypeChip(type: MonsterType) {
    val typeColor = when (type) {
        MonsterType.FIRE -> Color.Red
        MonsterType.WATER -> Color.Blue
        MonsterType.GRASS -> Color.Green
        MonsterType.ELECTRIC -> Color.Yellow
        MonsterType.ICE -> Color.Cyan
        MonsterType.FIGHTING -> Color(0xFFC03028)
        MonsterType.POISON -> Color(0xFFA040A0)
        MonsterType.GROUND -> Color(0xFFE0C068)
        MonsterType.FLYING -> Color(0xFFA890F0)
        MonsterType.PSYCHIC -> Color(0xFFF85888)
        MonsterType.BUG -> Color(0xFFA8B820)
        MonsterType.ROCK -> Color(0xFFB8A038)
        MonsterType.GHOST -> Color(0xFF705898)
        MonsterType.DRAGON -> Color(0xFF7038F8)
        MonsterType.DARK -> Color(0xFF705848)
        MonsterType.STEEL -> Color(0xFFB8B8D0)
        else -> PixelLightGray
    }
    
    Card(
        colors = CardDefaults.cardColors(containerColor = typeColor),
        modifier = Modifier.border(1.dp, Color.White, RoundedCornerShape(4.dp))
    ) {
        Text(
            text = type.name,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = Color.White,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun HPBar(
    currentHp: Int,
    maxHp: Int,
    modifier: Modifier = Modifier
) {
    val percentage = currentHp.toFloat() / maxHp.toFloat()
    val color = when {
        percentage > 0.6f -> PixelGreen
        percentage > 0.3f -> PixelYellow
        else -> PixelRed
    }
    
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "HP",
                color = Color.White,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "$currentHp/$maxHp",
                color = Color.White,
                style = MaterialTheme.typography.bodySmall
            )
        }
        
        LinearProgressIndicator(
            progress = percentage,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = color,
            trackColor = PixelDarkGray
        )
    }
}

@Composable
private fun MPBar(
    currentMp: Int,
    maxMp: Int,
    modifier: Modifier = Modifier
) {
    val percentage = currentMp.toFloat() / maxMp.toFloat()
    
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "MP",
                color = Color.White,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "$currentMp/$maxMp",
                color = Color.White,
                style = MaterialTheme.typography.bodySmall
            )
        }
        
        LinearProgressIndicator(
            progress = percentage,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = PixelBlue,
            trackColor = PixelDarkGray
        )
    }
}