package com.example.mkmpa

import com.example.mkmpa.loan.DEFAULT_INTEREST_RATE
import com.example.mkmpa.loan.MIN_LOAN_AMOUNT
import com.example.mkmpa.loan.calculateTotal
import com.example.mkmpa.loan.isAmountValid
import com.example.mkmpa.loan.isPeriodValid
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ComposeAppCommonTest {

    @Test
    fun calculateTotal_respectsInterest() {
        val base = 10_000
        val total = calculateTotal(base, DEFAULT_INTEREST_RATE)
        assertEquals(11_500, total)
    }

    @Test
    fun validation_rulesWork() {
        assertTrue(isAmountValid(MIN_LOAN_AMOUNT))
        assertFalse(isAmountValid(1_000))
        assertTrue(isPeriodValid(14))
        assertFalse(isPeriodValid(5))
    }
}