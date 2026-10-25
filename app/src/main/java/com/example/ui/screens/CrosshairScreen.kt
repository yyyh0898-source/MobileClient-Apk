package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.CompanionViewModel
import com.example.ui.components.ColorPickerRow
import com.example.ui.components.McCard
import com.example.ui.components.McSliderRow
import com.example.ui.theme.*

@Composable
fun CrosshairScreen(
    viewModel: CompanionViewModel,
    modifier: Modifier = Modifier
) {
    val crosshair by viewModel.crosshairSettings.collectAsState()
    val shapes = listOf("Plus", "Dot", "Circle", "Cross X")

    var testBackdrop by remember { mutableStateOf("Cave") }
    val backdrops = listOf("Cave", "Grass", "Nether", "End Void")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(McBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Column {
            Text(
                text = "CROSSHAIR STUDIO",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = McAccent,
                letterSpacing = 1.sp
            )
            Text(
                text = "Pixel-accurate reticle designer for competitive Minecraft Bedrock PvP",
                fontSize = 12.sp,
                color = McMuted
            )
        }

        // Crosshair High-Resolution Preview Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(2.dp, McStroke, RoundedCornerShape(16.dp))
                .testTag("crosshair_preview_box")
        ) {
            val bgBrush = when (testBackdrop) {
                "Cave" -> Brush.radialGradient(
                    listOf(Color(0xFF241D17), Color(0xFF0F0B08), Color(0xFF000000))
                )
                "Grass" -> Brush.verticalGradient(
                    listOf(Color(0xFF4C935E), Color(0xFF285E37), Color(0xFF14301C))
                )
                "Nether" -> Brush.verticalGradient(
                    listOf(Color(0xFF91241C), Color(0xFF4C100C), Color(0xFF1C0503))
                )
                else -> Brush.verticalGradient( // End Void
                    listOf(Color(0xFF2E124D), Color(0xFF130621), Color(0xFF05010A))
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(bgBrush)
            )

            // Centered reticle
            CrosshairCanvas(
                settings = crosshair,
                modifier = Modifier
                    .size(140.dp)
                    .align(Alignment.Center)
            )

            // Backdrop selector overlay
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                backdrops.forEach { item ->
                    val isSel = testBackdrop == item
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSel) McAccent else McBackground.copy(alpha = 0.75f))
                            .border(1.dp, if (isSel) Color.White else McStroke, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = item,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSel) Color.White else McMuted
                        )
                    }
                }
            }
        }

        // Shape Selection
        McCard {
            Text(
                text = "RETICLE SHAPE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = McMuted,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(shapes) { shape ->
                    val isSelected = crosshair.shape == shape
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.updateCrosshairShape(shape) },
                        label = { Text(shape, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = McAccent,
                            selectedLabelColor = Color.White,
                            containerColor = McPanel,
                            labelColor = McText
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) McAccent else McStroke
                        ),
                        modifier = Modifier.testTag("shape_chip_$shape")
                    )
                }
            }
        }

        // Dimensions Sliders
        McCard {
            Text(
                text = "DIMENSIONS & SPACING",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = McMuted,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            McSliderRow(
                label = "Reticle Size",
                value = crosshair.size,
                onValueChange = { viewModel.updateCrosshairSize(it) },
                valueRange = 3f..40f,
                step = 1f,
                unit = "dp"
            )

            Spacer(modifier = Modifier.height(8.dp))
            McSliderRow(
                label = "Line Thickness",
                value = crosshair.thickness,
                onValueChange = { viewModel.updateCrosshairThickness(it) },
                valueRange = 1f..8f,
                step = 0.5f,
                unit = "dp"
            )

            Spacer(modifier = Modifier.height(8.dp))
            McSliderRow(
                label = "Center Gap",
                value = crosshair.gap,
                onValueChange = { viewModel.updateCrosshairGap(it) },
                valueRange = 0f..20f,
                step = 0.5f,
                unit = "dp"
            )
        }

        // Color Customization
        McCard {
            Text(
                text = "COLOR PALETTE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = McMuted,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            ColorPickerRow(
                selectedColor = crosshair.color,
                onColorSelected = { viewModel.updateCrosshairColor(it) }
            )
        }

        // Presets Quick Load
        McCard {
            Text(
                text = "POPULAR PVP PRESETS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = McMuted,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        viewModel.updateCrosshairShape("Dot")
                        viewModel.updateCrosshairSize(6f)
                        viewModel.updateCrosshairThickness(3.5f)
                        viewModel.updateCrosshairGap(0f)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Sniper Dot", fontSize = 11.sp)
                }

                OutlinedButton(
                    onClick = {
                        viewModel.updateCrosshairShape("Plus")
                        viewModel.updateCrosshairSize(10f)
                        viewModel.updateCrosshairThickness(2f)
                        viewModel.updateCrosshairGap(4f)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("CS Plus", fontSize = 11.sp)
                }

                OutlinedButton(
                    onClick = {
                        viewModel.updateCrosshairShape("Circle")
                        viewModel.updateCrosshairSize(14f)
                        viewModel.updateCrosshairThickness(2f)
                        viewModel.updateCrosshairGap(2f)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Circular", fontSize = 11.sp)
                }
            }
        }
    }
}
