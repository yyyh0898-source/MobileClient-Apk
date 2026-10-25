package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.CompanionViewModel
import com.example.data.CrosshairSettings
import com.example.data.HudModuleItem
import com.example.data.ModuleCategory
import com.example.ui.components.McCard
import com.example.ui.components.McSliderRow
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HudStudioScreen(
    viewModel: CompanionViewModel,
    modifier: Modifier = Modifier
) {
    val modules by viewModel.hudModules.collectAsState()
    val crosshair by viewModel.crosshairSettings.collectAsState()
    val filter by viewModel.filterSettings.collectAsState()
    val backdrop by viewModel.previewBackdrop.collectAsState()

    var selectedCategory by remember { mutableStateOf<ModuleCategory?>(null) }
    var expandedModuleId by remember { mutableStateOf<String?>(null) }

    val backdrops = listOf("Grassland", "Nether", "The End", "Night Battle")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(McBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
    ) {
        // Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "HUD STUDIO",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = McAccent,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Real-time in-game overlay sandbox & visual layout editor",
                        fontSize = 12.sp,
                        color = McMuted
                    )
                }
                StatusBadge(text = "${modules.count { it.enabled }} Active", color = McSuccess)
            }
        }

        // Live Minecraft In-Game Viewport Preview
        item {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .border(2.dp, McStroke, RoundedCornerShape(14.dp))
                    .testTag("hud_preview_viewport")
            ) {
                val boxWidth = maxWidth
                val boxHeight = maxHeight

                // Simulated Minecraft Environment Background
                val backdropBrush = when (backdrop) {
                    "Nether" -> Brush.verticalGradient(
                        listOf(Color(0xFF4A1010), Color(0xFF260505), Color(0xFF140202))
                    )
                    "The End" -> Brush.verticalGradient(
                        listOf(Color(0xFF1B0B2E), Color(0xFF0C0514), Color(0xFF030105))
                    )
                    "Night Battle" -> Brush.verticalGradient(
                        listOf(Color(0xFF081426), Color(0xFF030811), Color(0xFF020408))
                    )
                    else -> Brush.verticalGradient( // Grassland
                        listOf(Color(0xFF2E6F40), Color(0xFF1D4A2B), Color(0xFF102817))
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(backdropBrush)
                )

                // Optional Screen Filter Overlay preview
                if (filter.enabled) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(filter.color.copy(alpha = filter.strength))
                    )
                }

                // Subtitle watermark in viewport
                Text(
                    text = "MINECRAFT BEDROCK - LIVE HUD PREVIEW",
                    fontSize = 9.sp,
                    color = Color.White.copy(alpha = 0.35f),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 6.dp)
                )

                // Crosshair in center
                val chModule = modules.find { it.id == "crosshair" }
                if (chModule?.enabled == true) {
                    CrosshairCanvas(
                        settings = crosshair,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(60.dp)
                    )
                }

                // Render dynamic draggable HUD Modules
                modules.filter { it.enabled && it.id != "crosshair" && it.id != "screen_filter" }.forEach { mod ->
                    val posX = (boxWidth.value * mod.posX).dp
                    val posY = (boxHeight.value * mod.posY).dp

                    Box(
                        modifier = Modifier
                            .offset(x = posX, y = posY)
                            .pointerInput(mod.id) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    val newX = (mod.posX + dragAmount.x / (boxWidth.value * 2.5f)).coerceIn(0.02f, 0.85f)
                                    val newY = (mod.posY + dragAmount.y / (boxHeight.value * 2.5f)).coerceIn(0.02f, 0.85f)
                                    viewModel.updateModulePosition(mod.id, newX, newY)
                                }
                            }
                            .clip(RoundedCornerShape(6.dp))
                            .background(McBackground.copy(alpha = 0.80f))
                            .border(1.dp, McAccent.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = getPreviewModuleText(mod, viewModel),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = McText
                        )
                    }
                }

                // Floating Menu Button representation
                val clientMod = modules.find { it.id == "client" }
                if (clientMod?.enabled == true) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 8.dp)
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(McAccent.copy(alpha = 0.9f))
                            .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("☰", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Environment Backdrop Switcher
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "PREVIEW ENVIRONMENT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = McMuted,
                    letterSpacing = 1.sp
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(backdrops) { item ->
                        val isSelected = backdrop == item
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setPreviewBackdrop(item) },
                            label = { Text(item, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = McAccent,
                                selectedLabelColor = Color.White,
                                containerColor = McCard,
                                labelColor = McMuted
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) McAccent else McStroke
                            )
                        )
                    }
                }
            }
        }

        // Category Filter Chips
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { selectedCategory = null },
                    label = { Text("All Modules (${modules.size})", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = McChip,
                        selectedLabelColor = McAccent,
                        containerColor = McCard,
                        labelColor = McMuted
                    )
                )
                ModuleCategory.values().forEach { cat ->
                    val isSelected = selectedCategory == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat.label, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = McChip,
                            selectedLabelColor = McAccent,
                            containerColor = McCard,
                            labelColor = McMuted
                        )
                    )
                }
            }
        }

        // Modules List
        val filtered = if (selectedCategory == null) modules else modules.filter { it.category == selectedCategory }

        items(filtered, key = { it.id }) { mod ->
            val isExpanded = expandedModuleId == mod.id

            McCard(
                padding = PaddingValues(14.dp),
                modifier = Modifier.testTag("module_card_${mod.id}")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = mod.name,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (mod.enabled) McText else McMuted
                            )
                            StatusBadge(
                                text = mod.category.label,
                                color = if (mod.enabled) McAccent else McOff
                            )
                        }
                        Text(
                            text = mod.description,
                            fontSize = 12.sp,
                            color = McMuted,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Switch(
                            checked = mod.enabled,
                            onCheckedChange = { viewModel.toggleModule(mod.id) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = McAccent,
                                uncheckedThumbColor = McMuted,
                                uncheckedTrackColor = McCard
                            ),
                            modifier = Modifier.testTag("switch_${mod.id}")
                        )

                        IconButton(
                            onClick = {
                                expandedModuleId = if (isExpanded) null else mod.id
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.Tune,
                                contentDescription = "Tune",
                                tint = if (isExpanded) McAccent else McMuted
                            )
                        }
                    }
                }

                // Expanded Configuration Controls
                if (isExpanded) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = McStroke, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    if (mod.id != "crosshair" && mod.id != "screen_filter") {
                        Text(
                            text = "HUD POSITION (DRAG OR SLIDE)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = McMuted,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        McSliderRow(
                            label = "Horizontal (X)",
                            value = mod.posX * 100f,
                            onValueChange = { viewModel.updateModulePosition(mod.id, it / 100f, mod.posY) },
                            valueRange = 1f..95f,
                            unit = "%"
                        )
                        McSliderRow(
                            label = "Vertical (Y)",
                            value = mod.posY * 100f,
                            onValueChange = { viewModel.updateModulePosition(mod.id, mod.posX, it / 100f) },
                            valueRange = 1f..95f,
                            unit = "%"
                        )
                    } else if (mod.id == "screen_filter") {
                        Text(
                            text = "FILTER INTENSITY & WARMTH",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = McMuted,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        McSliderRow(
                            label = "Strength",
                            value = filter.strength * 100f,
                            onValueChange = { viewModel.updateScreenFilterStrength(it / 100f) },
                            valueRange = 2f..70f,
                            unit = "%"
                        )
                    } else {
                        Text(
                            text = "Head to the Crosshair tab for deep shape & reticle customization.",
                            fontSize = 12.sp,
                            color = McAccent
                        )
                    }
                }
            }
        }
    }
}

private fun getPreviewModuleText(mod: HudModuleItem, vm: CompanionViewModel): String = when (mod.id) {
    "fps" -> "FPS: 60"
    "cps" -> "CPS: 0"
    "clock" -> SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    "battery" -> "Battery: ${vm.getDeviceBatteryPercent()}% +"
    "memory" -> "RAM: ${vm.getProcessRamMb()} MB"
    "ping" -> "Ping: 28 ms"
    "reference_image" -> "[Blueprint: guide.png]"
    else -> mod.name
}

@Composable
fun CrosshairCanvas(
    settings: CrosshairSettings,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val sz = settings.size * density
        val gp = settings.gap * density
        val th = settings.thickness * density

        for (pass in 0..1) {
            val isOutline = pass == 0
            val drawColor = if (isOutline) Color.Black.copy(alpha = 0.8f) else settings.color
            val strokeW = if (isOutline) th + (2f * density) else th

            when (settings.shape) {
                "Dot" -> {
                    drawCircle(
                        color = drawColor,
                        radius = (th * 1.5f) + (if (isOutline) density else 0f),
                        center = Offset(cx, cy)
                    )
                }
                "Circle" -> {
                    drawCircle(
                        color = drawColor,
                        radius = (sz / 2f) + gp,
                        center = Offset(cx, cy),
                        style = Stroke(width = strokeW)
                    )
                }
                "Cross X" -> {
                    val k = 0.7071f
                    drawLine(drawColor, Offset(cx - gp * k, cy - gp * k), Offset(cx - (gp + sz) * k, cy - (gp + sz) * k), strokeW)
                    drawLine(drawColor, Offset(cx + gp * k, cy - gp * k), Offset(cx + (gp + sz) * k, cy - (gp + sz) * k), strokeW)
                    drawLine(drawColor, Offset(cx - gp * k, cy + gp * k), Offset(cx - (gp + sz) * k, cy + (gp + sz) * k), strokeW)
                    drawLine(drawColor, Offset(cx + gp * k, cy + gp * k), Offset(cx + (gp + sz) * k, cy + (gp + sz) * k), strokeW)
                }
                else -> { // Plus
                    drawLine(drawColor, Offset(cx - gp - sz, cy), Offset(cx - gp, cy), strokeW)
                    drawLine(drawColor, Offset(cx + gp, cy), Offset(cx + gp + sz, cy), strokeW)
                    drawLine(drawColor, Offset(cx, cy - gp - sz), Offset(cx, cy - gp), strokeW)
                    drawLine(drawColor, Offset(cx, cy + gp), Offset(cx, cy + gp + sz), strokeW)
                }
            }
        }
    }
}
