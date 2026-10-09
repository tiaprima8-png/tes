package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.local.entity.ActivityReportEntity
import java.io.File
import com.example.ui.components.FuelBadge
import com.example.ui.components.ShiftBadge
import com.example.ui.components.SyncStatusBadge
import com.example.ui.components.UnitStatusBadge
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

@Composable
fun ReportDetailDialog(
    report: ActivityReportEntity,
    onDismiss: () -> Unit,
    onDelete: (Long) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Detail Laporan Aktivitas",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                        )
                        Text(
                            text = "UUID: ${report.clientUuid.take(13)}...",
                            style = MaterialTheme.typography.bodySmall.copy(color = Slate400)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = Slate500)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Slate200)

                // Status & Shift Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        UnitStatusBadge(status = report.unitStatus)
                        if (report.isRefueled && report.fuelLiters > 0.0) {
                            FuelBadge(liters = report.fuelLiters)
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

                Spacer(modifier = Modifier.height(14.dp))

                // Detail Rows
                DetailItemRow(
                    icon = Icons.Default.PrecisionManufacturing,
                    label = "Unit Alat Berat",
                    value = "${report.unitCode} (${report.unitType}) - Status: ${report.unitStatus}"
                )

                if (report.isRefueled && report.fuelLiters > 0.0) {
                    DetailItemRow(
                        icon = Icons.Default.LocalGasStation,
                        label = "Pengisian Bahan Bakar",
                        value = "${if (report.fuelLiters % 1.0 == 0.0) report.fuelLiters.toInt() else report.fuelLiters} Liter Solar"
                    )
                }

                DetailItemRow(
                    icon = Icons.Default.Assignment,
                    label = "Aktivitas & Kode SAP",
                    value = "${report.activityName}\n[${report.sapCode}]"
                )

                DetailItemRow(
                    icon = Icons.Default.Person,
                    label = "Operator Pelaksana",
                    value = report.operatorName
                )

                DetailItemRow(
                    icon = Icons.Default.LocationOn,
                    label = "Lokasi Kerja Lapangan",
                    value = report.workLocation
                )

                DetailItemRow(
                    icon = Icons.Default.Engineering,
                    label = "Pengawas Lapangan",
                    value = report.supervisorName
                )

                // Hour Meter Summary Card inside dialog
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Emerald50,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Emerald100)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("HM Awal: ${String.format("%.1f", report.hmAwal)} Jam", color = Slate700, fontSize = 12.sp)
                            Text("HM Akhir: ${String.format("%.1f", report.hmAkhir)} Jam", color = Slate700, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("HM Harian Berjalan:", fontWeight = FontWeight.Bold, color = Emerald900, fontSize = 13.sp)
                            Text("+${String.format("%.2f", report.hmDailyRunning)} Jam", fontWeight = FontWeight.ExtraBold, color = Emerald700, fontSize = 15.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Hasil Kerja:", fontWeight = FontWeight.SemiBold, color = Slate900, fontSize = 13.sp)
                            Text("${String.format("%.1f", report.workOutput)} ${report.unitOfMeasure}", fontWeight = FontWeight.Bold, color = Emerald600, fontSize = 14.sp)
                        }
                    }
                }

                if (report.notes.isNotEmpty()) {
                    Text(
                        text = "Catatan: ${report.notes}",
                        style = MaterialTheme.typography.bodySmall.copy(color = Slate500),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // Foto Bukti Kegiatan Lapangan
                val hasPhoto = !report.photoPath.isNullOrBlank() || !report.photoBase64.isNullOrBlank()
                if (hasPhoto) {
                    val context = LocalContext.current
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF8FAFC),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Foto Bukti Kegiatan Lapangan",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Slate900
                                )
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Emerald100
                                ) {
                                    Text(
                                        text = "Ukuran: ${report.photoSizeKb ?: 145} KB",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = Emerald900,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            val photoFile = report.photoPath?.let { File(it) }
                            if (photoFile != null && photoFile.exists()) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(photoFile)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Foto Bukti Lapangan",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(140.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(60.dp)
                                        .background(Color(0xFFE2E8F0), RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Foto Bukti Terkompresi (Base64 tersimpan)",
                                        fontSize = 12.sp,
                                        color = Slate700
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            onDelete(report.id)
                            onDismiss()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRose),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ErrorRose.copy(alpha = 0.5f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Hapus")
                    }

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                        modifier = Modifier.weight(1.5f)
                    ) {
                        Text("Tutup")
                    }
                }
            }
        }
    }
}

@Composable
fun DetailItemRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Slate400, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(label, style = MaterialTheme.typography.bodySmall.copy(color = Slate400, fontSize = 11.sp))
            Text(value, style = MaterialTheme.typography.bodyMedium.copy(color = Slate900, fontWeight = FontWeight.SemiBold))
        }
    }
}
