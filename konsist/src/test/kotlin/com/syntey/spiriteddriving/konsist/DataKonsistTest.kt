package com.syntey.spiriteddriving.konsist

import com.lemonappdev.konsist.api.verify.KoRule
import com.lemonappdev.konsist.api.verify.assertTrue
import kotlin.test.Test

class DataKonsistTest {

    @Test
    fun `DTOs are internal serializable classes with explicit wire names`() {
        productionScope
            .classes()
            .filter { it.residesInPackage("com.syntey.spiriteddriving.data.network.dto..") }
            .assertTrue(
                rule = KoRule(
                    id = "DATA-1",
                    title = "Classes in data.network.dto are internal @Serializable *Dto classes, " +
                        "and every constructor property has @SerialName.",
                    reason = "DTOs mirror the API contract and never leave :data:network. " +
                        "@SerialName keeps the wire name when a property is renamed in Kotlin.",
                    howToFix = "Add the Dto suffix, make the class internal, annotate it with @Serializable, and give each constructor " +
                        "property @SerialName with the field name from the API spec.",
                ),
            ) { dto ->
                dto.name.endsWith("Dto") &&
                    dto.hasInternalModifier &&
                    dto.hasAnnotationWithName("Serializable") &&
                    dto.properties().filter { it.isConstructorDefined }.all { it.hasAnnotationWithName("SerialName") }
            }
    }

    @Test
    fun `API interfaces return NetworkResult from suspend functions`() {
        productionScope
            .interfaces()
            .filter { it.residesInPackage("com.syntey.spiriteddriving.data.network.api..") }
            .assertTrue(
                rule = KoRule(
                    id = "DATA-2",
                    title = "Ktorfit *Api interfaces are internal, and each of their functions is suspend and returns NetworkResult.",
                    reason = "NetworkResultConverterFactory turns every response and failure into a NetworkResult, so no caller catches " +
                        "Ktor exceptions.",
                    howToFix = "Make the interface internal, mark each function suspend, and wrap its return type in NetworkResult<…> " +
                        "(NetworkResult<Unit> when there is no body).",
                ),
            ) { api ->
                api.name.endsWith("Api") &&
                    api.hasInternalModifier &&
                    api.functions().all { it.hasSuspendModifier && it.returnType?.text?.startsWith("NetworkResult<") == true }
            }
    }

    @Test
    fun `implementations of domain interfaces are internal Koin definitions`() {
        productionScope
            .classes()
            .filter { implementation ->
                // A parent's package isn't known to Konsist, so a domain interface is recognized by its import
                val domainImports = implementation.containingFile.imports
                    .map { it.name }
                    .filter { it.startsWith("com.syntey.spiriteddriving.core.domain.") }
                implementation.residesInModule(":data..") &&
                    implementation.parents().any { parent -> domainImports.any { it.endsWith(".${parent.simpleName}") } }
            }
            .assertTrue(
                rule = KoRule(
                    id = "DATA-3",
                    title = "A :data:* class that implements a :core:domain interface is internal and annotated with @Factory or @Single.",
                    reason = "Features see only the :core:domain interface. Koin binds the implementation to it in the :shared graph.",
                    howToFix = "Make the class internal and annotate it with @Factory, or @Single when it holds state.",
                ),
            ) {
                it.hasInternalModifier && it.hasAnnotationWithName("Factory", "Single")
            }
    }
}
