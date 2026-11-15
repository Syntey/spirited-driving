package com.syntey.spiriteddriving.feature.profile.ui.accountScreen

import com.syntey.spiriteddriving.core.platform.binding.SignInProvider
import com.syntey.spiriteddriving.core.resources.*
import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.desc.desc

data class AccountViewState(
    val name: String? = null,
    val email: String? = null,
    val provider: SignInProvider = SignInProvider.Google,
    val isDeleteDialogShown: Boolean = false,
    val isDeleting: Boolean = false,
) {
    val providerName: StringDesc
        get() = when (provider) {
            SignInProvider.Google -> MR.strings.account_provider_google.desc()
            SignInProvider.Apple -> MR.strings.account_provider_apple.desc()
        }

    val footer: StringDesc
        get() = when (provider) {
            SignInProvider.Google -> MR.strings.account_footer_google.desc()
            SignInProvider.Apple -> MR.strings.account_footer_apple.desc()
        }
}
