package com.syntey.spiriteddriving.data.network.result

import de.jensklingenberg.ktorfit.Ktorfit
import de.jensklingenberg.ktorfit.converter.Converter
import de.jensklingenberg.ktorfit.converter.KtorfitResult
import de.jensklingenberg.ktorfit.converter.TypeData
import io.ktor.client.call.body
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import io.ktor.util.reflect.TypeInfo
import org.koin.core.annotation.Single
import kotlin.reflect.cast
import io.ktor.client.statement.HttpResponse as KtorHttpResponse

/**
 * This converter allows usage of [NetworkResult] as API return type.
 */
@Single
class NetworkResultConverterFactory internal constructor(private val errorParser: NetworkErrorParser) : Converter.Factory {

    override fun suspendResponseConverter(
        typeData: TypeData,
        ktorfit: Ktorfit,
    ): Converter.SuspendResponseConverter<KtorHttpResponse, *>? {
        if (typeData.typeInfo.type == NetworkResult::class) {
            return object : Converter.SuspendResponseConverter<KtorHttpResponse, Any> {

                override suspend fun convert(result: KtorfitResult): Any {
                    val wrappedTypeData = typeData.typeArgs.first() // NetworkResult<wrappedTypeData>
                    val wrappedTypeInfo = wrappedTypeData.typeInfo

                    return when (result) {
                        is KtorfitResult.Success -> if (wrappedTypeInfo.type == Revalidated::class) {
                            result.response.toRevalidatedResult(expectedType = wrappedTypeData.typeArgs.first().typeInfo)
                        } else {
                            result.response.toNetworkResult(expectedType = wrappedTypeInfo)
                        }

                        is KtorfitResult.Failure -> NetworkResult.error(errorParser.parse(result.throwable))
                    }
                }
            }
        } else {
            return null
        }
    }

    private suspend fun KtorHttpResponse.toRevalidatedResult(expectedType: TypeInfo): NetworkResult<Any> {
        if (status == HttpStatusCode.NotModified) {
            return NetworkResult.success(Revalidated.NotModified)
        }

        return toNetworkResult(expectedType).map { Revalidated.Modified(it, etag = headers[HttpHeaders.ETag]) }
    }

    private suspend inline fun KtorHttpResponse.toNetworkResult(expectedType: TypeInfo): NetworkResult<Any> {
        if (!status.isSuccess()) {
            return NetworkResult.error(errorParser.parse(status))
        }

        return runCatching {
            NetworkResult.success(expectedType.type.cast(body(expectedType)))
        }.getOrElse { throwable ->
            NetworkResult.error(errorParser.parse(throwable))
        }
    }
}
