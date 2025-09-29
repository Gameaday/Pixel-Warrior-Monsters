package com.pixelwarrior.monsters.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pixelwarrior.monsters.data.model.*
import com.pixelwarrior.monsters.game.world.*
import com.pixelwarrior.monsters.ui.theme.*

/**
 * Screen for exploring dungeons with multiple floors and themed areas
 */
@Composable
fun DungeonExplorationScreen(
    gameViewModel: GameViewModel,
    onNavigateBack: () -> Unit,
    onBattleStart: (Monster) -> Unit
) {
    var selectedDungeon by remember { mutableStateOf<Dungeon?>(null) }
    var currentFloor by remember { mutableStateOf<DungeonFloor?>(null) }
    var showEventDialog by remember { mutableStateOf(false) }
    var eventResult by remember { mutableStateOf<String?>(null) }
    
    val dungeonSystem = remember { DungeonSystem() }
    val gameState by gameViewModel.gameSave.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PixelBlack)
            .padding(16.dp)
    ) {
        // Header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = PixelDarkGreen)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (selectedDungeon == null) "🏰 Dungeon Explorer 🏰" 
                          else "${selectedDungeon?.name} - Floor ${currentFloor?.floorNumber ?: 1}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                if (currentFloor != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = currentFloor!!.description,
                        fontSize = 14.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        
        if (selectedDungeon == null) {
            // Dungeon selection screen
            DungeonSelectionView(
                dungeons = dungeonSystem.getAvailableDungeons(gameState?.storyProgress ?: emptyMap()),
                onDungeonSelected = { dungeon ->
                    selectedDungeon = dungeon
                    currentFloor = dungeonSystem.enterDungeon(dungeon.id)
                }
            )
        } else {
            // Current floor exploration
            currentFloor?.let { floor ->
                DungeonFloorView(
                    floor = floor,
                    onExplore = { direction ->
                        // Handle exploration in direction
                        val encounter = attemptRandomEncounter(floor, gameState?.partyMonsters?.firstOrNull()?.level ?: 1)
                        encounter?.let { onBattleStart(it) }
                        
                        // Check for wandering events
                        val wanderingEvent = floor.wanderingEvents.randomOrNull()
                        if (wanderingEvent != null && kotlin.random.Random.nextFloat() < 0.1f) {
                            eventResult = handleWanderingEvent(wanderingEvent, gameViewModel)
                            showEventDialog = true
                        }
                    },
                    onProceedToNextFloor = {
                        selectedDungeon?.let { dungeon ->
                            val nextFloor = dungeonSystem.proceedToNextFloor(dungeon.id, floor.floorNumber)
                            if (nextFloor != null) {
                                currentFloor = nextFloor
                            } else {
                                // Completed dungeon
                                selectedDungeon = null
                                currentFloor = null
                                gameViewModel.addGameMessage("Congratulations! You've completed ${dungeon.name}!")
                            }
                        }
                    },
                    onReturnToHub = {
                        selectedDungeon = null
                        currentFloor = null
                    }
                )
            }
        }
        
        // Navigation buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { 
                    if (selectedDungeon != null) {
                        selectedDungeon = null
                        currentFloor = null
                    } else {
                        onNavigateBack()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PixelRed)
            ) {
                Text(if (selectedDungeon != null) "Exit Dungeon" else "Back to Hub", color = Color.White)
            }
        }
    }
    
    // Event dialog
    if (showEventDialog && eventResult != null) {
        AlertDialog(
            onDismissRequest = { showEventDialog = false },
            title = { Text("Random Event!", fontWeight = FontWeight.Bold) },
            text = { Text(eventResult!!) },
            confirmButton = {
                Button(
                    onClick = { showEventDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = PixelGreen)
                ) {
                    Text("Continue", color = Color.White)
                }
            }
        )
    }
}

@Composable
private fun DungeonSelectionView(
    dungeons: List<Dungeon>,
    onDungeonSelected: (Dungeon) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(dungeons) { dungeon ->
            DungeonCard(
                dungeon = dungeon,
                onClick = { onDungeonSelected(dungeon) }
            )
        }
    }
}

@Composable
private fun DungeonCard(
    dungeon: Dungeon,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (dungeon.theme) {
                DungeonTheme.FOREST -> PixelGreen
                DungeonTheme.VOLCANIC -> PixelRed
                DungeonTheme.ICE -> Color(0xFF87CEEB)
                DungeonTheme.RUINS -> Color(0xFF8B4513)
                DungeonTheme.UNDERWATER -> Color(0xFF1E90FF)
                DungeonTheme.SKY -> Color(0xFF87CEFA)
                DungeonTheme.DESERT -> Color(0xFFDEB887)
                DungeonTheme.CRYSTAL_CAVE -> Color(0xFFDDA0DD)
            }
        ),
        onClick = onClick
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
                    text = dungeon.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "${dungeon.maxFloors} floors",
                    fontSize = 12.sp,
                    color = Color.White
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = dungeon.description,
                fontSize = 14.sp,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Theme: ${dungeon.theme.name.replace("_", " ").lowercase().split(" ").joinToString(" ") { it.capitalize() }}",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun DungeonFloorView(
    floor: DungeonFloor,
    onExplore: (String) -> Unit,
    onProceedToNextFloor: () -> Unit,
    onReturnToHub: () -> Unit
) {
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState())
    ) {
        // Floor type indicator
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = when (floor.type) {
                    FloorType.REGULAR -> PixelBlue
                    FloorType.BOSS -> PixelRed
                    FloorType.EVENT -> PixelPurple
                }
            )
        ) {
            Text(
                text = when (floor.type) {
                    FloorType.REGULAR -> "🗺️ Exploration Floor"
                    FloorType.BOSS -> "⚔️ Boss Floor"
                    FloorType.EVENT -> "✨ Special Event Floor"
                },
                modifier = Modifier.padding(16.dp),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        
        // Special features
        if (floor.specialFeatures.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = PixelGray)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "🔍 Special Features:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    floor.specialFeatures.forEach { feature ->
                        Text(
                            text = "• ${feature.replace("_", " ").split(" ").joinToString(" ") { it.capitalize() }}",
                            fontSize = 12.sp,
                            color = Color.White,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            }
        }
        
        // Exploration actions
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = PixelDarkGreen)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "🧭 Exploration Options:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                // Direction buttons in grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = { onExplore("UP") },
                        colors = ButtonDefaults.buttonColors(containerColor = PixelBlue),
                        modifier = Modifier.weight(1f).padding(2.dp)
                    ) {
                        Text("⬆️ North", color = Color.White, fontSize = 12.sp)
                    }
                    Button(
                        onClick = { onExplore("DOWN") },
                        colors = ButtonDefaults.buttonColors(containerColor = PixelBlue),
                        modifier = Modifier.weight(1f).padding(2.dp)
                    ) {
                        Text("⬇️ South", color = Color.White, fontSize = 12.sp)
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = { onExplore("LEFT") },
                        colors = ButtonDefaults.buttonColors(containerColor = PixelBlue),
                        modifier = Modifier.weight(1f).padding(2.dp)
                    ) {
                        Text("⬅️ West", color = Color.White, fontSize = 12.sp)
                    }
                    Button(
                        onClick = { onExplore("RIGHT") },
                        colors = ButtonDefaults.buttonColors(containerColor = PixelBlue),
                        modifier = Modifier.weight(1f).padding(2.dp)
                    ) {
                        Text("➡️ East", color = Color.White, fontSize = 12.sp)
                    }
                }
                
                if (floor.hasStairs) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onProceedToNextFloor,
                        colors = ButtonDefaults.buttonColors(containerColor = PixelYellow),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("🪜 Proceed to Next Floor", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        
        // Wandering events preview
        if (floor.wanderingEvents.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = PixelPurple)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "🌟 Possible Random Events:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    floor.wanderingEvents.forEach { event ->
                        Text(
                            text = "• ${event.description}",
                            fontSize = 12.sp,
                            color = Color.White,
                            modifier = Modifier.padding(start = 16.dp, bottom = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

// Helper functions
private fun attemptRandomEncounter(floor: DungeonFloor, playerLevel: Int): Monster? {
    if (kotlin.random.Random.nextFloat() > floor.encounterRate) return null
    
    val possibleEncounters = floor.possibleEncounters
    if (possibleEncounters.isEmpty()) return null
    
    val selectedSpecies = possibleEncounters.random()
    return generateDungeonMonster(selectedSpecies, playerLevel, floor.theme)
}

private fun generateDungeonMonster(speciesId: String, playerLevel: Int, theme: DungeonTheme): Monster {
    // This would normally use the monster generation system
    return Monster(
        id = java.util.UUID.randomUUID().toString(),
        speciesId = speciesId,
        name = speciesId.replace("_", " ").split(" ").joinToString(" ") { it.capitalize() },
        type1 = getTypeForTheme(theme),
        type2 = null,
        family = MonsterFamily.BEAST,
        level = playerLevel + kotlin.random.Random.nextInt(-1, 2),
        currentHp = 80,
        currentMp = 40,
        experience = 0,
        experienceToNext = 100,
        baseStats = MonsterStats(40, 35, 40, 30, 30, 80, 40),
        currentStats = MonsterStats(40, 35, 40, 30, 30, 80, 40),
        skills = listOf("tackle", "growl"),
        traits = emptyList(),
        isWild = true,
        captureRate = 100,
        growthRate = GrowthRate.MEDIUM_FAST
    )
}

private fun getTypeForTheme(theme: DungeonTheme): MonsterType {
    return when (theme) {
        DungeonTheme.FOREST -> MonsterType.GRASS
        DungeonTheme.VOLCANIC -> MonsterType.FIRE
        DungeonTheme.ICE -> MonsterType.ICE
        DungeonTheme.RUINS -> MonsterType.GHOST
        DungeonTheme.UNDERWATER -> MonsterType.WATER
        DungeonTheme.SKY -> MonsterType.FLYING
        DungeonTheme.DESERT -> MonsterType.GROUND
        DungeonTheme.CRYSTAL_CAVE -> MonsterType.ROCK
    }
}

private fun handleWanderingEvent(event: WanderingEvent, gameViewModel: GameViewModel): String {
    return when (event.outcome) {
        EventOutcome.HEALING -> {
            gameViewModel.addGameMessage("Your party feels refreshed!")
            "Your monsters were healed by the ${event.id.replace("_", " ")}!"
        }
        EventOutcome.EXPERIENCE -> {
            gameViewModel.addGameMessage("Your party gained experience!")
            "Your monsters learned something from the ${event.id.replace("_", " ")}!"
        }
        EventOutcome.TREASURE -> {
            gameViewModel.addGameMessage("You found treasure!")
            "The ${event.id.replace("_", " ")} contained valuable items!"
        }
        EventOutcome.STAT_BOOST -> {
            gameViewModel.addGameMessage("Your party feels stronger!")
            "The ${event.id.replace("_", " ")} boosted your monsters' abilities!"
        }
        else -> "You encountered a ${event.id.replace("_", " ")} and something mysterious happened!"
    }
}