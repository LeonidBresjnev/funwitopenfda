package com.openfda.funwitopenfda.openfda

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.WebElementView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.browser.document
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.w3c.dom.HTMLDivElement

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Feature(feature: Pair<String,List<String>>,
            onDismissRequest: () -> Unit,
            searchStr: String="",
            html: Boolean=false) {

    val webViewWidth = remember { mutableStateOf(0.dp) }
    val webViewHeight = remember { mutableStateOf(0.dp) }
    val coroutineScope = rememberCoroutineScope()

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
                .fillMaxHeight(0.9f)
                .border(1.dp, Color.Black),
            shape = MaterialTheme.shapes.large,
            tonalElevation = 6.dp
        ) {
            Column(modifier= Modifier.padding(16.dp)) {
                Text(text=feature.first, style = MaterialTheme.typography.displayMedium.copy(fontWeight = Bold))

                if (!html) {
                    LazyColumn {
                    itemsIndexed(items = feature.second) { idx,iu ->
                            val unquoted = searchStr.lowercase().removeSurrounding("\"")
                            println("searchstr: $searchStr unquoted: $unquoted")
                            if (unquoted.isNotEmpty()) {
                                val rabinKarp = iu.lowercase().rabinKarp(unquoted)
                                val builder = AnnotatedString.Builder()
                                builder.append(iu)
                                rabinKarp.forEach {

                                    builder.addStyle(
                                        style = SpanStyle(fontWeight = Bold),
                                        start = it,
                                        end = it + unquoted.length
                                    )
                                }
                                println(rabinKarp.joinToString(", "))
                                Text(text = builder.toAnnotatedString()/*, style = MaterialTheme.typography.bodyLarge*/)

                            } else Text(iu)
                        if (idx != feature.second.lastIndex) HorizontalDivider(thickness = 1.dp)
                        }

                    }
                } else {
                    Column {
                        feature.second.forEachIndexed { idx, iu ->
                            println("table $idx, $iu")
                            Box(
                                modifier = Modifier.weight(1f)

                                    .border(1.dp, Color.Green)
                            ) {
                                WebElementView(
                                    modifier = Modifier
                                       /* .sizeIn(
                                            minWidth = webViewWidth.value+50.dp,
                                            minHeight = webViewHeight.value+50.dp)*/ // Fallback min sizes
                                        .fillMaxSize()
                                        .border(1.dp, Color.Black),
                                    factory = {
                                        val tableElement = (document.createElement("div") as HTMLDivElement)

                                        //tableElement.innerHTML = iu
                                        return@WebElementView tableElement
                                    },
                                    update = { element ->
                                        element.innerHTML = iu
                                        coroutineScope.launch {
                                            delay(300) // Brief delay for DOM to settle (adjust if needed)
                                            val measuredWidth = element.offsetWidth.toFloat().dp
                                            val measuredHeight = element.offsetHeight.toFloat().dp
                                            webViewWidth.value = measuredWidth
                                            webViewHeight.value = measuredHeight
                                            println("${webViewWidth.value}, ${webViewHeight.value}")
                                        }
                                    }
                                )
                            }

                            if (idx != feature.second.lastIndex) HorizontalDivider(thickness = 1.dp)
                        }
                    }
                }
            }
        }
    }
}
