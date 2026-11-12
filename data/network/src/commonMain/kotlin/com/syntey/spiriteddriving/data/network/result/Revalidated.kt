package com.syntey.spiriteddriving.data.network.result

/**
 * The answer to a request that sent the version the app has (`If-None-Match`): new content, or word that the app's copy
 * is current (`304 Not Modified`).
 */
sealed interface Revalidated<out T> {

    /** @property etag The new content's version, from the `ETag` header. */
    data class Modified<T>(val data: T, val etag: String?) : Revalidated<T>

    data object NotModified : Revalidated<Nothing>
}
