package com.alekseivinogradov.network.impl.presentation.di

import com.alekseivinogradov.network.api.data.SafeApi
import com.alekseivinogradov.network.impl.data.SafeApiImpl
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton
import kotlin.time.Duration.Companion.milliseconds

@Module
interface NetworkModule {
    companion object {
        @Provides
        @Singleton
        fun provideMoshi(): Moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        @Provides
        @Singleton
        fun provideRetrofitBuilder(
            moshi: Moshi
        ): Retrofit.Builder = Retrofit.Builder()
            .addConverterFactory(MoshiConverterFactory.create(moshi))

        @Provides
        @Singleton
        fun provideSafeApi(): SafeApi = SafeApiImpl(
            maxAttempt = 3,
            attemptDelay = 2500.milliseconds
        )
    }
}
