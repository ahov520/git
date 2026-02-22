package com.antihub.mobile.di

import android.content.Context
import androidx.room.Room
import com.antihub.mobile.core.config.AppConfig
import com.antihub.mobile.core.network.AuthInterceptor
import com.antihub.mobile.data.auth.AuthManager
import com.antihub.mobile.data.auth.AuthManagerImpl
import com.antihub.mobile.data.local.AppDatabase
import com.antihub.mobile.data.local.SecurePreferences
import com.antihub.mobile.data.local.TranslationCacheDao
import com.antihub.mobile.data.remote.AuthApiService
import com.antihub.mobile.data.remote.GitHubApiService
import com.antihub.mobile.data.remote.TranslationApiService
import com.antihub.mobile.data.repository.GitHubRepositoryImpl
import com.antihub.mobile.data.repository.TranslationRepositoryImpl
import com.antihub.mobile.domain.repository.GitHubRepository
import com.antihub.mobile.domain.repository.TranslationRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import javax.inject.Qualifier
import javax.inject.Singleton
import okhttp3.MediaType.Companion.toMediaType

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthorizedClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PlainClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class GitHubRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class TranslationRetrofit

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    @Provides
    @Singleton
    fun provideAuthInterceptor(
        securePreferences: SecurePreferences,
    ): AuthInterceptor = AuthInterceptor(securePreferences)

    @Provides
    @Singleton
    @AuthorizedClient
    fun provideAuthorizedClient(
        authInterceptor: AuthInterceptor,
    ): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    @PlainClient
    fun providePlainClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    @GitHubRetrofit
    fun provideGitHubRetrofit(
        json: Json,
        @AuthorizedClient client: OkHttpClient,
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(AppConfig.githubApiBaseUrl)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    @Provides
    @Singleton
    @AuthRetrofit
    fun provideAuthRetrofit(
        json: Json,
        @PlainClient client: OkHttpClient,
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(AppConfig.authProxyBaseUrl)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    @Provides
    @Singleton
    @TranslationRetrofit
    fun provideTranslationRetrofit(
        json: Json,
        @PlainClient client: OkHttpClient,
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(AppConfig.translationApiBaseUrl)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    @Provides
    @Singleton
    fun provideGitHubApiService(
        @GitHubRetrofit retrofit: Retrofit,
    ): GitHubApiService = retrofit.create(GitHubApiService::class.java)

    @Provides
    @Singleton
    fun provideAuthApiService(
        @AuthRetrofit retrofit: Retrofit,
    ): AuthApiService = retrofit.create(AuthApiService::class.java)

    @Provides
    @Singleton
    fun provideTranslationApiService(
        @TranslationRetrofit retrofit: Retrofit,
    ): TranslationApiService = retrofit.create(TranslationApiService::class.java)

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "github_mobile.db",
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideTranslationCacheDao(database: AppDatabase): TranslationCacheDao = database.translationCacheDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class BindModule {

    @Binds
    @Singleton
    abstract fun bindAuthManager(impl: AuthManagerImpl): AuthManager

    @Binds
    @Singleton
    abstract fun bindGitHubRepository(impl: GitHubRepositoryImpl): GitHubRepository

    @Binds
    @Singleton
    abstract fun bindTranslationRepository(impl: TranslationRepositoryImpl): TranslationRepository
}
