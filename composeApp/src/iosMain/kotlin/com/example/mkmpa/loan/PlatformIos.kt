package com.example.mkmpa.loan

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.logging.Logging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.withContext
import platform.Foundation.NSUserDefaults

@Composable
actual fun rememberLoanPreferences(): LoanPreferences = remember { IosLoanPreferences() }

actual fun platformHttpClient(): HttpClient = HttpClient(Darwin) {
    install(Logging)
}

fun doNewLoanStore(): LoanStore {
    return LoanStore(RemoteLoanRepository(), IosLoanPreferences(), MainScope())
}

fun newLoanStore(
    scope: CoroutineScope = MainScope(),
    repository: LoanRepository = RemoteLoanRepository(),
    preferences: LoanPreferences = IosLoanPreferences()
): LoanStore = LoanStore(repository, preferences, scope)

private class IosLoanPreferences : LoanPreferences {
    private val defaults = NSUserDefaults.standardUserDefaults
    override suspend fun save(amount: Int, periodDays: Int) {
        withContext(Dispatchers.Default) {
            defaults.setInteger(amount.toLong(), "amount")
            defaults.setInteger(periodDays.toLong(), "period")
        }
    }

    override suspend fun load(): SavedLoan? = withContext(Dispatchers.Default) {
        val amount = defaults.integerForKey("amount").toInt()
        val period = defaults.integerForKey("period").toInt()
        return@withContext if (amount != 0 && period != 0) SavedLoan(amount, period) else null
    }
}

