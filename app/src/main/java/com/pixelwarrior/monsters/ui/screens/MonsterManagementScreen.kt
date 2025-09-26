package com.pixelwarrior.monsters.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.pixelwarrior.monsters.utils.getDisplayName
import com.pixelwarrior.monsters.utils.getHpPercentage
import com.pixelwarrior.monsters.utils.getMpPercentage
import com.pixelwarrior.monsters.utils.isFainted

/**
 * Monster management screen for viewing and organizing the player's monsters
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonsterManagementScreen(
    partyMonsters: List<Monster>,
    farmMonsters: List<Monster>,
    onMonsterSelected: (Monster) -> Unit,
    onPartyChanged: (List<Monster>) -> Unit,
    onBackPressed: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    var selectedMonster by remember { mutableStateOf<Monster?>(null) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PixelBlack)
    ) {
        // Header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
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
                Text(
                    text = "Monster Management",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                
                Button(
                    onClick = onBackPressed,
                    colors = ButtonDefaults.buttonColors(containerColor = PixelBlue)
                ) {
                    Text("Back", color = Color.White)
                }
            }
        }
        
        // Tab selector
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = PixelDarkGray,
            contentColor = Color.White,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Party (${partyMonsters.size}/4)") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Farm (${farmMonsters.size})") }
            )
        }
        
        when (selectedTab) {
            0 -> PartyMonstersList(
                monsters = partyMonsters,
                onMonsterSelected = { selectedMonster = it },
                modifier = Modifier.weight(1f)
            )
            1 -> FarmMonstersList(
                monsters = farmMonsters,
                onMonsterSelected = { selectedMonster = it },
                modifier = Modifier.weight(1f)
            )
        }
        
        // Monster details panel
        selectedMonster?.let { monster ->
            MonsterDetailsPanel(
                monster = monster,
                isInParty = partyMonsters.contains(monster),
                onDismiss = { selectedMonster = null },
                onPartyAction = { action ->
                    when (action) {
                        "add_to_party" -> {
                            if (partyMonsters.size < 4) {
                                onPartyChanged(partyMonsters + monster)
                            }
                        }
                        "remove_from_party" -> {
                            onPartyChanged(partyMonsters - monster)
                        }
                    }
                    selectedMonster = null
                }
            )
        }
    }
}

@Composable
fun PartyMonstersList(
    monsters: List<Monster>,
    onMonsterSelected: (Monster) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(monsters) { monster ->
            MonsterCard(
                monster = monster,
                onClick = { onMonsterSelected(monster) },
                showPartyIndicator = true
            )
        }
        
        // Empty slots
        val emptySlots = (4 - monsters.size).coerceAtLeast(0)
        items(emptySlots) {
            EmptyPartySlotCard()
        }
    }
}

@Composable
fun FarmMonstersList(
    monsters: List<Monster>,
    onMonsterSelected: (Monster) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (monsters.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = PixelDarkGray),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No monsters in farm.\nCatch some wild monsters!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = PixelLightGray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(monsters) { monster ->
                MonsterCard(
                    monster = monster,
                    onClick = { onMonsterSelected(monster) },
                    showPartyIndicator = false
                )
            }
        }
    }
}

@Composable
fun MonsterCard(
    monster: Monster,
    onClick: () -> Unit,
    showPartyIndicator: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = PixelDarkGray),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Monster sprite placeholder
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = getTypeColor(monster.type1),
                        shape = RoundedCornerShape(4.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = monster.name.first().toString(),
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = monster.getDisplayName(),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    
                    if (monster.isFainted()) {
                        Text(
                            text = "FAINTED",
                            style = MaterialTheme.typography.labelSmall,
                            color = HpRed,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Text(
                    text = "${monster.type1}${monster.type2?.let { " / $it" } ?: ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = PixelLightGray
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // HP bar
                ProgressBar(
                    label = "HP",
                    current = monster.currentHp,
                    max = monster.currentStats.maxHp,
                    color = HpRed,
                    percentage = monster.getHpPercentage()
                )
                
                // MP bar
                ProgressBar(
                    label = "MP",
                    current = monster.currentMp,
                    max = monster.currentStats.maxMp,
                    color = MpBlue,
                    percentage = monster.getMpPercentage()
                )
            }
            
            if (showPartyIndicator) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.Star,
                    contentDescription = "In Party",
                    tint = ExpYellow,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun EmptyPartySlotCard(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp),
        colors = CardDefaults.cardColors(containerColor = PixelDarkGray),
        shape = RoundedCornerShape(8.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Empty Slot",
                style = MaterialTheme.typography.bodyMedium,
                color = PixelLightGray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ProgressBar(
    label: String,
    current: Int,
    max: Int,
    color: Color,
    percentage: Float,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            modifier = Modifier.width(24.dp)
        )
        
        Box(
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .background(PixelBlack, RoundedCornerShape(3.dp))
                .border(1.dp, PixelGray, RoundedCornerShape(3.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(percentage)
                    .fillMaxHeight()
                    .background(color, RoundedCornerShape(3.dp))
            )
        }
        
        Text(
            text = "$current/$max",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            modifier = Modifier.width(48.dp),
            textAlign = TextAlign.End
        )
    }
}

@Composable
fun MonsterDetailsPanel(
    monster: Monster,
    isInParty: Boolean,
    onDismiss: () -> Unit,
    onPartyAction: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = PixelDarkGray),
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
                    text = monster.getDisplayName(),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = PixelBlue)
                ) {
                    Text("✕", color = Color.White)
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Monster stats
            Row {
                Column(modifier = Modifier.weight(1f)) {
                    StatDisplay("ATK", monster.currentStats.attack)
                    StatDisplay("DEF", monster.currentStats.defense)
                    StatDisplay("AGI", monster.currentStats.agility)
                }
                Column(modifier = Modifier.weight(1f)) {
                    StatDisplay("MAG", monster.currentStats.magic)
                    StatDisplay("WIS", monster.currentStats.wisdom)
                    StatDisplay("EXP", monster.experience.toString())
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Skills
            Text(
                text = "Skills:",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = monster.skills.joinToString(", ") { it.replaceFirstChar { c -> c.uppercase() } },
                style = MaterialTheme.typography.bodySmall,
                color = PixelLightGray
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Traits
            if (monster.traits.isNotEmpty()) {
                Text(
                    text = "Traits:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = monster.traits.joinToString(", "),
                    style = MaterialTheme.typography.bodySmall,
                    color = PixelLightGray
                )
                
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isInParty) {
                    Button(
                        onClick = { onPartyAction("remove_from_party") },
                        colors = ButtonDefaults.buttonColors(containerColor = HpRed),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Remove from Party", color = Color.White)
                    }
                } else {
                    Button(
                        onClick = { onPartyAction("add_to_party") },
                        colors = ButtonDefaults.buttonColors(containerColor = PixelGreen),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Add to Party", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun StatDisplay(
    label: String,
    value: Any,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodySmall,
            color = PixelLightGray
        )
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.bodySmall,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

fun getTypeColor(type: MonsterType): Color {
    return when (type) {
        MonsterType.FIRE -> Color(0xFFF08030)
        MonsterType.WATER -> Color(0xFF6890F0)
        MonsterType.GRASS -> Color(0xFF78C850)
        MonsterType.ELECTRIC -> Color(0xFFF8D030)
        MonsterType.ICE -> Color(0xFF98D8D8)
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
        MonsterType.NORMAL -> Color(0xFFA8A878)
    }
}