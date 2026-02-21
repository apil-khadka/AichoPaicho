package dev.nyxigale.aichopaicho.ui.util

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import java.util.Locale
import kotlin.math.abs

private const val APP_PREFS_NAME = "app_preferences"
private const val KEY_HIDE_AMOUNTS = "hide_amounts_enabled"
private const val MASKED_AMOUNT = "****"

@Composable
fun rememberHideAmountsEnabled(): Boolean {
    val context = LocalContext.current
    val prefs = remember(context) {
        context.getSharedPreferences(APP_PREFS_NAME, Context.MODE_PRIVATE)
    }
    var hideAmountsEnabled by remember(prefs) {
        mutableStateOf(prefs.getBoolean(KEY_HIDE_AMOUNTS, false))
    }

    DisposableEffect(prefs) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            if (key == KEY_HIDE_AMOUNTS) {
                hideAmountsEnabled = sharedPreferences.getBoolean(KEY_HIDE_AMOUNTS, false)
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    return hideAmountsEnabled
}

fun formatCurrencyAmount(currency: String, amount: Int, hideAmounts: Boolean): String {
    return if (hideAmounts) "$currency $MASKED_AMOUNT" else "$currency $amount"
}

fun formatCurrencyAmount(
    currency: String,
    amount: Double,
    hideAmounts: Boolean,
    decimals: Int = 2
): String {
    return if (hideAmounts) {
        "$currency $MASKED_AMOUNT"
    } else {
        "$currency ${"%1$,.${decimals}f".format(Locale.getDefault(), amount)}"
    }
}

fun formatAbsoluteCurrencyAmount(
    currency: String,
    amount: Double,
    hideAmounts: Boolean,
    decimals: Int = 2
): String {
    return formatCurrencyAmount(currency, abs(amount), hideAmounts, decimals)
}

fun formatSignedCurrencyAmount(
    sign: String,
    currency: String,
    amount: Int,
    hideAmounts: Boolean
): String {
    return if (hideAmounts) "$sign $currency $MASKED_AMOUNT" else "$sign $currency $amount"
}

