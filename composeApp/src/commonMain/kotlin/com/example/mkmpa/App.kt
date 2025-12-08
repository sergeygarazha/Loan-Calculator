package com.example.mkmpa

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import org.jetbrains.compose.ui.tooling.preview.Preview
import com.example.mkmpa.loan.LoanCalculatorScreen
import com.example.mkmpa.loan.LoanStore
import com.example.mkmpa.loan.RemoteLoanRepository
import com.example.mkmpa.loan.rememberLoanPreferences
import kotlinx.coroutines.CoroutineScope

@Composable
@Preview
fun App() {
    MaterialTheme {
        val scope = rememberCoroutineScope()
        val store = rememberLoanStore(scope)

        Surface(modifier = Modifier.fillMaxSize()) {
            LoanCalculatorScreen(store)
        }
    }
}

@Composable
private fun rememberLoanStore(scope: CoroutineScope): LoanStore {
    val preferences = rememberLoanPreferences()
    val repository = RemoteLoanRepository()
    return remember(preferences) { LoanStore(repository, preferences, scope) }
}