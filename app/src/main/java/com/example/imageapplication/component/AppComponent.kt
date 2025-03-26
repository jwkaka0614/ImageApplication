package com.example.imageapplication.component

import android.app.Application
import com.example.imageapplication.MainActivity
import com.example.imageapplication.module.AppModule
import com.example.imageapplication.module.ViewModelModule
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class, ViewModelModule::class])
interface AppComponent {
    fun inject(activity: MainActivity)

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance application: Application): AppComponent
    }

}