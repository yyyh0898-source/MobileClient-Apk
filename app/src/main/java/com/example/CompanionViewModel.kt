package com.example

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.BatteryManager
import android.os.Debug
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.network.RakNetPing
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CompanionViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs: SharedPreferences =
        application.getSharedPreferences("mcmobile_companion_prefs", Context.MODE_PRIVATE)

    private val vibrator = application.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator

    // ---- Install & Launcher State ----
    private val _gameStatus = MutableStateFlow(GameInstallStatus())
    val gameStatus: StateFlow<GameInstallStatus> = _gameStatus.asStateFlow()

    private val _selectedMode = MutableStateFlow("patch") // "patch" or "vanilla"
    val selectedMode: StateFlow<String> = _selectedMode.asStateFlow()

    private val _customPackage = MutableStateFlow(
        prefs.getString("custom_pkg", "com.mojang.minecraftpe") ?: "com.mojang.minecraftpe"
    )
    val customPackage: StateFlow<String> = _customPackage.asStateFlow()

    // ---- HUD Modules State ----
    private val defaultModules = listOf(
        HudModuleItem("fps", "FPS Counter", "Direct render loop FPS hook or choreographer", ModuleCategory.HUD, true, 0.05f, 0.04f),
        HudModuleItem("cps", "CPS Counter", "Screen taps per second counter for PvP", ModuleCategory.HUD, true, 0.05f, 0.12f),
        HudModuleItem("clock", "Clock", "Local device clock in 12h or 24h format", ModuleCategory.HUD, true, 0.05f, 0.20f),
        HudModuleItem("battery", "Battery Level", "Battery percentage and charging status", ModuleCategory.HUD, true, 0.05f, 0.28f),
        HudModuleItem("memory", "Memory (RAM)", "Process RAM consumption indicator", ModuleCategory.HUD, false, 0.05f, 0.36f),
        HudModuleItem("ping", "Server Ping", "Live RakNet latency to current Bedrock server", ModuleCategory.HUD, true, 0.05f, 0.44f),
        HudModuleItem("crosshair", "Custom Crosshair", "Configurable center reticle with multiple shapes", ModuleCategory.VISUAL, true, 0.5f, 0.5f),
        HudModuleItem("screen_filter", "Screen Filter", "Atmospheric tint overlay (warm / night filter)", ModuleCategory.VISUAL, false),
        HudModuleItem("reference_image", "Reference Blueprint", "Pixel art and building overlay on screen", ModuleCategory.HUD, false, 0.70f, 0.05f),
        HudModuleItem("client", "Menu Button", "Floating draggable ☰ client menu trigger", ModuleCategory.CLIENT, true, 0.02f, 0.45f)
    )

    private val _hudModules = MutableStateFlow(defaultModules)
    val hudModules: StateFlow<List<HudModuleItem>> = _hudModules.asStateFlow()

    // ---- Crosshair State ----
    private val _crosshairSettings = MutableStateFlow(CrosshairSettings())
    val crosshairSettings: StateFlow<CrosshairSettings> = _crosshairSettings.asStateFlow()

    // ---- Screen Filter State ----
    private val _filterSettings = MutableStateFlow(ScreenFilterSettings())
    val filterSettings: StateFlow<ScreenFilterSettings> = _filterSettings.asStateFlow()

    // ---- Preview Backdrop ----
    private val _previewBackdrop = MutableStateFlow("Grassland") // Grassland, Nether, The End, Night Battle
    val previewBackdrop: StateFlow<String> = _previewBackdrop.asStateFlow()

    // ---- Servers State ----
    private val defaultServers = listOf(
        BedrockServerInfo("hive", "The Hive", "geo.hivebedrock.network", 19132, true, -1, false, "The Hive Bedrock Network"),
        BedrockServerInfo("cubecraft", "CubeCraft Games", "play.cubecraft.net", 19132, true, -1, false, "CubeCraft Bedrock Server"),
        BedrockServerInfo("nethergames", "NetherGames", "play.nethergames.org", 19132, true, -1, false, "PvP, Factions & Minigames"),
        BedrockServerInfo("galaxite", "Galaxite", "play.galaxite.net", 19132, true, -1, false, "Galaxite Minigames Planet"),
        BedrockServerInfo("mineville", "Mineville", "play.inpvp.net", 19132, true, -1, false, "Mineville City & Roleplay"),
        BedrockServerInfo("lifeboat", "Lifeboat Network", "play.lbsg.net", 19132, true, -1, false, "Lifeboat Survival & Skyblock")
    )

    private val _servers = MutableStateFlow<List<BedrockServerInfo>>(defaultServers)
    val servers: StateFlow<List<BedrockServerInfo>> = _servers.asStateFlow()

    // ---- CPS Trainer State ----
    private val _cpsTimerSeconds = MutableStateFlow(5) // 5s, 10s, 30s
    val cpsTimerSeconds: StateFlow<Int> = _cpsTimerSeconds.asStateFlow()

    private val _cpsRemainingSeconds = MutableStateFlow(5)
    val cpsRemainingSeconds: StateFlow<Int> = _cpsRemainingSeconds.asStateFlow()

    private val _cpsRunning = MutableStateFlow(false)
    val cpsRunning: StateFlow<Boolean> = _cpsRunning.asStateFlow()

    private val _cpsTapsCount = MutableStateFlow(0)
    val cpsTapsCount: StateFlow<Int> = _cpsTapsCount.asStateFlow()

    private val _currentCps = MutableStateFlow(0.0f)
    val currentCps: StateFlow<Float> = _currentCps.asStateFlow()

    private val _highestCps = MutableStateFlow(prefs.getFloat("highest_cps", 0f))
    val highestCps: StateFlow<Float> = _highestCps.asStateFlow()

    private val _cpsHistory = MutableStateFlow<List<Float>>(emptyList())
    val cpsHistory: StateFlow<List<Float>> = _cpsHistory.asStateFlow()

    // ---- Profiles State ----
    private val _profiles = MutableStateFlow<List<ClientProfile>>(emptyList())
    val profiles: StateFlow<List<ClientProfile>> = _profiles.asStateFlow()

    // ---- UI Notice / Toast ----
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    init {
        refreshGameStatus()
        loadProfiles()
        pingAllServers()
    }

    // ---- Launcher Actions ----
    @Suppress("DEPRECATION")
    fun refreshGameStatus() {
        val pm = getApplication<Application>().packageManager
        val pkg = _customPackage.value
        try {
            val info = pm.getPackageInfo(pkg, PackageManager.GET_PROVIDERS)
            val isPatched = info.providers?.any { it.name == "com.mobileclient.sdk.ClientInitProvider" } == true
            val ver = info.versionName ?: "v1.21.x"
            _gameStatus.value = GameInstallStatus(
                packageName = pkg,
                isInstalled = true,
                versionName = ver,
                isClientPatched = isPatched,
                statusMessage = if (isPatched) "Client patch verified & ready" else "Original Minecraft (No patch detected)"
            )
        } catch (e: Exception) {
            _gameStatus.value = GameInstallStatus(
                packageName = pkg,
                isInstalled = false,
                versionName = "",
                isClientPatched = false,
                statusMessage = "Minecraft not detected at $pkg"
            )
        }
    }

    fun setPackageName(pkg: String) {
        val sanitized = pkg.trim().ifEmpty { "com.mojang.minecraftpe" }
        _customPackage.value = sanitized
        prefs.edit().putString("custom_pkg", sanitized).apply()
        refreshGameStatus()
    }

    fun setSelectedMode(mode: String) {
        _selectedMode.value = mode
    }

    fun launchMinecraft(): Boolean {
        val ctx = getApplication<Application>()
        val pm = ctx.packageManager
        val pkg = _customPackage.value
        val intent = pm.getLaunchIntentForPackage(pkg)

        if (intent == null) {
            _userMessage.value = "Cannot launch: $pkg is not installed on this device."
            return false
        }

        // Pass mode extra matching MobileClient.EXTRA_MODE
        val mode = if (_selectedMode.value == "patch") "patch" else "vanilla"
        intent.putExtra("mobileclient.mode", mode)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        return try {
            ctx.startActivity(intent)
            _userMessage.value = "Launching Minecraft in ${mode.uppercase()} mode..."
            true
        } catch (e: Exception) {
            _userMessage.value = "Failed to launch Minecraft: ${e.localizedMessage}"
            false
        }
    }

    // ---- Module Actions ----
    fun toggleModule(id: String) {
        _hudModules.update { list ->
            list.map { if (it.id == id) it.copy(enabled = !it.enabled) else it }
        }
    }

    fun updateModulePosition(id: String, x: Float, y: Float) {
        _hudModules.update { list ->
            list.map {
                if (it.id == id) it.copy(
                    posX = x.coerceIn(0.01f, 0.95f),
                    posY = y.coerceIn(0.01f, 0.95f)
                ) else it
            }
        }
    }

    // ---- Crosshair Actions ----
    fun updateCrosshairShape(shape: String) {
        _crosshairSettings.update { it.copy(shape = shape) }
    }

    fun updateCrosshairSize(size: Float) {
        _crosshairSettings.update { it.copy(size = size.coerceIn(3f, 40f)) }
    }

    fun updateCrosshairThickness(thick: Float) {
        _crosshairSettings.update { it.copy(thickness = thick.coerceIn(1f, 8f)) }
    }

    fun updateCrosshairGap(gap: Float) {
        _crosshairSettings.update { it.copy(gap = gap.coerceIn(0f, 20f)) }
    }

    fun updateCrosshairColor(color: Color) {
        _crosshairSettings.update { it.copy(color = color) }
    }

    fun setPreviewBackdrop(backdrop: String) {
        _previewBackdrop.value = backdrop
    }

    // ---- Screen Filter Actions ----
    fun toggleScreenFilter(enabled: Boolean) {
        _filterSettings.update { it.copy(enabled = enabled) }
        _hudModules.update { list ->
            list.map { if (it.id == "screen_filter") it.copy(enabled = enabled) else it }
        }
    }

    fun updateScreenFilterColor(color: Color) {
        _filterSettings.update { it.copy(color = color) }
    }

    fun updateScreenFilterStrength(strength: Float) {
        _filterSettings.update { it.copy(strength = strength.coerceIn(0.02f, 0.70f)) }
    }

    // ---- Server Ping Actions ----
    fun pingServer(id: String) {
        val server = _servers.value.find { it.id == id } ?: return
        _servers.update { list ->
            list.map { if (it.id == id) it.copy(isPinging = true) else it }
        }

        viewModelScope.launch {
            val res = RakNetPing.pingServer(server.host, server.port)
            _servers.update { list ->
                list.map {
                    if (it.id == id) it.copy(
                        pingMs = res.latencyMs,
                        isPinging = false,
                        motd = if (res.motd.isNotBlank()) res.motd else it.motd,
                        playerCount = res.players
                    ) else it
                }
            }
        }
    }

    fun pingAllServers() {
        val current = _servers.value
        _servers.update { list -> list.map { it.copy(isPinging = true) } }
        viewModelScope.launch {
            current.forEach { server ->
                launch {
                    val res = RakNetPing.pingServer(server.host, server.port)
                    _servers.update { list ->
                        list.map {
                            if (it.id == server.id) it.copy(
                                pingMs = res.latencyMs,
                                isPinging = false,
                                motd = if (res.motd.isNotBlank()) res.motd else it.motd,
                                playerCount = res.players
                            ) else it
                        }
                    }
                }
            }
        }
    }

    fun addCustomServer(name: String, address: String) {
        val parts = address.trim().split(":")
        val host = parts[0]
        val port = if (parts.size > 1) parts[1].toIntOrNull() ?: 19132 else 19132
        val newServer = BedrockServerInfo(
            id = "custom_${System.currentTimeMillis()}",
            name = name.ifBlank { host },
            host = host,
            port = port,
            isOfficialFeatured = false
        )
        _servers.update { listOf(newServer) + it }
        pingServer(newServer.id)
    }

    // ---- CPS Trainer Logic ----
    private var cpsJob: Job? = null
    private val tapTimestamps = mutableListOf<Long>()

    fun setCpsTimer(seconds: Int) {
        if (!_cpsRunning.value) {
            _cpsTimerSeconds.value = seconds
            _cpsRemainingSeconds.value = seconds
        }
    }

    fun recordCpsTap() {
        val now = SystemClock.uptimeMillis()
        tapTimestamps.add(now)

        // Haptic feedback
        vibrateClick()

        if (!_cpsRunning.value) {
            // Start test automatically on first tap
            startCpsTest()
        }

        _cpsTapsCount.update { it + 1 }

        // Filter last 1000ms
        tapTimestamps.removeAll { now - it > 1000L }
        val cps = tapTimestamps.size.toFloat()
        _currentCps.value = cps

        if (cps > _highestCps.value) {
            _highestCps.value = cps
            prefs.edit().putFloat("highest_cps", cps).apply()
        }
    }

    private fun startCpsTest() {
        _cpsRunning.value = true
        _cpsTapsCount.value = 1
        _cpsRemainingSeconds.value = _cpsTimerSeconds.value
        tapTimestamps.clear()
        tapTimestamps.add(SystemClock.uptimeMillis())

        cpsJob?.cancel()
        cpsJob = viewModelScope.launch {
            val totalSeconds = _cpsTimerSeconds.value
            for (sec in totalSeconds downTo 1) {
                _cpsRemainingSeconds.value = sec
                delay(1000L)
            }
            _cpsRemainingSeconds.value = 0
            _cpsRunning.value = false

            // Compute final average
            val totalTaps = _cpsTapsCount.value
            val avg = if (totalSeconds > 0) totalTaps.toFloat() / totalSeconds else 0f
            _cpsHistory.update { listOf(avg) + it.take(9) }
            _userMessage.value = "Test complete! Total taps: $totalTaps (${String.format(Locale.US, "%.1f", avg)} CPS avg)"
        }
    }

    fun resetCpsTest() {
        cpsJob?.cancel()
        _cpsRunning.value = false
        _cpsTapsCount.value = 0
        _currentCps.value = 0f
        _cpsRemainingSeconds.value = _cpsTimerSeconds.value
        tapTimestamps.clear()
    }

    private fun vibrateClick() {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(12, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(12)
            }
        } catch (_: Exception) {}
    }

    // ---- Profiles & Config JSON Export ----
    private fun loadProfiles() {
        val savedJson = prefs.getString("saved_profiles", null)
        if (savedJson != null) {
            try {
                val array = JSONArray(savedJson)
                val list = mutableListOf<ClientProfile>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        ClientProfile(
                            id = obj.getString("id"),
                            name = obj.getString("name"),
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
                _profiles.value = list
            } catch (_: Exception) {
                setupDefaultProfiles()
            }
        } else {
            setupDefaultProfiles()
        }
    }

    private fun setupDefaultProfiles() {
        val presets = listOf(
            ClientProfile("pvp", "Competitive Bedwars / PvP", activeModules = listOf("fps", "cps", "crosshair", "ping")),
            ClientProfile("builder", "Survival & Blueprint Builder", activeModules = listOf("fps", "clock", "battery", "reference_image")),
            ClientProfile("streamer", "Streamer Minimalist", activeModules = listOf("fps", "crosshair"))
        )
        _profiles.value = presets
    }

    fun saveCurrentProfile(name: String) {
        val newProfile = ClientProfile(
            id = "prof_${System.currentTimeMillis()}",
            name = name,
            crosshair = _crosshairSettings.value,
            filter = _filterSettings.value,
            activeModules = _hudModules.value.filter { it.enabled }.map { it.id }
        )
        _profiles.update { listOf(newProfile) + it }
        saveProfilesToPrefs()
        _userMessage.value = "Profile '$name' saved!"
    }

    fun loadProfile(profile: ClientProfile) {
        _hudModules.update { list ->
            list.map { it.copy(enabled = profile.activeModules.contains(it.id)) }
        }
        _crosshairSettings.value = profile.crosshair
        _filterSettings.value = profile.filter
        _userMessage.value = "Loaded preset: ${profile.name}"
    }

    fun deleteProfile(id: String) {
        _profiles.update { list -> list.filter { it.id != id } }
        saveProfilesToPrefs()
    }

    private fun saveProfilesToPrefs() {
        val arr = JSONArray()
        _profiles.value.forEach { p ->
            val obj = JSONObject()
            obj.put("id", p.id)
            obj.put("name", p.name)
            obj.put("createdAt", p.createdAt)
            arr.put(obj)
        }
        prefs.edit().putString("saved_profiles", arr.toString()).apply()
    }

    // Export format compatible with MobileClient's ModuleManager snapshot
    fun exportConfigJson(): String {
        val root = JSONObject()
        val modulesObj = JSONObject()
        _hudModules.value.forEach { m ->
            val modJson = JSONObject()
            modJson.put("enabled", m.enabled)
            modJson.put("x", m.posX)
            modJson.put("y", m.posY)
            modulesObj.put(m.id, modJson)
        }
        root.put("modules", modulesObj)

        val chObj = JSONObject()
        val ch = _crosshairSettings.value
        chObj.put("shape", ch.shape)
        chObj.put("size", ch.size)
        chObj.put("thick", ch.thickness)
        chObj.put("gap", ch.gap)
        chObj.put("color", ch.color.toArgb())
        root.put("crosshair", chObj)

        root.put("client_version", "1.0.0")
        root.put("exported_at", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date()))
        return root.toString(2)
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    // Live device metrics for realistic simulation in preview
    fun getDeviceBatteryPercent(): Int {
        val ctx = getApplication<Application>()
        val bm = ctx.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        return bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: 85
    }

    fun getProcessRamMb(): Int {
        val mi = Debug.MemoryInfo()
        Debug.getMemoryInfo(mi)
        return (mi.totalPss / 1024).coerceAtLeast(42)
    }
}
