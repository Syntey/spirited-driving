package com.syntey.spiriteddriving.data.network.dto.plan

import com.syntey.spiriteddriving.data.network.dto.geo.GeoPointDto
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A place sent with a plan (`PlaceInput` schema). The backend names a start without a [name] after its nearest address,
 * so the key is left out rather than sent as null.
 */
@Serializable
internal data class PlaceInputDto(
    @SerialName("location") val location: GeoPointDto,
    @EncodeDefault(EncodeDefault.Mode.NEVER) @SerialName("name") val name: String? = null,
    @EncodeDefault(EncodeDefault.Mode.NEVER) @SerialName("area") val area: String? = null,
)
