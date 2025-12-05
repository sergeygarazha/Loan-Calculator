package com.example.mkmpa

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform