package com.syntey.spiriteddriving.core.platform.binding

/** The Firebase App Check SDK, which proves to the backend that requests come from this app. */
interface PlatformAppCheck {

    /**
     * Reports a Firebase App Check token for a backend call to [listener], cached and refreshed by the SDK. Reports null
     * when no token can be had. Can be called from any thread; [listener] may be called on any thread.
     */
    fun token(listener: TokenListener)
}
