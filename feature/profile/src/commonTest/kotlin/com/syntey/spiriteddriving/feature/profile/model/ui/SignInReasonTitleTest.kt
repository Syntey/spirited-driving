package com.syntey.spiriteddriving.feature.profile.model.ui

import com.syntey.spiriteddriving.core.resources.*
import com.syntey.spiriteddriving.feature.profile.model.SignInReason
import dev.icerock.moko.resources.desc.desc
import kotlin.test.Test
import kotlin.test.assertEquals

class SignInReasonTitleTest {

    @Test
    fun `saving a route asks to sign in to save this route`() {
        assertEquals(MR.strings.sign_in_sheet_title_save_route.desc(), SignInReason.SaveRoute.title())
    }

    @Test
    fun `the saved routes row asks to sign in to keep routes for later`() {
        assertEquals(MR.strings.sign_in_sheet_title_saved_routes.desc(), SignInReason.SavedRoutes.title())
    }
}
