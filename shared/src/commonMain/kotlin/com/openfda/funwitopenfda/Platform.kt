package com.openfda.funwitopenfda

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform