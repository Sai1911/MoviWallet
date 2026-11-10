package pe.moviwallet.passenger

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test

class DemoFlowTest {
    @get:Rule val compose = createEmptyComposeRule()

    @Test fun topupTravelTransferAndPersistence() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        context.getSharedPreferences("moviwallet_demo", 0).edit().clear().commit()
        ActivityScenario.launch(MainActivity::class.java).use {
            compose.onNodeWithText("Explorar demo").performScrollTo().performClick()
            compose.onNodeWithText("Recargar", useUnmergedTree = true).performClick()
            compose.onNodeWithText("Recargar S/ 10.00").performScrollTo().performClick()
            compose.onNodeWithText("Confirmar", substring = false).performClick()
            compose.onNode(hasText("Saldo actual: S/ 10.00") and hasAnyAncestor(isDialog())).assertIsDisplayed()
            compose.onNodeWithText("Listo").performClick()
            compose.onNodeWithText("Pase", substring = false).performClick()
            compose.onNodeWithText("Simular viaje · Operador A").performScrollTo().performClick()
            compose.onNode(hasText("Saldo actual: S/ 8.00") and hasAnyAncestor(isDialog())).assertIsDisplayed()
            compose.onNodeWithText("Listo").performClick()
            compose.onNodeWithText("Operador B", substring = false).performScrollTo().performClick()
            compose.onNodeWithText("Simular viaje · Operador B").performScrollTo().performClick()
            compose.onNode(hasText("Saldo actual: S/ 6.00") and hasAnyAncestor(isDialog())).assertIsDisplayed()
            compose.onNodeWithText("Ahorraste S/ 0.50").assertIsDisplayed()
            compose.onNodeWithText("Listo").performClick()
            compose.onNodeWithText("Actividad", substring = false).performClick()
            compose.onNodeWithText("Viaje · Operador A").assertExists()
            compose.onNodeWithText("Viaje · Operador B").assertExists()
        }
        ActivityScenario.launch(MainActivity::class.java).use {
            compose.onNodeWithText("S/ 6.00", substring = false).assertExists()
        }
    }
}
