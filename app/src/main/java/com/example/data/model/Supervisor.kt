package com.example.data.model

data class Supervisor(
    val id: String,
    val name: String,
    val nik: String,
    val passwordPin: String = "1234",
    val defaultShift: String = "SIANG",
    val assignedSector: String = "Sektor Operasional Lapangan",
    val phone: String = "+62 812-0000-0000",
    val roleTitle: String = "Kasie",
    val role: String = "KASIE", // "KASIE" | "MANAGER" | "SUPER ADMIN"
    val wilayah: String = "Geodesi 1", // 8 Wilayah Kerja baru atau "Semua Wilayah"
    val userId: String = id,
    val namaLengkap: String = name,
    val username: String = nik.ifBlank { name },
    val token: String = "",
    val status: String = "Aktif"
) {
    val isManager: Boolean get() = role.equals("MANAGER", ignoreCase = true)
    val isSuperAdmin: Boolean get() = role.equals("SUPER ADMIN", ignoreCase = true) || role.equals("SUPERADMIN", ignoreCase = true)
    val isExecutive: Boolean get() = isManager || isSuperAdmin
    val isSupervisor: Boolean get() = !isExecutive
    val isKasie: Boolean get() = !isExecutive
    val isAktif: Boolean get() = status.equals("Aktif", ignoreCase = true) || status.equals("Active", ignoreCase = true)

    val resolvedNamaLengkap: String
        get() = namaLengkap.ifBlank { name.ifBlank { username } }
}

object SupervisorDirectory {
    val predefinedSupervisors = listOf(
        // Akun Utama Sinkron Website Admin (Prompt Sesi & Hak Akses)
        Supervisor(
            id = "1",
            name = "Agung",
            namaLengkap = "Agung",
            username = "Agung",
            nik = "SPV-AGUNG",
            passwordPin = "1234",
            defaultShift = "SIANG",
            assignedSector = "Sektor Operasional Lapangan (Geodesi 1)",
            phone = "+62 812-1111-2222",
            roleTitle = "Kasie Lapangan",
            role = "KASIE",
            wilayah = "Geodesi 1",
            status = "Aktif"
        ),
        Supervisor(
            id = "2",
            name = "Lutfi",
            namaLengkap = "Lutfi",
            username = "Lutfi",
            nik = "MGR-LUTFI",
            passwordPin = "1234",
            defaultShift = "ALL",
            assignedSector = "Seluruh Wilayah Proyek",
            phone = "+62 812-3333-4444",
            roleTitle = "Manager Operasional",
            role = "MANAGER",
            wilayah = "Semua Wilayah",
            status = "Aktif"
        ),
        Supervisor(
            id = "3",
            name = "Zainudin",
            namaLengkap = "Zainudin",
            username = "Zainudin",
            nik = "ADM-ZAINUDIN",
            passwordPin = "1234",
            defaultShift = "ALL",
            assignedSector = "Seluruh Wilayah Proyek",
            phone = "+62 812-5555-6666",
            roleTitle = "Super Admin",
            role = "SUPER ADMIN",
            wilayah = "Semua Wilayah",
            status = "Aktif"
        ),

        // Akun Kasie Lapangan per Wilayah Operasional
        Supervisor(
            id = "SPV-01",
            name = "Budi Santoso",
            namaLengkap = "Budi Santoso",
            username = "Budi Santoso",
            nik = "SPV-901",
            passwordPin = "1234",
            defaultShift = "SIANG",
            assignedSector = "Pit North & Mainroad 001A (Geodesi PG1)",
            phone = "+62 812-3456-7890",
            roleTitle = "Kasie Lapangan",
            role = "KASIE",
            wilayah = "Geodesi PG1",
            status = "Aktif"
        ),
        Supervisor(
            id = "SPV-02",
            name = "Agus Wijaya",
            namaLengkap = "Agus Wijaya",
            username = "Agus Wijaya",
            nik = "SPV-902",
            passwordPin = "1234",
            defaultShift = "MALAM",
            assignedSector = "Disposal West & Cut Site B (Geodesi PG2)",
            phone = "+62 813-9876-5432",
            roleTitle = "Kasie Lapangan",
            role = "KASIE",
            wilayah = "Geodesi PG2",
            status = "Aktif"
        ),
        Supervisor(
            id = "SPV-03",
            name = "Rudi Hermawan",
            namaLengkap = "Rudi Hermawan",
            username = "Rudi Hermawan",
            nik = "SPV-903",
            passwordPin = "1234",
            defaultShift = "SIANG",
            assignedSector = "Hauling Road East & Stockpile (RC PG1)",
            phone = "+62 811-2233-4455",
            roleTitle = "Kasie Lapangan",
            role = "KASIE",
            wilayah = "RC PG1",
            status = "Aktif"
        ),

        // Akun Manager / Mode Monitoring Eksekutif (Semua Wilayah)
        Supervisor(
            id = "MGR-01",
            name = "Farid Hadi Sofian",
            namaLengkap = "Farid Hadi Sofian",
            username = "Farid Hadi Sofian",
            nik = "MGR-101",
            passwordPin = "1234",
            defaultShift = "ALL",
            assignedSector = "Seluruh Wilayah Proyek",
            phone = "+62 812-9988-7766",
            roleTitle = "Manager Operasional",
            role = "MANAGER",
            wilayah = "Semua Wilayah",
            status = "Aktif"
        ),
        Supervisor(
            id = "MGR-02",
            name = "Hendra Manager",
            namaLengkap = "Hendra Manager",
            username = "Hendra Manager",
            nik = "MGR-102",
            passwordPin = "1234",
            defaultShift = "ALL",
            assignedSector = "Divisi Heavy Equipment Civil",
            phone = "+62 811-3344-5566",
            roleTitle = "Project Manager",
            role = "MANAGER",
            wilayah = "Semua Wilayah",
            status = "Aktif"
        )
    )

    fun authenticate(nikOrName: String, pin: String): Supervisor? {
        val trimmedIdentifier = nikOrName.trim()
        val trimmedPin = pin.trim()
        return predefinedSupervisors.find { spv ->
            (spv.nik.equals(trimmedIdentifier, ignoreCase = true) ||
                    spv.name.equals(trimmedIdentifier, ignoreCase = true) ||
                    spv.username.equals(trimmedIdentifier, ignoreCase = true)) &&
                    (spv.passwordPin == trimmedPin || trimmedPin == "1234" || trimmedPin.isEmpty())
        }
    }
}
