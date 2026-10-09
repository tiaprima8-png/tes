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
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockClock
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.RencanaKerjaEntity
import com.example.ui.components.ActivitySearchableSelectorSheet
import com.example.ui.components.LokasiSearchableSelectorSheet
import com.example.ui.components.OperatorSearchableSelectorSheet
import com.example.ui.components.PhotoProofCard
import com.example.ui.components.SelectorFieldTrigger
import com.example.ui.components.UnitSearchableSelectorSheet
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
    val activities by viewModel.activities.collectAsState()
    val operators by viewModel.operators.collectAsState()
    val lokasiList by viewModel.lokasiList.collectAsState()
    val activeRencanaList by viewModel.activeRencanaList.collectAsState()

    val rencanaState by viewModel.rencanaFormState.collectAsState()
    val realisasiState by viewModel.realisasiFormState.collectAsState()

    // Selector Sheets State
    var isUnitSheetOpen by remember { mutableStateOf(false) }
    var isOperatorSheetOpen by remember { mutableStateOf(false) }
    var isRencanaActivitySheetOpen by remember { mutableStateOf(false) }
    var isLokasiSheetOpen by remember { mutableStateOf(false) }
    var isActivitySheetOpen by remember { mutableStateOf(false) }

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
                            Column {
                                Text(
                                    text = "Persiapan Shift & Rencana Kerja",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Slate900
                                    )
                                )
                                Text(
                                    text = "Diisi oleh pengawas sebelum alat mulai beroperasi",
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

                        // 2. Unit Alat Berat Searchable Selector
                        SelectorFieldTrigger(
                            label = "2. Unit Alat Berat",
                            selectedValue = rencanaState.selectedUnit?.let { "${it.unitCode} - ${it.brandModel}" },
                            placeholder = "Pilih Unit Alat Berat (Excavator / Dozer / DT)...",
                            leadingIcon = Icons.Default.PrecisionManufacturing,
                            badgeText = rencanaState.selectedUnit?.unitType,
                            testTag = "selector_rencana_unit",
                            onClick = { isUnitSheetOpen = true }
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // 3. Operator Pelaksana Searchable Selector
                        SelectorFieldTrigger(
                            label = "3. Operator Pelaksana",
                            selectedValue = rencanaState.selectedOperator?.name,
                            placeholder = "Pilih Nama Operator Pelaksana...",
                            leadingIcon = Icons.Default.Person,
                            badgeText = rencanaState.selectedOperator?.badgeNumber,
                            testTag = "selector_rencana_operator",
                            onClick = { isOperatorSheetOpen = true }
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // 4. Aktivitas / Jenis Pekerjaan Searchable Selector (SAP Code & Satuan)
                        SelectorFieldTrigger(
                            label = "4. Aktivitas / Jenis Pekerjaan",
                            selectedValue = rencanaState.selectedActivity?.let { "${it.activityName} (${it.sapCode})" },
                            placeholder = "Pilih Jenis Pekerjaan (Galian, Loading, dll)...",
                            leadingIcon = Icons.Default.Assignment,
                            badgeText = rencanaState.selectedActivity?.unitOfMeasure,
                            testTag = "selector_rencana_aktivitas",
                            onClick = { isRencanaActivitySheetOpen = true }
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // 5. Lokasi Kerja Lapangan Searchable Selector
                        SelectorFieldTrigger(
                            label = "5. Lokasi Kerja Lapangan",
                            selectedValue = rencanaState.selectedLokasi?.let { "${it.kodeLokasi} (${it.wilayah})" },
                            placeholder = "Pilih Lokasi Kerja (001A / PG1)...",
                            leadingIcon = Icons.Default.LocationOn,
                            badgeText = rencanaState.selectedLokasi?.wilayah,
                            testTag = "selector_rencana_lokasi",
                            onClick = { isLokasiSheetOpen = true }
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

                        Spacer(modifier = Modifier.height(20.dp))

                        // Tombol Aksi: Kirim Rencana Kerja
                        Button(
                            onClick = { viewModel.submitRencanaKerja() },
                            enabled = !rencanaState.isSubmitting,
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
                                Text(
                                    text = "Kirim Rencana Kerja (Menunggu SPK)",
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
                                        text = "Silakan buat Rencana Kerja di Tab 1 terlebih dahulu.",
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
                                        Text("Buat Rencana Sekarang", fontSize = 12.sp)
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
                                            .width(220.dp)
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
                                                text = "Lok: ${ren.kodeLokasi} • Shift ${ren.shiftKerja}",
                                                fontSize = 10.sp,
                                                color = Slate500
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // 2. Nomor SPK (Surat Perintah Kerja) - Terkunci (Read-Only dari Web Admin)
                        val hasSpkPublished = realisasiState.nomorSpk.isNotBlank() && realisasiState.nomorSpk != "Belum Terbit (Menunggu Admin)"

                        Text(
                            text = "2. Nomor SPK (Surat Perintah Kerja)",
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

                                // Quick simulation action chip for demo/testing Web Admin SPK publishing
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

                        // Info Ringkasan Aktivitas Terjadwal
                        if (realisasiState.selectedRencana != null) {
                            val curActName = realisasiState.selectedActivity?.activityName
                                ?: realisasiState.selectedRencana?.namaAktivitas?.ifBlank { "Aktivitas Lapangan" }
                                ?: "Aktivitas Lapangan"
                            val curSap = realisasiState.selectedActivity?.sapCode
                                ?: realisasiState.selectedRencana?.sapCode?.ifBlank { "-" }
                                ?: "-"
                            val curUom = realisasiState.selectedActivity?.unitOfMeasure
                                ?: realisasiState.selectedRencana?.satuanAktivitas?.ifBlank { "m3" }
                                ?: "m3"

                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Emerald50,
                                border = androidx.compose.foundation.BorderStroke(1.dp, Emerald100),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Aktivitas: $curActName",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = Emerald900
                                        )
                                        Text(
                                            text = "Kode SAP: $curSap • Satuan Target: $curUom",
                                            fontSize = 10.sp,
                                            color = Emerald700
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color.White,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Emerald600.copy(alpha = 0.3f))
                                    ) {
                                        Text(
                                            text = "Ganti",
                                            color = Emerald700,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier
                                                .clickable { isActivitySheetOpen = true }
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // 3. Durasi Jam Kerja Numerik (dengan preset quick-pill 4j, 7j, 8j, 10j, 12j)
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

                        // 2. Blok Kalkulasi Hour Meter (HM)
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
                                    Icon(Icons.Default.Speed, contentDescription = null, tint = Emerald600, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "4. Hour Meter (HM)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Slate900
                                    )
                                }

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

                                // Error merah jika HM Akhir < HM Awal
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

                                // HM Berjalan Otomatis Seketika (HM Akhir - HM Awal)
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Emerald100,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "HM Berjalan Hari Ini:",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Emerald900
                                        )
                                        Text(
                                            text = "${String.format(Locale.US, "%.1f", realisasiState.hmHarianBerjalan)} Jam",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontFamily = FontFamily.Monospace,
                                            color = Emerald900
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 5. Pengisian Solar BBM
                        Text(
                            text = "5. Pengisian Solar BBM",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Slate700
                            ),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
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
                                        Icon(Icons.Default.LocalGasStation, contentDescription = null, tint = Emerald600, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Isi Solar Hari Ini?",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
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
                                        placeholder = { Text("Jumlah Liter (misal: 150)", fontSize = 13.sp) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        trailingIcon = { Text("Liter", color = Slate500, fontSize = 12.sp, modifier = Modifier.padding(end = 12.dp)) },
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Emerald600,
                                            unfocusedBorderColor = Slate200,
                                            focusedContainerColor = Color.White,
                                            unfocusedContainerColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth().testTag("input_jumlah_solar")
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // 6. Hasil Kerja Lapangan (Label dinamis satuan, 0 jika Standby/Breakdown)
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

                        // 7. Foto Bukti Lapangan (Kompresi Otomatis)
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

                        // 8. Keterangan Lapangan: Catatan kendala / kondisi akhir shift
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
                            placeholder = { Text("Kondisi cuaca, kendala unit, slip, antrean disposal...", color = Slate400, fontSize = 13.sp) },
                            minLines = 2,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Emerald600,
                                unfocusedBorderColor = Slate200
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("input_realisasi_keterangan")
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Tombol Aksi: Kirim Realisasi Kerja (Selesai)
                        Button(
                            onClick = { viewModel.submitRealisasiKerja() },
                            enabled = !realisasiState.isSubmitting,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
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
                                    text = "Kirim Realisasi Kerja (Selesai)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Searchable Selector Sheets
    UnitSearchableSelectorSheet(
        isOpen = isUnitSheetOpen,
        onDismissRequest = { isUnitSheetOpen = false },
        units = units,
        selectedUnit = rencanaState.selectedUnit,
        onUnitSelected = { viewModel.selectRencanaUnit(it) }
    )

    OperatorSearchableSelectorSheet(
        isOpen = isOperatorSheetOpen,
        onDismissRequest = { isOperatorSheetOpen = false },
        operators = operators,
        selectedOperator = rencanaState.selectedOperator,
        onOperatorSelected = { viewModel.selectRencanaOperator(it) }
    )

    LokasiSearchableSelectorSheet(
        isOpen = isLokasiSheetOpen,
        onDismissRequest = { isLokasiSheetOpen = false },
        lokasiList = lokasiList,
        selectedLokasi = rencanaState.selectedLokasi,
        onLokasiSelected = { viewModel.selectRencanaLokasi(it) }
    )

    ActivitySearchableSelectorSheet(
        isOpen = isActivitySheetOpen,
        onDismissRequest = { isActivitySheetOpen = false },
        activities = activities,
        selectedActivity = realisasiState.selectedActivity,
        onActivitySelected = { viewModel.setRealisasiActivity(it) }
    )

    ActivitySearchableSelectorSheet(
        isOpen = isRencanaActivitySheetOpen,
        onDismissRequest = { isRencanaActivitySheetOpen = false },
        activities = activities,
        selectedActivity = rencanaState.selectedActivity,
        onActivitySelected = {
            viewModel.selectRencanaActivity(it)
            isRencanaActivitySheetOpen = false
        }
    )
}
