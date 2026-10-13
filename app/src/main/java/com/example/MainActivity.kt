package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.AppDatabase
import com.example.data.remote.ApiClientManager
import com.example.data.repository.HeavyTrackRepository
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.DistributionDialog
import com.example.ui.screens.FormReportScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald50
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Emerald700
import com.example.ui.theme.Emerald900
import com.example.ui.theme.ErrorRose
import com.example.ui.theme.ErrorRose50
import com.example.ui.theme.HeavyTrackTheme
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.WarningAmber
import com.example.ui.viewmodel.HeavyTrackViewModel
import com.example.ui.viewmodel.HeavyTrackViewModelFactory
import com.example.ui.viewmodel.UiMessage
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Schedule WorkManager background auto-retry synchronization
        com.example.worker.OfflineSyncWorker.scheduleAutoSync(this)
        setContent {
            HeavyTrackTheme {
                HeavyTrackApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeavyTrackApp() {
    val context = LocalContext.current
    val database = remember { AppDatabase.getInstance(context) }
    val apiClientManager = remember { ApiClientManager(context) }
    val repository = remember { HeavyTrackRepository(database, apiClientManager) }
    val viewModel: HeavyTrackViewModel = viewModel(
        factory = HeavyTrackViewModelFactory(repository)
    )

    var currentTab by remember { mutableIntStateOf(0) }
    var showMasterDataScreen by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showProfilePopup by remember { mutableStateOf(false) }
    var showDistributionDialog by remember { mutableStateOf(false) }
    val customApkUrl by viewModel.customApkUrl.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val isManagerSession by viewModel.isManagerSession.collectAsState()
    val unsyncedCount by viewModel.unsyncedCount.collectAsState()
    val isSyncingMaster by viewModel.isSyncingMaster.collectAsState()
    val isSyncingPending by viewModel.isSyncingPending.collectAsState()
    val isBootstrapping by viewModel.isBootstrapping.collectAsState()
    val isOfflineSimulated by viewModel.isOfflineSimulated.collectAsState()
    val isSocketConnected by viewModel.isSocketConnected.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Handle back button: return to default main tab (Tab 0) if in other tabs, or close master data
    BackHandler(enabled = isLoggedIn && (showMasterDataScreen || currentTab != 0)) {
        if (showMasterDataScreen) {
            showMasterDataScreen = false
        } else {
            currentTab = 0
        }
    }

    // Reset tab ke posisi awal saat role berganti atau login baru
    LaunchedEffect(isManagerSession, isLoggedIn) {
        currentTab = 0
    }

    // Collect UI messages / notifications
    LaunchedEffect(viewModel) {
        viewModel.uiEvents.collectLatest { event ->
            when (event) {
                is UiMessage.Success -> snackbarHostState.showSnackbar(event.message)
                is UiMessage.Error -> snackbarHostState.showSnackbar(event.message)
                is UiMessage.Info -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    if (!isLoggedIn) {
        Box(modifier = Modifier.fillMaxSize()) {
            LoginScreen(
                onLoginSuccess = { supervisor ->
                    viewModel.login(supervisor)
                },
                onLoginWithApi = { username, password ->
                    viewModel.loginWithApi(username, password)
                }
            )
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = {
                SnackbarHost(
                    hostState = snackbarHostState,
                    snackbar = { data ->
                        Snackbar(
                            snackbarData = data,
                            containerColor = Slate900,
                            contentColor = Color.White,
                            actionColor = Emerald600
                        )
                    }
                )
            },
            topBar = {
                val supervisorSession by viewModel.currentSupervisorSession.collectAsState()
                val activeSupervisorName by viewModel.activeSupervisor.collectAsState()

                Column {
                    // Header (AppBar) RKCE dengan Indikator Status Minimalis di Pojok Kanan Atas
                    TopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Emerald600,
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.PrecisionManufacturing,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "RKCE",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Slate900,
                                            fontSize = 17.sp
                                        )
                                    )
                                    Text(
                                        text = "Rencana Kerja Civil Engineering",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Slate500,
                                            fontWeight = FontWeight.Normal,
                                            fontSize = 10.sp
                                        )
                                    )
                                }
                            }
                        },
                        actions = {
                            // Tombol Download Installer APK di Pojok Kanan Atas AppBar
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Emerald600,
                                shadowElevation = 2.dp,
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .height(34.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable {
                                        showDistributionDialog = true
                                    }
                                    .testTag("appbar_download_installer_btn")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Download,
                                        contentDescription = "Download Installer APK",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = "Unduh APK",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            // Tombol "Sinkron" (Manual Pull dari Server) di Pojok Kanan Atas AppBar
                            val isSyncing = isSyncingMaster || isSyncingPending
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = when {
                                    isSyncing -> Emerald100
                                    isOfflineSimulated -> Color(0xFFFEF3C7)
                                    else -> Emerald50
                                },
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    when {
                                        isSyncing -> Emerald600
                                        isOfflineSimulated -> WarningAmber.copy(alpha = 0.6f)
                                        else -> Emerald600.copy(alpha = 0.5f)
                                    }
                                ),
                                modifier = Modifier
                                    .padding(end = 12.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable {
                                        if (!isSyncing) {
                                            viewModel.triggerHeaderSync()
                                        }
                                    }
                                    .testTag("appbar_sync_status_chip")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (isSyncing) {
                                        CircularProgressIndicator(
                                            color = Emerald700,
                                            modifier = Modifier.size(13.dp),
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Menyinkronkan...",
                                            fontSize = 12.sp,
                                            color = Emerald900,
                                            fontWeight = FontWeight.Bold
                                        )
                                    } else if (isOfflineSimulated) {
                                        Box(
                                            modifier = Modifier
                                                .size(7.dp)
                                                .background(WarningAmber, CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Sinkron (Offline)",
                                            fontSize = 12.sp,
                                            color = Color(0xFFB45309),
                                            fontWeight = FontWeight.Bold
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.CloudSync,
                                            contentDescription = "Tombol Sinkron Manual Pull dari Server",
                                            tint = Emerald700,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Sinkron",
                                            fontSize = 12.sp,
                                            color = Emerald900,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (isSocketConnected) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .background(Color(0xFF10B981), CircleShape)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                // Tombol Web Admin Master Unit
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (showMasterDataScreen) Emerald600 else Color(0xFFF1F5F9),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (showMasterDataScreen) Emerald600 else Slate300),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { showMasterDataScreen = !showMasterDataScreen }
                                        .testTag("btn_top_master_data")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PrecisionManufacturing,
                                            contentDescription = "Kelola Master Unit (Web Admin)",
                                            tint = if (showMasterDataScreen) Color.White else Slate700,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Master",
                                            fontSize = 11.5.sp,
                                            color = if (showMasterDataScreen) Color.White else Slate800,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.White
                        ),
                        modifier = Modifier.testTag("main_app_bar")
                    )

                    // IDENTITAS USER PENGAWAS AKTIF (Konsisten di bagian atas)
                    Surface(
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("active_supervisor_identity_bar")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val rawSpvName = supervisorSession?.namaLengkap?.ifBlank { null }
                                ?: supervisorSession?.name
                                ?: activeSupervisorName
                            val initials = getInitials(rawSpvName)
                            val isSuperAdmin = supervisorSession?.isSuperAdmin == true
                            val isMgr = supervisorSession?.isManager == true

                            val avatarBg = when {
                                isSuperAdmin -> Color(0xFF6366F1) // Indigo
                                isMgr -> Color(0xFFF59E0B) // Amber
                                else -> Emerald600 // Emerald
                            }

                            val accountLabel = when {
                                isSuperAdmin -> "$rawSpvName • Super Admin (Semua Wilayah)"
                                isMgr -> "$rawSpvName • Manager (Monitoring)"
                                else -> "$rawSpvName • Kasie (${supervisorSession?.wilayah ?: "Geodesi 1"})"
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { showProfilePopup = true }
                                    .testTag("avatar_profile_trigger")
                            ) {
                                // Avatar Inisial Dinamis: Lingkaran inisial huruf nama (misal "AG", "LT", "ZA", "BS")
                                Surface(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .testTag("avatar_initials_circle"),
                                    shape = CircleShape,
                                    color = avatarBg,
                                    shadowElevation = 2.dp
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = initials,
                                            color = Color.White,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 12.5.sp
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = accountLabel,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 12.5.sp,
                                        color = Slate900,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = if (isManagerSession) "Mode Eksekutif • Sentuh untuk detail akun" else "Wilayah: ${supervisorSession?.wilayah ?: "Geodesi 1"} • Shift ${supervisorSession?.defaultShift ?: "SIANG"}",
                                        fontSize = 10.sp,
                                        color = Slate500,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Tombol Log Out di Bar Identitas Pengawas
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = ErrorRose50,
                                border = androidx.compose.foundation.BorderStroke(1.dp, ErrorRose.copy(alpha = 0.35f)),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { showLogoutDialog = true }
                                    .testTag("btn_logout")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Logout,
                                        contentDescription = "Log Out",
                                        tint = ErrorRose,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Keluar",
                                        color = ErrorRose,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            },
            bottomBar = {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 8.dp,
                    modifier = Modifier.testTag("main_navigation_bar")
                ) {
                    if (isManagerSession) {
                        // MODE MANAGER EKSEKUTIF: HANYA 2 TAB MONITORING (HASIL KERJA & ANALITIK)
                        // Tab "Formulir" input lapangan disembunyikan sepenuhnya!

                        // Tab 0 (Manager): Hasil Kerja / Monitoring Lapangan
                        NavigationBarItem(
                            selected = currentTab == 0,
                            onClick = { currentTab = 0 },
                            icon = {
                                BadgedBox(
                                    badge = {
                                        if (unsyncedCount > 0) {
                                            Badge(
                                                containerColor = WarningAmber,
                                                contentColor = Slate900
                                            ) {
                                                Text("$unsyncedCount", fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = "Hasil Kerja",
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            },
                            label = {
                                Text(
                                    text = "Hasil Kerja",
                                    fontWeight = if (currentTab == 0) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 11.sp
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Emerald900,
                                selectedTextColor = Emerald900,
                                indicatorColor = Emerald100,
                                unselectedIconColor = Slate500,
                                unselectedTextColor = Slate500
                            ),
                            modifier = Modifier.testTag("nav_item_history")
                        )

                        // Tab 1 (Manager): Analitik Eksekutif
                        NavigationBarItem(
                            selected = currentTab == 1,
                            onClick = { currentTab = 1 },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.BarChart,
                                    contentDescription = "Analitik",
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = "Analitik",
                                    fontWeight = if (currentTab == 1) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 11.sp
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Emerald900,
                                selectedTextColor = Emerald900,
                                indicatorColor = Emerald100,
                                unselectedIconColor = Slate500,
                                unselectedTextColor = Slate500
                            ),
                            modifier = Modifier.testTag("nav_item_analytics")
                        )
                    } else {
                        // MODE SUPERVISOR / PENGAWAS LAPANGAN: 3 TAB OPERASIONAL LENGKAP
                        // Tab 0: Formulir (Rencana & Realisasi)
                        NavigationBarItem(
                            selected = currentTab == 0,
                            onClick = {
                                currentTab = 0
                                viewModel.autoFetchMasterDataOnResume()
                            },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Assignment,
                                    contentDescription = "Formulir",
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = "Formulir",
                                    fontWeight = if (currentTab == 0) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 11.sp
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Emerald900,
                                selectedTextColor = Emerald900,
                                indicatorColor = Emerald100,
                                unselectedIconColor = Slate500,
                                unselectedTextColor = Slate500
                            ),
                            modifier = Modifier.testTag("nav_item_form")
                        )

                        // Tab 1: Riwayat Aktivitas
                        NavigationBarItem(
                            selected = currentTab == 1,
                            onClick = { currentTab = 1 },
                            icon = {
                                BadgedBox(
                                    badge = {
                                        if (unsyncedCount > 0) {
                                            Badge(
                                                containerColor = WarningAmber,
                                                contentColor = Slate900
                                            ) {
                                                Text("$unsyncedCount", fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = "Riwayat Saya",
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            },
                            label = {
                                Text(
                                    text = "Riwayat",
                                    fontWeight = if (currentTab == 1) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 11.sp
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Emerald900,
                                selectedTextColor = Emerald900,
                                indicatorColor = Emerald100,
                                unselectedIconColor = Slate500,
                                unselectedTextColor = Slate500
                            ),
                            modifier = Modifier.testTag("nav_item_history")
                        )

                        // Tab 2: Analitik Fleet
                        NavigationBarItem(
                            selected = currentTab == 2,
                            onClick = { currentTab = 2 },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.BarChart,
                                    contentDescription = "Analitik",
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = "Analitik",
                                    fontWeight = if (currentTab == 2) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 11.sp
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Emerald900,
                                selectedTextColor = Emerald900,
                                indicatorColor = Emerald100,
                                unselectedIconColor = Slate500,
                                unselectedTextColor = Slate500
                            ),
                            modifier = Modifier.testTag("nav_item_analytics")
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .imePadding()
            ) {
                if (showMasterDataScreen) {
                    com.example.ui.screens.MasterDataScreen(
                        viewModel = viewModel,
                        onBack = { showMasterDataScreen = false }
                    )
                } else if (isManagerSession) {
                    when (currentTab) {
                        0 -> HistoryScreen(
                            viewModel = viewModel,
                            onNavigateToForm = { /* Tidak ada aksi navigasi formulir untuk Manager */ }
                        )
                        1 -> AnalyticsScreen(
                            viewModel = viewModel
                        )
                        else -> HistoryScreen(
                            viewModel = viewModel,
                            onNavigateToForm = { }
                        )
                    }
                } else {
                    when (currentTab) {
                        0 -> FormReportScreen(
                            viewModel = viewModel,
                            onNavigateToHistory = { currentTab = 1 }
                        )
                        1 -> HistoryScreen(
                            viewModel = viewModel,
                            onNavigateToForm = { currentTab = 0 }
                        )
                        2 -> AnalyticsScreen(
                            viewModel = viewModel
                        )
                    }
                }
            }
        }

        if (showDistributionDialog) {
            DistributionDialog(
                onDismiss = { showDistributionDialog = false },
                currentApkUrl = customApkUrl,
                onSaveApkLocally = {
                    viewModel.downloadApk(context)
                    showDistributionDialog = false
                },
                onShareWhatsApp = {
                    viewModel.shareApkViaWhatsApp(context)
                    showDistributionDialog = false
                },
                onShareGeneral = {
                    viewModel.shareApkGeneral(context)
                    showDistributionDialog = false
                },
                onInstallLocalApk = {
                    viewModel.launchApkInstaller(context)
                    showDistributionDialog = false
                },
                onOpenInBrowser = { url ->
                    viewModel.openApkDownloadInBrowser(context, url)
                },
                onUpdateApkUrl = { newUrl ->
                    viewModel.updateCustomApkUrl(newUrl)
                }
            )
        }

        if (showProfilePopup) {
            val supervisorSession by viewModel.currentSupervisorSession.collectAsState()
            val activeSupervisorName by viewModel.activeSupervisor.collectAsState()
            val spvName = supervisorSession?.namaLengkap?.ifBlank { null } ?: supervisorSession?.name ?: activeSupervisorName
            val initials = getInitials(spvName)
            val isSuperAdmin = supervisorSession?.isSuperAdmin == true
            val isMgr = supervisorSession?.isManager == true
            val avatarBg = when {
                isSuperAdmin -> Color(0xFF6366F1)
                isMgr -> Color(0xFFF59E0B)
                else -> Emerald600
            }

            AlertDialog(
                onDismissRequest = { showProfilePopup = false },
                icon = {
                    Surface(
                        shape = CircleShape,
                        color = avatarBg,
                        shadowElevation = 4.dp,
                        modifier = Modifier.size(54.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = initials,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp
                            )
                        }
                    }
                },
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = spvName,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp,
                            color = Slate900
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = when {
                                isSuperAdmin -> Color(0xFFEEF2FF)
                                isMgr -> Color(0xFFFEF3C7)
                                else -> Emerald100
                            }
                        ) {
                            Text(
                                text = supervisorSession?.role ?: if (isManagerSession) "MANAGER" else "KASIE",
                                color = when {
                                    isSuperAdmin -> Color(0xFF3730A3)
                                    isMgr -> Color(0xFF92400E)
                                    else -> Emerald900
                                },
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                },
                text = {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ProfileDetailRow("Nama Lengkap", spvName)
                            ProfileDetailRow("Username", supervisorSession?.username ?: spvName)
                            ProfileDetailRow("User ID", supervisorSession?.userId ?: "1")
                            ProfileDetailRow("Jabatan / Role", supervisorSession?.roleTitle ?: if (isManagerSession) "Manager Operasional" else "Kasie Lapangan")
                            ProfileDetailRow("Wilayah Kerja", supervisorSession?.wilayah ?: if (isManagerSession) "Semua Wilayah" else "Geodesi 1")
                            ProfileDetailRow("Status Akun", "Aktif", isStatusSuccess = true)

                            Spacer(modifier = Modifier.height(4.dp))
                            Button(
                                onClick = {
                                    showProfilePopup = false
                                    showMasterDataScreen = true
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                                modifier = Modifier.fillMaxWidth().testTag("btn_profile_open_master_data")
                            ) {
                                Icon(Icons.Default.PrecisionManufacturing, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Buka Web Admin Master Unit", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showProfilePopup = false
                            showLogoutDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ErrorRose,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("btn_popup_logout")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Keluar / Logout", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { showProfilePopup = false },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Tutup", color = Slate700)
                    }
                }
            )
        }

        if (showLogoutDialog) {
            val supervisorSession by viewModel.currentSupervisorSession.collectAsState()
            val activeSupervisorName by viewModel.activeSupervisor.collectAsState()
            val spvName = supervisorSession?.namaLengkap?.ifBlank { null } ?: supervisorSession?.name ?: activeSupervisorName

            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                icon = {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(ErrorRose50, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = null,
                            tint = ErrorRose,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                title = {
                    Text(
                        text = "Konfirmasi Log Out",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Slate900
                    )
                },
                text = {
                    Text(
                        text = "Apakah Anda yakin ingin keluar dari sesi pengawas ($spvName)? Anda dapat masuk kembali dengan memilih nama atau memasukkan NIK/PIN pengawas.",
                        fontSize = 13.sp,
                        color = Slate600
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showLogoutDialog = false
                            viewModel.logout()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ErrorRose,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("btn_confirm_logout")
                    ) {
                        Text(
                            text = "Ya, Log Out",
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { showLogoutDialog = false },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Slate700
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Slate300),
                        modifier = Modifier.testTag("btn_cancel_logout")
                    ) {
                        Text(text = "Batal")
                    }
                },
                containerColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }

    // OVERLAY RINGAN: SINKRONISASI DATA OPERASIONAL & RIWAYAT (DATA HYDRATION)
    if (isBootstrapping) {
        Dialog(
            onDismissRequest = { /* Non-dismissible during initial hydration */ },
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                shadowElevation = 8.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .testTag("bootstrap_loading_overlay")
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(Emerald50, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = Emerald600,
                            modifier = Modifier.size(32.dp),
                            strokeWidth = 3.dp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Menyinkronkan data operasional & riwayat...",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Slate900,
                            textAlign = TextAlign.Center
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Mengunduh rencana kerja SPK, riwayat aktivitas terverifikasi, dan master data dari server admin...",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Slate500,
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF1F5F9)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudSync,
                                contentDescription = null,
                                tint = Emerald600,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Database Hydration • SQLite Room",
                                fontSize = 11.sp,
                                color = Slate700,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun getInitials(name: String): String {
    val clean = name.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }
    return when {
        clean.isEmpty() -> "U"
        clean.size == 1 -> clean[0].take(2).uppercase()
        else -> (clean[0].take(1) + clean[1].take(1)).uppercase()
    }
}

@Composable
private fun ProfileDetailRow(
    label: String,
    value: String,
    isStatusSuccess: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 11.5.sp, color = Slate500, fontWeight = FontWeight.Medium)
        if (isStatusSuccess) {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = Emerald100
            ) {
                Text(
                    text = "● $value",
                    color = Emerald900,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        } else {
            Text(
                text = value,
                fontSize = 11.5.sp,
                color = Slate900,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}
