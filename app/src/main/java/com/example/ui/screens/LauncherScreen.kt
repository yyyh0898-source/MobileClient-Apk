package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.CompanionViewModel
import com.example.R
import com.example.ui.components.McCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@Composable
fun LauncherScreen(
    viewModel: CompanionViewModel,
    onNavigateToHud: () -> Unit,
    onNavigateToServers: () -> Unit,
    onNavigateToPatcher: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gameStatus by viewModel.gameStatus.collectAsState()
    val selectedMode by viewModel.selectedMode.collectAsState()
    val modules by viewModel.hudModules.collectAsState()
    val highestCps by viewModel.highestCps.collectAsState()
    val servers by viewModel.servers.collectAsState()

    var showPkgDialog by remember { mutableStateOf(false) }
    var editPkgText by remember { mutableStateOf(gameStatus.packageName) }

    val activeModulesCount = modules.count { it.enabled }
    val bestPing = servers.map { it.pingMs }.filter { it > 0 }.minOrNull() ?: -1

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(McBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, McStroke, RoundedCornerShape(16.dp))
        ) {
            Image(
                painter = painterResource(id = R.drawable.mcmobile_hero),
                contentDescription = "MC Mobile Hero Banner",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, McBackground.copy(alpha = 0.92f))
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "MOBILE CLIENT",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = McAccent,
                        letterSpacing = 1.sp
                    )
                    StatusBadge(text = "v1.0.0", color = McAccent)
                }
                Text(
                    text = "Quality-of-life client mod & HUD companion for Minecraft Bedrock",
                    fontSize = 12.sp,
                    color = McMuted
                )
            }
        }

        // Installation Status Card
        McCard(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("status_card")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = if (gameStatus.isInstalled) Icons.Default.SportsEsports else Icons.Default.WarningAmber,
                        contentDescription = "Game Status Icon",
                        tint = if (gameStatus.isInstalled) McSuccess else McWarning,
                        modifier = Modifier.size(28.dp)
                    )
                    Column {
                        Text(
                            text = if (gameStatus.isInstalled) "Minecraft ${gameStatus.versionName}" else "Minecraft Not Detected",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = McText
                        )
                        Text(
                            text = gameStatus.packageName,
                            fontSize = 12.sp,
                            color = McMuted
                        )
                    }
                }
                TextButton(
                    onClick = {
                        editPkgText = gameStatus.packageName
                        showPkgDialog = true
                    },
                    modifier = Modifier.testTag("change_package_button")
                ) {
                    Text("Change", color = McAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = McStroke, thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = if (gameStatus.isClientPatched) Icons.Default.CheckCircle else Icons.Default.Info,
                        contentDescription = "Patch Status",
                        tint = if (gameStatus.isClientPatched) McSuccess else McMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (gameStatus.isClientPatched) "Client patch detected in APK" else "Original Minecraft (Unpatched)",
                        fontSize = 12.sp,
                        color = if (gameStatus.isClientPatched) McSuccess else McMuted
                    )
                }
                IconButton(
                    onClick = { viewModel.refreshGameStatus() },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh Status",
                        tint = McAccentLight,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Mode Selector: Vanilla PE vs Mc-Mobile Patched
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "START MODE",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = McMuted,
                letterSpacing = 1.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Vanilla Mode Tile
                val isVanilla = selectedMode == "vanilla"
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(McCard)
                        .border(
                            width = if (isVanilla) 2.dp else 1.dp,
                            color = if (isVanilla) McAccent else McStroke,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { viewModel.setSelectedMode("vanilla") }
                        .padding(14.dp)
                        .testTag("mode_vanilla_tile")
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            RadioButton(
                                selected = isVanilla,
                                onClick = { viewModel.setSelectedMode("vanilla") },
                                colors = RadioButtonDefaults.colors(selectedColor = McAccent),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Minecraft PE",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = McText
                            )
                        }
                        Text(
                            text = "Original vanilla build. Client stays dormant.",
                            fontSize = 11.sp,
                            color = McMuted,
                            lineHeight = 15.sp
                        )
                    }
                }

                // Patch Mode Tile
                val isPatch = selectedMode == "patch"
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(McCard)
                        .border(
                            width = if (isPatch) 2.dp else 1.dp,
                            color = if (isPatch) McAccent else McStroke,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { viewModel.setSelectedMode("patch") }
                        .padding(14.dp)
                        .testTag("mode_patch_tile")
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            RadioButton(
                                selected = isPatch,
                                onClick = { viewModel.setSelectedMode("patch") },
                                colors = RadioButtonDefaults.colors(selectedColor = McAccent),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Patch Client",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = McAccent
                            )
                        }
                        Text(
                            text = "Full client with ☰ menu, HUD stats & crosshair.",
                            fontSize = 11.sp,
                            color = McMuted,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }

        // Unpatched warning
        if (selectedMode == "patch" && gameStatus.isInstalled && !gameStatus.isClientPatched) {
            McCard(
                backgroundColor = McPanel,
                borderColor = McWarning.copy(alpha = 0.5f),
                padding = PaddingValues(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Warning",
                        tint = McWarning
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Client patch not yet injected into this APK",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = McWarning
                        )
                        Text(
                            text = "The game will start in vanilla mode. Check the Patcher Guide for instructions.",
                            fontSize = 11.sp,
                            color = McMuted
                        )
                    }
                    TextButton(onClick = onNavigateToPatcher) {
                        Text("Guide", color = McAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Primary Play Button
        Button(
            onClick = { viewModel.launchMinecraft() },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("play_game_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = McAccent,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = if (selectedMode == "patch") "PLAY CLIENT" else "PLAY MINECRAFT",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }
        }

        // Quick Stats Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // HUD Modules Count Card
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(McCard)
                    .border(1.dp, McStroke, RoundedCornerShape(12.dp))
                    .clickable { onNavigateToHud() }
                    .padding(12.dp)
            ) {
                Column {
                    Text("ACTIVE HUD", fontSize = 10.sp, color = McMuted, fontWeight = FontWeight.Bold)
                    Text("$activeModulesCount Modules", fontSize = 14.sp, color = McText, fontWeight = FontWeight.Bold)
                    Text("Customize →", fontSize = 11.sp, color = McAccent)
                }
            }

            // Server Ping Card
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(McCard)
                    .border(1.dp, McStroke, RoundedCornerShape(12.dp))
                    .clickable { onNavigateToServers() }
                    .padding(12.dp)
            ) {
                Column {
                    Text("BEST PING", fontSize = 10.sp, color = McMuted, fontWeight = FontWeight.Bold)
                    Text(if (bestPing > 0) "$bestPing ms" else "Check Ping", fontSize = 14.sp, color = if (bestPing > 0) McSuccess else McText, fontWeight = FontWeight.Bold)
                    Text("Server List →", fontSize = 11.sp, color = McAccent)
                }
            }

            // Max CPS Card
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(McCard)
                    .border(1.dp, McStroke, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Text("MAX CPS", fontSize = 10.sp, color = McMuted, fontWeight = FontWeight.Bold)
                    Text(if (highestCps > 0) String.format(java.util.Locale.US, "%.1f", highestCps) else "0.0", fontSize = 14.sp, color = McAccentLight, fontWeight = FontWeight.Bold)
                    Text("Taps / Sec", fontSize = 11.sp, color = McMuted)
                }
            }
        }

        // Instructions footer
        McCard(padding = PaddingValues(14.dp)) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.TouchApp,
                    contentDescription = "Tap Tip",
                    tint = McAccent,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "In game: tap the floating ☰ button to open the client click menu. If Minecraft is already running in background, close it first to switch modes.",
                    fontSize = 12.sp,
                    color = McMuted,
                    lineHeight = 16.sp
                )
            }
        }

        Text(
            text = "Not an official Minecraft product. Not approved by or associated with Mojang or Microsoft. Not affiliated with MarkClient.",
            fontSize = 10.sp,
            color = McMuted.copy(alpha = 0.6f),
            lineHeight = 14.sp,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
        )
    }

    // Change Package Dialog
    if (showPkgDialog) {
        AlertDialog(
            onDismissRequest = { showPkgDialog = false },
            title = { Text("Minecraft Package Name", color = McText, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Standard: com.mojang.minecraftpe\nBeta: com.mojang.minecraftpe.beta",
                        fontSize = 12.sp,
                        color = McMuted
                    )
                    OutlinedTextField(
                        value = editPkgText,
                        onValueChange = { editPkgText = it },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = McAccent,
                            unfocusedBorderColor = McStroke,
                            focusedTextColor = McText,
                            unfocusedTextColor = McText
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("package_name_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.setPackageName(editPkgText)
                        showPkgDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = McAccent)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPkgDialog = false }) {
                    Text("Cancel", color = McMuted)
                }
            },
            containerColor = McPanel
        )
    }
}
