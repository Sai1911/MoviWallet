package pe.moviwallet.passenger

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class WalletViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("moviwallet_demo", 0)
    var wallet by mutableStateOf(load())
        private set
    var signedIn by mutableStateOf(prefs.getBoolean("signedIn", false))
        private set
    var offline by mutableStateOf(false)
    var rejectTopup by mutableStateOf(false)

    fun enter() { signedIn = true; prefs.edit().putBoolean("signedIn", true).apply() }
    fun logout() { signedIn = false; prefs.edit().putBoolean("signedIn", false).apply() }
    fun student(value: Boolean) { wallet = wallet.copy(student = value); save() }
    fun reset() { wallet = Wallet(); offline = false; rejectTopup = false; save() }
    fun topup(cents: Int): Result<Movement> = runCatching {
        check(!offline) { "Sin conexión. Vuelve a intentarlo cuando estés conectado." }
        check(!rejectTopup) { "Recarga rechazada. Tu saldo no ha cambiado." }
        require(cents in listOf(500, 1000, 2000))
        val entry = Movement(UUID.randomUUID().toString(), "Recarga de saldo", "Recarga simulada", cents, System.currentTimeMillis())
        wallet = wallet.copy(balance = wallet.balance + cents, movements = listOf(entry) + wallet.movements)
        save()
        entry
    }
    fun travel(operator: String): Result<Movement> = runCatching {
        check(!offline) { "Sin conexión. No se pudo confirmar el viaje. No se realizó ningún cobro." }
        wallet = DemoRules.travel(wallet, operator, System.currentTimeMillis(), UUID.randomUUID().toString())
        save()
        wallet.movements.first()
    }
    private fun save() {
        val entries = JSONArray()
        wallet.movements.forEach { m -> entries.put(JSONObject().put("id", m.id).put("title", m.title).put("subtitle", m.subtitle).put("cents", m.cents).put("timestamp", m.timestamp).put("discount", m.discount)) }
        val json = JSONObject().put("balance", wallet.balance).put("student", wallet.student).put("entries", entries)
            .put("windowStart", wallet.windowStart).put("firstOperator", wallet.firstOperator).put("discountUsed", wallet.discountUsed)
            .put("lastTap", wallet.lastTap).put("lastOperator", wallet.lastOperator)
        prefs.edit().putString("wallet", json.toString()).apply()
    }
    private fun load(): Wallet = runCatching {
        val json = JSONObject(prefs.getString("wallet", null) ?: return Wallet())
        val entries = json.getJSONArray("entries")
        Wallet(json.getInt("balance"), json.optBoolean("student"), List(entries.length()) { i ->
            val m = entries.getJSONObject(i)
            Movement(m.getString("id"), m.getString("title"), m.getString("subtitle"), m.getInt("cents"), m.getLong("timestamp"), m.optInt("discount"))
        }, if (json.has("windowStart")) json.getLong("windowStart") else null,
            if (json.has("firstOperator")) json.getString("firstOperator") else null, json.optBoolean("discountUsed"),
            if (json.has("lastTap")) json.getLong("lastTap") else null,
            if (json.has("lastOperator")) json.getString("lastOperator") else null)
    }.getOrElse { Wallet() }
}
