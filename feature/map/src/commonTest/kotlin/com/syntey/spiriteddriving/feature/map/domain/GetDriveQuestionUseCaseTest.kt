package com.syntey.spiriteddriving.feature.map.domain

import com.syntey.spiriteddriving.core.domain.drive.DriveQuestion
import com.syntey.spiriteddriving.core.domain.drive.FakeOpenDriveQuestion
import com.syntey.spiriteddriving.core.domain.route.FakeRoutePlanner
import com.syntey.spiriteddriving.core.platform.binding.DriveReminders
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class GetDriveQuestionUseCaseTest {

    private val at = Instant.parse("2026-11-13T10:00:00Z")
    private val route = FakeRoutePlanner.Recorded.funRoutes.first()
    private val duration = 60.minutes
    private val halfway = at + 30.minutes
    private val arrival = at + duration

    @Test
    fun `no question asks nothing`() = runTest {
        val prompt = useCase(FakeOpenDriveQuestion()).build(halfway)

        assertNull(prompt)
    }

    @Test
    fun `just before half the planned duration asks nothing and keeps the question`() = runTest {
        val questions = FakeOpenDriveQuestion(question())

        val prompt = useCase(questions).build(halfway - 1.seconds)

        assertNull(prompt)
        assertNotNull(questions.question)
    }

    @Test
    fun `at half the planned duration the rating step opens`() = runTest {
        val prompt = useCase(FakeOpenDriveQuestion(question())).build(halfway)

        assertEquals(DrivePrompt.Kind.RatingStep, prompt?.kind)
    }

    @Test
    fun `at the planned arrival plus two hours the rating step still opens`() = runTest {
        val prompt = useCase(FakeOpenDriveQuestion(question())).build(arrival + 2.hours)

        assertEquals(DrivePrompt.Kind.RatingStep, prompt?.kind)
    }

    @Test
    fun `just after the planned arrival plus two hours the reminder bubble shows`() = runTest {
        val prompt = useCase(FakeOpenDriveQuestion(question())).build(arrival + 2.hours + 1.seconds)

        assertEquals(DrivePrompt.Kind.Bubble, prompt?.kind)
    }

    @Test
    fun `just before 48 hours after the hand-off the reminder bubble shows`() = runTest {
        val prompt = useCase(FakeOpenDriveQuestion(question())).build(at + 48.hours - 1.seconds)

        assertEquals(DrivePrompt.Kind.Bubble, prompt?.kind)
    }

    @Test
    fun `at 48 hours after the hand-off nothing is asked and the question is cleared`() = runTest {
        val questions = FakeOpenDriveQuestion(question())

        val prompt = useCase(questions).build(at + 48.hours)

        assertNull(prompt)
        assertNull(questions.question)
    }

    @Test
    fun `an expired question cancels its notification`() = runTest {
        val reminders = FakePlatformDriveReminders()

        useCase(FakeOpenDriveQuestion(question()), reminders).build(at + 48.hours)

        assertEquals(listOf("drive-1"), reminders.cancelled)
    }

    private fun useCase(
        questions: FakeOpenDriveQuestion,
        reminders: FakePlatformDriveReminders = FakePlatformDriveReminders(),
    ) = GetDriveQuestionUseCase(questions, DriveReminders(reminders))

    private fun question() = DriveQuestion(
        driveId = "drive-1",
        startedAt = at,
        routeNumber = 1,
        start = route.start.location,
        destination = route.destination,
        duration = duration,
        distance = route.distance,
        line = route.line,
        attribution = route.attribution,
    )
}
