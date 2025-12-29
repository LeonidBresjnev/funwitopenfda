package com.openfda.funwitopenfda.openfda

import androidx.compose.runtime.MutableState

data class SearchField(val label: String,
                       var field: MutableState<String>,
                       val openFDAName: String,
                       val onUpdate: (String) -> Unit)
