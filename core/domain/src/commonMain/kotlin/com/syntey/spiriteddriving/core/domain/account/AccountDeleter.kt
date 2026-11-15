package com.syntey.spiriteddriving.core.domain.account

import com.syntey.spiriteddriving.core.domain.error.OfflineException

/**
 * Deletes the signed-in user's account and everything it holds on the backend. Returns normally also when the account
 * was already deleted, and repeating it is safe. Throws [OfflineException] without a connection, other failures as they are.
 */
interface AccountDeleter {

    suspend fun delete()
}
