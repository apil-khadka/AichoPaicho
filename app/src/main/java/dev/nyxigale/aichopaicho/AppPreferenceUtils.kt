package dev.nyxigale.aichopaicho

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit



object AppPreferenceUtils {
    private const val PREFS_NAME = "app_preferences"

    private const val KEY_CURRENCY_CODE = "currency_code"
    private const val KEY_LANGUAGE_CODE = "language_code"
    private const val KEY_ANALYTICS_ENABLED = "analytics_enabled"
    private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"

    private const val DEFAULT_CURRENCY = "NPR"
    private const val DEFAULT_LANGUAGE = "en"

    private const val KEY_HIDE_AMOUNTS = "hide_amounts_enabled"

    private const val KEY_SECURITY_ENABLED = "security_enabled"
    private const val KEY_PIN = "security_pin"
    private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun isSecurityEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_SECURITY_ENABLED, false)
    }

    fun setSecurityEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit {
            putBoolean(KEY_SECURITY_ENABLED, enabled)
        }
    }

    fun getPin(context: Context): String? {
        return getPrefs(context).getString(KEY_PIN, null)
    }

    fun setPin(context: Context, pin: String?) {
        getPrefs(context).edit {
            putString(KEY_PIN, pin)
        }
    }

    fun isBiometricEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_BIOMETRIC_ENABLED, false)
    }

    fun setBiometricEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit {
            putBoolean(KEY_BIOMETRIC_ENABLED, enabled)
        }
    }

    fun isHideAmountsEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_HIDE_AMOUNTS, false)
    }

    fun setHideAmountsEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit {
            putBoolean(KEY_HIDE_AMOUNTS, enabled)
        }
    }

    fun getCurrencyCode(context: Context): String {
        return getPrefs(context).getString(KEY_CURRENCY_CODE, DEFAULT_CURRENCY)
            ?: DEFAULT_CURRENCY
    }

    fun setCurrencyCode(context: Context, currencyCode: String) {
        getPrefs(context).edit {
            putString(KEY_CURRENCY_CODE, currencyCode)
        }
    }

    fun getCurrencySymbol(context: Context): String {
        val currencyCode = getCurrencyCode(context)
/*        return try {
            java.util.Currency.getInstance(currencyCode).symbol
        } catch (e: Exception) {
            "$"
        }*/
        return currencyCode
    }

    @SuppressLint("DefaultLocale")
    fun formatAmount(context: Context, amount: Double): String {
        val symbol = getCurrencySymbol(context)
        return "$symbol${String.format("%.2f", amount)}"
    }

    fun getLanguageCode(context: Context): String {
        return getPrefs(context).getString(KEY_LANGUAGE_CODE, DEFAULT_LANGUAGE)
            ?: DEFAULT_LANGUAGE
    }

    fun setLanguageCode(context: Context, langCode: String) {
        getPrefs(context).edit {
            putString(KEY_LANGUAGE_CODE, langCode)
        }
    }

    fun isAnalyticsEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_ANALYTICS_ENABLED, true)
    }

    fun setAnalyticsEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit {
            putBoolean(KEY_ANALYTICS_ENABLED, enabled)
        }
    }

    fun isOnboardingCompleted(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }

    fun setOnboardingCompleted(context: Context, completed: Boolean) {
        getPrefs(context).edit {
            putBoolean(KEY_ONBOARDING_COMPLETED, completed)
        }
    }

}