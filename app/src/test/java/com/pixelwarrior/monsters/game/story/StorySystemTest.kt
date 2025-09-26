package com.pixelwarrior.monsters.game.story

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for Story System - Phase 2 Implementation
 * Validates story progression, quest management, and milestone triggers
 */
class StorySystemTest {

    private lateinit var storySystem: StorySystem

    @Before
    fun setup() {
        storySystem = StorySystem()
    }

    @Test
    fun `story system initializes with default progress`() = runTest {
        storySystem.initializeStory()
        
        val progress = storySystem.currentStoryProgress.first()
        
        assertTrue("Game should start as started", progress["game_started"] == true)
        assertFalse("Tutorial should not be completed initially", progress["completed_tutorial"] == true)
        assertFalse("No monster should be captured initially", progress["first_monster_captured"] == true)
    }

    @Test
    fun `tutorial quest is available at start`() = runTest {
        storySystem.initializeStory()
        
        val activeQuests = storySystem.activeQuests.first()
        
        assertEquals("Should have one active quest initially", 1, activeQuests.size)
        assertEquals("Should be tutorial quest", "tutorial_quest", activeQuests[0].id)
        assertEquals("Tutorial quest should have 3 objectives", 3, activeQuests[0].objectives.size)
    }

    @Test
    fun `milestone triggers unlock new content`() = runTest {
        storySystem.initializeStory()
        
        val unlocks = storySystem.triggerMilestone("completed_tutorial")
        
        assertTrue("Should have unlocks from completing tutorial", unlocks.isNotEmpty())
        val progress = storySystem.currentStoryProgress.first()
        assertTrue("Tutorial should be marked as completed", progress["completed_tutorial"] == true)
    }

    @Test
    fun `quest rewards are properly structured`() = runTest {
        storySystem.initializeStory()
        
        val reward = storySystem.completeQuest("tutorial_quest")
        
        assertNotNull("Should get reward from completed quest", reward)
        assertEquals("Should get gold reward", 500, reward!!.gold)
        assertTrue("Should get herb items", reward.items["herb"] == 5)
        assertEquals("Should get experience", 100, reward.experience)
    }

    @Test
    fun `story gates work correctly`() = runTest {
        storySystem.initializeStory()
        
        assertFalse("Library gate should be locked initially", 
                   storySystem.isStoryGateUnlocked("monster_library"))
        
        storySystem.triggerMilestone("completed_tutorial")
        
        assertTrue("Library gate should unlock after tutorial", 
                  storySystem.isStoryGateUnlocked("monster_library"))
    }
}