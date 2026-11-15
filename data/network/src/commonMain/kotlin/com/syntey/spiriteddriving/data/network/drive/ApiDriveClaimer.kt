package com.syntey.spiriteddriving.data.network.drive

import com.syntey.spiriteddriving.core.domain.drive.DriveClaimer
import com.syntey.spiriteddriving.data.network.api.SpiritedDrivingApi
import com.syntey.spiriteddriving.data.network.dto.drive.ClaimDrivesRequestDto
import com.syntey.spiriteddriving.data.network.result.getOrThrowDomainError
import org.koin.core.annotation.Factory

@Factory
internal class ApiDriveClaimer(private val api: SpiritedDrivingApi) : DriveClaimer {

    override suspend fun claim(driveIds: List<String>) {
        api.claimDrives(ClaimDrivesRequestDto(driveIds)).getOrThrowDomainError()
    }
}
