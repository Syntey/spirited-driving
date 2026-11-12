package com.syntey.spiriteddriving.data.network.place

import com.syntey.spiriteddriving.core.domain.place.Place
import com.syntey.spiriteddriving.core.domain.place.PlaceSuggestion
import com.syntey.spiriteddriving.data.network.dto.place.PlaceSuggestionDto
import com.syntey.spiriteddriving.data.network.geo.toDomain

internal fun PlaceSuggestionDto.toDomain(): PlaceSuggestion = PlaceSuggestion(
    place = Place(name = name, area = area, location = location.toDomain()),
    isCovered = covered,
)
