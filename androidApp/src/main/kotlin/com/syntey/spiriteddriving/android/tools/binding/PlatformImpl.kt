package com.syntey.spiriteddriving.android.tools.binding

import android.content.Context
import android.os.Build
import com.syntey.spiriteddriving.android.BuildConfig
import com.syntey.spiriteddriving.platform.binding.Platform
import com.syntey.spiriteddriving.resources.*

class PlatformImpl(applicationContext: Context) : Platform {
    override val appName: String = MR.strings.app_name.getString(applicationContext)
    override val appVersionName: String = BuildConfig.VERSION_NAME
    override val appBuildNumber: String = BuildConfig.VERSION_CODE.toString()
    override val applicationId: String = BuildConfig.APPLICATION_ID
    override val osNameWithVersion: String = "Android ${Build.VERSION.RELEASE}"
    override val deviceModel: String = Build.MODEL
}
