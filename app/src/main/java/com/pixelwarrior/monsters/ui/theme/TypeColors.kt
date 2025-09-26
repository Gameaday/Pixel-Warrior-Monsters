package com.pixelwarrior.monsters.ui.theme

import androidx.compose.ui.graphics.Color
import com.pixelwarrior.monsters.data.model.MonsterType

/**
 * UI utility for getting type-specific colors for monsters
 * Provides consistent color scheme across all UI components
 */
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