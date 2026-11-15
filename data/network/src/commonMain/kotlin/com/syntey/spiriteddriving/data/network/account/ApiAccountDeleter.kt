package com.syntey.spiriteddriving.data.network.account

import com.syntey.spiriteddriving.core.domain.account.AccountDeleter
import com.syntey.spiriteddriving.data.network.api.SpiritedDrivingApi
import com.syntey.spiriteddriving.data.network.result.NetworkError
import com.syntey.spiriteddriving.data.network.result.NetworkResult
import com.syntey.spiriteddriving.data.network.result.getOrThrowDomainError
import com.syntey.spiriteddriving.data.network.tools.Constants
import io.ktor.http.HttpStatusCode
import org.koin.core.annotation.Factory

@Factory
internal class ApiAccountDeleter(private val api: SpiritedDrivingApi) : AccountDeleter {

    override suspend fun delete() {
        val result = api.deleteAccount()
        // A retry after the Firebase account is gone gets 401 ID_TOKEN_INVALID, which means the deletion is already done
        val error = (result as? NetworkResult.Failure)?.error
        if (error is NetworkError.HttpError && error.statusCode == HttpStatusCode.Unauthorized.value &&
            error.code == Constants.ProblemCode.ID_TOKEN_INVALID
        ) {
            return
        }
        result.getOrThrowDomainError()
    }
}
