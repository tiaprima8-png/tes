package com.example.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EditLocation
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockClock
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PrecisionManufacturing
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ActivityMasterEntity
import com.example.data.local.entity.KendalaEntity
import com.example.data.local.entity.LokasiEntity
import com.example.data.local.entity.OperatorEntity
import com.example.data.local.entity.RencanaKerjaEntity
import com.example.ui.components.ActivitySearchableSelectorSheet
import com.example.ui.components.KendalaSearchableSelectorSheet
import com.example.ui.components.LokasiSearchableSelectorSheet
import com.example.ui.components.OperatorSearchableSelectorSheet
import com.example.ui.components.PhotoProofCard
import com.example.ui.components.SelectorFieldTrigger
import com.example.ui.components.UnitPlanSearchableSelectorSheet
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald50
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Emerald700
import com.example.ui.theme.Emerald900
import com.example.ui.theme.ErrorRose
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.ui.theme.WarningAmber
import com.example.ui.viewmodel.HeavyTrackViewModel

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

    // Confirmation dialog for Over-HM (> max standard)
    var showOverHmConfirmDialog by remember { mutableStateOf(false) }

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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Persiapan Shift & Rencana Kerja",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Slate900
                                    )
                                )
                                Text(
                                    text = "Seluruh unit wajib dibuatkan rencana harian tanpa terlewat",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Slate500)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Emerald100
                            ) {
                                Text(
                                    text = "MENUNGGU SPK",
                                    color = Emerald900,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Banner jika semua unit sudah selesai terisi hari ini & tidak ada backlog
                        if (availableUnitsForRencana.isEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Emerald50,
                                border = androidx.compose.foundation.BorderStroke(1.dp, Emerald600.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Emerald600, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Semua Unit Binaan Selesai!",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = Emerald900
                                        )
                                        Text(
                                            text = "Seluruh unit telah dibuatkan rencana kerja hari ini & tidak ada tanggungan backlog. Unit otomatis hilang dari daftar.",
                                            fontSize = 11.sp,
                                            color = Emerald700
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

                        // 2. Unit Alat Berat Searchable Selector (Otomatis Tanggal & Deteksi Backlog)
                        SelectorFieldTrigger(
                            label = "2. Unit Alat Berat (Deteksi Backlog & Tanggal Otomatis)",
                            selectedValue = rencanaState.selectedUnit?.let { u ->
                                "${u.unitCode} - ${u.brandModel}"
                            },
                            placeholder = if (availableUnitsForRencana.isEmpty())
                                "Semua unit telah terisi rencana kerja"
                            else
                                "Pilih Unit Alat Berat binaan pengawas...",
                            leadingIcon = Icons.Default.PrecisionManufacturing,
                            badgeText = rencanaState.selectedUnit?.let {
                                if (rencanaState.isBacklog) "BACKLOG H-1" else "HARI INI"
                            },
                            testTag = "selector_rencana_unit",
                            onClick = { isUnitSheetOpen = true }
                        )

                        // Info Tanggal Otomatis Target
                        if (rencanaState.selectedUnit != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (rencanaState.isBacklog) Color(0xFFFEF2F2) else Color(0xFFF0FDF4),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (rencanaState.isBacklog) Color(0xFFFCA5A5) else Color(0xFF86EFAC)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (rencanaState.isBacklog) Icons.Default.Warning else Icons.Default.Info,
                                        contentDescription = null,
                                        tint = if (rencanaState.isBacklog) Color(0xFFDC2626) else Emerald700,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = if (rencanaState.isBacklog)
                                                "TARGET: TANGGAL KEMARIN (${rencanaState.targetDate})"
                                            else
                                                "TARGET: TANGGAL HARI INI (${rencanaState.targetDate})",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = if (rencanaState.isBacklog) Color(0xFF991B1B) else Emerald900
                                        )
                                        Text(
                                            text = if (rencanaState.isBacklog)
                                                "Unit belum diisi kemarin. Pengawas wajib menyelesaikan pengisian backlog ini terlebih dahulu."
                                            else
                                                "Tanggal otomatis terisi oleh sistem. Pengguna tidak perlu menginput tanggal manual.",
                                            fontSize = 10.sp,
                                            color = if (rencanaState.isBacklog) Color(0xFFB91C1C) else Emerald700
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
                        Text(
                            text = "Pelaporan Realisasi Akhir Shift",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                        )
                        Text(
                            text = "Diisi setelah pekerjaan selesai atau pada saat pergantian shift",
                            style = MaterialTheme.typography.bodySmall.copy(color = Slate500)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Pemilih Rencana Kerja Aktif
                        Text(
                            text = "Pilih Rencana Kerja Aktif Pengawas:",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Slate700
                            ),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )

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
                                    Button(
                                        onClick = { viewModel.setFormSubTab(0) },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text("Buat Rencana di Tab 1", fontSize = 12.sp)
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
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) Emerald50 else Color(0xFFF8FAFC),
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.5.dp,
                                            if (isSelected) Emerald600 else Slate200
                                        ),
                                        modifier = Modifier
                                            .width(230.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable { viewModel.selectRencanaForRealisasi(ren) }
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = ren.kodeUnit,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    color = Slate900
                                                )
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
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "Opr: ${ren.operator}",
                                                fontSize = 11.sp,
                                                color = Slate700
                                            )
                                            Text(
                                                text = "Tgl: ${ren.tanggalFormat} • Shift ${ren.shiftKerja}",
                                                fontSize = 10.sp,
                                                color = Slate500
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // 1. Nomor SPK (Surat Perintah Kerja) - Terkunci
                        val hasSpkPublished = realisasiState.nomorSpk.isNotBlank() && realisasiState.nomorSpk != "Belum Terbit (Menunggu Admin)"

                        Text(
                            text = "1. Nomor SPK (Surat Perintah Kerja)",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Slate700
                            ),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (hasSpkPublished) Color(0xFFF1F5F9) else Color(0xFFF8FAFC),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (hasSpkPublished) Emerald600.copy(alpha = 0.5f) else Slate200
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("spk_locked_field")
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = if (hasSpkPublished) Icons.Default.Lock else Icons.Default.LockClock,
                                            contentDescription = null,
                                            tint = if (hasSpkPublished) Emerald700 else Slate500,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = if (hasSpkPublished) realisasiState.nomorSpk else "Belum Terbit (Menunggu Admin)",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                fontFamily = if (hasSpkPublished) FontFamily.Monospace else FontFamily.Default,
                                                color = if (hasSpkPublished) Slate900 else Slate500
                                            )
                                            Text(
                                                text = if (hasSpkPublished)
                                                    "Terkunci resmi dari Web Admin (Read-Only)"
                                                else
                                                    "Admin belum menerbitkan SPK. Realisasi tetap dapat diisi.",
                                                fontSize = 10.sp,
                                                color = if (hasSpkPublished) Emerald700 else Slate400
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (hasSpkPublished) Emerald100 else Color(0xFFFEF3C7)
                                    ) {
                                        Text(
                                            text = if (hasSpkPublished) "TERKUNCI" else "MENUNGGU",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (hasSpkPublished) Emerald900 else Color(0xFFB45309),
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                if (!hasSpkPublished && realisasiState.selectedRencana != null) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Emerald50,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Emerald600.copy(alpha = 0.3f)),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                viewModel.adminSimulatePublishSpk(
                                                    realisasiState.selectedRencana!!.id,
                                                    realisasiState.selectedRencana!!.kodeUnit
                                                )
                                            }
                                            .testTag("btn_simulate_spk")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Bolt,
                                                contentDescription = null,
                                                tint = Emerald700,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Simulasikan Web Admin Terbitkan SPK",
                                                fontSize = 11.sp,
                                                color = Emerald900,
                                                fontWeight = FontWeight.Bold
                                            )
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
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Speed, contentDescription = null, tint = Emerald600, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "4. Hour Meter (HM) Mesin",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = Slate900
                                        )
                                    }

                                    // Standar Unit Badge dari Master Unit
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Emerald50,
                                        border = androidx.compose.foundation.BorderStroke(0.5.dp, Emerald600)
                                    ) {
                                        Text(
                                            text = "Standar: ${realisasiState.hmMinStandar} - ${realisasiState.hmMaxStandar} HM",
                                            color = Emerald900,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // HM Awal: Terkunci (Read-Only)
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("HM Awal (Terkunci)", fontSize = 11.sp, color = Slate500, fontWeight = FontWeight.SemiBold)
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = Color(0xFFE2E8F0),
                                            modifier = Modifier.fillMaxWidth().height(48.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.CenterStart, modifier = Modifier.padding(horizontal = 12.dp)) {
                                                Text(
                                                    text = String.format(Locale.US, "%.1f", realisasiState.hmAwal),
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    fontFamily = FontFamily.Monospace,
                                                    color = Slate700
                                                )
                                            }
                                        }
                                    }

                                    // HM Akhir: Input numerik desimal
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("HM Akhir Lapangan", fontSize = 11.sp, color = Slate700, fontWeight = FontWeight.Bold)
                                        OutlinedTextField(
                                            value = realisasiState.hmAkhirInput,
                                            onValueChange = { viewModel.setRealisasiHmAkhir(it) },
                                            placeholder = { Text("Contoh: 4132.5", fontSize = 12.sp, color = Slate400) },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                            isError = !realisasiState.isHmValid,
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = Emerald600,
                                                unfocusedBorderColor = Slate200,
                                                focusedContainerColor = Color.White,
                                                unfocusedContainerColor = Color.White
                                            ),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("input_hm_akhir")
                                        )
                                    }
                                }

                                // Quick Add Hours Buttons (+4j, +7j, +8j)
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                                ) {
                                    listOf(4.0, 7.0, 8.0, 10.0).forEach { qh ->
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Emerald50,
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Emerald100),
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(6.dp))
                                                .clickable { viewModel.quickAddRealisasiHours(qh) }
                                        ) {
                                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 4.dp)) {
                                                Text("+${qh.toInt()} Jam", fontSize = 10.sp, color = Emerald900, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }

                                if (!realisasiState.isHmValid && realisasiState.hmErrorMessage != null) {
                                    Text(
                                        text = realisasiState.hmErrorMessage ?: "",
                                        color = ErrorRose,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(top = 6.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Hasil Kalkulasi HM Berjalan Seketika
                                val runningHm = realisasiState.hmHarianBerjalan
                                val isUnderMin = runningHm > 0.0 && runningHm < realisasiState.hmMinStandar
                                val isOverMax = runningHm > realisasiState.hmMaxStandar

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = when {
                                        isUnderMin -> Color(0xFFFEE2E2)
                                        isOverMax -> Color(0xFFFEF3C7)
                                        else -> Emerald100
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "HM Berjalan Hari Ini:",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = when {
                                                    isUnderMin -> Color(0xFF991B1B)
                                                    isOverMax -> Color(0xFF92400E)
                                                    else -> Emerald900
                                                }
                                            )
                                            Text(
                                                text = when {
                                                    isUnderMin -> "Kurang dari standar minimal (${realisasiState.hmMinStandar} HM)"
                                                    isOverMax -> "Melebihi standar maksimal (${realisasiState.hmMaxStandar} HM)"
                                                    else -> "Sesuai standar operasional"
                                                },
                                                fontSize = 10.sp,
                                                color = when {
                                                    isUnderMin -> Color(0xFFB91C1C)
                                                    isOverMax -> Color(0xFFB45309)
                                                    else -> Emerald700
                                                }
                                            )
                                        }
                                        Text(
                                            text = "${String.format(Locale.US, "%.1f", runningHm)} Jam",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontFamily = FontFamily.Monospace,
                                            color = when {
                                                isUnderMin -> Color(0xFF991B1B)
                                                isOverMax -> Color(0xFF92400E)
                                                else -> Emerald900
                                            }
                                        )
                                    }
                                }

                                // ==========================================
                                // VALIDASI HM KURANG DARI STANDAR MINIMAL
                                // Wajib pilih penyebab kendala dari Master Kendala
                                // ==========================================
                                if (isUnderMin) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0xFFFFFBEB),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(18.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "Wajib Memilih Data Master Kendala",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp,
                                                    color = Color(0xFF92400E)
                                                )
                                            }
                                            Text(
                                                text = "HM berjalan kurang dari batas minimal (${realisasiState.hmMinStandar} HM). Pilih penyebab hambatan dari data master kendala Web Admin:",
                                                fontSize = 11.sp,
                                                color = Color(0xFF78350F),
                                                modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                                            )

                                            // Searchable Dropdown Master Kendala
                                            SelectorFieldTrigger(
                                                label = "Penyebab Kendala (Master Kendala)",
                                                selectedValue = realisasiState.selectedKendala?.let {
                                                    "${it.kodeKendala} - ${it.namaKendala} (${it.kategori})"
                                                },
                                                placeholder = "Pilih kendala (Hujan, Breakdown, dll)...",
                                                leadingIcon = Icons.Default.ReportProblem,
                                                badgeText = realisasiState.selectedKendala?.kategori,
                                                isError = realisasiState.selectedKendala == null,
                                                errorMessage = if (realisasiState.selectedKendala == null) "Wajib dipilih untuk membuka tombol kirim" else null,
                                                testTag = "selector_master_kendala",
                                                onClick = { isKendalaSheetOpen = true }
                                            )

                                            Spacer(modifier = Modifier.height(8.dp))

                                            // Kolom Opsional Catatan Tambahan Kendala
                                            OutlinedTextField(
                                                value = realisasiState.catatanKendala,
                                                onValueChange = { viewModel.setRealisasiCatatanKendala(it) },
                                                placeholder = { Text("Keterangan spesifik kendala di lapangan (opsional)...", fontSize = 12.sp, color = Slate400) },
                                                minLines = 2,
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = Emerald600,
                                                    unfocusedBorderColor = Slate200,
                                                    focusedContainerColor = Color.White,
                                                    unfocusedContainerColor = Color.White
                                                ),
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.fillMaxWidth().testTag("input_catatan_kendala")
                                            )
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
                                            Text(
                                                text = "HM Berjalan melebihi standar maksimal unit (${realisasiState.hmMaxStandar} HM). Dialog konfirmasi lembur operasional akan ditampilkan sebelum kirim.",
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

                                Spacer(modifier = Modifier.height(10.dp))

                                // Toggle Switch Pengisian Solar BBM Tambahan
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color.White,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
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
                                                    text = "Pengisian Solar BBM Tambahan",
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
                                                )
                                            )
                                        }

                                        if (realisasiState.isIsiSolar) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            OutlinedTextField(
                                                value = realisasiState.jumlahLiterSolarInput,
                                                onValueChange = { viewModel.setRealisasiJumlahSolar(it) },
                                                placeholder = { Text("Jumlah Liter Fuel Truck (misal: 150)", fontSize = 12.sp) },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                                trailingIcon = { Text("Liter", color = Slate500, fontSize = 12.sp, modifier = Modifier.padding(end = 12.dp)) },
                                                singleLine = true,
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = Emerald600,
                                                    unfocusedBorderColor = Slate200,
                                                    focusedContainerColor = Color(0xFFF8FAFC),
                                                    unfocusedContainerColor = Color(0xFFF8FAFC)
                                                ),
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.fillMaxWidth().testTag("input_jumlah_solar")
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Kalkulasi Visual Pemakaian Solar Otomatis
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Emerald50,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Emerald100),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "Kalkulasi Pemakaian Solar:",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Emerald900
                                            )
                                            val awalVal = realisasiState.stikSolarAwalInput.toDoubleOrNull() ?: 0.0
                                            val akhirVal = realisasiState.stikSolarAkhirInput.toDoubleOrNull() ?: 0.0
                                            val delta = (awalVal - akhirVal).coerceAtLeast(0.0)
                                            val refuel = if (realisasiState.isIsiSolar) (realisasiState.jumlahLiterSolarInput.toDoubleOrNull() ?: 0.0) else 0.0
                                            Text(
                                                text = "Selisih Stik (${String.format(Locale.US, "%.1f", delta)}) + Refuel (${String.format(Locale.US, "%.1f", refuel)} L)",
                                                fontSize = 10.sp,
                                                color = Emerald700
                                            )
                                        }
                                        Text(
                                            text = "${String.format(Locale.US, "%.1f", realisasiState.pemakaianSolarOtomatis)} Liter / cm",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontFamily = FontFamily.Monospace,
                                            color = Emerald900
                                        )
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

                        // 8. Keterangan Lapangan
                        Text(
                            text = "8. Keterangan Lapangan",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Slate700
                            ),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        OutlinedTextField(
                            value = realisasiState.keterangan,
                            onValueChange = { viewModel.setRealisasiKeterangan(it) },
                            placeholder = { Text("Kondisi cuaca, slip, antrean disposal, catatan shift...", color = Slate400, fontSize = 13.sp) },
                            minLines = 2,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Emerald600,
                                unfocusedBorderColor = Slate200
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("input_realisasi_keterangan")
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Validasi penguncian tombol kirim jika HM kurang dari standar dan kendala belum dipilih
                        val isLockedDueToKendala = realisasiState.hmHarianBerjalan > 0.0 &&
                                realisasiState.hmHarianBerjalan < realisasiState.hmMinStandar &&
                                realisasiState.selectedKendala == null

                        val isSubmitEnabled = !realisasiState.isSubmitting &&
                                realisasiState.selectedRencana != null &&
                                realisasiState.isHmValid &&
                                realisasiState.hmAkhirInput.isNotBlank() &&
                                !isLockedDueToKendala

                        // Tombol Aksi: Kirim Realisasi Kerja (Selesai)
                        Button(
                            onClick = {
                                if (realisasiState.hmHarianBerjalan > realisasiState.hmMaxStandar) {
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
                                    text = if (isLockedDueToKendala)
                                        "Pilih Kendala Terlebih Dahulu"
                                    else
                                        "Kirim Realisasi Kerja (Selesai)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = if (isSubmitEnabled) Color.White else Slate500
                                )
                            }
                        }
                    }
                }
            }
        }
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
                    Text(
                        text = "HM Berjalan yang diinput adalah ${String.format(Locale.US, "%.1f", realisasiState.hmHarianBerjalan)} Jam, melebihi standar maksimal unit (${realisasiState.hmMaxStandar} Jam).",
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
        selectedKendala = realisasiState.selectedKendala,
        onKendalaSelected = {
            viewModel.selectRealisasiKendala(it)
            isKendalaSheetOpen = false
        }
    )
}
