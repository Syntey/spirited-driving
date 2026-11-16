package com.syntey.spiriteddriving.core.platform.binding

/** The in-app browser: a Custom Tab on Android, a Safari view controller on iOS. */
interface PlatformWebBrowser {

    /** Shows the web page at [url] over the app. Does nothing when the phone has no browser to show it with. */
    fun open(url: String)
}
