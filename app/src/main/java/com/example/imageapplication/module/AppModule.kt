package com.example.imageapplication.module

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.imageapplication.ImageViewModel
import com.example.imageapplication.manager.FolderRepository
import com.example.imageapplication.manager.ImageManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap

@Module
class AppModule {
    @Provides
    fun provideApplicationContext(application: Application): Context = application.applicationContext

    @Provides
    fun provideImageManager(context: Context): ImageManager = ImageManager(context)

    @Provides
    fun provideFolderRepository(context: Context): FolderRepository = FolderRepository(context)
}