package com.bhaskar.anytranslate

import android.app.Application
import com.bhaskar.anytranslate.data.local.AppDatabase
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AnyTranslateApp : Application() {

    val database: AppDatabase by lazy {
        AppDatabase.getInstance(this)
    }

    override fun onCreate() {
        super.onCreate()

        // Initialize Google Mobile Ads SDK asynchronously
        CoroutineScope(Dispatchers.IO).launch {
            MobileAds.initialize(this@AnyTranslateApp) {}
        }
    }
}
