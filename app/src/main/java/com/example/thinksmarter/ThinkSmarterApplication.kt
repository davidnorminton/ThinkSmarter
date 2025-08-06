package com.example.thinksmarter

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.thinksmarter.domain.repository.ThinkSmarterRepository

@HiltAndroidApp
class ThinkSmarterApplication : Application() {

    @Inject
    lateinit var repository: ThinkSmarterRepository

    override fun onCreate() {
        super.onCreate()
        // Initialize default categories
        CoroutineScope(Dispatchers.IO).launch {
            repository.initializeDefaultCategories()
        }
    }
}
