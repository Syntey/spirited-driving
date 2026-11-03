package com.syntey.spiriteddriving.network.graphql.injection

import com.syntey.spiriteddriving.network.graphql.client.ApolloClientFactory
import com.apollographql.apollo.ApolloClient
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@ComponentScan("com.syntey.spiriteddriving.network.graphql")
@Configuration
class NetworkGraphqlModule {
    @Single
    fun apolloClient(factory: ApolloClientFactory): ApolloClient = factory.create()
}
