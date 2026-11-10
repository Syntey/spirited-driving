package com.syntey.spiriteddriving.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class PersonDto(
    @SerialName("name") val name: String? = null,
    @SerialName("homeworld") val homeworld: String? = null,
    @SerialName("gender") val gender: String? = null,
    @SerialName("url") val url: String? = null,
)
