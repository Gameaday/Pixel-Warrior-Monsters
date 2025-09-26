package com.pixelwarrior.monsters.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Search
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

/**
 * Monster Encyclopedia/Codex screen for viewing discovered monsters
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonsterCodexScreen(
    discoveredMonsters: List<MonsterSpecies>,
    allSpecies: List<MonsterSpecies>,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val audioViewModel: AudioViewModel = viewModel(factory = AudioViewModelFactory(context))
    
    var selectedSpecies by remember { mutableStateOf<MonsterSpecies?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(MonsterType.NORMAL) }
    var showOnlyDiscovered by remember { mutableStateOf(false) }
    
    val filteredSpecies = allSpecies.filter { species ->
        val matchesSearch = species.name.contains(searchQuery, ignoreCase = true)
        val matchesType = selectedFilter == MonsterType.NORMAL || species.type1 == selectedFilter || species.type2 == selectedFilter
        val matchesDiscovery = !showOnlyDiscovered || discoveredMonsters.contains(species)
        matchesSearch && matchesType && matchesDiscovery
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PixelBlack)
    ) {
        // Header
        CodexHeader(
            discoveredCount = discoveredMonsters.size,
            totalCount = allSpecies.size,
            onBackPressed = {
                audioViewModel.playMenuBackSound()
                onBackPressed()
            }
        )
        
        // Filters and Search
        CodexFilters(
            searchQuery = searchQuery,
            onSearchChanged = { searchQuery = it },
            selectedFilter = selectedFilter,
            onFilterChanged = { 
                audioViewModel.playMenuSelectSound()
                selectedFilter = it 
            },
            showOnlyDiscovered = showOnlyDiscovered,
            onDiscoveryFilterChanged = { 
                audioViewModel.playMenuSelectSound()
                showOnlyDiscovered = it 
            }
        )
        
        if (selectedSpecies != null) {
            // Detailed view
            MonsterDetailView(
                species = selectedSpecies!!,
                isDiscovered = discoveredMonsters.contains(selectedSpecies),
                onBack = { 
                    audioViewModel.playMenuBackSound()
                    selectedSpecies = null 
                }
            )
        } else {
            // Grid view
            MonsterGrid(
                species = filteredSpecies,
                discoveredMonsters = discoveredMonsters,
                onSpeciesSelected = { species ->
                    audioViewModel.playMenuSelectSound()
                    selectedSpecies = species
                }
            )
        }
    }
}

@Composable
private fun CodexHeader(
    discoveredCount: Int,
    totalCount: Int,
    onBackPressed: () -> Unit
) {
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
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Book,
                    contentDescription = "Monster Codex",
                    tint = ExpYellow,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Monster Codex",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Discovered: $discoveredCount / $totalCount",
                        style = MaterialTheme.typography.bodySmall,
                        color = ExpYellow
                    )
                }
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
private fun CodexFilters(
    searchQuery: String,
    onSearchChanged: (String) -> Unit,
    selectedFilter: MonsterType,
    onFilterChanged: (MonsterType) -> Unit,
    showOnlyDiscovered: Boolean,
    onDiscoveryFilterChanged: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        colors = CardDefaults.cardColors(containerColor = PixelDarkGray),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChanged,
                placeholder = { Text("Search monsters...", color = PixelGray) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = PixelGray) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = PixelBlue,
                    unfocusedBorderColor = PixelGray
                ),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Filter toggles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Show only discovered",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
                
                Switch(
                    checked = showOnlyDiscovered,
                    onCheckedChange = onDiscoveryFilterChanged,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = ExpYellow,
                        checkedTrackColor = PixelGreen
                    )
                )
            }
        }
    }
}

@Composable
private fun MonsterGrid(
    species: List<MonsterSpecies>,
    discoveredMonsters: List<MonsterSpecies>,
    onSpeciesSelected: (MonsterSpecies) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(species) { species ->
            MonsterGridItem(
                species = species,
                isDiscovered = discoveredMonsters.contains(species),
                onClick = { onSpeciesSelected(species) }
            )
        }
    }
}

@Composable
private fun MonsterGridItem(
    species: MonsterSpecies,
    isDiscovered: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isDiscovered) PixelDarkGray else PixelBlack
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Monster icon/sprite placeholder
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = if (isDiscovered) getTypeColor(species.type1) else PixelGray,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .border(
                        width = if (isDiscovered) 2.dp else 1.dp,
                        color = if (isDiscovered) Color.White else PixelDarkGray,
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isDiscovered) {
                    Text(
                        text = species.name.first().toString(),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = "?",
                        style = MaterialTheme.typography.titleMedium,
                        color = PixelDarkGray,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = if (isDiscovered) species.name else "???",
                style = MaterialTheme.typography.bodySmall,
                color = if (isDiscovered) Color.White else PixelGray,
                textAlign = TextAlign.Center,
                fontWeight = if (isDiscovered) FontWeight.Bold else FontWeight.Normal
            )
            
            if (isDiscovered) {
                Text(
                    text = species.type1.name.lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelSmall,
                    color = getTypeColor(species.type1),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun MonsterDetailView(
    species: MonsterSpecies,
    isDiscovered: Boolean,
    onBack: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = PixelDarkGray),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header with back button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isDiscovered) species.name else "Unknown Monster",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                
                Button(
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(containerColor = PixelBlue),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    Text("Back", color = Color.White)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (isDiscovered) {
                LazyColumn {
                    item {
                        // Monster sprite placeholder
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(getTypeColor(species.type1), RoundedCornerShape(8.dp))
                                .align(Alignment.CenterHorizontally),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = species.name.first().toString(),
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Basic Info
                        DetailSection("Basic Information") {
                            DetailRow("Family", species.family.name)
                            DetailRow("Type", "${species.type1}${species.type2?.let { " / $it" } ?: ""}")
                            DetailRow("Capture Rate", "${species.captureRate}/255")
                            DetailRow("Growth Rate", species.growthRate.name.lowercase().replaceFirstChar { it.uppercase() })
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Base Stats
                        DetailSection("Base Stats") {
                            DetailRow("HP", species.baseStats.maxHp.toString())
                            DetailRow("MP", species.baseStats.maxMp.toString())
                            DetailRow("Attack", species.baseStats.attack.toString())
                            DetailRow("Defense", species.baseStats.defense.toString())
                            DetailRow("Agility", species.baseStats.agility.toString())
                            DetailRow("Magic", species.baseStats.magic.toString())
                            DetailRow("Wisdom", species.baseStats.wisdom.toString())
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Description
                        if (species.description.isNotEmpty()) {
                            DetailSection("Description") {
                                Text(
                                    text = species.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = PixelLightGray,
                                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
                                )
                            }
                        }
                    }
                }
            } else {
                // Unknown monster display
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(PixelGray, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "?",
                                style = MaterialTheme.typography.titleLarge,
                                color = PixelDarkGray,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "This monster hasn't been discovered yet.\nEncounter it in the wild to learn more!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = PixelLightGray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = ExpYellow,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Card(
            colors = CardDefaults.cardColors(containerColor = PixelBlack.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(6.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
private fun DetailRow(
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