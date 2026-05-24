package com.spc.nutricoach.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.spc.nutricoach.data.api.AuthApiService
import com.spc.nutricoach.data.api.ClienteApiService
import com.spc.nutricoach.data.api.DietaApiService
import com.spc.nutricoach.data.api.RutinaApiService
import com.spc.nutricoach.data.api.ComunidadApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "https://api-nutri-coach.onrender.com/"

    @Provides
    @Singleton
    fun provideJson(): Json = Json { ignoreUnknownKeys = true }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, json: Json): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthApiService(retrofit: Retrofit): AuthApiService {
        return retrofit.create(AuthApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideDietaApiService(retrofit: Retrofit): DietaApiService {
        return retrofit.create(DietaApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideRutinaApiService(retrofit: Retrofit): RutinaApiService {
        return retrofit.create(RutinaApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideClienteApiService(retrofit: Retrofit): ClienteApiService {
        return retrofit.create(ClienteApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideComunidadApiService(retrofit: Retrofit): ComunidadApiService {
        return retrofit.create(ComunidadApiService::class.java)
    }
}
