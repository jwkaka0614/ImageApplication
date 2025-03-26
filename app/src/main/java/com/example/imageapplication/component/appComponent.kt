package com.example.imageapplication.component

import com.example.imageapplication.MainActivity
import com.example.imageapplication.module.AppModule
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class])
interface appComponent {
    fun inject(app: MainActivity)

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance app: MainActivity): appComponent
    }

}