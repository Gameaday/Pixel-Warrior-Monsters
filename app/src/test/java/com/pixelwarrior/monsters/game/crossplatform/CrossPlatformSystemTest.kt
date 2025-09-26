package com.pixelwarrior.monsters.game.crossplatform

import com.pixelwarrior.monsters.game.crossplatform.*
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * Test suite for Cross-Platform System
 */
class CrossPlatformSystemTest {
    
    private lateinit var crossPlatformSystem: CrossPlatformSystem
    
    @Before
    fun setup() {
        crossPlatformSystem = CrossPlatformSystem()
    }
    
    @Test
    fun `steam integration works correctly`() = runTest {
        // Test initial state
        assertEquals(SteamStatus.DISCONNECTED, crossPlatformSystem.steamStatus.value)
        
        // Test Steam initialization
        val initResult = crossPlatformSystem.initializeSteam()
        assertTrue(initResult is SteamInitResult.SUCCESS)
        assertEquals(SteamStatus.CONNECTED, crossPlatformSystem.steamStatus.value)
        
        // Test achievement unlock when connected
        assertTrue(crossPlatformSystem.unlockSteamAchievement("test_achievement"))
        
        // Test stats update when connected
        val stats = mapOf("battles_won" to 10, "monsters_caught" to 5)
        assertTrue(crossPlatformSystem.updateSteamStats(stats))
        
        // Test workshop upload
        val modData = ModData(
            content = "test mod content".toByteArray(),
            metadata = mapOf("name" to "Test Mod")
        )
        val uploadResult = crossPlatformSystem.uploadToSteamWorkshop(modData)
        assertTrue(uploadResult is WorkshopUploadResult.SUCCESS)
    }
    
    @Test
    fun `cloud sync works correctly`() = runTest {
        // Test initial state
        assertEquals(CloudSyncStatus.OFFLINE, crossPlatformSystem.cloudSyncStatus.value)
        
        // Test cloud sync
        val saveData = "test save data".toByteArray()
        val syncResult = crossPlatformSystem.syncToCloud(saveData, CloudProvider.STEAM)
        assertTrue(syncResult is CloudSyncResult.SUCCESS)
        assertEquals(CloudSyncStatus.SYNCED, crossPlatformSystem.cloudSyncStatus.value)
        
        // Test cloud download
        val downloadResult = crossPlatformSystem.downloadFromCloud(CloudProvider.STEAM)
        assertTrue(downloadResult is CloudDownloadResult.SUCCESS)
        assertNotNull((downloadResult as CloudDownloadResult.SUCCESS).data)
        
        // Test conflict resolution
        val localSave = "local".toByteArray()
        val cloudSave = "cloud".toByteArray()
        
        val useLocal = crossPlatformSystem.resolveCloudConflict(localSave, cloudSave, ConflictResolutionStrategy.USE_LOCAL)
        assertEquals("local", String(useLocal))
        
        val useCloud = crossPlatformSystem.resolveCloudConflict(localSave, cloudSave, ConflictResolutionStrategy.USE_CLOUD)
        assertEquals("cloud", String(useCloud))
        
        val merged = crossPlatformSystem.resolveCloudConflict(localSave, cloudSave, ConflictResolutionStrategy.MERGE)
        assertEquals("localcloud", String(merged))
    }
    
    @Test
    fun `mod system works correctly`() = runTest {
        // Test initial state
        assertTrue(crossPlatformSystem.installedMods.value.isEmpty())
        
        // Test mod loading
        val loadResult = crossPlatformSystem.loadMod("/test/mod/path")
        assertTrue(loadResult is ModLoadResult.SUCCESS)
        
        val loadedMod = (loadResult as ModLoadResult.SUCCESS).modInfo
        assertEquals("Sample Mod", loadedMod.name)
        assertTrue(loadedMod.isEnabled)
        
        // Verify mod was added to installed list
        val installedMods = crossPlatformSystem.installedMods.value
        assertEquals(1, installedMods.size)
        assertEquals(loadedMod.id, installedMods[0].id)
        
        // Test mod validation
        val validModData = "small mod content".toByteArray()
        val validationResult = crossPlatformSystem.validateMod(validModData)
        assertTrue(validationResult is ModValidationResult.VALID)
        
        // Test mod enable/disable
        assertTrue(crossPlatformSystem.disableMod(loadedMod.id))
        val disabledMod = crossPlatformSystem.installedMods.value.find { it.id == loadedMod.id }
        assertFalse(disabledMod!!.isEnabled)
        
        assertTrue(crossPlatformSystem.enableMod(loadedMod.id))
        val enabledMod = crossPlatformSystem.installedMods.value.find { it.id == loadedMod.id }
        assertTrue(enabledMod!!.isEnabled)
    }
    
    @Test
    fun `social features work correctly`() = runTest {
        // Test initial state
        assertTrue(crossPlatformSystem.friendsList.value.isEmpty())
        
        // Test adding friend
        val friendResult = crossPlatformSystem.addFriend("test_friend_123")
        assertTrue(friendResult is FriendResult.SUCCESS)
        
        // Verify friend was added
        val friends = crossPlatformSystem.friendsList.value
        assertEquals(1, friends.size)
        assertEquals("test_friend_123", friends[0].id)
        assertEquals(FriendStatus.ONLINE, friends[0].status)
        
        // Test monster sharing
        val shareResult = crossPlatformSystem.shareMonster("test_friend_123", "monster_456")
        assertTrue(shareResult is ShareResult.SUCCESS)
        
        // Test global leaderboard
        val leaderboard = crossPlatformSystem.getGlobalLeaderboard(LeaderboardCategory.BATTLES_WON)
        assertEquals(10, leaderboard.size)
        assertEquals(1, leaderboard[0].rank)
        assertEquals(1000, leaderboard[0].score)
        assertEquals(LeaderboardCategory.BATTLES_WON, leaderboard[0].category)
    }
    
    @Test
    fun `multiplayer system works correctly`() = runTest {
        // Test initial state
        assertEquals(MultiplayerStatus.OFFLINE, crossPlatformSystem.multiplayerStatus.value)
        
        // Test multiplayer connection
        val connectResult = crossPlatformSystem.connectToMultiplayer()
        assertTrue(connectResult is MultiplayerConnectResult.SUCCESS)
        assertEquals(MultiplayerStatus.CONNECTED, crossPlatformSystem.multiplayerStatus.value)
        
        // Test matchmaking
        val matchResult = crossPlatformSystem.findMatch(MatchType.CASUAL)
        assertTrue(matchResult is MatchmakingResult.SUCCESS)
        
        val matchData = matchResult as MatchmakingResult.SUCCESS
        assertEquals("Rival Trainer", matchData.opponent.name)
        assertEquals(1500, matchData.opponent.rating)
        assertNotNull(matchData.matchId)
        
        // Test spectating
        val spectateResult = crossPlatformSystem.spectateMatch("test_match_id")
        assertTrue(spectateResult is SpectateResult.SUCCESS)
    }
    
    @Test
    fun `multiplayer requires connection`() = runTest {
        // Test matchmaking without connection
        val matchResult = crossPlatformSystem.findMatch(MatchType.RANKED)
        assertTrue(matchResult is MatchmakingResult.FAILED)
        assertEquals("Not connected to multiplayer", (matchResult as MatchmakingResult.FAILED).reason)
    }
    
    @Test
    fun `steam features require connection`() {
        // Test achievement unlock without connection
        assertFalse(crossPlatformSystem.unlockSteamAchievement("test_achievement"))
        
        // Test stats update without connection
        val stats = mapOf("test" to 1)
        assertFalse(crossPlatformSystem.updateSteamStats(stats))
    }
}