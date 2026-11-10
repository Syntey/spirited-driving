package com.syntey.spiriteddriving.konsist

import com.lemonappdev.konsist.api.ext.list.withNameEndingWith
import com.lemonappdev.konsist.api.verify.KoRule
import com.lemonappdev.konsist.api.verify.assertTrue
import kotlin.test.Test

class ScreenKonsistTest {

    @Test
    fun `feature components are internal Koin factories with a generated factory`() {
        productionScope
            .classes()
            .withNameEndingWith("Component")
            .filter { it.residesInModule(":feature..") && !it.hasAbstractModifier }
            .assertTrue(
                rule = KoRule(
                    id = "SCREEN-1",
                    title = "A feature's components are internal, @Factory and @GenerateFactory, " +
                        "and extend ScreenComponent or AppComponent.",
                    reason = "Navigation hosts in :shared create a feature's screens only through its *Screens object, which calls the " +
                        "factory Arkitekt generates, and Koin supplies the component's dependencies.",
                    howToFix = "Make the class internal, annotate it with @Factory and @GenerateFactory, and extend ScreenComponent.",
                ),
            ) {
                it.hasInternalModifier &&
                    it.hasAnnotationWithName("Factory") &&
                    it.hasAnnotationWithName("GenerateFactory") &&
                    it.parents().any { parent -> parent.simpleName in setOf("ScreenComponent", "AppComponent") }
            }
    }

    @Test
    fun `view states are immutable data classes`() {
        productionScope
            .classes()
            .withNameEndingWith("ViewState")
            .assertTrue(
                rule = KoRule(
                    id = "SCREEN-2",
                    title = "A *ViewState is a data class (or data object) whose properties are all val and of no Mutable* type.",
                    reason = "Components change state only through componentState.update { it.copy(…) }, " +
                        "and the UI renders each new value.",
                    howToFix = "Make it a data class, turn var into val, and replace mutable collections with kotlinx ImmutableList, " +
                        "ImmutableSet or ImmutableMap.",
                ),
            ) { viewState ->
                viewState.hasDataModifier &&
                    viewState.properties().all { property ->
                        property.isVal && property.type?.text?.startsWith("Mutable") != true
                    }
            }
    }

    @Test
    fun `screen UI files have a preview`() {
        productionScope
            .files
            .filter { it.name.endsWith("ScreenUi") && !it.isTemplateSample }
            .assertTrue(
                rule = KoRule(
                    id = "SCREEN-3",
                    title = "Every *ScreenUi.kt file has a @ScreenPreviews preview of its content.",
                    reason = "Previews are how a screen is checked against its design without running the app, " +
                        "and @ScreenPreviews also shows it in dark mode and with large fonts.",
                    howToFix = "Add a private @ScreenPreviews function that renders the private Content composable inside " +
                        "Showcase { }, with a view state from the screen's *ScreenPreviews object and noOpActions().",
                ),
            ) { file ->
                file.functions().any { it.hasAnnotationWithName("ScreenPreviews") }
            }
    }
}
