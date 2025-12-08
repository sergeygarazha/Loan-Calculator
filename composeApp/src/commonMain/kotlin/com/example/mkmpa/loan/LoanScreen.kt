package com.example.mkmpa.loan

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LoanCalculatorScreen(store: LoanStore) {
    val state by store.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Займ",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Сумма", style = MaterialTheme.typography.titleMedium)
                AmountSlider(
                    amount = state.amount,
                    onAmountChange = { store.dispatch(LoanAction.AmountChanged(it)) }
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(formatAmount(MIN_LOAN_AMOUNT))
                    Text(formatAmount(MAX_LOAN_AMOUNT))
                }
                Text(
                    text = "${formatAmount(state.amount)} USD",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Срок", style = MaterialTheme.typography.titleMedium)
                PeriodSlider(
                    period = state.periodDays,
                    onPeriodChange = { store.dispatch(LoanAction.PeriodChanged(it)) }
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    LOAN_PERIOD_OPTIONS.forEach { option ->
                        Text("$option дн.", fontSize = 13.sp)
                    }
                }
            }
        }

        SummaryCard(state = state)

        AnimatedVisibility(visible = state.errorMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colorScheme.errorContainer)
                    .padding(12.dp)
            ) {
                Text(
                    text = state.errorMessage ?: "",
                    color = colorScheme.onErrorContainer
                )
            }
        }

        AnimatedVisibility(visible = state.submissionResult is SubmissionResult.Success) {
            val confirmation = (state.submissionResult as? SubmissionResult.Success)?.confirmationId.orEmpty()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colorScheme.primaryContainer)
                    .padding(12.dp)
            ) {
                Text(
                    text = "Заявка отправлена (#$confirmation)",
                    color = colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { store.dispatch(LoanAction.Submit) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = !state.isLoading,
            colors = ButtonDefaults.buttonColors()
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    color = colorScheme.onPrimary,
                    strokeWidth = 2.dp,
                    modifier = Modifier.height(24.dp)
                )
            } else {
                Text("Подать заявку")
            }
        }
    }
}

@Composable
private fun AmountSlider(amount: Int, onAmountChange: (Int) -> Unit) {
    val steps = ((MAX_LOAN_AMOUNT - MIN_LOAN_AMOUNT) / 500) - 1
    Slider(
        value = amount.toFloat(),
        onValueChange = { onAmountChange(it.toInt()) },
        valueRange = MIN_LOAN_AMOUNT.toFloat()..MAX_LOAN_AMOUNT.toFloat(),
        steps = steps,
        colors = SliderDefaults.colors()
    )
}

@Composable
private fun PeriodSlider(period: Int, onPeriodChange: (Int) -> Unit) {
    val index = LOAN_PERIOD_OPTIONS.indexOf(period).coerceAtLeast(0)
    Slider(
        value = index.toFloat(),
        onValueChange = { value ->
            val newIndex = value.toInt().coerceIn(0, LOAN_PERIOD_OPTIONS.lastIndex)
            onPeriodChange(LOAN_PERIOD_OPTIONS[newIndex])
        },
        valueRange = 0f..LOAN_PERIOD_OPTIONS.lastIndex.toFloat(),
        steps = LOAN_PERIOD_OPTIONS.size - 2,
        colors = SliderDefaults.colors()
    )
}

@Composable
private fun SummaryCard(state: LoanState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SummaryRow("Ставка", "${(state.interestRate * 100).toInt()}%")
            SummaryRow("К возврату", "${formatAmount(state.totalRepayment)} USD")
            SummaryRow("Дата возврата", state.returnDateLabel)
            AnimatedContent(
                targetState = state.submissionResult,
                label = "status"
            ) { result ->
                when (result) {
                    is SubmissionResult.Success -> SummaryRow("Статус", "Отправлено")
                    is SubmissionResult.Error -> SummaryRow("Статус", "Ошибка")
                    null -> SummaryRow("Статус", "Черновик")
                }
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.End)
    }
}

