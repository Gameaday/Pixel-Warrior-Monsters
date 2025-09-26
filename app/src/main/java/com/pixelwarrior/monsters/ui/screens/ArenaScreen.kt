package com.pixelwarrior.monsters.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pixelwarrior.monsters.game.tournament.*
import com.pixelwarrior.monsters.ui.components.GameInfoPanel
import com.pixelwarrior.monsters.ui.theme.PixelGreen
import com.pixelwarrior.monsters.ui.theme.PixelRed
import com.pixelwarrior.monsters.ui.theme.PixelBlue
import com.pixelwarrior.monsters.ui.theme.PixelYellow
import com.pixelwarrior.monsters.data.model.Monster

/**
 * Arena Interface for Tournament System
 * Complete UI for tournament registration, rival battles, and championship tracking
 */

@Composable
fun ArenaScreen(
    tournamentSystem: TournamentSystem,
    playerGold: Int,
    playerParty: List<Monster>,
    onBattleRival: (RivalTrainer) -> Unit,
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Tournaments", "Rivals", "Leaderboard", "Seasonal")
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        // Arena Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(containerColor = PixelRed)
            ) {
                Text("Back", color = Color.White)
            }
            
            Text(
                "Battle Arena",
                color = PixelYellow,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            
            GameInfoPanel(
                gold = playerGold,
                partySize = playerParty.size,
                modifier = Modifier.widthIn(max = 120.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Tab Navigation
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Black,
            contentColor = PixelYellow
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Tab Content
        when (selectedTab) {
            0 -> TournamentTab(tournamentSystem, playerGold, playerParty)
            1 -> RivalsTab(tournamentSystem, onBattleRival)
            2 -> LeaderboardTab(tournamentSystem)
            3 -> SeasonalTab(tournamentSystem)
        }
    }
}

@Composable
fun TournamentTab(
    tournamentSystem: TournamentSystem,
    playerGold: Int,
    playerParty: List<Monster>
) {
    val tiers = tournamentSystem.getTournamentTiers()
    
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "Tournament Tiers",
                color = PixelYellow,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        items(tiers) { tier ->
            TournamentTierCard(
                tier = tier,
                canEnter = tournamentSystem.canEnterTournament(tier, playerGold, playerParty),
                onEnter = { /* Handle tournament entry */ }
            )
        }
        
        item {
            Spacer(modifier = Modifier.height(16.dp))
            PlayerRecordCard(tournamentSystem.getPlayerRecord())
        }
    }
}

@Composable
fun TournamentTierCard(
    tier: TournamentTier,
    canEnter: Boolean,
    onEnter: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Gray.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        tier.name,
                        color = PixelYellow,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        tier.description,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
                
                Button(
                    onClick = onEnter,
                    enabled = canEnter,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (canEnter) PixelGreen else Color.Gray
                    )
                ) {
                    Text("Enter", color = Color.White)
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Entry Fee: ${tier.entryFee}G", color = Color.White)
                Text("Prize: ${tier.prizePool}G", color = PixelYellow)
                Text("Levels: ${tier.minLevel}-${tier.maxLevel}", color = Color.White)
            }
        }
    }
}

@Composable
fun RivalsTab(
    tournamentSystem: TournamentSystem,
    onBattleRival: (RivalTrainer) -> Unit
) {
    val rivals = tournamentSystem.getRivalTrainers()
    
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "Rival Trainers",
                color = PixelYellow,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        items(rivals) { rival ->
            RivalTrainerCard(rival, onBattleRival)
        }
    }
}

@Composable
fun RivalTrainerCard(
    rival: RivalTrainer,
    onBattle: (RivalTrainer) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Gray.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        rival.name,
                        color = PixelYellow,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        rival.title,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    Text(
                        "Specialty: ${rival.preferredType.name}",
                        color = getTypeColor(rival.preferredType),
                        fontSize = 12.sp
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "Difficulty: ${rival.difficulty}/10",
                        color = getDifficultyColor(rival.difficulty),
                        fontSize = 12.sp
                    )
                    Text(
                        "Win Rate: ${(rival.winRate * 100).toInt()}%",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Button(
                        onClick = { onBattle(rival) },
                        colors = ButtonDefaults.buttonColors(containerColor = PixelBlue)
                    ) {
                        Text("Battle", color = Color.White)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                "Strategy: ${rival.signature}",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun LeaderboardTab(
    tournamentSystem: TournamentSystem
) {
    val leaderboard = tournamentSystem.getLeaderboard()
    
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                "Tournament Leaderboard",
                color = PixelYellow,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        itemsIndexed(leaderboard) { index, (name, winRate) ->
            LeaderboardEntry(
                rank = index + 1,
                name = name,
                winRate = winRate,
                isPlayer = name == "Player"
            )
        }
    }
}

@Composable
fun LeaderboardEntry(
    rank: Int,
    name: String,
    winRate: Float,
    isPlayer: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPlayer) PixelBlue.copy(alpha = 0.3f) else Color.Gray.copy(alpha = 0.2f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "#$rank",
                color = when (rank) {
                    1 -> PixelYellow
                    2 -> Color.White
                    3 -> Color(0xFFCD7F32) // Bronze
                    else -> Color.Gray
                },
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                name,
                color = if (isPlayer) PixelYellow else Color.White,
                fontSize = 16.sp,
                fontWeight = if (isPlayer) FontWeight.Bold else FontWeight.Normal
            )
            
            Text(
                "${(winRate * 100).toInt()}%",
                color = when {
                    winRate >= 0.8f -> PixelGreen
                    winRate >= 0.6f -> PixelYellow
                    else -> PixelRed
                },
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun SeasonalTab(
    tournamentSystem: TournamentSystem
) {
    val currentSeasonal = tournamentSystem.getCurrentSeasonalTournament()
    val allSeasonals = SeasonalTournament.ALL_SEASONS
    
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "Seasonal Tournaments",
                color = PixelYellow,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        if (currentSeasonal != null) {
            item {
                Text(
                    "🏆 Current Event",
                    color = PixelGreen,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                SeasonalTournamentCard(currentSeasonal, isActive = true)
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
        
        item {
            Text(
                "All Seasonal Events",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        items(allSeasonals) { seasonal ->
            SeasonalTournamentCard(seasonal, isActive = seasonal == currentSeasonal)
        }
    }
}

@Composable
fun SeasonalTournamentCard(
    tournament: SeasonalTournament,
    isActive: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) PixelGreen.copy(alpha = 0.3f) else Color.Gray.copy(alpha = 0.2f)
        )
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
                    tournament.name,
                    color = PixelYellow,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                
                if (isActive) {
                    Text(
                        "ACTIVE",
                        color = PixelGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                tournament.description,
                color = Color.White,
                fontSize = 14.sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Entry: ${tournament.entryFee}G", color = Color.White, fontSize = 12.sp)
                Text("Grand Prize: ${tournament.grandPrize}G", color = PixelYellow, fontSize = 12.sp)
                Text("Duration: ${tournament.durationDays} days", color = Color.White, fontSize = 12.sp)
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                "Restrictions: ${tournament.restrictions}",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun PlayerRecordCard(record: TournamentRecord) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = PixelBlue.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Your Tournament Record",
                color = PixelYellow,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Wins: ${record.wins}", color = PixelGreen, fontSize = 14.sp)
                    Text("Losses: ${record.losses}", color = PixelRed, fontSize = 14.sp)
                }
                Column {
                    Text("Win Rate: ${(record.winRate * 100).toInt()}%", color = PixelYellow, fontSize = 14.sp)
                    Text("Current Streak: ${record.currentStreak}", color = Color.White, fontSize = 14.sp)
                }
                Column {
                    Text("Best Streak: ${record.highestStreak}", color = Color.White, fontSize = 14.sp)
                    Text("Total Prizes: ${record.totalPrizes}G", color = PixelYellow, fontSize = 14.sp)
                }
            }
            
            if (record.championTitles.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Championships: ${record.championTitles.entries.joinToString { "${it.key} (${it.value}x)" }}",
                    color = PixelYellow,
                    fontSize = 12.sp
                )
            }
        }
    }
}

// Helper functions
private fun getTypeColor(type: com.pixelwarrior.monsters.data.model.MonsterType): Color {
    return when (type) {
        com.pixelwarrior.monsters.data.model.MonsterType.FIRE -> Color.Red
        com.pixelwarrior.monsters.data.model.MonsterType.WATER -> Color.Blue
        com.pixelwarrior.monsters.data.model.MonsterType.GRASS -> Color.Green
        com.pixelwarrior.monsters.data.model.MonsterType.ELECTRIC -> Color.Yellow
        com.pixelwarrior.monsters.data.model.MonsterType.ICE -> Color.Cyan
        com.pixelwarrior.monsters.data.model.MonsterType.AIR -> Color.White
        com.pixelwarrior.monsters.data.model.MonsterType.EARTH -> Color(0xFF8B4513) // SaddleBrown
        com.pixelwarrior.monsters.data.model.MonsterType.STEEL -> Color.Gray
        com.pixelwarrior.monsters.data.model.MonsterType.DARK -> Color.Magenta
        com.pixelwarrior.monsters.data.model.MonsterType.LIGHT -> Color.White
        com.pixelwarrior.monsters.data.model.MonsterType.CRYSTAL -> Color(0xFFFF69B4) // HotPink
        else -> Color.White
    }
}

private fun getDifficultyColor(difficulty: Int): Color {
    return when {
        difficulty <= 3 -> PixelGreen
        difficulty <= 6 -> PixelYellow
        difficulty <= 8 -> Color(0xFFFF8C00) // DarkOrange
        else -> PixelRed
    }
}