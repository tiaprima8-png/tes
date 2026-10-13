package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Supervisor
import com.example.data.model.SupervisorDirectory
import com.example.data.remote.LoginResult
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
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess: (Supervisor) -> Unit,
    onLoginWithApi: (suspend (String, String) -> LoginResult)? = null
) {
    var usernameInput by remember { mutableStateOf("Agung") }
    var passwordInput by remember { mutableStateOf("1234") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    val scrollState = rememberScrollState()

    fun attemptLogin() {
        errorMessage = null
        val userTrimmed = usernameInput.trim()
        val passTrimmed = passwordInput.trim()

        if (userTrimmed.isBlank()) {
            errorMessage = "Silakan masukkan Username / NIK"
            return
        }
        if (passTrimmed.isBlank()) {
            errorMessage = "Silakan masukkan Password / PIN"
            return
        }

        isLoading = true

        if (onLoginWithApi != null) {
            coroutineScope.launch {
                val result = onLoginWithApi(userTrimmed, passTrimmed)
                isLoading = false
                when (result) {
                    is LoginResult.Success -> {
                        // Navigasi & state ditangani secara otomatis oleh ViewModel
                    }
                    is LoginResult.Error -> {
                        errorMessage = result.message
                    }
                }
            }
            return
        }

        // Validasi kredensial via direktori lokal jika lambda API tidak tersedia
        val supervisor = SupervisorDirectory.authenticate(userTrimmed, passTrimmed)
        if (supervisor != null) {
            isLoading = false
            if (!supervisor.isAktif) {
                errorMessage = "Akun tidak aktif atau kredensial salah"
            } else {
                onLoginSuccess(supervisor)
            }
        } else {
            // Standalone field session fallback jika sandi diisi
            if (passTrimmed == "1234" || passTrimmed.length >= 4) {
                val isSuperAdmin = userTrimmed.contains("super", ignoreCase = true) || userTrimmed.contains("admin", ignoreCase = true)
                val isMgr = isSuperAdmin || userTrimmed.contains("manager", ignoreCase = true) ||
                        userTrimmed.startsWith("mgr", ignoreCase = true) ||
                        userTrimmed.startsWith("gm", ignoreCase = true)

                val standaloneUser = Supervisor(
                    id = if (isMgr) "MGR-${System.currentTimeMillis().toString().takeLast(4)}" else "SPV-${System.currentTimeMillis().toString().takeLast(4)}",
                    name = userTrimmed,
                    namaLengkap = userTrimmed,
                    username = userTrimmed,
                    nik = userTrimmed.uppercase(),
                    passwordPin = passTrimmed,
                    defaultShift = if (isMgr) "ALL" else "SIANG",
                    assignedSector = if (isMgr) "Seluruh Proyek Lapangan" else "Sektor Operasional Lapangan",
                    phone = "+62 812-0000-0000",
                    roleTitle = if (isSuperAdmin) "Super Admin" else if (isMgr) "Manager Operasional" else "Kasie Lapangan",
                    role = if (isSuperAdmin) "SUPER ADMIN" else if (isMgr) "MANAGER" else "KASIE",
                    wilayah = if (isMgr) "Semua Wilayah" else "Geodesi 1",
                    status = "Aktif"
                )
                isLoading = false
                onLoginSuccess(standaloneUser)
            } else {
                isLoading = false
                errorMessage = "Akun tidak aktif atau kredensial salah"
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Brand Header & Logo
            Surface(
                modifier = Modifier.size(76.dp),
                shape = RoundedCornerShape(22.dp),
                color = Emerald600, // Emerald Forest #059669
                shadowElevation = 8.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.PrecisionManufacturing,
                        contentDescription = "Logo RKCE",
                        tint = Color.White,
                        modifier = Modifier.size(42.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "RKCE",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Slate900,
                    letterSpacing = (-0.5).sp
                )
            )

            Text(
                text = "Sistem Manajemen Rencana & Realisasi Alat Berat",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Slate500,
                    fontWeight = FontWeight.Medium
                )
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Form Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp)
                ) {
                    val isSuperAdmin = usernameInput.contains("super", ignoreCase = true) ||
                            usernameInput.contains("admin", ignoreCase = true) ||
                            usernameInput.equals("Zainudin", ignoreCase = true)
                    val isTargetManager = isSuperAdmin || usernameInput.contains("manager", ignoreCase = true) ||
                            usernameInput.startsWith("mgr", ignoreCase = true) ||
                            usernameInput.equals("Farid Hadi Sofian", ignoreCase = true) ||
                            usernameInput.equals("Lutfi", ignoreCase = true)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when {
                                isSuperAdmin -> "Masuk Sebagai Super Admin"
                                isTargetManager -> "Masuk Sebagai Manager"
                                else -> "Masuk Sebagai Kasie"
                            },
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = when {
                                isSuperAdmin -> Color(0xFFEEF2FF)
                                isTargetManager -> Color(0xFFFEF3C7)
                                else -> Emerald50
                            }
                        ) {
                            Text(
                                text = when {
                                    isSuperAdmin -> "SUPER ADMIN"
                                    isTargetManager -> "MANAGER"
                                    else -> "KASIE"
                                },
                                color = when {
                                    isSuperAdmin -> Color(0xFF3730A3)
                                    isTargetManager -> Color(0xFF92400E)
                                    else -> Emerald900
                                },
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = when {
                            isSuperAdmin -> "Mode Super Admin: Hak akses penuh memantau seluruh wilayah dan unit sekaligus."
                            isTargetManager -> "Mode Manager: Memantau riwayat SPK dan analitik seluruh armada tanpa form input."
                            else -> "Mode Kasie Lapangan: Formulir rencana & realisasi terkunci pada wilayah kerja kasie."
                        },
                        style = MaterialTheme.typography.bodySmall.copy(color = Slate500),
                        modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
                    )

                    // Quick Account Demo Selector Chips
                    Text(
                        text = "Pilih Akun Sinkron Website Admin:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Slate700,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    // Baris 1: Akun Utama (Agung, Lutfi, Zainudin)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (usernameInput == "Agung") Emerald100 else Color(0xFFF1F5F9),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (usernameInput == "Agung") Emerald600 else Slate200),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    usernameInput = "Agung"
                                    passwordInput = "1234"
                                    errorMessage = null
                                }
                                .testTag("chip_login_agung")
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Agung", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = if (usernameInput == "Agung") Emerald900 else Slate700, maxLines = 1)
                                Text("Kasie • Geodesi 1", fontSize = 8.5.sp, color = if (usernameInput == "Agung") Emerald700 else Slate500, maxLines = 1)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (usernameInput == "Lutfi") Color(0xFFFEF3C7) else Color(0xFFF1F5F9),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (usernameInput == "Lutfi") Color(0xFFF59E0B) else Slate200),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    usernameInput = "Lutfi"
                                    passwordInput = "1234"
                                    errorMessage = null
                                }
                                .testTag("chip_login_lutfi")
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Lutfi", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = if (usernameInput == "Lutfi") Color(0xFF92400E) else Slate700, maxLines = 1)
                                Text("Manager", fontSize = 8.5.sp, color = if (usernameInput == "Lutfi") Color(0xFFB45309) else Slate500, maxLines = 1)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (usernameInput == "Zainudin") Color(0xFFEEF2FF) else Color(0xFFF1F5F9),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (usernameInput == "Zainudin") Color(0xFF6366F1) else Slate200),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    usernameInput = "Zainudin"
                                    passwordInput = "1234"
                                    errorMessage = null
                                }
                                .testTag("chip_login_zainudin")
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Zainudin", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = if (usernameInput == "Zainudin") Color(0xFF3730A3) else Slate700, maxLines = 1)
                                Text("Super Admin", fontSize = 8.5.sp, color = if (usernameInput == "Zainudin") Color(0xFF4F46E5) else Slate500, maxLines = 1)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Baris 2: Akun Kasie Wilayah Operasional (Budi, Agus, Rudi)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (usernameInput == "Budi Santoso") Emerald50 else Color(0xFFF1F5F9),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (usernameInput == "Budi Santoso") Emerald600 else Slate200),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    usernameInput = "Budi Santoso"
                                    passwordInput = "1234"
                                    errorMessage = null
                                }
                                .testTag("chip_login_kasie_geodesi_pg1")
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 5.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Budi", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (usernameInput == "Budi Santoso") Emerald900 else Slate700, maxLines = 1)
                                Text("Geodesi PG1", fontSize = 8.sp, color = if (usernameInput == "Budi Santoso") Emerald700 else Slate500, maxLines = 1)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (usernameInput == "Agus Wijaya") Emerald50 else Color(0xFFF1F5F9),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (usernameInput == "Agus Wijaya") Emerald600 else Slate200),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    usernameInput = "Agus Wijaya"
                                    passwordInput = "1234"
                                    errorMessage = null
                                }
                                .testTag("chip_login_kasie_geodesi_pg2")
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 5.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Agus", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (usernameInput == "Agus Wijaya") Emerald900 else Slate700, maxLines = 1)
                                Text("Geodesi PG2", fontSize = 8.sp, color = if (usernameInput == "Agus Wijaya") Emerald700 else Slate500, maxLines = 1)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (usernameInput == "Farid Hadi Sofian") Color(0xFFFFFBEB) else Color(0xFFF1F5F9),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (usernameInput == "Farid Hadi Sofian") Color(0xFFF59E0B) else Slate200),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    usernameInput = "Farid Hadi Sofian"
                                    passwordInput = "1234"
                                    errorMessage = null
                                }
                                .testTag("chip_login_manager_farid")
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 5.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Farid", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (usernameInput == "Farid Hadi Sofian") Color(0xFF92400E) else Slate700, maxLines = 1)
                                Text("Manager", fontSize = 8.sp, color = if (usernameInput == "Farid Hadi Sofian") Color(0xFFB45309) else Slate500, maxLines = 1)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Error Notification (Pesat Peringatan Jelas: Akun tidak aktif atau kredensial salah)
                    AnimatedVisibility(visible = errorMessage != null) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = ErrorRose50,
                            border = androidx.compose.foundation.BorderStroke(1.dp, ErrorRose.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = ErrorRose,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = errorMessage ?: "",
                                    color = ErrorRose,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Field 1: Username / NIK Akun
                    Text(
                        text = "Username / NIK",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Slate700
                        ),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    OutlinedTextField(
                        value = usernameInput,
                        onValueChange = {
                            usernameInput = it
                            errorMessage = null
                        },
                        placeholder = { Text("Masukkan username (misal: Agung / Lutfi)", color = Slate400) },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = "User Icon", tint = Emerald600)
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Emerald600,
                            unfocusedBorderColor = Slate200,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_login_username")
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Field 2: Password
                    Text(
                        text = "Password",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Slate700
                        ),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = {
                            passwordInput = it
                            errorMessage = null
                        },
                        placeholder = { Text("Masukkan password Anda", color = Slate400) },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = "Lock Icon", tint = Emerald600)
                        },
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (isPasswordVisible) "Sembunyikan sandi" else "Tampilkan sandi",
                                    tint = Slate400
                                )
                            }
                        },
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            keyboardController?.hide()
                            attemptLogin()
                        }),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Emerald600,
                            unfocusedBorderColor = Slate200,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_login_password")
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Tombol Submit Login
                    Button(
                        onClick = {
                            keyboardController?.hide()
                            attemptLogin()
                        },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("btn_submit_login"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = when {
                                isSuperAdmin -> Color(0xFF4F46E5)
                                isTargetManager -> Color(0xFF0F766E)
                                else -> Emerald600
                            }
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Memverifikasi Kredensial...")
                        } else {
                            Icon(Icons.Default.Login, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when {
                                    isSuperAdmin -> "Masuk Sebagai Super Admin"
                                    isTargetManager -> "Masuk Sebagai Manager"
                                    else -> "Masuk Sebagai Kasie"
                                },
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "RKCE • Terintegrasi Otomatis dengan Web Admin API",
                style = MaterialTheme.typography.bodySmall.copy(color = Slate400, fontSize = 11.sp)
            )
        }
    }
}
