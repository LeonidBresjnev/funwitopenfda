package com.openfda.funwitopenfda


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.openfda.funwitopenfda.openfda.Feature
import com.openfda.funwitopenfda.openfda.Packages
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalMaterial3Api::class, ExperimentalUuidApi::class, ExperimentalComposeUiApi::class)
@Composable
fun FunWithOpenFDA(modifier: Modifier= Modifier) {

    var generic by rememberSaveable { mutableStateOf("") }
    var brand by rememberSaveable { mutableStateOf("") }
    var manufacturer by rememberSaveable { mutableStateOf("") }
    var indication by rememberSaveable { mutableStateOf("") }
    var maxHits by rememberSaveable { mutableStateOf(20) }

    var response by rememberSaveable { mutableStateOf<OpenFDAEntry?>(null) }
    var status by remember { mutableStateOf(0) }

    var extendedProductType by remember { mutableStateOf(false) }
    var selectedProductType by remember { mutableStateOf("Any") }

    var extendedRoute by remember { mutableStateOf(false) }
    var selectedRoute by remember { mutableStateOf("Any") }

    var showFeature by remember { mutableStateOf(false) }
    var showHtml by remember { mutableStateOf(false) }
    //var featureIdx by remember { mutableIntStateOf(-1) }
    var shownFeature by remember { mutableStateOf<Pair<String, List<String>>>(Pair("", emptyList())) }


    var showPackages by remember { mutableStateOf(false) }
    var productNdc by remember { mutableStateOf<List<String>>(emptyList()) }

    //var linkNext by remember { mutableStateOf("init") }
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    var filter by rememberSaveable { mutableStateOf<List<Boolean>> ( emptyList() ) }
    var applyFilter by remember { mutableStateOf(false) }

    //var htmlContent = false

    val productTypes = listOf("Any", "Human OTC drug", "Human prescription drug", "CELLULAR THERAPY")

    val routes = listOf("Any","ORAL","TOPICAL", "INTRAVENOUS","INTRAMUSCULAR","OPHTHALMIC","RESPIRATORY (INHALATION)","DENTAL","SUBCUTANEOUS","SUBLINGUAL","CUTANEOUS","NASAL",
"RECTAL","TRANSDERMAL","INFILTRATION","VAGINAL","PERINEURAL","AURICULAR (OTIC)","EPIDURAL","INTRA-ARTICULAR","INTRALESIONAL","INTRACAUDAL","SOFT TISSUE","BUCCAL","EXTRACORPOREAL","INTRATHECAL",
"PERIODONTAL","SUBMUCOSAL","IRRIGATION","PARENTERAL","PERCUTANEOUS","INTRA-ARTERIAL","INTRAVESICAL","INTRAOCULAR","INTRAVITREAL","INTRAPERITONEAL","INTRAVASCULAR","ENDOTRACHEAL","INTRADERMAL","RETROBULBAR",
"INTRACAVITARY","INTRASYNOVIAL","INTRAMEDULLARY","INTRAPLEURAL","HEMODIALYSIS","INTRAUTERINE","INTRAVENTRICULAR","INTRACARDIAC","SUBARACHNOID","INTRACAVERNOUS","OROPHARYNGEAL","SUBCONJUNCTIVAL",
"CONJUNCTIVAL","INTRABRONCHIAL","INTRASINAL","SUPRACHOROIDAL","ENDOCERVICAL","INTRACAMERAL","INTRAEPIDERMAL","TRANSMUCOSAL","ENTERAL","INTRACANALICULAR","INTRACEREBRAL",
"INTRACORONARY","INTRAGASTRIC","INTRALUMINAL","INTRALYMPHATIC","INTRAMENINGEAL","INTRATHORACIC","SUBGINGIVAL","TRANSTRACHEAL","URETERAL","URETHRAL")

    Column(modifier=modifier) {
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Adaptive(minSize = 280.dp),
            modifier=Modifier
                .padding(15.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalItemSpacing = 12.dp,
            contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp)
        ) {
            item {
                TextField(
                    value = generic,
                    onValueChange = { generic = it },
                    enabled = true,
                    singleLine = true,
                    label = { Text("generic name") },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Next
                    ),
                )
            }
            item {
                TextField(
                    value = brand,
                    onValueChange = { brand = it },
                    enabled = true,
                    singleLine = true,
                    label = { Text("brand name") },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Next
                    ),
                )
            }
            item {
                TextField(
                    value = manufacturer,
                    onValueChange = { manufacturer = it },
                    enabled = true,
                    singleLine = true,
                    label = { Text("Manufacturer") },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Next
                    ),
                )
            }
            item {
                TextField(
                    value = indication,

                    onValueChange = { indication = it },
                    enabled = true,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Next
                    ),
                    label = { Text("Indication") }
                )
            }

            item {
                ExposedDropdownMenuBox(
                    expanded = extendedProductType,
                    onExpandedChange = { extendedProductType = it }
                ) {
                    TextField(
                        modifier = Modifier.menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                        colors = ExposedDropdownMenuDefaults.textFieldColors(),
                        value = selectedProductType,
                        onValueChange = {},
                        singleLine = true,
                        label = { Text("Product type") },
                        keyboardOptions = KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Next
                        ),
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = extendedProductType)
                        },

                        )
                    DropdownMenu(
                        expanded = extendedProductType,
                        onDismissRequest = { extendedProductType = false }
                    ) {
                        productTypes.forEach {
                            DropdownMenuItem(
                                onClick = {
                                    selectedProductType = it
                                    extendedProductType = false
                                },
                                text = { Text(it) }
                            )
                        }
                    }
                }
            }

            item {
                ExposedDropdownMenuBox(
                    expanded = extendedRoute,
                    onExpandedChange = { extendedRoute = it }
                ) {
                    TextField(
                        modifier = Modifier.menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                        colors = ExposedDropdownMenuDefaults.textFieldColors(),
                        value = selectedRoute,
                        onValueChange = {},
                        singleLine = true,
                        label = { Text("Route of administration") },
                        keyboardOptions = KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Next
                        ),
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = extendedRoute)
                        },

                        )
                    DropdownMenu(
                        expanded = extendedRoute,
                        onDismissRequest = { extendedRoute = false }
                    ) {
                        routes.forEach {
                            DropdownMenuItem(
                                onClick = {
                                    selectedRoute = it
                                    extendedRoute = false
                                },
                                text = { Text(it) }
                            )
                        }
                    }
                }
            }

            item {
                TextField(
                    value = maxHits.toString(),
                    onValueChange = { maxHits = it.toIntOrNull() ?: maxHits },
                    enabled = true,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    label = { Text("hits per page") }
                )
            }
            item {
                Button(
                    enabled = !isLoading && (generic.length >= 3 || brand.length >= 3 || indication.length >= 3 || manufacturer.length >= 3),
                    onClick = {
                        /*CoroutineScope(context= Dispatchers.Default).launch {*/
                        scope.launch(context = Dispatchers.Default) {
                            val client = HttpClient {

                                install(HttpTimeout) {
                                    requestTimeoutMillis = 15_000   // whole request
                                    connectTimeoutMillis = 15_000   // TCP connect
                                    socketTimeoutMillis = 15_000

                                }

                                install(ContentNegotiation) {
                                    json(
                                        Json {
                                            ignoreUnknownKeys = true
                                            /*   isLenient = true*/
                                        },
                                        contentType = ContentType.Application.Json
                                    )
                                }
                            }
                            //println(client.engine.config.toString())

                            val genericQuery = if (generic.length >= 3) "+AND+openfda.generic_name:$generic*" else ""
                            val brandQuery = if (brand.length >= 3) "+AND+openfda.brand_name:$brand*" else ""
                            val manufactuerQuery =
                                if (manufacturer.length >= 3) "+AND+openfda.manufacturer_name:$manufacturer*" else ""
                            val producttypeQuery =
                                if (selectedProductType != "Any") "+AND+openfda.product_type:\"$selectedProductType\"" else ""
                            val routeQuery = if (selectedRoute != "Any") "+AND+openfda.route:$selectedRoute" else ""
                            val indicationQuery =
                                if (indication.length >= 3) "+AND+_exists_:indications_and_usage+AND+indications_and_usage:$indication*" else ""

                            val baseurl = "http://127.0.0.1:8080/openfda?search=_exists_:openfda"

                            isLoading = true
                            val resultDef = async {
                                val httpResponse: Result<HttpResponse> = runCatching {
                                    client.get(urlString = "$baseurl$genericQuery$brandQuery$manufactuerQuery$indicationQuery$producttypeQuery$routeQuery&limit=$maxHits")
                                }
                                println("inside async of FunWithOpenFDA - button click")
                                return@async httpResponse
                            }
                            val result = resultDef.await()
                            isLoading = false
                            result.onSuccess { action ->
                                status = action.status.value
                                val headers = action.headers.entries()
                                println("Headers:")
                                headers.forEach {
                                    println("${it.key}: ${it.value}, ${it.value.joinToString(", ")}")
                                }
                                //linkNext = action.headers["Link"] ?: ""
                                response = if (action.status == HttpStatusCode.OK) {
                                    action.body<OpenFDAEntry>()
                                } else {
                                    null
                                }
                                filter = emptyList()
                            }
                            result.onFailure { error ->
                                println(error.message)
                                response = null
                            }



                            client.close()
                        }
                    }
                ) {
                    Text("Search")
                }
            }
            item {
                Button(
                    enabled = (!isLoading) && (response != null) && (response!!.results.any { it.indications_and_usage.isNotEmpty() }) && (indication.isNotEmpty()),
                    onClick = {
                        isLoading = true
                        val client = HttpClient {
                            install(ContentNegotiation) {
                                json(
                                    Json {
                                        ignoreUnknownKeys = true
                                    },
                                    contentType = ContentType.Application.Json
                                )
                            }
                            install(HttpTimeout) {
                                requestTimeoutMillis = 15_000   // whole request
                                connectTimeoutMillis = 15_000   // TCP connect
                                socketTimeoutMillis = 15_000

                            }
                        }
                        val baseurl = "http://127.0.0.1:8080/context?"

                        scope.launch {

                            val labelList = response!!.results.map { it2 ->
                                it2.indications_and_usage.joinToString(". ").replace("\"", "'")
                            }
                            val uniqueSet = labelList.toSet()
                            println("size=${uniqueSet.size}")

                            val resultDef = async(context= Dispatchers.Default) {
                                val httpResponse: Result<HttpResponse> = runCatching {
                                    //println("inside runCatching")
                                    println(baseurl + "indication=$indication")
                                    val response = client.post(baseurl + "indication=$indication") {

                                        timeout {
                                            requestTimeoutMillis = 15000L
                                        }
                                        contentType(ContentType.Application.Json)
                                        headers {
                                            append(HttpHeaders.Accept, value = "application/json")
                                        }



                                        setBody(Json.encodeToJsonElement(uniqueSet))
                                    }
                                    return@runCatching response
                                }
                                return@async httpResponse
                            }
                            val result = resultDef.await()

                            val uniqueAnswers = result.run {
                                onSuccess { action ->
                                    if (action.status == HttpStatusCode.OK) {
                                        action.headers.entries().forEachIndexed { idx,it ->
                                            println("$idx ${it.key}: ${it.value}")
                                        }
                                        return@run action.body<List<Boolean>>()
                                    } else {
                                        return@run emptyList()
                                    }
                                }
                                onFailure { error ->
                                    println(error.message)
                                    return@run emptyList()
                                }
                                return@run emptyList()

                            }
                            if (uniqueAnswers.size == uniqueSet.size) {
                                val uniqueFilter = uniqueSet.map { it.hashCode() }.zip(uniqueAnswers).toMap()
                                filter = labelList.map { uniqueFilter[it.hashCode()] ?: false }

                            }
                            isLoading = false

                        }
                    }
                ) {
                    Text("context filter")
                }
            }
            item {
                Checkbox(
                    checked = applyFilter,
                    onCheckedChange = { applyFilter = it }
                )
            }
            /*
        IconButton(
            onClick = {
                /*CoroutineScope(context= Dispatchers.Default).launch {*/
                scope.launch(context = Dispatchers.Default) {
                    val client = HttpClient {

                        install(HttpTimeout) {
                            requestTimeoutMillis = 5000
                        }

                        install(ContentNegotiation) {
                            json(Json {
                                ignoreUnknownKeys = true
                                isLenient = true
                            })
                        }
                    }

                    val resultDef = async {
                        val httpResponse: Result<HttpResponse> = runCatching {
                            client.get(linkNext)
                        }
                        println("inside async of FunWithOpenFDA - button click")
                        return@async httpResponse
                    }
                    val result = resultDef.await()
                    result.onSuccess { action ->
                        status = action.status.value
                        linkNext = action.headers["LINK"] ?: ""
                        response = if (action.status == HttpStatusCode.OK) {
                            action.body<OpenFDAEntry>()
                        } else {
                            null
                        }
                    }
                    result.onFailure { error ->
                        response = null
                    }
                }
            },
            enabled = (linkNext.isNotEmpty()),
            content = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Next Page",
                )½
            }
        )*/
        }

        Column {
            if (status !in listOf(0,200)) Text("status: $status, ${HttpStatusCode.fromValue(value=status).description}")
            Text("Hits: ${response?.meta?.results?.total ?: 0} Results: ${response?.results?.size ?: 0}")
            //Text("Next page link: $linkNext")

            response?.run {
                if (!isLoading) {
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Adaptive(860.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalItemSpacing = 16.dp,
                        contentPadding = PaddingValues(
                            start =  8.dp,
                            end =  8.dp,
                            top = 8.dp,
                            bottom =  8.dp,
                        )
                    ) {
                        itemsIndexed(
                            items = if (applyFilter && (filter.size == response?.results?.size )) {
                                response?.results?.filterIndexed { idx, _ -> filter[idx] } ?: emptyList()
                            }
                            else this@run.results,
                            key = { _, it -> it.key }
                        ) { idx,item ->
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                                modifier = Modifier.padding(5.dp).sizeIn(maxWidth = 860.dp, maxHeight = 300.dp)
                            ) {
                                Row(modifier = Modifier.padding(5.dp).fillMaxSize()) {
                                    Column(
                                        modifier = Modifier.weight(0.5f)
                                            .fillMaxHeight()
                                            .verticalScroll(rememberScrollState())
                                    ) {
                                        Text(text = "General information", style = MaterialTheme.typography.headlineMedium)
                                        Text("Generic Name(s): ${item.openfda.generic_name.joinToString(", ")}",style = MaterialTheme.typography.bodyLarge)
                                        Text("Brand Name(s): ${item.openfda.brand_name.joinToString(", ")}",style = MaterialTheme.typography.bodyLarge)
                                        Text("Substance Name(s): ${item.openfda.substance_name.joinToString(", ")}",style = MaterialTheme.typography.bodyLarge)
                                        Text("Product Type(s): ${item.openfda.product_type.joinToString(", ")}",style = MaterialTheme.typography.bodyLarge)
                                        Text("Manufacturer name(s): ${item.openfda.manufacturer_name.joinToString(", ")}",style = MaterialTheme.typography.bodyLarge)
                                        Text("Route(s) of administration: ${item.openfda.route.joinToString(", ")}",style = MaterialTheme.typography.bodyLarge)
                                        Text( text="Package information", color= Color.Blue,style = MaterialTheme.typography.bodyLarge,
                                            modifier = Modifier.clickable(
                                                onClick = {
                                                    productNdc = item.openfda.product_ndc
                                                    showPackages=true
                                                }
                                            )
                                        )
                                        if (filter.size == response?.results?.size ) {
                                            Text("Indication in right context: ${if (filter[idx]) "Yes" else "No"} ")
                                        }

                                    }
                                    val features = listOf(

                                        Pair("Indication and usage", item.indications_and_usage),
Pair("Purpose", item.purpose),

                                        Pair("Dosage and administration", item.dosage_and_administration),
                                        Pair("Dosage form and strength", item.dosage_forms_and_strengths),
                                        Pair("Contraindications", item.contraindications),
                                        Pair("Warnings and cautions", item.warnings_and_cautions),
                                        Pair("Boxed warnings", item.boxed_warning),
                                        Pair("Warnings", item.warnings),
                                        Pair("Precautions", item.precautions),
                                        Pair("User safety warnings", item.user_safety_warnings),
                                        Pair("General precautions", item.general_precautions),

                                        Pair("Adverse reactions", item.adverse_reactions),
                                        Pair("Drug interactions", item.drug_interactions),
                                        Pair("Drug and/or laboratory test interactions",item.drug_and_or_laboratory_test_interactions),


                                        Pair("Use in specific populations", item.use_in_specific_populations),
                                        Pair("Pregnancy", item.pregnancy),
                                        Pair("Pregnancy or breast feeding", item.pregnancy_or_breast_feeding),
                                        Pair("Labor and delivery",item.labor_and_delivery),
                                        Pair("Nursing mothers",item.nursing_mothers),
                                        Pair("Pediatric use",item.pediatric_use),
                                        Pair("Geriatric use", item.geriatric_use),
                                        Pair("Teratogenic", item.teratogenic_effects),

                                        Pair("Drug abuse and dependence",item.drug_abuse_and_dependence),
                                        Pair("Controlled substance",item.controlled_substance),
                                        Pair("Abuse",item.abuse),
                                        Pair("Dependence",item.dependence),

                                        Pair("Overdosage", item.overdosage),

                                        Pair("Description", item.description),

                                        Pair("Clinical pharmacology", item.clinical_pharmacology),
                                        Pair("Mechanism of action", item.mechanism_of_action),
                                        Pair("Pharmacodynamics", item.pharmacodynamics),
                                        Pair("Pharmacokinetics", item.pharmacokinetics),

                                        Pair("Microbiology", item.microbiology),

                                        Pair("Nonclinical toxicology", item.nonclinical_toxicology),
                                        Pair(
                                            "Carcinogenesis and mutagenesis and impairment of fertility",
                                            item.carcinogenesis_and_mutagenesis_and_impairment_of_fertility
                                        ),
                                        Pair("Animal pharmacology and/or toxicology",item.animal_pharmacology_and_or_toxicology),

                                        Pair("Clinical studies", item.clinical_studies),
                                        Pair("How supplied", item.how_supplied),
                                        Pair("Storage and handling",item.storage_and_handling),
                                        Pair("Safe handling warning",item.safe_handling_warning),

                                        Pair("Information for patients", item.information_for_patients),
                                        Pair("Patient medication information", item.patient_medication_information),
                                        Pair("Ask doctor", item.ask_doctor),
                                        Pair("Ask doctor or pharmacist",item.ask_doctor_or_pharmacist),
                                        Pair("Do not use", item.do_not_use),
                                        Pair("Information for owners or caregivers",item.information_for_owners_or_caregivers),
                                        Pair("Instructions for use",item.instructions_for_use),
                                        Pair("Keep out of reach of children",item.keep_out_of_reach_of_children),
                                        Pair("Other safety information",item.other_safety_information),
                                        Pair("Questions",item.questions),

                                        Pair("Med. guide", item.spl_medguide),
                                        Pair(
                                            "Package label principal display panel",
                                            item.package_label_principal_display_panel
                                        ),
                                        Pair("Stop use", item.stop_use),
                                        Pair("When use", item.when_using),
                                        Pair("Patient package insert information",item.spl_patient_package_insert),
                                        Pair("Unclassified section", item.spl_unclassified_section),

                                        Pair("Laboratory tests", item.laboratory_tests),
                                        Pair("Recent major changes", item.recent_major_changes),
                                        Pair("References", item.references),
                                        Pair("Product data elements",item.spl_product_data_elements),
                                        Pair("Active ingredient(s)", item.active_ingredient),
                                        Pair("Inactive ingredient(s)", item.inactive_ingredient)/*,
                                        Pair("Effective time", item.effective_time)*/
                                    ).filter { it.second.isNotEmpty() }

                                    val tables = listOf(
                                        Pair("Clinical pharmacology", item.clinical_pharmacology_table),
                                        Pair("Microbiology", item.microbiology_table),
                                        Pair("Dosage and administration", item.dosage_and_administration_table),
                                        Pair("Dosage form and strength", item.dosage_forms_and_strengths_table),
                                        Pair("Warnings and cautions", item.warnings_and_cautions_table),
                                        Pair("Adverse reactions",item.adverse_reactions_table),
                                        Pair("Drug interactions", item.drug_interactions_table),
                                        Pair("How supplied", item.how_supplied_table),
                                        Pair("Clinical studies",item.clinical_studies_table),
                                        Pair("Patient package insert information",item.spl_patient_package_insert_table),
                                        Pair("Med. guide", item.spl_medguide_table),
                                        Pair("Unclassified section", item.spl_unclassified_section_table),
                                        Pair("Recent major changes", item.recent_major_changes_table)
                                    ).filter { it.second.isNotEmpty() }

                                    if (features.isNotEmpty()) {
                                        Column(modifier = Modifier.weight(0.5f)) {
                                            Text(text = "Details", style = MaterialTheme.typography.headlineMedium)
                                            LazyColumn {
                                                itemsIndexed(items = features, key = { _, it -> it.first }) { idx, iu ->
                                                    Text(
                                                        modifier = Modifier.clickable(
                                                            enabled = iu.second.isNotEmpty(),
                                                            onClick = {
                                                                showFeature = true
                                                                showHtml = false
                                                                shownFeature = features[idx]
                                                            }
                                                        ),
                                                        text = iu.first,
                                                        style = MaterialTheme.typography.bodyLarge,
                                                        color = Color.Blue)
                                                    HorizontalDivider(thickness = 1.dp)
                                                }
                                            }
                                        }
                                    }
                                    if (tables.isNotEmpty()) {
                                        Column(modifier = Modifier.weight(0.5f)) {
                                            Text(text = "Tables", style = MaterialTheme.typography.headlineMedium)
                                            LazyColumn {
                                                itemsIndexed(items = tables, key = { _, it -> it.first }) { idx, iu ->
                                                    Text(
                                                        modifier = Modifier.clickable(
                                                            enabled = iu.second.isNotEmpty(),
                                                            onClick = {
                                                                showFeature = true
                                                                showHtml = true
                                                                shownFeature = tables[idx]
                                                            }
                                                        ),
                                                        text = iu.first,
                                                        style = MaterialTheme.typography.bodyLarge,
                                                        color = Color.Blue)
                                                    HorizontalDivider(thickness = 1.dp)
                                                }
                                            }
                                        }
                                    }


                                }
                            }
                        }
                    }
                }
                else {
                    CircularProgressIndicator()
                }
            }
        }
        if (showFeature) {
            //println("featureIdx: $featureIdx")
            //println("features: ${shownFeature.first}, ${shownFeature.second.joinToString(", ")}")
            Feature(
                feature = shownFeature,
                onDismissRequest = { showFeature = false },
                searchStr = indication,
                html = showHtml
            )
        }
        if (showPackages) {
            //println("featureIdx: $featureIdx")
            //println("features: ${shownFeature.first}, ${shownFeature.second.joinToString(", ")}")
            Packages(
                productNdc = productNdc,
                onDismissRequest = { showPackages = false },
            )
        }
    }
}

