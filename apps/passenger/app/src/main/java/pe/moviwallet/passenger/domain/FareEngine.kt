package pe.moviwallet.passenger.domain

import kotlin.math.min

object Rules {
    const val TRANSFER_WINDOW_MINUTES = 120
    const val TRANSFER_DISCOUNT_CENTS = 50
    const val REPEAT_WINDOW_MILLIS = 30_000L
    val TOPUP_OPTIONS = listOf(500, 1000, 2000)
}

data class TransferWindow(
    val originOperator: Operator,
    val startMillis: Long,
    val discountUsed: Boolean
) {
    val expiresAtMillis: Long get() = startMillis + Rules.TRANSFER_WINDOW_MINUTES * 60_000L
    fun isActive(now: Long): Boolean = now in startMillis until expiresAtMillis
    fun remainingMillis(now: Long): Long = (expiresAtMillis - now).coerceAtLeast(0L)
}

data class FareQuote(
    val baseCents: Int,
    val profileDiscountCents: Int,
    val transferDiscountCents: Int,
    val totalCents: Int,
    val transferApplied: Boolean
)

object FareEngine {

    fun isRepeated(lastOperator: Operator?, lastTapMillis: Long?, op: Operator, now: Long): Boolean =
        lastOperator == op && lastTapMillis != null && now - lastTapMillis in 0 until Rules.REPEAT_WINDOW_MILLIS

    /** Tarifa del perfil sobre la tarifa base (p. ej. universitario = 50 %). */
    fun profileFare(op: Operator, profile: UserProfile): Int =
        op.baseFareCents * profile.fareFactorPercent / 100

    fun quote(op: Operator, profile: UserProfile, window: TransferWindow?, now: Long): FareQuote {
        val afterProfile = profileFare(op, profile)
        val eligible = window != null &&
            window.isActive(now) &&
            !window.discountUsed &&
            window.originOperator != op
        val transfer = if (eligible) min(Rules.TRANSFER_DISCOUNT_CENTS, afterProfile) else 0
        return FareQuote(
            baseCents = op.baseFareCents,
            profileDiscountCents = op.baseFareCents - afterProfile,
            transferDiscountCents = transfer,
            totalCents = afterProfile - transfer,
            transferApplied = eligible
        )
    }

    /** Ventana resultante tras cobrar un viaje. Un solo descuento por ventana. */
    fun nextWindow(window: TransferWindow?, op: Operator, now: Long, quote: FareQuote): TransferWindow {
        val active = window != null && window.isActive(now)
        return when {
            window != null && quote.transferApplied -> window.copy(discountUsed = true)
            window != null && active -> window
            else -> TransferWindow(op, now, false)
        }
    }

    /** Tarifa del segundo viaje con transbordo (para tablas informativas). */
    fun transferFare(second: Operator, profile: UserProfile): Int =
        profileFare(second, profile) - min(Rules.TRANSFER_DISCOUNT_CENTS, profileFare(second, profile))
}
