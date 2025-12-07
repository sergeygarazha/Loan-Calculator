package com.example.mkmpa.loan

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.content.TextContent
import io.ktor.http.isSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface LoanRepository {
    suspend fun submitLoan(amount: Int, periodDays: Int, totalRepayment: Int): String
}

class RemoteLoanRepository(
    private val client: HttpClient = platformHttpClient()
) : LoanRepository {
    override suspend fun submitLoan(amount: Int, periodDays: Int, totalRepayment: Int): String =
        withContext(Dispatchers.Default) {
            val body = """{"amount":$amount,"period":$periodDays,"totalRepayment":$totalRepayment}"""
            val response: HttpResponse = client.post("https://jsonplaceholder.typicode.com/posts") {
                setBody(TextContent(body, ContentType.Application.Json))
            }
            if (!response.status.isSuccess()) {
                throw IllegalStateException("Server error: ${response.status.value}")
            }
            // mock API returns an id field; fall back to status text when missing
            val text = response.bodyAsText()
            return@withContext if (text.isNotBlank()) text else "ok-${response.status.value}"
        }
}

expect fun platformHttpClient(): HttpClient


