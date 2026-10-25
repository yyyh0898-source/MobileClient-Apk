package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.CompanionViewModel
import com.example.data.ClientProfile
import com.example.ui.components.McCard
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProfilesScreen(
    viewModel: CompanionViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val profiles by viewModel.profiles.collectAsState()

    var showSaveDialog by remember { mutableStateOf(false) }
    var newProfileName by remember { mutableStateOf("") }
    var showExportDialog by remember { mutableStateOf(false) }
    var exportedJsonText by remember { mutableStateOf("") }

    fun copyToClipboard(text: String, label: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
    }

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
                        text = "CLIENT PROFILES",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = McAccent,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Save, load, and export modular presets for Mc-Mobile",
                        fontSize = 12.sp,
                        color = McMuted
                    )
                }

                Button(
                    onClick = { showSaveDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = McAccent),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    modifier = Modifier.height(38.dp).testTag("save_profile_button")
                ) {
                    Icon(Icons.Default.BookmarkAdd, contentDescription = "Save", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Save Current", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Export Config Banner
        item {
            McCard(backgroundColor = McPanel) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Export Client Config (JSON)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = McText
                        )
                        Text(
                            text = "Directly compatible with MobileClient's ModuleManager snapshot format",
                            fontSize = 11.sp,
                            color = McMuted
                        )
                    }
                    Button(
                        onClick = {
                            exportedJsonText = viewModel.exportConfigJson()
                            showExportDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = McChip),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("export_json_button")
                    ) {
                        Text("Export", color = McAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Profiles List
        items(profiles, key = { it.id }) { profile ->
            ProfileItemCard(
                profile = profile,
                onLoad = { viewModel.loadProfile(profile) },
                onDelete = { viewModel.deleteProfile(profile.id) }
            )
        }
    }

    // Save Profile Dialog
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Save Current Setup", color = McText, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Saves active HUD modules, crosshair styling, and screen filters into a reusable preset.",
                        fontSize = 12.sp,
                        color = McMuted
                    )
                    OutlinedTextField(
                        value = newProfileName,
                        onValueChange = { newProfileName = it },
                        label = { Text("Profile Name", fontSize = 12.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = McAccent,
                            unfocusedBorderColor = McStroke,
                            focusedTextColor = McText,
                            unfocusedTextColor = McText
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("profile_name_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newProfileName.isNotBlank()) {
                            viewModel.saveCurrentProfile(newProfileName.trim())
                            newProfileName = ""
                            showSaveDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = McAccent)
                ) {
                    Text("Save Preset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("Cancel", color = McMuted)
                }
            },
            containerColor = McPanel
        )
    }

    // Export Dialog
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Exported JSON Snapshot", color = McText, fontSize = 16.sp) },
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(McBackground)
                        .border(1.dp, McStroke, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = exportedJsonText,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = McAccentLight
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        copyToClipboard(exportedJsonText, "Client JSON")
                        showExportDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = McAccent)
                ) {
                    Text("Copy JSON")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Close", color = McMuted)
                }
            },
            containerColor = McPanel
        )
    }
}

@Composable
fun ProfileItemCard(
    profile: ClientProfile,
    onLoad: () -> Unit,
    onDelete: () -> Unit
) {
    val dateStr = SimpleDateFormat("MMM d, yyyy", Locale.US).format(Date(profile.createdAt))

    McCard(
        padding = PaddingValues(14.dp),
        modifier = Modifier.testTag("profile_card_${profile.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = profile.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = McText
                )
                Text(
                    text = "${profile.activeModules.size} active modules · Created $dateStr",
                    fontSize = 11.sp,
                    color = McMuted,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onLoad,
                    colors = ButtonDefaults.buttonColors(containerColor = McAccent),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(34.dp).testTag("load_profile_${profile.id}")
                ) {
                    Text("Load", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                if (profile.id.startsWith("prof_")) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete",
                            tint = McDanger,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
