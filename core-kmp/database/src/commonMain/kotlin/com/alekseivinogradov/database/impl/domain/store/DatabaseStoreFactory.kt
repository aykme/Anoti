package com.alekseivinogradov.database.impl.domain.store

import com.alekseivinogradov.database.api.domain.repository.AnimeDatabaseRepository
import com.alekseivinogradov.database.api.domain.store.DatabaseExecutor
import com.alekseivinogradov.database.api.domain.store.DatabaseStore
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory

class DatabaseStoreFactory(
    private val storeFactory: StoreFactory,
    repository: AnimeDatabaseRepository
) {
    private val executorFactory: () -> DatabaseExecutor = {
        DatabaseExecutorImpl(repository = repository)
    }

    fun create(): DatabaseStore {
        return object : DatabaseStore,
            Store<DatabaseStore.Intent, DatabaseStore.State, DatabaseStore.Label>
            by storeFactory.create(
                name = "DatabaseStore",
                initialState = DatabaseStore.State(),
                bootstrapper = SimpleBootstrapper(DatabaseStore.Action.SubscribeToDatabase),
                executorFactory = executorFactory,
                reducer = DatabaseReducerImpl()
            ) {}
    }
}
