package com.example.mkmpa.loan

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.logging.Logging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val PREFS_NAME = "loan_prefs"
private const val KEY_AMOUNT = "amount"
private const val KEY_PERIOD = "period"

@Composable
actual fun rememberLoanPreferences(): LoanPreferences {
    val context = LocalContext.current.applicationContext
    return remember { AndroidLoanPreferences(context) }
}

actual fun platformHttpClient(): HttpClient = HttpClient(OkHttp) {
    install(Logging)
}

private class AndroidLoanPreferences(private val context: Context) : LoanPreferences {
    override suspend fun save(amount: Int, periodDays: Int) {
        withContext(Dispatchers.IO) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .putInt(KEY_AMOUNT, amount)
                .putInt(KEY_PERIOD, periodDays)
                .apply()
        }
    }

    override suspend fun load(): SavedLoan? = withContext(Dispatchers.IO) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val amount = prefs.getInt(KEY_AMOUNT, -1)
        val period = prefs.getInt(KEY_PERIOD, -1)
        return@withContext if (amount != -1 && period != -1) SavedLoan(amount, period) else null
    }
}

