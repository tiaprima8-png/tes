package com.example.ui.screens

import android.app.TimePickerDialog
import android.net.Uri
import java.util.Locale
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.ui.focus.onFocusEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditLocation
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockClock
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ActivityMasterEntity
import com.example.data.local.entity.KendalaEntity
import com.example.data.local.entity.LokasiEntity
import com.example.data.local.entity.OperatorEntity
import com.example.data.local.entity.RencanaKerjaEntity
import com.example.ui.viewmodel.KendalaItemFormState
import com.example.ui.components.ActivitySearchableSelectorSheet
import com.example.ui.components.KendalaSearchableSelectorSheet
import com.example.ui.components.LokasiSearchableSelectorSheet
import com.example.ui.components.OperatorSearchableSelectorSheet
import com.example.ui.components.PhotoProofCard
import com.example.ui.components.SelectorFieldTrigger
import com.example.ui.components.UnitPlanSearchableSelectorSheet
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald200
import com.example.ui.theme.Emerald50
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Emerald700
import com.example.ui.theme.Emerald800
import com.example.ui.theme.Emerald900
import com.example.ui.theme.ErrorRose
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.WarningAmber
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import com.example.ui.viewmodel.HeavyTrackViewModel

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun FormReportScreen(
    viewModel: HeavyTrackViewModel,
    onNavigateToHistory: () -> Unit
) {
    val context = LocalContext.current
    val formSubTab by viewModel.formSubTab.collectAsState()
    val activeSupervisor by viewModel.activeSupervisor.collectAsState()

    val units by viewModel.units.collectAsState()
    val availableUnitsForRencana by viewModel.availableUnitsForRencana.collectAsState()
    val activities by viewModel.activities.collectAsState()
    val operators by viewModel.operators.collectAsState()
    val lokasiList by viewModel.lokasiList.collectAsState()
    val kendalaList by viewModel.kendalaList.collectAsState()
    val activeRencanaList by viewModel.activeRencanaList.collectAsState()

    val rencanaState by viewModel.rencanaFormState.collectAsState()
    val realisasiState by viewModel.realisasiFormState.collectAsState()
    val fleetProgress by viewModel.fleetShiftProgress.collectAsState()
    val isSocketConnected by viewModel.isSocketConnected.collectAsState()

    // 5. AUTO-FETCH DATA MASTER SAAT HALAMAN DIBUKA (ONRESUME / ONENTER)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.autoFetchMasterDataOnResume()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.autoFetchMasterDataOnResume()
    }

    // Selector Sheets State
    var isUnitSheetOpen by remember { mutableStateOf(false) }
    var isOperatorSheetOpen by remember { mutableStateOf(false) }
    var isRencanaActivitySheetOpen by remember { mutableStateOf(false) }
    var isLokasiSheetOpen by remember { mutableStateOf(false) }

    // Sub-Plan sheet routing: targetSubPlanId -> null for main form, or subPlan.id
    var activeSubPlanOperatorTarget by remember { mutableStateOf<String?>(null) }
    var activeSubPlanActivityTarget by remember { mutableStateOf<String?>(null) }
    var activeSubPlanLokasiTarget by remember { mutableStateOf<String?>(null) }

    // Realisasi Editable Searchable Sheets
    var isRealisasiActivitySheetOpen by remember { mutableStateOf(false) }
    var isRealisasiLokasiSheetOpen by remember { mutableStateOf(false) }
    var isKendalaSheetOpen by remember { mutableStateOf(false) }
    var activeKendalaIndex by remember { mutableStateOf<Int?>(null) }

    // Confirmation dialog for Over-HM (> max standard) & Extreme HM Warning (> 14 hours)
    var showOverHmConfirmDialog by remember { mutableStateOf(false) }
    var showExtremeHmWarningConfirmDialog by remember { mutableStateOf(false) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
    val keteranganBringIntoViewRequester = remember { BringIntoViewRequester() }

    // Image Picker for Realisasi Photo Proof
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.processAndCompressRealisasiPhoto(context, uri)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .testTag("form_report_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ======================================================================
        // 4. KARTU RINGKASAN PROGRES HARIAN DI BERANDA (SHIFT PROGRESS TRACKER)
        // ======================================================================
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                modifier = Modifier.fillMaxWidth().testTag("card_shift_progress_tracker")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = if (fleetProgress.isAllCompleted) Emerald100 else Color(0xFFFEF3C7),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (fleetProgress.isAllCompleted) Icons.Default.CheckCircle else Icons.Default.PrecisionManufacturing,
                                        contentDescription = null,
                                        tint = if (fleetProgress.isAllCompleted) Emerald700 else Color(0xFFD97706),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Progres Input Unit",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Slate900
                                )
                                Text(
                                    text = "${fleetProgress.realizedUnitsCount} dari ${fleetProgress.totalUnitsCount} Unit Selesai Terealisasi",
                                    fontSize = 11.sp,
                                    color = Slate500
                                )
                            }
                        }

                        // Badge status progres
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (fleetProgress.isAllCompleted) Emerald100 else Color(0xFFFEF3C7)
                        ) {
                            Text(
                                text = if (fleetProgress.isAllCompleted) "100% SELESAI" else "${(fleetProgress.progressPercentage * 100).toInt()}% TEREALISASI",
                                color = if (fleetProgress.isAllCompleted) Emerald900 else Color(0xFF92400E),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Progress bar visual (hijau jika 100% selesai, kuning jika masih ada unit tertunda)
                    androidx.compose.material3.LinearProgressIndicator(
                        progress = { fleetProgress.progressPercentage },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (fleetProgress.isAllCompleted) Emerald600 else Color(0xFFF59E0B),
                        trackColor = Color(0xFFE2E8F0)
                    )

                    if (fleetProgress.pendingUnitsCount > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        val pendingUnitNames = fleetProgress.unitItems
                            .filter { !it.isRealized }
                            .map { it.unitCode }
                            .take(5)
                            .joinToString(", ")
                        val remainingCount = fleetProgress.pendingUnitsCount - 5
                        val remainingText = if (remainingCount > 0) " +$remainingCount lainnya" else ""

                        Text(
                            text = "Unit belum realisasi: $pendingUnitNames$remainingText",
                            fontSize = 11.sp,
                            color = Color(0xFFB45309),
                            fontWeight = FontWeight.Medium
                        )
                    } else if (fleetProgress.totalUnitsCount > 0) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Seluruh unit telah dilaporkan lengkap sebelum pergantian shift.",
                            fontSize = 11.sp,
                            color = Emerald700,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // TAB HEADER DUA TAHAP (RENCANA VS REALISASI)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                modifier = Modifier.fillMaxWidth()
            ) {
                TabRow(
                    selectedTabIndex = formSubTab,
                    containerColor = Color.White,
                    contentColor = Emerald600,
                    divider = {}
                ) {
                    Tab(
                        selected = formSubTab == 0,
                        onClick = { viewModel.setFormSubTab(0) },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Assignment,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "1. Rencana Kerja",
                                    fontWeight = if (formSubTab == 0) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.sp
                                )
                            }
                        },
                        modifier = Modifier.testTag("tab_rencana_kerja")
                    )
                    Tab(
                        selected = formSubTab == 1,
                        onClick = { viewModel.setFormSubTab(1) },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.TaskAlt,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "2. Realisasi Kerja",
                                    fontWeight = if (formSubTab == 1) FontWeight.Bold else FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                if (activeRencanaList.isNotEmpty()) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = CircleShape,
                                        color = Emerald600
                                    ) {
                                        Text(
                                            text = "${activeRencanaList.size}",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                        },
                        modifier = Modifier.testTag("tab_realisasi_kerja")
                    )
                }
            }
        }

        // ==========================================
        // KONTEN TAHAP 1: FORMULIR RENCANA KERJA
        // ==========================================
        if (formSubTab == 0) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("card_rencana_kerja"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        // Header Persiapan Rencana Kerja Bersih (Sesuai Permintaan)
                        Text(
                            text = "Seluruh unit wajib dibuatkan rencana kerja",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Slate700
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Banner Info Kasus C: Unit Sudah Memiliki Rencana Hari Ini (Rencana Susulan)
                        if (rencanaState.isSusulan && rencanaState.selectedUnit != null) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFECFDF5), // Hijau lembut
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA7F3D0)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = Emerald600,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "Rencana Kerja Tambahan / Susulan",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = Emerald900
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Unit ini sudah memiliki rencana kerja hari ini. Anda sedang membuat Rencana Kerja Tambahan/Susulan.",
                                            fontSize = 12.sp,
                                            color = Emerald700,
                                            lineHeight = 16.sp
                                        )
                                    }
                                }
                            }
                        }

                        // 1. Status Unit Selector Pill [OPERASI] | [STANDBY] | [BREAKDOWN]
                        Text(
                            text = "1. Status Unit Operasional",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Slate700
                            ),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("OPERASI", "STANDBY", "BREAKDOWN").forEach { status ->
                                val isSelected = rencanaState.statusUnit == status
                                val (bgCol, txtCol) = when {
                                    isSelected && status == "OPERASI" -> Emerald600 to Color.White
                                    isSelected && status == "STANDBY" -> WarningAmber to Slate900
                                    isSelected && status == "BREAKDOWN" -> ErrorRose to Color.White
                                    else -> Color(0xFFF1F5F9) to Slate700
                                }
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = bgCol,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) bgCol else Slate200
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { viewModel.setRencanaStatus(status) }
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.padding(vertical = 10.dp)
                                    ) {
                                        Text(
                                            text = status,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = txtCol
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // 2. Unit Alat Berat Searchable Selector (Otomatis Tanggal, Deteksi Backlog & Tambah Susulan)
                        SelectorFieldTrigger(
                            label = "2. Unit Alat Berat (Deteksi Backlog & Rencana Susulan)",
                            selectedValue = rencanaState.selectedUnit?.let { u ->
                                when {
                                    rencanaState.isBacklog -> "${u.unitCode} - ${u.brandModel} [Backlog Kemarin]"
                                    rencanaState.isSusulan -> "${u.unitCode} - ${u.brandModel} [Rencana Susulan]"
                                    else -> "${u.unitCode} - ${u.brandModel}"
                                }
                            },
                            placeholder = if (availableUnitsForRencana.isEmpty())
                                "Tidak ada unit terdaftar"
                            else
                                "Pilih Unit Alat Berat binaan pengawas...",
                            leadingIcon = Icons.Default.PrecisionManufacturing,
                            badgeText = rencanaState.selectedUnit?.let {
                                when {
                                    rencanaState.isBacklog -> "BACKLOG H-1"
                                    rencanaState.isSusulan -> "SUSULAN"
                                    else -> "HARI INI"
                                }
                            },
                            testTag = "selector_rencana_unit",
                            onClick = { isUnitSheetOpen = true }
                        )

                        // Info Tanggal Otomatis Target
                        if (rencanaState.selectedUnit != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = when {
                                    rencanaState.isBacklog -> Color(0xFFFEF2F2)
                                    rencanaState.isSusulan -> Color(0xFFECFDF5)
                                    else -> Color(0xFFF0FDF4)
                                },
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    when {
                                        rencanaState.isBacklog -> Color(0xFFFCA5A5)
                                        rencanaState.isSusulan -> Color(0xFFA7F3D0)
                                        else -> Color(0xFF86EFAC)
                                    }
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = when {
                                            rencanaState.isBacklog -> Icons.Default.Warning
                                            rencanaState.isSusulan -> Icons.Default.Add
                                            else -> Icons.Default.Info
                                        },
                                        contentDescription = null,
                                        tint = when {
                                            rencanaState.isBacklog -> Color(0xFFDC2626)
                                            rencanaState.isSusulan -> Emerald700
                                            else -> Emerald700
                                        },
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = when {
                                                rencanaState.isBacklog -> "TARGET: TANGGAL KEMARIN (${rencanaState.targetDate}) - BACKLOG"
                                                rencanaState.isSusulan -> "TARGET: TANGGAL HARI INI (${rencanaState.targetDate}) - RENCANA SUSULAN"
                                                else -> "TARGET: TANGGAL HARI INI (${rencanaState.targetDate})"
                                            },
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = when {
                                                rencanaState.isBacklog -> Color(0xFF991B1B)
                                                rencanaState.isSusulan -> Emerald900
                                                else -> Emerald900
                                            }
                                        )
                                        Text(
                                            text = when {
                                                rencanaState.isBacklog -> "Unit belum diisi kemarin. Pengawas wajib menyelesaikan pengisian backlog ini terlebih dahulu."
                                                rencanaState.isSusulan -> "Unit ini sudah memiliki rencana kerja hari ini. Anda sedang membuat Rencana Kerja Tambahan/Susulan."
                                                else -> "Tanggal otomatis terisi oleh sistem. Pengguna tidak perlu menginput tanggal manual."
                                            },
                                            fontSize = 10.sp,
                                            color = when {
                                                rencanaState.isBacklog -> Color(0xFFB91C1C)
                                                rencanaState.isSusulan -> Emerald700
                                                else -> Emerald700
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // 3. Operator Pelaksana Utama
                        SelectorFieldTrigger(
                            label = "3. Operator Pelaksana (Rencana Utama)",
                            selectedValue = rencanaState.selectedOperator?.name,
                            placeholder = "Pilih Nama Operator Pelaksana...",
                            leadingIcon = Icons.Default.Person,
                            badgeText = rencanaState.selectedOperator?.badgeNumber,
                            testTag = "selector_rencana_operator",
                            onClick = {
                                activeSubPlanOperatorTarget = null
                                isOperatorSheetOpen = true
                            }
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // 4. Aktivitas / Jenis Pekerjaan Utama
                        SelectorFieldTrigger(
                            label = "4. Aktivitas / Jenis Pekerjaan (Rencana Utama)",
                            selectedValue = rencanaState.selectedActivity?.let { "${it.activityName} (${it.sapCode})" },
                            placeholder = "Pilih Jenis Pekerjaan (Galian, Loading, dll)...",
                            leadingIcon = Icons.Default.Assignment,
                            badgeText = rencanaState.selectedActivity?.unitOfMeasure,
                            testTag = "selector_rencana_aktivitas",
                            onClick = {
                                activeSubPlanActivityTarget = null
                                isRencanaActivitySheetOpen = true
                            }
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // 5. Lokasi Kerja Lapangan Utama
                        SelectorFieldTrigger(
                            label = "5. Lokasi Kerja Lapangan (Rencana Utama)",
                            selectedValue = rencanaState.selectedLokasi?.let { "${it.kodeLokasi} (${it.wilayah})" },
                            placeholder = "Pilih Lokasi Kerja (001A / PG1)...",
                            leadingIcon = Icons.Default.LocationOn,
                            badgeText = rencanaState.selectedLokasi?.wilayah,
                            testTag = "selector_rencana_lokasi",
                            onClick = {
                                activeSubPlanLokasiTarget = null
                                isLokasiSheetOpen = true
                            }
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // 6. Shift Kerja Toggle Pill [SIANG] | [MALAM]
                        Text(
                            text = "6. Shift Kerja",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Slate700
                            ),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            listOf(
                                "SIANG" to Icons.Default.WbSunny,
                                "MALAM" to Icons.Default.NightlightRound
                            ).forEach { (shift, icon) ->
                                val isSelected = rencanaState.shiftKerja == shift
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) Emerald600 else Color(0xFFF1F5F9),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) Emerald600 else Slate200),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { viewModel.setRencanaShift(shift) }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(vertical = 10.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = if (isSelected) Color.White else Slate700,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "SHIFT $shift",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = if (isSelected) Color.White else Slate700
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // 7. Keterangan Rencana / Catatan Awal
                        Text(
                            text = "7. Keterangan Rencana Kerja",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Slate700
                            ),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        OutlinedTextField(
                            value = rencanaState.keterangan,
                            onValueChange = { viewModel.setRencanaKeterangan(it) },
                            placeholder = { Text("Instruksi kerja pengawas, target galian, rute hauling...", color = Slate400, fontSize = 13.sp) },
                            minLines = 2,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Emerald600,
                                unfocusedBorderColor = Slate200
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("input_rencana_keterangan")
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // ==========================================
                        // DUKUNGAN MULTI-RENCANA (SUB-FORM)
                        // 1 Unit & 1 Tanggal bisa > 1 Rencana Kerja
                        // ==========================================
                        if (rencanaState.subPlans.isNotEmpty()) {
                            Text(
                                text = "Rencana Kerja Tambahan (${rencanaState.subPlans.size} Sub-Rencana)",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                ),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            rencanaState.subPlans.forEachIndexed { index, sub ->
                                Card(
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Emerald600.copy(alpha = 0.4f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 12.dp)
                                        .testTag("sub_rencana_card_${index + 1}")
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Surface(
                                                    shape = CircleShape,
                                                    color = Emerald600
                                                ) {
                                                    Text(
                                                        text = "#${index + 2}",
                                                        color = Color.White,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "Rencana Tambahan #${index + 2}",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = Slate900
                                                )
                                            }

                                            IconButton(
                                                onClick = { viewModel.removeSubRencana(sub.id) },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.DeleteOutline,
                                                    contentDescription = "Hapus Sub Rencana",
                                                    tint = Color(0xFFDC2626)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        // Sub 1: Operator
                                        SelectorFieldTrigger(
                                            label = "Operator Pelaksana #${index + 2}",
                                            selectedValue = sub.selectedOperator?.name,
                                            placeholder = "Pilih Operator Pelaksana...",
                                            leadingIcon = Icons.Default.Person,
                                            testTag = "sub_operator_${index + 1}",
                                            onClick = {
                                                activeSubPlanOperatorTarget = sub.id
                                                isOperatorSheetOpen = true
                                            }
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Sub 2: Aktivitas
                                        SelectorFieldTrigger(
                                            label = "Aktivitas / Jenis Pekerjaan #${index + 2}",
                                            selectedValue = sub.selectedActivity?.let { "${it.activityName} (${it.sapCode})" },
                                            placeholder = "Pilih Aktivitas...",
                                            leadingIcon = Icons.Default.Assignment,
                                            badgeText = sub.selectedActivity?.unitOfMeasure,
                                            testTag = "sub_aktivitas_${index + 1}",
                                            onClick = {
                                                activeSubPlanActivityTarget = sub.id
                                                isRencanaActivitySheetOpen = true
                                            }
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Sub 3: Lokasi
                                        SelectorFieldTrigger(
                                            label = "Lokasi Kerja Lapangan #${index + 2}",
                                            selectedValue = sub.selectedLokasi?.let { "${it.kodeLokasi} (${it.wilayah})" },
                                            placeholder = "Pilih Lokasi Kerja...",
                                            leadingIcon = Icons.Default.LocationOn,
                                            testTag = "sub_lokasi_${index + 1}",
                                            onClick = {
                                                activeSubPlanLokasiTarget = sub.id
                                                isLokasiSheetOpen = true
                                            }
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Sub 4: Shift Kerja
                                        Text(
                                            text = "Shift Kerja #${index + 2}",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                color = Slate700
                                            ),
                                            modifier = Modifier.padding(bottom = 4.dp)
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            listOf("SIANG", "MALAM").forEach { sft ->
                                                val isSel = sub.shiftKerja == sft
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = if (isSel) Emerald600 else Color.White,
                                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) Emerald600 else Slate200),
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .clickable { viewModel.updateSubRencanaShift(sub.id, sft) }
                                                ) {
                                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 6.dp)) {
                                                        Text(
                                                            text = "SHIFT $sft",
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (isSel) Color.White else Slate700
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Sub 5: Keterangan
                                        OutlinedTextField(
                                            value = sub.keterangan,
                                            onValueChange = { viewModel.updateSubRencanaKeterangan(sub.id, it) },
                                            placeholder = { Text("Keterangan sub-rencana #${index + 2}...", fontSize = 12.sp, color = Slate400) },
                                            minLines = 1,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = Emerald600,
                                                unfocusedBorderColor = Slate200,
                                                focusedContainerColor = Color.White,
                                                unfocusedContainerColor = Color.White
                                            ),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.fillMaxWidth().testTag("sub_keterangan_${index + 1}")
                                        )
                                    }
                                }
                            }
                        }

                        // Tombol "+ Tambah Rencana Kerja" (Sub-Form Multi-Rencana)
                        OutlinedButton(
                            onClick = { viewModel.addSubRencana() },
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, Emerald600),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Emerald700),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("btn_tambah_sub_rencana")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "+ Tambah Rencana Kerja (Multi-Rencana)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Tombol Aksi: Kirim Rencana Kerja
                        Button(
                            onClick = { viewModel.submitRencanaKerja() },
                            enabled = !rencanaState.isSubmitting && rencanaState.selectedUnit != null,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("btn_kirim_rencana_kerja")
                        ) {
                            if (rencanaState.isSubmitting) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Menyimpan Rencana Kerja...")
                            } else {
                                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                val totalToSubmit = 1 + rencanaState.subPlans.size
                                Text(
                                    text = if (totalToSubmit > 1)
                                        "Kirim $totalToSubmit Rencana Kerja Sekaligus"
                                    else
                                        "Kirim Rencana Kerja (Menunggu SPK)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // KONTEN TAHAP 2: FORMULIR REALISASI KERJA
        // ==========================================
        if (formSubTab == 1) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("card_realisasi_kerja"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        if (realisasiState.selectedRencana != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                androidx.compose.material3.OutlinedButton(
                                    onClick = { showResetConfirmDialog = true },
                                    colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                                        contentColor = Color(0xFFDC2626)
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.testTag("btn_reset_formulir")
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Reset Formulir", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        // Pemilih Rencana Kerja Aktif
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Pilih Rencana Kerja Aktif Pengawas:",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = Slate700
                                )
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Emerald50,
                                border = androidx.compose.foundation.BorderStroke(1.dp, Emerald600.copy(alpha = 0.4f)),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { viewModel.triggerHeaderSync() }
                                    .testTag("btn_tarik_rencana_terbaru")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Tarik Terbaru dari Server",
                                        tint = Emerald700,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Tarik Terbaru",
                                        fontSize = 11.sp,
                                        color = Emerald900,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        if (activeRencanaList.isEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFF1F5F9),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "Belum ada rencana kerja aktif hari ini.",
                                        fontWeight = FontWeight.SemiBold,
                                        color = Slate700,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "Seluruh rencana sebelumnya telah selesai direalisasikan, atau buat Rencana baru di Tab 1.",
                                        color = Slate500,
                                        fontSize = 11.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = { viewModel.setFormSubTab(0) },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text("Buat Rencana di Tab 1", fontSize = 12.sp)
                                        }
                                        OutlinedButton(
                                            onClick = { viewModel.syncActivePlansFromServer() },
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Tarik dari Web Admin", fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        } else {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                            ) {
                                items(activeRencanaList) { ren ->
                                    val isSelected = realisasiState.selectedRencana?.id == ren.id
                                    val sameUnitPlans = activeRencanaList.filter { it.kodeUnit.equals(ren.kodeUnit, ignoreCase = true) }
                                    val isMultipleForUnit = sameUnitPlans.size > 1
                                    val unitPlanIndex = sameUnitPlans.sortedBy { it.createdAt }.indexOfFirst { it.id == ren.id } + 1
                                    val isSusulan = isMultipleForUnit && unitPlanIndex > 1

                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) Emerald50 else Color(0xFFF8FAFC),
                                        border = androidx.compose.foundation.BorderStroke(
                                            if (isSelected) 2.dp else 1.dp,
                                            if (isSelected) Emerald600 else if (isSusulan) Color(0xFF86EFAC) else Slate200
                                        ),
                                        modifier = Modifier
                                            .width(240.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable { viewModel.selectRencanaForRealisasi(ren) }
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = ren.kodeUnit,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 14.sp,
                                                        color = Slate900
                                                    )
                                                    if (isMultipleForUnit) {
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Surface(
                                                            shape = RoundedCornerShape(4.dp),
                                                            color = if (isSusulan) Color(0xFFDCFCE7) else Color(0xFFEFF6FF)
                                                        ) {
                                                            Text(
                                                                text = if (isSusulan) "Susulan #$unitPlanIndex" else "Rencana #1",
                                                                fontSize = 9.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = if (isSusulan) Color(0xFF166534) else Color(0xFF1E40AF),
                                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = if (ren.statusUnit == "OPERASI") Emerald100 else Color(0xFFFEF3C7)
                                                ) {
                                                    Text(
                                                        text = ren.statusUnit,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (ren.statusUnit == "OPERASI") Emerald900 else Color(0xFFB45309),
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Akt: ${ren.namaAktivitas.ifBlank { ren.sapCode }}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Slate800,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "Opr: ${ren.operator} • Shift ${ren.shiftKerja}",
                                                fontSize = 11.sp,
                                                color = Slate600
                                            )
                                            Text(
                                                text = "Lok: ${ren.kodeLokasi} • Tgl: ${ren.tanggalFormat}",
                                                fontSize = 10.sp,
                                                color = Slate500
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                val isUnitSpkTerbit = ren.isSpkTerbit
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = if (isUnitSpkTerbit)
                                                        Color(0xFFDCFCE7)
                                                    else
                                                        Color(0xFFFEF3C7)
                                                ) {
                                                    Text(
                                                        text = if (isUnitSpkTerbit)
                                                            "SPK: ${ren.nomorSpk}"
                                                        else
                                                            "Menunggu SPK",
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isUnitSpkTerbit)
                                                            Color(0xFF166534)
                                                        else
                                                            Color(0xFF92400E),
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                    )
                                                }
                                                Text(
                                                    text = if (isSelected) "Terpilih" else "Siap Realisasi",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSelected) Emerald700 else Slate400
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // ==============================================================
                        // 1. FORM INPUT NOMOR SPK (Surat Perintah Kerja)
                        // Menerima penerbitan dari Web Admin secara real-time untuk masing-masing rencana kerja
                        // Serta mendukung input / koreksi manual oleh pengawas di lapangan.
                        // ==============================================================
                        val isSpkTerbit = realisasiState.isSpkTerbit
                        val currentUnitCode = realisasiState.selectedRencana?.kodeUnit ?: "-"

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSpkTerbit) Emerald500.copy(alpha = 0.5f) else Slate200
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("spk_form_card")
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                // 1. Header: Title, Unit Context, and Compact Status Badge
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f, fill = false)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .background(
                                                    if (isSpkTerbit) Emerald50 else Color(0xFFF1F5F9),
                                                    RoundedCornerShape(8.dp)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (isSpkTerbit) Icons.Default.CheckCircle else Icons.Default.Assignment,
                                                contentDescription = null,
                                                tint = if (isSpkTerbit) Emerald700 else Slate600,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "1. Nomor SPK (Surat Perintah Kerja)",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Slate800
                                            )
                                            Text(
                                                text = if (realisasiState.selectedRencana != null)
                                                    "Unit: ${realisasiState.selectedRencana?.kodeUnit}"
                                                else
                                                    "Pilih rencana unit di atas",
                                                fontSize = 10.sp,
                                                color = Slate500
                                            )
                                        }
                                    }

                                    // Compact Status Badge
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isSpkTerbit) Emerald100 else Color(0xFFFEF3C7)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(5.dp)
                                                    .background(
                                                        if (isSpkTerbit) Emerald600 else Color(0xFFD97706),
                                                        CircleShape
                                                    )
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (isSpkTerbit) "SPK Terbit" else "Menunggu Terbit",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSpkTerbit) Emerald900 else Color(0xFFB45309)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // 2. Clean Compact TextField Input
                                OutlinedTextField(
                                    value = realisasiState.nomorSpk,
                                    onValueChange = { newSpk ->
                                        viewModel.setRealisasiNomorSpk(newSpk)
                                    },
                                    placeholder = {
                                        Text(
                                            text = "Nomor SPK dari Web Admin / Input manual...",
                                            fontSize = 13.sp,
                                            color = Slate400
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Assignment,
                                            contentDescription = null,
                                            tint = if (isSpkTerbit) Emerald600 else Slate400,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    trailingIcon = {
                                        if (realisasiState.nomorSpk.isNotBlank()) {
                                            IconButton(
                                                onClick = { viewModel.setRealisasiNomorSpk("") },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Hapus input SPK",
                                                    tint = Slate400,
                                                    modifier = Modifier.size(15.dp)
                                                )
                                            }
                                        }
                                    },
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    textStyle = TextStyle(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        color = Slate900
                                    ),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = if (isSpkTerbit) Emerald600 else Slate400,
                                        unfocusedBorderColor = if (isSpkTerbit) Emerald500.copy(alpha = 0.5f) else Slate200,
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("spk_input_field")
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // 3. Compact Footer: Live Status & Action Chips
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Status info
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f, fill = false)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .background(
                                                    if (isSpkTerbit) Emerald500 else if (isSocketConnected) Color(0xFF3B82F6) else Slate400,
                                                    CircleShape
                                                )
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = if (isSpkTerbit)
                                                "Tersinkron otomatis dari Admin"
                                            else if (isSocketConnected)
                                                "Live Web Admin aktif"
                                            else
                                                "Ketik manual / tarik SPK",
                                            fontSize = 10.sp,
                                            color = if (isSpkTerbit) Emerald700 else Slate500,
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(6.dp))

                                    // Action Chips
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Tombol Tarik SPK
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (isSpkTerbit) Color(0xFFF1F5F9) else Color(0xFFEFF6FF),
                                            border = androidx.compose.foundation.BorderStroke(
                                                1.dp,
                                                if (isSpkTerbit) Slate300 else Color(0xFF93C5FD)
                                            ),
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .clickable { viewModel.fetchLatestSpkForCurrentRencana() }
                                                .testTag("btn_pull_spk_admin")
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Refresh,
                                                    contentDescription = null,
                                                    tint = if (isSpkTerbit) Slate700 else Color(0xFF1D4ED8),
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text(
                                                    text = "Tarik SPK",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = if (isSpkTerbit) Slate700 else Color(0xFF1D4ED8)
                                                )
                                            }
                                        }

                                        // Opsi SPK Menyusul (jika belum terbit)
                                        if (!isSpkTerbit) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = Color(0xFFFFFBEB),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCD34D)),
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .clickable { viewModel.setRealisasiNomorSpk("SPK Menyusul") }
                                                    .testTag("btn_spk_menyusul")
                                            ) {
                                                Text(
                                                    text = "SPK Menyusul",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = Color(0xFFB45309),
                                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                                                )
                                            }
                                        }

                                        // Tombol Simulasi Terbitkan SPK (Test)
                                        val selectedRen = realisasiState.selectedRencana
                                        if (selectedRen != null) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = Emerald50,
                                                border = androidx.compose.foundation.BorderStroke(1.dp, Emerald200),
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .clickable { viewModel.adminSimulatePublishSpk(selectedRen.id, selectedRen.kodeUnit) }
                                                    .testTag("btn_simulate_spk")
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Bolt,
                                                        contentDescription = null,
                                                        tint = Emerald700,
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(2.dp))
                                                    Text(
                                                        text = "Test",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Emerald800
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // ==========================================
                        // FLEKSIBILITAS KONDISI LAPANGAN
                        // FIELD LOKASI & AKTIVITAS DAPAT DIEDIT
                        // ==========================================
                        Text(
                            text = "2. Fleksibilitas Lapangan (Aktivitas & Lokasi Aktual)",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Slate700
                            ),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )

                        // Editable Dropdown Aktivitas
                        SelectorFieldTrigger(
                            label = "Aktivitas / Jenis Pekerjaan Aktual",
                            selectedValue = realisasiState.selectedActivity?.let { "${it.activityName} (${it.sapCode})" }
                                ?: realisasiState.selectedRencana?.namaAktivitas,
                            placeholder = "Pilih Aktivitas Aktual Lapangan...",
                            leadingIcon = Icons.Default.Assignment,
                            badgeText = "Dapat Diubah",
                            testTag = "selector_realisasi_aktivitas_editable",
                            onClick = { isRealisasiActivitySheetOpen = true }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Editable Dropdown Lokasi
                        SelectorFieldTrigger(
                            label = "Lokasi Kerja Lapangan Aktual",
                            selectedValue = realisasiState.selectedLokasi?.let { "${it.kodeLokasi} (${it.wilayah})" }
                                ?: realisasiState.selectedRencana?.kodeLokasi,
                            placeholder = "Pilih Lokasi Aktual Lapangan...",
                            leadingIcon = Icons.Default.LocationOn,
                            badgeText = "Dapat Diubah",
                            testTag = "selector_realisasi_lokasi_editable",
                            onClick = { isRealisasiLokasiSheetOpen = true }
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // 3. Durasi Jam Kerja
                        Text(
                            text = "3. Durasi Jam Kerja",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Slate700
                            ),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        OutlinedTextField(
                            value = String.format(Locale.US, "%.1f", realisasiState.durasiJamKerja),
                            onValueChange = { input ->
                                val parsed = input.replace(',', '.').toDoubleOrNull()
                                if (parsed != null) viewModel.setRealisasiDurasiJam(parsed)
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            leadingIcon = {
                                Icon(Icons.Default.Schedule, contentDescription = null, tint = Emerald600)
                            },
                            trailingIcon = { Text("Jam", color = Slate500, fontSize = 13.sp, modifier = Modifier.padding(end = 12.dp)) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Emerald600,
                                unfocusedBorderColor = Slate200
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("input_durasi_jam")
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Preset Quick Pills
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf(4.0, 7.0, 8.0, 10.0, 12.0).forEach { hours ->
                                val isSelected = realisasiState.durasiJamKerja == hours
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) Emerald600 else Color(0xFFF1F5F9),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { viewModel.setRealisasiDurasiJam(hours) }
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.padding(vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "${hours.toInt()}j",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else Slate700
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // ==========================================
                        // 4. BLOK KALKULASI HOUR METER (HM) & VALIDASI STANDAR
                        // ==========================================
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFFF8FAFC),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                // Header: Title & Subtitle + Standar HM Badge
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(Emerald50, RoundedCornerShape(10.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Speed,
                                                contentDescription = null,
                                                tint = Emerald600,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "4. Hour Meter (HM) Mesin",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = Slate900
                                            )
                                            Text(
                                                text = "Validasi operasi & standar unit",
                                                fontSize = 11.sp,
                                                color = Slate500
                                            )
                                        }
                                    }

                                    // Standar Unit Badge dari Master Unit
                                    val minStdBadge = if (realisasiState.hmMinStandar % 1.0 == 0.0) realisasiState.hmMinStandar.toInt().toString() else String.format(Locale.US, "%.1f", realisasiState.hmMinStandar)
                                    val maxStdBadge = if (realisasiState.hmMaxStandar % 1.0 == 0.0) realisasiState.hmMaxStandar.toInt().toString() else String.format(Locale.US, "%.1f", realisasiState.hmMaxStandar)
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Emerald50,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Emerald600.copy(alpha = 0.4f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Tune,
                                                contentDescription = null,
                                                tint = Emerald700,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Standar: $minStdBadge – $maxStdBadge HM",
                                                color = Emerald900,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // KESINAMBUNGAN HM PADA MULTI-RENCANA DALAM 1 UNIT (SEQUENTIAL HM)
                                if (realisasiState.isSequentialHmFromPreviousPlan) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Emerald50,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Emerald100),
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).testTag("badge_sequential_hm")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.TaskAlt,
                                                contentDescription = null,
                                                tint = Emerald700,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "HM Berantai Otomatis: HM Awal ${String.format(Locale.US, "%.1f", realisasiState.hmAwal)} tersambung langsung dari HM Akhir rencana sebelumnya.",
                                                fontSize = 11.sp,
                                                color = Emerald900,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }

                                // BARIS STATUS & TOMBOL KOREKSI HM
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (realisasiState.isHmAwalCorrected) Color(0xFFFFFBEB) else Color(0xFFF1F5F9),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (realisasiState.isHmAwalCorrected) Color(0xFFFDE68A) else Color(0xFFE2E8F0)
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                imageVector = if (realisasiState.isHmAwalCorrected) Icons.Default.EditNote else Icons.Default.Lock,
                                                contentDescription = null,
                                                tint = if (realisasiState.isHmAwalCorrected) Color(0xFFD97706) else Slate500,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (realisasiState.isHmAwalCorrected) "Mode Koreksi HM Awal Aktif" else "HM Awal Terkunci Otomatis",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (realisasiState.isHmAwalCorrected) Color(0xFF92400E) else Slate600
                                            )
                                        }

                                        // Tombol Koreksi HM
                                        Surface(
                                            shape = RoundedCornerShape(16.dp),
                                            color = if (realisasiState.isHmAwalCorrected) Color(0xFFFEE2E2) else Color.White,
                                            border = androidx.compose.foundation.BorderStroke(
                                                1.dp,
                                                if (realisasiState.isHmAwalCorrected) Color(0xFFFCA5A5) else Emerald600
                                            ),
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(16.dp))
                                                .clickable { viewModel.toggleKoreksiHmAwal() }
                                                .testTag("btn_koreksi_hm_awal")
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = if (realisasiState.isHmAwalCorrected) Icons.Default.Close else Icons.Default.Edit,
                                                    contentDescription = "Koreksi HM Awal",
                                                    tint = if (realisasiState.isHmAwalCorrected) Color(0xFFDC2626) else Emerald700,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = if (realisasiState.isHmAwalCorrected) "Batal Koreksi" else "Koreksi HM Awal",
                                                    fontSize = 11.sp,
                                                    color = if (realisasiState.isHmAwalCorrected) Color(0xFFDC2626) else Emerald800,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // DUAL ODOMETER INPUTS: HM AWAL ➔ HM AKHIR
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // HM Awal
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(bottom = 4.dp)
                                        ) {
                                            Text(
                                                text = "HM AWAL",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (realisasiState.isHmAwalCorrected) Color(0xFFB45309) else Slate700
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            if (!realisasiState.isHmAwalCorrected) {
                                                Icon(
                                                    imageVector = Icons.Default.Lock,
                                                    contentDescription = "Terkunci",
                                                    tint = Slate400,
                                                    modifier = Modifier.size(11.dp)
                                                )
                                            }
                                        }

                                        if (!realisasiState.isHmAwalCorrected) {
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = Color(0xFFF1F5F9),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                                modifier = Modifier.fillMaxWidth().height(52.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .padding(horizontal = 12.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(
                                                        text = String.format(Locale.US, "%.1f", realisasiState.hmAwal),
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 15.sp,
                                                        fontFamily = FontFamily.Monospace,
                                                        color = Slate800
                                                    )
                                                    Text(
                                                        text = "HM",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = Slate500
                                                    )
                                                }
                                            }
                                        } else {
                                            OutlinedTextField(
                                                value = realisasiState.hmAwalInput,
                                                onValueChange = { viewModel.setRealisasiHmAwalManual(it) },
                                                placeholder = { Text("HM Baru", fontSize = 12.sp, color = Slate400) },
                                                trailingIcon = {
                                                    Text("HM", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD97706), modifier = Modifier.padding(end = 8.dp))
                                                },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                                singleLine = true,
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = Color(0xFFD97706),
                                                    unfocusedBorderColor = Color(0xFFFDE68A),
                                                    focusedContainerColor = Color(0xFFFFFBEB),
                                                    unfocusedContainerColor = Color(0xFFFFFBEB)
                                                ),
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.fillMaxWidth().height(52.dp).testTag("input_hm_awal_manual")
                                            )
                                        }
                                    }

                                    // Flow Arrow Indicator
                                    Box(
                                        modifier = Modifier
                                            .padding(top = 18.dp)
                                            .size(24.dp)
                                            .background(Color(0xFFE2E8F0), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = null,
                                            tint = Slate600,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }

                                    // HM Akhir
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(bottom = 4.dp)
                                        ) {
                                            Text(
                                                text = "HM AKHIR",
                                                fontSize = 11.sp,
                                                color = Slate900,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "*Wajib",
                                                fontSize = 10.sp,
                                                color = Emerald700,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        OutlinedTextField(
                                            value = realisasiState.hmAkhirInput,
                                            onValueChange = { viewModel.setRealisasiHmAkhir(it) },
                                            placeholder = { Text("Contoh: 4132.5", fontSize = 12.sp, color = Slate400) },
                                            trailingIcon = {
                                                Text("HM", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate500, modifier = Modifier.padding(end = 8.dp))
                                            },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                            isError = !realisasiState.isHmValid && realisasiState.hmAkhirInput.isNotBlank(),
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = Emerald600,
                                                unfocusedBorderColor = Slate300,
                                                errorBorderColor = ErrorRose,
                                                focusedContainerColor = Color.White,
                                                unfocusedContainerColor = Color.White
                                            ),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.fillMaxWidth().height(52.dp).testTag("input_hm_akhir")
                                        )
                                    }
                                }

                                // Quick Add Hours Buttons (+4j, +7j, +8j, +10j)
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Cepat:",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Slate500
                                    )
                                    listOf(4.0, 7.0, 8.0, 10.0).forEach { qh ->
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Emerald50,
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Emerald200),
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(6.dp))
                                                .clickable { viewModel.quickAddRealisasiHours(qh) }
                                        ) {
                                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 5.dp)) {
                                                Text("+${qh.toInt()} Jam", fontSize = 11.sp, color = Emerald900, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }

                                // Bagian Peringatan & Alasan jika HM Awal dikoreksi manual
                                if (realisasiState.isHmAwalCorrected) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    if (realisasiState.hmAwal != realisasiState.hmAwalOriginal) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFFFEF3C7),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A)),
                                            modifier = Modifier.fillMaxWidth().testTag("peringatan_koreksi_hm")
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Warning,
                                                    contentDescription = null,
                                                    tint = Color(0xFFD97706),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "Peringatan: HM Awal telah diubah dari nilai riwayat sistem (${String.format(Locale.US, "%.1f", realisasiState.hmAwalOriginal)} ➔ ${String.format(Locale.US, "%.1f", realisasiState.hmAwal)})",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = Color(0xFF92400E)
                                                )
                                            }
                                        }
                                    }

                                    Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                                        Text(
                                            text = "Alasan Koreksi HM Awal (wajib diisi jika dikoreksi):",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (realisasiState.alasanKoreksiHm.isBlank()) Color(0xFFDC2626) else Slate700
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        OutlinedTextField(
                                            value = realisasiState.alasanKoreksiHm,
                                            onValueChange = { viewModel.setRealisasiAlasanKoreksiHm(it) },
                                            placeholder = { Text("Contoh: Salah input shift sebelumnya / pergantian meteran alat", fontSize = 12.sp, color = Slate400) },
                                            singleLine = true,
                                            isError = realisasiState.alasanKoreksiHm.isBlank(),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = Color(0xFFD97706),
                                                unfocusedBorderColor = if (realisasiState.alasanKoreksiHm.isBlank()) Color(0xFFFCA5A5) else Slate200,
                                                focusedContainerColor = Color.White,
                                                unfocusedContainerColor = Color.White
                                            ),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.fillMaxWidth().testTag("input_alasan_koreksi_hm")
                                        )
                                        if (realisasiState.alasanKoreksiHm.isBlank()) {
                                            Text(
                                                text = "Alasan Koreksi HM Awal (wajib diisi jika dikoreksi)",
                                                color = Color(0xFFDC2626),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Medium,
                                                modifier = Modifier.padding(top = 2.dp)
                                            )
                                        }
                                    }
                                }

                                if (!realisasiState.isHmValid && realisasiState.hmErrorMessage != null) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFFFEF2F2),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECACA)),
                                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ErrorOutline,
                                                contentDescription = null,
                                                tint = ErrorRose,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = realisasiState.hmErrorMessage ?: "",
                                                color = ErrorRose,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // HASIL KALKULASI HM BERJALAN & STATUS EVALUASI STANDAR
                                val runningHm = realisasiState.hmHarianBerjalan
                                val isUnderMin = runningHm > 0.0 && runningHm < realisasiState.hmMinStandar
                                val isOverMax = runningHm > realisasiState.hmMaxStandar

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = when {
                                        isUnderMin -> Color(0xFFFFF1F2)
                                        isOverMax -> Color(0xFFFFFBEB)
                                        runningHm > 0.0 -> Emerald50
                                        else -> Color(0xFFF1F5F9)
                                    },
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        when {
                                            isUnderMin -> Color(0xFFFECDD3)
                                            isOverMax -> Color(0xFFFDE68A)
                                            runningHm > 0.0 -> Emerald200
                                            else -> Color(0xFFCBD5E1)
                                        }
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = "TOTAL HM BERJALAN",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = when {
                                                        isUnderMin -> Color(0xFF9F1239)
                                                        isOverMax -> Color(0xFF92400E)
                                                        runningHm > 0.0 -> Emerald900
                                                        else -> Slate700
                                                    }
                                                )
                                                val endVal = realisasiState.hmAkhirInput.replace(',', '.').toDoubleOrNull() ?: realisasiState.hmAwal
                                                Text(
                                                    text = "Formula: ${String.format(Locale.US, "%.1f", endVal)} – ${String.format(Locale.US, "%.1f", realisasiState.hmAwal)} Jam",
                                                    fontSize = 10.sp,
                                                    color = when {
                                                        isUnderMin -> Color(0xFFBE123C)
                                                        isOverMax -> Color(0xFFB45309)
                                                        runningHm > 0.0 -> Emerald700
                                                        else -> Slate500
                                                    }
                                                )
                                            }

                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = when {
                                                    isUnderMin -> Color(0xFFE11D48)
                                                    isOverMax -> Color(0xFFD97706)
                                                    runningHm > 0.0 -> Emerald600
                                                    else -> Slate500
                                                }
                                            ) {
                                                Text(
                                                    text = "${String.format(Locale.US, "%.1f", runningHm)} Jam",
                                                    fontSize = 17.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontFamily = FontFamily.Monospace,
                                                    color = Color.White,
                                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))
                                        HorizontalDivider(
                                            color = when {
                                                isUnderMin -> Color(0xFFFECDD3)
                                                isOverMax -> Color(0xFFFDE68A)
                                                runningHm > 0.0 -> Emerald200
                                                else -> Color(0xFFCBD5E1)
                                            },
                                            thickness = 0.5.dp
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))

                                        val minStdLabel = if (realisasiState.hmMinStandar % 1.0 == 0.0) realisasiState.hmMinStandar.toInt().toString() else String.format(Locale.US, "%.1f", realisasiState.hmMinStandar)
                                        val maxStdLabel = if (realisasiState.hmMaxStandar % 1.0 == 0.0) realisasiState.hmMaxStandar.toInt().toString() else String.format(Locale.US, "%.1f", realisasiState.hmMaxStandar)

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                                Icon(
                                                    imageVector = when {
                                                        isUnderMin -> Icons.Default.WarningAmber
                                                        isOverMax -> Icons.Default.Info
                                                        runningHm > 0.0 -> Icons.Default.CheckCircle
                                                        else -> Icons.Default.Speed
                                                    },
                                                    contentDescription = null,
                                                    tint = when {
                                                        isUnderMin -> Color(0xFFE11D48)
                                                        isOverMax -> Color(0xFFD97706)
                                                        runningHm > 0.0 -> Emerald700
                                                        else -> Slate500
                                                    },
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = when {
                                                        isUnderMin -> "Kurang dari standar minimal ($minStdLabel HM) • Wajib kendala"
                                                        isOverMax -> "Melebihi standar maksimal ($maxStdLabel HM) • Lembur"
                                                        runningHm > 0.0 -> "Sesuai standar operasional ($minStdLabel – $maxStdLabel HM)"
                                                        else -> "Standar target unit: $minStdLabel – $maxStdLabel HM"
                                                    },
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = when {
                                                        isUnderMin -> Color(0xFFBE123C)
                                                        isOverMax -> Color(0xFFB45309)
                                                        runningHm > 0.0 -> Emerald800
                                                        else -> Slate600
                                                    }
                                                )
                                            }

                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = when {
                                                    isUnderMin -> Color(0xFFFEE2E2)
                                                    isOverMax -> Color(0xFFFEF3C7)
                                                    runningHm > 0.0 -> Emerald100
                                                    else -> Color(0xFFE2E8F0)
                                                }
                                            ) {
                                                Text(
                                                    text = when {
                                                        isUnderMin -> "DI BAWAH STD"
                                                        isOverMax -> "LEBIH STD"
                                                        runningHm > 0.0 -> "SESUAI STD"
                                                        else -> "NORMAL"
                                                    },
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = when {
                                                        isUnderMin -> Color(0xFF9F1239)
                                                        isOverMax -> Color(0xFF92400E)
                                                        runningHm > 0.0 -> Emerald900
                                                        else -> Slate600
                                                    },
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                // ==========================================
                                // VALIDASI HM KURANG DARI STANDAR MINIMAL
                                // DUKUNGAN MULTI-KENDALA & INPUT WAKTU KEJADIAN
                                // ==========================================
                                if (isUnderMin) {
                                    val minStdNotice = if (realisasiState.hmMinStandar % 1.0 == 0.0) realisasiState.hmMinStandar.toInt().toString() else String.format(Locale.US, "%.1f", realisasiState.hmMinStandar)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xFFFFFBEB),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B)),
                                        modifier = Modifier.fillMaxWidth().testTag("container_multi_kendala")
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        Icons.Default.Warning,
                                                        contentDescription = null,
                                                        tint = Color(0xFFD97706),
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "Wajib Mencatat Kendala Lapangan",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.sp,
                                                        color = Color(0xFF92400E)
                                                    )
                                                }
                                                if (realisasiState.totalHambatanMenit > 0) {
                                                    Surface(
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = Color(0xFFFDE68A)
                                                    ) {
                                                        Text(
                                                            text = realisasiState.totalHambatanLabel,
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color(0xFF92400E),
                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }
                                            }
                                            Text(
                                                text = "HM berjalan (${String.format(Locale.US, "%.1f", realisasiState.hmHarianBerjalan)} HM) kurang dari batas minimal ($minStdNotice HM). Catat jenis hambatan dan rentang waktu kejadian secara akurat:",
                                                fontSize = 11.sp,
                                                color = Color(0xFF78350F),
                                                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                                            )

                                            // Daftar Kartu Kendala (Multi-Kendala)
                                            realisasiState.daftarKendala.forEachIndexed { index, item ->
                                                val isRowOverlapping = realisasiState.isKendalaOverlapping(index)
                                                val isCardValid = item.isValid && !isRowOverlapping
                                                Card(
                                                    shape = RoundedCornerShape(10.dp),
                                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                                    border = androidx.compose.foundation.BorderStroke(
                                                        1.dp,
                                                        if (isCardValid) Color(0xFFE2E8F0) else Color(0xFFFCA5A5)
                                                    ),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(bottom = 10.dp)
                                                        .testTag("card_kendala_${index + 1}")
                                                ) {
                                                    Column(modifier = Modifier.padding(12.dp)) {
                                                        // Header Baris Kendala
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                                Surface(
                                                                    shape = CircleShape,
                                                                    color = Color(0xFFFEF3C7),
                                                                    modifier = Modifier.size(22.dp)
                                                                ) {
                                                                    Box(contentAlignment = Alignment.Center) {
                                                                        Text(
                                                                            text = "${index + 1}",
                                                                            fontSize = 11.sp,
                                                                            fontWeight = FontWeight.Bold,
                                                                            color = Color(0xFF92400E)
                                                                        )
                                                                    }
                                                                }
                                                                Spacer(modifier = Modifier.width(6.dp))
                                                                Text(
                                                                    text = "Kendala #${index + 1}",
                                                                    fontWeight = FontWeight.Bold,
                                                                    fontSize = 12.sp,
                                                                    color = Slate900
                                                                )
                                                            }

                                                            // Tombol Hapus (Ikon Tempat Sampah) jika baris > 1
                                                            if (realisasiState.daftarKendala.size > 1) {
                                                                IconButton(
                                                                    onClick = { viewModel.removeKendalaRow(index) },
                                                                    modifier = Modifier
                                                                        .size(28.dp)
                                                                        .testTag("btn_hapus_kendala_${index + 1}")
                                                                ) {
                                                                    Icon(
                                                                        imageVector = Icons.Default.DeleteOutline,
                                                                        contentDescription = "Hapus Kendala #${index + 1}",
                                                                        tint = Color(0xFFDC2626),
                                                                        modifier = Modifier.size(18.dp)
                                                                    )
                                                                }
                                                            }
                                                        }

                                                        Spacer(modifier = Modifier.height(8.dp))

                                                        // a. Dropdown Penyebab Kendala (Searchable Selector)
                                                        SelectorFieldTrigger(
                                                            label = "Penyebab Kendala (Master Kendala)",
                                                            selectedValue = item.selectedKendala?.let {
                                                                "${it.kodeKendala} - ${it.namaKendala} (${it.kategori})"
                                                            },
                                                            placeholder = "Pilih penyebab kendala (Hujan, Breakdown, dll)...",
                                                            leadingIcon = Icons.Default.ReportProblem,
                                                            badgeText = item.selectedKendala?.kategori,
                                                            isError = item.selectedKendala == null,
                                                            errorMessage = if (item.selectedKendala == null) "Penyebab kendala wajib dipilih" else null,
                                                            testTag = "selector_kendala_${index + 1}",
                                                            onClick = {
                                                                activeKendalaIndex = index
                                                                isKendalaSheetOpen = true
                                                            }
                                                        )

                                                        Spacer(modifier = Modifier.height(10.dp))

                                                        // b. Dua Kolom Waktu (Time Picker / Format JJ:MM)
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                        ) {
                                                            KendalaTimeInputField(
                                                                label = "Waktu Mulai Kendala",
                                                                value = item.waktuMulai,
                                                                onValueChange = { viewModel.updateKendalaWaktuMulai(index, it) },
                                                                placeholder = "08:00",
                                                                isError = item.waktuMulai.isBlank() || KendalaItemFormState.parseTimeToMinutes(item.waktuMulai) == null || isRowOverlapping,
                                                                errorMessage = if (item.waktuMulai.isNotBlank() && KendalaItemFormState.parseTimeToMinutes(item.waktuMulai) == null) "Format salah (JJ:MM)" else null,
                                                                modifier = Modifier.weight(1f),
                                                                testTag = "input_waktu_mulai_${index + 1}"
                                                            )

                                                            KendalaTimeInputField(
                                                                label = "Waktu Selesai Kendala",
                                                                value = item.waktuSelesai,
                                                                onValueChange = { viewModel.updateKendalaWaktuSelesai(index, it) },
                                                                placeholder = "10:15",
                                                                isError = item.waktuSelesai.isBlank() || KendalaItemFormState.parseTimeToMinutes(item.waktuSelesai) == null || (item.waktuMulai.isNotBlank() && !item.isTimeRangeValid) || isRowOverlapping,
                                                                errorMessage = if (item.waktuSelesai.isNotBlank() && KendalaItemFormState.parseTimeToMinutes(item.waktuSelesai) == null) "Format salah (JJ:MM)" else null,
                                                                modifier = Modifier.weight(1f),
                                                                testTag = "input_waktu_selesai_${index + 1}"
                                                            )
                                                        }

                                                        Spacer(modifier = Modifier.height(8.dp))

                                                        // c. Perhitungan Durasi Otomatis & Validasi Waktu / Overlapping
                                                        if (item.waktuMulai.isNotBlank() && item.waktuSelesai.isNotBlank()) {
                                                            if (!item.isTimeRangeValid) {
                                                                Surface(
                                                                    shape = RoundedCornerShape(8.dp),
                                                                    color = Color(0xFFFEF2F2),
                                                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECACA)),
                                                                    modifier = Modifier.fillMaxWidth().testTag("error_waktu_kendala_${index + 1}")
                                                                ) {
                                                                    Row(
                                                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                                        verticalAlignment = Alignment.CenterVertically
                                                                    ) {
                                                                        Icon(
                                                                            Icons.Default.ErrorOutline,
                                                                            contentDescription = null,
                                                                            tint = Color(0xFFDC2626),
                                                                            modifier = Modifier.size(16.dp)
                                                                        )
                                                                        Spacer(modifier = Modifier.width(6.dp))
                                                                        Text(
                                                                            text = if (item.startMinutes == null || item.endMinutes == null) {
                                                                                "Format jam salah (gunakan format JJ:MM, misal 08:00 atau 23:00)"
                                                                            } else {
                                                                                "Waktu Mulai dan Waktu Selesai kendala tidak boleh sama (JJ:MM)"
                                                                            },
                                                                            fontSize = 11.sp,
                                                                            fontWeight = FontWeight.Medium,
                                                                            color = Color(0xFFB91C1C)
                                                                        )
                                                                    }
                                                                }
                                                            } else if (isRowOverlapping) {
                                                                // Validasi Anti-Overlapping Jam Kendala
                                                                Surface(
                                                                    shape = RoundedCornerShape(8.dp),
                                                                    color = Color(0xFFFEF2F2),
                                                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECACA)),
                                                                    modifier = Modifier.fillMaxWidth().testTag("error_overlap_kendala_${index + 1}")
                                                                ) {
                                                                    Row(
                                                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                                        verticalAlignment = Alignment.CenterVertically
                                                                    ) {
                                                                        Icon(
                                                                            Icons.Default.ErrorOutline,
                                                                            contentDescription = null,
                                                                            tint = Color(0xFFDC2626),
                                                                            modifier = Modifier.size(16.dp)
                                                                        )
                                                                        Spacer(modifier = Modifier.width(6.dp))
                                                                        Text(
                                                                            text = "Jam kendala bertabrakan dengan kendala lainnya!",
                                                                            fontSize = 11.sp,
                                                                            fontWeight = FontWeight.Bold,
                                                                            color = Color(0xFFB91C1C)
                                                                        )
                                                                    }
                                                                }
                                                            } else {
                                                                Surface(
                                                                    shape = RoundedCornerShape(8.dp),
                                                                    color = Color(0xFFFEF3C7),
                                                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A)),
                                                                    modifier = Modifier.fillMaxWidth().testTag("durasi_kendala_${index + 1}")
                                                                ) {
                                                                    Row(
                                                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                                        verticalAlignment = Alignment.CenterVertically
                                                                    ) {
                                                                        Icon(
                                                                            Icons.Default.Schedule,
                                                                            contentDescription = null,
                                                                            tint = Color(0xFFD97706),
                                                                            modifier = Modifier.size(16.dp)
                                                                        )
                                                                        Spacer(modifier = Modifier.width(6.dp))
                                                                        Text(
                                                                            text = "Total Hambatan: ${item.durasiLabel} (${item.durasiMenit} Menit)",
                                                                            fontSize = 12.sp,
                                                                            fontWeight = FontWeight.Bold,
                                                                            color = Color(0xFF92400E)
                                                                        )
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }

                                            // Alert Banner jika Overlap terjadi di baris mana pun
                                            if (realisasiState.hasAnyKendalaOverlap) {
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = Color(0xFFFEF2F2),
                                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECACA)),
                                                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp).testTag("banner_overlap_kendala")
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(10.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text(
                                                            text = "Jam kendala bertabrakan dengan kendala lainnya! Perbaiki rentang waktu kendala sebelum mengirim.",
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color(0xFFB91C1C)
                                                        )
                                                    }
                                                }
                                            }

                                            // Alert Banner jika Akumulasi Jam Kendala melebihi Shift
                                            if (realisasiState.isTotalKendalaExceedingShift) {
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = Color(0xFFFEF2F2),
                                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECACA)),
                                                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp).testTag("banner_limit_shift_kendala")
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(10.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text(
                                                            text = "Akumulasi total kendala (${realisasiState.totalHambatanLabel}) melebihi batas maksimal durasi jam kerja shift (${String.format(Locale.US, "%.1f", realisasiState.maxShiftHours)} Jam)!",
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color(0xFFB91C1C)
                                                        )
                                                    }
                                                }
                                            }

                                            // Tombol Tambah Kendala Lain
                                            OutlinedButton(
                                                onClick = { viewModel.addKendalaRow() },
                                                shape = RoundedCornerShape(10.dp),
                                                colors = ButtonDefaults.outlinedButtonColors(
                                                    contentColor = Color(0xFF92400E)
                                                ),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B)),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .testTag("btn_tambah_kendala")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Add,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "+ Tambah Kendala Lain",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp
                                                )
                                            }
                                        }
                                    }
                                }

                                // Warning jika over-standard
                                if (isOverMax) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0xFFFFF7ED),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFB923C)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFEA580C), modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            val maxStdNotice = if (realisasiState.hmMaxStandar % 1.0 == 0.0) realisasiState.hmMaxStandar.toInt().toString() else String.format(Locale.US, "%.1f", realisasiState.hmMaxStandar)
                                            Text(
                                                text = "HM Berjalan melebihi standar maksimal unit ($maxStdNotice HM). Dialog konfirmasi lembur operasional akan ditampilkan sebelum kirim.",
                                                fontSize = 11.sp,
                                                color = Color(0xFF9A3412),
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // ==========================================
                        // 5. PARAMETER STOK / STIK SOLAR & BBM
                        // ==========================================
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFFF8FAFC),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(bottom = 10.dp)
                                ) {
                                    Icon(Icons.Default.LocalGasStation, contentDescription = null, tint = Emerald600, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "5. Stok Stik Solar & Pemakaian BBM",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Slate900
                                    )
                                }

                                // Input Stik Solar Awal & Akhir
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Stik Solar Awal", fontSize = 11.sp, color = Slate700, fontWeight = FontWeight.SemiBold)
                                        OutlinedTextField(
                                            value = realisasiState.stikSolarAwalInput,
                                            onValueChange = { viewModel.setRealisasiStikAwal(it) },
                                            placeholder = { Text("Contoh: 85.0", fontSize = 12.sp, color = Slate400) },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                            trailingIcon = { Text("cm/L", color = Slate400, fontSize = 11.sp, modifier = Modifier.padding(end = 8.dp)) },
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = Emerald600,
                                                unfocusedBorderColor = Slate200,
                                                focusedContainerColor = Color.White,
                                                unfocusedContainerColor = Color.White
                                            ),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.fillMaxWidth().testTag("input_stik_solar_awal")
                                        )
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Stik Solar Akhir", fontSize = 11.sp, color = Slate700, fontWeight = FontWeight.SemiBold)
                                        OutlinedTextField(
                                            value = realisasiState.stikSolarAkhirInput,
                                            onValueChange = { viewModel.setRealisasiStikAkhir(it) },
                                            placeholder = { Text("Contoh: 60.0", fontSize = 12.sp, color = Slate400) },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                            trailingIcon = { Text("cm/L", color = Slate400, fontSize = 11.sp, modifier = Modifier.padding(end = 8.dp)) },
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = Emerald600,
                                                unfocusedBorderColor = Slate200,
                                                focusedContainerColor = Color.White,
                                                unfocusedContainerColor = Color.White
                                            ),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.fillMaxWidth().testTag("input_stik_solar_akhir")
                                        )
                                    }
                                }

                                // 1. Peringatan Oranye: Stik Akhir > Stik Awal tanpa toggle Isi Solar
                                if (realisasiState.isStikAkhirHigherWithoutRefuel) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0xFFFFFBEB),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A)),
                                        modifier = Modifier.fillMaxWidth().testTag("warning_stik_akhir_higher")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Warning,
                                                contentDescription = null,
                                                tint = Color(0xFFD97706),
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Stik akhir lebih tinggi dari stik awal. Apakah ada pengisian BBM lapangan? Aktifkan pengisian jika unit baru diisi solar.",
                                                fontSize = 12.sp,
                                                color = Color(0xFFB45309),
                                                fontWeight = FontWeight.SemiBold,
                                                lineHeight = 16.sp
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Toggle Switch Pengisian Solar BBM Tambahan
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color.White,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (realisasiState.isRefuelLitersMissing) ErrorRose else Slate200),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.LocalGasStation, contentDescription = null, tint = Emerald600, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "Pengisian Solar BBM Tambahan (Fuel Truck)",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp,
                                                    color = Slate900
                                                )
                                            }
                                            Switch(
                                                checked = realisasiState.isIsiSolar,
                                                onCheckedChange = { viewModel.setRealisasiIsiSolar(it) },
                                                colors = SwitchDefaults.colors(
                                                    checkedThumbColor = Color.White,
                                                    checkedTrackColor = Emerald600
                                                ),
                                                modifier = Modifier.testTag("switch_isi_solar_tambahan")
                                            )
                                        }

                                        if (realisasiState.isIsiSolar) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "Jumlah Liter Pengisian BBM * (Wajib Diisi)",
                                                fontSize = 11.sp,
                                                color = if (realisasiState.isRefuelLitersMissing) ErrorRose else Slate700,
                                                fontWeight = FontWeight.SemiBold,
                                                modifier = Modifier.padding(bottom = 4.dp)
                                            )
                                            OutlinedTextField(
                                                value = realisasiState.jumlahLiterSolarInput,
                                                onValueChange = { viewModel.setRealisasiJumlahSolar(it) },
                                                placeholder = { Text("Contoh: 150 (Wajib diisi)", fontSize = 12.sp) },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                                isError = realisasiState.isRefuelLitersMissing,
                                                trailingIcon = { Text("Liter", color = Slate500, fontSize = 12.sp, modifier = Modifier.padding(end = 12.dp)) },
                                                singleLine = true,
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = Emerald600,
                                                    unfocusedBorderColor = if (realisasiState.isRefuelLitersMissing) ErrorRose else Slate200,
                                                    focusedContainerColor = Color(0xFFF8FAFC),
                                                    unfocusedContainerColor = Color(0xFFF8FAFC)
                                                ),
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.fillMaxWidth().testTag("input_jumlah_solar")
                                            )
                                            if (realisasiState.isRefuelLitersMissing) {
                                                Text(
                                                    text = "Wajib memasukkan jumlah liter pengisian BBM lapangan (> 0).",
                                                    color = ErrorRose,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    modifier = Modifier.padding(top = 4.dp, start = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Kalkulasi Visual Pemakaian Solar Otomatis:
                                // Formula: Konsumsi Solar = (Stik Solar Awal + Total Isi Solar Tambahan) - Stik Solar Akhir
                                val awalVal = realisasiState.parsedStikAwal ?: 0.0
                                val akhirVal = realisasiState.parsedStikAkhir ?: 0.0
                                val refuelVal = if (realisasiState.isIsiSolar) (realisasiState.parsedJumlahLiterSolar ?: 0.0) else 0.0
                                val konsumsiTotal = realisasiState.computedKonsumsiSolar

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (realisasiState.isSolarConsumptionNegative) Color(0xFFFEF2F2) else Emerald50,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (realisasiState.isSolarConsumptionNegative) Color(0xFFFECACA) else Emerald100),
                                    modifier = Modifier.fillMaxWidth().testTag("card_kalkulasi_solar")
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = "Formula Konsumsi Solar Otomatis:",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (realisasiState.isSolarConsumptionNegative) Color(0xFF991B1B) else Emerald900
                                                )
                                                Text(
                                                    text = "(${String.format(Locale.US, "%.1f", awalVal)} Awal + ${String.format(Locale.US, "%.1f", refuelVal)} Isi) - ${String.format(Locale.US, "%.1f", akhirVal)} Akhir",
                                                    fontSize = 10.sp,
                                                    color = if (realisasiState.isSolarConsumptionNegative) Color(0xFFB91C1C) else Emerald700,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                            Text(
                                                text = "${String.format(Locale.US, "%.1f", konsumsiTotal)} Liter",
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontFamily = FontFamily.Monospace,
                                                color = if (realisasiState.isSolarConsumptionNegative) Color(0xFFB91C1C) else Emerald900
                                            )
                                        }

                                        // 3. Peringatan Konsumsi Negatif
                                        if (realisasiState.isSolarConsumptionNegative) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = Color(0xFFFEE2E2),
                                                modifier = Modifier.fillMaxWidth().testTag("alert_solar_negatif")
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.ErrorOutline,
                                                        contentDescription = null,
                                                        tint = ErrorRose,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "Konsumsi solar tidak boleh bernilai negatif (${String.format(Locale.US, "%.1f", konsumsiTotal)} L). Periksa kembali angka stik solar dan pengisian BBM.",
                                                        fontSize = 11.sp,
                                                        color = Color(0xFF991B1B),
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // 6. Hasil Kerja Lapangan
                        val isOperasi = realisasiState.selectedRencana?.statusUnit != "STANDBY" && realisasiState.selectedRencana?.statusUnit != "BREAKDOWN"
                        val dynamicUnitLabel = realisasiState.selectedActivity?.unitOfMeasure ?: "m3"

                        Text(
                            text = "6. Hasil Kerja Lapangan (${if (isOperasi) "Satuan: $dynamicUnitLabel" else "Non-Aktif untuk Standby/Breakdown"})",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Slate700
                            ),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        OutlinedTextField(
                            value = if (isOperasi) realisasiState.hasilKerjaInput else "0 (Standby / Breakdown)",
                            onValueChange = { if (isOperasi) viewModel.setRealisasiHasilKerja(it) },
                            enabled = isOperasi,
                            placeholder = { Text("Volume hasil kerja (misal: 250)", color = Slate400, fontSize = 13.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            trailingIcon = {
                                Text(
                                    text = dynamicUnitLabel,
                                    color = if (isOperasi) Emerald700 else Slate400,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(end = 12.dp)
                                )
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Emerald600,
                                unfocusedBorderColor = Slate200,
                                disabledContainerColor = Color(0xFFF1F5F9),
                                disabledTextColor = Slate500
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("input_hasil_kerja")
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // 7. Foto Bukti Lapangan
                        Text(
                            text = "7. Foto Bukti Lapangan",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Slate700
                            ),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        PhotoProofCard(
                            photoPath = realisasiState.photoPath,
                            photoSizeKb = realisasiState.photoSizeKb,
                            photoDimensions = realisasiState.photoDimensions,
                            isCompressing = realisasiState.isCompressingPhoto,
                            onPhotoSelected = { uri ->
                                viewModel.processAndCompressRealisasiPhoto(context, uri)
                            },
                            onRemovePhoto = { viewModel.removeRealisasiPhoto() }
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // 8. Keterangan Lapangan (Anti-tertutup keyboard/QWERTY)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF8FAFC),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                            modifier = Modifier
                                .fillMaxWidth()
                                .bringIntoViewRequester(keteranganBringIntoViewRequester)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.EditNote,
                                            contentDescription = null,
                                            tint = Emerald600,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "8. Keterangan & Catatan Lapangan",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Slate800,
                                                fontSize = 12.sp
                                            )
                                        )
                                    }
                                    if (realisasiState.keterangan.isNotBlank()) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Emerald100
                                        ) {
                                            Text(
                                                text = "${realisasiState.keterangan.length} Karakter",
                                                color = Emerald900,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = realisasiState.keterangan,
                                    onValueChange = { viewModel.setRealisasiKeterangan(it) },
                                    placeholder = { Text("Kondisi cuaca, slip, antrean disposal, catatan shift...", color = Slate400, fontSize = 13.sp) },
                                    minLines = 3,
                                    maxLines = 6,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White,
                                        focusedBorderColor = Emerald600,
                                        unfocusedBorderColor = Slate200
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_realisasi_keterangan")
                                        .onFocusEvent { focusState ->
                                            if (focusState.isFocused) {
                                                coroutineScope.launch {
                                                    delay(250)
                                                    keteranganBringIntoViewRequester.bringIntoView()
                                                }
                                            }
                                        }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Validasi penguncian tombol kirim:
                        // 1. Standar HM minimal: jika HM kurang dari standar, kendala wajib diisi lengkap
                        val isUnderMinLimit = realisasiState.hmHarianBerjalan > 0.0 &&
                                realisasiState.hmHarianBerjalan < realisasiState.hmMinStandar

                        // 2. Anti-Overlapping & Batas Jam Shift
                        val hasAnyKendalaOverlap = realisasiState.hasAnyKendalaOverlap
                        val isTotalKendalaExceedingShift = realisasiState.isTotalKendalaExceedingShift
                        val hasIncompleteKendala = realisasiState.daftarKendala.any {
                            (it.selectedKendala != null || it.waktuMulai.isNotBlank() || it.waktuSelesai.isNotBlank()) && !it.isValid
                        }

                        val isLockedDueToKendala = if (isUnderMinLimit) {
                            realisasiState.daftarKendala.isEmpty() ||
                                    !realisasiState.daftarKendala.all { it.isValid } ||
                                    hasAnyKendalaOverlap ||
                                    isTotalKendalaExceedingShift
                        } else {
                            hasAnyKendalaOverlap || isTotalKendalaExceedingShift || hasIncompleteKendala
                        }

                        // 3. Koreksi Manual HM Awal: Alasan wajib diisi jika koreksi aktif
                        val isLockedDueToHmAwalKoreksi = realisasiState.isHmAwalCorrected && realisasiState.alasanKoreksiHm.isBlank()

                        // 4. HM Akhir wajib lebih besar dari HM Awal
                        val isHmAkhirEntered = realisasiState.hmAkhirInput.isNotBlank()
                        val isHmAkhirGreater = realisasiState.isHmValid && ((realisasiState.hmAkhirInput.replace(',', '.').toDoubleOrNull() ?: 0.0) > realisasiState.hmAwal)

                        // 5. Validasi Solar BBM: Wajib isi liter jika toggle aktif & cegah konsumsi bernilai negatif
                        val isLockedDueToSolar = realisasiState.isRefuelLitersMissing || realisasiState.isSolarConsumptionNegative

                        // 6. Validasi Lonjakan HM Ekstrem (> 24.0 jam)
                        val isLockedDueToExtremeHm24h = realisasiState.isHmJumpExtreme24h

                        val isSubmitEnabled = !realisasiState.isSubmitting &&
                                realisasiState.selectedRencana != null &&
                                isHmAkhirEntered &&
                                isHmAkhirGreater &&
                                !isLockedDueToExtremeHm24h &&
                                !isLockedDueToKendala &&
                                !isLockedDueToHmAwalKoreksi &&
                                !isLockedDueToSolar

                        // Tombol Aksi: Kirim Realisasi Kerja (Selesai)
                        Button(
                            onClick = {
                                if (realisasiState.isHmWarningExceed14h) {
                                    showExtremeHmWarningConfirmDialog = true
                                } else if (realisasiState.hmHarianBerjalan > realisasiState.hmMaxStandar) {
                                    showOverHmConfirmDialog = true
                                } else {
                                    viewModel.submitRealisasiKerja()
                                }
                            },
                            enabled = isSubmitEnabled,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Emerald600,
                                disabledContainerColor = Slate200
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("btn_kirim_realisasi_selesai")
                        ) {
                            if (realisasiState.isSubmitting) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Mengirim Realisasi...")
                            } else {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = when {
                                        realisasiState.selectedRencana == null -> "Pilih Unit / Rencana Kerja Terlebih Dahulu"
                                        !isHmAkhirEntered -> "Masukkan HM Akhir Lapangan"
                                        isLockedDueToExtremeHm24h -> "Lonjakan HM > 24 Jam! Periksa HM Akhir"
                                        !isHmAkhirGreater -> realisasiState.hmErrorMessage ?: "HM Akhir Wajib Lebih Besar dari HM Awal"
                                        isLockedDueToHmAwalKoreksi -> "Isi Alasan Koreksi HM Awal"
                                        hasAnyKendalaOverlap -> "Jam Kendala Bertabrakan!"
                                        isTotalKendalaExceedingShift -> "Durasi Kendala Melebihi Jam Shift!"
                                        isUnderMinLimit && realisasiState.daftarKendala.isEmpty() -> "Wajib Tambah Data Kendala"
                                        isUnderMinLimit && realisasiState.daftarKendala.any { it.selectedKendala == null } -> "Pilih Penyebab Kendala"
                                        isUnderMinLimit && realisasiState.daftarKendala.any { !it.isTimeRangeValid } -> "Lengkapi Jam Kendala (JJ:MM)"
                                        hasIncompleteKendala -> "Lengkapi Baris Kendala yang Diisi"
                                        realisasiState.isRefuelLitersMissing -> "Isi Jumlah Liter Pengisian BBM"
                                        realisasiState.isSolarConsumptionNegative -> "Konsumsi Solar Negatif (Periksa Stik)"
                                        !isSpkTerbit -> "Kirim Realisasi Kerja (SPK Menyusul)"
                                        else -> "Kirim Realisasi Kerja (Selesai)"
                                    },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = if (isSubmitEnabled) Color.White else Slate500
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
            // Spacer bawah agar keyboard QWERTY tidak menutupi input keterangan & tombol
            item {
                Spacer(modifier = Modifier.height(180.dp).imePadding())
            }
        }
    }

    // ==========================================
    // DIALOG KONFIRMASI RESET FORMULIR MANUAL
    // ==========================================
    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Reset Formulir Realisasi?", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(
                    text = "Seluruh draft inputan yang belum terkirim untuk unit ${realisasiState.selectedRencana?.kodeUnit ?: ""} akan dihapus dari cache lokal. Apakah Anda yakin?",
                    fontSize = 13.sp,
                    color = Slate700
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showResetConfirmDialog = false
                        viewModel.resetRealisasiFormManual()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Ya, Reset Formulir")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text("Batal", color = Slate700)
                }
            }
        )
    }

    // ======================================================================
    // DIALOG KONFIRMASI LONJAKAN HM > 14.0 JAM (DI ATAS JAM KERJA WAJAR SHIFT)
    // ======================================================================
    if (showExtremeHmWarningConfirmDialog) {
        val durasiHmVal = String.format(Locale.US, "%.1f", realisasiState.hmHarianBerjalan)
        AlertDialog(
            onDismissRequest = { showExtremeHmWarningConfirmDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Konfirmasi Lonjakan HM Shift", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(
                        text = "HM berjalan terdeteksi $durasiHmVal jam. Apakah angka meteran sudah benar?",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Slate900
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Durasi ini melebihi jam kerja normal shift (14 jam). Pastikan angka HM Akhir tidak salah ketik (typo).",
                        fontSize = 12.sp,
                        color = Slate600
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showExtremeHmWarningConfirmDialog = false
                        viewModel.submitRealisasiKerja()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald600)
                ) {
                    Text("Ya, Angka Sudah Benar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExtremeHmWarningConfirmDialog = false }) {
                    Text("Periksa Kembali", color = Slate700)
                }
            }
        )
    }

    // ==========================================
    // DIALOG KONFIRMASI OVER-HM (> STANDAR MAKSIMAL)
    // ==========================================
    if (showOverHmConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showOverHmConfirmDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Konfirmasi HM Melebihi Batas Standar", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    val maxStdDialog = if (realisasiState.hmMaxStandar % 1.0 == 0.0) realisasiState.hmMaxStandar.toInt().toString() else String.format(Locale.US, "%.1f", realisasiState.hmMaxStandar)
                    Text(
                        text = "HM Berjalan yang diinput adalah ${String.format(Locale.US, "%.1f", realisasiState.hmHarianBerjalan)} Jam, melebihi standar maksimal unit ($maxStdDialog Jam).",
                        fontSize = 13.sp,
                        color = Slate700
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Apakah Anda mengonfirmasi pekerjaan lembur shift atau kebutuhan operasional tinggi ini telah disetujui?",
                        fontSize = 12.sp,
                        color = Slate500
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showOverHmConfirmDialog = false
                        viewModel.submitRealisasiKerja()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald600)
                ) {
                    Text("Ya, Kirim Realisasi")
                }
            },
            dismissButton = {
                TextButton(onClick = { showOverHmConfirmDialog = false }) {
                    Text("Periksa Kembali", color = Slate700)
                }
            }
        )
    }

    // ==========================================
    // MODAL SEARCHABLE SELECTOR SHEETS
    // ==========================================

    // Sheet 1: Unit Selector with Backlog Detection
    UnitPlanSearchableSelectorSheet(
        isOpen = isUnitSheetOpen,
        onDismissRequest = { isUnitSheetOpen = false },
        availableUnits = availableUnitsForRencana,
        selectedStatus = rencanaState.selectedUnitStatus,
        onStatusSelected = {
            viewModel.selectRencanaUnitStatus(it)
            isUnitSheetOpen = false
        }
    )

    // Sheet 2: Operator Selector (Main or Sub-Plan)
    OperatorSearchableSelectorSheet(
        isOpen = isOperatorSheetOpen,
        onDismissRequest = {
            isOperatorSheetOpen = false
            activeSubPlanOperatorTarget = null
        },
        operators = operators,
        selectedOperator = if (activeSubPlanOperatorTarget != null) {
            rencanaState.subPlans.find { it.id == activeSubPlanOperatorTarget }?.selectedOperator
        } else {
            rencanaState.selectedOperator
        },
        onOperatorSelected = { op ->
            val subId = activeSubPlanOperatorTarget
            if (subId != null) {
                viewModel.updateSubRencanaOperator(subId, op)
            } else {
                viewModel.selectRencanaOperator(op)
            }
            activeSubPlanOperatorTarget = null
            isOperatorSheetOpen = false
        }
    )

    // Sheet 3: Activity Selector (Main or Sub-Plan)
    ActivitySearchableSelectorSheet(
        isOpen = isRencanaActivitySheetOpen,
        onDismissRequest = {
            isRencanaActivitySheetOpen = false
            activeSubPlanActivityTarget = null
        },
        activities = activities,
        selectedActivity = if (activeSubPlanActivityTarget != null) {
            rencanaState.subPlans.find { it.id == activeSubPlanActivityTarget }?.selectedActivity
        } else {
            rencanaState.selectedActivity
        },
        onActivitySelected = { act ->
            val subId = activeSubPlanActivityTarget
            if (subId != null) {
                viewModel.updateSubRencanaActivity(subId, act)
            } else {
                viewModel.selectRencanaActivity(act)
            }
            activeSubPlanActivityTarget = null
            isRencanaActivitySheetOpen = false
        }
    )

    // Sheet 4: Lokasi Selector (Main or Sub-Plan)
    LokasiSearchableSelectorSheet(
        isOpen = isLokasiSheetOpen,
        onDismissRequest = {
            isLokasiSheetOpen = false
            activeSubPlanLokasiTarget = null
        },
        lokasiList = lokasiList,
        selectedLokasi = if (activeSubPlanLokasiTarget != null) {
            rencanaState.subPlans.find { it.id == activeSubPlanLokasiTarget }?.selectedLokasi
        } else {
            rencanaState.selectedLokasi
        },
        onLokasiSelected = { lok ->
            val subId = activeSubPlanLokasiTarget
            if (subId != null) {
                viewModel.updateSubRencanaLokasi(subId, lok)
            } else {
                viewModel.selectRencanaLokasi(lok)
            }
            activeSubPlanLokasiTarget = null
            isLokasiSheetOpen = false
        }
    )

    // Sheet 5: Realisasi Editable Activity Sheet
    ActivitySearchableSelectorSheet(
        isOpen = isRealisasiActivitySheetOpen,
        onDismissRequest = { isRealisasiActivitySheetOpen = false },
        activities = activities,
        selectedActivity = realisasiState.selectedActivity,
        onActivitySelected = {
            viewModel.setRealisasiActivity(it)
            isRealisasiActivitySheetOpen = false
        }
    )

    // Sheet 6: Realisasi Editable Lokasi Sheet
    LokasiSearchableSelectorSheet(
        isOpen = isRealisasiLokasiSheetOpen,
        onDismissRequest = { isRealisasiLokasiSheetOpen = false },
        lokasiList = lokasiList,
        selectedLokasi = realisasiState.selectedLokasi,
        onLokasiSelected = {
            viewModel.setRealisasiLokasi(it)
            isRealisasiLokasiSheetOpen = false
        }
    )

    // Sheet 7: Master Kendala Searchable Selector Sheet
    KendalaSearchableSelectorSheet(
        isOpen = isKendalaSheetOpen,
        onDismissRequest = { isKendalaSheetOpen = false },
        kendalaList = kendalaList,
        selectedKendala = activeKendalaIndex?.let { realisasiState.daftarKendala.getOrNull(it)?.selectedKendala },
        onKendalaSelected = { kendala ->
            activeKendalaIndex?.let { idx ->
                viewModel.updateKendalaSelection(idx, kendala)
            }
            isKendalaSheetOpen = false
        }
    )
}

@Composable
fun KendalaTimeInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "JJ:MM",
    isError: Boolean = false,
    errorMessage: String? = null,
    testTag: String = ""
) {
    val context = LocalContext.current
    val parsedHour = value.split(":").getOrNull(0)?.toIntOrNull() ?: 8
    val parsedMinute = value.split(":").getOrNull(1)?.toIntOrNull() ?: 0

    val timePickerDialog = remember(context, value) {
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                val formatted = String.format(Locale.US, "%02d:%02d", hourOfDay, minute)
                onValueChange(formatted)
            },
            parsedHour.coerceIn(0, 23),
            parsedMinute.coerceIn(0, 59),
            true // 24-hour format
        )
    }

    Column(modifier = modifier) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isError) Color(0xFFDC2626) else Slate700,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = { input ->
                val filtered = input.take(5)
                onValueChange(filtered)
            },
            placeholder = { Text(placeholder, fontSize = 12.sp, color = Slate400) },
            singleLine = true,
            isError = isError,
            trailingIcon = {
                IconButton(
                    onClick = { timePickerDialog.show() },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = "Pilih Waktu",
                        tint = if (isError) Color(0xFFDC2626) else Emerald600,
                        modifier = Modifier.size(18.dp)
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Emerald600,
                unfocusedBorderColor = if (isError) Color(0xFFEF4444) else Slate200,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag(testTag)
        )
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                fontSize = 10.sp,
                color = Color(0xFFDC2626),
                modifier = Modifier.padding(top = 2.dp, start = 2.dp)
            )
        }
    }
}
