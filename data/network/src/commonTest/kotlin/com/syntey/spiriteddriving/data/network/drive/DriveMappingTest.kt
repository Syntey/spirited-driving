package com.syntey.spiriteddriving.data.network.drive

import com.syntey.spiriteddriving.core.domain.drive.Drive
import com.syntey.spiriteddriving.data.network.dto.drive.RecordDriveRequestDto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class DriveMappingTest {

    @Test
    fun `route ID and start time map to the request`() {
        val drive = Drive(
            id = "3f2b8c1e-5d4a-4e7b-9a6c-1d2e3f4a5b6c",
            routeId = "9c4e7a2d-1b3f-4c8e-8d5a-6f7b2e9c0a1d",
            startedAt = Instant.parse("2026-11-13T10:15:30.250Z"),
            plannedDuration = 3900.seconds,
            isRecorded = true,
        )

        assertEquals(
            RecordDriveRequestDto(
                routeId = "9c4e7a2d-1b3f-4c8e-8d5a-6f7b2e9c0a1d",
                startedAt = "2026-11-13T10:15:30.250Z",
            ),
            drive.toDto(),
        )
    }
}
