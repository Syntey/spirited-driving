package com.syntey.spiriteddriving.feature.map.domain

import com.syntey.spiriteddriving.core.domain.route.FakeRoutePlanner
import com.syntey.spiriteddriving.core.domain.route.Route
import com.syntey.spiriteddriving.core.domain.savedroute.FakeSavedRoutesCache
import com.syntey.spiriteddriving.core.domain.savedroute.FakeSavedRoutesSource
import com.syntey.spiriteddriving.core.domain.savedroute.SavedRoute
import com.syntey.spiriteddriving.core.domain.savedroute.SavedRoutesSource
import com.syntey.spiriteddriving.core.domain.savedroute.toSavedRoute
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant

class SaveRouteUseCaseTest {

    private val route: Route = FakeRoutePlanner.Recorded.funRoutes.first()

    @Test
    fun `a route is saved named after its destination`() = runTest {
        val source = FakeSavedRoutesSource()
        val result = saveRoute(route, source = source, cache = FakeSavedRoutesCache(routes = emptyList()))

        val saved = assertIs<SaveRouteResult.Saved>(result).savedRoute
        assertEquals(route.destination.name, saved.name)
        assertEquals(route.id, saved.routeId)
    }

    @Test
    fun `a destination without a name is named after its area`() = runTest {
        val unnamed = route.copy(destination = route.destination.copy(name = null, area = "Beroun"))

        val saved = assertIs<SaveRouteResult.Saved>(
            saveRoute(unnamed, source = FakeSavedRoutesSource(), cache = FakeSavedRoutesCache(routes = emptyList())),
        ).savedRoute

        assertEquals("Beroun", saved.name)
    }

    @Test
    fun `the default name is the destination's name trimmed and cut to 100 characters`() = runTest {
        val long = route.copy(destination = route.destination.copy(name = "  ${"a".repeat(120)}  "))

        val saved = assertIs<SaveRouteResult.Saved>(
            saveRoute(long, source = FakeSavedRoutesSource(), cache = FakeSavedRoutesCache(routes = emptyList())),
        ).savedRoute

        assertEquals("a".repeat(100), saved.name)
    }

    @Test
    fun `a route already saved is returned without saving it again`() = runTest {
        val existing = route.toSavedRoute(id = "saved-9", name = "Old name", savedAt = SAVED_AT)
        val source = FakeSavedRoutesSource(routes = listOf(existing))

        val result = saveRoute(route, source = source, cache = FakeSavedRoutesCache(routes = listOf(existing)))

        assertEquals(existing, assertIs<SaveRouteResult.Saved>(result).savedRoute)
        assertEquals(listOf(existing), source.routes)
    }

    @Test
    fun `a taken name asks for a free one instead of saving`() = runTest {
        val taken = route.toSavedRoute(id = "saved-1", name = route.destination.name.orEmpty(), savedAt = SAVED_AT)
        val other = FakeRoutePlanner.Recorded.fastest
        val source = FakeSavedRoutesSource(routes = listOf(taken))

        val result = assertIs<SaveRouteResult.NameTaken>(
            saveRoute(other.copy(destination = route.destination), source = source, cache = FakeSavedRoutesCache(routes = listOf(taken))),
        )

        assertEquals(route.destination.name, result.takenName)
        assertEquals(
            "${route.destination.name} from ${other.start.shortArea}",
            result.suggestion,
        )
        assertEquals(listOf(taken.name), result.takenNames)
        assertEquals(listOf(taken), source.routes)
    }

    @Test
    fun `a given name is saved trimmed`() = runTest {
        val source = FakeSavedRoutesSource()

        val saved = assertIs<SaveRouteResult.Saved>(
            saveRoute(route, name = "  Sunday loop  ", source = source, cache = FakeSavedRoutesCache(routes = emptyList())),
        ).savedRoute

        assertEquals("Sunday loop", saved.name)
    }

    @Test
    fun `a given name that is taken asks for a free one instead of saving`() = runTest {
        val taken = route.toSavedRoute(id = "saved-1", name = "Sunday loop", savedAt = SAVED_AT)
        val other = FakeRoutePlanner.Recorded.fastest
        val source = FakeSavedRoutesSource(routes = listOf(taken))

        val result = assertIs<SaveRouteResult.NameTaken>(
            saveRoute(other, name = "Sunday loop", source = source, cache = FakeSavedRoutesCache(routes = listOf(taken))),
        )

        assertEquals("Sunday loop", result.takenName)
        assertEquals(listOf(taken), source.routes)
    }

    @Test
    fun `the saved routes are read from the backend when nothing is kept`() = runTest {
        val existing = route.toSavedRoute(id = "saved-9", name = "Old name", savedAt = SAVED_AT)
        val cache = FakeSavedRoutesCache(routes = null)

        val result = saveRoute(route, source = FakeSavedRoutesSource(routes = listOf(existing)), cache = cache)

        assertEquals(existing, assertIs<SaveRouteResult.Saved>(result).savedRoute)
        assertEquals(listOf(existing), cache.get())
    }

    @Test
    fun `the saved route is kept with its full route`() = runTest {
        val cache = FakeSavedRoutesCache(routes = emptyList())

        val saved = assertIs<SaveRouteResult.Saved>(saveRoute(route, source = FakeSavedRoutesSource(), cache = cache)).savedRoute

        assertEquals(route, cache.getRoute(saved.id))
        assertEquals(listOf(saved), cache.get())
    }

    private suspend fun saveRoute(
        route: Route,
        name: String? = null,
        source: SavedRoutesSource,
        cache: FakeSavedRoutesCache,
    ): SaveRouteResult = SaveRouteUseCase(source, cache).build(SaveRouteUseCase.Args(route = route, name = name))

    private companion object {
        val SAVED_AT: Instant = Instant.parse("2026-11-08T09:30:00Z")
    }
}
