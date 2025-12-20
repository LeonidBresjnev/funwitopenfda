package com.openfda.funwitopenfda

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
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
    }


    MaterialTheme {
        Scaffold(
            modifier=Modifier.fillMaxSize(),
            topBar = { TopAppBar(
                title = { Text("Fun with OpenFDA") },
                colors= TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )) }
            /*,
            bottomBar = {  Text(text = getPlatform().name) }*/
        ) { innerPadding ->
            FunWithOpenFDA(modifier = Modifier.padding(innerPadding),
                httpClient = client)
            /*
        var showContent by remember { mutableStateOf(false) }
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primaryContainer)
                .safeContentPadding()
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(onClick = { showContent = !showContent }) {
                Text("Click me!")
            }
            AnimatedVisibility(showContent) {
                val greeting = remember { Greeting().greet() }
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Image(painterResource(Res.drawable.compose_multiplatform), null)
                    Text("Compose: $greeting")
                }
            }
        }*/
        }
    }

}