package com.openfda.funwitopenfda.openfda

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.openfda.funwitopenfda.OpenFdaNdc
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.serialization.json.Json


@Composable
fun Packages(productNdc: List<String>,
             onDismissRequest: () -> Unit,
             client: HttpClient) {

    var response by remember { mutableStateOf<List<OpenFdaNdc?>>(emptyList()) }
    var finished by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = finished) {
        isLoading = true
        val baseUrl = "https://api.fda.gov/drug/ndc.json?search=_exists_:product_ndc+AND+"
        if (productNdc.isEmpty()) return@LaunchedEffect


        val resultDefs = productNdc.map {
            async(context = Dispatchers.Default) {
                val httpResponse: Result<HttpResponse> = runCatching {
                    client.get("${baseUrl}product_ndc:$it")
                }
                return@async httpResponse
            }
        }

        val results = resultDefs.awaitAll()

        isLoading = false
        response = results.map { result ->
            result.onSuccess { action ->
                val status = action.status.value
                println("status=$status")
                return@map if (action.status == HttpStatusCode.OK) {
                    action.body<OpenFdaNdc>()
                } else {
                    null
                }
            }
            result.onFailure { error ->
                println(error.message)
                return@map null
            }
            return@map null
        }
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false // Allows custom sizing
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .border(1.dp, androidx.compose.ui.graphics.Color.Black),
            shape = MaterialTheme.shapes.large,
            tonalElevation = 6.dp
        ) {

            Column(modifier= Modifier.padding(16.dp)) {
                Text(text = "Package information", style = MaterialTheme.typography.displayMedium.copy(fontWeight = Bold))
              /*  Text("title-small",style=MaterialTheme.typography.titleSmall)
                Text("title-medium",style=MaterialTheme.typography.titleMedium)
                Text("title-large",style=MaterialTheme.typography.titleLarge)
                Text(text="body-small",style=MaterialTheme.typography.bodySmall)
                Text(text="body-medium",style=MaterialTheme.typography.bodyMedium)
                Text(text="body-large",style=MaterialTheme.typography.bodyLarge)
                Text(text="label-small",style=MaterialTheme.typography.labelSmall)
                Text(text="label-medium",style=MaterialTheme.typography.labelMedium)
                Text(text="label-large",style=MaterialTheme.typography.labelLarge)
                Text(text="headline-small",style=MaterialTheme.typography.headlineSmall)
                Text(text="headline-medium",style=MaterialTheme.typography.headlineMedium)
                Text(text="headline-large",style=MaterialTheme.typography.headlineLarge)
                Text(text="display-small",style=MaterialTheme.typography.displaySmall)
                Text(text="display-medium",style=MaterialTheme.typography.displayMedium)
                Text(text="display-large",style=MaterialTheme.typography.displayLarge)*/


                if (isLoading) CircularProgressIndicator()
                else response.forEach { response ->
                    response?.results?.forEach { result ->
                        Text(
                            text= result.active_ingredients.joinToString(" + ") { it.name + ": " + it.strength },
                            style = MaterialTheme.typography.headlineMedium
                        )

                        result.packaging.forEach { pack ->
                            Text(text = pack.description + (if (pack.sample) " (sample)" else ""), style = MaterialTheme.typography.bodyLarge)
                        }
                        HorizontalDivider()
                    }
                }
            }
        }

    }
}