package com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen.placeSearch

import com.syntey.spiriteddriving.feature.map.model.ui.SearchState

/**
 * What a place search shows.
 *
 * @property search The search as the sheet shows it: the text, the suggestions and the recent places.
 */
internal data class PlaceSearchViewState(val search: SearchState = SearchState())
