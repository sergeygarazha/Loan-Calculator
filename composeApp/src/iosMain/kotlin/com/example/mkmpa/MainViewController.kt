package com.example.mkmpa

import androidx.compose.ui.window.ComposeUIViewController
import com.example.mkmpa.loan.LoanCalculatorApp
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController = ComposeUIViewController { LoanCalculatorApp() }