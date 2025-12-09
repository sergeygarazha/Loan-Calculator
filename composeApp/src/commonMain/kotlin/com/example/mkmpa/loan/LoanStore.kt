package com.example.mkmpa.loan

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DisposableHandle
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
        val currentState = _state.value
        val result = LoanReducer.reduce(currentState, action)
        if (result.state != currentState) {
            _state.value = result.state
        }
        handleEffects(result.effects)
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

    private fun handleEffects(effects: List<LoanEffect>) {
        effects.forEach { effect ->
            when (effect) {
                is LoanEffect.PersistChoice -> persistChoice(effect.amount, effect.periodDays)
                is LoanEffect.SubmitLoan -> submitLoan(effect)
            }
        }
    }

    private fun persistChoice(amount: Int, periodDays: Int) {
        scope.launch {
            runCatching {
                preferences.save(amount, periodDays)
            }
        }
    }

    private fun submitLoan(effect: LoanEffect.SubmitLoan) {
        activeJob?.cancel()
        activeJob = scope.launch {
            runCatching {
                repository.submitLoan(effect.amount, effect.periodDays, effect.totalRepayment)
            }.onSuccess { confirmation ->
                preferences.save(effect.amount, effect.periodDays)
                dispatch(LoanAction.Submitted(confirmation))
            }.onFailure { error ->
                dispatch(LoanAction.SubmitFailed(error.message ?: "Не удалось отправить заявку"))
            }
        }
    }
}


