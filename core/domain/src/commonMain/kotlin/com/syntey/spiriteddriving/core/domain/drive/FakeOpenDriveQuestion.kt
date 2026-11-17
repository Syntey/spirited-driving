package com.syntey.spiriteddriving.core.domain.drive

/**
 * [OpenDriveQuestion] that keeps the question in memory.
 */
class FakeOpenDriveQuestion(question: DriveQuestion? = null) : OpenDriveQuestion {

    var question: DriveQuestion? = question
        private set

    override suspend fun get(): DriveQuestion? = question

    override suspend fun set(question: DriveQuestion) {
        this.question = question
    }

    override suspend fun clear(driveId: String) {
        if (question?.driveId == driveId) question = null
    }
}
