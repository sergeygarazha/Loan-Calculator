package com.example.mkmpa

import androidx.compose.foundation.isSystemInDarkTheme
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
import io.github.alexzhirkevich.cupertino.adaptive.AdaptiveTheme
import io.github.alexzhirkevich.cupertino.adaptive.ExperimentalAdaptiveApi
import io.github.alexzhirkevich.cupertino.adaptive.Theme
import io.github.alexzhirkevich.cupertino.theme.CupertinoTheme
import kotlinx.coroutines.CoroutineScope
import com.example.mkmpa.loan.getPlatformTheme

@Composable
@Preview
fun App() {
    AppTheme() {
        val scope = rememberCoroutineScope()
        val store = rememberLoanStore(scope)

        Surface(modifier = Modifier.fillMaxSize()) {
            LoanCalculatorScreen(store)
        }
    }
}

@OptIn(ExperimentalAdaptiveApi::class)
@Composable
fun AppTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    theme: Theme = getPlatformTheme(),
    content: @Composable () -> Unit
) {
    AdaptiveTheme(
        target = theme,
        material = {
            MaterialTheme(content = it)
        },
        cupertino = {
            CupertinoTheme(content = it)
        },
        content = content
    )
}

@Composable
private fun rememberLoanStore(scope: CoroutineScope): LoanStore {
    val preferences = rememberLoanPreferences()
    val repository = RemoteLoanRepository()
    return remember(preferences) { LoanStore(repository, preferences, scope) }
}