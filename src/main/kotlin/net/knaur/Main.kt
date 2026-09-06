package net.knaur

fun getGreeting(): String {
    val osName = System.getProperty("os.name")
    val osArch = System.getProperty("os.arch")
    return "Hello, Kotlin Native World! Running natively on $osName ($osArch)"
}

fun main() {
    println(getGreeting())
}
