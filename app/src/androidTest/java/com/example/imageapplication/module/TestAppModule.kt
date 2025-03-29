package com.example.imageapplication.module

import android.app.Application
import android.content.Context
import com.example.imageapplication.manager.FolderRepository
import com.example.imageapplication.manager.IImageManager
import com.example.imageapplication.manager.TestImageManager
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class TestAppModule() {

    @Provides
    @Singleton
    fun provideApplicationContext(application: Application): Context = application.applicationContext

    @Provides
    @Singleton
    fun provideImageManager(context: Context): IImageManager = TestImageManager(context)

    @Provides
    @Singleton
    fun provideFolderRepository(context: Context): FolderRepository = FolderRepository(context)
}