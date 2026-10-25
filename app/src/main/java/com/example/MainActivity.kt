package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.*
import com.example.ui.theme.*

enum class AppTab(val title: String, val icon: ImageVector, val tag: String) {
    LAUNCHER("Launcher", Icons.Default.SportsEsports, "nav_tab_launcher"),
    HUD("HUD", Icons.Default.Dashboard, "nav_tab_hud"),
    CROSSHAIR("Crosshair", Icons.Default.FilterCenterFocus, "nav_tab_crosshair"),
    SERVERS("Servers", Icons.Default.Dns, "nav_tab_servers"),
    CPS("CPS Test", Icons.Default.Speed, "nav_tab_cps"),
    PATCHER("Patcher", Icons.Default.Build, "nav_tab_patcher"),
    PROFILES("Profiles", Icons.Default.Bookmarks, "nav_tab_profiles")
}

class MainActivity : ComponentActivity() {

    private val viewModel: CompanionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                MainAppScaffold(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScaffold(viewModel: CompanionViewModel) {
    var currentTab by remember { mutableStateOf(AppTab.LAUNCHER) }
    val userMessage by viewModel.userMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Launch message effect
    LaunchedEffect(userMessage) {
        userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUserMessage()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(McBackground),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, McAccent, RoundedCornerShape(8.dp))
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_launcher_fg_1792932896163),
                                contentDescription = "App Icon",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Column {
                            Text(
                                text = "MC MOBILE",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = McAccent,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Minecraft Bedrock Client Companion",
                                fontSize = 10.sp,
                                color = McMuted
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { currentTab = AppTab.PROFILES },
                        modifier = Modifier.testTag("top_bar_profiles_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bookmarks,
                            contentDescription = "Profiles",
                            tint = if (currentTab == AppTab.PROFILES) McAccent else McMuted
                        )
                    }

                    IconButton(
                        onClick = { currentTab = AppTab.PATCHER },
                        modifier = Modifier.testTag("top_bar_patcher_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = "Patcher Guide",
                            tint = if (currentTab == AppTab.PATCHER) McAccent else McMuted
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = McPanel,
                    titleContentColor = McText
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = McPanel,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .border(1.dp, McStroke, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            ) {
                val primaryTabs = listOf(
                    AppTab.LAUNCHER,
                    AppTab.HUD,
                    AppTab.CROSSHAIR,
                    AppTab.SERVERS,
                    AppTab.CPS
                )

                primaryTabs.forEach { tab ->
                    val selected = currentTab == tab
                    NavigationBarItem(
                        selected = selected,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                tint = if (selected) McAccent else McMuted,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 10.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                color = if (selected) McAccent else McMuted
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = McCard,
                            selectedIconColor = McAccent,
                            selectedTextColor = McAccent,
                            unselectedIconColor = McMuted,
                            unselectedTextColor = McMuted
                        ),
                        modifier = Modifier.testTag(tab.tag)
                    )
                }
            }
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                snackbar = { data ->
                    Snackbar(
                        containerColor = McPanel,
                        contentColor = McText,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .padding(16.dp)
                            .border(1.dp, McAccent, RoundedCornerShape(10.dp))
                    ) {
                        Text(data.visuals.message, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(McBackground)
        ) {
            when (currentTab) {
                AppTab.LAUNCHER -> LauncherScreen(
                    viewModel = viewModel,
                    onNavigateToHud = { currentTab = AppTab.HUD },
                    onNavigateToServers = { currentTab = AppTab.SERVERS },
                    onNavigateToPatcher = { currentTab = AppTab.PATCHER }
                )
                AppTab.HUD -> HudStudioScreen(viewModel = viewModel)
                AppTab.CROSSHAIR -> CrosshairScreen(viewModel = viewModel)
                AppTab.SERVERS -> ServersScreen(viewModel = viewModel)
                AppTab.CPS -> CpsTrainerScreen(viewModel = viewModel)
                AppTab.PATCHER -> PatcherGuideScreen(viewModel = viewModel)
                AppTab.PROFILES -> ProfilesScreen(viewModel = viewModel)
            }
        }
    }
}
