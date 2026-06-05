package com.openfda.funwitopenfda

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.basic
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.compression.Compression
import io.ktor.server.plugins.compression.deflate
import io.ktor.server.plugins.compression.gzip
import io.ktor.server.plugins.compression.minimumSize
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.utils.io.*
//import io.ktor.utils.io.jvm.javaio.copyTo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import java.io.File

fun main() {

    embeddedServer(Netty, port = SERVER_PORT, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.configureHTTP() {
    println("Configuring CORS...")
    install(plugin=CORS) {
        allowCredentials=true
        allowHeader("user_session")
        exposeHeader("user_session")
        exposeHeader("link")


        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)

        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowHost("localhost:8080", schemes = listOf("http"))
        allowHost("localhost:8081", schemes = listOf("http"))
        allowHost("localhost:8082", schemes = listOf("http"))
        allowHost("hluu3305h", schemes = listOf("http"))
        //allowHost("localhost:*", schemes = listOf("http"))
        //anyHost()  Don't do this in production if possible. Try to limit it.
    }

    install(Compression) {
        gzip {
            priority = 1.0
        }
        deflate()
        minimumSize(1024) // compress only responses >= 1KB
    }


    install(plugin=ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
            }
        )
    }

    install(plugin= Authentication) {
        basic("auth-basic") {
            realm = "Access to the '/' path"
            validate { credentials ->
                if (credentials.name=="funWithOpenFDA" && credentials.password=="W3@r30nTh3DrugS") {
                    UserIdPrincipal(credentials.name)
                } else {
                    null
                }
            }
        }
    }
}

fun Application.module() {
    val apiFile = File("api.key")
    val key = apiFile.readText()

    val client = HttpClient(CIO) {

        install(HttpTimeout) {
            requestTimeoutMillis = 45_000   // whole request
            connectTimeoutMillis = 45_000   // TCP connect
            socketTimeoutMillis = 45_000
        }

        install(plugin= io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                }
            )
        }
    }



    configureHTTP()
    routing {

        authenticate("auth-basic") {
            get("/openfda") {

                val rawQuery = call.request.queryString() // exactly what the client sent after '?'
                println("rawQuery=$rawQuery")

                val upstreamUrl = if (rawQuery.isBlank()) {
                    "https://api.fda.gov/drug/label.json"
                } else if (rawQuery.startsWith("link=")) {
                    rawQuery.substringAfter("link=")
                } else {
                    "https://api.fda.gov/drug/label.json?$rawQuery"
                }
                println("upstreamUrl=$upstreamUrl")
                val resultDef = async(context = Dispatchers.IO) {
                    val httpResponse: Result<HttpResponse> = runCatching {
                        val upstreamResponse = client.get(upstreamUrl)


                        return@runCatching upstreamResponse
                    }
                    return@async httpResponse
                }

                val result = resultDef.await()

                result.onSuccess { action ->

                    withContext(Dispatchers.Default) {
                        action.headers[HttpHeaders.Link]?.let { link ->
                            println("link=$link")
                            call.response.header(HttpHeaders.Link, link)
                        }
                        println(action.status.value)
                        call.respondBytesWriter(
                            contentType = action.contentType(),
                            status = action.status
                        ) {
                            action.bodyAsChannel().copyTo(this)
                        }
                    }
                    /*   call.respondOutputStream(
                    contentType = action.contentType(),
                    status = action.status
                ) {
                    action.bodyAsChannel().copyTo(out=this,limit=320000L)
                }*/

                }
                result.onFailure { error ->
                    println(error.message)
                    call.respond(HttpStatusCode.ExpectationFailed)
                }

                //call.respondText("Ktor: ${Greeting().greet()}")
            }

            get("/") {
                call.respondText("Ktor: ${Greeting().greet()}")
            }
            post(path = "/context") {
                println("context route called")
                val labels: List<String> = call.receive()
                println("labels: $labels")
                val indication = call.request.queryParameters["indication"] ?: ""
                val question = call.request.queryParameters["question"]?.toIntOrNull() ?: 0
                println("indication: $indication")


                val system = OpenAIInput(
                    role = "system",
                    content = when (question) {
                        0 -> {
                            "determine if the users provide a label for a drug that treats ${indication.removeSurrounding("\"")}, either in monotherapy or in combination with other drugs. Only use information from the label, and not from anywhere else. answer true or false."
                        }
                        1 -> {
                            "determine if the users provide a label for a drug that has ${indication.removeSurrounding("\"")} as an adverse reaction, either in monotherapy or in combination with other drugs. Only use information from the label, and not from anywhere else. answer true or false."
                        }
                        else -> {
                            "determine if the users provide a label for a drug that has ${indication.removeSurrounding("\"")}, either in monotherapy or in combination with other drugs. Only use information from the label, and not from anywhere else. answer true or false."
                        }
                    }
                )


                val baseurl =
                    "https://datascience-azure-openai-swedencentral.cognitiveservices.azure.com/openai/responses?api-version=2025-03-01-preview"

                val responsesDef = labels.map { it2 ->

                    val input0 = Json.encodeToJsonElement(
                        value = listOf(
                            system, OpenAIInput(
                                role = "user",
                                content = it2
                            )
                        )
                    )

                    val body = """{
"model": "gpt-5-mini",
"input": $input0,
"text": {
"format": {
"type": "json_schema",
"name": "person",
"strict": true,
"schema": {
"type": "object",
"properties": {
"name": {
"type": "boolean"
}
},
"required": [
"name"
],
"additionalProperties": false
}
}
},
"temperature": 1.0
}""".trimIndent()
                    //var tokenFile = File("secrets/apikey")

                    val resultDef = async {
                        val httpResponse: Result<HttpResponse> = runCatching {
                            //println("inside runCatching")
                            client.post(urlString = baseurl) {
                                contentType(ContentType.Application.Json)
                                headers {
                                    append(
                                        name = HttpHeaders.Accept,
                                        value = "application/json"
                                    )
                                }

                                bearerAuth(token = key)


                                //println(body)
                                setBody(body)
                            }
                        }
                        //  println("inside async - after post")
                        return@async httpResponse
                    }
                    return@map resultDef
                }


                val uniqueResponses = responsesDef.awaitAll()

                var errorFlag = false
                val uniqueAnswers = uniqueResponses.map { result ->
                    result.onSuccess { action ->
                        println("success")
                        println(action.status.value)


                        // Or: call.response.headers.append("X-Upstream-Link", link)


                        val openAIresponse = if (action.status==HttpStatusCode.OK) {
                            action.body<OpenAIResponse>()
                        } else {
                            null
                        }
                        println(openAIresponse?.output?.first { it.type=="message" }?.content?.joinToString { it.text }
                            ?: "?")
                        return@map openAIresponse?.output?.first { it.type=="message" }?.content?.first()?.text?.contains(
                            other = "True",
                            ignoreCase = true
                        ) ?: false
                    }


                    result.onFailure { error ->
                        println(error.message)
                        errorFlag = true
                    }
                    return@map false
                }
                if (errorFlag) call.respond(HttpStatusCode.InternalServerError)
                call.response.headers.append("Link", "hello")
                if (uniqueAnswers.isNotEmpty()) {
                    call.respond<List<Boolean>>(uniqueAnswers)
                }

            }
        }
    }
}