package com.syntey.spiriteddriving.konsist

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.declaration.KoParentDeclaration
import com.lemonappdev.konsist.api.provider.KoPackageProvider

/**
 * Production code of every module. Parsing the files is the slow part, so all rules share this scope.
 */
internal val productionScope = Konsist.scopeFromProduction()

/**
 * The project template's sample screens, kept until real features replace them. Rules that only conventions added
 * since the template break skip them; delete this together with `:feature:sample`.
 */
internal val KoPackageProvider.isTemplateSample: Boolean
    get() = packagee?.name?.startsWith("com.syntey.spiriteddriving.feature.sample") == true

/**
 * The parent's name without type arguments: `ScreenComponent` for `ScreenComponent<FirstViewState, …>`.
 */
internal val KoParentDeclaration.simpleName: String
    get() = name.substringBefore('<')
