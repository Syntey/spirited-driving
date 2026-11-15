package com.syntey.spiriteddriving.feature.profile.model.ui

import com.syntey.spiriteddriving.core.platform.binding.AuthUser
import com.syntey.spiriteddriving.core.platform.binding.SignInProvider
import com.syntey.spiriteddriving.core.resources.*
import com.syntey.spiriteddriving.feature.profile.tools.Constants
import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.desc.desc

/** The Account row in Profile: its second line, the email or the provider. */
data class AccountRow(val supporting: StringDesc)

/** The row for a signed-in user. A Hide My Email address, or none, reads as the provider instead. */
internal fun AuthUser.toAccountRow(): AccountRow {
    val address = email
    return if (address == null || address.endsWith("@${Constants.Account.APPLE_PRIVATE_RELAY_DOMAIN}", ignoreCase = true)) {
        AccountRow(supporting = provider.signedInWithResource().desc())
    } else {
        AccountRow(supporting = address.desc())
    }
}

private fun SignInProvider.signedInWithResource() = when (this) {
    SignInProvider.Google -> MR.strings.profile_account_signed_in_with_google
    SignInProvider.Apple -> MR.strings.profile_account_signed_in_with_apple
}
