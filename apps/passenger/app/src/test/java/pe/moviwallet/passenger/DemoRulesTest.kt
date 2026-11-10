package pe.moviwallet.passenger

import org.junit.Assert.*
import org.junit.Test

class DemoRulesTest {
    @Test fun regularJourneyAndTransfer() {
        val first = DemoRules.travel(Wallet(balance = 1000), "A", 1_000, "a")
        val second = DemoRules.travel(first, "B", 2_000, "b")
        assertEquals(600, second.balance)
        assertEquals(50, second.movements.first().discount)
        assertEquals(250, DemoRules.fare(second, "B", 40_000).cents)
    }
    @Test fun studentJourneyAndTransfer() {
        val first = DemoRules.travel(Wallet(balance = 1000, student = true), "A", 1_000, "a")
        assertEquals(825, DemoRules.travel(first, "B", 2_000, "b").balance)
    }
    @Test fun windowDoesNotRestartAndBoundaryIsExclusive() {
        val first = DemoRules.travel(Wallet(balance = 1000), "A", 1_000, "a")
        val second = DemoRules.travel(first, "B", 2_000, "b")
        assertEquals(first.windowStart, second.windowStart)
        assertEquals(0, DemoRules.fare(first, "B", 1_000 + DemoRules.WINDOW_MS).discount)
        assertEquals(50, DemoRules.fare(first, "B", 999 + DemoRules.WINDOW_MS).discount)
    }
    @Test fun insufficientBalanceAndDuplicateCannotDebit() {
        val wallet = Wallet(balance = 100)
        assertTrue(runCatching { DemoRules.travel(wallet, "A", 1_000, "a") }.isFailure)
        assertEquals(100, wallet.balance)
        val first = DemoRules.travel(Wallet(balance = 1000), "A", 1_000, "a")
        assertTrue(runCatching { DemoRules.travel(first, "A", 2_000, "b") }.isFailure)
        assertEquals(0, DemoRules.fare(first, "A", 32_000).discount)
    }
}
