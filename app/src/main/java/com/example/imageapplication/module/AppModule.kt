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
abstract class AppModule(private val application: Application) {

    @Provides
    fun provideApplicationContext(): Context = application.applicationContext

    @Binds
    @IntoMap
    @ViewModelKey(ImageViewModel::class)
    abstract fun bindImageViewModel(imageViewModel: ImageViewModel): ViewModel


    @Provides
    fun provideImageManager(context: Context): ImageManager {
        return ImageManager(context)
    }

    @Provides
    fun provideFolderRepository(context: Context): FolderRepository {
        return FolderRepository(context)
    }

}