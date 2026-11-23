package pe.moviwallet.passenger.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import pe.moviwallet.passenger.data.DemoStore
import androidx.lifecycle.viewModelScope
import pe.moviwallet.passenger.domain.AppUser
import pe.moviwallet.passenger.domain.FareEngine
import pe.moviwallet.passenger.domain.FareQuote
import pe.moviwallet.passenger.domain.Operator
import pe.moviwallet.passenger.domain.Transaction
import pe.moviwallet.passenger.domain.TransferWindow
import pe.moviwallet.passenger.domain.TxType
import pe.moviwallet.passenger.domain.UserProfile
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AppState(
    val user: AppUser? = null,
    val profile: UserProfile = UserProfile.NORMAL,
    val balanceCents: Int = 0,
    val transactions: List<Transaction> = emptyList(), // más reciente primero
    val window: TransferWindow? = null,
    val clockOffsetMillis: Long = 0L,
    val credentialActive: Boolean = true,
    val pushEnabled: Boolean = true,
    val biometricsEnabled: Boolean = false,
    val txCounter: Int = 0,
    val lastTapMillis: Long? = null,
    val lastOperator: Operator? = null
)

enum class TopUpScenario(val label: String) {
    SUCCESS("Aprobada"), REJECTED("Rechazada"), TIMEOUT("Timeout"), CONNECTION_ERROR("Sin conexión")
}

enum class PayScenario(val label: String) {
    NORMAL("Normal"), INVALID_CREDENTIAL("Credencial inválida"), CONNECTION_ERROR("Sin conexión")
}

sealed interface TopUpPhase {
    data class Select(val selectedCents: Int? = null) : TopUpPhase
    data class Confirm(val amountCents: Int) : TopUpPhase
    data class Processing(val amountCents: Int) : TopUpPhase
    data class Success(val tx: Transaction) : TopUpPhase
    data class Rejected(val amountCents: Int) : TopUpPhase
    data class Timeout(val amountCents: Int) : TopUpPhase
    data class ConnectionError(val amountCents: Int) : TopUpPhase
}

sealed interface PayPhase {
    data object Prepare : PayPhase
    data class Reading(val op: Operator) : PayPhase
    data class Processing(val op: Operator) : PayPhase
    data class Approved(val tx: Transaction, val quote: FareQuote) : PayPhase
    data class Insufficient(val op: Operator, val requiredCents: Int, val balanceCents: Int) : PayPhase
    data class InvalidCredential(val op: Operator) : PayPhase
    data class ConnectionError(val op: Operator) : PayPhase
    data class Repeated(val op: Operator) : PayPhase
}

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val store = DemoStore(application)
    private val _state = MutableStateFlow(store.loadSession())
    val state: StateFlow<AppState> = _state.asStateFlow()

    private val _topUp = MutableStateFlow<TopUpPhase>(TopUpPhase.Select())
    val topUp: StateFlow<TopUpPhase> = _topUp.asStateFlow()

    private val _pay = MutableStateFlow<PayPhase>(PayPhase.Prepare)
    val pay: StateFlow<PayPhase> = _pay.asStateFlow()

    var topUpScenario by mutableStateOf(TopUpScenario.SUCCESS)
    var payOperator by mutableStateOf(Operator.A)
    var payScenario by mutableStateOf(PayScenario.NORMAL)

    private var topUpJob: Job? = null
    private var payJob: Job? = null

    /** Reloj de demostración: hora real + desplazamiento simulado. */
    fun nowMillis(): Long = System.currentTimeMillis() + _state.value.clockOffsetMillis

    // ---------- Autenticación ----------

    fun loginDemo() {
        resetFlows()
        _state.value = store.loadAccount(DemoStore.DEMO_EMAIL)
            ?: AppState(user = AppUser("Alex Luna", DemoStore.DEMO_EMAIL, "00000000"))
        store.save(_state.value)
    }

    fun login(email: String) {
        resetFlows()
        _state.value = store.loadAccount(email) ?: run {
            val name = email.substringBefore("@").replace('.', ' ').replace('_', ' ')
                .split(" ").filter { it.isNotBlank() }
                .joinToString(" ") { w -> w.replaceFirstChar { it.uppercase() } }
                .ifBlank { "Usuario" }
            AppState(user = AppUser(name, email, "00000000"))
        }
        store.save(_state.value)
    }

    fun register(name: String, dni: String, email: String, profile: UserProfile) {
        resetFlows()
        val user = AppUser(name.trim(), email.trim(), dni.trim())
        _state.value = (store.loadAccount(email) ?: AppState()).copy(user = user, profile = profile)
        store.save(_state.value)
    }

    fun logout() {
        resetFlows()
        store.logout()
        _state.value = AppState()
    }

    fun resetDemo() {
        resetFlows()
        updateState {
            it.copy(
                balanceCents = 0, transactions = emptyList(), window = null,
                clockOffsetMillis = 0L, txCounter = 0,
                lastTapMillis = null, lastOperator = null, credentialActive = true
            )
        }
    }

    // ---------- Perfil y ajustes ----------

    fun setProfile(p: UserProfile) = updateState { it.copy(profile = p) }
    fun setCredentialActive(v: Boolean) = updateState { it.copy(credentialActive = v) }
    fun setPush(v: Boolean) = updateState { it.copy(pushEnabled = v) }
    fun setBiometrics(v: Boolean) = updateState { it.copy(biometricsEnabled = v) }

    // ---------- Reloj de demostración (transbordos) ----------

    fun advanceClock(minutes: Int) =
        updateState { it.copy(clockOffsetMillis = it.clockOffsetMillis + minutes * 60_000L) }

    fun clearWindow() = updateState { it.copy(window = null) }

    // ---------- Recargas ----------

    fun resetTopUp() {
        topUpJob?.cancel()
        _topUp.value = TopUpPhase.Select()
    }

    fun selectTopUp(cents: Int) {
        require(cents in pe.moviwallet.passenger.domain.Rules.TOPUP_OPTIONS)
        _topUp.value = TopUpPhase.Select(cents)
    }

    fun goToTopUpConfirm() {
        val p = _topUp.value as? TopUpPhase.Select ?: return
        val amount = p.selectedCents ?: return
        _topUp.value = TopUpPhase.Confirm(amount)
    }

    fun backToTopUpSelect() {
        val p = _topUp.value
        _topUp.value = TopUpPhase.Select((p as? TopUpPhase.Confirm)?.amountCents)
    }

    fun submitTopUp() {
        val p = _topUp.value as? TopUpPhase.Confirm ?: return
        val amount = p.amountCents
        val scenario = topUpScenario
        _topUp.value = TopUpPhase.Processing(amount)
        topUpJob?.cancel()
        topUpJob = viewModelScope.launch {
            delay(2000)
            _topUp.value = when (scenario) {
                TopUpScenario.REJECTED -> TopUpPhase.Rejected(amount)
                TopUpScenario.TIMEOUT -> TopUpPhase.Timeout(amount)
                TopUpScenario.CONNECTION_ERROR -> TopUpPhase.ConnectionError(amount)
                TopUpScenario.SUCCESS -> TopUpPhase.Success(applyTopUp(amount))
            }
        }
    }

    private fun applyTopUp(amount: Int): Transaction {
        val s = _state.value
        val n = s.txCounter + 1
        val tx = Transaction(
            id = txId(n), type = TxType.TOPUP, timestampMillis = nowMillis(),
            operator = null, profile = s.profile,
            baseFareCents = 0, profileDiscountCents = 0, transferDiscountCents = 0,
            amountCents = amount, balanceAfterCents = s.balanceCents + amount
        )
        updateState {
            it.copy(
                balanceCents = it.balanceCents + amount,
                transactions = listOf(tx) + it.transactions,
                txCounter = n
            )
        }
        return tx
    }

    // ---------- Pago NFC (simulado) ----------

    fun resetPay() {
        payJob?.cancel()
        _pay.value = PayPhase.Prepare
    }

    fun startPay() {
        if (_pay.value !is PayPhase.Prepare) return
        val op = payOperator
        val scenario = payScenario
        _pay.value = PayPhase.Reading(op)
        payJob?.cancel()
        payJob = viewModelScope.launch {
            delay(2400)
            _pay.value = PayPhase.Processing(op)
            delay(1400)
            _pay.value = settlePayment(op, scenario)
        }
    }

    private fun settlePayment(op: Operator, scenario: PayScenario): PayPhase {
        val s = _state.value
        if (scenario == PayScenario.CONNECTION_ERROR) return PayPhase.ConnectionError(op)
        if (scenario == PayScenario.INVALID_CREDENTIAL || !s.credentialActive) {
            return PayPhase.InvalidCredential(op)
        }
        val now = nowMillis()
        if (FareEngine.isRepeated(s.lastOperator, s.lastTapMillis, op, now)) return PayPhase.Repeated(op)
        val quote = FareEngine.quote(op, s.profile, s.window, now)
        if (s.balanceCents < quote.totalCents) {
            return PayPhase.Insufficient(op, quote.totalCents, s.balanceCents)
        }
        val newBalance = s.balanceCents - quote.totalCents // nunca negativo
        val n = s.txCounter + 1
        val tx = Transaction(
            id = txId(n), type = TxType.TRIP, timestampMillis = now,
            operator = op, profile = s.profile,
            baseFareCents = quote.baseCents,
            profileDiscountCents = quote.profileDiscountCents,
            transferDiscountCents = quote.transferDiscountCents,
            amountCents = quote.totalCents, balanceAfterCents = newBalance
        )
        updateState {
            it.copy(
                balanceCents = newBalance,
                transactions = listOf(tx) + it.transactions,
                window = FareEngine.nextWindow(it.window, op, now, quote),
                txCounter = n, lastTapMillis = now, lastOperator = op
            )
        }
        return PayPhase.Approved(tx, quote)
    }

    private fun txId(n: Int) = "T" + n.toString().padStart(5, '0')

    private fun updateState(transform: (AppState) -> AppState) {
        _state.update(transform)
        store.save(_state.value)
    }

    private fun resetFlows() {
        topUpJob?.cancel()
        payJob?.cancel()
        _topUp.value = TopUpPhase.Select()
        _pay.value = PayPhase.Prepare
        topUpScenario = TopUpScenario.SUCCESS
        payScenario = PayScenario.NORMAL
        payOperator = Operator.A
    }
}
