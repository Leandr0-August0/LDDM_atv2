package com.fatec.at2_base

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform