package pe.moviwallet.passenger

import java.util.Locale

fun money(cents: Int): String = String.format(Locale.US, "S/ %.2f", cents / 100.0)

data class Movement(
    val id: String,
    val title: String,
    val subtitle: String,
    val cents: Int,
    val timestamp: Long,
    val discount: Int = 0
)

data class Wallet(
    val balance: Int = 0,
    val student: Boolean = false,
    val movements: List<Movement> = emptyList(),
    val windowStart: Long? = null,
    val firstOperator: String? = null,
    val discountUsed: Boolean = false,
    val lastTap: Long? = null,
    val lastOperator: String? = null
)

data class Fare(val cents: Int, val discount: Int, val newWindow: Boolean)

object DemoRules {
    const val WINDOW_MS = 120 * 60 * 1000L
    fun fare(wallet: Wallet, operator: String, now: Long): Fare {
        require(operator == "A" || operator == "B")
        val base = if (operator == "A") 200 else 250
        val profileFare = if (wallet.student) base / 2 else base
        val active = wallet.windowStart?.let { now - it in 0 until WINDOW_MS } ?: false
        val discount = if (active && operator != wallet.firstOperator && !wallet.discountUsed) 50 else 0
        return Fare((profileFare - discount).coerceAtLeast(0), discount, !active)
    }
    fun repeated(wallet: Wallet, operator: String, now: Long): Boolean =
        wallet.lastOperator == operator && wallet.lastTap?.let { now - it in 0 until 30_000L } == true

    fun travel(wallet: Wallet, operator: String, now: Long, id: String): Wallet {
        require(!repeated(wallet, operator, now)) { "Lectura repetida. Espera 30 segundos antes de volver a viajar con este operador." }
        val fare = fare(wallet, operator, now)
        require(wallet.balance >= fare.cents) { "Saldo insuficiente. Recarga tu billetera para continuar." }
        val entry = Movement(id, "Viaje · Operador $operator", if (fare.discount > 0) "Transbordo con descuento" else "Tarifa ${if (wallet.student) "estudiantil" else "regular"}", -fare.cents, now, fare.discount)
        return wallet.copy(
            balance = wallet.balance - fare.cents,
            movements = listOf(entry) + wallet.movements,
            windowStart = if (fare.newWindow) now else wallet.windowStart,
            firstOperator = if (fare.newWindow) operator else wallet.firstOperator,
            discountUsed = if (fare.newWindow) false else wallet.discountUsed || fare.discount > 0,
            lastTap = now, lastOperator = operator
        )
    }
}
