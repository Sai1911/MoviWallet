package pe.moviwallet.passenger

import org.junit.Assert.*
import org.junit.Test
import pe.moviwallet.passenger.domain.*

class FareEngineTest {
    @Test fun regularJourneyAndTransfer() {
        val first = FareEngine.quote(Operator.A, UserProfile.NORMAL, null, 1_000)
        val window = FareEngine.nextWindow(null, Operator.A, 1_000, first)
        val second = FareEngine.quote(Operator.B, UserProfile.NORMAL, window, 2_000)
        assertEquals(600, 1000 - first.totalCents - second.totalCents)
        assertEquals(50, second.transferDiscountCents)
        val used = FareEngine.nextWindow(window, Operator.B, 2_000, second)
        assertEquals(250, FareEngine.quote(Operator.B, UserProfile.NORMAL, used, 40_000).totalCents)
    }
    @Test fun studentJourneyAndTransfer() {
        val first = FareEngine.quote(Operator.A, UserProfile.UNIVERSITARIO, null, 1_000)
        val window = FareEngine.nextWindow(null, Operator.A, 1_000, first)
        val second = FareEngine.quote(Operator.B, UserProfile.UNIVERSITARIO, window, 2_000)
        assertEquals(825, 1000 - first.totalCents - second.totalCents)
    }
    @Test fun exclusiveBoundaryAndOriginalWindowPreserved() {
        val window = TransferWindow(Operator.A, 1_000, false)
        assertEquals(50, FareEngine.quote(Operator.B, UserProfile.NORMAL, window, window.expiresAtMillis - 1).transferDiscountCents)
        assertEquals(0, FareEngine.quote(Operator.B, UserProfile.NORMAL, window, window.expiresAtMillis).transferDiscountCents)
        val fare = FareEngine.quote(Operator.B, UserProfile.NORMAL, window, 2_000)
        assertEquals(1_000, FareEngine.nextWindow(window, Operator.B, 2_000, fare).startMillis)
    }
    @Test fun sameOperatorAndUsedWindowDoNotDiscount() {
        assertEquals(0, FareEngine.quote(Operator.A, UserProfile.NORMAL, TransferWindow(Operator.A, 0, false), 10).transferDiscountCents)
        assertEquals(0, FareEngine.quote(Operator.B, UserProfile.NORMAL, TransferWindow(Operator.A, 0, true), 10).transferDiscountCents)
    }
    @Test fun repeatGuardAndBoundary() {
        assertTrue(FareEngine.isRepeated(Operator.A, 1_000, Operator.A, 30_999))
        assertFalse(FareEngine.isRepeated(Operator.A, 1_000, Operator.A, 31_000))
        assertFalse(FareEngine.isRepeated(Operator.A, 1_000, Operator.B, 2_000))
        assertFalse(FareEngine.isRepeated(null, null, Operator.A, 2_000))
    }
}
