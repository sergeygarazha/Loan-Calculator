package com.example.mkmpa

import com.example.mkmpa.loan.LoanAction
import com.example.mkmpa.loan.LoanPreferences
import com.example.mkmpa.loan.LoanStore
import com.example.mkmpa.loan.SavedLoan
import com.example.mkmpa.loan.LoanState
import com.example.mkmpa.loan.SubmissionResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.advanceUntilIdle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LoanStoreTest {
    @Test
    fun store_persistsChoiceOnChange() = runTest {
        val repository = FakeRepository()
        val preferences = FakePreferences()
        val store = LoanStore(repository, preferences, CoroutineScope(coroutineContext))

        store.dispatch(LoanAction.AmountChanged(12_000))
        store.dispatch(LoanAction.PeriodChanged(21))
        advanceUntilIdle()

        assertEquals(12_000, preferences.savedAmount)
        assertEquals(21, preferences.savedPeriod)
    }

    @Test
    fun store_loads_saved_preferences_on_init() = runTest {
        val saved = SavedLoan(15_000, 21)
        val repository = FakeRepository()
        val preferences = FakePreferences(saved)

        val store = LoanStore(repository, preferences, CoroutineScope(coroutineContext))

        advanceUntilIdle()

        assertEquals(saved.amount, store.state.value.amount)
        assertEquals(saved.periodDays, store.state.value.periodDays)
    }

    @Test
    fun submit_sets_loading_then_success_state() = runTest {
        val repository = FakeRepository()
        val preferences = FakePreferences()
        val store = LoanStore(repository, preferences, CoroutineScope(coroutineContext))

        store.dispatch(LoanAction.Submit)

        // Immediately after dispatch the reducer should put us into loading
        assertTrue(store.state.value.isLoading)
        assertNull(store.state.value.submissionResult)

        advanceUntilIdle()

        assertFalse(store.state.value.isLoading)
        assertEquals("fake-confirmation", (store.state.value.submissionResult as SubmissionResult.Success).confirmationId)
        assertEquals(store.state.value.amount, preferences.savedAmount)
        assertEquals(store.state.value.periodDays, preferences.savedPeriod)
        assertEquals(1, repository.submittedCount)
    }

    @Test
    fun submit_failure_sets_error_state() = runTest {
        val repository = FailingRepository()
        val preferences = FakePreferences()
        val store = LoanStore(repository, preferences, CoroutineScope(coroutineContext))

        store.dispatch(LoanAction.Submit)
        advanceUntilIdle()

        val state = store.state.value
        assertFalse(state.isLoading)
        assertTrue(state.submissionResult is SubmissionResult.Error)
        assertEquals("boom", state.errorMessage)
        assertEquals(0, preferences.savedAmount)
    }
}

private class FakeRepository : com.example.mkmpa.loan.LoanRepository {
    var submittedCount = 0
    override suspend fun submitLoan(amount: Int, periodDays: Int, totalRepayment: Int): String {
        submittedCount++
        // simulate some work to exercise loading state
        delay(10)
        return "fake-confirmation"
    }
}

private class FakePreferences : LoanPreferences {
    private val savedOnInit: SavedLoan?
    var savedAmount = 0
    var savedPeriod = 0

    constructor(savedOnInit: SavedLoan? = null) {
        this.savedOnInit = savedOnInit
    }

    override suspend fun save(amount: Int, periodDays: Int) {
        savedAmount = amount
        savedPeriod = periodDays
    }

    override suspend fun load(): SavedLoan? = savedOnInit
}

private class FailingRepository : com.example.mkmpa.loan.LoanRepository {
    override suspend fun submitLoan(amount: Int, periodDays: Int, totalRepayment: Int): String {
        throw IllegalStateException("boom")
    }
}



