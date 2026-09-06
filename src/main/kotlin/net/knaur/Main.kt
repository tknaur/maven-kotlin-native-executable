package net.knaur

import org.http4k.core.*
import org.http4k.core.Method.GET
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.Jetty
import org.http4k.server.asServer

fun getGreeting(): String {
    val osName = System.getProperty("os.name")
    val osArch = System.getProperty("os.arch")
    return "Hello, Kotlin Native World! Running natively on $osName ($osArch)"
}

fun main() {
    println(getGreeting())
    print("Webapp is running on port 7070")

    // Simple route registry: add new entries here to expose more routes.
    val app: HttpHandler = routes(
        "/" bind GET to { _: Request -> Response(Status.OK).body("Hello World") },
        "/greeting" bind GET to { _: Request -> Response(Status.OK).body(getGreeting()) }
    )

    // Start the server on port 7070 using Jetty adapter
    val server = app.asServer(Jetty(7070)).start()
    println("Server started: ${server.port()}")
}