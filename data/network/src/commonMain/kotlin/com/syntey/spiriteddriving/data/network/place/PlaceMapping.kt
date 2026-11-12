package com.syntey.spiriteddriving.data.network.place

import com.syntey.spiriteddriving.core.domain.place.Place
import com.syntey.spiriteddriving.core.domain.place.PlaceKind
import com.syntey.spiriteddriving.core.domain.place.PlaceSuggestion
import com.syntey.spiriteddriving.data.network.dto.place.PlaceSuggestionDto
import com.syntey.spiriteddriving.data.network.geo.toDomain

internal fun PlaceSuggestionDto.toDomain(): PlaceSuggestion = PlaceSuggestion(
    place = Place(name = name, area = area, location = location.toDomain()),
    isCovered = covered,
    kind = kind.toPlaceKind(),
)

// A kind the app doesn't know yet is shown as a generic place, so a new backend kind doesn't fail the search
private fun String.toPlaceKind(): PlaceKind = when (this) {
    "TOWN" -> PlaceKind.Town
    "STREET" -> PlaceKind.Street
    "ADDRESS" -> PlaceKind.Address
    "NATURE" -> PlaceKind.Nature
    else -> PlaceKind.PointOfInterest
}
