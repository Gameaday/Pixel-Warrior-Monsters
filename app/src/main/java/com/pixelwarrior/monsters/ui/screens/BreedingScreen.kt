package com.pixelwarrior.monsters.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pixelwarrior.monsters.data.model.*
import com.pixelwarrior.monsters.game.breeding.BreedingSystem
import com.pixelwarrior.monsters.ui.theme.*
import com.pixelwarrior.monsters.utils.getDisplayName

/**
 * Breeding screen for monster genetics and reproduction
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BreedingScreen(
    availableMonsters: List<Monster>,
    onBreed: (Monster, Monster) -> Unit,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedParent1 by remember { mutableStateOf<Monster?>(null) }
    var selectedParent2 by remember { mutableStateOf<Monster?>(null) }
    var showBreedingResult by remember { mutableStateOf(false) }
    var breedingOffspring by remember { mutableStateOf<Monster?>(null) }
    
    val breedingSystem = remember { BreedingSystem() }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PixelBlack)
    ) {
        // Header
        BreedingHeader(
            onBackPressed = onBackPressed
        )
        
        if (showBreedingResult && breedingOffspring != null) {
            BreedingResultScreen(
                offspring = breedingOffspring!!,
                onAccept = {
                    showBreedingResult = false
                    breedingOffspring = null
                    selectedParent1 = null
                    selectedParent2 = null
                },
                onReject = {
                    showBreedingResult = false
                    breedingOffspring = null
                }
            )
        } else {
            // Parent selection area
            ParentSelectionSection(
                parent1 = selectedParent1,
                parent2 = selectedParent2,
                onParent1Selected = { selectedParent1 = it },
                onParent2Selected = { selectedParent2 = it },
                breedingSystem = breedingSystem
            )
            
            // Breeding action button
            selectedParent1?.let { p1 ->
                selectedParent2?.let { p2 ->
                    if (breedingSystem.canBreed(p1, p2)) {
                        BreedingActionSection(
                            parent1 = p1,
                            parent2 = p2,
                            breedingSystem = breedingSystem,
                            onBreed = { offspring ->
                                breedingOffspring = offspring
                                showBreedingResult = true
                                onBreed(p1, p2)
                            }
                        )
                    }
                }
            }
            
            // Available monsters list
            AvailableMonstersSection(
                monsters = availableMonsters.filter { it.level >= 10 }, // Only level 10+ can breed
                selectedParent1 = selectedParent1,
                selectedParent2 = selectedParent2,
                onMonsterSelected = { monster ->
                    when {
                        selectedParent1 == null -> selectedParent1 = monster
                        selectedParent2 == null && monster != selectedParent1 -> selectedParent2 = monster
                        else -> {
                            selectedParent1 = monster
                            selectedParent2 = null
                        }
                    }
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun BreedingHeader(
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
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
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Breeding",
                    tint = ExpYellow,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Monster Breeding",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Button(
                onClick = onBackPressed,
                colors = ButtonDefaults.buttonColors(containerColor = PixelBlue)
            ) {
                Text("Back", color = Color.White)
            }
        }
    }
}

@Composable
fun ParentSelectionSection(
    parent1: Monster?,
    parent2: Monster?,
    onParent1Selected: (Monster?) -> Unit,
    onParent2Selected: (Monster?) -> Unit,
    breedingSystem: BreedingSystem,
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
            Text(
                text = "Select Parents",
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Parent 1 slot
                ParentSlot(
                    monster = parent1,
                    label = "Parent 1",
                    onClear = { onParent1Selected(null) },
                    modifier = Modifier.weight(1f)
                )
                
                // Breeding compatibility indicator
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.CenterVertically),
                    contentAlignment = Alignment.Center
                ) {
                    val isCompatible = parent1 != null && parent2 != null && 
                                     breedingSystem.canBreed(parent1, parent2)
                    
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                if (isCompatible) PixelGreen else PixelGray,
                                RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isCompatible) "♥" else "?",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
                
                // Parent 2 slot
                ParentSlot(
                    monster = parent2,
                    label = "Parent 2",
                    onClear = { onParent2Selected(null) },
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Compatibility message
            if (parent1 != null && parent2 != null) {
                val canBreed = breedingSystem.canBreed(parent1, parent2)
                Text(
                    text = if (canBreed) {
                        "Compatible! Success rate: ${(breedingSystem.calculateBreedingSuccessRate(parent1, parent2) * 100).toInt()}%"
                    } else {
                        "Incompatible - different families or requirements not met"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (canBreed) PixelGreen else HpRed,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun ParentSlot(
    monster: Monster?,
    label: String,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(120.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (monster != null) PixelBlue.copy(alpha = 0.2f) else PixelBlack
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            if (monster != null) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Monster representation
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(getTypeColor(monster.type1), RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = monster.name.first().toString(),
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = monster.getDisplayName(),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    
                    Text(
                        text = monster.family.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = PixelLightGray,
                        textAlign = TextAlign.Center
                    )
                    
                    Button(
                        onClick = onClear,
                        colors = ButtonDefaults.buttonColors(containerColor = HpRed),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text("Clear", color = Color.White, style = MaterialTheme.typography.labelSmall)
                    }
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .border(2.dp, PixelGray, RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "?",
                            style = MaterialTheme.typography.titleMedium,
                            color = PixelGray
                        )
                    }
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodySmall,
                        color = PixelGray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun BreedingActionSection(
    parent1: Monster,
    parent2: Monster,
    breedingSystem: BreedingSystem,
    onBreed: (Monster?) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = PixelGreen.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = "Breeding Time",
                    tint = ExpYellow,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Breeding time: ${breedingSystem.getBreedingTime(parent1, parent2)} minutes",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = {
                    val offspring = breedingSystem.breedMonsters(parent1, parent2)
                    onBreed(offspring)
                },
                colors = ButtonDefaults.buttonColors(containerColor = ExpYellow),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Start Breeding",
                    color = PixelBlack,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun AvailableMonstersSection(
    monsters: List<Monster>,
    selectedParent1: Monster?,
    selectedParent2: Monster?,
    onMonsterSelected: (Monster) -> Unit,
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
            Text(
                text = "Available for Breeding (Level 10+)",
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            if (monsters.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No monsters ready for breeding.\nTrain your monsters to level 10+",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PixelLightGray,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(monsters) { monster ->
                        BreedingMonsterItem(
                            monster = monster,
                            isSelected = monster == selectedParent1 || monster == selectedParent2,
                            onClick = { onMonsterSelected(monster) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BreedingMonsterItem(
    monster: Monster,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) PixelBlue.copy(alpha = 0.3f) else PixelBlack
        ),
        shape = RoundedCornerShape(6.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Monster icon
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(getTypeColor(monster.type1), RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = monster.name.first().toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = monster.getDisplayName(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${monster.family} • ${monster.type1}",
                    style = MaterialTheme.typography.bodySmall,
                    color = PixelLightGray
                )
            }
            
            if (isSelected) {
                Text(
                    text = "Selected",
                    style = MaterialTheme.typography.labelSmall,
                    color = ExpYellow,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun BreedingResultScreen(
    offspring: Monster,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = PixelDarkGray),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Breeding Successful!",
                style = MaterialTheme.typography.titleLarge,
                color = ExpYellow,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Offspring display
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(getTypeColor(offspring.type1), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = offspring.name.first().toString(),
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = offspring.name,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "${offspring.family} • ${offspring.type1}${offspring.type2?.let { " / $it" } ?: ""}",
                style = MaterialTheme.typography.bodyMedium,
                color = PixelLightGray
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Stats preview
            Text(
                text = "Starting Stats:",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatPreview("HP", offspring.baseStats.maxHp)
                StatPreview("ATK", offspring.baseStats.attack)
                StatPreview("DEF", offspring.baseStats.defense)
                StatPreview("AGI", offspring.baseStats.agility)
            }
            
            if (offspring.skills.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Inherited Skills: ${offspring.skills.joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = PixelLightGray,
                    textAlign = TextAlign.Center
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onReject,
                    colors = ButtonDefaults.buttonColors(containerColor = HpRed),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Release", color = Color.White)
                }
                Button(
                    onClick = onAccept,
                    colors = ButtonDefaults.buttonColors(containerColor = PixelGreen),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Keep", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun StatPreview(
    label: String,
    value: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = PixelLightGray
        )
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.bodyMedium,
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