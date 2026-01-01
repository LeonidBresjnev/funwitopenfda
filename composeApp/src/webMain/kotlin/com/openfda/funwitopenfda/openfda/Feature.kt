package com.openfda.funwitopenfda.openfda

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
import org.w3c.dom.HTMLDivElement

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Feature(feature: Pair<String,List<String>>,
            onDismissRequest: () -> Unit,
            searchStrs: List<String> = emptyList(),
            html: Boolean=false) {


    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false // Allows custom sizing
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .border(1.dp, Color.Black),
            shape = MaterialTheme.shapes.large,
            tonalElevation = 6.dp
        ) {
            Column(modifier= Modifier.padding(16.dp)) {
                val state = rememberLazyListState()
                Text(text=feature.first, style = MaterialTheme.typography.displayMedium.copy(fontWeight = Bold))

                if (!html) {

                    Box(modifier=Modifier.fillMaxSize()) {
                        LazyColumn(
                            state = state,
                            modifier = Modifier.fillMaxSize().padding(end = 12.dp) // Leave space for bar
                        ) {
                            itemsIndexed(items = feature.second) { idx, iu ->
                                val unquoted = searchStrs.map {
                                    it.lowercase().removeSurrounding("\"")
                                }
                                //println("searchstr: $searchStr unquoted: $unquoted")
                                if (unquoted.any { it.isNotBlank() }) {
                                    val rabinKarp = iu.lowercase().rabinKarp(unquoted)
                                    val builder = AnnotatedString.Builder()
                                    builder.append(iu)
                                    rabinKarp.zip(unquoted).forEach {
                                        it.first.forEach { it2 ->
                                            builder.addStyle(
                                                style = SpanStyle(fontWeight = Bold),
                                                start = it2,
                                                end = it2 + it.second.length
                                            )
                                        }
                                    }
                                    //println(rabinKarp.joinToString(", "))
                                    SelectionContainer {
                                        Text(
                                            text = builder.toAnnotatedString()/*, style = MaterialTheme.typography.bodyLarge*/
                                        )
                                    }
                                } else {
                                    SelectionContainer {
                                        Text(text = iu)
                                    }
                                }

                                if (idx != feature.second.lastIndex) HorizontalDivider(thickness = 1.dp)
                            }

                        }

                        VerticalScrollbar(
                            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                            adapter = rememberScrollbarAdapter(scrollState = state)
                        )
                    }
                } else {

                        Column(modifier = Modifier.fillMaxSize()) {
                            feature.second.forEachIndexed { idx, iu ->
                                //println("table $idx, $iu")
                                Box(
                                    modifier = Modifier.weight(1f)

                                        .border(1.dp, Color.Black)
                                ) {
                                    WebElementView(
                                        modifier = Modifier.padding(10.dp)
                                            /* .sizeIn(
                                            minWidth = webViewWidth.value+50.dp,
                                            minHeight = webViewHeight.value+50.dp)*/ // Fallback min sizes
                                            .fillMaxSize(),
                                        factory = {
                                            val tableElement = (document.createElement("div") as HTMLDivElement).apply {
                                                style.overflowY = "auto"
                                                style.maxHeight = "100%"
                                                style.overflowX = "auto"
                                                style.maxWidth = "100%"
                                                /* style.padding="10px"*/

                                            }

                                            //tableElement.innerHTML = iu
                                            return@WebElementView tableElement
                                        },
                                        update = { element ->
                                            element.innerHTML = iu
                                            /* coroutineScope.launch {
                                            delay(300) // Brief delay for DOM to settle (adjust if needed)
                                            val measuredWidth = element.offsetWidth.toFloat().dp
                                            val measuredHeight = element.offsetHeight.toFloat().dp
                                            webViewWidth.value = measuredWidth
                                            webViewHeight.value = measuredHeight
                                            println("${webViewWidth.value}, ${webViewHeight.value}")
                                        }*/
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
