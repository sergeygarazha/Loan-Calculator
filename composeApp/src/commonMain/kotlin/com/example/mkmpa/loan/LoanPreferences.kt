package com.example.mkmpa.loan

interface LoanPreferences {
    suspend fun save(amount: Int, periodDays: Int)
    suspend fun load(): SavedLoan?
}

data class SavedLoan(val amount: Int, val periodDays: Int)

expect fun rememberLoanPreferences(): LoanPreferences


