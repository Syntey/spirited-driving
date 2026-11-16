package com.syntey.spiriteddriving.feature.profile.domain

import com.syntey.spiriteddriving.core.domain.coverage.CoverageCache
import com.syntey.spiriteddriving.core.domain.coverage.FakeCoverageSource
import com.syntey.spiriteddriving.core.domain.coverage.VersionedCoverage
import com.syntey.spiriteddriving.core.domain.settings.CountryCode
import com.syntey.spiriteddriving.core.domain.settings.FakeVignetteSettings
import com.syntey.spiriteddriving.core.platform.binding.Platform
import com.syntey.spiriteddriving.feature.profile.model.VignetteCountry
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class ObserveVignetteCountriesUseCaseTest {

    private val slovakia = CountryCode("SK")
    private val austria = CountryCode("AT")
    private val names = NamingPlatform(mapOf("CZ" to "Czechia", "SK" to "Slovakia", "AT" to "Austria"))

    @Test
    fun `the covered area's countries come in its order with their names and answers`() = runTest {
        val cache = InMemoryCoverageCache(coveredWith(listOf(CountryCode.Czechia, slovakia, austria)))
        val settings = FakeVignetteSettings(mapOf(CountryCode.Czechia to true, slovakia to false))

        val countries = ObserveVignetteCountriesUseCase(cache, settings, names).build(Unit).first()

        assertEquals(
            listOf(
                VignetteCountry(code = CountryCode.Czechia, name = "Czechia", hasVignette = true),
                VignetteCountry(code = slovakia, name = "Slovakia", hasVignette = false),
                VignetteCountry(code = austria, name = "Austria", hasVignette = false),
            ),
            countries,
        )
    }

    @Test
    fun `a country the phone doesn't name shows its code`() = runTest {
        val cache = InMemoryCoverageCache(coveredWith(listOf(CountryCode("XK"))))

        val countries = ObserveVignetteCountriesUseCase(cache, FakeVignetteSettings(), NamingPlatform(emptyMap())).build(Unit).first()

        assertEquals(listOf(VignetteCountry(code = CountryCode("XK"), name = "XK", hasVignette = false)), countries)
    }

    @Test
    fun `with no covered area kept only Czechia is asked about`() = runTest {
        val countries = ObserveVignetteCountriesUseCase(InMemoryCoverageCache(null), FakeVignetteSettings(), names).build(Unit).first()

        assertEquals(listOf(VignetteCountry(code = CountryCode.Czechia, name = "Czechia", hasVignette = false)), countries)
    }

    @Test
    fun `a covered area without vignette countries asks about Czechia only`() = runTest {
        val cache = InMemoryCoverageCache(coveredWith())

        val countries = ObserveVignetteCountriesUseCase(cache, FakeVignetteSettings(), names).build(Unit).first()

        assertEquals(listOf(CountryCode.Czechia), countries.map { it.code })
    }

    @Test
    fun `a changed vignette answer emits the countries again`() = runTest {
        val cache = InMemoryCoverageCache(coveredWith(listOf(CountryCode.Czechia, slovakia)))
        val settings = FakeVignetteSettings()
        val slovakiaAnswers = mutableListOf<Boolean>()
        val collecting = launch(UnconfinedTestDispatcher(testScheduler)) {
            ObserveVignetteCountriesUseCase(cache, settings, names).build(Unit).collect { countries ->
                slovakiaAnswers += countries.single { it.code == slovakia }.hasVignette
            }
        }

        settings.setHasVignette(slovakia, true)
        runCurrent()
        collecting.cancel()

        assertEquals(listOf(false, true), slovakiaAnswers)
    }

    @Test
    fun `a covered area that arrives later replaces Czechia alone`() = runTest {
        val cache = InMemoryCoverageCache(null)
        val codes = mutableListOf<List<CountryCode>>()
        val collecting = launch(UnconfinedTestDispatcher(testScheduler)) {
            ObserveVignetteCountriesUseCase(cache, FakeVignetteSettings(), names).build(Unit).collect { countries ->
                codes += countries.map { it.code }
            }
        }

        cache.save(coveredWith(listOf(CountryCode.Czechia, slovakia)))
        runCurrent()
        collecting.cancel()

        assertEquals(listOf(listOf(CountryCode.Czechia), listOf(CountryCode.Czechia, slovakia)), codes)
    }

    private fun coveredWith(countries: List<CountryCode> = emptyList()) = VersionedCoverage(
        coverage = FakeCoverageSource.Czechia.copy(vignetteCountries = countries),
        version = "\"test\"",
    )
}

private class InMemoryCoverageCache(initial: VersionedCoverage?) : CoverageCache {
    private val kept = MutableStateFlow(initial)

    override suspend fun get(): VersionedCoverage? = kept.value

    override fun observeVignetteCountries(): Flow<List<CountryCode>?> =
        kept.map { it?.coverage?.vignetteCountries }

    override suspend fun save(coverage: VersionedCoverage) {
        kept.value = coverage
    }
}

private class NamingPlatform(private val names: Map<String, String>) : Platform {
    override val appName = "App"
    override val appVersionName = "1.0"
    override val appBuildNumber = "1"
    override val applicationId = "app.id"
    override val storeReviewUrl = "https://store.test/app"
    override val osNameWithVersion = "OS 1"
    override val deviceModel = "Model"
    override val preferredLanguages = listOf("en")
    override val regionCode: String? = null

    override fun countryName(code: String): String? = names[code]

    override fun openSourceLibrariesJson(): String = ""
}
