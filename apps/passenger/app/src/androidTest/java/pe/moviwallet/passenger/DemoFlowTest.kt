package pe.moviwallet.passenger

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test

class DemoFlowTest {
    @get:Rule val compose = createEmptyComposeRule()
    private fun waitFor(text: String) {
        compose.waitUntil(15_000) { compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty() }
    }
    @Test fun topupTravelTransferHistoryAndPersistence() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        check(context.packageName.endsWith(".qa"))
        context.getSharedPreferences("moviwallet_demo", 0).edit().clear().commit()
        context.getSharedPreferences("moviwallet_demo_v2", 0).edit().clear().commit()
        ActivityScenario.launch(MainActivity::class.java).use {
            compose.onNodeWithText("Explorar con cuenta demo").performClick()
            compose.onNodeWithText("Recargar").performScrollTo().performClick()
            compose.onNodeWithText("S/ 10", substring = false).performClick()
            compose.onNodeWithText("Continuar").performClick()
            compose.onNodeWithText("Confirmar recarga de S/ 10.00").performClick()
            waitFor("Recarga exitosa")
            compose.onNodeWithText("Listo").performClick()
            compose.onNodeWithText("Pagar", substring = false).performClick()
            compose.onNodeWithText("Acercar teléfono al lector").performClick()
            waitFor("Pago autorizado")
            compose.onNodeWithText("S/ 8.00", substring = false).assertExists()
            compose.onNodeWithText("Volver al inicio").performClick()
            compose.onNodeWithText("Pagar", substring = false).performClick()
            compose.onNodeWithText("Operador B", substring = false).performClick()
            compose.onNodeWithText("Acercar teléfono al lector").performClick()
            waitFor("Pago autorizado")
            compose.onNodeWithText("S/ 6.00", substring = false).assertExists()
            compose.onNodeWithText("Ver comprobante").performClick()
            compose.onNodeWithText("Descuento por transbordo").assertExists()
            compose.onNodeWithText("Saldo después").assertExists()
            compose.onNodeWithText("Listo").performClick()
        }
        ActivityScenario.launch(MainActivity::class.java).use {
            compose.onNodeWithText("S/ 6.00", substring = false).assertExists()
            compose.onNodeWithText("Pagar", substring = false).performClick()
            compose.onNodeWithText("Operador B", substring = false).performClick()
            compose.onNodeWithText("Acercar teléfono al lector").performClick()
            waitFor("Lectura repetida")
        }
    }
}
