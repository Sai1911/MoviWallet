package pe.moviwallet.passenger

import android.app.Application
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import pe.moviwallet.passenger.domain.*
import pe.moviwallet.passenger.viewmodel.*

class LocalStateTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val app get() = instrumentation.targetContext.applicationContext as Application
    private lateinit var vm: AppViewModel
    private fun main(block: () -> Unit) = instrumentation.runOnMainSync(block)
    private fun await(condition: () -> Boolean) {
        val deadline = System.currentTimeMillis() + 12_000
        while (!condition() && System.currentTimeMillis() < deadline) Thread.sleep(40)
        assertTrue("La operación no terminó a tiempo", condition())
    }
    @Before fun cleanQaOnly() {
        check(app.packageName.endsWith(".qa"))
        app.getSharedPreferences("moviwallet_demo", 0).edit().clear().commit()
        app.getSharedPreferences("moviwallet_demo_v2", 0).edit().clear().commit()
        main { vm = AppViewModel(app); vm.loginDemo() }
    }
    private fun topup(scenario: TopUpScenario = TopUpScenario.SUCCESS) {
        main { vm.resetTopUp(); vm.topUpScenario = scenario; vm.selectTopUp(1000); vm.goToTopUpConfirm(); vm.submitTopUp() }
        await { vm.topUp.value !is TopUpPhase.Processing }
    }
    private fun pay(op: Operator, scenario: PayScenario = PayScenario.NORMAL) {
        main { vm.resetPay(); vm.payOperator = op; vm.payScenario = scenario; vm.startPay() }
        await { vm.pay.value !is PayPhase.Reading && vm.pay.value !is PayPhase.Processing }
    }
    @Test fun studentBalanceAndRepeatGuardSurviveNewViewModel() {
        main { vm.setProfile(UserProfile.UNIVERSITARIO) }
        topup(); pay(Operator.A); pay(Operator.B)
        assertEquals(825, vm.state.value.balanceCents)
        val original = vm.state.value
        main { vm = AppViewModel(app) }
        assertEquals(original, vm.state.value)
        pay(Operator.B)
        assertTrue(vm.pay.value is PayPhase.Repeated)
        assertEquals(original, vm.state.value)
    }
    @Test fun rejectionTimeoutAndOfflineDoNotCredit() {
        for (scenario in listOf(TopUpScenario.REJECTED, TopUpScenario.TIMEOUT, TopUpScenario.CONNECTION_ERROR)) {
            topup(scenario)
            assertEquals(0, vm.state.value.balanceCents)
            assertTrue(vm.state.value.transactions.isEmpty())
        }
    }
    @Test fun insufficientInvalidAndOfflineDoNotDebit() {
        pay(Operator.A)
        assertTrue(vm.pay.value is PayPhase.Insufficient)
        topup()
        val original = vm.state.value
        pay(Operator.A, PayScenario.INVALID_CREDENTIAL)
        assertTrue(vm.pay.value is PayPhase.InvalidCredential)
        assertEquals(original, vm.state.value)
        pay(Operator.A, PayScenario.CONNECTION_ERROR)
        assertTrue(vm.pay.value is PayPhase.ConnectionError)
        assertEquals(original, vm.state.value)
    }
    @Test fun logoutAndDifferentDemoAccountsPreserveTheirOwnWallets() {
        topup()
        main { vm.logout(); vm = AppViewModel(app) }
        assertNull(vm.state.value.user)
        main { vm.login("second@example.test") }
        assertEquals(0, vm.state.value.balanceCents)
        main { vm.loginDemo() }
        assertEquals(1000, vm.state.value.balanceCents)
        main { vm.resetDemo(); vm = AppViewModel(app) }
        assertEquals(0, vm.state.value.balanceCents)
        assertTrue(vm.state.value.transactions.isEmpty())
    }
    @Test fun legacyMigrationPreservesMovementsAndDoesNotRunTwice() {
        app.getSharedPreferences("moviwallet_demo_v2", 0).edit().clear().commit()
        val timestamp = System.currentTimeMillis()
        val raw = """{"balance":600,"student":false,"entries":[
            {"id":"b","title":"Viaje · Operador B","subtitle":"Transbordo con descuento","cents":-200,"timestamp":$timestamp,"discount":50},
            {"id":"a","title":"Viaje · Operador A","subtitle":"Tarifa regular","cents":-200,"timestamp":$timestamp,"discount":0},
            {"id":"topup","title":"Recarga de saldo","subtitle":"Recarga simulada","cents":1000,"timestamp":$timestamp,"discount":0}],
            "windowStart":$timestamp,"firstOperator":"A","discountUsed":true,"lastTap":$timestamp,"lastOperator":"B"}"""
        app.getSharedPreferences("moviwallet_demo", 0).edit().putString("wallet", raw).putBoolean("signedIn", true).commit()
        main { vm = AppViewModel(app) }
        assertEquals(600, vm.state.value.balanceCents)
        assertEquals(listOf(600, 800, 1000), vm.state.value.transactions.map { it.balanceAfterCents })
        assertEquals(50, vm.state.value.transactions.first().transferDiscountCents)
        assertEquals(Operator.B, vm.state.value.lastOperator)
        main { vm.resetDemo(); vm = AppViewModel(app) }
        assertEquals(0, vm.state.value.balanceCents)
        assertEquals(raw, app.getSharedPreferences("moviwallet_demo", 0).getString("wallet", null))
    }
}
