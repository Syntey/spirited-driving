package com.syntey.spiriteddriving.data.network.dto.problem

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Error body of every call (`Problem`, `application/problem+json`). */
@Serializable
internal data class ProblemDto(@SerialName("code") val code: String)
