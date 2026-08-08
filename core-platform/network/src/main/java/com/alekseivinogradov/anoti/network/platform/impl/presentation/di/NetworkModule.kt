package com.alekseivinogradov.anoti.network.platform.impl.presentation.di

import com.alekseivinogradov.anoti.network.kmp.api.data.SafeApi
import com.alekseivinogradov.anoti.network.platform.impl.data.SafeApiImpl
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import javax.inject.Singleton
import kotlin.time.Duration.Companion.milliseconds
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

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
