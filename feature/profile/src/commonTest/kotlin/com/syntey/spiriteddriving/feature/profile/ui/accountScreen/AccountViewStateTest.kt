package com.syntey.spiriteddriving.feature.profile.ui.accountScreen

import com.syntey.spiriteddriving.core.platform.binding.SignInProvider
import com.syntey.spiriteddriving.core.resources.*
import dev.icerock.moko.resources.desc.desc
import kotlin.test.Test
import kotlin.test.assertEquals

class AccountViewStateTest {

    @Test
    fun `a Google account names Google and its footer says so`() {
        val state = AccountViewState(provider = SignInProvider.Google)

        assertEquals(MR.strings.account_provider_google.desc(), state.providerName)
        assertEquals(MR.strings.account_footer_google.desc(), state.footer)
    }

    @Test
    fun `an Apple account names Apple and its footer says so`() {
        val state = AccountViewState(provider = SignInProvider.Apple)

        assertEquals(MR.strings.account_provider_apple.desc(), state.providerName)
        assertEquals(MR.strings.account_footer_apple.desc(), state.footer)
    }
}
