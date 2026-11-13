package com.syntey.spiriteddriving.feature.map.domain

import com.syntey.spiriteddriving.core.domain.coverage.Coverage
import com.syntey.spiriteddriving.core.domain.coverage.CoverageCache
import com.syntey.spiriteddriving.core.domain.coverage.CoverageSource
import com.syntey.spiriteddriving.core.domain.coverage.FakeCoverageSource
import com.syntey.spiriteddriving.core.domain.coverage.VersionedCoverage
import com.syntey.spiriteddriving.core.domain.geo.GeoPoint
import com.syntey.spiriteddriving.core.domain.geo.GeoPolygon
import com.syntey.spiriteddriving.core.domain.settings.CountryCode
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ObserveCoverageUseCaseTest {

    @Test
    fun `with nothing kept it fetches without a version and emits the backend's area`() = runTest {
        val cache = InMemoryCoverageCache()
        val source = FakeCoverageSource(backend)

        assertEquals(listOf(backend.coverage), ObserveCoverageUseCase(source, cache).build(Unit).toList())
        assertEquals(listOf<String?>(null), source.knownVersions)
    }

    @Test
    fun `with nothing kept it keeps the backend's area`() = runTest {
        val cache = InMemoryCoverageCache()

        ObserveCoverageUseCase(FakeCoverageSource(backend), cache).build(Unit).toList()

        assertEquals(backend, cache.get())
    }

    @Test
    fun `a current kept copy is emitted alone`() = runTest {
        val cache = InMemoryCoverageCache(kept)
        val source = FakeCoverageSource(backend.copy(version = kept.version))

        assertEquals(listOf(kept.coverage), ObserveCoverageUseCase(source, cache).build(Unit).toList())
    }

    @Test
    fun `a current kept copy is checked against its version`() = runTest {
        val cache = InMemoryCoverageCache(kept)
        val source = FakeCoverageSource(backend.copy(version = kept.version))

        ObserveCoverageUseCase(source, cache).build(Unit).toList()

        assertEquals(listOf(kept.version), source.knownVersions)
    }

    @Test
    fun `a current kept copy is not saved again`() = runTest {
        val cache = InMemoryCoverageCache(kept)
        val source = FakeCoverageSource(backend.copy(version = kept.version))

        ObserveCoverageUseCase(source, cache).build(Unit).toList()

        assertEquals(0, cache.saves)
    }

    @Test
    fun `an outdated kept copy is emitted before the backend's`() = runTest {
        val cache = InMemoryCoverageCache(kept)

        assertEquals(
            listOf(kept.coverage, backend.coverage),
            ObserveCoverageUseCase(FakeCoverageSource(backend), cache).build(Unit).toList(),
        )
    }

    @Test
    fun `an outdated kept copy is replaced by the backend's`() = runTest {
        val cache = InMemoryCoverageCache(kept)

        ObserveCoverageUseCase(FakeCoverageSource(backend), cache).build(Unit).toList()

        assertEquals(backend, cache.get())
    }

    @Test
    fun `a failed fetch emits the kept copy and then fails with the error`() = runTest {
        val cache = InMemoryCoverageCache(kept)
        val flow = ObserveCoverageUseCase(FailingCoverageSource(), cache).build(Unit)
        val emitted = mutableListOf<Coverage>()

        assertFailsWith<IllegalStateException> { flow.collect { emitted += it } }
        assertEquals(listOf(kept.coverage), emitted)
    }

    private val backend = VersionedCoverage(
        coverage = Coverage(
            area = listOf(
                GeoPolygon(outline = listOf(GeoPoint(50.0, 12.0), GeoPoint(50.0, 13.0), GeoPoint(51.0, 13.0), GeoPoint(50.0, 12.0))),
            ),
            vignetteCountries = listOf(CountryCode.Czechia),
        ),
        version = "\"backend\"",
    )

    private val kept = VersionedCoverage(
        coverage = Coverage(
            area = listOf(
                GeoPolygon(outline = listOf(GeoPoint(49.0, 12.0), GeoPoint(49.0, 13.0), GeoPoint(50.0, 13.0), GeoPoint(49.0, 12.0))),
            ),
            vignetteCountries = listOf(CountryCode.Czechia),
        ),
        version = "\"kept\"",
    )

    private class InMemoryCoverageCache(private var stored: VersionedCoverage? = null) : CoverageCache {
        var saves = 0
            private set

        override suspend fun get(): VersionedCoverage? = stored

        override suspend fun save(coverage: VersionedCoverage) {
            saves++
            stored = coverage
        }
    }

    private class FailingCoverageSource : CoverageSource {
        override suspend fun fetch(knownVersion: String?): VersionedCoverage? = error("Backend unreachable")
    }
}
