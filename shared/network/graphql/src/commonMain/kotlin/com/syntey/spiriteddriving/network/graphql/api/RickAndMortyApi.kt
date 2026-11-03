package com.syntey.spiriteddriving.network.graphql.api

import com.syntey.spiriteddriving.network.graphql.fragment.EpisodeFragment
import com.syntey.spiriteddriving.network.graphql.result.NetworkResult

interface RickAndMortyApi {
    suspend fun getEpisodes(): NetworkResult<List<EpisodeFragment>>
}
