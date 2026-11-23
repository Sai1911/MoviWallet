package pe.moviwallet.passenger.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import pe.moviwallet.passenger.domain.*
import pe.moviwallet.passenger.viewmodel.AppState
import java.util.Locale

/** Solo datos ficticios. Cada correo demo conserva su propia billetera local. */
class DemoStore(context: Context) {
    private val prefs = context.getSharedPreferences("moviwallet_demo_v2", Context.MODE_PRIVATE)
    private val legacy = context.getSharedPreferences("moviwallet_demo", Context.MODE_PRIVATE)

    init { migrateLegacy() }

    private fun key(email: String) = "account:" + email.trim().lowercase(Locale.ROOT)

    fun loadSession(): AppState = prefs.getString("activeEmail", null)?.let(::loadAccount) ?: AppState()

    fun loadAccount(email: String): AppState? = prefs.getString(key(email), null)?.let(::decode)

    fun save(state: AppState) {
        val user = state.user ?: return
        prefs.edit().putString(key(user.email), encode(state)).putString("activeEmail", user.email).apply()
    }

    fun logout() { prefs.edit().remove("activeEmail").apply() }

    private fun encode(s: AppState): String {
        val user = requireNotNull(s.user)
        val items = JSONArray()
        s.transactions.forEach { t -> items.put(JSONObject()
            .put("id", t.id).put("type", t.type.name).put("time", t.timestampMillis)
            .put("operator", t.operator?.name).put("profile", t.profile.name)
            .put("base", t.baseFareCents).put("profileDiscount", t.profileDiscountCents)
            .put("transferDiscount", t.transferDiscountCents).put("amount", t.amountCents)
            .put("balanceAfter", t.balanceAfterCents)) }
        return JSONObject().put("name", user.name).put("email", user.email).put("dni", user.dni)
            .put("profile", s.profile.name).put("balance", s.balanceCents).put("transactions", items)
            .put("clockOffset", s.clockOffsetMillis).put("credentialActive", s.credentialActive)
            .put("pushEnabled", s.pushEnabled).put("biometricsEnabled", s.biometricsEnabled)
            .put("txCounter", s.txCounter).put("lastTap", s.lastTapMillis).put("lastOperator", s.lastOperator?.name)
            .put("window", s.window?.let { JSONObject().put("operator", it.originOperator.name)
                .put("start", it.startMillis).put("used", it.discountUsed) }).toString()
    }

    private fun decode(raw: String): AppState {
        val j = JSONObject(raw)
        val a = j.getJSONArray("transactions")
        return AppState(
            user = AppUser(j.getString("name"), j.getString("email"), j.getString("dni")),
            profile = UserProfile.valueOf(j.getString("profile")), balanceCents = j.getInt("balance"),
            transactions = List(a.length()) { i -> val t = a.getJSONObject(i)
                Transaction(t.getString("id"), TxType.valueOf(t.getString("type")), t.getLong("time"),
                    t.optString("operator").takeIf { it.isNotBlank() }?.let(Operator::valueOf),
                    UserProfile.valueOf(t.getString("profile")), t.getInt("base"), t.getInt("profileDiscount"),
                    t.getInt("transferDiscount"), t.getInt("amount"), t.getInt("balanceAfter")) },
            window = j.optJSONObject("window")?.let { TransferWindow(Operator.valueOf(it.getString("operator")), it.getLong("start"), it.getBoolean("used")) },
            clockOffsetMillis = j.optLong("clockOffset"), credentialActive = j.optBoolean("credentialActive", true),
            pushEnabled = j.optBoolean("pushEnabled", true), biometricsEnabled = j.optBoolean("biometricsEnabled"),
            txCounter = j.optInt("txCounter"), lastTapMillis = if (j.has("lastTap")) j.getLong("lastTap") else null,
            lastOperator = j.optString("lastOperator").takeIf { it.isNotBlank() }?.let(Operator::valueOf)
        )
    }

    /** Copia una sola vez la versión 0.1, manteniendo intactas las preferencias originales. */
    private fun migrateLegacy() {
        if (prefs.getBoolean("legacyMigrated", false)) return
        val raw = legacy.getString("wallet", null)
        if (raw != null && loadAccount(DEMO_EMAIL) == null) {
            val j = JSONObject(raw)
            val a = j.getJSONArray("entries")
            // Reconstruir el saldo histórico desde el saldo final y los movimientos originales.
            var balance = j.getInt("balance")
            val transactions = List(a.length()) { i ->
                val m = a.getJSONObject(i)
                val signed = m.getInt("cents")
                val op = if (signed >= 0) null else if (m.getString("title").endsWith("A")) Operator.A else Operator.B
                val discount = m.optInt("discount")
                val base = op?.baseFareCents ?: 0
                val student = op != null && -signed + discount == base / 2
                val t = Transaction(m.getString("id"), if (signed >= 0) TxType.TOPUP else TxType.TRIP,
                    m.getLong("timestamp"), op, if (student) UserProfile.UNIVERSITARIO else UserProfile.NORMAL,
                    base, if (student) base / 2 else 0, discount, kotlin.math.abs(signed), balance)
                balance -= signed
                t
            }
            val state = AppState(user = AppUser("Alex Luna", DEMO_EMAIL, "00000000"),
                profile = if (j.optBoolean("student")) UserProfile.UNIVERSITARIO else UserProfile.NORMAL,
                balanceCents = j.getInt("balance"), transactions = transactions, txCounter = transactions.size,
                window = if (j.has("windowStart") && j.has("firstOperator")) TransferWindow(
                    Operator.valueOf(j.getString("firstOperator")), j.getLong("windowStart"), j.optBoolean("discountUsed")) else null,
                lastTapMillis = if (j.has("lastTap")) j.getLong("lastTap") else null,
                lastOperator = j.optString("lastOperator").takeIf { it.isNotBlank() }?.let(Operator::valueOf))
            save(state)
            if (!legacy.getBoolean("signedIn", false)) logout()
        }
        prefs.edit().putBoolean("legacyMigrated", true).apply()
    }

    companion object { const val DEMO_EMAIL = "demo@moviwallet.pe" }
}
