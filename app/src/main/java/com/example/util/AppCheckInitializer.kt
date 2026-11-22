package com.example.util

import android.content.Context
import android.content.Intent
import com.example.BuildConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.recaptcha.RecaptchaAppCheckProviderFactory

/**
 * Configures Firebase App Check before Firebase AI Logic or Firestore calls are made.
 * Must be invoked once in MainActivity.onCreate() after super.onCreate().
 */
object AppCheckInitializer {

    fun configureAppCheck(context: Context, intent: Intent?) {
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context)
        }
        val appCheck = FirebaseAppCheck.getInstance()
        if (BuildConfig.DEBUG) {
            val debugToken = intent?.getStringExtra("FIREBASE_APPCHECK_DEBUG_TOKEN")
            if (!debugToken.isNullOrEmpty()) {
                val app = FirebaseApp.getInstance()
                val prefsName = "com.google.firebase.appcheck.debug.store.${app.persistenceKey}"
                context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
                    .edit()
                    .putString("com.google.firebase.appcheck.debug.DEBUG_SECRET", debugToken)
                    .commit()
            }
            appCheck.installAppCheckProviderFactory(DebugAppCheckProviderFactory.getInstance())
        } else {
            val siteKeyResId = context.resources.getIdentifier(
                "recaptcha_site_key",
                "string",
                context.packageName
            )
            if (siteKeyResId != 0) {
                val siteKey = context.getString(siteKeyResId)
                if (siteKey.isNotBlank()) {
                    appCheck.installAppCheckProviderFactory(
                        RecaptchaAppCheckProviderFactory.getInstance(siteKey)
                    )
                }
            }
        }
    }
}
