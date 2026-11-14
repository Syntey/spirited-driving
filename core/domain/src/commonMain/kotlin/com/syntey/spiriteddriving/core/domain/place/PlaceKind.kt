package com.syntey.spiriteddriving.core.domain.place

/**
 * What kind of place a [PlaceSuggestion] is, shown as its icon so places with the same name can be told apart.
 */
enum class PlaceKind {

    /** A city, town, village or district. */
    Town,

    /** A street, without a house number. */
    Street,

    /** A street or village with a house number. */
    Address,

    /** A peak, pass, viewpoint, lake or other natural feature. */
    Nature,

    /** Any other place, such as a castle, and any kind the app doesn't know. */
    PointOfInterest,
}
