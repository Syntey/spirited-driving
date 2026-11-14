package com.syntey.spiriteddriving.data.network.drive

import com.syntey.spiriteddriving.core.domain.drive.Drive
import com.syntey.spiriteddriving.data.network.dto.drive.RecordDriveRequestDto

internal fun Drive.toDto(): RecordDriveRequestDto = RecordDriveRequestDto(
    routeId = routeId,
    startedAt = startedAt.toString(),
)
