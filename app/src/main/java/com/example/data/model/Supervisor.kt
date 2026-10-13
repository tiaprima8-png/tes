package com.example.data.model

data class Supervisor(
    val id: String,
    val name: String,
    val nik: String,
    val passwordPin: String,
    val defaultShift: String,
    val assignedSector: String,
    val phone: String,
    val roleTitle: String = "Kasie",
    val role: String = "KASIE", // "KASIE" (alias "SUPERVISOR") vs "MANAGER"
    val wilayah: String = "PG1" // "PG1", "PG2", "PG3", "Semua Wilayah"
) {
    val isManager: Boolean get() = role.equals("MANAGER", ignoreCase = true)
    val isSupervisor: Boolean get() = !isManager
    val isKasie: Boolean get() = !isManager
}

object SupervisorDirectory {
    val predefinedSupervisors = listOf(
        // Akun Kasie Lapangan per Wilayah
        Supervisor(
            id = "SPV-01",
            name = "Budi Santoso",
            nik = "SPV-901",
            passwordPin = "1234",
            defaultShift = "SIANG",
            assignedSector = "Pit North & Mainroad 001A (PG1)",
            phone = "+62 812-3456-7890",
            roleTitle = "Kasie Lapangan",
            role = "KASIE",
            wilayah = "PG1"
        ),
        Supervisor(
            id = "SPV-02",
            name = "Agus Wijaya",
            nik = "SPV-902",
            passwordPin = "1234",
            defaultShift = "MALAM",
            assignedSector = "Disposal West & Cut Site B (PG2)",
            phone = "+62 813-9876-5432",
            roleTitle = "Kasie Lapangan",
            role = "KASIE",
            wilayah = "PG2"
        ),
        Supervisor(
            id = "SPV-03",
            name = "Rudi Hermawan",
            nik = "SPV-903",
            passwordPin = "1234",
            defaultShift = "SIANG",
            assignedSector = "Hauling Road East & Stockpile (PG3)",
            phone = "+62 811-2233-4455",
            roleTitle = "Kasie Lapangan",
            role = "KASIE",
            wilayah = "PG3"
        ),

        // Akun Manager / Mode Monitoring Eksekutif (Semua Wilayah)
        Supervisor(
            id = "MGR-01",
            name = "Farid Hadi Sofian",
            nik = "MGR-101",
            passwordPin = "1234",
            defaultShift = "ALL",
            assignedSector = "Seluruh Wilayah (Monitoring Eksekutif)",
            phone = "+62 812-9988-7766",
            roleTitle = "Manager Operasional",
            role = "MANAGER",
            wilayah = "Semua Wilayah"
        ),
        Supervisor(
            id = "MGR-02",
            name = "Hendra Manager",
            nik = "MGR-102",
            passwordPin = "1234",
            defaultShift = "ALL",
            assignedSector = "Divisi Heavy Equipment Civil (Semua Wilayah)",
            phone = "+62 811-3344-5566",
            roleTitle = "Project Manager",
            role = "MANAGER",
            wilayah = "Semua Wilayah"
        )
    )

    fun authenticate(nikOrName: String, pin: String): Supervisor? {
        val trimmedIdentifier = nikOrName.trim()
        val trimmedPin = pin.trim()
        val matched = predefinedSupervisors.find { spv ->
            (spv.nik.equals(trimmedIdentifier, ignoreCase = true) ||
                    spv.name.equals(trimmedIdentifier, ignoreCase = true)) &&
                    (spv.passwordPin == trimmedPin || trimmedPin == "1234" || trimmedPin.isEmpty())
        }
        if (matched != null) return matched

        // Deteksi dinamis jika username mengandung keyword manager
        if (trimmedIdentifier.contains("manager", ignoreCase = true) ||
            trimmedIdentifier.startsWith("mgr", ignoreCase = true) ||
            trimmedIdentifier.startsWith("gm", ignoreCase = true)
        ) {
            return Supervisor(
                id = "MGR-${System.currentTimeMillis().toString().takeLast(3)}",
                name = trimmedIdentifier,
                nik = trimmedIdentifier.uppercase(),
                passwordPin = trimmedPin,
                defaultShift = "ALL",
                assignedSector = "Seluruh Proyek & Armada (Eksekutif)",
                phone = "+62 812-0000-9999",
                roleTitle = "Manager Operasional",
                role = "MANAGER",
                wilayah = "Semua Wilayah"
            )
        }

        // Default login Kasie
        return Supervisor(
            id = "SPV-${System.currentTimeMillis().toString().takeLast(3)}",
            name = trimmedIdentifier,
            nik = trimmedIdentifier.uppercase(),
            passwordPin = trimmedPin,
            defaultShift = "SIANG",
            assignedSector = "Sektor Operasional Lapangan (PG1)",
            phone = "+62 812-3456-7890",
            roleTitle = "Kasie Lapangan",
            role = "KASIE",
            wilayah = "PG1"
        )
    }
}
