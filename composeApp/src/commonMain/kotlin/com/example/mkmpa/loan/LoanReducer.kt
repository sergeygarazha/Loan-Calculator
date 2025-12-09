package com.example.mkmpa.loan

private const val INVALID_INPUT_MESSAGE = "Введите корректные значения"

data class LoanResult(
    val state: LoanState,
    val effects: List<LoanEffect> = emptyList()
)

sealed interface LoanEffect {
    data class PersistChoice(val amount: Int, val periodDays: Int) : LoanEffect
    data class SubmitLoan(val amount: Int, val periodDays: Int, val totalRepayment: Int) : LoanEffect
}

object LoanReducer {
    fun reduce(state: LoanState, action: LoanAction): LoanResult =
        when (action) {
            is LoanAction.AmountChanged -> reduceAmountChanged(state, action)
            is LoanAction.PeriodChanged -> reducePeriodChanged(state, action)
            is LoanAction.RestoreSaved -> reduceRestoreSaved(state, action)
            is LoanAction.Submit -> reduceSubmit(state)
            is LoanAction.Submitted -> LoanResult(
                state.copy(
                    isLoading = false,
                    submissionResult = SubmissionResult.Success(action.confirmationId),
                    errorMessage = null
                )
            )
            is LoanAction.SubmitFailed -> LoanResult(
                state.copy(
                    isLoading = false,
                    submissionResult = SubmissionResult.Error(action.message),
                    errorMessage = action.message
                )
            )
            LoanAction.ClearMessage -> LoanResult(state.copy(submissionResult = null, errorMessage = null))
        }

    private fun reduceAmountChanged(state: LoanState, action: LoanAction.AmountChanged): LoanResult {
        if (!isAmountValid(action.amount)) return LoanResult(state)
        val nextState = state.copy(amount = action.amount, submissionResult = null, errorMessage = null)
        return LoanResult(nextState, listOf(LoanEffect.PersistChoice(action.amount, state.periodDays)))
    }

    private fun reducePeriodChanged(state: LoanState, action: LoanAction.PeriodChanged): LoanResult {
        if (!isPeriodValid(action.periodDays)) return LoanResult(state)
        val nextState = state.copy(periodDays = action.periodDays, submissionResult = null, errorMessage = null)
        return LoanResult(nextState, listOf(LoanEffect.PersistChoice(state.amount, action.periodDays)))
    }

    private fun reduceRestoreSaved(state: LoanState, action: LoanAction.RestoreSaved): LoanResult {
        val amount = action.amount.coerceIn(MIN_LOAN_AMOUNT, MAX_LOAN_AMOUNT)
        val period = if (isPeriodValid(action.periodDays)) action.periodDays else LOAN_PERIOD_OPTIONS.first()
        val nextState = state.copy(
            amount = amount,
            periodDays = period,
            submissionResult = null,
            errorMessage = null
        )
        return LoanResult(nextState)
    }

    private fun reduceSubmit(state: LoanState): LoanResult {
        if (!isAmountValid(state.amount) || !isPeriodValid(state.periodDays) || state.isLoading) {
            return LoanResult(state.copy(errorMessage = INVALID_INPUT_MESSAGE, submissionResult = null))
        }
        val nextState = state.copy(isLoading = true, errorMessage = null, submissionResult = null)
        val effect = LoanEffect.SubmitLoan(
            amount = state.amount,
            periodDays = state.periodDays,
            totalRepayment = state.totalRepayment
        )
        return LoanResult(nextState, listOf(effect))
    }
}

