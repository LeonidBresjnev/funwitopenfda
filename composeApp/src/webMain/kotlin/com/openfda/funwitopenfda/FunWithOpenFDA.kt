package com.openfda.funwitopenfda


import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
//import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import com.openfda.funwitopenfda.openfda.Feature
import com.openfda.funwitopenfda.openfda.Packages
import com.openfda.funwitopenfda.openfda.SearchField
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalMaterial3Api::class, ExperimentalUuidApi::class, ExperimentalComposeUiApi::class)
@Composable
fun FunWithOpenFDA(
    modifier: Modifier= Modifier,
    httpClient: HttpClient) {

    val generic = rememberSaveable { mutableStateOf("") }
    val brand = rememberSaveable { mutableStateOf("") }
    val manufacturer = rememberSaveable { mutableStateOf("") }
    val indication = rememberSaveable { mutableStateOf("") }
    val adverseEvent = rememberSaveable { mutableStateOf("") }
    val substance = rememberSaveable { mutableStateOf("") }
    val pharm_class_cs = rememberSaveable { mutableStateOf("") }
    val pharm_class_epc = rememberSaveable { mutableStateOf("") }
    val pharm_class_pe = rememberSaveable { mutableStateOf("") }
    val pharm_class_moa = rememberSaveable { mutableStateOf("") }

    var maxHits by rememberSaveable { mutableStateOf(20) }

    var response by rememberSaveable { mutableStateOf<OpenFDAEntry?>(null) }
    var status by remember { mutableStateOf(0) }

    var extendedProductType by remember { mutableStateOf(false) }
    var selectedProductType by remember { mutableStateOf("Any") }

    var extendedRoute by remember { mutableStateOf(false) }
    var selectedRoute by remember { mutableStateOf("Any") }

    var showFeature by remember { mutableStateOf(false) }
    var showHtml by remember { mutableStateOf(false) }

    var shownFeature by remember { mutableStateOf<Pair<String, List<String>>>(Pair("", emptyList())) }


    var showPackages by remember { mutableStateOf(false) }
    var productNdc by remember { mutableStateOf<List<String>>(emptyList()) }

    var linkNext by remember { mutableStateOf("init") }
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    var contextTerm by rememberSaveable { mutableStateOf("") }

    var currentPageInterval by rememberSaveable { mutableStateOf(-1..-1) }
    //var currentPage by rememberSaveable { mutableIntStateOf(-1)}
    var loadedPages by rememberSaveable { mutableIntStateOf(0)}
    var totalPages by rememberSaveable { mutableIntStateOf(0)}

    val repository = remember { mutableListOf<OpenFDAResultEntry>() }

    val pagerState = rememberPagerState(pageCount = {
        totalPages
    })
    val contextValues = rememberSaveable {
        mutableMapOf<String, Boolean>()
    }

    //var htmlContent = false

    val productTypes = listOf("Any", "Human OTC drug", "Human prescription drug", "CELLULAR THERAPY")

    val routes = listOf("Any","ORAL","TOPICAL", "INTRAVENOUS","INTRAMUSCULAR","OPHTHALMIC","RESPIRATORY (INHALATION)","DENTAL","SUBCUTANEOUS","SUBLINGUAL","CUTANEOUS","NASAL",
"RECTAL","TRANSDERMAL","INFILTRATION","VAGINAL","PERINEURAL","AURICULAR (OTIC)","EPIDURAL","INTRA-ARTICULAR","INTRALESIONAL","INTRACAUDAL","SOFT TISSUE","BUCCAL","EXTRACORPOREAL","INTRATHECAL",
"PERIODONTAL","SUBMUCOSAL","IRRIGATION","PARENTERAL","PERCUTANEOUS","INTRA-ARTERIAL","INTRAVESICAL","INTRAOCULAR","INTRAVITREAL","INTRAPERITONEAL","INTRAVASCULAR","ENDOTRACHEAL","INTRADERMAL","RETROBULBAR",
"INTRACAVITARY","INTRASYNOVIAL","INTRAMEDULLARY","INTRAPLEURAL","HEMODIALYSIS","INTRAUTERINE","INTRAVENTRICULAR","INTRACARDIAC","SUBARACHNOID","INTRACAVERNOUS","OROPHARYNGEAL","SUBCONJUNCTIVAL",
"CONJUNCTIVAL","INTRABRONCHIAL","INTRASINAL","SUPRACHOROIDAL","ENDOCERVICAL","INTRACAMERAL","INTRAEPIDERMAL","TRANSMUCOSAL","ENTERAL","INTRACANALICULAR","INTRACEREBRAL",
"INTRACORONARY","INTRAGASTRIC","INTRALUMINAL","INTRALYMPHATIC","INTRAMENINGEAL","INTRATHORACIC","SUBGINGIVAL","TRANSTRACHEAL","URETERAL","URETHRAL")

    val linkStyles = TextLinkStyles(
        style = SpanStyle(color = Color.Blue, textDecoration = TextDecoration.Underline),
        hoveredStyle = SpanStyle(color = Color.Cyan),
        pressedStyle = SpanStyle(color = Color.Red)
    )

    val focusRequester = remember { FocusRequester() }


    val allFields = listOf(
        SearchField(label="Generic name", field=generic, onUpdate= { generic.value = it }, openFDAName = "openfda.generic_name"),
        SearchField(label="Brand name", field=brand, onUpdate= { brand.value = it }, openFDAName = "openfda.brand_name"),
        SearchField(label="Manufacturer", field=manufacturer, onUpdate= {manufacturer.value = it }, openFDAName = "openfda.manufacturer_name"),
        SearchField(label="Substance", field=substance, onUpdate= { substance.value = it }, openFDAName = "openfda.substance_name"),
        SearchField(label="Indication", field=indication, onUpdate= { indication.value = it }, openFDAName = "indications_and_usage"),
        SearchField(label="Adverse reaction", field=adverseEvent, onUpdate= { adverseEvent.value = it }, openFDAName = "adverse_reactions"),
        SearchField(label="Chemical structure class", field=pharm_class_cs, onUpdate= { pharm_class_cs.value = it }, openFDAName = "openfda.pharm_class_cs"),
        SearchField(label="Established Pharmacologic class", field=pharm_class_epc, onUpdate= { pharm_class_epc.value = it }, openFDAName = "openfda.pharm_class_epc"),
        SearchField(label="Physiologic/pharmacodynamic effect", field=pharm_class_pe, onUpdate= { pharm_class_pe.value = it }, openFDAName = "openfda.pharm_class_pe"),
        SearchField(label="Mechanism of action class", field=pharm_class_moa, onUpdate= {pharm_class_moa.value = it }, openFDAName = "openfda.pharm_class_moa"),
    )
    val onSuccess: suspend (HttpResponse) -> Unit = { action ->
        status = action.status.value
        linkNext = action.headers["link"]?.split(";")[0]?.removeSurrounding("<", ">") ?: ""
        response = if (action.status==HttpStatusCode.OK) {
            action.body<OpenFDAEntry>()
        } else {
            null
        }
        response?.results?.apply {
            repository.addAll(elements = this)
        }
        loadedPages++
    }

    Column(modifier=modifier) {
        Row(modifier=Modifier.fillMaxWidth()) {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Adaptive(minSize = 280.dp),
                modifier = Modifier
                    .padding(2.dp)
                    .weight(1f)
                    ,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalItemSpacing = 12.dp,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp)
            ) {
                allFields.forEachIndexed { idx, it ->
                    item(key = idx, contentType = 1) {
                        TextField(
                            value = it.field.value,
                            onValueChange = it.onUpdate,
                            enabled = true,
                            singleLine = true,
                            label = { Text(it.label) },
                            keyboardOptions = KeyboardOptions.Default.copy(
                                imeAction = ImeAction.Next
                            ),
                        )
                    }
                }

                item(key = allFields.size+0, contentType = 1) {
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

                item(key = allFields.size+1, contentType = 1) {
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

                item(key = allFields.size+2, contentType = 1) {
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


            }
            Button(modifier=Modifier
                .padding(14.dp).width(280.dp)
            .wrapContentWidth() // only needed width
                .align(Alignment.Bottom),
                enabled = !isLoading && allFields.any { it.field.value.length >= 3},
                onClick = {

                    isLoading = true
                    repository.clear()
                    contextValues.clear()
                    loadedPages = 0
                    //currentPage = -1
                    currentPageInterval = -1..-1
                    linkNext = ""
                    /*CoroutineScope(context= Dispatchers.Default).launch {*/
                    scope.launch(context = Dispatchers.Default) {
                        //println(client.engine.config.toString())
                        val queries=allFields
                            .filter { it.field.value.isNotBlank() }
                            .joinToString(prefix="+AND+", separator = "+AND+") {
                                "_exists_:${it.openFDAName}+AND+${it.openFDAName}:${it.field.value}*"
                        }

                        val producttypeQuery =
                            if (selectedProductType != "Any") "+AND+openfda.product_type:\"$selectedProductType\"" else ""
                        val routeQuery = if (selectedRoute != "Any") "+AND+openfda.route:$selectedRoute" else ""


                        val baseurl = "http://10.11.12.120:$SERVER_PORT/openfda?search=_exists_:openfda"
                        //val baseurl="https://visualopenfda.ew.r.appspot.com/openfda?search=_exists_:openfda"
                       // println("$baseurl$queries$producttypeQuery$routeQuery&limit=$maxHits")
                        val resultDef = async {
                            val httpResponse: Result<HttpResponse> = runCatching {
                                httpClient.get(urlString = "$baseurl$queries$producttypeQuery$routeQuery&limit=$maxHits")
                            }
                            //println("inside async of FunWithOpenFDA - button click")
                            return@async httpResponse
                        }
                        val result = resultDef.await()
                        isLoading = false
                        result.onSuccess { action ->
                            onSuccess(action)

                            contextTerm = indication.value.ifEmpty { adverseEvent.value.ifEmpty { "" } }
                            focusRequester.requestFocus()
                            totalPages = response?.let {
                                return@let ((it.meta.results.total+ it.meta.results.limit-1)/ it.meta.results.limit )
                            } ?: 0

                            launch {
                                pagerState.scrollToPage(0)
                                snapshotFlow { pagerState.isScrollInProgress }
                                    .filter { !it }
                                    .first()

                               // println("Pager is now completely stationary at page 0")
                            }

                              //  println(pagerState.currentPage)
                                if (repository.isNotEmpty()) {
                                    currentPageInterval = 0.. repository.lastIndex
                                }


                        }
                        result.onFailure { error ->
                            repository.clear()
                            totalPages = 0
                            currentPageInterval = 0..0
                            println(error.message)
                            response = null
                            contextTerm = ""
                        }
                    }

                }
            ) {
                Text("Search")
            }
        }
        HorizontalDivider()

        Column(modifier=Modifier.padding(16.dp)) {
            Text("Context filter")
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                TextField(
                    value=contextTerm,
                    onValueChange = { contextTerm = it },
                    enabled = true,
                    singleLine = true,
                    label = { Text("context term") },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Next
                    ),
                )

                Button(
                    enabled = (!isLoading) && (response != null) && (response!!.results.any { it.indications_and_usage.isNotEmpty() }) && (contextTerm.isNotEmpty()),
                    onClick = {
                        isLoading = true
                        val baseurl = "http://10.11.12.120:$SERVER_PORT/context?"

                        scope.launch {

                            val labelList = response!!.results.map { it2 ->
                                Pair(it2.indications_and_usage
                                    .joinToString(". ")
                                    .replace("\"", "'"),it2.id)
                            }
                            val uniqueSet = labelList.map{ it.first }.toSet()
                          //  println("size=${uniqueSet.size}")

                            val resultDef = async(context = Dispatchers.Default) {
                                val httpResponse: Result<HttpResponse> = runCatching {
                                    //println("inside runCatching")
                                 //   println(baseurl + "indication=$indication")
                                    val response = httpClient.post(baseurl + "indication=$contextTerm") {

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
                                       /* action.headers.entries().forEachIndexed { idx, it ->
                                          //  println("$idx ${it.key}: ${it.value}")
                                        }*/
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

                            val uniqueFilter = uniqueSet
                                .map { it.hashCode() }
                                .zip(uniqueAnswers)
                                .toMap()

                            labelList.forEach {
                                uniqueFilter[it.first.hashCode()]?.let { it2->
                                    contextValues[it.second]= it2
                                }
                            }
                            isLoading = false

                        }
                    }
                ) {
                    Text("context filter")
                }

            }
        }

        HorizontalDivider()

        Row(modifier=Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = {
                    scope.launch {
                        if (pagerState.currentPage > 0) {
                            pagerState.animateScrollToPage(pagerState.currentPage - 1)

                            if (repository.isNotEmpty()) {
                                currentPageInterval = pagerState.currentPage*maxHits.coerceIn(0, repository.lastIndex)..((pagerState.currentPage+1)*maxHits-1).coerceIn(0,repository.lastIndex)
                            }
                        }
                    }
                },
                enabled = (/*currentPageInterval.first > 1*/pagerState.canScrollBackward),
                content = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Previous Page",
                    )
                }
            )
            IconButton(
                onClick = {
                    if (pagerState.currentPage+1<loadedPages) {
                        //currentPage++
                        scope.launch {
                            if (pagerState.currentPage < pagerState.pageCount-1) {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                currentPageInterval = pagerState.currentPage*maxHits.coerceIn(0, repository.lastIndex)..((pagerState.currentPage+1)*maxHits-1).coerceIn(0,repository.lastIndex)
                            }
                        }
                    } else {

                        //println("repository size: ${repository.size}")
                        isLoading = true
                        scope.launch(context = Dispatchers.Default) {

                            val resultDef = async {
                                val httpResponse: Result<HttpResponse> = runCatching {

                                    httpClient.get("http://10.11.12.120:$SERVER_PORT/openfda?link=$linkNext")
                                }
                                //println("inside async of FunWithOpenFDA - button click")
                                return@async httpResponse
                            }
                            val result = resultDef.await()
                            result.onSuccess { action ->
                                onSuccess(action)

                                    if (pagerState.currentPage < pagerState.pageCount-1) {
                                        pagerState.scrollToPage(pagerState.currentPage + 1)
                                        currentPageInterval = pagerState.currentPage*maxHits.coerceIn(0, repository.lastIndex)..((pagerState.currentPage+1)*maxHits-1).coerceIn(0,repository.lastIndex)
                                    }

                            }
                            result.onFailure { error ->
                                println(error.message)
                                response = null
                            }

                            isLoading = false
                        }


                    }
                },
                enabled = /*linkNext.isNotEmpty()||(currentPage<loadedPages)*/ pagerState.canScrollForward,
                content = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next Page",
                    )
                }
            )
            if (status !in listOf(0,200)) Text("status: $status, ${HttpStatusCode.fromValue(value = status).description}")
            if (repository.isNotEmpty()) {
                Text("Page ${pagerState.currentPage + 1} of ${pagerState.pageCount}. Item ${currentPageInterval.first() + 1} to ${currentPageInterval.last() + 1} of ${response?.meta?.results?.total ?: 0}")
            }
            else Text("No items")
            //Text("link: $linkNext")
        }
        HorizontalDivider()



        if (!isLoading && repository.isNotEmpty() && (currentPageInterval.first>=0)) {

            var containerHeight by remember { mutableStateOf(0) }

            LaunchedEffect(repository.size) {
                /*val focusresult =*/ focusRequester.requestFocus(focusDirection = FocusDirection.Enter)
             //   println("Focus result: $focusresult")
            }

            HorizontalPager(state=pagerState) { page ->
                val gridState = rememberLazyGridState() // 1. Remember state
                val scope = rememberCoroutineScope()
                val dragState = rememberDraggableState { delta ->
                    scope.launch {
                        // We scroll by negative delta because dragging 'down' (positive)
                        // should move the content 'up'
                        gridState.scrollBy(-delta)
                    }
                }
                Box(modifier = Modifier.fillMaxSize()) {
                    LazyVerticalGrid(
                        state = gridState,
                        modifier = Modifier
                            .padding(top = 2.dp, end = 24.dp)
                            .draggable(state = dragState, orientation = Orientation.Vertical)

                            .focusRequester(focusRequester)
                            .focusable()
                            .onGloballyPositioned { coordinates ->
                                containerHeight = coordinates.size.height
                            }
                            .onKeyEvent { keyEvent ->
                                if (keyEvent.type==KeyEventType.KeyDown) {
                                    val scrollAmount = 20f
                                    when (keyEvent.key) {
                                        Key.PageDown -> {
                                            scope.launch {
                                                gridState.scrollBy(containerHeight.toFloat())
                                            }
                                            true
                                        }

                                        Key.PageUp -> {
                                            scope.launch {
                                                gridState.scrollBy(-containerHeight.toFloat())
                                            }
                                            true
                                        }

                                        Key.DirectionUp -> {
                                            scope.launch {
                                                gridState.scrollBy(-scrollAmount)
                                            }
                                            true
                                        }

                                        Key.DirectionDown -> {
                                            scope.launch {
                                                gridState.scrollBy(scrollAmount)
                                            }
                                            true
                                        }

                                        else -> false
                                    }
                                } else false
                            },
                        columns = GridCells.Adaptive(minSize = 860.dp),
                        horizontalArrangement = Arrangement.spacedBy(space = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(space = 16.dp),
                        /*verticalItemSpacing = 16.dp,*/
                        contentPadding = PaddingValues(
                            start = 8.dp,
                            end = 8.dp,
                            top = 8.dp,
                            bottom = 8.dp,
                        )
                    ) {
                        items(
                            items = repository.subList(
                                fromIndex = (page * maxHits).coerceIn(0, repository.lastIndex),
                                toIndex = ((page + 1) * maxHits).coerceIn(0, repository.lastIndex + 1)),
                            key = {  it.key }
                        ) {  item ->
                            val backgroundColor = when (contextValues[item.id]) {
                                true -> Color(0xFFE8F5E9) // Light Green
                                false -> Color(0xFFFFEBEE) // Light Red
                                null -> CardDefaults.cardColors().containerColor // Default
                            }

                            Card(
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                                colors = CardDefaults.cardColors(containerColor = backgroundColor),
                                modifier = Modifier
                                    .padding(5.dp)
                                    .sizeIn(maxWidth = 860.dp, maxHeight = 300.dp)
                            ) {
                                Row(modifier = Modifier.padding(5.dp).fillMaxSize()) {
                                    Column(
                                        modifier = Modifier.padding(2.dp).weight(0.5f)
                                            .fillMaxHeight()
                                    ) {
                                        Text(
                                            text = "General information",
                                            style = MaterialTheme.typography.headlineMedium
                                        )
                                        val scrollState = rememberScrollState()

                                        Box(modifier = Modifier.fillMaxSize()) {
                                            val basics = listOf(
                                                Pair("Generic Name", item.openfda.generic_name),
                                                Pair("Brand Name",item.openfda.brand_name),
                                                Pair("Substance Name(s)",item.openfda.substance_name),
                                                Pair("Product Type(s)",item.openfda.product_type),
                                                Pair("Product NDC(s)",item.openfda.product_ndc),
                                                Pair("Manufacturer name(s)",item.openfda.manufacturer_name),
                                                Pair("Route(s) of administration",item.openfda.route),
                                                Pair("Chemical structure class",item.openfda.pharm_class_cs),
                                                Pair("Established pharmacologic class",item.openfda.pharm_class_epc),
                                                Pair("Physiologic/pharmacodynamic effect class",item.openfda.pharm_class_pe),
                                                Pair("Mechanism of action class",item.openfda.pharm_class_moa),
                                            ).filter { it.second.isNotEmpty() }
                                            Column(
                                                modifier = Modifier.fillMaxSize()
                                                    .padding(end = 12.dp)
                                                    .verticalScroll(scrollState)
                                            ) {
                                                basics.forEach { (label, value) ->
                                                    val builder = AnnotatedString.Builder()
                                                    builder.append("$label: ${value.joinToString(", ")}")
                                                    builder.addStyle(
                                                        style = SpanStyle(fontWeight = Bold),
                                                        start = 0,
                                                        end =  label.length+1
                                                    )
                                                    SelectionContainer {
                                                        Text(text=builder.toAnnotatedString())
                                                    }
                                                }
                                                val packageInfo = buildAnnotatedString {
                                                    withLink(
                                                        LinkAnnotation.Clickable(
                                                            tag = "package_info",
                                                            styles = linkStyles,
                                                            linkInteractionListener = {
                                                                // This opens your dialog by updating state
                                                                productNdc = item.openfda.product_ndc
                                                                showPackages = true
                                                            }
                                                        )
                                                    ) {
                                                        append("Package information")
                                                    }
                                                }
                                                Text(text = packageInfo)
                                                /*
                                            Text(
                                                text = "Package information",
                                                color = Color.Blue,
                                                style = TextLinkStyles /* MaterialTheme.typography.bodyLarge*/,
                                                modifier = Modifier.clickable(
                                                    onClick = {
                                                        productNdc = item.openfda.product_ndc
                                                        showPackages = true
                                                    }
                                                )
                                            )*/


                                            }

                                            VerticalScrollbar(
                                                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                                                adapter = rememberScrollbarAdapter(scrollState = scrollState)
                                            )
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
                                        Pair(
                                            "Drug and/or laboratory test interactions",
                                            item.drug_and_or_laboratory_test_interactions
                                        ),


                                        Pair("Use in specific populations", item.use_in_specific_populations),
                                        Pair("Pregnancy", item.pregnancy),
                                        Pair("Pregnancy or breast feeding", item.pregnancy_or_breast_feeding),
                                        Pair("Labor and delivery", item.labor_and_delivery),
                                        Pair("Nursing mothers", item.nursing_mothers),
                                        Pair("Pediatric use", item.pediatric_use),
                                        Pair("Geriatric use", item.geriatric_use),
                                        Pair("Teratogenic", item.teratogenic_effects),

                                        Pair("Drug abuse and dependence", item.drug_abuse_and_dependence),
                                        Pair("Controlled substance", item.controlled_substance),
                                        Pair("Abuse", item.abuse),
                                        Pair("Dependence", item.dependence),

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
                                        Pair(
                                            "Animal pharmacology and/or toxicology",
                                            item.animal_pharmacology_and_or_toxicology
                                        ),

                                        Pair("Clinical studies", item.clinical_studies),
                                        Pair("How supplied", item.how_supplied),
                                        Pair("Storage and handling", item.storage_and_handling),
                                        Pair("Safe handling warning", item.safe_handling_warning),

                                        Pair("Information for patients", item.information_for_patients),
                                        Pair("Patient medication information", item.patient_medication_information),
                                        Pair("Ask doctor", item.ask_doctor),
                                        Pair("Ask doctor or pharmacist", item.ask_doctor_or_pharmacist),
                                        Pair("Do not use", item.do_not_use),
                                        Pair(
                                            "Information for owners or caregivers",
                                            item.information_for_owners_or_caregivers
                                        ),
                                        Pair("Instructions for use", item.instructions_for_use),
                                        Pair("Keep out of reach of children", item.keep_out_of_reach_of_children),
                                        Pair("Other safety information", item.other_safety_information),
                                        Pair("Questions", item.questions),

                                        Pair("Med. guide", item.spl_medguide),
                                        Pair(
                                            "Package label principal display panel",
                                            item.package_label_principal_display_panel
                                        ),
                                        Pair("Stop use", item.stop_use),
                                        Pair("When use", item.when_using),
                                        Pair("Patient package insert information", item.spl_patient_package_insert),
                                        Pair("Unclassified section", item.spl_unclassified_section),

                                        Pair("Laboratory tests", item.laboratory_tests),
                                        Pair("Recent major changes", item.recent_major_changes),
                                        Pair("References", item.references),
                                        Pair("Product data elements", item.spl_product_data_elements),
                                        Pair("Active ingredient(s)", item.active_ingredient),
                                        Pair("Inactive ingredient(s)", item.inactive_ingredient)/*,
                            Pair("Effective time", item.effective_time)*/
                                    ).filter { it.second.isNotEmpty() }

                                    val tables = listOf(
                                        Pair("Indication and usage", item.indications_and_usage_table),

                                        Pair("Dosage and administration", item.dosage_and_administration_table),
                                        Pair("Dosage form and strength", item.dosage_forms_and_strengths_table),
                                        Pair("Contraindications", item.contraindications_table),
                                        Pair("Warnings and cautions", item.warnings_and_cautions_table),
                                        Pair("Boxed warnings", item.boxed_warning_table),
                                        Pair("Warnings", item.warnings_table),
                                        Pair("Precautions", item.precautions_table),
                                        Pair("User safety warnings", item.user_safety_warnings_table),
                                        Pair("General precautions", item.general_precautions_table),

                                        Pair("Adverse reactions", item.adverse_reactions_table),
                                        Pair("Drug interactions", item.drug_interactions_table),
                                        Pair(
                                            "Drug and/or laboratory test interactions",
                                            item.drug_and_or_laboratory_test_interactions_table
                                        ),


                                        Pair("Use in specific populations", item.use_in_specific_populations_table),
                                        Pair("Pregnancy", item.pregnancy_table),
                                        Pair("Pregnancy or breast feeding", item.pregnancy_or_breast_feeding_table),
                                        Pair("Labor and delivery", item.labor_and_delivery_table),
                                        Pair("Nursing mothers", item.nursing_mothers_table),
                                        Pair("Pediatric use", item.pediatric_use_table),
                                        Pair("Geriatric use", item.geriatric_use_table),
                                        Pair("Teratogenic", item.teratogenic_effects_table),

                                        Pair("Drug abuse and dependence", item.drug_abuse_and_dependence_table),
                                        Pair("Controlled substance", item.controlled_substance_table),
                                        Pair("Abuse", item.abuse_table),
                                        Pair("Dependence", item.dependence_table),

                                        Pair("Overdosage", item.overdosage_table),

                                        Pair("Description", item.description_table),

                                        Pair("Clinical pharmacology", item.clinical_pharmacology_table),
                                        Pair("Mechanism of action", item.mechanism_of_action_table),
                                        Pair("Pharmacodynamics", item.pharmacodynamics_table),
                                        Pair("Pharmacokinetics", item.pharmacokinetics_table),

                                        Pair("Microbiology", item.microbiology_table),

                                        Pair("Nonclinical toxicology", item.nonclinical_toxicology_table),
                                        Pair(
                                            "Carcinogenesis and mutagenesis and impairment of fertility",
                                            item.carcinogenesis_and_mutagenesis_and_impairment_of_fertility_table
                                        ),
                                        Pair(
                                            "Animal pharmacology and/or toxicology",
                                            item.animal_pharmacology_and_or_toxicology_table
                                        ),

                                        Pair("Clinical studies", item.clinical_studies_table),
                                        Pair("How supplied", item.how_supplied_table),
                                        Pair("Storage and handling", item.storage_and_handling_table),
                                        Pair("Safe handling warning", item.safe_handling_warning_table),

                                        Pair("Information for patients", item.information_for_patients_table),
                                        Pair(
                                            "Patient medication information",
                                            item.patient_medication_information_table
                                        ),
                                        Pair("Ask doctor or pharmacist", item.ask_doctor_or_pharmacist_table),
                                        Pair(
                                            "Information for owners or caregivers",
                                            item.information_for_owners_or_caregivers_table
                                        ),
                                        Pair(
                                            "Keep out of reach of children",
                                            item.keep_out_of_reach_of_children_table
                                        ),
                                        Pair("Other safety information", item.other_safety_information_table),
                                        Pair("Questions", item.questions_table),

                                        Pair("Med. guide", item.spl_medguide_table),
                                        Pair(
                                            "Package label principal display panel",
                                            item.package_label_principal_display_panel_table
                                        ),
                                        Pair("Stop use", item.stop_use_table),
                                        Pair("When use", item.when_using_table),
                                        Pair(
                                            "Patient package insert information",
                                            item.spl_patient_package_insert_table
                                        ),
                                        Pair("Unclassified section", item.spl_unclassified_section_table),

                                        Pair("Laboratory tests", item.laboratory_tests_table),
                                        Pair("Recent major changes", item.recent_major_changes_table),
                                        Pair("References", item.references_table),
                                        Pair("Product data elements", item.spl_product_data_elements_table),
                                        Pair("Active ingredient(s)", item.active_ingredient_table),
                                        Pair("Inactive ingredient(s)", item.inactive_ingredient_table),

                                        ).filter { it.second.isNotEmpty() }

                                    if (features.isNotEmpty()) {
                                        Column(modifier = Modifier.padding(2.dp).weight(0.5f)) {

                                            Text(text = "Details", style = MaterialTheme.typography.headlineMedium)
                                            val state = rememberLazyListState()
                                            Box(modifier = Modifier.fillMaxSize()) {
                                                LazyColumn(
                                                    state = state,
                                                    modifier = Modifier.fillMaxSize()
                                                        .padding(end = 12.dp) // Leave space for bar
                                                ) {


                                                    features.mapIndexed { idx, iu ->
                                                        item(key = idx) {
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
                                                        }
                                                    }


                                                }
                                                VerticalScrollbar(
                                                    modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                                                    adapter = rememberScrollbarAdapter(scrollState = state)
                                                )
                                            }
                                        }
                                    }
                                    if (tables.isNotEmpty()) {
                                        Column(modifier = Modifier.padding(2.dp).weight(0.5f)) {
                                            val state = rememberLazyListState()
                                            Text(text = "Tables", style = MaterialTheme.typography.headlineMedium)
                                            Box(modifier = Modifier.fillMaxSize()) {
                                                LazyColumn(
                                                    state = state,
                                                    modifier = Modifier.fillMaxSize()
                                                        .padding(end = 12.dp) // Leave space for bar
                                                ) {
                                                    tables.mapIndexed { idx, iu ->
                                                        item(key = idx) {
                                                            /*itemsIndexed(
                                                    items = tables,
                                                    key = { _, it -> it.first }) { idx, iu ->*/
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
                                                            //HorizontalDivider(thickness = 1.dp)

                                                        }
                                                    }
                                                }

                                                VerticalScrollbar(
                                                    modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                                                    adapter = rememberScrollbarAdapter(scrollState = state)
                                                )
                                            }
                                        }
                                    }


                                }
                            }
                        }
                    }

                VerticalScrollbar(
                    modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                    adapter = rememberScrollbarAdapter(scrollState = gridState),
                    style = defaultScrollbarStyle().copy(
                        thickness = 24.dp // Increase this value for a wider bar
                    )
                )
                }
            }

        } else if (isLoading) {
            CircularProgressIndicator()
        }

        if (showFeature) {
            //println("featureIdx: $featureIdx")
            //println("features: ${shownFeature.first}, ${shownFeature.second.joinToString(", ")}")
            Feature(
                feature = shownFeature,
                onDismissRequest = { showFeature = false },
                searchStrs = allFields.map{ it.field.value}.filter {
                    it.isNotBlank()
                },
                html = showHtml
            )
        }
        if (showPackages) {
            //println("featureIdx: $featureIdx")
            //println("features: ${shownFeature.first}, ${shownFeature.second.joinToString(", ")}")
            Packages(
                productNdc = productNdc,
                onDismissRequest = { showPackages = false },
                client = httpClient
            )
        }
    }
}
/*
@Composable
fun Modifier.mouseDragScroll(state: LazyStaggeredGridState): Modifier {
    val scope = rememberCoroutineScope()
    return this.pointerInput(Unit) {
        detectDragGestures { change, dragAmount ->
            change.consume()
            scope.launch {
                // dragAmount.y is the movement; we scroll by the negative of it
                state.scrollBy(-dragAmount.y)
            }
        }
    }
}*/
