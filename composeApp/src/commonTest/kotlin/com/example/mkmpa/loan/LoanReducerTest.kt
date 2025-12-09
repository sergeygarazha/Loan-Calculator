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
}

