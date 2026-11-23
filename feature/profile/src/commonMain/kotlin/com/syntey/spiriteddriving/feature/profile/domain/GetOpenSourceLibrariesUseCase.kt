package com.syntey.spiriteddriving.feature.profile.domain

import app.futured.arkitekt.crusecases.UseCase
import com.syntey.spiriteddriving.core.platform.binding.Platform
import com.syntey.spiriteddriving.feature.profile.model.OpenSourceLibrary
import com.syntey.spiriteddriving.feature.profile.tools.OpenSourceLibrariesParser
import kotlinx.collections.immutable.ImmutableList
import org.koin.core.annotation.Factory

/** Lists the open-source libraries the app ships, sorted by name. Throws when the build's JSON is invalid. */
@Factory
internal class GetOpenSourceLibrariesUseCase(private val platform: Platform) : UseCase<Unit, ImmutableList<OpenSourceLibrary>> {

    override suspend fun build(args: Unit): ImmutableList<OpenSourceLibrary> =
        OpenSourceLibrariesParser.parse(platform.openSourceLibrariesJson())
}
