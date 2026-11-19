package com.syntey.spiriteddriving.core.domain.savedroute

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SavedRouteNamesTest {

    @Test
    fun `a name differing only in case and spaces is taken`() {
        assertTrue(listOf(route("1", "Karlštejn Castle")).hasName(" karlštejn castle "))
    }

    @Test
    fun `a different name is not taken`() {
        assertFalse(listOf("Karlštejn Castle").containsName("Karlštejn Castle Tower"))
    }

    @Test
    fun `containsName ignores case and surrounding spaces`() {
        assertTrue(listOf("Karlštejn Castle").containsName("  karlštejn castle "))
    }

    @Test
    fun `a blank name is not savable`() {
        assertFalse(isSavableName("   "))
    }

    @Test
    fun `a name of the most characters is savable`() {
        assertTrue(isSavableName("a".repeat(40)))
    }

    @Test
    fun `a name one character over the most is not savable`() {
        assertFalse(isSavableName("a".repeat(41)))
    }

    @Test
    fun `a route's own name isn't taken for it`() {
        assertFalse(listOf(route("1", "Karlštejn Castle")).hasName("Karlštejn Castle", exceptId = SavedRouteId("1")))
    }

    @Test
    fun `the free name adds the start's area`() {
        assertEquals(
            "Karlštejn Castle from Smíchov",
            listOf(route("1", "Karlštejn Castle")).freeNameFor("Karlštejn Castle", startArea = "Smíchov"),
        )
    }

    @Test
    fun `the free name adds a number when the name with the area is taken too`() {
        val routes = listOf(route("1", "Karlštejn Castle"), route("2", "Karlštejn Castle from Smíchov"))

        assertEquals("Karlštejn Castle from Smíchov 2", routes.freeNameFor("Karlštejn Castle", startArea = "Smíchov"))
    }

    @Test
    fun `the free name without a start area numbers the name`() {
        assertEquals("Karlštejn Castle 2", listOf(route("1", "Karlštejn Castle")).freeNameFor("Karlštejn Castle", startArea = null))
    }

    @Test
    fun `a long free name is cut to the most a name may have`() {
        val name = "Hrad Karlštejn and the Berounka valley road"

        val free = listOf(route("1", name)).freeNameFor(name, startArea = "Smíchov")

        assertEquals("Hrad Karlštejn and the Berounka valley r", free)
    }

    @Test
    fun `a long numbered free name keeps its number within the most a name may have`() {
        val name = "Hrad Karlštejn and the Berounka valley road"
        val routes = listOf(route("1", name), route("2", "Hrad Karlštejn and the Berounka valley r"))

        assertEquals("Hrad Karlštejn and the Berounka valley 2", routes.freeNameFor(name, startArea = "Smíchov"))
    }

    private fun route(id: String, name: String) = FakeSavedRoutesSource.Recorded.copy(id = SavedRouteId(id), name = name)
}
