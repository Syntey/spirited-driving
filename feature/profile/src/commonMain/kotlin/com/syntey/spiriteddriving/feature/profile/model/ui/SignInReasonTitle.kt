package com.syntey.spiriteddriving.feature.profile.model.ui

import com.syntey.spiriteddriving.core.resources.*
import com.syntey.spiriteddriving.feature.profile.model.SignInReason
import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.desc.desc

internal fun SignInReason.title(): StringDesc = when (this) {
    SignInReason.SaveRoute -> MR.strings.sign_in_sheet_title_save_route.desc()
    SignInReason.SavedRoutes -> MR.strings.sign_in_sheet_title_saved_routes.desc()
    SignInReason.TripHistory -> MR.strings.sign_in_sheet_title_trip_history.desc()
}
