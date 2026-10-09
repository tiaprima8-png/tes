package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.ui.components.FuelBadge
import com.example.ui.components.ShiftBadge
import com.example.ui.components.SyncStatusBadge
import com.example.ui.components.UnitStatusBadge
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald50
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Emerald700
import com.example.ui.theme.Emerald900
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
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
    val reports by viewModel.reports.collectAsState()
    val unsyncedCount by viewModel.unsyncedCount.collectAsState()
    val isSyncingPending by viewModel.isSyncingPending.collectAsState()
    val activeSupervisor by viewModel.activeSupervisor.collectAsState()

    var selectedFilter by remember { mutableStateOf("ALL") } // ALL, PENDING, SYNCED
    var selectedReportForDetail by remember { mutableStateOf<ActivityReportEntity?>(null) }

    val filteredReports = remember(reports, selectedFilter) {
        when (selectedFilter) {
            "PENDING" -> reports.filter { !it.isSynced }
            "SYNCED" -> reports.filter { it.isSynced }
            else -> reports
        }
    }

    val totalHm = remember(reports) {
        reports.sumOf { it.hmDailyRunning }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .testTag("history_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Summary & Stats Card
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
                                text = "Riwayat Aktivitas Saya",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                            )
                            Text(
                                text = "Pengawas: $activeSupervisor",
                                style = MaterialTheme.typography.bodySmall.copy(color = Slate500)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Emerald50
                        ) {
                            Text(
                                text = "${reports.size} Total Laporan",
                                color = Emerald900,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

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
                                Text("Total HM Operasi", style = MaterialTheme.typography.bodySmall.copy(color = Slate500, fontSize = 11.sp))
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

                    // Sync button if unsynced items exist
                    if (unsyncedCount > 0) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.syncPendingReports() },
                            enabled = !isSyncingPending,
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("sync_pending_reports_button")
                        ) {
                            if (isSyncingPending) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Menyinkronkan ke Web Admin...", fontSize = 13.sp)
                            } else {
                                Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Sinkronkan $unsyncedCount Laporan Tertunda", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }

        // Filter chips row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(
                    "ALL" to "Semua (${reports.size})",
                    "PENDING" to "Tertunda ($unsyncedCount)",
                    "SYNCED" to "Tersinkron (${reports.count { it.isSynced }})"
                ).forEach { (key, label) ->
                    val isSelected = selectedFilter == key
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) Emerald600 else Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) Emerald600 else Slate200),
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { selectedFilter = key }
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) Color.White else Slate700,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // List of reports
        if (filteredReports.isEmpty()) {
            item {
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
                            text = "Belum Ada Laporan",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Slate900)
                        )
                        Text(
                            text = "Input formulir pelaporan harian alat berat untuk melihat riwayat aktivitas di sini.",
                            style = MaterialTheme.typography.bodySmall.copy(color = Slate500),
                            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                        )
                        Button(
                            onClick = onNavigateToForm,
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Buka Formulir Input")
                        }
                    }
                }
            }
        } else {
            items(filteredReports, key = { it.id }) { report ->
                ReportHistoryCard(
                    report = report,
                    onClick = { selectedReportForDetail = report }
                )
            }
        }
    }

    // Detail Dialog
    selectedReportForDetail?.let { rep ->
        ReportDetailDialog(
            report = rep,
            onDismiss = { selectedReportForDetail = null },
            onDelete = { id -> viewModel.deleteReport(id) }
        )
    }
}

@Composable
fun ReportHistoryCard(
    report: ActivityReportEntity,
    onClick: () -> Unit
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
                            color = Slate900
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFFF1F5F9)
                    ) {
                        Text(
                            text = report.unitType,
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
                    if (!report.photoPath.isNullOrBlank() || !report.photoBase64.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Emerald100
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    tint = Emerald700,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "${report.photoSizeKb ?: 145}KB",
                                    color = Emerald900,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    ShiftBadge(shift = report.shift)
                    SyncStatusBadge(isSynced = report.isSynced)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Operational Status Chip & Fuel Log Badge
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                UnitStatusBadge(status = report.unitStatus)
                if (report.isRefueled && report.fuelLiters > 0.0) {
                    FuelBadge(liters = report.fuelLiters)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Activity & SAP Code
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Assignment,
                    contentDescription = null,
                    tint = Emerald600,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = report.activityName,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Slate900
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Emerald100
                ) {
                    Text(
                        text = report.sapCode,
                        color = Emerald900,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Operator & Location
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Slate400,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = report.operatorName,
                        style = MaterialTheme.typography.bodySmall.copy(color = Slate500),
                        maxLines = 1
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Slate400,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = report.workLocation,
                        style = MaterialTheme.typography.bodySmall.copy(color = Slate500),
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // HM Running Highlight Box
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
                                fontWeight = FontWeight.Medium
                            )
                        )
                        Text(
                            text = "Tanggal: ${report.reportDate}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Slate400,
                                fontSize = 11.sp
                            )
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "+${String.format("%.2f", report.hmDailyRunning)} Jam",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Emerald700
                                )
                            )
                            if (report.workOutput > 0) {
                                Text(
                                    text = "${String.format("%.1f", report.workOutput)} ${report.unitOfMeasure}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Emerald900,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
