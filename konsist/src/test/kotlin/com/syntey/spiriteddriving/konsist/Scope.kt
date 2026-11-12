package com.syntey.spiriteddriving.konsist

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.declaration.KoParentDeclaration

/**
 * Production code of every module. Parsing the files is the slow part, so all rules share this scope.
 */
internal val productionScope = Konsist.scopeFromProduction()

/**
 * The parent's name without type arguments: `ScreenComponent` for `ScreenComponent<FirstViewState, …>`.
 */
internal val KoParentDeclaration.simpleName: String
    get() = name.substringBefore('<')
