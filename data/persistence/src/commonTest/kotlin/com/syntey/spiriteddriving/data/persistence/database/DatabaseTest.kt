package com.syntey.spiriteddriving.data.persistence.database

/** Base of tests that need the database: each test gets a fresh in-memory database, built per platform. */
internal expect abstract class DatabaseTest() {

    fun database(): PersistenceDatabase
}
