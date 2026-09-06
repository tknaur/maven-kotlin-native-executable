package net.knaur

import io.javalin.Javalin

fun getGreeting(): String {
    val osName = System.getProperty("os.name")
    val osArch = System.getProperty("os.arch")
    return "Hello, Kotlin Native World! Running natively on $osName ($osArch)"
}

fun main() {
    println(getGreeting())
    print("Webapp is running on port 7070")
    val app = Javalin.create() { config ->
        config.routes.get("/") { ctx -> ctx.json("Hello World") }
    }.start(7070)
}

