package com.syntey.spiriteddriving.feature.onboarding.domain

import app.futured.arkitekt.crusecases.UseCase
import com.syntey.spiriteddriving.core.platform.binding.PlatformWebBrowser
import org.koin.core.annotation.Factory

/** Opens a web page in the in-app browser. */
@Factory
internal class OpenWebPageUseCase(private val webBrowser: PlatformWebBrowser) : UseCase<OpenWebPageUseCase.Args, Unit> {

    override suspend fun build(args: Args) = webBrowser.open(args.url)

    data class Args(val url: String)
}
