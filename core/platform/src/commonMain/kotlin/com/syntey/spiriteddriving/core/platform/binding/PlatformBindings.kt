package com.syntey.spiriteddriving.core.platform.binding

import org.koin.core.annotation.Provided

/**
 * This interface provides native binding interfaces which are used to interface between KMP and all its targets.
 * Each interface must be implemented natively on each platform.
 * The app passes it to Koin at start, so it is `@Provided`.
 */
@Provided
interface PlatformBindings {

    /**
     * Provides information about platform and application.
     */
    fun platform(): Platform

    /**
     * Provides a native binding to Firebase Crashlytics SDK.
     */
    fun firebaseCrashlytics(): PlatformFirebaseCrashlytics

    /**
     * Provides the permission to use the location, see [LocationPermission].
     */
    fun locationPermission(): PlatformLocationPermission

    /** Provides the user's location while the app is in use, see [UserLocation]. */
    fun location(): PlatformLocation

    /** Provides the phone's drive feedback notification, see [DriveReminders]. */
    fun driveReminders(): PlatformDriveReminders

    /** Provides the phone's maps app that drives are handed to, see [MapsApp]. */
    fun mapsApp(): PlatformMapsApp

    /** Provides the in-app browser, see [PlatformWebBrowser]. */
    fun webBrowser(): PlatformWebBrowser

    /** Provides the Firebase sign-in, see [PlatformAuth]. */
    fun auth(): PlatformAuth

    /** Provides the Firebase App Check token, see [PlatformAppCheck]. */
    fun appCheck(): PlatformAppCheck
}
