package com.example.mkmpa

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

fun MainIOSViewController(): UIViewController {
    return ComposeUIViewController { App() }
}