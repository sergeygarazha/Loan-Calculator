package com.example.mkmpa.loan

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.logging.Logging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicReference

@Composable
actual fun rememberLoanPreferences(): LoanPreferences = remember { DesktopLoanPreferences }

actual fun platformHttpClient(): HttpClient = HttpClient(CIO) {
    install(Logging)
}

private object DesktopLoanPreferences : LoanPreferences {
    private val state = AtomicReference<SavedLoan?>(null)
    override suspend fun save(amount: Int, periodDays: Int) {
        withContext(Dispatchers.Default) {
            state.set(SavedLoan(amount, periodDays))
        }
    }

    override suspend fun load(): SavedLoan? = withContext(Dispatchers.Default) { state.get() }
}

