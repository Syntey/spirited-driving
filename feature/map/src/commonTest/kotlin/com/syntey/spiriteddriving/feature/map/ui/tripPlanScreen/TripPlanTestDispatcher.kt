package com.syntey.spiriteddriving.feature.map.ui.tripPlanScreen

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

/**
 * The dispatcher a trip plan test runs on, as Main. A test class calls [install] in its @BeforeTest and [uninstall] in its
 * @AfterTest, and runs its test on [dispatcher].
 */
internal class TripPlanTestDispatcher {

    val dispatcher = StandardTestDispatcher()

    fun install() {
        Dispatchers.setMain(dispatcher)
    }

    fun uninstall() {
        Dispatchers.resetMain()
    }
}
