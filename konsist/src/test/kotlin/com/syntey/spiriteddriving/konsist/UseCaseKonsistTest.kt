package com.syntey.spiriteddriving.konsist

import com.lemonappdev.konsist.api.ext.list.withNameEndingWith
import com.lemonappdev.konsist.api.verify.KoRule
import com.lemonappdev.konsist.api.verify.assertTrue
import kotlin.test.Test

class UseCaseKonsistTest {

    @Test
    fun `use cases are internal Koin factories in a domain package`() {
        productionScope
            .classes()
            .withNameEndingWith("UseCase")
            .assertTrue(
                rule = KoRule(
                    id = "USECASE-1",
                    title = "A *UseCase is an internal @Factory class in its module's domain package.",
                    reason = "Use cases are a module's own logic: each component gets a new instance from Koin, " +
                        "and no other module uses them.",
                    howToFix = "Make the class internal, annotate it with @Factory, and move it to the module's `domain` package.",
                ),
            ) {
                it.hasInternalModifier &&
                    it.hasAnnotationWithName("Factory") &&
                    it.residesInPackage("..domain..")
            }
    }
}
