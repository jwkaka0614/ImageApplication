package com.example.imageapplication.component


import com.example.imageapplication.module.TestAppModule
import android.app.Application
import com.example.imageapplication.MainActivity
import com.example.imageapplication.module.ViewModelModule
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [TestAppModule::class, ViewModelModule::class])
interface TestAppComponent {
    // 注入至你的測試類別中（例如 ImageViewModelTest）
    fun inject(activity: MainActivity)

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance application: Application): TestAppComponent
    }
}