package com.example.imageapplication

import android.app.Application
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.imageapplication.component.DaggerTestAppComponent
import com.example.imageapplication.component.TestAppComponent
import com.example.imageapplication.module.TestAppModule
import org.junit.runner.RunWith

class TestApplication : Application(), AppInjectionProvider {

    private lateinit var testAppComponent: TestAppComponent

    override fun onCreate() {
        super.onCreate()
        testAppComponent = DaggerTestAppComponent.factory().create(this)
    }

    override fun inject(activity: MainActivity) {
        testAppComponent.inject(activity)
    }
}