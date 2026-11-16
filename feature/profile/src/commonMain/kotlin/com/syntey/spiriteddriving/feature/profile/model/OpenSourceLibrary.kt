package com.syntey.spiriteddriving.feature.profile.model

import kotlinx.collections.immutable.ImmutableList

/** A library the app ships, with the licences it's available under. */
data class OpenSourceLibrary(val id: String, val name: String, val licences: ImmutableList<Licence>)

/** [text] is the full licence text, null when the build didn't include it; [url] points to it. */
data class Licence(val name: String, val text: String?, val url: String?)
