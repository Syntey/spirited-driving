package com.syntey.spiriteddriving.core.resources.datetime

import android.content.Context
import android.icu.text.DateFormat
import com.syntey.spiriteddriving.core.resources.*
import dev.icerock.moko.resources.desc.StringDesc
import java.util.Date
import java.util.Locale
import kotlin.time.Instant
import kotlin.time.toJavaInstant

/**
 * A [StringDesc] implementation which translates provided [Instant] into localized date-time string.
 *
 * Formats provided [Instant] using pattern in [UTS#35 Unicode Locale Data Markup Language (LDML)](http://www.unicode.org/reports/tr35/)
 * localized in the app's language, using the phone's region when the phone is in that language.
 *
 * For example, template "MMMMd" produces:
 * - "December 31" for "en_US" locale
 * - "31 December" for "en_GB" locale
 *
 * See [Android docs](https://developer.android.com/reference/android/icu/text/SimpleDateFormat)
 * See [iOS docs](https://developer.apple.com/documentation/foundation/dateformatter)
 */
actual class DateTimeStringDesc actual constructor(private val instant: Instant, private val pattern: String) : StringDesc {

    override fun toString(context: Context): String =
        DateFormat
            .getInstanceForSkeleton(pattern, formatLocale(context))
            .format(Date.from(instant.toJavaInstant()))

    private fun formatLocale(context: Context): Locale {
        val appLanguage = context.getString(MR.strings.app_language.resourceId)
        val device = context.resources.configuration.locales[0]
        return if (device.language == appLanguage) device else Locale.forLanguageTag(appLanguage)
    }
}
