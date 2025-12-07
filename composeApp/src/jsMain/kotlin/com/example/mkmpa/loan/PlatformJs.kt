package com.example.mkmpa.loan

import androidx.compose.runtime.Composable
import io.ktor.client.HttpClient
import io.ktor.client.plugins.logging.Logging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.properties.Delegates

@Composable
actual fun rememberLoanPreferences(): LoanPreferences = WebLoanPreferences

actual fun platformHttpClient(): HttpClient = HttpClient {
    install(Logging)
}

private object WebLoanPreferences : LoanPreferences {
    private var cached: SavedLoan? by Delegates.observable<SavedLoan?>(null) { _, _, _ -> }

    override suspend fun save(amount: Int, periodDays: Int) {
        withContext(Dispatchers.Default) {
            cached = SavedLoan(amount, periodDays)
        }
    }

    override suspend fun load(): SavedLoan? = withContext(Dispatchers.Default) { cached }
}



