# Data layer

How data reaches a store or background work: the HTTP client and `SafeApi`, services and response
models, sources and usecases, the Room KMP database and its schema changes, serialization, and
paging.

Read when: adding or changing a network call, a service, a response model, a source, a usecase, the
database or its schema, a model mapper at the data boundary, or a paged list.

## Network

- One `HttpClient` serves the app, an `@AppScope` binding built by `createHttpClient(engine)` in
  the network core module.
  Example: [HttpClientFactory](../../core-kmp/network/src/commonMain/kotlin/com/alekseivinogradov/anoti/network/kmp/impl/data/client/HttpClientFactory.kt)
- The engine comes from a platform component of that module: OkHttp on Android, Darwin on iOS.
  Example: [DiNetworkPlatformComponent](../../core-kmp/network/src/iosMain/kotlin/com/alekseivinogradov/anoti/network/ios/impl/di/DiNetworkPlatformComponent.kt)
- Client settings: `expectSuccess = true`, so a non-2xx status throws. JSON content negotiation
  with `ignoreUnknownKeys = true`. `HttpTimeout` sets request, connect and socket timeouts to one
  per-attempt constant.
- Every call goes through `SafeApi.call { }`, which returns a `CallResult`. `SafeApiImpl` retries
  any failure except cancellation. Its delay grows linearly: the base delay times the attempt
  number.
- The attempt count and base delay are set in the `SafeApi` DI provider, not in the class.
  Example: [DiNetworkComponent](../../core-kmp/network/src/commonMain/kotlin/com/alekseivinogradov/anoti/network/kmp/impl/di/DiNetworkComponent.kt)
- `CallResult` is `Success(value)` or a `Failure`: `HttpError(code)` for a `ResponseException`,
  `NetworkError` for an `IOException`, `OtherError` for anything else. Match `Failure` when only
  "did it fail" matters. What each failure shows the user is owned by
  [error-handling.md](error-handling.md).
  Example: [CallResult](../../core-kmp/network/src/commonMain/kotlin/com/alekseivinogradov/anoti/network/kmp/api/domain/model/CallResult.kt)
- A service is an interface in `api/data/service` of the shared base module, with
  `<Name>ServiceImpl` in `impl/data/service`. The implementation takes the `HttpClient` and builds
  each URL from the base-URL constant and a path constant.
  Example: [ShikimoriApiServiceImpl](../../feature-kmp/anime-base/src/commonMain/kotlin/com/alekseivinogradov/anoti/animebase/kmp/impl/data/service/ShikimoriApiServiceImpl.kt)
- The service binding sits in the shared base module's component, mixed into the app graph; its
  scope is in [dependency-injection.md "Scopes"](dependency-injection.md#scopes).
- A response model is a `@Serializable data class <Name>Response` in `api/data/response`. Every
  field has a `@SerialName`, a nullable type and a `null` default.
  Example: [AnimeShortResponse](../../feature-kmp/anime-base/src/commonMain/kotlin/com/alekseivinogradov/anoti/animebase/kmp/api/data/response/AnimeShortResponse.kt)
- Values the API takes or sends as strings are `*Data` enums with a `value: String`, in
  `api/data/model`.
  Example: [SortData](../../feature-kmp/anime-base/src/commonMain/kotlin/com/alekseivinogradov/anoti/animebase/kmp/api/data/model/SortData.kt)

## Sources and usecases

- A source is an interface `<Feature>Source` in `api/domain/source` that returns
  `CallResult<*Domain>`. `<Feature>SourceImpl` in `impl/data/source` takes the service and
  `SafeApi`.
- Each source method wraps the service call and the response-to-domain mapping in one
  `safeApi.call { }`. A mapping failure then becomes an `OtherError`. Items without an id are
  dropped. As found: the list source filters them before mapping, the background source drops
  them in its mapper, and the favorites single fetch maps a missing id to `-1`.
  Example: [AnimeListSourceImpl](../../feature-kmp/anime-list/src/commonMain/kotlin/com/alekseivinogradov/anoti/animelist/kmp/impl/data/source/AnimeListSourceImpl.kt)
- A usecase used only inside its module is a concrete class in `impl/domain/usecase` with one
  `execute` function. It takes a source and fixes the parameters the caller need not choose.
  Example: [FetchOngoingAnimeListUsecase](../../feature-kmp/anime-list/src/commonMain/kotlin/com/alekseivinogradov/anoti/animelist/kmp/impl/domain/usecase/FetchOngoingAnimeListUsecase.kt)
- A usecase another module calls is an interface in `api/domain/usecase`, implemented by
  `<Name>UsecaseImpl` in `impl/domain/usecase`. As found: iOS binds the background update
  usecase to a single-flight wrapper; see
  [platform-mirroring.md "Mirror table"](platform-mirroring.md#mirror-table).
  Example: [InsertAnimeDatabaseItemUsecase](../../core-kmp/anime-database/src/commonMain/kotlin/com/alekseivinogradov/anoti/animedatabase/kmp/api/domain/usecase/InsertAnimeDatabaseItemUsecase.kt)
- An executor gets its usecases as one `data class <Name>Usecases` in a `usecase/wrapper`
  sub-package.
  Example: [FavoritesUsecases](../../feature-kmp/anime-favorites/src/commonMain/kotlin/com/alekseivinogradov/anoti/animefavorites/kmp/impl/domain/usecase/wrapper/FavoritesUsecases.kt)
- Sources and usecases are unscoped bindings of their owning module's component: the feature
  graph for module-local ones, the owner's module component for exported ones. A usecase whose
  instance holds state every caller must share is `@AppScope`. The exceptions found are listed
  in [dependency-injection.md "Scopes"](dependency-injection.md#scopes).
  Example: [DiAnimeBackgroundUpdatePlatformComponent](../../feature-kmp/anime-background-update/src/iosMain/kotlin/com/alekseivinogradov/anoti/animebackgroundupdate/ios/impl/di/DiAnimeBackgroundUpdatePlatformComponent.kt)
- Each feature has its own domain models in `api/domain/model`, even where two features describe
  the same thing.
- Boundary mappers are extension functions: response to domain in `api/data/mapper`
  (`toListItemDomain()`), domain to and from the Db model in `api/domain/mapper`
  (`toDb()`, `toDomain()`).
  Example: [AnimeFavoritesDatabaseMapper](../../feature-kmp/anime-favorites/src/commonMain/kotlin/com/alekseivinogradov/anoti/animefavorites/kmp/api/domain/mapper/AnimeFavoritesDatabaseMapper.kt)

## Persistence

- One persistence core module owns the database, on Room KMP with the bundled SQLite driver.
  Example: [AnimeDatabase](../../core-kmp/anime-database/src/commonMain/kotlin/com/alekseivinogradov/anoti/animedatabase/kmp/impl/data/AnimeDatabase.kt)
- The `@Database` class carries `@ConstructedBy(<Name>DatabaseConstructor::class)`, declared as an
  `expect object`. Room's KSP processor generates its `actual`s; see [kmp.md](kmp.md), section
  "Expect and actual".
- A common `getRoomDatabase(builder)` adds migrations, an opening-log callback, the
  `BundledSQLiteDriver` and the query context, then builds.
- Each platform makes the builder in `<Name>Database.android.kt` or `<Name>Database.ios.kt`. The
  platform component provides the database as an `@AppScope` binding.
  Example: [AnimeDatabase.android.kt](../../core-kmp/anime-database/src/androidMain/kotlin/com/alekseivinogradov/anoti/animedatabase/android/impl/data/AnimeDatabase.android.kt)
- Entity, DAO, database, repository implementation and entity mapper sit in `impl/data`. The
  repository interface and the `*Db*` models other modules exchange sit in `api/domain`.
- The query context is `Dispatchers.IO`, set on the builder. It is the named exception to taking
  dispatchers from the provider; see [concurrency-and-lifecycle.md](concurrency-and-lifecycle.md).
- The UI reads and writes through the module's store. Background work calls its `api` usecases.
  Example: [AnimeUpdateManagerImpl](../../feature-kmp/anime-background-update/src/commonMain/kotlin/com/alekseivinogradov/anoti/animebackgroundupdate/kmp/impl/domain/manager/AnimeUpdateManagerImpl.kt)
- The store's writes outlive the screen that asked for them; the scope they run on is in
  [concurrency-and-lifecycle.md "Scope owners"](concurrency-and-lifecycle.md#scope-owners).
- The module's build setup (KSP per target, the Room plugin, `-Xexpect-actual-classes`, the SQLite
  natives for host tests) is owned by
  [new-module.md "Boilerplate notes"](new-module.md#boilerplate-notes).

## Schema changes

- Bump `version` in `@Database`.
- Write the `Migration(startVersion, endVersion)` by hand, as an `internal val MIGRATION_<a>_<b>`
  next to the database class. Add it in `getRoomDatabase` with `addMigrations`.
- A new column gets a SQL `DEFAULT` in the migration and the same `defaultValue` in its
  `@ColumnInfo`, so existing rows stay valid.
- Prove continuity on both platforms: a test seeds a database file at the old version with raw
  SQL, opens it through the real builder, and checks that the rows survived.
- The Android test sits in `androidHostTest`, the iOS one in `iosTest`.
  Example: [AnimeDatabaseContinuityTest](../../core-kmp/anime-database/src/androidHostTest/kotlin/com/alekseivinogradov/anoti/animedatabase/android/impl/data/AnimeDatabaseContinuityTest.kt)
- As found: `exportSchema = false`, while the build sets a `schemas` directory and no schema file
  is tracked. No exported schema exists to diff or to test migrations against.
  Example: [build.gradle.kts](../../core-kmp/anime-database/build.gradle.kts)

## Serialization

kotlinx.serialization is used for:

- HTTP responses, decoded through Ktor content negotiation (section "Network" above).
- Root navigation configs; see [navigation.md](navigation.md), section "Root stack".
- Values a screen component keeps in its `StateKeeper`, and the iOS saved-state string; see
  [state-restoration.md](state-restoration.md).

## Paging

- `Paginator<T>(firstPage, loadPage)` in the UI kit module pages through a
  `suspend (page: Int) -> CallResult<List<T>>`.
  Example: [Paginator](../../core-kmp/celebrity/src/commonMain/kotlin/com/alekseivinogradov/anoti/celebrity/kmp/api/domain/paging/Paginator.kt)
- `loadFirstPage()` resets to the first page and loads it. `loadNextPage()` returns `null` once an
  empty page has arrived, or while a load is in flight.
- A load returns a `PageLoadResult` with `isFirstPage`: `Success(items)`, `Error` for an HTTP or
  network failure, `UnexpectedError` for `OtherError` or a throwing `loadPage`. Cancellation is
  rethrown.
- The executor holds the paginator in a `private var`. A refresh cancels the jobs in flight and
  builds a new paginator before loading the first page.
  Example: [OngoingSectionExecutorImpl](../../feature-kmp/anime-list/src/commonMain/kotlin/com/alekseivinogradov/anoti/animelist/kmp/impl/domain/store/ongoingsection/OngoingSectionExecutorImpl.kt)
- A next page is appended without items whose id is already in the list. The backend can repeat
  an item between two requests.
  Example: [ListItemsPage](../../feature-kmp/anime-list/src/commonMain/kotlin/com/alekseivinogradov/anoti/animelist/kmp/impl/domain/store/ListItemsPage.kt)
- The first page number and the page size are shared constants of the shared base module.

## Testing

- HTTP is faked with Ktor's `MockEngine`; other data doubles are handwritten. See
  [testing.md](testing.md), section "Test doubles".
  Example: [ShikimoriApiServiceImplTest](../../feature-kmp/anime-base/src/commonTest/kotlin/com/alekseivinogradov/anoti/animebase/kmp/impl/data/service/ShikimoriApiServiceImplTest.kt)
- `SafeApiFake` never retries and wraps any failure as `OtherError`.
  Example: [SafeApiFake](../../core-kmp/network/src/commonMain/kotlin/com/alekseivinogradov/anoti/network/kmp/impl/data/fake/SafeApiFake.kt)
