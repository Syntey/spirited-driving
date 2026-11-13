---
name: network-worker
description: Add or change a backend API call - the :core:domain interface, models and errors, the internal DTOs and the Ktorfit SpiritedDrivingApi in :data:network, the Api* implementation that maps DTOs and errors to domain types, and their tests. Use whenever you touch :data:network, model an endpoint from the API spec, or map network errors, even if the user only says "load X from the backend".
---

# Network worker

Features never see the network. They call an interface in `:core:domain`, `:data:network` implements it over HTTP, and
the Koin graph in `:shared` connects the two. Konsist checks the shape of DTOs, APIs and implementations (`DATA-*` in
`konsist/`), and its failure message says how to fix a violation.

## The contract

- The backend's OpenAPI spec owns every path, field, unit and error code. Read the operation before writing code, and
  don't guess a name, a `required` flag or a status code: a wrong guess compiles and fails only against the backend.
- A local backend serves the spec as Swagger UI at http://localhost:8080/swagger and as YAML at
  http://localhost:8080/swagger/openapi.yaml. Check real responses there before trusting a DTO.
- Dev builds call the local backend: the Android emulator at `10.0.2.2`, the iOS simulator at `localhost`, and an
  Android phone at `ANDROID_DEV_API_URL` from `local.properties`.
- The spec's server URL ends in `/v1`, so the app's base URL does too, and API paths are the spec's paths without the
  leading slash (`/plans` → `"plans"`).
- Errors come as Problem Details (`application/problem+json`) with a machine-readable `code`. An unknown code is
  treated like a failure of its HTTP status.

## `:core:domain`: what features see

In `core/domain/src/commonMain/kotlin/.../core/domain/<area>/`:

- **Models**: data classes named for the domain (`Route`, `TripPlan`), using domain types (`GeoPoint`,
  `kotlin.time.Duration`), not wire shapes.
- **The interface**, named for what it does (`RoutePlanner`, `PlaceSearch`): `suspend` functions that return models
  and throw domain errors.
- **Domain errors**: exceptions for what the UI explains differently, such as offline, no route within the budget, or
  outside the covered area. Errors several areas share are defined once.
- **`Fake<Interface>`** with recorded data, for previews, tests and running without a backend.

## `:data:network`: DTOs

In `data/network/src/commonMain/kotlin/.../data/network/dto/<area>/`:

```kotlin
/** Request body of `POST /plans` (`PlanRequest`). */
@Serializable
internal data class PlanRequestDto(
    @SerialName("start") val start: PlaceInputDto,
    @SerialName("destination") val destination: PlaceInputDto,
    @SerialName("budget") val budget: BudgetDto,
    @SerialName("vignettes") val vignettes: Set<String>,
    @SerialName("maxHandOffWaypoints") val maxHandOffWaypoints: Int,
)
```

- The name is the spec's schema name plus `Dto`.
- Requests: `required` fields are non-null without a default, so the compiler makes every caller set them; optional
  ones are nullable `= null`.
- Responses: `required` fields are non-null, the rest nullable `= null`. Model only the fields the app uses; the JSON
  config ignores the rest.
- A value set the backend may extend, like the problem `code`, is a `String` mapped in code: an unknown enum value
  fails the whole response.
- The codes it's mapped from (`Constants.ProblemCode`, `Constants.RouteKindCode`) live in `tools/Constants.kt`, with the
  HTTP timeouts. Tests keep the literal codes, so a wrong constant fails them.
- The constructor mirrors the wire. Derived values are `get()` properties in the class body, which kotlinx.serialization
  skips; a body property with an initializer would be serialized too.
- KDoc names the method, path and schema, and nothing the types already say.

## `:data:network`: the API

Every endpoint is a function of one Ktorfit interface, `data/network/.../api/SpiritedDrivingApi.kt`, in the order
of the spec's paths:

```kotlin
internal interface SpiritedDrivingApi {

    /** `POST /plans` (`planTrip`). */
    @POST("plans")
    suspend fun planTrip(
        @Body request: PlanRequestDto,
    ): NetworkResult<PlanDto>
}
```

- Optional query parameters are nullable `= null`.
- `injection/NetworkModule.kt` provides it (`ktorFit.createSpiritedDrivingApi()`), so a new endpoint needs no wiring.
- A call that sends `If-None-Match` returns `NetworkResult<Revalidated<…>>`, which carries the `ETag` or says the copy
  is current (304).
- Headers every call needs (App Check, the signed-in user's ID token) come from an `HttpClientPlugin`, not from
  method parameters, so no call can forget one.

## `:data:network`: the implementation

```kotlin
@Factory
internal class ApiRoutePlanner(private val api: SpiritedDrivingApi) : RoutePlanner {

    override suspend fun plan(trip: TripRequest): TripPlan =
        api.planTrip(trip.toDto()).getOrThrow().toDomain()
}
```

- Koin binds it to the interface, so there's nothing else to wire.
- Mapping lives in pure extension functions (`toDomain()`, `toDto()`) next to the implementation, so tests cover it
  without HTTP.
- Map `NetworkError` to domain errors here: `ConnectionError` means offline. `NetworkResultConverterFactory` reads the
  Problem body's `code` into `HttpError.code`, and the implementation passes `getOrThrowDomainError` a function that
  turns the codes it knows into domain errors (as `ApiRoutePlanner` does with `OUTSIDE_COVERAGE`). Mapping several
  calls share is written once in `result/`. Errors the UI doesn't tell apart propagate as they are, and features show
  them as a generic failure. Nothing outside `:data:network` branches on `NetworkError`.
- Don't catch `CancellationException`, or a broad `Exception` that includes it: it's how coroutines cancel, which is
  why `NetworkErrorParser` rethrows it.

## Tests

- Mapping both ways, for every field the app uses, and each error mapping.
- Decoding a response recorded from the local backend with the app's JSON config (`NetworkModule().restApiJson()`).
  This catches nullability that doesn't match the backend.
- `SpiritedDrivingApi` with the converter is tested over Ktor's `MockEngine`, as in `ApiCoverageSourceTest`.

## Not set up yet

The first network change that needs one of these sets it up, then updates this section:

- **Auth headers**: an `HttpClientPlugin` that adds `X-Firebase-AppCheck` to every call and
  `Authorization: Bearer <ID token>` while signed in.

## Before you call it done

Besides the Definition of done in `CLAUDE.md`, check that the call works against the local backend, not only in tests.
