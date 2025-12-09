package com.example.mkmpa.loan

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DisposableHandle
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoanStore(
    private val repository: LoanRepository,
    private val preferences: LoanPreferences,
    private val scope: CoroutineScope,
) {
    private val _state = MutableStateFlow(LoanState())
    val state: StateFlow<LoanState> = _state.asStateFlow()
    private var activeJob: Job? = null

    init {
        scope.launch {
            preferences.load()?.let { saved ->
                dispatch(LoanAction.RestoreSaved(saved.amount, saved.periodDays))
            }
        }
    }

    fun dispatch(action: LoanAction) {
        when (action) {
            is LoanAction.AmountChanged -> handleAmountChange(action.amount)
            is LoanAction.PeriodChanged -> handlePeriodChange(action.periodDays)
            is LoanAction.RestoreSaved -> _state.update {
                it.copy(
                    amount = action.amount.coerceIn(MIN_LOAN_AMOUNT, MAX_LOAN_AMOUNT),
                    periodDays = if (isPeriodValid(action.periodDays)) action.periodDays else LOAN_PERIOD_OPTIONS.first(),
                    submissionResult = null,
                    errorMessage = null
                )
            }
            is LoanAction.Submitted -> _state.update {
                it.copy(isLoading = false, submissionResult = SubmissionResult.Success(action.confirmationId), errorMessage = null)
            }
            is LoanAction.SubmitFailed -> _state.update {
                it.copy(isLoading = false, submissionResult = SubmissionResult.Error(action.message), errorMessage = action.message)
            }
            LoanAction.Submit -> submit()
            LoanAction.ClearMessage -> _state.update { it.copy(submissionResult = null, errorMessage = null) }
        }
    }

    /**
     * Allows native UI layers (SwiftUI/Jetpack) to observe state changes in a Redux-like way.
     */
    fun observeState(onChange: (LoanState) -> Unit): DisposableHandle {
        val job = scope.launch {
            state.collect { onChange(it) }
        }
        return DisposableHandle { job.cancel() }
    }

    private fun handleAmountChange(amount: Int) {
        if (!isAmountValid(amount)) return
        val periodDays = _state.value.periodDays
        _state.update { it.copy(amount = amount, submissionResult = null, errorMessage = null) }
        persistChoice(amount, periodDays)
    }

    private fun handlePeriodChange(period: Int) {
        if (!isPeriodValid(period)) return
        val amount = _state.value.amount
        _state.update { it.copy(periodDays = period, submissionResult = null, errorMessage = null) }
        persistChoice(amount, period)
    }

    private fun persistChoice(amount: Int, periodDays: Int) {
        scope.launch {
            runCatching {
                preferences.save(amount, periodDays)
            }
        }
    }

    private fun submit() {
        val snapshot = _state.value
        if (!isAmountValid(snapshot.amount) || !isPeriodValid(snapshot.periodDays) || snapshot.isLoading) {
            _state.update { it.copy(errorMessage = "Введите корректные значения") }
            return
        }

        activeJob?.cancel()
        _state.update { it.copy(isLoading = true, errorMessage = null, submissionResult = null) }

        activeJob = scope.launch {
            runCatching {
                repository.submitLoan(snapshot.amount, snapshot.periodDays, snapshot.totalRepayment)
            }.onSuccess { confirmation ->
                preferences.save(snapshot.amount, snapshot.periodDays)
                dispatch(LoanAction.Submitted(confirmation))
            }.onFailure { error ->
                dispatch(LoanAction.SubmitFailed(error.message ?: "Не удалось отправить заявку"))
            }
        }
    }
}


