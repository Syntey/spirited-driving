package com.syntey.spiriteddriving.core.resources.datetime

import com.syntey.spiriteddriving.core.resources.*
import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.desc.desc
import kotlin.time.Instant
import kotlinx.datetime.toNSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale
import platform.Foundation.currentLocale
import platform.Foundation.languageCode

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

    private val formatter by lazy { NSDateFormatter() }

    override fun localized(): String = formatter.run {
        locale = formatLocale()
        setLocalizedDateFormatFromTemplate(pattern)
        stringFromDate(instant.toNSDate())
    }

    private fun formatLocale(): NSLocale {
        val appLanguage = MR.strings.app_language.desc().localized()
        val current = NSLocale.currentLocale
        return if (current.languageCode == appLanguage) current else NSLocale(localeIdentifier = appLanguage)
    }
}
