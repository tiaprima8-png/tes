package com.example.ui.screens

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import com.example.data.model.UnitConstants
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entity.ActivityMasterEntity
import com.example.data.local.entity.KendalaEntity
import com.example.data.local.entity.LokasiEntity
import com.example.data.local.entity.OperatorEntity
import com.example.data.local.entity.UnitEntity
import com.example.data.repository.SyncLogItem
import com.example.ui.components.LokasiSelectorItemCard
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald50
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Emerald700
import com.example.ui.theme.Emerald800
import com.example.ui.theme.Emerald900
import com.example.ui.theme.ErrorRose
import com.example.ui.theme.ErrorRose50
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WarningAmber50
import com.example.ui.viewmodel.HeavyTrackViewModel

@Composable
fun MasterDataScreen(
    viewModel: HeavyTrackViewModel,
    onBack: (() -> Unit)? = null
) {
    if (onBack != null) {
        BackHandler { onBack() }
    }

    val context = LocalContext.current
    val activeSupervisor by viewModel.activeSupervisor.collectAsState()
    val isSyncingMaster by viewModel.isSyncingMaster.collectAsState()
    val syncLogs by viewModel.syncLogs.collectAsState()
    val units by viewModel.units.collectAsState()
    val activities by viewModel.activities.collectAsState()
    val operators by viewModel.operators.collectAsState()
    val lokasiList by viewModel.lokasiList.collectAsState()
    val kendalaList by viewModel.kendalaList.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var showDistributionDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .testTag("master_data_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (onBack != null) {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                    modifier = Modifier.fillMaxWidth().testTag("bar_master_back_nav")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.size(36.dp).testTag("btn_back_master_data")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Kembali ke Beranda",
                                tint = Slate700
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Web Admin • Master Data",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                            )
                            Text(
                                text = "Kelola Unit, Lokasi, Aktivitas, & Operator",
                                style = MaterialTheme.typography.bodySmall.copy(color = Slate500)
                            )
                        }
                    }
                }
            }
        }

        // TOP CARD: Pengawas Sesi & Aksi Distribusi Installer
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_master_header"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(Emerald50, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Storage,
                                    contentDescription = null,
                                    tint = Emerald600,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Master Data Lapangan",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Slate900
                                    )
                                )
                                Text(
                                    text = "Kasie: $activeSupervisor",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Emerald700,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                        }

                        // Tombol Refresh / Pull Data
                        IconButton(
                            onClick = { viewModel.fetchMasterData() },
                            enabled = !isSyncingMaster,
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFFF1F5F9), CircleShape)
                        ) {
                            if (isSyncingMaster) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = Emerald600
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Muat Ulang Master Data",
                                    tint = Emerald600,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Tombol Aksi Utama: "Download & Bagikan Installer APK"
                    Button(
                        onClick = { showDistributionDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("btn_open_distribution"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Download Installer APK",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 13.sp
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Emerald700
                                ) {
                                    Text(
                                        text = "v1.2",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Ekstrak & bagikan berkas APK resmi pembaruan",
                                fontSize = 10.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Tombol Log Out dari Sesi Pengawas
                    OutlinedButton(
                        onClick = { showLogoutDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("btn_logout_master"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = ErrorRose
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ErrorRose.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Log Out",
                            tint = ErrorRose,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Log Out dari Sesi Kasie",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = ErrorRose
                            )
                        )
                    }
                }
            }
        }

        // CARD 2: Multi-Tab Master Data Bersih & Rapi
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_master_content"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    ScrollableTabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color.White,
                        contentColor = Emerald600,
                        edgePadding = 12.dp
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("Unit (${units.size})", fontWeight = FontWeight.SemiBold, fontSize = 12.sp) }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("Lokasi (${lokasiList.size})", fontWeight = FontWeight.SemiBold, fontSize = 12.sp) }
                        )
                        Tab(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            text = { Text("Aktivitas (${activities.size})", fontWeight = FontWeight.SemiBold, fontSize = 12.sp) }
                        )
                        Tab(
                            selected = selectedTab == 3,
                            onClick = { selectedTab = 3 },
                            text = { Text("Operator (${operators.size})", fontWeight = FontWeight.SemiBold, fontSize = 12.sp) }
                        )
                        Tab(
                            selected = selectedTab == 4,
                            onClick = { selectedTab = 4 },
                            text = { Text("Kendala (${kendalaList.size})", fontWeight = FontWeight.SemiBold, fontSize = 12.sp) }
                        )
                        Tab(
                            selected = selectedTab == 5,
                            onClick = { selectedTab = 5 },
                            text = { Text("Log Sync (${syncLogs.size})", fontWeight = FontWeight.SemiBold, fontSize = 12.sp) }
                        )
                    }

                    Box(modifier = Modifier.padding(14.dp)) {
                        when (selectedTab) {
                            0 -> CleanUnitsTab(units, viewModel)
                            1 -> CleanLokasiTab(lokasiList)
                            2 -> CleanActivitiesTab(activities)
                            3 -> CleanOperatorsTab(operators)
                            4 -> CleanKendalaTab(kendalaList)
                            5 -> CleanSyncLogsTab(syncLogs)
                        }
                    }
                }
            }
        }
    }

    val customApkUrl by viewModel.customApkUrl.collectAsState()

    // Modal Dialog Distribusi APK & Share WhatsApp
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

    if (showLogoutDialog) {
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
                    text = "Apakah Anda yakin ingin keluar dari sesi pengawas ($activeSupervisor)?",
                    fontSize = 14.sp,
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
                    modifier = Modifier.testTag("btn_confirm_logout_master")
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
                    modifier = Modifier.testTag("btn_cancel_logout_master")
                ) {
                    Text("Batal")
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun CleanUnitsTab(
    units: List<UnitEntity>,
    viewModel: HeavyTrackViewModel
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedJenisFilter by remember { mutableStateOf("Semua Jenis") }
    var isFilterDropdownExpanded by remember { mutableStateOf(false) }

    var showAddEditDialog by remember { mutableStateOf(false) }
    var editingUnit by remember { mutableStateOf<UnitEntity?>(null) }
    var unitToDelete by remember { mutableStateOf<UnitEntity?>(null) }

    val filteredUnits = remember(units, searchQuery, selectedJenisFilter) {
        units.filter { u ->
            val matchType = if (selectedJenisFilter == "Semua Jenis") true
            else viewModel.matchesJenisUnit(u.unitType, selectedJenisFilter)
            val matchQuery = searchQuery.isBlank() ||
                    u.unitCode.contains(searchQuery, ignoreCase = true) ||
                    u.brandModel.contains(searchQuery, ignoreCase = true) ||
                    u.supervisorAssigned.contains(searchQuery, ignoreCase = true)
            matchType && matchQuery
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // 1. KARTU METRIK TOTAL UNIT (DASHBOARD METRIC CARD - DIPERBARUI OTOMATIS)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("card_metric_total_unit"),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PrecisionManufacturing,
                            contentDescription = null,
                            tint = Emerald600,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Total Unit (Master Unit)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate800
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Unit Operasional Aktif (Dump Truck & Bulldozer Dihapus)",
                        fontSize = 10.sp,
                        color = Slate500
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Emerald600
                ) {
                    Text(
                        text = "${units.size} Unit",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // 2. BILAH FILTER TABEL MASTER UNIT & TOMBOL TAMBAH UNIT
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Dropdown Filter Jenis Unit (Hanya Opsi Unit Operasional Aktif)
            Box {
                OutlinedButton(
                    onClick = { isFilterDropdownExpanded = true },
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (selectedJenisFilter != "Semua Jenis") Emerald600 else Slate300),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (selectedJenisFilter != "Semua Jenis") Emerald50 else Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(36.dp).testTag("btn_filter_jenis_master")
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = null,
                        tint = if (selectedJenisFilter != "Semua Jenis") Emerald700 else Slate500,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = selectedJenisFilter,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (selectedJenisFilter != "Semua Jenis") Emerald800 else Slate700
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = Slate500,
                        modifier = Modifier.size(16.dp)
                    )
                }

                DropdownMenu(
                    expanded = isFilterDropdownExpanded,
                    onDismissRequest = { isFilterDropdownExpanded = false }
                ) {
                    UnitConstants.FILTER.forEach { opt ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = opt,
                                    fontSize = 12.sp,
                                    fontWeight = if (opt == selectedJenisFilter) FontWeight.Bold else FontWeight.Normal,
                                    color = if (opt == selectedJenisFilter) Emerald700 else Slate800
                                )
                            },
                            onClick = {
                                selectedJenisFilter = opt
                                isFilterDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // Tombol Tambah Unit (Membuka Modal Tambah Unit)
            Button(
                onClick = {
                    editingUnit = null
                    showAddEditDialog = true
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.height(36.dp).testTag("btn_tambah_master_unit")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                Spacer(modifier = Modifier.width(4.dp))
                Text("+ Tambah Unit", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        // Pencarian Teks
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Cari kode unit, model, atau kasie...", fontSize = 11.sp, color = Slate400) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = Slate400, modifier = Modifier.size(16.dp))
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(10.dp)
        )

        // DAFTAR UNIT MASTER
        if (filteredUnits.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Tidak ada unit yang cocok dengan filter.", color = Slate400, fontSize = 13.sp)
            }
        } else {
            filteredUnits.forEach { unit ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF8FAFC),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(unit.unitCode, fontWeight = FontWeight.Bold, color = Slate900, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(shape = RoundedCornerShape(4.dp), color = Emerald100) {
                                    Text(
                                        text = unit.unitType,
                                        color = Emerald900,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Tombol Edit Unit (Membuka Modal Edit Unit)
                                IconButton(
                                    onClick = {
                                        editingUnit = unit
                                        showAddEditDialog = true
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Unit",
                                        tint = Slate600,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                // Tombol Hapus Unit
                                IconButton(
                                    onClick = {
                                        unitToDelete = unit
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Hapus Unit",
                                        tint = ErrorRose,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${unit.brandModel} • Kasie: ${unit.supervisorAssigned} • Wilayah: ${unit.wilayah}",
                            fontSize = 11.sp,
                            color = Slate500
                        )
                        Text(
                            text = "HM Terakhir: ${String.format("%.1f", unit.lastHm)} Jam • Standar: ${unit.hmMinStandar}-${unit.hmMaxStandar} HM",
                            fontSize = 11.sp,
                            color = Emerald700,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    // Modal Tambah Unit & Edit Unit
    if (showAddEditDialog) {
        UnitFormModal(
            unit = editingUnit,
            onDismiss = {
                showAddEditDialog = false
                editingUnit = null
            },
            onSave = { savedUnit ->
                viewModel.addOrUpdateUnit(savedUnit)
                showAddEditDialog = false
                editingUnit = null
            }
        )
    }

    // Konfirmasi Hapus Unit
    unitToDelete?.let { unit ->
        AlertDialog(
            onDismissRequest = { unitToDelete = null },
            title = {
                Text("Hapus Unit ${unit.unitCode}?", fontWeight = FontWeight.Bold, color = Slate900)
            },
            text = {
                Text(
                    "Unit ini beserta transaksi terkait akan dihapus dari database SQLite untuk menjaga integritas data.",
                    fontSize = 13.sp,
                    color = Slate600
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteUnit(unit.unitCode)
                        unitToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRose)
                ) {
                    Text("Hapus", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { unitToDelete = null }) {
                    Text("Batal")
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White
        )
    }
}

/**
 * Modal Tambah Unit & Edit Unit:
 * Dropdown pilihan "Jenis Unit" HANYA menampilkan:
 * - Excavator Long Arm
 * - Excavator Mini
 * - Excavator Standar
 * - Motor Grader
 * - Compactor
 * (Dump Truck & Bulldozer dihapus secara permanen).
 */
@Composable
fun UnitFormModal(
    unit: UnitEntity?,
    onDismiss: () -> Unit,
    onSave: (UnitEntity) -> Unit
) {
    val isEdit = unit != null
    var kodeUnit by remember { mutableStateOf(unit?.unitCode ?: "") }
    var selectedJenis by remember {
        mutableStateOf(
            unit?.unitType?.takeIf { UnitConstants.isJenisUnitValid(it) } ?: UnitConstants.AKTIF.first()
        )
    }
    var isJenisDropdownOpen by remember { mutableStateOf(false) }
    var brandModel by remember { mutableStateOf(unit?.brandModel ?: "") }
    var supervisor by remember { mutableStateOf(unit?.supervisorAssigned ?: "Budi Santoso") }
    var wilayah by remember { mutableStateOf(unit?.wilayah ?: "Geodesi PG1") }
    var lastHmStr by remember { mutableStateOf(unit?.lastHm?.toString() ?: "0.0") }
    var minHmStr by remember { mutableStateOf(unit?.hmMinStandar?.toString() ?: "6.0") }
    var maxHmStr by remember { mutableStateOf(unit?.hmMaxStandar?.toString() ?: "10.0") }
    var status by remember { mutableStateOf(unit?.status ?: "OPERASIONAL") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (isEdit) "Edit Master Unit" else "Tambah Master Unit",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                )
                Text(
                    text = "Daftar unit operasional aktif (Dump Truck & Bulldozer telah dihapus).",
                    fontSize = 11.sp,
                    color = Slate500
                )

                if (errorMessage != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ErrorRose50,
                        border = androidx.compose.foundation.BorderStroke(1.dp, ErrorRose.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            color = ErrorRose,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                // Kode Unit
                OutlinedTextField(
                    value = kodeUnit,
                    onValueChange = { kodeUnit = it.uppercase() },
                    label = { Text("Kode Unit (e.g. EXC-03)") },
                    enabled = !isEdit,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Dropdown Pilihan "Jenis Unit": HANYA opsi operasional aktif (Excavator Long Arm, Excavator Mini, Excavator Standar, Motor Grader, Compactor)
                Column {
                    Text("Jenis Unit Operasional Aktif:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Slate700)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { isJenisDropdownOpen = true },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Emerald600),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = Emerald50)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(selectedJenis, fontWeight = FontWeight.Bold, color = Emerald900, fontSize = 13.sp)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Emerald700)
                            }
                        }

                        DropdownMenu(
                            expanded = isJenisDropdownOpen,
                            onDismissRequest = { isJenisDropdownOpen = false },
                            modifier = Modifier.fillMaxWidth(0.85f)
                        ) {
                            UnitConstants.AKTIF.forEach { jenisOpt ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = jenisOpt,
                                            fontWeight = if (jenisOpt == selectedJenis) FontWeight.Bold else FontWeight.Normal,
                                            color = if (jenisOpt == selectedJenis) Emerald700 else Slate800,
                                            fontSize = 13.sp
                                        )
                                    },
                                    onClick = {
                                        selectedJenis = jenisOpt
                                        isJenisDropdownOpen = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Merk / Model
                OutlinedTextField(
                    value = brandModel,
                    onValueChange = { brandModel = it },
                    label = { Text("Merk / Model Unit") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Kasie & Wilayah
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = supervisor,
                        onValueChange = { supervisor = it },
                        label = { Text("Kasie Penanggung Jawab") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = wilayah,
                        onValueChange = { wilayah = it },
                        label = { Text("Wilayah") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                // HM Terakhir & Standar
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = lastHmStr,
                        onValueChange = { lastHmStr = it },
                        label = { Text("HM Awal/Terakhir") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = minHmStr,
                        onValueChange = { minHmStr = it },
                        label = { Text("Min HM") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = maxHmStr,
                        onValueChange = { maxHmStr = it },
                        label = { Text("Max HM") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Actions
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Batal")
                    }
                    Button(
                        onClick = {
                            val cleanKode = kodeUnit.trim()
                            if (cleanKode.isBlank()) {
                                errorMessage = "Kode unit tidak boleh kosong"
                                return@Button
                            }
                            if (!UnitConstants.isJenisUnitValid(selectedJenis)) {
                                errorMessage = "Jenis unit $selectedJenis tidak diperbolehkan"
                                return@Button
                            }
                            val lastHm = lastHmStr.toDoubleOrNull() ?: 0.0
                            val minHm = minHmStr.toDoubleOrNull() ?: 6.0
                            val maxHm = maxHmStr.toDoubleOrNull() ?: 10.0

                            onSave(
                                UnitEntity(
                                    unitCode = cleanKode,
                                    unitType = selectedJenis,
                                    brandModel = brandModel.trim().ifBlank { cleanKode },
                                    lastHm = lastHm,
                                    supervisorAssigned = supervisor.trim(),
                                    status = status,
                                    hmMinStandar = minHm,
                                    hmMaxStandar = maxHm,
                                    wilayah = wilayah.trim()
                                )
                            )
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Simpan Unit", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun CleanLokasiTab(lokasiList: List<LokasiEntity>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Daftar Master Lokasi Kerja Tambang (Tersinkron):", fontSize = 12.sp, color = Slate500)
        if (lokasiList.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                Text("Belum ada data lokasi kerja.", color = Slate400, fontSize = 13.sp)
            }
        } else {
            lokasiList.forEach { lok ->
                LokasiSelectorItemCard(
                    lokasi = lok,
                    isSelected = false,
                    onClick = {}
                )
            }
        }
    }
}

@Composable
fun CleanActivitiesTab(activities: List<ActivityMasterEntity>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Daftar Master Aktivitas & Kode SAP Perusahaan:", fontSize = 12.sp, color = Slate500)
        if (activities.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                Text("Belum ada aktivitas kerja.", color = Slate400, fontSize = 13.sp)
            }
        } else {
            activities.forEach { act ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF8FAFC),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(act.activityName, fontWeight = FontWeight.Bold, color = Slate900, fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(shape = RoundedCornerShape(4.dp), color = Emerald100) {
                                    Text(
                                        text = act.sapCode,
                                        color = Emerald900,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text("Satuan Pengukuran: ${act.unitOfMeasure} • Kategori: ${act.category}", fontSize = 11.sp, color = Slate500)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CleanOperatorsTab(operators: List<OperatorEntity>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Daftar Operator Pelaksana Aktif Lapangan:", fontSize = 12.sp, color = Slate500)
        if (operators.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                Text("Belum ada operator terdaftar.", color = Slate400, fontSize = 13.sp)
            }
        } else {
            operators.forEach { op ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF8FAFC),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(op.name, fontWeight = FontWeight.Bold, color = Slate900, fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFE0E7FF)) {
                                    Text(
                                        text = op.badgeNumber,
                                        color = Color(0xFF3730A3),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text("Kasie: ${op.supervisorAssigned} • Status: ${op.status}", fontSize = 11.sp, color = Slate500)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CleanKendalaTab(kendalaList: List<KendalaEntity>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Daftar Master Kendala Lapangan (Sinkron Web Admin):",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Slate500
            )
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Emerald50,
                border = androidx.compose.foundation.BorderStroke(1.dp, Emerald100)
            ) {
                Text(
                    text = "${kendalaList.size} Kendala Aktif",
                    color = Emerald700,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }

        if (kendalaList.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                Text("Belum ada data kendala terdaftar.", color = Slate400, fontSize = 13.sp)
            }
        } else {
            kendalaList.forEachIndexed { index, knd ->
                val badgeBg = when (knd.kategori.uppercase()) {
                    "CUACA" -> Color(0xFFE0F2FE)
                    "MEKANIKAL" -> Color(0xFFFEE2E2)
                    "LOGISTIK" -> Color(0xFFFEF3C7)
                    else -> Color(0xFFF1F5F9)
                }
                val badgeText = when (knd.kategori.uppercase()) {
                    "CUACA" -> Color(0xFF0369A1)
                    "MEKANIKAL" -> Color(0xFF991B1B)
                    "LOGISTIK" -> Color(0xFF92400E)
                    else -> Slate700
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF8FAFC),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Nomor Urut 1 s/d 15
                        Surface(
                            shape = CircleShape,
                            color = Slate200,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "${index + 1}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Slate700
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = knd.namaKendala,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900,
                                    fontSize = 13.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(shape = RoundedCornerShape(4.dp), color = badgeBg) {
                                    Text(
                                        text = knd.kodeKendala,
                                        color = badgeText,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Kategori: ${knd.kategori}${if (knd.deskripsi.isNotBlank()) " • ${knd.deskripsi}" else ""}",
                                fontSize = 11.sp,
                                color = Slate500
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CleanSyncLogsTab(logs: List<SyncLogItem>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Riwayat Transaksi Jaringan dan Sinkronisasi:", fontSize = 12.sp, color = Slate500)
        if (logs.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                Text("Belum ada log sinkronisasi.", color = Slate400, fontSize = 13.sp)
            }
        } else {
            logs.forEach { item ->
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (item.isSuccess) Color(0xFFF8FAFC) else ErrorRose50,
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (item.isSuccess) Slate200 else ErrorRose.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = item.endpoint, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Slate900, fontFamily = FontFamily.Monospace)
                            Text(text = item.timestamp, fontSize = 10.sp, color = Slate400)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = item.detail, fontSize = 11.sp, color = Slate700, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(text = item.status, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (item.isSuccess) Emerald700 else ErrorRose)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Dialog Distribusi & Pemasangan Aplikasi (Download APK, Bagikan WhatsApp, & Solusi Instalasi)
 */
@Composable
fun DistributionDialog(
    onDismiss: () -> Unit,
    currentApkUrl: String,
    onSaveApkLocally: () -> Unit,
    onShareWhatsApp: () -> Unit,
    onShareGeneral: () -> Unit,
    onInstallLocalApk: () -> Unit,
    onOpenInBrowser: (String) -> Unit,
    onUpdateApkUrl: (String) -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    var isEditingUrl by remember { mutableStateOf(false) }
    var urlInput by remember(currentApkUrl) { mutableStateOf(currentApkUrl) }
    var isCopied by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(scrollState)
            ) {
                // Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(Emerald50, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = null,
                            tint = Emerald600,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Distribusi RKCE Mobile",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Slate900
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Emerald100
                            ) {
                                Text(
                                    text = "v1.2 Update",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Emerald700,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Link installer APK resmi & pembaruan aplikasi lapangan",
                            fontSize = 11.sp,
                            color = Slate500,
                            lineHeight = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Info Box Tautan Download Resmi (GitHub / Cloud)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Slate100,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Tautan Installer Pembaruan:",
                                fontSize = 11.sp,
                                color = Slate700,
                                fontWeight = FontWeight.SemiBold
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isCopied) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Emerald50
                                    ) {
                                        Text(
                                            text = "Tersalin!",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Emerald600,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                IconButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(currentApkUrl))
                                        isCopied = true
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Salin Tautan",
                                        tint = Slate600,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { isEditingUrl = !isEditingUrl },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Tautan",
                                        tint = Slate600,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        if (isEditingUrl) {
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = urlInput,
                                onValueChange = { urlInput = it },
                                label = { Text("URL Download APK Kustom", fontSize = 11.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        urlInput = currentApkUrl
                                        isEditingUrl = false
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Batal", fontSize = 11.sp)
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Button(
                                    onClick = {
                                        onUpdateApkUrl(urlInput)
                                        isEditingUrl = false
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Emerald600)
                                ) {
                                    Text("Simpan", fontSize = 11.sp)
                                }
                            }
                        } else {
                            Text(
                                text = currentApkUrl,
                                fontSize = 10.sp,
                                color = Emerald900,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Tombol Aksi Cepat Tautan: Buka di Browser & Preset Rilis
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onOpenInBrowser(currentApkUrl) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Emerald700)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudDownload,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Buka di Browser", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    onUpdateApkUrl(HeavyTrackViewModel.GITHUB_RELEASES_PAGE_URL)
                                    onOpenInBrowser(HeavyTrackViewModel.GITHUB_RELEASES_PAGE_URL)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Slate700)
                            ) {
                                Text("Halaman Rilis GitHub", fontSize = 10.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Tombol Aksi Utama: DOWNLOAD / SIMPAN FILE APK RESMI KE HP
                Button(
                    onClick = onSaveApkLocally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("btn_download_apk"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(22.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Unduh Berkas Installer APK (.apk)", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                        Text("Simpan file APK versi terbaru langsung ke folder Download HP", fontSize = 10.sp, color = Color.White.copy(alpha = 0.9f))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Tombol Aksi 2: Bagikan File APK via WhatsApp
                Button(
                    onClick = onShareWhatsApp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("btn_share_whatsapp"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Kirim Berkas APK Pembaruan v1.2 via WhatsApp", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                        Text("Langsung kirim berkas .apk asli ke chat (Paling Praktis & Bebas Error)", fontSize = 10.sp, color = Color.White.copy(alpha = 0.9f))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Tombol Aksi 3: Bagikan Berkas APK ke Aplikasi Lain
                Button(
                    onClick = onShareGeneral,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("btn_share_general"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Slate800)
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Kirim File APK ke Aplikasi Lain (Bluetooth / Drive)", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color.White)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Tombol Aksi 4: Coba Buka / Pasang Installer di HP Ini
                OutlinedButton(
                    onClick = onInstallLocalApk,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("btn_install_test"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp), tint = Slate700)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Coba Pasang / Buka Penginstal Paket di HP Ini", fontSize = 12.sp, color = Slate700)
                }

                Spacer(modifier = Modifier.height(14.dp))

                // KARTU PANDUAN: SOLUSI MENGAPA GAGAL DIINSTAL DI HP LAIN & PEMBARUAN APLIKASI
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = WarningAmber50,
                    border = androidx.compose.foundation.BorderStroke(1.dp, WarningAmber.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = WarningAmber,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Panduan Pembaruan Aplikasi & Instalasi HP Lain:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Slate900
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "1. PENTING - Solusi Jika Gagal Update ('Aplikasi Tidak Terpasang'):\n" +
                                   "   Jika di HP tujuan sebelumnya sudah pernah terpasang aplikasi RKCE versi lama, Android dapat memblokir pembaruan karena perbedaan tanda tangan (signature). Solusinya: Hapus / copot (UNINSTALL) versi lama terlebih dahulu, lalu pasang berkas pembaruan v1.2 ini.\n\n" +
                                   "2. Peringatan Play Protect / 'Aplikasi Tidak Dikenal':\n" +
                                   "   Aplikasi didistribusikan langsung tanpa Play Store. Jika Play Protect memblokir, klik 'Detail Lainnya' (More details) lalu pilih 'Tetap Instal' (Install anyway).\n\n" +
                                   "3. Izin 'Sumber Tidak Dikenal':\n" +
                                   "   Saat membuka berkas dari WhatsApp atau File Manager, masuk ke 'Setelan' dan aktifkan 'Izinkan dari sumber ini'.\n\n" +
                                   "4. Berkas Asli 100% Utuh & Terkini:\n" +
                                   "   Gunakan tombol 'Kirim Berkas APK via WhatsApp' di atas. Sistem mengirim berkas biner asli aplikasi yang sedang berjalan (v1.2) sehingga berkas dijamin lengkap dan tidak rusak (bukan 0 byte).",
                            fontSize = 11.sp,
                            color = Slate700,
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Tutup", color = Slate700, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
