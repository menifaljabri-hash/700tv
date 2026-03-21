package com.iptv.player.di

import com.iptv.player.data.api.XtreamApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /**
     * NOTE: The base URL is a placeholder. The actual server URL is injected
     * per-request via a dynamic Retrofit instance (see DynamicRetrofitFactory).
     * This singleton handles the default/initial setup.
     */
    private const val DEFAULT_BASE_URL = "http://localhost/"

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
        )
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", "IPTVPlayer-Android/1.0")
                .build()
            chain.proceed(request)
        }
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(DEFAULT_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Provides
    @Singleton
    fun provideXtreamApiService(retrofit: Retrofit): XtreamApiService =
        retrofit.create(XtreamApiService::class.java)
}

// ── Dynamic Retrofit factory for per-playlist base URLs ──────────────────────

@Module
@InstallIn(SingletonComponent::class)
object DynamicRetrofitModule {

    @Provides
    @Singleton
    fun provideDynamicRetrofitFactory(okHttpClient: OkHttpClient): DynamicRetrofitFactory =
        DynamicRetrofitFactory(okHttpClient)
}

class DynamicRetrofitFactory(private val okHttpClient: OkHttpClient) {
    fun create(baseUrl: String): XtreamApiService {
        val safeUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        return Retrofit.Builder()
            .baseUrl(safeUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(XtreamApiService::class.java)
    }
}
