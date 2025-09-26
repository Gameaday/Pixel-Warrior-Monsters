package com.pixelwarrior.monsters.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pixelwarrior.monsters.game.exploration.ExplorationSystem
import com.pixelwarrior.monsters.ui.theme.*
import java.time.format.DateTimeFormatter

/**
 * Complete exploration hub interface for advanced exploration features
 */
@Composable
fun ExplorationHubScreen(
    explorationSystem: ExplorationSystem,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabTitles = listOf("Weather", "Gate Keys", "Monster Nests", "Hidden Areas")
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PixelDarkGreen)
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(containerColor = PixelBrown)
            ) {
                Text("Back", color = Color.White)
            }
            
            Text(
                "Exploration Hub",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            // Current Time and Weather
            Column(horizontalAlignment = Alignment.End) {
                val state = explorationSystem.getCurrentState()
                Text(
                    state.timeOfDay.format(DateTimeFormatter.ofPattern("HH:mm")),
                    color = Color.White,
                    fontSize = 14.sp
                )
                Text(
                    state.currentWeather.displayName,
                    color = getWeatherColor(state.currentWeather),
                    fontSize = 12.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Tab Navigation
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(tabTitles.size) { index ->
                Button(
                    onClick = { selectedTab = index },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedTab == index) PixelBlue else PixelGray
                    )
                ) {
                    Text(
                        tabTitles[index],
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Tab Content
        when (selectedTab) {
            0 -> WeatherTab(explorationSystem)
            1 -> GateKeysTab(explorationSystem)
            2 -> MonsterNestsTab(explorationSystem)
            3 -> HiddenAreasTab(explorationSystem)
        }
    }
}

@Composable
private fun WeatherTab(explorationSystem: ExplorationSystem) {
    val state = explorationSystem.getCurrentState()
    val forecast = explorationSystem.getWeatherForecast()
    val timeEvents = explorationSystem.checkTimeBasedEvents()
    
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Current Weather
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = PixelLightGreen)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "Current Weather",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                state.currentWeather.displayName,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = getWeatherColor(state.currentWeather)
                            )
                            Text(
                                "Encounter Rate: ${(state.currentWeather.encounterModifier * 100).toInt()}%",
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }
                        
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                if (explorationSystem.isNightTime()) "Night" else "Day",
                                fontSize = 16.sp,
                                color = if (explorationSystem.isNightTime()) PixelPurple else PixelYellow
                            )
                            Text(
                                state.timeOfDay.format(DateTimeFormatter.ofPattern("HH:mm")),
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Battle Effect: ${state.currentWeather.battleEffect}",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }
        }
        
        // Weather Forecast
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = PixelBlue)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "Weather Forecast",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    forecast.forEach { (time, weather) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(time, color = Color.White, fontSize = 12.sp)
                            Text(
                                weather.displayName,
                                color = getWeatherColor(weather),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
        
        // Time-Based Events
        if (timeEvents.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = PixelPurple)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            "Current Events",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        timeEvents.forEach { event ->
                            Text(
                                "• $event",
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GateKeysTab(explorationSystem: ExplorationSystem) {
    val allKeys = explorationSystem.getAvailableKeys()
    val collectedKeys = explorationSystem.getCurrentState().gateKeysCollected
    
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                "Gate Keys Collected: ${collectedKeys.size}/${allKeys.size}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        
        items(allKeys) { key ->
            val isCollected = key in collectedKeys
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isCollected) PixelGold else PixelGray
                )
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
                            key.displayName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            key.description,
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }
                    
                    Text(
                        if (isCollected) "✓" else "✗",
                        fontSize = 20.sp,
                        color = if (isCollected) PixelGreen else PixelRed
                    )
                }
            }
        }
    }
}

@Composable
private fun MonsterNestsTab(explorationSystem: ExplorationSystem) {
    val activeNests = explorationSystem.getActiveNests()
    val allKeys = explorationSystem.getAvailableKeys()
    val hasKeys = explorationSystem.getCurrentState().gateKeysCollected
    
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                "Active Monster Nests: ${activeNests.size}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        
        items(activeNests) { nest ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = PixelGreen)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        nest.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        "Location: ${nest.location}",
                        fontSize = 12.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        nest.specialBonus,
                        fontSize = 12.sp,
                        color = PixelYellow
                    )
                    Text(
                        "Breeding Time: ${(nest.breedingTimeReduction * 100).toInt()}% of normal",
                        fontSize = 11.sp,
                        color = Color.White
                    )
                    Text(
                        "Rare Chance: ${(nest.rareMonsterChance * 100).toInt()}%",
                        fontSize = 11.sp,
                        color = Color.White
                    )
                }
            }
        }
        
        // Show locked nests
        allKeys.forEach { key ->
            if (key !in hasKeys) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = PixelGray)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                "Locked Nest",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                "Requires: ${key.displayName}",
                                fontSize = 12.sp,
                                color = PixelRed
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HiddenAreasTab(explorationSystem: ExplorationSystem) {
    val hiddenPassages = explorationSystem.getHiddenPassages()
    
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                "Hidden Passages Discovered: ${hiddenPassages.size}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        
        if (hiddenPassages.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = PixelGray)
                ) {
                    Text(
                        "No hidden passages discovered yet.\nSolve puzzles and explore carefully to find secret areas!",
                        modifier = Modifier.padding(16.dp),
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            items(hiddenPassages.toList()) { passage ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = PixelPurple)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            passage,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            "Discovered ✓",
                            fontSize = 12.sp,
                            color = PixelGreen
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun getWeatherColor(weather: ExplorationSystem.WeatherType): Color {
    return when (weather) {
        ExplorationSystem.WeatherType.SUNNY -> PixelYellow
        ExplorationSystem.WeatherType.RAINY -> PixelBlue
        ExplorationSystem.WeatherType.STORMY -> PixelPurple
        ExplorationSystem.WeatherType.FOGGY -> PixelGray
        ExplorationSystem.WeatherType.SNOWY -> Color.White
        ExplorationSystem.WeatherType.WINDY -> PixelGreen
    }
}