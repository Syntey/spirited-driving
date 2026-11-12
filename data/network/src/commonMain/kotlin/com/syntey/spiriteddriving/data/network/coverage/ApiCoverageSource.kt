package com.syntey.spiriteddriving.data.network.coverage

import com.syntey.spiriteddriving.core.domain.coverage.CoverageSource
import com.syntey.spiriteddriving.core.domain.coverage.VersionedCoverage
import com.syntey.spiriteddriving.data.network.api.SpiritedDrivingApi
import com.syntey.spiriteddriving.data.network.result.Revalidated
import com.syntey.spiriteddriving.data.network.result.getOrThrow
import org.koin.core.annotation.Factory

@Factory
internal class ApiCoverageSource(private val api: SpiritedDrivingApi) : CoverageSource {

    override suspend fun fetch(knownVersion: String?): VersionedCoverage? =
        when (val revalidated = api.getCoverage(ifNoneMatch = knownVersion).getOrThrow()) {
            is Revalidated.Modified -> VersionedCoverage(revalidated.data.toDomain(), revalidated.etag)
            Revalidated.NotModified -> null
        }
}
