package pe.moviwallet.passenger.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import pe.moviwallet.passenger.viewmodel.AppViewModel
import kotlinx.coroutines.delay

/** Hora de demostración que se refresca cada segundo (para cuenta regresiva de transbordo). */
@Composable
fun rememberNow(vm: AppViewModel, offsetMillis: Long): State<Long> =
    produceState(initialValue = vm.nowMillis(), key1 = offsetMillis) {
        while (true) {
            value = vm.nowMillis()
            delay(1000)
        }
    }
