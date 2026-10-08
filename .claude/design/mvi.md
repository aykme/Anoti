# MVI

How a store is built with MVIKotlin: its contract, factory, executor and reducer, the controller
that wires stores to each other and to the UI, and the mappers between them. Names follow
[module-anatomy.md](module-anatomy.md), section "Naming"; tests, [testing.md](testing.md).

Read when: adding or changing a store, an executor, a reducer, a controller, or a mapper between
stores or from a store's state to a UI model.

## Store contract

- A store is an interface in `api/domain/store` extending MVIKotlin's `Store<Intent, State, Label>`.
  Its five types are nested in it, each with KDoc.
- `State` is a `data class` with a default for every field. `State()` is the initial state.
- `Intent`, `Label`, `Action` and `Message` are sealed interfaces of `data class` and
  `data object` members. Callers send intents through `accept` and react to labels.
- `Action` comes from the bootstrapper; `Message` goes from the executor to the reducer. Callers
  never send either. A store without bootstrapper actions keeps an empty `sealed interface Action`.
- Which bootstrapper a store gets is set by
  [state-restoration.md](state-restoration.md), section "Screen restore protocol".

```kotlin
interface <Feature>MainStore :
    Store<<Feature>MainStore.Intent, <Feature>MainStore.State, <Feature>MainStore.Label> {
    data class State(val items: List<<Name>Domain> = listOf())
    sealed interface Intent {
        data object OpenSection : Intent
        data class ItemClick(val id: Int) : Intent
    }
    sealed interface Label { data class ItemClick(val id: Int) : Label }
    sealed interface Action
    sealed interface Message { data class UpdateItems(val items: List<<Name>Domain>) : Message }
}
```

Mirrors: [AnimeFavoritesMainStore](../../feature-kmp/anime-favorites/src/commonMain/kotlin/com/alekseivinogradov/anoti/animefavorites/kmp/api/domain/store/AnimeFavoritesMainStore.kt)

## Store factory

- `<Feature>MainStoreFactory` in `impl/domain/store` takes the injected `StoreFactory` and the
  executor factory. `create()` returns an object that implements the store interface by delegation.
- `StoreFactory` is the app-wide `DefaultStoreFactory` binding of the UI kit module.
  Example: [DiCelebrityComponent](../../core-kmp/celebrity/src/commonMain/kotlin/com/alekseivinogradov/anoti/celebrity/kmp/impl/di/DiCelebrityComponent.kt)
- A store's binding and disposal:
  [dependency-injection.md "Scopes"](dependency-injection.md#scopes),
  [concurrency-and-lifecycle.md "Disposal"](concurrency-and-lifecycle.md#disposal).

```kotlin
class <Feature>MainStoreFactory(
    private val storeFactory: StoreFactory,
    private val executorFactory: <Feature>ExecutorFactory,
) {
    fun create(): <Feature>MainStore = object :
        <Feature>MainStore,
        Store<<Feature>MainStore.Intent, <Feature>MainStore.State, <Feature>MainStore.Label>
        by storeFactory.create(
            name = "<Feature>MainStore",
            initialState = <Feature>MainStore.State(),
            bootstrapper = SimpleBootstrapper(),
            executorFactory = executorFactory,
            reducer = <Feature>ReducerImpl()
        ) {}
}
```

Mirrors: [AnimeFavoritesMainStoreFactory](../../feature-kmp/anime-favorites/src/commonMain/kotlin/com/alekseivinogradov/anoti/animefavorites/kmp/impl/domain/store/AnimeFavoritesMainStoreFactory.kt)

## Executors and state

- Executors are pure orchestration. Mutable data that reflects real application state (a flag, a
  counter, a "has this happened before" marker) lives in the Store's `State`, changed only through
  a `Message`/reducer. Never as a bare `private var` field on the Executor.
- An executor reaches a platform service only through a usecase that fronts it, as it reaches
  data.
- A `private var` on an Executor is acceptable only for non-observable coroutine plumbing that
  isn't application state: a `Job` handle, a `MutableStateFlow` used to debounce/trigger work, a
  `Paginator` instance. Never for anything the reducer or UI would otherwise need to reason about.
- When a decision needs "has this already happened", derive it from an existing `State` field
  (e.g. a `contentType` still at its untouched default). Don't add a tracking property for that
  one case.

How an executor is written:

- `internal typealias <Feature>Executor = CoroutineExecutor<Intent, Action, State, Message, Label>`
  sits next to the store contract. `<Feature>ExecutorImpl` in `impl/domain/store` extends it.
- Its `mainContext` is `coroutineContextProvider.newMainCoroutineContext()`. What that context
  carries is owned by
  [concurrency-and-lifecycle.md "Coroutine contexts"](concurrency-and-lifecycle.md#coroutine-contexts).
- As found: an executor that launches no coroutine keeps `CoroutineExecutor`'s default context.
  Example: [BottomNavigationBarExecutorImpl](../../feature-kmp/bottom-navigation-bar/src/commonMain/kotlin/com/alekseivinogradov/anoti/bottomnavigationbar/kmp/impl/domain/store/BottomNavigationBarExecutorImpl.kt)
- `executeIntent` and `executeAction` are exhaustive `when`s, one private function per case. The
  executor reads `state()`, dispatches messages, publishes labels, and launches work in `scope`.
- Dependencies come through the constructor: the `CoroutineContextProvider`, a `*Usecases` wrapper,
  and the `SystemMessageProvider`. Usecases are owned by [data-layer.md](data-layer.md).
- The executor factory is `typealias <Feature>ExecutorFactory = () -> <Feature>ExecutorImpl`. The
  feature graph provides it as a lambda that builds the executor.
  Example: [DiAnimeFavoritesComponent](../../feature-kmp/anime-favorites/src/commonMain/kotlin/com/alekseivinogradov/anoti/animefavorites/kmp/impl/di/DiAnimeFavoritesComponent.kt)
- As found: only two modules declare that typealias; others pass the function type inline or a
  constructor reference.
  Example: [AnimeDatabaseStoreFactory](../../core-kmp/anime-database/src/commonMain/kotlin/com/alekseivinogradov/anoti/animedatabase/kmp/impl/domain/store/AnimeDatabaseStoreFactory.kt)

```kotlin
typealias <Feature>ExecutorFactory = () -> <Feature>ExecutorImpl

class <Feature>ExecutorImpl(
    coroutineContextProvider: CoroutineContextProvider,
    private val usecases: <Feature>Usecases,
    private val systemMessageProvider: SystemMessageProvider
) : <Feature>Executor(mainContext = coroutineContextProvider.newMainCoroutineContext()) {
    private var loadJob: Job? = null
    override fun executeIntent(intent: <Feature>MainStore.Intent) {
        when (intent) {
            <Feature>MainStore.Intent.OpenSection -> openSection()
            is <Feature>MainStore.Intent.ItemClick ->
                publish(<Feature>MainStore.Label.ItemClick(intent.id))
        }
    }
    private fun openSection() {
        loadJob?.cancel()
        loadJob = scope.launch {
            when (val result = usecases.fetch<Name>Usecase.execute()) {
                is CallResult.Success -> dispatch(<Feature>MainStore.Message.UpdateItems(result.value))
                is CallResult.HttpError,
                is CallResult.NetworkError -> systemMessageProvider.makeConnectionErrorSystemMessage()
                is CallResult.OtherError -> systemMessageProvider.makeUnknownErrorSystemMessage()
            }
        }
    }
}
```

Mirrors: [AnimeFavoritesExecutorImpl](../../feature-kmp/anime-favorites/src/commonMain/kotlin/com/alekseivinogradov/anoti/animefavorites/kmp/impl/domain/store/AnimeFavoritesExecutorImpl.kt)

## Reducer

- `internal class <Feature>ReducerImpl` in `impl/domain/store` implements `Reducer<State, Message>`.
- It is a pure function: an exhaustive `when` over messages, each branch returning `copy(...)`.

```kotlin
internal class <Feature>ReducerImpl : Reducer<<Feature>MainStore.State, <Feature>MainStore.Message> {
    override fun <Feature>MainStore.State.reduce(msg: <Feature>MainStore.Message) = when (msg) {
        is <Feature>MainStore.Message.UpdateItems -> copy(items = msg.items)
    }
}
```

Mirrors: [AnimeFavoritesReducerImpl](../../feature-kmp/anime-favorites/src/commonMain/kotlin/com/alekseivinogradov/anoti/animefavorites/kmp/impl/domain/store/AnimeFavoritesReducerImpl.kt)

## Controllers

- A controller wires the stores of one screen component or root-level element. Its owner builds it
  once and passes its lifecycle; see [navigation.md](navigation.md), section "Screen components".
- `state` is `mainStore.stateFlow(lifecycle)`. `accept` forwards an intent to the main store; the
  route passes `controller::accept` as the screen's `dispatch`.
- `bind(lifecycle, BinderLifecycleMode.START_STOP)` connects states and labels through mapper
  function references, so the wiring runs only while the owner is started.
- A root-level element's controller hands its labels to an `onLabel` callback instead of a store.
  Example: [BottomNavigationBarController](../../feature-kmp/bottom-navigation-bar/src/commonMain/kotlin/com/alekseivinogradov/anoti/bottomnavigationbar/kmp/impl/presentation/BottomNavigationBarController.kt)
- The controller disposes nothing. The owner that created the stores disposes them.

```kotlin
class <Feature>Controller(
    lifecycle: Lifecycle,
    private val mainStore: <Feature>MainStore,
    private val <name>Store: <Name>Store,
) {
    val state: StateFlow<<Feature>MainStore.State> = mainStore.stateFlow(lifecycle)
    init {
        bind(lifecycle, BinderLifecycleMode.START_STOP) {
            <name>Store.states.map(::map<Name>StoreStateToMainStoreIntent) bindTo mainStore
            mainStore.labels.map(::mapMainStoreLabelTo<Name>StoreIntent) bindTo <name>Store
        }
    }
    fun accept(intent: <Feature>MainStore.Intent) = mainStore.accept(intent)
}
```

Mirrors: [AnimeFavoritesController](../../feature-kmp/anime-favorites/src/commonMain/kotlin/com/alekseivinogradov/anoti/animefavorites/kmp/impl/presentation/AnimeFavoritesController.kt)

## Main and section stores

- A screen with sections has a main store, which the UI draws, and one store per section. The
  main store's labels reach a section through `mapNotNull` and a mapper that returns `null` for
  labels meant for other sections.
- Each section's state maps back to a main-store intent that carries the section's content.
- A shared store, such as the persistence store, joins the same way.
  Example: [AnimeListController](../../feature-kmp/anime-list/src/commonMain/kotlin/com/alekseivinogradov/anoti/animelist/kmp/impl/presentation/AnimeListController.kt)

## Mappers and UI models

- A store-to-store mapper is a top-level
  `internal fun map<Source>Store<State|Label>To<Target>StoreIntent` in `api/domain/mapper`. Its
  file drops the `map` prefix and adds `Mapper`: `<Source>StoreStateTo<Target>StoreIntentMapper.kt`.
  Example: [DatabaseStoreStateToMainStoreIntentMapper](../../feature-kmp/anime-favorites/src/commonMain/kotlin/com/alekseivinogradov/anoti/animefavorites/kmp/api/domain/mapper/DatabaseStoreStateToMainStoreIntentMapper.kt)
- State to UI: `internal fun mapStateToUiModel(state)` in
  `api/presentation/mapper/StateToUiModelMapper.kt`. The route calls it inside `remember(state)`;
  see [ui-compose.md](ui-compose.md), section "Screens and routes".
- The UI model is a `data class <Feature>UiModel` in `api/presentation/model`, with a default for
  every field; its parts are `*Ui` types. Stability is owned by [ui-compose.md](ui-compose.md),
  section "Stability".
  Example: [AnimeFavoritesUiModel](../../feature-kmp/anime-favorites/src/commonMain/kotlin/com/alekseivinogradov/anoti/animefavorites/kmp/api/presentation/model/AnimeFavoritesUiModel.kt)
- The UI mapper turns `*Domain` enums into `*Ui` enums with an exhaustive `when`, and builds the
  display strings. Mappers at the data boundary are owned by [data-layer.md](data-layer.md).
- A mapper converts and decides nothing. Which items show and in what order, a filter or a sort,
  is the store's state.
- As found: mapper file names and sub-packages vary between modules; follow the names above.
  Example: [MainStoreLabelToDatabaseStoreIntent](../../feature-kmp/anime-favorites/src/commonMain/kotlin/com/alekseivinogradov/anoti/animefavorites/kmp/api/domain/mapper/MainStoreLabelToDatabaseStoreIntent.kt)
