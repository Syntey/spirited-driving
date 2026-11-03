package com.syntey.spiriteddriving.network.graphql.api

import com.syntey.spiriteddriving.network.graphql.GetEpisodesQuery
import com.syntey.spiriteddriving.network.graphql.client.ApiManager
import com.syntey.spiriteddriving.network.graphql.client.ApolloApiAdapter
import com.syntey.spiriteddriving.network.graphql.fragment.EpisodeFragment
import com.syntey.spiriteddriving.network.graphql.result.NetworkResult
import com.syntey.spiriteddriving.network.graphql.result.map
import org.koin.core.annotation.Single

@Single
internal class RickAndMortyApiImpl(override val apiAdapter: ApolloApiAdapter) :
    RickAndMortyApi,
    ApiManager {

    override suspend fun getEpisodes(): NetworkResult<List<EpisodeFragment>> = executeQuery(GetEpisodesQuery())
        .map { data ->
            val results = data.episodes?.results ?: emptyList()
            results.mapNotNull { result -> result?.episodeFragment }
        }
}
