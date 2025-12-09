package com.example.mkmpa.loan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LoanReducerTest {
    @Test
    fun amount_change_returns_persist_effect() {
        val initial = LoanState(amount = 10_000, periodDays = 14)

        val result = LoanReducer.reduce(initial, LoanAction.AmountChanged(12_000))

        assertEquals(12_000, result.state.amount)
        assertEquals(null, result.state.errorMessage)
        assertEquals(listOf(LoanEffect.PersistChoice(12_000, 14)), result.effects)
    }

    @Test
    fun submit_with_invalid_data_sets_error_without_effects() {
        val invalid = LoanState(amount = 1_000, periodDays = 14)

        val result = LoanReducer.reduce(invalid, LoanAction.Submit)

        assertEquals("Введите корректные значения", result.state.errorMessage)
        assertTrue(result.effects.isEmpty())
    }

    @Test
    fun invalid_amount_change_keeps_state() {
        val initial = LoanState(amount = 10_000, periodDays = 14)

        val result = LoanReducer.reduce(initial, LoanAction.AmountChanged(500))

        assertEquals(initial, result.state)
        assertTrue(result.effects.isEmpty())
    }

    @Test
    fun invalid_period_change_keeps_state() {
        val initial = LoanState(amount = 10_000, periodDays = 14)

        val result = LoanReducer.reduce(initial, LoanAction.PeriodChanged(5))

        assertEquals(initial, result.state)
        assertTrue(result.effects.isEmpty())
    }

    @Test
    fun restore_saved_clamps_values() {
        val initial = LoanState(amount = 10_000, periodDays = 14)

        val result = LoanReducer.reduce(initial, LoanAction.RestoreSaved(60_000, 5))

        assertEquals(50_000, result.state.amount)
        assertEquals(7, result.state.periodDays)
        assertTrue(result.effects.isEmpty())
    }

    @Test
    fun submit_sets_loading_and_emits_submit_effect() {
        val initial = LoanState(amount = 15_000, periodDays = 21)

        val result = LoanReducer.reduce(initial, LoanAction.Submit)

        assertTrue(result.state.isLoading)
        assertEquals(null, result.state.errorMessage)
        assertEquals(
            listOf(LoanEffect.SubmitLoan(15_000, 21, initial.totalRepayment)),
            result.effects
        )
    }

    @Test
    fun submit_when_already_loading_sets_error() {
        val loading = LoanState(amount = 15_000, periodDays = 21, isLoading = true)

        val result = LoanReducer.reduce(loading, LoanAction.Submit)

        assertEquals("Введите корректные значения", result.state.errorMessage)
        assertTrue(result.effects.isEmpty())
    }

    @Test
    fun submit_success_clears_loading_and_sets_result() {
        val loading = LoanState(amount = 15_000, periodDays = 21, isLoading = true)

        val result = LoanReducer.reduce(loading, LoanAction.Submitted("abc-123"))

        assertEquals(false, result.state.isLoading)
        assertEquals("abc-123", (result.state.submissionResult as SubmissionResult.Success).confirmationId)
    }

    @Test
    fun submit_failure_sets_error_message() {
        val loading = LoanState(amount = 15_000, periodDays = 21, isLoading = true)

        val result = LoanReducer.reduce(loading, LoanAction.SubmitFailed("boom"))

        assertEquals(false, result.state.isLoading)
        assertEquals("boom", result.state.errorMessage)
        assertEquals("boom", (result.state.submissionResult as SubmissionResult.Error).message)
    }

    @Test
    fun clear_message_resets_feedback() {
        val withMessages = LoanState(
            amount = 10_000,
            periodDays = 14,
            submissionResult = SubmissionResult.Error("oops"),
            errorMessage = "oops"
        )

        val result = LoanReducer.reduce(withMessages, LoanAction.ClearMessage)

        assertEquals(null, result.state.errorMessage)
        assertEquals(null, result.state.submissionResult)
    }
}

