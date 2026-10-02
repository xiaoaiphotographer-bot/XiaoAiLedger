package com.xiaoai.ledger

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import com.xiaoai.ledger.data.Repository

class App : Application() {
    private val appScope = CoroutineScope(SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        instance = this
        appScope.launch {
            Repository.get(this@App).initIfFirstLaunch()
        }
    }

    companion object {
        lateinit var instance: App
            private set
    }
}
