package com.aspiring_creators.aichopaicho

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object AppLocaleManager {

    fun setAppLocale(context: Context, langCode: String): Context {
        val locale = Locale.forLanguageTag(langCode)

        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)

        return context.createConfigurationContext(config)
    }
}
