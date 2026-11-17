package com.syntey.spiriteddriving.feature.map.domain

import com.syntey.spiriteddriving.core.domain.drive.DriveQuestion

/** How the app asks about [question] now. */
internal data class DrivePrompt(val question: DriveQuestion, val kind: Kind) {
    enum class Kind {
        /** The rating step opens in the sheet. */
        RatingStep,

        /** The reminder bubble shows on the map. */
        Bubble,
    }
}
