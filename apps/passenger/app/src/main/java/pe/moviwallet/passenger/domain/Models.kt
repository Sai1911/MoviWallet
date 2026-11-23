package pe.moviwallet.passenger.domain

enum class Operator(val label: String, val mode: String, val baseFareCents: Int) {
    A("Operador A", "Metro · Tren urbano", 200),
    B("Operador B", "Bus · Corredores", 250);

    val other: Operator get() = if (this == A) B else A
}

enum class UserProfile(val label: String, val fareFactorPercent: Int) {
    NORMAL("Regular", 100),
    UNIVERSITARIO("Universitario", 50)
}

enum class TxType { TOPUP, TRIP }

/** Todos los importes están en céntimos (Int) para evitar errores de redondeo. */
data class Transaction(
    val id: String,
    val type: TxType,
    val timestampMillis: Long,
    val operator: Operator?,
    val profile: UserProfile,
    val baseFareCents: Int,
    val profileDiscountCents: Int,
    val transferDiscountCents: Int,
    val amountCents: Int,
    val balanceAfterCents: Int
) {
    val receiptCode: String get() = "MW-$id"
}

data class AppUser(val name: String, val email: String, val dni: String)
