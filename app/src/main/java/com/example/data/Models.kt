package com.example.data

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.McPaletteColors

enum class ModuleCategory(val label: String) {
    HUD("HUD"),
    VISUAL("Visual"),
    CLIENT("Client")
}

data class HudModuleItem(
    val id: String,
    val name: String,
    val description: String,
    val category: ModuleCategory,
    val enabled: Boolean,
    val posX: Float = 0.05f, // 0..1 normalized viewport coordinate
    val posY: Float = 0.05f,
    val customText: String? = null
)

data class CrosshairSettings(
    val shape: String = "Plus", // Plus, Dot, Circle, Cross X
    val size: Float = 12f,      // dp (3..40)
    val thickness: Float = 2.5f,// dp (1..6)
    val gap: Float = 3.5f,      // dp (0..20)
    val color: Color = Color(0xFF3A9BDC),
    val hasOutline: Boolean = true
)

data class ScreenFilterSettings(
    val enabled: Boolean = false,
    val color: Color = Color(0xFFFFA040),
    val strength: Float = 0.25f // 0.02 .. 0.70
)

data class BedrockServerInfo(
    val id: String,
    val name: String,
    val host: String,
    val port: Int = 19132,
    val isOfficialFeatured: Boolean = false,
    val pingMs: Int = -1,
    val isPinging: Boolean = false,
    val motd: String = "",
    val playerCount: String = ""
)

data class ClientProfile(
    val id: String,
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
    val crosshair: CrosshairSettings = CrosshairSettings(),
    val filter: ScreenFilterSettings = ScreenFilterSettings(),
    val activeModules: List<String> = listOf("fps", "cps", "crosshair", "battery", "clock")
)

data class GameInstallStatus(
    val packageName: String = "com.mojang.minecraftpe",
    val isInstalled: Boolean = false,
    val versionName: String = "",
    val isClientPatched: Boolean = false,
    val statusMessage: String = "Detecting installation..."
)
