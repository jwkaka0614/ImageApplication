package com.example.imageapplication.module

import androidx.lifecycle.ViewModel
import com.example.imageapplication.ImageViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ViewModelModule {
    @Binds
    @IntoMap
    @ViewModelKey(ImageViewModel::class)
    abstract fun bindImageViewModel(imageViewModel: ImageViewModel): ViewModel

}