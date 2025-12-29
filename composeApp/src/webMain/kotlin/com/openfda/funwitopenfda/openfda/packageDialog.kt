package com.openfda.funwitopenfda.openfda

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.openfda.funwitopenfda.OpenFdaNdc
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll


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

                if (isLoading) CircularProgressIndicator()
                else response.forEach { response ->
                    response?.results?.forEach { result ->

                        SelectionContainer {
                            Text(
                                text = result.active_ingredients.joinToString(" + ") { it.name + ": " + it.strength } + ( if (result.dosage_form.isNotBlank()) " (${result.dosage_form})" else ""),
                                style = MaterialTheme.typography.headlineMedium
                            )
                        }

                            result.packaging.forEach { pack ->
                                SelectionContainer {
                                Text(text = pack.description + (if (pack.sample) " (sample)" else ""), style = MaterialTheme.typography.bodyLarge)
                            }
                        }

                        HorizontalDivider()
                    }
                }
            }
        }

    }
}