package pe.moviwallet.passenger.ui.screens.pay

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.WifiOff
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.moviwallet.passenger.domain.FareEngine
import pe.moviwallet.passenger.domain.Operator
import pe.moviwallet.passenger.domain.soles
import pe.moviwallet.passenger.ui.components.ChoiceChip
import pe.moviwallet.passenger.ui.components.Hairline
import pe.moviwallet.passenger.ui.components.InfoRow
import pe.moviwallet.passenger.ui.components.OperatorAvatar
import pe.moviwallet.passenger.ui.components.PrimaryButton
import pe.moviwallet.passenger.ui.components.ScreenHeader
import pe.moviwallet.passenger.ui.components.SecondaryButton
import pe.moviwallet.passenger.ui.components.StatusScreen
import pe.moviwallet.passenger.ui.components.card
import pe.moviwallet.passenger.ui.components.rememberNow
import pe.moviwallet.passenger.ui.theme.BrandBlue
import pe.moviwallet.passenger.ui.theme.BrandBlueDark
import pe.moviwallet.passenger.ui.theme.ErrorRed
import pe.moviwallet.passenger.ui.theme.Ink
import pe.moviwallet.passenger.ui.theme.InkMuted
import pe.moviwallet.passenger.ui.theme.Mint
import pe.moviwallet.passenger.ui.theme.Outline
import pe.moviwallet.passenger.ui.theme.Success
import pe.moviwallet.passenger.ui.theme.Warning
import pe.moviwallet.passenger.ui.theme.color
import pe.moviwallet.passenger.viewmodel.AppViewModel
import pe.moviwallet.passenger.viewmodel.PayPhase
import pe.moviwallet.passenger.viewmodel.PayScenario

@Composable
fun PayScreen(
    vm: AppViewModel,
    onClose: () -> Unit,
    onHome: () -> Unit,
    onTopUp: () -> Unit,
    onReceipt: (String) -> Unit
) {
    val phase by vm.pay.collectAsStateWithLifecycle()

    DisposableEffect(Unit) { onDispose { vm.resetPay() } }

    BackHandler {
        when (phase) {
            is PayPhase.Prepare -> onClose()
            is PayPhase.Reading, is PayPhase.Processing -> vm.resetPay()
            else -> onClose()
        }
    }

    when (val p = phase) {
        is PayPhase.Prepare -> PreparePhase(vm, onClose)
        is PayPhase.Reading -> ReadingPhase(p.op, onCancel = { vm.resetPay() })
        is PayPhase.Processing -> ProcessingPhase(p.op)
        is PayPhase.Approved -> StatusScreen(
            icon = Icons.Filled.Check,
            tint = Success,
            title = "Pago autorizado",
            message = "Puedes pasar. ¡Buen viaje!",
            primaryLabel = "Ver comprobante",
            onPrimary = { onReceipt(p.tx.id) },
            secondaryLabel = "Volver al inicio",
            onSecondary = onHome,
            details = {
                Column(Modifier.fillMaxWidth().card().padding(horizontal = 18.dp, vertical = 8.dp)) {
                    InfoRow("Operador", p.tx.operator?.label ?: "-")
                    InfoRow("Tarifa base", p.quote.baseCents.soles())
                    if (p.quote.profileDiscountCents > 0) {
                        InfoRow("Descuento universitario", "-" + p.quote.profileDiscountCents.soles(), valueColor = Success)
                    }
                    if (p.quote.transferDiscountCents > 0) {
                        InfoRow("Descuento por transbordo", "-" + p.quote.transferDiscountCents.soles(), valueColor = Success)
                    }
                    Hairline(Modifier.padding(vertical = 4.dp))
                    InfoRow("Total cobrado", p.tx.amountCents.soles(), bold = true)
                    InfoRow("Saldo restante", p.tx.balanceAfterCents.soles(), valueColor = BrandBlue, bold = true)
                }
            }
        )
        is PayPhase.Insufficient -> StatusScreen(
            icon = Icons.Filled.AccountBalanceWallet,
            tint = Warning,
            title = "Saldo insuficiente",
            message = "No se realizó ningún cobro. Recarga tu saldo para completar el viaje.",
            primaryLabel = "Recargar saldo",
            onPrimary = onTopUp,
            secondaryLabel = "Volver",
            onSecondary = { vm.resetPay() },
            details = {
                Column(Modifier.fillMaxWidth().card().padding(horizontal = 18.dp, vertical = 8.dp)) {
                    InfoRow("Tarifa a cobrar", p.requiredCents.soles())
                    InfoRow("Tu saldo", p.balanceCents.soles())
                    InfoRow("Te faltan", (p.requiredCents - p.balanceCents).soles(), valueColor = ErrorRed, bold = true)
                }
            }
        )
        is PayPhase.InvalidCredential -> StatusScreen(
            icon = Icons.Filled.Block,
            tint = ErrorRed,
            title = "Credencial inválida",
            message = "El lector no pudo validar tu credencial NFC. No se realizó ningún cobro. Revisa que esté activa en tu perfil o que la simulación esté en «Normal».",
            primaryLabel = "Intentar de nuevo",
            onPrimary = { vm.resetPay() },
            secondaryLabel = "Cerrar",
            onSecondary = onClose
        )
        is PayPhase.ConnectionError -> StatusScreen(
            icon = Icons.Filled.WifiOff,
            tint = Warning,
            title = "Error de conexión",
            message = "Error de conexión simulado. No se confirmó el viaje ni se modificó tu saldo.",
            primaryLabel = "Reintentar",
            onPrimary = { vm.resetPay() },
            secondaryLabel = "Cerrar",
            onSecondary = onClose
        )
        is PayPhase.Repeated -> StatusScreen(
            icon = Icons.Filled.Info,
            tint = Warning,
            title = "Lectura repetida",
            message = "Ya pagaste con este operador hace menos de 30 segundos. No se realizó otro cobro.",
            primaryLabel = "Volver al inicio",
            onPrimary = onHome,
            secondaryLabel = "Reintentar",
            onSecondary = { vm.resetPay() }
        )
    }
}

@Composable
private fun PreparePhase(vm: AppViewModel, onClose: () -> Unit) {
    val state by vm.state.collectAsStateWithLifecycle()
    val now by rememberNow(vm, state.clockOffsetMillis)
    val op = vm.payOperator
    val quote = FareEngine.quote(op, state.profile, state.window, now)
    val insufficient = state.balanceCents < quote.totalCents

    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Pagar con NFC", onClose)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
            Text("Selecciona el operador", style = MaterialTheme.typography.titleMedium, color = Ink)
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Operator.values().forEach { o ->
                    val selected = o == op
                    val shape = RoundedCornerShape(20.dp)
                    val fare = FareEngine.quote(o, state.profile, state.window, now).totalCents
                    Column(
                        Modifier
                            .weight(1f)
                            .clip(shape)
                            .background(if (selected) o.color().copy(alpha = 0.08f) else Color.White, shape)
                            .border(if (selected) 2.dp else 1.dp, if (selected) o.color() else Outline, shape)
                            .clickable { vm.payOperator = o }
                            .padding(16.dp)
                    ) {
                        OperatorAvatar(o)
                        Spacer(Modifier.height(10.dp))
                        Text(o.label, style = MaterialTheme.typography.titleSmall, color = Ink)
                        Text(o.mode, style = MaterialTheme.typography.bodySmall, color = InkMuted)
                        Spacer(Modifier.height(8.dp))
                        Text(fare.soles(), style = MaterialTheme.typography.titleMedium, color = o.color())
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            Column(Modifier.fillMaxWidth().card().padding(horizontal = 18.dp, vertical = 12.dp)) {
                Text("Resumen del cobro", style = MaterialTheme.typography.titleSmall, color = Ink)
                Spacer(Modifier.height(4.dp))
                InfoRow("Tarifa base · ${op.label}", quote.baseCents.soles())
                if (quote.profileDiscountCents > 0) {
                    InfoRow("Descuento universitario (50 %)", "-" + quote.profileDiscountCents.soles(), valueColor = Success)
                }
                if (quote.transferApplied) {
                    InfoRow("Descuento por transbordo", "-" + quote.transferDiscountCents.soles(), valueColor = Success)
                } else {
                    InfoRow("Descuento por transbordo", "No aplica", valueColor = InkMuted)
                }
                Hairline(Modifier.padding(vertical = 6.dp))
                InfoRow("Total a pagar", quote.totalCents.soles(), bold = true)
                InfoRow("Saldo disponible", state.balanceCents.soles(), valueColor = if (insufficient) ErrorRed else Ink)
            }

            if (quote.transferApplied) {
                Spacer(Modifier.height(12.dp))
                Banner(Icons.Filled.SwapHoriz, "Transbordo detectado: ahorras ${quote.transferDiscountCents.soles()} en este viaje.", Success)
            }
            if (insufficient) {
                Spacer(Modifier.height(12.dp))
                Banner(Icons.Filled.Info, "Tu saldo no alcanza para esta tarifa. Puedes recargar o probar igualmente para ver el aviso.", Warning)
            }

            Spacer(Modifier.height(20.dp))
            Text("Simulación del lector (demo)", style = MaterialTheme.typography.titleMedium, color = Ink)
            Spacer(Modifier.height(4.dp))
            Text(
                "Prototipo: la lectura NFC es simulada visualmente; no se usa el chip NFC real.",
                style = MaterialTheme.typography.bodySmall, color = InkMuted
            )
            Spacer(Modifier.height(10.dp))
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PayScenario.values().forEach { sc ->
                    ChoiceChip(sc.label, vm.payScenario == sc, { vm.payScenario = sc })
                }
            }
            Spacer(Modifier.height(16.dp))
        }
        Box(Modifier.padding(20.dp)) {
            PrimaryButton("Acercar teléfono al lector", onClick = { vm.startPay() }, icon = Icons.Filled.Nfc)
        }
    }
}

@Composable
private fun Banner(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, tint: Color) {
    Row(
        Modifier.fillMaxWidth().card(color = tint.copy(alpha = 0.10f)).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(10.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = Ink)
    }
}

@Composable
private fun ReadingPhase(op: Operator, onCancel: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.weight(1f))
        NfcPulse()
        Spacer(Modifier.height(24.dp))
        Text("Acerca tu teléfono al lector", style = MaterialTheme.typography.headlineSmall, color = Ink, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(
            "Lectura simulada · ${op.label}",
            style = MaterialTheme.typography.bodyLarge, color = InkMuted, textAlign = TextAlign.Center
        )
        Spacer(Modifier.weight(1f))
        SecondaryButton("Cancelar", onCancel)
    }
}

@Composable
private fun NfcPulse() {
    val t = rememberInfiniteTransition(label = "nfcPulse")
    val p1 by t.animateFloat(
        0f, 1f, infiniteRepeatable(tween(2000, easing = LinearEasing)), label = "p1"
    )
    val p2 by t.animateFloat(
        0f, 1f, infiniteRepeatable(tween(2000, easing = LinearEasing), initialStartOffset = StartOffset(660)), label = "p2"
    )
    val p3 by t.animateFloat(
        0f, 1f, infiniteRepeatable(tween(2000, easing = LinearEasing), initialStartOffset = StartOffset(1330)), label = "p3"
    )
    Box(Modifier.size(260.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            listOf(p1, p2, p3).forEach { p ->
                drawCircle(
                    color = BrandBlue.copy(alpha = (1f - p) * 0.35f),
                    radius = size.minDimension / 2f * (0.35f + 0.65f * p)
                )
            }
        }
        Box(
            Modifier.size(100.dp).background(Brush.linearGradient(listOf(Mint, BrandBlue, BrandBlueDark)), CircleShape),
            contentAlignment = Alignment.Center
        ) { Icon(Icons.Filled.Nfc, null, tint = Color.White, modifier = Modifier.size(48.dp)) }
    }
}

@Composable
private fun ProcessingPhase(op: Operator) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(color = BrandBlue, strokeWidth = 5.dp, modifier = Modifier.size(64.dp))
        Spacer(Modifier.height(28.dp))
        Text("Procesando pago", style = MaterialTheme.typography.headlineSmall, color = Ink)
        Spacer(Modifier.height(8.dp))
        Text(
            "Validando credencial y calculando tarifa en ${op.label}…",
            style = MaterialTheme.typography.bodyLarge, color = InkMuted, textAlign = TextAlign.Center
        )
    }
}
