package pe.moviwallet.passenger.ui.screens.topup

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.moviwallet.passenger.domain.Rules
import pe.moviwallet.passenger.domain.soles
import pe.moviwallet.passenger.ui.components.ChoiceChip
import pe.moviwallet.passenger.ui.components.Hairline
import pe.moviwallet.passenger.ui.components.InfoRow
import pe.moviwallet.passenger.ui.components.PrimaryButton
import pe.moviwallet.passenger.ui.components.ScreenHeader
import pe.moviwallet.passenger.ui.components.SecondaryButton
import pe.moviwallet.passenger.ui.components.StatusScreen
import pe.moviwallet.passenger.ui.components.card
import pe.moviwallet.passenger.ui.theme.BrandBlue
import pe.moviwallet.passenger.ui.theme.ErrorRed
import pe.moviwallet.passenger.ui.theme.Ink
import pe.moviwallet.passenger.ui.theme.InkMuted
import pe.moviwallet.passenger.ui.theme.Outline
import pe.moviwallet.passenger.ui.theme.Success
import pe.moviwallet.passenger.ui.theme.Warning
import pe.moviwallet.passenger.viewmodel.AppViewModel
import pe.moviwallet.passenger.viewmodel.TopUpPhase
import pe.moviwallet.passenger.viewmodel.TopUpScenario

@Composable
fun TopUpScreen(
    vm: AppViewModel,
    onClose: () -> Unit,
    onReceipt: (String) -> Unit
) {
    val phase by vm.topUp.collectAsStateWithLifecycle()
    val state by vm.state.collectAsStateWithLifecycle()

    DisposableEffect(Unit) { onDispose { vm.resetTopUp() } }

    BackHandler {
        when (phase) {
            is TopUpPhase.Confirm -> vm.backToTopUpSelect()
            is TopUpPhase.Processing -> Unit // no se puede cancelar mientras procesa
            else -> onClose()
        }
    }

    when (val p = phase) {
        is TopUpPhase.Select -> SelectPhase(vm, p, state.balanceCents, onClose)
        is TopUpPhase.Confirm -> ConfirmPhase(vm, p, state.balanceCents)
        is TopUpPhase.Processing -> ProcessingPhase(p.amountCents)
        is TopUpPhase.Success -> StatusScreen(
            icon = Icons.Filled.Check,
            tint = Success,
            title = "Recarga exitosa",
            message = "Se acreditaron ${p.tx.amountCents.soles()} a tu saldo.",
            primaryLabel = "Ver comprobante",
            onPrimary = { onReceipt(p.tx.id) },
            secondaryLabel = "Listo",
            onSecondary = onClose,
            details = {
                Column(Modifier.fillMaxWidth().card().padding(horizontal = 18.dp, vertical = 8.dp)) {
                    InfoRow("Importe recargado", p.tx.amountCents.soles())
                    InfoRow("Nuevo saldo", p.tx.balanceAfterCents.soles(), valueColor = Success, bold = true)
                }
            }
        )
        is TopUpPhase.Rejected -> StatusScreen(
            icon = Icons.Filled.Close,
            tint = ErrorRed,
            title = "Recarga rechazada",
            message = "La pasarela simulada rechazó el cargo de ${p.amountCents.soles()}. No se descontó dinero ni se modificó tu saldo.",
            primaryLabel = "Intentar de nuevo",
            onPrimary = { vm.backToTopUpSelect() },
            secondaryLabel = "Cancelar",
            onSecondary = onClose
        )
        is TopUpPhase.Timeout -> StatusScreen(
            icon = Icons.Filled.Timer,
            tint = Warning,
            title = "Tiempo de espera agotado",
            message = "La pasarela simulada no respondió a tiempo. Tu saldo no cambió; puedes volver a intentarlo.",
            primaryLabel = "Reintentar",
            onPrimary = { vm.backToTopUpSelect() },
            secondaryLabel = "Cancelar",
            onSecondary = onClose
        )
        is TopUpPhase.ConnectionError -> StatusScreen(
            icon = Icons.Filled.Close,
            tint = Warning,
            title = "Sin conexión",
            message = "Error simulado. La recarga no se procesó y tu saldo no cambió.",
            primaryLabel = "Reintentar",
            onPrimary = { vm.backToTopUpSelect() },
            secondaryLabel = "Cancelar",
            onSecondary = onClose
        )
    }
}

@Composable
private fun SelectPhase(vm: AppViewModel, p: TopUpPhase.Select, balance: Int, onClose: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Recargar saldo", onClose)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
            Row(Modifier.fillMaxWidth().card().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Saldo actual", style = MaterialTheme.typography.bodyMedium, color = InkMuted)
                    Text(balance.soles(), style = MaterialTheme.typography.headlineMedium, color = Ink)
                }
            }
            Spacer(Modifier.height(24.dp))
            Text("Elige un importe", style = MaterialTheme.typography.titleMedium, color = Ink)
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Rules.TOPUP_OPTIONS.forEach { cents ->
                    val selected = p.selectedCents == cents
                    val shape = RoundedCornerShape(18.dp)
                    Column(
                        Modifier
                            .weight(1f)
                            .clip(shape)
                            .background(if (selected) BrandBlue.copy(alpha = 0.08f) else androidx.compose.ui.graphics.Color.White, shape)
                            .border(if (selected) 2.dp else 1.dp, if (selected) BrandBlue else Outline, shape)
                            .clickable { vm.selectTopUp(cents) }
                            .padding(vertical = 22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            cents.soles().replace(".00", ""),
                            style = MaterialTheme.typography.headlineSmall,
                            color = if (selected) BrandBlue else Ink
                        )
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
            Text("Medio de pago", style = MaterialTheme.typography.titleMedium, color = Ink)
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth().card().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(44.dp).background(BrandBlue.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Filled.CreditCard, null, tint = BrandBlue) }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Tarjeta simulada •••• 4242", style = MaterialTheme.typography.titleSmall, color = Ink)
                    Text("Pasarela de pagos de demostración", style = MaterialTheme.typography.bodySmall, color = InkMuted)
                }
            }
            Spacer(Modifier.height(24.dp))
            Text("Resultado de la pasarela (demo)", style = MaterialTheme.typography.titleMedium, color = Ink)
            Spacer(Modifier.height(4.dp))
            Text(
                "Elige qué respuesta simulará el banco para probar cada estado.",
                style = MaterialTheme.typography.bodySmall, color = InkMuted
            )
            Spacer(Modifier.height(10.dp))
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TopUpScenario.values().forEach { sc ->
                    ChoiceChip(sc.label, vm.topUpScenario == sc, { vm.topUpScenario = sc })
                }
            }
            Spacer(Modifier.height(16.dp))
        }
        Box(Modifier.padding(20.dp)) {
            PrimaryButton("Continuar", onClick = { vm.goToTopUpConfirm() }, enabled = p.selectedCents != null)
        }
    }
}

@Composable
private fun ConfirmPhase(vm: AppViewModel, p: TopUpPhase.Confirm, balance: Int) {
    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Confirmar recarga", onBack = { vm.backToTopUpSelect() })
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
            Column(Modifier.fillMaxWidth().card().padding(horizontal = 20.dp, vertical = 12.dp)) {
                InfoRow("Importe a recargar", p.amountCents.soles(), bold = true)
                InfoRow("Comisión", "S/ 0.00")
                InfoRow("Medio de pago", "Tarjeta simulada •••• 4242")
                Hairline(Modifier.padding(vertical = 8.dp))
                InfoRow("Saldo actual", balance.soles())
                InfoRow("Saldo después de recargar", (balance + p.amountCents).soles(), valueColor = Success, bold = true)
            }
            Spacer(Modifier.height(14.dp))
            Text(
                "Este es un prototipo: no se realiza ningún cobro real.",
                style = MaterialTheme.typography.bodySmall, color = InkMuted, textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Column(Modifier.padding(20.dp)) {
            PrimaryButton("Confirmar recarga de ${p.amountCents.soles()}", onClick = { vm.submitTopUp() })
            Spacer(Modifier.height(10.dp))
            SecondaryButton("Cambiar importe", onClick = { vm.backToTopUpSelect() })
        }
    }
}

@Composable
private fun ProcessingPhase(amount: Int) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(color = BrandBlue, strokeWidth = 5.dp, modifier = Modifier.size(64.dp))
        Spacer(Modifier.height(28.dp))
        Text("Procesando recarga", style = MaterialTheme.typography.headlineSmall, color = Ink)
        Spacer(Modifier.height(8.dp))
        Text(
            "Contactando a la pasarela de pagos simulada para ${amount.soles()}…",
            style = MaterialTheme.typography.bodyLarge, color = InkMuted, textAlign = TextAlign.Center
        )
    }
}
