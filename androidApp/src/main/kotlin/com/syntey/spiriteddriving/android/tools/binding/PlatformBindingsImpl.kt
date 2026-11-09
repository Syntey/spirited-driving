package com.syntey.spiriteddriving.android.tools.binding

import android.content.Context
import com.syntey.spiriteddriving.core.platform.binding.Platform
import com.syntey.spiriteddriving.core.platform.binding.PlatformBindings
import com.syntey.spiriteddriving.core.platform.binding.PlatformFirebaseCrashlytics

class PlatformBindingsImpl(private val applicationContext: Context) : PlatformBindings {
    override fun platform(): Platform = PlatformImpl(applicationContext)
    override fun firebaseCrashlytics(): PlatformFirebaseCrashlytics = PlatformFirebaseCrashlyticsImpl()
}
