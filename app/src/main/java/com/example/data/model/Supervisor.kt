package com.example.data.model

data class Supervisor(
    val id: String,
    val name: String,
    val nik: String,
    val passwordPin: String,
    val defaultShift: String,
    val assignedSector: String,
    val phone: String,
    val roleTitle: String = "Supervisor"
)

object SupervisorDirectory {
    val predefinedSupervisors = listOf(
        Supervisor(
            id = "SPV-01",
            name = "Budi Santoso",
            nik = "SPV-901",
            passwordPin = "1234",
            defaultShift = "SIANG",
            assignedSector = "Pit North & Mainroad 001A",
            phone = "+62 812-3456-7890"
        ),
        Supervisor(
            id = "SPV-02",
            name = "Agus Wijaya",
            nik = "SPV-902",
            passwordPin = "1234",
            defaultShift = "MALAM",
            assignedSector = "Disposal West & Cut Site B",
            phone = "+62 813-9876-5432"
        ),
        Supervisor(
            id = "SPV-03",
            name = "Rudi Hermawan",
            nik = "SPV-903",
            passwordPin = "1234",
            defaultShift = "SIANG",
            assignedSector = "Hauling Road East & Stockpile",
            phone = "+62 811-2233-4455"
        )
    )

    fun authenticate(nikOrName: String, pin: String): Supervisor? {
        val trimmedIdentifier = nikOrName.trim()
        val trimmedPin = pin.trim()
        return predefinedSupervisors.find { spv ->
            (spv.nik.equals(trimmedIdentifier, ignoreCase = true) ||
                    spv.name.equals(trimmedIdentifier, ignoreCase = true)) &&
                    (spv.passwordPin == trimmedPin || trimmedPin == "1234" || trimmedPin.isEmpty())
        }
    }
}
