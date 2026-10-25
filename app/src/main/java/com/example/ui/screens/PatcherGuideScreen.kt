package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.CompanionViewModel
import com.example.ui.components.McCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@Composable
fun PatcherGuideScreen(
    viewModel: CompanionViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var baseApkPath by remember { mutableStateOf("base.apk") }
    var outApkPath by remember { mutableStateOf("mc-patched.apk") }

    val generatedCommand = "tools/patch_apk.sh $baseApkPath client-sdk-release.aar shadowhook-1.0.9.aar $outApkPath"

    fun copyToClipboard(text: String, label: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
    }

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
                text = "APK PATCHER GUIDE",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = McAccent,
                letterSpacing = 1.sp
            )
            Text(
                text = "How to inject Mc-Mobile client into Minecraft Bedrock",
                fontSize = 12.sp,
                color = McMuted
            )
        }

        // Overview Card
        McCard {
            Text(
                text = "HOW MC-MOBILE WORKS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = McMuted,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Mc-Mobile uses an automatic Android ContentProvider (ClientInitProvider) and native ShadowHook hooks (eglSwapBuffers) injected directly into the APK. When Minecraft starts, it attaches the in-game HUD overlay and the floating ☰ click menu.",
                fontSize = 12.sp,
                color = McText,
                lineHeight = 17.sp
            )
        }

        // Interactive Command Generator
        McCard(borderColor = McAccent.copy(alpha = 0.5f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "COMMAND GENERATOR",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = McAccent,
                    letterSpacing = 1.sp
                )
                StatusBadge(text = "patch_apk.sh", color = McAccent)
            }
            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = baseApkPath,
                    onValueChange = { baseApkPath = it },
                    label = { Text("Base APK", fontSize = 11.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = McAccent,
                        unfocusedBorderColor = McStroke,
                        focusedTextColor = McText,
                        unfocusedTextColor = McText
                    ),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = outApkPath,
                    onValueChange = { outApkPath = it },
                    label = { Text("Output APK", fontSize = 11.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = McAccent,
                        unfocusedBorderColor = McStroke,
                        focusedTextColor = McText,
                        unfocusedTextColor = McText
                    ),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(McBackground)
                    .border(1.dp, McStroke, RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Text(
                    text = generatedCommand,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = McAccentLight
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { copyToClipboard(generatedCommand, "Patch Command") },
                colors = ButtonDefaults.buttonColors(containerColor = McAccent),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().testTag("copy_patch_command_button")
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Copy Patch Command", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }

        // Step-by-Step Instructions
        Text(
            text = "STEP-BY-STEP WORKFLOW",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = McMuted,
            letterSpacing = 1.sp
        )

        StepCard(
            stepNumber = 1,
            title = "Extract APK from your Device",
            description = "Pull the legal base APK from your installed Minecraft game:",
            codeSnippet = "adb shell pm path com.mojang.minecraftpe\nadb pull /data/app/.../base.apk"
        ) {
            copyToClipboard("adb shell pm path com.mojang.minecraftpe", "ADB Pull")
        }

        StepCard(
            stepNumber = 2,
            title = "Crucial: Backup Your Worlds",
            description = "Before reinstalling, safeguard your worlds, resource packs, and skins at:",
            codeSnippet = "/sdcard/Android/data/com.mojang.minecraftpe/files/games/com.mojang"
        ) {
            copyToClipboard("/sdcard/Android/data/com.mojang.minecraftpe/files/games/com.mojang", "World Backup Path")
        }

        StepCard(
            stepNumber = 3,
            title = "Execute patch_apk.sh (PC or Termux)",
            description = "Uses apktool to merge the AAR manifest, shadowhook library, and provider:",
            codeSnippet = "tools/patch_apk.sh base.apk client-sdk-release.aar shadowhook-1.0.9.aar out.apk"
        ) {
            copyToClipboard("tools/patch_apk.sh base.apk client-sdk-release.aar shadowhook-1.0.9.aar out.apk", "Patch Script")
        }

        StepCard(
            stepNumber = 4,
            title = "Install & Launch in Patch Mode",
            description = "Install the patched out.apk on your Android phone, then return to MC Mobile Companion and tap 'PLAY CLIENT'!",
            codeSnippet = "adb install out.apk"
        ) {
            copyToClipboard("adb install out.apk", "ADB Install")
        }

        // Legitimacy & FAQ Card
        McCard {
            Text(
                text = "ANTI-CHEAT & LEGITIMACY",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = McMuted,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "• No Cheats: Mc-Mobile contains zero combat hacks (no killaura, reach, or flight) — it is designed strictly as a legitimate client like Lunar or Feather on PC.\n• Server Policy Support: Servers can broadcast a JSON policy to disable specific modules.\n• Safe Overlay: All HUD graphics render through Android's hardware accelerated overlay, never corrupting game binaries.",
                fontSize = 11.sp,
                color = McMuted,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun StepCard(
    stepNumber: Int,
    title: String,
    description: String,
    codeSnippet: String,
    onCopy: () -> Unit
) {
    McCard(padding = PaddingValues(12.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(McAccent),
                contentAlignment = Alignment.Center
            ) {
                Text("$stepNumber", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = McText)
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(description, fontSize = 11.sp, color = McMuted)
        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(McBackground)
                .border(1.dp, McStroke, RoundedCornerShape(6.dp))
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = codeSnippet,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = McAccentLight,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onCopy, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        tint = McMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
