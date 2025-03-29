package com.example.imageapplication.module

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.imageapplication.ImageViewModel
import com.example.imageapplication.manager.FolderRepository
import com.example.imageapplication.manager.IImageManager
import com.example.imageapplication.manager.ImageManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import javax.inject.Singleton

@Module
class AppModule {
    @Provides
    @Singleton
    fun provideApplicationContext(application: Application): Context = application.applicationContext

    @Provides
    @Singleton
    fun provideImageManager(context: Context): IImageManager = ImageManager(context)

    @Provides
    @Singleton
    fun provideFolderRepository(context: Context): FolderRepository = FolderRepository(context)
}