package com.openfda.funwitopenfda.openfda

data class SearchField(val label: String,
                       var value: String,
                       val openFDAName: String,
                       val onUpdate: (String) -> Unit)
