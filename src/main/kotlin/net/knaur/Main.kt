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

    // Simple route registry: map path -> handler. Add new entries here to expose more routes.
    val routes: Map<String, (io.javalin.http.Context) -> Unit> = mapOf(
        "/" to { ctx -> ctx.json("Hello World") },
        "/greeting" to { ctx -> ctx.json(getGreeting()) }
    )

    // Create and start the app with routes registered
    val app = Javalin.create { config ->
        config.routes.apply {
            routes.forEach { (path, handler) ->
                get(path, handler)
            }
        }
    }.start(7070)
}