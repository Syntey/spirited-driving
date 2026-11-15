package com.syntey.spiriteddriving.feature.profile.domain

import app.futured.arkitekt.crusecases.UseCase
import com.syntey.spiriteddriving.core.platform.binding.Platform
import com.syntey.spiriteddriving.feature.profile.model.Licence
import com.syntey.spiriteddriving.feature.profile.tools.Constants
import com.syntey.spiriteddriving.feature.profile.tools.OpenSourceLibrariesParser
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided

/** The licences of one library on the Licences screen, or of the Phosphor icons. Empty when the library isn't listed. */
@Factory
internal class GetLicencesUseCase(@Provided private val platform: Platform) : UseCase<GetLicencesUseCase.Args, ImmutableList<Licence>> {

    data class Args(val libraryId: String)

    override suspend fun build(args: Args): ImmutableList<Licence> {
        if (args.libraryId == Constants.Licences.PHOSPHOR_ID) return persistentListOf(PHOSPHOR_LICENCE)

        return OpenSourceLibrariesParser.parse(platform.openSourceLibrariesJson())
            .firstOrNull { it.id == args.libraryId }
            ?.licences
            ?: persistentListOf()
    }

    private companion object {
        // The licence's name is English in every locale, like the names the generated JSON carries
        val PHOSPHOR_LICENCE = Licence(
            name = "MIT License",
            text = Constants.Licences.PHOSPHOR_LICENCE_TEXT,
            url = Constants.Licences.PHOSPHOR_LICENCE_URL,
        )
    }
}
