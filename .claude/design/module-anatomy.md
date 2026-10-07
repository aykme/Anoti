# Module anatomy

What one Gradle module looks like inside: its source sets and directories, the package scheme, the
`api`/`impl` split, the naming suffixes, and the manifest and Compose resources settings. It is
the single owner of class-name suffixes, packages and source sets; other design files link here.
File names are in [code-style.md](code-style.md). Which kinds
of module exist is in [project-structure.md](project-structure.md); which source set a piece of
code belongs in is in [kmp.md](kmp.md), section "Where code lives".

Read when: creating a file, a package or a module; naming a class; choosing between `api` and
`impl`; editing a manifest or a module's Compose resources settings.

## Source sets

Every module except the Android host is a KMP module with these source sets:

| Source set | Holds | Example |
|---|---|---|
| `commonMain` | Everything that compiles for every target; the default | [anime-list commonMain](../../feature-kmp/anime-list/src/commonMain) |
| `commonTest` | Tests of common code; they run on the Android host JVM and on iOS | [anime-list commonTest](../../feature-kmp/anime-list/src/commonTest) |
| `androidMain` | Code that needs an Android API; the module's `AndroidManifest.xml` | [anime-notification androidMain](../../feature-kmp/anime-notification/src/androidMain) |
| `androidHostTest` | Robolectric tests and composable tests on the JVM | [anime-notification androidHostTest](../../feature-kmp/anime-notification/src/androidHostTest) |
| `androidDeviceTest` | Instrumented tests, only where the module enables them | [anime-notification androidDeviceTest](../../feature-kmp/anime-notification/src/androidDeviceTest) |
| `iosMain` | Code that needs an iOS API, and the iOS-only code [kmp.md](kmp.md) names | [anime-notification iosMain](../../feature-kmp/anime-notification/src/iosMain) |
| `iosTest` | Tests only a real iOS runtime can answer | [main iosTest](../../main/src/iosTest) |

The Android host is a plain Android application module. Its source sets are `main`, `test` (host
tests) and `androidTest` (instrumented tests).
Example: [androidApp/src](../../androidApp/src).

- Source directories are `kotlin`, never `java`, in any source set (`src/main/kotlin`,
  `src/test/kotlin`, `src/androidTest/kotlin`).
- Compose resources live in `src/commonMain/composeResources/`, split into `values`, `drawable`
  and `font`.
  Example: [UI kit composeResources](../../core-kmp/celebrity/src/commonMain/composeResources).
- The UI kit holds the launcher and notification icons and the starting-window theme in
  `src/androidMain/res`. The Android host holds `app_name` in `src/main/res`. What belongs in
  platform resources is in [kmp.md "Resources"](kmp.md#resources).
  Example: [UI kit res](../../core-kmp/celebrity/src/androidMain/res),
  [host res](../../androidApp/src/main/res).
- A module's root holds its `build.gradle.kts`, its README and its regression file. The files a
  module must carry, and their names, are set by [module-docs.md](../rules/module-docs.md).
- A test sits in the package of the code it tests. A test of common code keeps the `kmp` segment
  even in `androidHostTest`.
  Example: [PosterLoaderTest](../../feature-kmp/anime-notification/src/androidHostTest/kotlin/com/alekseivinogradov/anoti/animenotification/kmp/impl/presentation/poster/PosterLoaderTest.kt).

## Packages

The package scheme is `<root-package>.<module-id>.<platform>.<visibility>.<layer>.<role>`.

| Segment | Values | Notes |
|---|---|---|
| `<module-id>` | The Gradle module name without hyphens | `anime-list` → `animelist`; a `-external` module adds `.external`: `animenotification.external` |
| `<platform>` | `kmp`, `android`, `ios` | `kmp` in `commonMain` and its tests, `android` in `androidMain`, `ios` in `iosMain` |
| `<visibility>` | `api`, `impl` | See the criterion below |
| `<layer>` | `data`, `domain`, `presentation`, `di` | `di` holds DI components and dependencies contracts |
| `<role>` | One or more sub-packages | See the role table below |

Role sub-packages in use:

| Layer | Roles |
|---|---|
| `data` | `client`, `mapper`, `model`, `response`, `service`, `source`, `repository`; files at the layer root for the Room database and DAO, and the safe-call wrapper |
| `domain` | `model`, `store` (one sub-package per store when a screen has several: `main`, `<name>section`), `usecase` (+ `wrapper`), `source`, `mapper` (+ `store`), `manager`, `scheduler`, `worker`, `repository`, `coroutinecontext`, `formatter`, `paging`, `systemmessage.controller`, `systemmessage.provider`; constants and typealiases at the layer root |
| `presentation` | `compose`, `navigation`, `mapper` (+ `model`), `model` (+ `itemcontent`), `di`, `factory`, `manager`, `poster`, `provider`; controllers at the layer root. The entry module adds `notification`, `orientation`, `permission`, `savedstate`, `lifecycle` |
| any | `fake` as the last segment, for test doubles: see [testing.md](testing.md), section "Test doubles" |

A declaration is in `api` when both hold:

1. Outside code programs against it or implements it: another module, a host, or the app graph.
2. Its signature names only `api` types and external libraries, never an `impl` type.

Everything else is in `impl`. That includes every `@Composable` (screens, dialogs, hosts, themes),
composable `Modifier` extensions, controllers, screen components, routes and DI components.
Being an interface does not by itself make a type `api`.

Settled exceptions:

- `*Dimens.kt`, `Colors.kt` and `Fonts.kt` are in `api`.
- `Paginator` and `SystemMessageController` are classes passed into `api` types, and are in `api`.
- Feature mappers are in `api`.
- Dependencies contracts (`Di<Feature>Dependencies`, `DiRootDependencies`) are in `api`.
- In the entry module, the component holder, the root child and the root dependencies bundle
  (`RootDependencies`) reference `impl` types, and stay in `impl`.
  Example: [NavRootChild](../../main/src/commonMain/kotlin/com/alekseivinogradov/anoti/main/impl/presentation/navigation/NavRootChild.kt).

`impl` packages are still public across modules. Which modules read another module's `impl`, and
why, is in [project-structure.md](project-structure.md), section "Visibility".

The Android namespace is `<root-package>.<module-id>.kmp`.
Example: [anime-list build file](../../feature-kmp/anime-list/build.gradle.kts).

As found, a few modules leave the scheme; copy the scheme, not them:

- The entry module uses `<root-package>.main.<visibility>...` with no platform segment in any source
  set, and its namespace has no `.kmp`.
- The composition root and the DI-scope leaf share the base package `<root-package>.di.kmp`, with
  no visibility or layer; the leaf adds `scope` and `qualifier` under it. Their namespaces are
  `<root-package>.di.kmp` and `<root-package>.discope.kmp`.
- The navigation core module keeps its files directly in `<root-package>.navigation.kmp`.
- The Android host's namespace is `<root-package>`, and its code is in
  `<root-package>.impl.presentation`. Its instrumented flow test sits in `<root-package>` itself.
- The Android platform DI packages differ: `android.impl.di` in some modules,
  `android.impl.presentation.di` in others, `main.impl.presentation.di` in the entry module. The
  iOS ones are all `ios.impl.di`.
  Example: [Android DiAnimeNotificationPlatformComponent](../../feature-kmp/anime-notification/src/androidMain/kotlin/com/alekseivinogradov/anoti/animenotification/android/impl/presentation/di/DiAnimeNotificationPlatformComponent.kt).
- The Android notification intent contract sits in an `impl` package of the external module.
  Example: [AnimeNotificationIntentProvider](../../feature-kmp/anime-notification-external/src/androidMain/kotlin/com/alekseivinogradov/anoti/animenotification/external/android/impl/presentation/provider/AnimeNotificationIntentProvider.kt).

## Naming

File naming is in [code-style.md](code-style.md). The suffix says what a type is:

| Name | Kind | Where | Example |
|---|---|---|---|
| `Di<Module>Component` | Module component (DI bindings of one module) | `impl.di` | [DiCelebrityComponent](../../core-kmp/celebrity/src/commonMain/kotlin/com/alekseivinogradov/anoti/celebrity/kmp/impl/di/DiCelebrityComponent.kt) |
| `Di<Module>PlatformComponent` | Platform component, one per platform, same simple name | `android…di`, `ios.impl.di` | [DiAnimeNotificationPlatformComponent](../../feature-kmp/anime-notification/src/iosMain/kotlin/com/alekseivinogradov/anoti/animenotification/ios/impl/di/DiAnimeNotificationPlatformComponent.kt) |
| `Di<Feature>Component` + `createDi<Feature>Component` | Feature graph and its creator | `impl.di` | [DiAnimeListComponent](../../feature-kmp/anime-list/src/commonMain/kotlin/com/alekseivinogradov/anoti/animelist/kmp/impl/di/DiAnimeListComponent.kt) |
| `Di<Feature>Dependencies` | Dependencies contract | `api.di` | [DiAnimeListDependencies](../../feature-kmp/anime-list/src/commonMain/kotlin/com/alekseivinogradov/anoti/animelist/kmp/api/di/DiAnimeListDependencies.kt) |
| `Nav<Feature>ScreenComponent` | Screen component | `impl.presentation.navigation` | [NavAnimeListScreenComponent](../../feature-kmp/anime-list/src/commonMain/kotlin/com/alekseivinogradov/anoti/animelist/kmp/impl/presentation/navigation/NavAnimeListScreenComponent.kt) |
| `<Feature>Route` | Composable that reads a controller and draws the screen | `impl.presentation.navigation` (screen), entry module `compose` (root-level element) | [AnimeListRoute](../../feature-kmp/anime-list/src/commonMain/kotlin/com/alekseivinogradov/anoti/animelist/kmp/impl/presentation/navigation/AnimeListRoute.kt) |
| `<Feature>Screen`, `<Feature>Item`, `<Feature>TopBar` | Stateless composables | `impl.presentation.compose` | [AnimeListScreen](../../feature-kmp/anime-list/src/commonMain/kotlin/com/alekseivinogradov/anoti/animelist/kmp/impl/presentation/compose/AnimeListScreen.kt) |
| `<Feature>Controller` | Controller | `impl.presentation` | [AnimeListController](../../feature-kmp/anime-list/src/commonMain/kotlin/com/alekseivinogradov/anoti/animelist/kmp/impl/presentation/AnimeListController.kt) |
| `<Feature>UiModel`, `<Name>Ui` | UI models: whole screen, one part | `api.presentation.model` | [AnimeListUiModel](../../feature-kmp/anime-list/src/commonMain/kotlin/com/alekseivinogradov/anoti/animelist/kmp/api/presentation/model/AnimeListUiModel.kt) |
| `<Name>State` | State holder of a UI-only component, kept by its host | entry module `compose` | [NotificationsRationaleState](../../main/src/commonMain/kotlin/com/alekseivinogradov/anoti/main/impl/presentation/compose/NotificationsRationaleState.kt) |
| `<Feature>MainStore`, `<Name>SectionStore`, `<Name>Store` | Store contract: a screen's main store, its section stores, any other store | `api.domain.store` | [AnimeListMainStore](../../feature-kmp/anime-list/src/commonMain/kotlin/com/alekseivinogradov/anoti/animelist/kmp/api/domain/store/main/AnimeListMainStore.kt) |
| `<Name>StoreFactory` | Builds a store | `impl.domain.store` | [AnimeListMainStoreFactory](../../feature-kmp/anime-list/src/commonMain/kotlin/com/alekseivinogradov/anoti/animelist/kmp/impl/domain/store/main/AnimeListMainStoreFactory.kt) |
| `<Name>Executor`, `<Name>ExecutorImpl`, `<Name>ExecutorFactory` | Executor typealias, executor, executor factory typealias | `api.domain.store`, `impl.domain.store` | [AnimeListExecutorImpl](../../feature-kmp/anime-list/src/commonMain/kotlin/com/alekseivinogradov/anoti/animelist/kmp/impl/domain/store/main/AnimeListExecutorImpl.kt) |
| `<Name>ReducerImpl` | Reducer | `impl.domain.store` | [AnimeListReducerImpl](../../feature-kmp/anime-list/src/commonMain/kotlin/com/alekseivinogradov/anoti/animelist/kmp/impl/domain/store/main/AnimeListReducerImpl.kt) |
| `<Feature>Source`, `<Feature>SourceImpl` | Source contract and implementation | `api.domain.source`, `impl.data.source` | [AnimeListSourceImpl](../../feature-kmp/anime-list/src/commonMain/kotlin/com/alekseivinogradov/anoti/animelist/kmp/impl/data/source/AnimeListSourceImpl.kt) |
| `<Verb><Object>Usecase` | Usecase: a concrete class used inside its module, or an exported interface | `impl.domain.usecase`, `api.domain.usecase` | [InsertAnimeDatabaseItemUsecase](../../core-kmp/anime-database/src/commonMain/kotlin/com/alekseivinogradov/anoti/animedatabase/kmp/api/domain/usecase/InsertAnimeDatabaseItemUsecase.kt) |
| `<Verb><Object>UsecaseImpl` | Implementation of an exported usecase | `impl.domain.usecase` | [InsertAnimeDatabaseItemUsecaseImpl](../../core-kmp/anime-database/src/commonMain/kotlin/com/alekseivinogradov/anoti/animedatabase/kmp/impl/domain/usecase/InsertAnimeDatabaseItemUsecaseImpl.kt) |
| `<Name>Usecases` | Data class bundling the usecases one store needs | `…usecase.wrapper` | [AnimeDatabaseUsecases](../../core-kmp/anime-database/src/commonMain/kotlin/com/alekseivinogradov/anoti/animedatabase/kmp/api/domain/usecase/wrapper/AnimeDatabaseUsecases.kt) |
| `<Name>Service`, `<Name>ServiceImpl` | Network service | `api.data.service`, `impl.data.service` | [ShikimoriApiService](../../feature-kmp/anime-base/src/commonMain/kotlin/com/alekseivinogradov/anoti/animebase/kmp/api/data/service/ShikimoriApiService.kt) |
| `<Name>Response` | Response model | `api.data.response` | [AnimeShortResponse](../../feature-kmp/anime-base/src/commonMain/kotlin/com/alekseivinogradov/anoti/animebase/kmp/api/data/response/AnimeShortResponse.kt) |
| `<Name>Data` | Enum of the network API's string values | `api.data.model` | [ReleaseStatusData](../../feature-kmp/anime-base/src/commonMain/kotlin/com/alekseivinogradov/anoti/animebase/kmp/api/data/model/ReleaseStatusData.kt) |
| `<Name>Domain` | Domain model | `api.domain.model` | [ListItemDomain](../../feature-kmp/anime-list/src/commonMain/kotlin/com/alekseivinogradov/anoti/animelist/kmp/api/domain/model/ListItemDomain.kt) |
| `<Name>DbDomain`, `<Name>Db`, `<Name>DbEntity` | Db models: exported model, exported enum, Room entity | `api.domain.model`, `impl.data.model` | [AnimeDbDomain](../../core-kmp/anime-database/src/commonMain/kotlin/com/alekseivinogradov/anoti/animedatabase/kmp/api/domain/model/AnimeDbDomain.kt) |
| `<Name>Repository`, `<Name>RepositoryImpl`, `<Name>Dao` | Persistence | `api.domain.repository`, `impl.data…` | [AnimeDatabaseRepository](../../core-kmp/anime-database/src/commonMain/kotlin/com/alekseivinogradov/anoti/animedatabase/kmp/api/domain/repository/AnimeDatabaseRepository.kt) |
| `<From>To<To>Mapper.kt`, `<Module>DatabaseMapper.kt` | Files of top-level mapping functions; which mappers exist is in [mvi.md](mvi.md). As found, one Db mapper file fits neither form. Example: [AnimeDbMapper](../../core-kmp/anime-database/src/commonMain/kotlin/com/alekseivinogradov/anoti/animedatabase/kmp/impl/data/mapper/AnimeDbMapper.kt) | `…mapper` | [DatabaseStoreStateToMainStoreIntentMapper](../../feature-kmp/anime-list/src/commonMain/kotlin/com/alekseivinogradov/anoti/animelist/kmp/api/domain/mapper/store/DatabaseStoreStateToMainStoreIntentMapper.kt) |
| `<Name>Manager`, `<Name>Scheduler`, `<Name>Provider` | Platform service contract | `api.domain…`, `api.presentation.provider` | [AnimeNotificationManager](../../feature-kmp/anime-notification/src/commonMain/kotlin/com/alekseivinogradov/anoti/animenotification/kmp/api/domain/manager/AnimeNotificationManager.kt) |
| `…Impl` of the above | Implementation; per platform when it needs a platform API | platform `impl` package | [AnimeNotificationManagerImpl](../../feature-kmp/anime-notification/src/androidMain/kotlin/com/alekseivinogradov/anoti/animenotification/android/impl/presentation/manager/AnimeNotificationManagerImpl.kt) |
| `<Name>Factory` | Builds one platform object | `…factory` | [AnimeNotificationChannelFactory](../../feature-kmp/anime-notification/src/androidMain/kotlin/com/alekseivinogradov/anoti/animenotification/android/impl/presentation/factory/AnimeNotificationChannelFactory.kt) |
| `<Name>Worker` | WorkManager worker | `android.impl.domain.worker` | [AnimeUpdateWorker](../../feature-kmp/anime-background-update/src/androidMain/kotlin/com/alekseivinogradov/anoti/animebackgroundupdate/android/impl/domain/worker/AnimeUpdateWorker.kt) |
| `<Module>Consts.kt`, `<Module>Dimens.kt` | Constants files; see [ui-compose.md](ui-compose.md), section "Design tokens" | `api.domain` (`internal` constants beside their user in `impl`), `api.presentation.compose` | [AnimeListDimens](../../feature-kmp/anime-list/src/commonMain/kotlin/com/alekseivinogradov/anoti/animelist/kmp/api/presentation/compose/AnimeListDimens.kt) |
| `<Name>Fake` | Test double; see [testing.md](testing.md), section "Test doubles" | `…fake` | [SafeApiFake](../../core-kmp/network/src/commonMain/kotlin/com/alekseivinogradov/anoti/network/kmp/impl/data/fake/SafeApiFake.kt) |
| `<Class>Test`, `<Name>DeviceTest` | Test class; instrumented test | test source sets | [AnimeNotificationIdsDeviceTest](../../feature-kmp/anime-notification/src/androidDeviceTest/kotlin/com/alekseivinogradov/anoti/animenotification/android/impl/presentation/manager/AnimeNotificationIdsDeviceTest.kt) |

Platform counterparts:

- A platform implementation has the same simple name on each platform, in that platform's
  package.
  Example: [AnimeNotificationManagerImpl (Android)](../../feature-kmp/anime-notification/src/androidMain/kotlin/com/alekseivinogradov/anoti/animenotification/android/impl/presentation/manager/AnimeNotificationManagerImpl.kt),
  [AnimeNotificationManagerImpl (iOS)](../../feature-kmp/anime-notification/src/iosMain/kotlin/com/alekseivinogradov/anoti/animenotification/ios/impl/presentation/manager/AnimeNotificationManagerImpl.kt).
- The file suffixes `<Name>.android.kt` and `<Name>.ios.kt` are used only for `actual`
  declarations and for platform builder files.
  Example: [PlatformContext.android.kt](../../core-kmp/di-scope/src/androidMain/kotlin/com/alekseivinogradov/anoti/di/kmp/PlatformContext.android.kt),
  [AnimeDatabase.android.kt](../../core-kmp/anime-database/src/androidMain/kotlin/com/alekseivinogradov/anoti/animedatabase/android/impl/data/AnimeDatabase.android.kt).
- An iOS-only type with no Android twin of the same name starts with `Ios`.
  Example: [IosScreenHost](../../main/src/iosMain/kotlin/com/alekseivinogradov/anoti/main/api/presentation/IosScreenHost.kt).
- As found: three iOS-only helpers have no `Ios` prefix.
  Example: [ChildLifecycle](../../main/src/iosMain/kotlin/com/alekseivinogradov/anoti/main/impl/presentation/lifecycle/ChildLifecycle.kt).

## Manifests and resources

- A KMP module's manifest is `src/androidMain/AndroidManifest.xml`. It declares the components and
  permissions its own code owns.
  Example: [entry module manifest](../../main/src/androidMain/AndroidManifest.xml)
  (the activity),
  [anime-notification manifest](../../feature-kmp/anime-notification/src/androidMain/AndroidManifest.xml)
  (`POST_NOTIFICATIONS`).
- App-wide entries live in the Android host's manifest: `INTERNET`, `AD_ID`, the `Application`
  class, and the removal of WorkManager's default initializer.
  Example: [androidApp manifest](../../androidApp/src/main/AndroidManifest.xml).
- A WorkManager worker needs no manifest entry.
- As found, two modules carry an empty `<manifest />`.
  Example: [anime-background-update manifest](../../feature-kmp/anime-background-update/src/androidMain/AndroidManifest.xml).

A module with Compose resources sets them up in its build file:

- `androidResources { enable = true }` inside `kotlin { android { } }`.
- A `compose.resources { }` block with `packageOfResClass =
  "<root-package>.<module-id>.kmp.generated.resources"`.
- `publicResClass = true` only where another module or a test in another module reads its `Res`.
  Otherwise `false`.
  Example: [UI kit build file](../../core-kmp/celebrity/build.gradle.kts) (public),
  [anime-list build file](../../feature-kmp/anime-list/build.gradle.kts) (not public).

How the generated `Res` reaches each platform is in [kmp.md](kmp.md), section "Resources". How
screens use strings, icons and fonts is in [ui-compose.md](ui-compose.md), section "Resources and
localization".
