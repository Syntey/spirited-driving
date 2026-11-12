package com.syntey.spiriteddriving.konsist

import com.lemonappdev.konsist.api.declaration.KoFileDeclaration
import com.lemonappdev.konsist.api.verify.KoRule
import com.lemonappdev.konsist.api.verify.assertTrue
import kotlin.test.Test

class UiKonsistTest {

    @Test
    fun `only Grid in core-ui theme Dimensions creates dp values`() {
        productionScope
            .files
            .filterNot { it.isGrid }
            .assertTrue(
                rule = KoRule(
                    id = "UI-1",
                    title = "Only Grid in core/ui theme/Dimensions.kt creates dp values.",
                    reason = "Spacing and sizes stay on the 4dp grid, and a value reads the same everywhere it's used.",
                    howToFix = "Use a Grid value (Grid.d4 is 16dp), or add the missing step to Grid. Keep a value that's used more " +
                        "than once in a local val inside the function.",
                ),
            ) { file ->
                file.imports.none { it.name == "androidx.compose.ui.unit.dp" }
            }
    }
}

// Grid is where dp values are made
private val KoFileDeclaration.isGrid: Boolean
    get() = name == "Dimensions" && packagee?.name == "com.syntey.spiriteddriving.core.ui.theme"
