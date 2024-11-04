package au.com.shiftyjelly.pocketcasts.repositories.utils

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DownloadUtil {
    @Provides
    @Singleton
    fun provideRequestBuilderProvider(): Provider<Request.Builder> {
        return Provider { Request.Builder() }
    }

    @Provides
    @Singleton
    fun provideCallFactory(): Call.Factory {
        return OkHttpClient()
    }
}