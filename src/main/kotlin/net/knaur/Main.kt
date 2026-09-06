package net.knaur

import net.knaur.model.ResponseMessage
import org.http4k.core.*
import org.http4k.core.Method.GET
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.Jetty
import org.http4k.server.asServer
import org.http4k.format.Jackson.auto
import java.time.Instant

fun getGreeting(): String {
    val osName = System.getProperty("os.name")
    val osArch = System.getProperty("os.arch")
    return "Hello, Kotlin Native World! Running natively on $osName ($osArch)"
}

fun main() {
    println(getGreeting())
    println("Webapp is running on port 7070")

    val messageLens = Body.auto<ResponseMessage>().toLens()

    val app: HttpHandler = routes(
        "/" bind GET to { _: Request ->
            val msg = ResponseMessage(Instant.now().epochSecond, "Hello World")
            Response(Status.OK).with(messageLens of msg)
        },
        "/greeting" bind GET to { _: Request ->
            val msg = ResponseMessage(Instant.now().epochSecond, getGreeting())
            Response(Status.OK).with(messageLens of msg)
        }
    )

    // Start the server on port 7070 using Jetty adapter
    val server = app.asServer(Jetty(7070)).start()
    println("Server started: ${server.port()}")
}