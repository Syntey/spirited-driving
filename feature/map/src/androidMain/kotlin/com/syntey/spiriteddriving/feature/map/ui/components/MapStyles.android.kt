package com.syntey.spiriteddriving.feature.map.ui.components

/**
 * Google Maps styles (JSON, as a map ID would move the map to a paid SKU): muted, so route lines stand out, and without
 * business and transit points of interest.
 */
internal object MapStyles {
    const val LIGHT = """[
  {"featureType": "poi.business", "stylers": [{"visibility": "off"}]},
  {"featureType": "transit", "stylers": [{"visibility": "off"}]},
  {"featureType": "landscape", "stylers": [{"saturation": -60}]},
  {"featureType": "poi", "elementType": "geometry", "stylers": [{"saturation": -50}]},
  {"featureType": "water", "elementType": "geometry", "stylers": [{"saturation": -40}]},
  {"featureType": "road", "elementType": "geometry", "stylers": [{"saturation": -100}]},
  {"featureType": "road.highway", "elementType": "geometry", "stylers": [{"saturation": -100}, {"lightness": 15}]},
  {"featureType": "road", "elementType": "labels.icon", "stylers": [{"saturation": -100}]}
]"""

    const val DARK = """[
  {"elementType": "geometry", "stylers": [{"color": "#1d2024"}]},
  {"elementType": "labels.text.fill", "stylers": [{"color": "#8a9099"}]},
  {"elementType": "labels.text.stroke", "stylers": [{"color": "#1d2024"}]},
  {"featureType": "administrative", "elementType": "geometry.stroke", "stylers": [{"color": "#3c4148"}]},
  {"featureType": "landscape.natural", "elementType": "geometry", "stylers": [{"color": "#20242a"}]},
  {"featureType": "poi", "elementType": "geometry", "stylers": [{"color": "#22262c"}]},
  {"featureType": "poi.park", "elementType": "geometry", "stylers": [{"color": "#1f2a26"}]},
  {"featureType": "road", "elementType": "geometry", "stylers": [{"color": "#2e333a"}]},
  {"featureType": "road", "elementType": "geometry.stroke", "stylers": [{"color": "#1d2024"}]},
  {"featureType": "road.highway", "elementType": "geometry", "stylers": [{"color": "#3a4048"}]},
  {"featureType": "road", "elementType": "labels.icon", "stylers": [{"saturation": -100}, {"lightness": -30}]},
  {"featureType": "water", "elementType": "geometry", "stylers": [{"color": "#151c24"}]},
  {"featureType": "water", "elementType": "labels.text.fill", "stylers": [{"color": "#4e5864"}]},
  {"featureType": "poi.business", "stylers": [{"visibility": "off"}]},
  {"featureType": "transit", "stylers": [{"visibility": "off"}]}
]"""
}
