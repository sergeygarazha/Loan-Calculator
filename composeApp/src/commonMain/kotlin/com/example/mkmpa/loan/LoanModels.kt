package com.example.mkmpa.loan

import kotlin.time.Clock
import kotlinx.datetime.*
import kotlin.time.ExperimentalTime
import kotlin.math.ceil

const val MIN_LOAN_AMOUNT = 5_000
const val MAX_LOAN_AMOUNT = 50_000
val LOAN_PERIOD_OPTIONS = listOf(7, 14, 21, 28)
const val DEFAULT_INTEREST_RATE = 0.15

data class LoanState(
    val amount: Int = 10_000,
    val periodDays: Int = 14,
    val interestRate: Double = DEFAULT_INTEREST_RATE,
    val isLoading: Boolean = false,
    val submissionResult: SubmissionResult? = null,
    val errorMessage: String? = null
) {
    val totalRepayment: Int
        get() = calculateTotal(amount, interestRate)

    @OptIn(ExperimentalTime::class)
    val returnDateLabel: String
        get() {
            val today = Clock.System.now()
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .date
            val returnDate = today.plus(DatePeriod(days = periodDays))
            val day = returnDate.day.toString().padStart(2, '0')
            val month = returnDate.month.toString().padStart(2, '0')
            return "$day.$month.${returnDate.year}"
        }
}

sealed interface SubmissionResult {
    data class Success(val confirmationId: String) : SubmissionResult
    data class Error(val message: String) : SubmissionResult
}

sealed interface LoanAction {
    data class AmountChanged(val amount: Int) : LoanAction
    data class PeriodChanged(val periodDays: Int) : LoanAction
    data class RestoreSaved(val amount: Int, val periodDays: Int) : LoanAction
    data class Submitted(val confirmationId: String) : LoanAction
    data class SubmitFailed(val message: String) : LoanAction
    data object Submit : LoanAction
    data object ClearMessage : LoanAction
}

fun calculateTotal(amount: Int, interestRate: Double): Int {
    val rate = amount * interestRate
    return ceil(amount + rate).toInt()
}

fun isAmountValid(amount: Int): Boolean = amount in MIN_LOAN_AMOUNT..MAX_LOAN_AMOUNT

fun isPeriodValid(period: Int): Boolean = LOAN_PERIOD_OPTIONS.contains(period)

fun formatAmount(value: Int): String {
    val reversed = value.toString().reversed().chunked(3).joinToString(",")
    return reversed.reversed()
}

