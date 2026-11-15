package com.syntey.spiriteddriving.feature.profile.model.ui

import com.syntey.spiriteddriving.core.platform.binding.AuthUser
import com.syntey.spiriteddriving.core.platform.binding.SignInProvider
import com.syntey.spiriteddriving.core.resources.*
import dev.icerock.moko.resources.desc.desc
import kotlin.test.Test
import kotlin.test.assertEquals

class AccountRowTest {

    @Test
    fun `a Google user shows the email`() {
        val row = user(email = "rider@example.com", provider = SignInProvider.Google).toAccountRow()

        assertEquals(AccountRow(supporting = "rider@example.com".desc()), row)
    }

    @Test
    fun `an Apple user with a real email shows the email`() {
        val row = user(email = "rider@icloud.com", provider = SignInProvider.Apple).toAccountRow()

        assertEquals(AccountRow(supporting = "rider@icloud.com".desc()), row)
    }

    @Test
    fun `a Hide My Email address shows signed in with Apple`() {
        val row = user(email = "x7k2@privaterelay.appleid.com", provider = SignInProvider.Apple).toAccountRow()

        assertEquals(AccountRow(supporting = MR.strings.profile_account_signed_in_with_apple.desc()), row)
    }

    @Test
    fun `the relay domain matches in any letter case`() {
        val row = user(email = "x7k2@PrivateRelay.AppleID.com", provider = SignInProvider.Apple).toAccountRow()

        assertEquals(AccountRow(supporting = MR.strings.profile_account_signed_in_with_apple.desc()), row)
    }

    @Test
    fun `an Apple user without an email shows signed in with Apple`() {
        val row = user(email = null, provider = SignInProvider.Apple).toAccountRow()

        assertEquals(AccountRow(supporting = MR.strings.profile_account_signed_in_with_apple.desc()), row)
    }

    @Test
    fun `a Google user without an email shows signed in with Google`() {
        val row = user(email = null, provider = SignInProvider.Google).toAccountRow()

        assertEquals(AccountRow(supporting = MR.strings.profile_account_signed_in_with_google.desc()), row)
    }

    private fun user(email: String?, provider: SignInProvider) = AuthUser(id = "uid-1", name = null, email = email, provider = provider)
}
