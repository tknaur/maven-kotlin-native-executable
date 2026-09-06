package net.knaur

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertTrue

class MainTest {
    @Test
    fun testGreeting() {
        val greeting = getGreeting()
        assertTrue(greeting.contains("Hello, Kotlin Native World!"))
        assertTrue(greeting.contains("Running natively on"))
    }
}
