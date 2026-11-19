package com.syntey.spiriteddriving.feature.profile.model.ui

import com.syntey.spiriteddriving.core.platform.binding.SignInResult
import com.syntey.spiriteddriving.core.resources.*
import dev.icerock.moko.resources.desc.desc
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class SignInStateTest {

    @Test
    fun `a signed in result ends the sign-in without an error`() {
        val state = SignInState.after(SignInResult.SignedIn)

        assertFalse(state.isSigningIn)
        assertEquals(null, state.error)
    }

    @Test
    fun `a cancelled sign-in ends without an error`() {
        val state = SignInState.after(SignInResult.Cancelled)

        assertFalse(state.isSigningIn)
        assertEquals(null, state.error)
    }

    @Test
    fun `a failed sign-in shows the sign-in failed error`() {
        val state = SignInState.after(SignInResult.Failed("network down"))

        assertFalse(state.isSigningIn)
        assertEquals(MR.strings.sign_in_failed.desc(), state.error)
    }
}
