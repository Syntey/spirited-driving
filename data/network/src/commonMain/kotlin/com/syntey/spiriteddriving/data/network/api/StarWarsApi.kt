package com.syntey.spiriteddriving.data.network.api

import com.syntey.spiriteddriving.data.network.dto.Person
import com.syntey.spiriteddriving.data.network.result.NetworkResult
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Path

interface StarWarsApi {

    @GET("people/{id}")
    suspend fun getPerson(
        @Path("id") personId: Int,
    ): NetworkResult<Person>
}
