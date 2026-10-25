package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.CompanionViewModel
import com.example.data.BedrockServerInfo
import com.example.ui.components.McCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@Composable
fun ServersScreen(
    viewModel: CompanionViewModel,
    modifier: Modifier = Modifier
) {
    val servers by viewModel.servers.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var newServerName by remember { mutableStateOf("") }
    var newServerAddr by remember { mutableStateOf("") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(McBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "BEDROCK SERVERS",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = McAccent,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "RakNet unconnected UDP latency and status monitor",
                        fontSize = 12.sp,
                        color = McMuted
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton(
                        onClick = { viewModel.pingAllServers() },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(McCard)
                            .border(1.dp, McStroke, RoundedCornerShape(8.dp))
                            .testTag("ping_all_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Ping All",
                            tint = McAccent
                        )
                    }

                    Button(
                        onClick = { showAddDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = McAccent),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        modifier = Modifier
                            .height(38.dp)
                            .testTag("add_server_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Info Banner
        item {
            McCard(
                backgroundColor = McPanel,
                padding = PaddingValues(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Sensors,
                        contentDescription = "RakNet",
                        tint = McAccent
                    )
                    Text(
                        text = "Uses genuine RakNet UDP ping packets (0x01) — the exact protocol Bedrock uses for the multiplayer server list.",
                        fontSize = 11.sp,
                        color = McMuted,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        // Server Cards List
        items(servers, key = { it.id }) { server ->
            ServerCard(
                server = server,
                onPing = { viewModel.pingServer(server.id) }
            )
        }
    }

    // Add Server Dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Bedrock Server", color = McText, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newServerName,
                        onValueChange = { newServerName = it },
                        label = { Text("Server Name", fontSize = 12.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = McAccent,
                            unfocusedBorderColor = McStroke,
                            focusedTextColor = McText,
                            unfocusedTextColor = McText
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("custom_server_name_input")
                    )
                    OutlinedTextField(
                        value = newServerAddr,
                        onValueChange = { newServerAddr = it },
                        label = { Text("Address (e.g. bds.example.com:19132)", fontSize = 12.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = McAccent,
                            unfocusedBorderColor = McStroke,
                            focusedTextColor = McText,
                            unfocusedTextColor = McText
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("custom_server_address_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newServerAddr.isNotBlank()) {
                            viewModel.addCustomServer(newServerName, newServerAddr)
                            newServerName = ""
                            newServerAddr = ""
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = McAccent)
                ) {
                    Text("Add & Ping")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel", color = McMuted)
                }
            },
            containerColor = McPanel
        )
    }
}

@Composable
fun ServerCard(
    server: BedrockServerInfo,
    onPing: () -> Unit
) {
    val pingColor = when {
        server.pingMs in 0..60 -> McSuccess
        server.pingMs in 61..120 -> McAccent
        server.pingMs in 121..200 -> McWarning
        server.pingMs > 200 -> McDanger
        else -> McMuted
    }

    val pingText = when {
        server.isPinging -> "Pinging..."
        server.pingMs >= 0 -> "${server.pingMs} ms"
        else -> "Timeout"
    }

    McCard(
        padding = PaddingValues(14.dp),
        modifier = Modifier.testTag("server_card_${server.id}")
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
                        text = server.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = McText
                    )
                    if (server.isOfficialFeatured) {
                        StatusBadge(text = "Official", color = McGold)
                    }
                }
                Text(
                    text = "${server.host}:${server.port}",
                    fontSize = 12.sp,
                    color = McMuted,
                    modifier = Modifier.padding(top = 2.dp)
                )
                if (server.motd.isNotBlank()) {
                    Text(
                        text = server.motd,
                        fontSize = 11.sp,
                        color = McAccentLight,
                        maxLines = 1,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Ping latency pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(pingColor.copy(alpha = 0.15f))
                        .border(1.dp, pingColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .clickable { onPing() },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (server.isPinging) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(10.dp),
                            color = McAccent,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(pingColor)
                        )
                    }
                    Text(
                        text = pingText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = pingColor
                    )
                }

                if (server.playerCount.isNotBlank()) {
                    Text(
                        text = "Players: ${server.playerCount}",
                        fontSize = 10.sp,
                        color = McMuted
                    )
                }
            }
        }
    }
}
