package com.syntey.spiriteddriving.android.tools.binding

import android.content.Context
import android.os.Build
import android.os.LocaleList
import com.syntey.spiriteddriving.android.BuildConfig
import com.syntey.spiriteddriving.core.platform.binding.Platform
import com.syntey.spiriteddriving.core.resources.*
import java.util.IllformedLocaleException
import java.util.Locale

class PlatformImpl(applicationContext: Context) : Platform {
    override val appName: String = MR.strings.app_name.getString(applicationContext)
    override val appVersionName: String = BuildConfig.VERSION_NAME
    override val appBuildNumber: String = BuildConfig.VERSION_CODE.toString()
    override val applicationId: String = BuildConfig.APPLICATION_ID
    override val storeReviewUrl: String = "https://play.google.com/store/apps/details?id=$applicationId"
    override val osNameWithVersion: String = "Android ${Build.VERSION.RELEASE}"
    override val deviceModel: String = Build.MODEL
    override val preferredLanguages: List<String>
        get() = LocaleList.getDefault().toLanguageTags().split(',').filter { it.isNotBlank() }
    override val regionCode: String?
        get() = Locale.getDefault().country.takeIf { it.isNotEmpty() }

    override fun countryName(code: String): String? {
        val name = try {
            Locale.Builder().setRegion(code).build().getDisplayCountry(Locale.ENGLISH)
        } catch (_: IllformedLocaleException) {
            null
        }
        return name?.takeIf { it.isNotBlank() && it != code }
    }
}
