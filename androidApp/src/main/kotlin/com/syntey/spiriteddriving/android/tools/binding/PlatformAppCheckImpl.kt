package com.syntey.spiriteddriving.android.tools.binding

import android.content.Context
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.appCheck
import com.syntey.spiriteddriving.core.platform.binding.PlatformAppCheck
import com.syntey.spiriteddriving.core.platform.binding.TokenListener

class PlatformAppCheckImpl(private val context: Context) : PlatformAppCheck {

    override fun token(listener: TokenListener) {
        // Without google-services.json there's no App Check, so the backend call goes without the header.
        if (FirebaseApp.getApps(context).isEmpty()) {
            listener.onToken(null)
            return
        }
        Firebase.appCheck.getAppCheckToken(false)
            .addOnCompleteListener { listener.onToken(if (it.isSuccessful) it.result.token else null) }
    }
}
