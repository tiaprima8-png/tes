package com.example.ui.screens

import android.content.Context
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
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
import com.example.ui.theme.Emerald900
import com.example.ui.theme.ErrorRose
import com.example.ui.theme.ErrorRose50
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.ui.viewmodel.HeavyTrackViewModel

@Composable
fun MasterDataScreen(
    viewModel: HeavyTrackViewModel
) {
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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .testTag("master_data_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
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
                                    text = "Pengawas: $activeSupervisor",
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

                    // Tombol Aksi Elegan: "Download & Bagikan Installer APK"
                    Button(
                        onClick = { showDistributionDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_open_distribution"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald600)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Download & Bagikan Installer APK",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
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
                            0 -> CleanUnitsTab(units)
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

    // Modal Dialog Distribusi APK & Share WhatsApp
    if (showDistributionDialog) {
        DistributionDialog(
            onDismiss = { showDistributionDialog = false },
            onDownload = {
                viewModel.downloadApk(context)
                showDistributionDialog = false
            },
            onShareWhatsApp = {
                viewModel.shareApkViaWhatsApp(context)
                showDistributionDialog = false
            }
        )
    }
}

@Composable
fun CleanUnitsTab(units: List<UnitEntity>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Daftar Unit Operasional di bawah Pengawasan:", fontSize = 12.sp, color = Slate500)
        if (units.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                Text("Belum ada data unit terdaftar.", color = Slate400, fontSize = 13.sp)
            }
        } else {
            units.forEach { unit ->
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
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${unit.brandModel} • Pengawas: ${unit.supervisorAssigned}",
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
                            Text("Pengawas: ${op.supervisorAssigned} • Status: ${op.status}", fontSize = 11.sp, color = Slate500)
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
        Text("Daftar Master Kendala Operasional (Sinkron Web Admin):", fontSize = 12.sp, color = Slate500)
        if (kendalaList.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                Text("Belum ada data kendala terdaftar.", color = Slate400, fontSize = 13.sp)
            }
        } else {
            kendalaList.forEach { knd ->
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
                                Text(knd.namaKendala, fontWeight = FontWeight.Bold, color = Slate900, fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFFEE2E2)) {
                                    Text(
                                        text = knd.kodeKendala,
                                        color = Color(0xFF991B1B),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Kategori: ${knd.kategori}${if (knd.deskripsi.isNotBlank()) " • Keterangan: ${knd.deskripsi}" else ""}",
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
 * Dialog Distribusi Aplikasi (Download APK & Bagikan WhatsApp)
 */
@Composable
fun DistributionDialog(
    onDismiss: () -> Unit,
    onDownload: () -> Unit,
    onShareWhatsApp: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(22.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(Emerald50, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = null,
                            tint = Emerald600,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Distribusi Aplikasi RKCE Mobile",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Slate900
                        )
                        Text(
                            text = "Unduh atau bagikan installer ke sesama pengawas",
                            fontSize = 11.sp,
                            color = Slate500
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Info Box Tautan Download
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF1F5F9),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Tautan Langsung File APK:",
                            fontSize = 11.sp,
                            color = Slate500,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = HeavyTrackViewModel.APK_DIRECT_DOWNLOAD_URL,
                            fontSize = 11.sp,
                            color = Emerald900,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Tombol 1: Unduh APK
                Button(
                    onClick = onDownload,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_download_apk"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald600)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Unduh File APK ke Perangkat", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Tombol 2: Bagikan via WhatsApp
                Button(
                    onClick = onShareWhatsApp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_share_whatsapp"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)) // WhatsApp Brand Green
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Bagikan via WhatsApp", fontWeight = FontWeight.Bold, color = Color.White)
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Tutup", color = Slate700)
                }
            }
        }
    }
}
