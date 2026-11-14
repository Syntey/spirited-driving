package com.syntey.spiriteddriving.core.domain.geo

/**
 * Decodes a route line in Google's encoded polyline format with precision 5, as the backend sends it.
 */
fun decodePolyline(encoded: String): List<GeoPoint> {
    val points = mutableListOf<GeoPoint>()
    var index = 0
    var latitude = 0
    var longitude = 0
    while (index < encoded.length) {
        val latitudeDelta = encoded.readSignedValue(index)
        index = latitudeDelta.nextIndex
        val longitudeDelta = encoded.readSignedValue(index)
        index = longitudeDelta.nextIndex
        latitude += latitudeDelta.value
        longitude += longitudeDelta.value
        points += GeoPoint(latitude = latitude / PRECISION, longitude = longitude / PRECISION)
    }
    return points
}

private class SignedValue(val value: Int, val nextIndex: Int)

// A value is 5-bit chunks, least significant first, each with a continuation bit; the sign is the lowest bit
private fun String.readSignedValue(start: Int): SignedValue {
    var index = start
    var result = 0
    var shift = 0
    var chunk: Int
    do {
        chunk = this[index++].code - ASCII_OFFSET
        result = result or ((chunk and CHUNK_MASK) shl shift)
        shift += CHUNK_BITS
    } while (chunk >= CONTINUATION_BIT)
    val isNegative = (result and 1) != 0
    val value = if (isNegative) (result shr 1).inv() else result shr 1
    return SignedValue(value = value, nextIndex = index)
}

private const val PRECISION = 1e5
private const val ASCII_OFFSET = 63
private const val CHUNK_MASK = 0x1f
private const val CHUNK_BITS = 5
private const val CONTINUATION_BIT = 0x20
