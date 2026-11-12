package com.syntey.spiriteddriving.feature.map.domain

import app.futured.arkitekt.crusecases.FlowUseCase
import com.syntey.spiriteddriving.core.domain.coverage.Coverage
import com.syntey.spiriteddriving.core.domain.coverage.CoverageCache
import com.syntey.spiriteddriving.core.domain.coverage.CoverageSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.koin.core.annotation.Factory

/**
 * Emits the covered area: the copy kept on the device at once, if there is one, then the backend's when it changed,
 * which is kept for next time. A failed fetch ends the flow with its error, after the kept copy.
 */
@Factory
internal class ObserveCoverageUseCase(private val coverageSource: CoverageSource, private val coverageCache: CoverageCache) :
    FlowUseCase<Unit, Coverage> {

    override fun build(args: Unit): Flow<Coverage> = flow {
        val kept = coverageCache.get()
        kept?.let { emit(it.coverage) }
        val fetched = coverageSource.fetch(knownVersion = kept?.version) ?: return@flow
        coverageCache.save(fetched)
        emit(fetched.coverage)
    }
}
