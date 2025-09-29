package com.pixelwarrior.monsters.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pixelwarrior.monsters.ui.theme.*

/**
 * Credits screen showing game credits and acknowledgments
 */
@Composable
fun CreditsScreen(
    onBackPressed: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PixelBlack)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = onBackPressed,
            colors = ButtonDefaults.buttonColors(containerColor = PixelBrown),
            modifier = Modifier.align(Alignment.Start)
        ) {
            Text("Back", color = Color.White)
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "🐉 Pixel Warrior Monsters",
            style = MaterialTheme.typography.headlineLarge,
            color = PixelYellow,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "A Dragon Quest Monsters Inspired Game",
            style = MaterialTheme.typography.bodyLarge,
            color = PixelLightGray,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = PixelDarkGray)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Development Team",
                    style = MaterialTheme.typography.titleLarge,
                    color = PixelYellow,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Game Design & Programming\nGameaday Studio",
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "Implementation Assistance\nGitHub Copilot",
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = PixelDarkBlue)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Special Thanks",
                    style = MaterialTheme.typography.titleLarge,
                    color = PixelYellow,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Inspired by Dragon Quest Monsters series\nby Square Enix",
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "Built with Android Jetpack Compose\nKotlin Programming Language",
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = PixelDarkGreen)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Game Features",
                    style = MaterialTheme.typography.titleLarge,
                    color = PixelYellow,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                val features = listOf(
                    "🐉 Dragon Quest Monsters-style taming",
                    "🍳 Cooking system with treat crafting",
                    "⚗️ Monster synthesis and breeding",
                    "⚔️ Turn-based battle system",
                    "🏰 Hub world exploration",
                    "📱 Modern mobile interface"
                )
                
                features.forEach { feature ->
                    Text(
                        text = feature,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Version 1.0 - Development Build",
            color = PixelGray,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Thank you for playing!",
            color = PixelYellow,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}