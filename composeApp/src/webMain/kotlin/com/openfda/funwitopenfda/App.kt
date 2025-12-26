package com.openfda.funwitopenfda

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BasicAuthCredentials
import io.ktor.client.plugins.auth.providers.basic
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    val client = HttpClient {

        install(HttpTimeout) {
            requestTimeoutMillis = 45_000   // whole request
            connectTimeoutMillis = 45_000   // TCP connect
            socketTimeoutMillis = 45_000
        }

        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }


        install(Auth) {
            basic {
                sendWithoutRequest { request ->
                    // Only send preemptively to your specific backend host
                    request.url.host == "visualopenfda.ew.r.appspot.com"
                }
                credentials {
                    BasicAuthCredentials(username = "funWithOpenFDA", password = "W3@r30nTh3DrugS")
                }
                realm = "Access to the '/' path"
            }
        }
    }


    MaterialTheme {
        Scaffold(
            modifier=Modifier.fillMaxSize(),
            topBar = { TopAppBar(
                title = { Text("Fun with OpenFDA") },
                colors= TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ))
            }

        ) { innerPadding ->
            FunWithOpenFDA(
                modifier = Modifier.padding(innerPadding),
                httpClient = client)
        }
    }

}