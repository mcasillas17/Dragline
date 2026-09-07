package com.mcasillas.dragline.di

import com.mcasillas.dragline.audio.FakeSpotifyProvider
import com.mcasillas.dragline.audio.LocalSoundProvider
import com.mcasillas.dragline.audio.SoundProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AudioModule {

    @Provides
    @Singleton
    fun provideSoundProviders(
        local: LocalSoundProvider,
        spotify: FakeSpotifyProvider
    ): List<SoundProvider> {
        return listOf(local, spotify)
    }
}
