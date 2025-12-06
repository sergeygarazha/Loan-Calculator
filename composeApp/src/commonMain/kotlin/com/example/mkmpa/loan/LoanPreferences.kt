package com.example.mkmpa.loan

import androidx.compose.runtime.Composable

interface LoanPreferences {
    suspend fun save(amount: Int, periodDays: Int)
    suspend fun load(): SavedLoan?
}

data class SavedLoan(val amount: Int, val periodDays: Int)

@Composable
expect fun rememberLoanPreferences(): LoanPreferences


