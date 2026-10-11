package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ActivityReportEntity
import com.example.data.local.entity.RencanaKerjaEntity
import com.example.ui.components.FuelBadge
import com.example.ui.components.FullscreenPhotoPreviewDialog
import com.example.ui.components.ReportPhotoThumbnail
import com.example.ui.components.ShiftBadge
import com.example.ui.components.SyncStatusBadge
import com.example.ui.components.UnitStatusBadge
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald50
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Emerald700
import com.example.ui.theme.Emerald900
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WarningAmber50
import com.example.ui.viewmodel.HeavyTrackViewModel

@Composable
fun HistoryScreen(
    viewModel: HeavyTrackViewModel,
    onNavigateToForm: () -> Unit
) {
    val myReports by viewModel.reports.collectAsState()
    val allReports by viewModel.allReports.collectAsState()
    val allRencanaList by viewModel.allRencanaGlobal.collectAsState()
    val unsyncedCount by viewModel.unsyncedCount.collectAsState()
    val isSyncingPending by viewModel.isSyncingPending.collectAsState()
    val activeSupervisor by viewModel.activeSupervisor.collectAsState()

    // Tab pilihan: 0 = Realisasi Kerja Lapangan, 1 = Rencana Kerja Shift
    var historyTab by remember { mutableIntStateOf(0) }

    // Toggle cakupan data: true = Tampilkan Semua Data Terinput, false = Hanya Pengawas Aktif
    var showAllDataScope by remember { mutableStateOf(true) }

    // Filter status sinkron: ALL, PENDING, SYNCED
    var selectedFilter by remember { mutableStateOf("ALL") }

    // Pencarian cepat
    var searchQuery by remember { mutableStateOf("") }

    var selectedReportForDetail by remember { mutableStateOf<ActivityReportEntity?>(null) }
    var selectedRencanaForDetail by remember { mutableStateOf<RencanaKerjaEntity?>(null) }
    var photoPreviewReport by remember { mutableStateOf<ActivityReportEntity?>(null) }

    // Dataset laporan realisasi sesuai cakupan
    val baseReports = if (showAllDataScope) allReports else myReports

    val filteredReports = remember(baseReports, selectedFilter, searchQuery) {
        baseReports.filter { rep ->
            val matchesFilter = when (selectedFilter) {
                "PENDING" -> !rep.isSynced
                "SYNCED" -> rep.isSynced
                else -> true
            }
            val matchesQuery = if (searchQuery.isBlank()) {
                true
            } else {
                val q = searchQuery.trim().lowercase()
                rep.unitCode.lowercase().contains(q) ||
                        rep.operatorName.lowercase().contains(q) ||
                        rep.activityName.lowercase().contains(q) ||
                        rep.workLocation.lowercase().contains(q) ||
                        rep.supervisorName.lowercase().contains(q) ||
                        rep.reportDate.lowercase().contains(q)
            }
            matchesFilter && matchesQuery
        }
    }

    val filteredRencana = remember(allRencanaList, showAllDataScope, activeSupervisor, searchQuery) {
        allRencanaList.filter { ren ->
            val matchesSupervisor = if (showAllDataScope) true else ren.supervisorName.equals(activeSupervisor, ignoreCase = true)
            val matchesQuery = if (searchQuery.isBlank()) {
                true
            } else {
                val q = searchQuery.trim().lowercase()
                ren.kodeUnit.lowercase().contains(q) ||
                        ren.operator.lowercase().contains(q) ||
                        ren.namaAktivitas.lowercase().contains(q) ||
                        ren.kodeLokasi.lowercase().contains(q) ||
                        ren.supervisorName.lowercase().contains(q)
            }
            matchesSupervisor && matchesQuery
        }
    }

    val totalHm = remember(baseReports) {
        baseReports.sumOf { it.hmDailyRunning }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .testTag("history_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ==========================================
        // 1. HEADER CARD: RINGKASAN & STATISTIK
        // ==========================================
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
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
                        Column {
                            Text(
                                text = "Riwayat Data Lapangan",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900,
                                    fontSize = 18.sp
                                )
                            )
                            Text(
                                text = "Pengawas Aktif: $activeSupervisor",
                                style = MaterialTheme.typography.bodySmall.copy(color = Slate500, fontSize = 11.sp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Emerald50
                        ) {
                            Text(
                                text = "${baseReports.size} Laporan",
                                color = Emerald900,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Stats Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF8FAFC),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Total HM Berjalan", style = MaterialTheme.typography.bodySmall.copy(color = Slate500, fontSize = 11.sp))
                                Text("${String.format("%.1f", totalHm)} Jam", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Emerald700))
                            }
                        }

                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = if (unsyncedCount > 0) WarningAmber50 else Color(0xFFF8FAFC),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (unsyncedCount > 0) WarningAmber.copy(alpha = 0.5f) else Slate200)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Tertunda Sync", style = MaterialTheme.typography.bodySmall.copy(color = if (unsyncedCount > 0) Color(0xFFB45309) else Slate500, fontSize = 11.sp))
                                Text("$unsyncedCount Laporan", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = if (unsyncedCount > 0) Color(0xFFB45309) else Slate900))
                            }
                        }
                    }

                    // Tombol Sinkronisasi
                    Spacer(modifier = Modifier.height(12.dp))
                    val isSyncEnabled = unsyncedCount > 0 && !isSyncingPending
                    Button(
                        onClick = {
                            if (unsyncedCount > 0) {
                                viewModel.syncPendingReports()
                            }
                        },
                        enabled = isSyncEnabled,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Emerald600,
                            contentColor = Color.White,
                            disabledContainerColor = Color(0xFFF1F5F9),
                            disabledContentColor = Slate500
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("sync_pending_reports_button")
                    ) {
                        if (isSyncingPending) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Menyinkronkan ke Web Admin...", fontSize = 13.sp)
                        } else if (unsyncedCount > 0) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sinkronkan $unsyncedCount Laporan Tertunda", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        } else {
                            Icon(Icons.Default.CloudDone, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Semua Laporan Tersinkron Lengkap", fontWeight = FontWeight.SemiBold, color = Slate700, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // ==========================================
        // 2. TAB SELECTION: REALISASI VS RENCANA
        // ==========================================
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                modifier = Modifier.fillMaxWidth()
            ) {
                TabRow(
                    selectedTabIndex = historyTab,
                    containerColor = Color.White,
                    contentColor = Emerald600,
                    divider = {}
                ) {
                    Tab(
                        selected = historyTab == 0,
                        onClick = { historyTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Realisasi (${baseReports.size})",
                                    fontWeight = if (historyTab == 0) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    )
                    Tab(
                        selected = historyTab == 1,
                        onClick = { historyTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Assignment, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Rencana (${filteredRencana.size})",
                                    fontWeight = if (historyTab == 1) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    )
                }
            }
        }

        // ==========================================
        // 3. SEARCH & SCOPE CONTROLS
        // ==========================================
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Search Input Field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari unit, operator, aktivitas, lokasi...", color = Slate400, fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = Slate400, modifier = Modifier.size(18.dp))
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Hapus pencarian", tint = Slate400, modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Emerald600,
                        unfocusedBorderColor = Slate200
                    ),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                )

                // Scope & Status Filter Chips Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Scope chip: Semua vs Pengawas Saya
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (showAllDataScope) Emerald100 else Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (showAllDataScope) Emerald600 else Slate200),
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { showAllDataScope = !showAllDataScope }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = null,
                                tint = if (showAllDataScope) Emerald900 else Slate600,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (showAllDataScope) "Semua Data Input" else "Hanya Saya",
                                color = if (showAllDataScope) Emerald900 else Slate700,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Status Chips for Realisasi
                    if (historyTab == 0) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(
                                "ALL" to "Semua",
                                "PENDING" to "Tertunda",
                                "SYNCED" to "Tersinkron"
                            ).forEach { (key, label) ->
                                val isSelected = selectedFilter == key
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (isSelected) Emerald600 else Color.White,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) Emerald600 else Slate200),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable { selectedFilter = key }
                                ) {
                                    Text(
                                        text = label,
                                        color = if (isSelected) Color.White else Slate700,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // 4. LIST CONTENT
        // ==========================================
        if (historyTab == 0) {
            // Tab Realisasi
            if (filteredReports.isEmpty()) {
                item {
                    EmptyHistoryPlaceholder(
                        title = if (searchQuery.isNotBlank()) "Data Tidak Ditemukan" else "Belum Ada Laporan Realisasi",
                        message = if (searchQuery.isNotBlank()) "Tidak ada laporan yang cocok dengan kata kunci '$searchQuery'." else "Input laporan realisasi alat berat melalui Formulir untuk melihat riwayat lengkap di sini.",
                        buttonText = "Buka Formulir Input",
                        onButtonClick = onNavigateToForm
                    )
                }
            } else {
                items(filteredReports, key = { it.id }) { report ->
                    ReportHistoryCard(
                        report = report,
                        onClick = { selectedReportForDetail = report },
                        onPhotoClick = { photoPreviewReport = report }
                    )
                }
            }
        } else {
            // Tab Rencana Kerja
            if (filteredRencana.isEmpty()) {
                item {
                    EmptyHistoryPlaceholder(
                        title = "Belum Ada Rencana Kerja",
                        message = "Rencana kerja yang dibuat oleh pengawas shift akan tercatat lengkap di daftar ini.",
                        buttonText = "Buat Rencana Kerja",
                        onButtonClick = onNavigateToForm
                    )
                }
            } else {
                items(filteredRencana, key = { it.id }) { rencana ->
                    RencanaHistoryCard(
                        rencana = rencana,
                        onClick = { selectedRencanaForDetail = rencana }
                    )
                }
            }
        }
    }

    // ==========================================
    // DIALOG DETAIL LAPORAN REALISASI (BISA DI-SCROLL KE BAWAH)
    // ==========================================
    selectedReportForDetail?.let { rep ->
        ReportDetailDialog(
            report = rep,
            onDismiss = { selectedReportForDetail = null },
            onDelete = { id -> viewModel.deleteReport(id) }
        )
    }

    // Fullscreen Photo Preview
    photoPreviewReport?.let { rep ->
        FullscreenPhotoPreviewDialog(
            photoPath = rep.photoPath,
            photoBase64 = rep.photoBase64,
            unitCode = rep.unitCode,
            reportDate = rep.reportDate,
            nomorSpk = null,
            photoSizeKb = rep.photoSizeKb,
            onDismiss = { photoPreviewReport = null }
        )
    }
}

@Composable
fun ReportHistoryCard(
    report: ActivityReportEntity,
    onClick: () -> Unit,
    onPhotoClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("report_card_${report.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header Row: Unit Code + Status & Shift
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = report.unitCode,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Slate900,
                            fontSize = 16.sp
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFFF1F5F9)
                    ) {
                        Text(
                            text = report.unitType.ifBlank { "HE" },
                            color = Slate700,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ShiftBadge(shift = report.shift)
                    SyncStatusBadge(isSynced = report.isSynced)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Body Row: Photo Thumbnail (Kiri) + Job Info (Kanan)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Compact thumbnail foto bukti
                ReportPhotoThumbnail(
                    photoPath = report.photoPath,
                    photoBase64 = report.photoBase64,
                    photoSizeKb = report.photoSizeKb,
                    onClick = onPhotoClick
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Detail Kolom Kanan
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        UnitStatusBadge(status = report.unitStatus)
                        if (report.isRefueled && report.fuelLiters > 0.0) {
                            FuelBadge(liters = report.fuelLiters)
                        }
                        if (report.isHmAwalCorrected) {
                            Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFFEF3C7)) {
                                Text("HM Koreksi", color = Color(0xFFB45309), fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Aktivitas & SAP
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Assignment,
                            contentDescription = null,
                            tint = Emerald600,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = report.activityName,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Slate900
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Operator & Lokasi
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Slate400, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = report.operatorName,
                                style = MaterialTheme.typography.bodySmall.copy(color = Slate500, fontSize = 11.sp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 6.dp)) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Slate400, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = report.workLocation,
                                style = MaterialTheme.typography.bodySmall.copy(color = Slate500, fontSize = 11.sp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Pengawas & Tanggal
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Engineering, contentDescription = null, tint = Slate400, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = report.supervisorName,
                                style = MaterialTheme.typography.bodySmall.copy(color = Slate400, fontSize = 10.sp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            text = report.reportDate,
                            style = MaterialTheme.typography.bodySmall.copy(color = Slate400, fontSize = 10.sp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // HM Running Banner & Output
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = Emerald50,
                border = androidx.compose.foundation.BorderStroke(1.dp, Emerald100)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "HM: ${String.format("%.1f", report.hmAwal)} ➔ ${String.format("%.1f", report.hmAkhir)}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Slate700,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        )
                        if (!report.kendalaNama.isNullOrBlank()) {
                            Text(
                                text = "Kendala: ${report.kendalaNama}",
                                color = Color(0xFFDC2626),
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "+${String.format("%.2f", report.hmDailyRunning)} Jam",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Emerald700,
                                    fontSize = 14.sp
                                )
                            )
                            if (report.workOutput > 0) {
                                Text(
                                    text = "${String.format("%.1f", report.workOutput)} ${report.unitOfMeasure}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Emerald900,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Catatan Singkat jika ada
            if (report.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Notes, contentDescription = null, tint = Slate400, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = report.notes,
                        style = MaterialTheme.typography.bodySmall.copy(color = Slate500, fontSize = 11.sp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "Ketuk untuk lihat detail selengkapnya ➔",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Emerald700
                )
            }
        }
    }
}

@Composable
fun RencanaHistoryCard(
    rencana: RencanaKerjaEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("rencana_card_${rencana.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = rencana.kodeUnit,
                        fontWeight = FontWeight.Bold,
                        color = Slate900,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    UnitStatusBadge(status = rencana.statusUnit)
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (rencana.isRealized) Emerald100 else Color(0xFFFEF3C7)
                ) {
                    Text(
                        text = if (rencana.isRealized) "SUDAH REALISASI" else "AKTIF / TERENCANA",
                        color = if (rencana.isRealized) Emerald900 else Color(0xFFB45309),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Assignment, contentDescription = null, tint = Emerald600, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = rencana.namaAktivitas,
                    fontWeight = FontWeight.SemiBold,
                    color = Slate900,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Slate400, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(rencana.operator, color = Slate600, fontSize = 11.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Slate400, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(rencana.kodeLokasi, color = Slate600, fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Shift ${rencana.shiftKerja} • Tgl: ${rencana.tanggalFormat}",
                    color = Slate400,
                    fontSize = 10.sp
                )
                Text(
                    text = "Pengawas: ${rencana.supervisorName}",
                    color = Slate500,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
fun EmptyHistoryPlaceholder(
    title: String,
    message: String,
    buttonText: String,
    onButtonClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = null,
                tint = Slate400,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Slate900)
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall.copy(color = Slate500),
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )
            Button(
                onClick = onButtonClick,
                colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(buttonText)
            }
        }
    }
}
