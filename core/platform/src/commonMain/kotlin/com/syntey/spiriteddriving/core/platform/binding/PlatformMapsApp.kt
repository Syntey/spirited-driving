package com.syntey.spiriteddriving.core.platform.binding

/** The phone's maps app, which guides drives turn by turn: Google Maps on Android, Apple Maps on iOS. */
interface PlatformMapsApp {

    /** Which maps app drives are handed to on this platform. */
    val kind: MapsAppKind

    /**
     * Opens [link], a link for [kind], in the maps app and reports to [listener], once, whether it opened. Reports
     * [MapsAppResult.Missing] when the app is deleted or turned off. Can be called from any thread; [listener] may be
     * called on any thread.
     */
    fun open(link: String, listener: MapsAppListener)

    /** Opens the maps app's page in the app store, where the user can get it or turn it on. Can be called from any thread. */
    fun openStore()
}

interface MapsAppListener {
    fun onResult(result: MapsAppResult)
}

enum class MapsAppKind { GoogleMaps, AppleMaps }

enum class MapsAppResult {
    Opened,

    /** Deleted, or turned off on Android. */
    Missing,
}
