# Recipes

The order of steps for changes that span several areas. Each step says what to do and links the
section that says how; a change inside one area follows that area's file directly.

Read when: a change spans several areas, as each heading below names one.

## New screen feature

1. Create the module as a screen feature: [new-module.md](new-module.md).
2. Write the main store: [mvi.md](mvi.md).
3. Reach data: [data-layer.md "Sources and usecases"](data-layer.md#sources-and-usecases).
4. Feature graph: [dependency-injection.md](dependency-injection.md#component-hierarchy),
   section "Component hierarchy".
5. Controller and UI mapper: [mvi.md "Controllers"](mvi.md#controllers),
   [mvi.md "Mappers and UI models"](mvi.md#mappers-and-ui-models).
6. Screen and route: [ui-compose.md "Screens and routes"](ui-compose.md#screens-and-routes).
7. Values and text: [ui-compose.md "Design tokens"](ui-compose.md#design-tokens),
   [ui-compose.md "Resources and localization"](ui-compose.md#resources-and-localization).
8. Scale, targets, semantics:
   [accessibility-and-adaptive-layout.md](accessibility-and-adaptive-layout.md).
9. Screen component: [navigation.md "Screen components"](navigation.md#screen-components),
   [state-restoration.md "Adding saved state"](state-restoration.md#adding-saved-state).
10. Register it: [navigation.md "Adding a destination"](navigation.md#adding-a-destination).
11. Tests and docs: [testing.md](testing.md), [testing-platforms.md](testing-platforms.md#android),
    [tests.md](../rules/tests.md), [module-docs.md](../rules/module-docs.md).

## Root-level element or overlay

An element with a store of its own, beside the stack:

1. Create the module as a root-level element: [new-module.md](new-module.md).
2. A store, and a controller passing labels to a callback: [mvi.md](mvi.md#controllers).
3. Its module component, mixed into the root graph:
   [dependency-injection.md](dependency-injection.md#module-and-platform-components), section
   "Module and platform components".
4. The root host reads the store, builds the controller, disposes the store; the route sits in
   the entry module; root content gets the controller in the root's bundle and places the route;
   a navigating element maps to the stack both ways:
   [navigation.md "Root-level elements"](navigation.md#root-level-elements).
5. Insets:
   [accessibility-and-adaptive-layout.md "Insets"](accessibility-and-adaptive-layout.md#insets).

An overlay over the root, such as a dialog:

1. The composable in a UI-only component module: [new-module.md](new-module.md), skeleton (d).
2. Its state holder in the entry module, built by the root host:
   [navigation.md "Root-level elements"](navigation.md#root-level-elements),
   [module-anatomy.md "Naming"](module-anatomy.md#naming).
3. Drawn by root content in its own small composable:
   [ui-compose.md "Recomposition"](ui-compose.md#recomposition).
4. Handed to root content by both screen hosts:
   [platform-mirroring.md "Mirror table"](platform-mirroring.md#mirror-table).

Either way, test and document: [testing.md](testing.md),
[testing-platforms.md](testing-platforms.md#android), [module-docs.md](../rules/module-docs.md).
Example: [RootHostTest](../../main/src/commonTest/kotlin/com/alekseivinogradov/anoti/main/impl/presentation/RootHostTest.kt).

## New store in a screen

1. Contract, factory, executor, reducer: [mvi.md](mvi.md), named per
   [module-anatomy.md "Naming"](module-anatomy.md#naming).
2. Bootstrapper: [state-restoration.md](state-restoration.md#screen-restore-protocol), section
   "Screen restore protocol".
3. Usecases: [data-layer.md "Sources and usecases"](data-layer.md#sources-and-usecases).
4. Unscoped feature-graph binding:
   [dependency-injection.md "Scopes"](dependency-injection.md#scopes).
5. Read once, disposed by the screen component:
   [concurrency-and-lifecycle.md "Disposal"](concurrency-and-lifecycle.md#disposal).
6. Bound to the main store: [mvi.md "Main and section stores"](mvi.md#main-and-section-stores).
7. Saved state, if any:
   [state-restoration.md "Adding saved state"](state-restoration.md#adding-saved-state).
8. Tests: [testing.md "Virtual time"](testing.md#virtual-time), [tests.md](../rules/tests.md).

## Paged list

1. A source taking a page:
   [data-layer.md "Sources and usecases"](data-layer.md#sources-and-usecases).
2. A paginator in the executor: [data-layer.md "Paging"](data-layer.md#paging),
   [mvi.md "Executors and state"](mvi.md#executors-and-state).
3. Page results to messages:
   [error-handling.md "Failure to message"](error-handling.md#failure-to-message).
4. Item keys: [ui-compose.md "Lists"](ui-compose.md#lists); next page from the scroll position:
   [ui-compose.md "Recomposition"](ui-compose.md#recomposition).
5. Keep the scroll position, not the items:
   [state-restoration.md "What is never saved"](state-restoration.md#what-is-never-saved).
6. Tests for first page, next page, refresh and failures: [testing.md](testing.md).

## Network endpoint

1. Response model, `*Data` enums and the call in the shared base module's service:
   [data-layer.md "Network"](data-layer.md#network).
2. Domain model, mapper, source method and usecase, bound in DI:
   [data-layer.md "Sources and usecases"](data-layer.md#sources-and-usecases).
3. The executor's call and its failures; the response as outside input:
   [error-handling.md "Failure to message"](error-handling.md#failure-to-message),
   [security-and-privacy.md "Untrusted input"](security-and-privacy.md#untrusted-input).
4. Tests with `MockEngine`: [testing.md "Test doubles"](testing.md#test-doubles),
   [testing.md "Virtual time"](testing.md#virtual-time).
5. Serialized classes are reached by name: [r8-minified.md](../rules/android/r8-minified.md).

## Database change

1. Entity, DAO, repository: [data-layer.md "Persistence"](data-layer.md#persistence).
2. Version bump, migration and continuity tests on both platforms:
   [data-layer.md "Schema changes"](data-layer.md#schema-changes).
3. `*Db*` models and feature mappers:
   [data-layer.md "Sources and usecases"](data-layer.md#sources-and-usecases).
4. Exposed through the persistence store or an `api` usecase; Room is reached by name:
   [data-layer.md](data-layer.md#persistence), [r8-minified.md](../rules/android/r8-minified.md).

## App-wide or root-scoped binding

1. Scope: [dependency-injection.md "Scopes"](dependency-injection.md#scopes).
2. A `@Provides` in the owning module's component, or in its platform component pair:
   [dependency-injection.md](dependency-injection.md#module-and-platform-components), section
   "Module and platform components", and its "Skeletons".
3. Mixed into both app graph twins or the root graph, and listed in each dependencies contract
   that reads it: [dependency-injection.md](dependency-injection.md#component-hierarchy), section
   "Component hierarchy".
4. Deferred access where the platform reaches back:
   [dependency-injection.md "Bindings"](dependency-injection.md#bindings).
5. Tests and the iOS wiring check: [build-check.md](../rules/ios/build-check.md),
   [dependency-injection.md "Testing a component"](dependency-injection.md#testing-a-component).

## Platform-specific implementation

1. Confirm the platform API, and build what both share once in `commonMain`:
   [source-sets.md](../rules/source-sets.md), [kmp.md "Where code lives"](kmp.md#where-code-lives).
2. Contract in `commonMain`: [kmp.md "Expect and actual"](kmp.md#expect-and-actual).
3. Twins under one simple name: [module-anatomy.md "Naming"](module-anatomy.md#naming),
   [platform-mirroring.md "Twins"](platform-mirroring.md#twins).
4. Bound in the platform components:
   [dependency-injection.md](dependency-injection.md#module-and-platform-components), section
   "Module and platform components".
5. A new role or difference:
   [platform-mirroring.md "Mirror table"](platform-mirroring.md#mirror-table),
   [platform-mirroring.md "Accepted asymmetries"](platform-mirroring.md#accepted-asymmetries).
6. Tests on each side: [testing-platforms.md](testing-platforms.md), [tests.md](../rules/tests.md).
7. iOS check: [build-check.md](../rules/ios/build-check.md).

## Open a screen from a notification

1. A root config with a stable name: [navigation.md "Root stack"](navigation.md#root-stack).
2. A tap target in the entry module, delivered to the live root:
   [navigation.md "Deep links"](navigation.md#deep-links).
3. Payload or intent through external contracts:
   [project-structure.md "How modules talk"](project-structure.md#how-modules-talk).
4. Their implementations per platform, and the payload read as outside input:
   [platform-mirroring.md "Accepted asymmetries"](platform-mirroring.md#accepted-asymmetries),
   [security-and-privacy.md "Untrusted input"](security-and-privacy.md#untrusted-input).
5. Each restore case: [state-restoration.md "Scenarios"](state-restoration.md#scenarios).
6. Tests in the root host's suite: [testing.md](testing.md).

## Background work

1. The pass itself in `commonMain`: [testing-platforms.md "iOS"](testing-platforms.md#ios),
   [kmp.md "Where code lives"](kmp.md#where-code-lives).
2. Data through `api` usecases: [data-layer.md "Persistence"](data-layer.md#persistence).
3. Its context and failures:
   [concurrency-and-lifecycle.md "Scope owners"](concurrency-and-lifecycle.md#scope-owners),
   [error-handling.md "Failure to message"](error-handling.md#failure-to-message).
4. Scheduled per platform, at launch:
   [platform-mirroring.md "Mirror table"](platform-mirroring.md#mirror-table),
   [platform-mirroring.md "Startup"](platform-mirroring.md#startup).
5. Deferred graph access: [dependency-injection.md "Bindings"](dependency-injection.md#bindings).
6. The iOS task identifier: [ios-host.md "Property list"](ios-host.md#property-list).
7. Log line, R8 (workers are reached by name) and tests: [logging.md](../rules/logging.md),
   [r8-minified.md](../rules/android/r8-minified.md), [testing-platforms.md](testing-platforms.md).

## Notification

1. Posting contract and twins:
   [platform-mirroring.md "Mirror table"](platform-mirroring.md#mirror-table).
2. Text and ids built once: [kmp.md "Where code lives"](kmp.md#where-code-lives),
   [ui-compose.md "Resources and localization"](ui-compose.md#resources-and-localization).
3. The Android channel at start-up:
   [platform-mirroring.md "Startup"](platform-mirroring.md#startup).
4. The permission: [security-and-privacy.md "Permissions"](security-and-privacy.md#permissions),
   [security-and-privacy.md](security-and-privacy.md#asking-for-a-permission), section "Asking for
   a permission".
5. A tap: "Open a screen from a notification" above. Log line: [logging.md](../rules/logging.md).
6. A post only a device shows:
   [android/instrumented-tests.md](../rules/android/instrumented-tests.md).

## New library

1. Nothing already does the job: [principles.md "No reinventing"](principles.md#no-reinventing),
   [decisions.md](decisions.md).
2. Add it: [tech-stack.md "Adding a library"](tech-stack.md#adding-a-library).

## Shared constant, token or string

1. File kind and module: [ui-compose.md "Design tokens"](ui-compose.md#design-tokens).
2. Its name: [code-style.md "Naming"](code-style.md#naming).
3. A string or an icon:
   [ui-compose.md "Resources and localization"](ui-compose.md#resources-and-localization).
4. `Res` open to other modules only when they read it:
   [module-anatomy.md "Manifests and resources"](module-anatomy.md#manifests-and-resources).
5. Wording both platforms build: [kmp.md "Where code lives"](kmp.md#where-code-lives).
