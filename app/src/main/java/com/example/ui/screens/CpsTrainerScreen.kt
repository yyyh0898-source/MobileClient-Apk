package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.CompanionViewModel
import com.example.ui.components.McCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@Composable
fun CpsTrainerScreen(
    viewModel: CompanionViewModel,
    modifier: Modifier = Modifier
) {
    val timerSeconds by viewModel.cpsTimerSeconds.collectAsState()
    val remainingSeconds by viewModel.cpsRemainingSeconds.collectAsState()
    val isRunning by viewModel.cpsRunning.collectAsState()
    val tapsCount by viewModel.cpsTapsCount.collectAsState()
    val currentCps by viewModel.currentCps.collectAsState()
    val highestCps by viewModel.highestCps.collectAsState()
    val cpsHistory by viewModel.cpsHistory.collectAsState()

    val durations = listOf(5, 10, 30)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(McBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "CPS CLICK TRAINER",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = McAccent,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Practice touchscreen tap speed & hit registration for PvP",
                    fontSize = 12.sp,
                    color = McMuted
                )
            }
            StatusBadge(
                text = if (isRunning) "${remainingSeconds}s Left" else "Ready",
                color = if (isRunning) McSuccess else McAccent
            )
        }

        // Duration selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("DURATION:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = McMuted)
            durations.forEach { d ->
                val isSel = timerSeconds == d
                FilterChip(
                    selected = isSel,
                    onClick = { viewModel.setCpsTimer(d) },
                    label = { Text("${d}s", fontWeight = FontWeight.Bold) },
                    enabled = !isRunning,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = McAccent,
                        selectedLabelColor = Color.White,
                        containerColor = McCard,
                        labelColor = McText
                    ),
                    modifier = Modifier.testTag("duration_${d}s_chip")
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            IconButton(
                onClick = { viewModel.resetCpsTest() },
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(McCard)
                    .border(1.dp, McStroke, RoundedCornerShape(8.dp))
                    .testTag("reset_cps_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset",
                    tint = McMuted
                )
            }
        }

        // Stats Dashboard Card
        McCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("CURRENT CPS", fontSize = 11.sp, color = McMuted, fontWeight = FontWeight.Bold)
                    Text(
                        text = String.format(java.util.Locale.US, "%.1f", currentCps),
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        color = when {
                            currentCps >= 15f -> McSuccess
                            currentCps >= 10f -> McAccent
                            currentCps >= 6f -> McWarning
                            else -> McText
                        }
                    )
                }

                Divider(
                    modifier = Modifier
                        .height(36.dp)
                        .width(1.dp),
                    color = McStroke
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("TOTAL TAPS", fontSize = 11.sp, color = McMuted, fontWeight = FontWeight.Bold)
                    Text(
                        text = "$tapsCount",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        color = McText
                    )
                }

                Divider(
                    modifier = Modifier
                        .height(36.dp)
                        .width(1.dp),
                    color = McStroke
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("BEST CPS", fontSize = 11.sp, color = McMuted, fontWeight = FontWeight.Bold)
                    Text(
                        text = String.format(java.util.Locale.US, "%.1f", highestCps),
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        color = McGold
                    )
                }
            }
        }

        // Big Click Tap Pad
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(if (isRunning) McPanel else McCard)
                .border(
                    width = if (isRunning) 2.dp else 1.dp,
                    color = if (isRunning) McAccent else McStroke,
                    shape = RoundedCornerShape(20.dp)
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(color = McAccentLight)
                ) {
                    viewModel.recordCpsTap()
                }
                .testTag("cps_tap_pad"),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(McAccent.copy(alpha = 0.15f))
                        .border(1.dp, McAccent, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.TouchApp,
                        contentDescription = "Tap",
                        tint = McAccent,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Text(
                    text = if (isRunning) "TAP AS FAST AS YOU CAN!" else "TAP TO START TEST",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isRunning) McAccent else McText,
                    letterSpacing = 1.sp
                )

                Text(
                    text = if (isRunning) "${remainingSeconds}s remaining" else "Uses 2-finger butterfly or jitter taps",
                    fontSize = 12.sp,
                    color = McMuted
                )
            }
        }

        // Recent Runs History
        if (cpsHistory.isNotEmpty()) {
            McCard {
                Text(
                    text = "RECENT RUN AVERAGES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = McMuted,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    cpsHistory.take(5).forEachIndexed { index, avg ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(McChip)
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("#${index + 1}", fontSize = 10.sp, color = McMuted)
                                Text(
                                    text = String.format(java.util.Locale.US, "%.1f", avg),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = McAccentLight
                                )
                            }
                        }
                    }
                }
            }
        }

        // Mobile PvP Clicking Guide
        McCard {
            Text(
                text = "MOBILE TOUCH PVP TIPS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = McMuted,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PvpTipItem("Butterfly Tap (12-16 CPS)", "Alternate tapping with index and middle finger on the right screen half.")
                PvpTipItem("Hit Selection & Comboing", "Time hits when opponent is airborne to maximize knockback on Bedrock.")
                PvpTipItem("Client Settings", "In Mc-Mobile settings, 'Count right half only' prevents left-hand movement taps from polluting your CPS meter.")
            }
        }
    }
}

@Composable
fun PvpTipItem(title: String, desc: String) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .size(6.dp)
                .clip(CircleShape)
                .background(McAccent)
        )
        Column {
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = McText)
            Text(desc, fontSize = 11.sp, color = McMuted, lineHeight = 15.sp)
        }
    }
}
