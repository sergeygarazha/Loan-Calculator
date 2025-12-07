package com.example.mkmpa

import com.example.mkmpa.loan.LoanAction
import com.example.mkmpa.loan.LoanPreferences
import com.example.mkmpa.loan.LoanStore
import com.example.mkmpa.loan.SavedLoan
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class LoanStoreTest {
    @Test
    fun store_updatesAndSaves() = runTest {
        val repository = FakeRepository()
        val preferences = FakePreferences()
        val store = LoanStore(repository, preferences, CoroutineScope(Dispatchers.Unconfined))

        store.dispatch(LoanAction.AmountChanged(12_000))
        store.dispatch(LoanAction.PeriodChanged(21))
        store.dispatch(LoanAction.Submit)

        assertEquals(1, repository.submittedCount)
        assertEquals(12_000, preferences.savedAmount)
        assertEquals(21, preferences.savedPeriod)
    }
}

private class FakeRepository : com.example.mkmpa.loan.LoanRepository {
    var submittedCount = 0
    override suspend fun submitLoan(amount: Int, periodDays: Int, totalRepayment: Int): String {
        submittedCount++
        return "fake"
    }
}

private class FakePreferences : LoanPreferences {
    var savedAmount = 0
    var savedPeriod = 0
    override suspend fun save(amount: Int, periodDays: Int) {
        savedAmount = amount
        savedPeriod = periodDays
    }

    override suspend fun load(): SavedLoan? = null
}



