package pe.moviwallet.passenger.ui.screens.transfer

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.shape.RoundedCornerShape
import pe.moviwallet.passenger.domain.FareEngine
import pe.moviwallet.passenger.domain.Rules
import pe.moviwallet.passenger.domain.formatClock
import pe.moviwallet.passenger.domain.formatHms
import pe.moviwallet.passenger.domain.formatRemaining
import pe.moviwallet.passenger.domain.soles
import pe.moviwallet.passenger.ui.components.Hairline
import pe.moviwallet.passenger.ui.components.InfoRow
import pe.moviwallet.passenger.ui.components.OperatorAvatar
import pe.moviwallet.passenger.ui.components.Pill
import pe.moviwallet.passenger.ui.components.ScreenHeader
import pe.moviwallet.passenger.ui.components.card
import pe.moviwallet.passenger.ui.components.rememberNow
import pe.moviwallet.passenger.ui.theme.BrandBlue
import pe.moviwallet.passenger.ui.theme.Ink
import pe.moviwallet.passenger.ui.theme.InkMuted
import pe.moviwallet.passenger.ui.theme.Mint
import pe.moviwallet.passenger.ui.theme.Outline
import pe.moviwallet.passenger.ui.theme.Success
import pe.moviwallet.passenger.ui.theme.Warning
import pe.moviwallet.passenger.viewmodel.AppViewModel

@Composable
fun TransferScreen(vm: AppViewModel, onBack: () -> Unit) {
    val state by vm.state.collectAsStateWithLifecycle()
    val now by rememberNow(vm, state.clockOffsetMillis)
    val window = state.window
    val active = window != null && window.isActive(now)
    val totalMillis = Rules.TRANSFER_WINDOW_MINUTES * 60_000f
    val remaining = if (active && window != null) window.remainingMillis(now) else 0L
    val progress = if (active) remaining / totalMillis else 0f

    val (statusLabel, statusColor) = when {
        window == null -> "Sin ventana activa" to InkMuted
        !active -> "Ventana vencida" to Warning
        window.discountUsed -> "Descuento aplicado" to BrandBlue
        else -> "Descuento disponible" to Success
    }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Transbordos", onBack)
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column(Modifier.fillMaxWidth().card().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Pill(statusLabel, statusColor)
                Spacer(Modifier.height(16.dp))
                Box(Modifier.size(210.dp), contentAlignment = Alignment.Center) {
                    val ringColor = if (window?.discountUsed == true) BrandBlue else Mint
                    Canvas(Modifier.fillMaxSize()) {
                        val stroke = 18.dp.toPx()
                        val arcSize = Size(size.width - stroke, size.height - stroke)
                        val topLeft = Offset(stroke / 2f, stroke / 2f)
                        drawArc(
                            color = Outline, startAngle = -90f, sweepAngle = 360f, useCenter = false,
                            topLeft = topLeft, size = arcSize, style = Stroke(stroke, cap = StrokeCap.Round)
                        )
                        if (progress > 0f) {
                            drawArc(
                                color = ringColor, startAngle = -90f, sweepAngle = 360f * progress, useCenter = false,
                                topLeft = topLeft, size = arcSize, style = Stroke(stroke, cap = StrokeCap.Round)
                            )
                        }
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            if (active) formatHms(remaining) else "--:--:--",
                            style = MaterialTheme.typography.headlineMedium, color = Ink
                        )
                        Text(
                            "de ${Rules.TRANSFER_WINDOW_MINUTES} min",
                            style = MaterialTheme.typography.bodySmall, color = InkMuted
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))
                Text(
                    when {
                        window == null || !active ->
                            "Tu próximo viaje iniciará una ventana de ${Rules.TRANSFER_WINDOW_MINUTES} minutos para transbordar."
                        window.discountUsed ->
                            "Ya usaste el descuento de esta ventana. Un nuevo viaje pagará tarifa completa hasta que venza."
                        else ->
                            "Viaja en ${window.originOperator.other.label} dentro de ${formatRemaining(remaining)} y paga " +
                                "${Rules.TRANSFER_DISCOUNT_CENTS.soles()} menos."
                    },
                    style = MaterialTheme.typography.bodyMedium, color = InkMuted, textAlign = TextAlign.Center
                )
            }

            Column(Modifier.fillMaxWidth().card().padding(horizontal = 18.dp, vertical = 12.dp)) {
                Text("Detalle de la ventana", style = MaterialTheme.typography.titleSmall, color = Ink)
                Spacer(Modifier.height(6.dp))
                if (window != null) {
                    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        OperatorAvatar(window.originOperator, 36.dp)
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Operador del primer viaje", style = MaterialTheme.typography.bodySmall, color = InkMuted)
                            Text(window.originOperator.label, style = MaterialTheme.typography.titleSmall, color = Ink)
                        }
                    }
                    Hairline()
                    InfoRow("Inicio de la ventana", formatClock(window.startMillis))
                    InfoRow("Vence a las", formatClock(window.expiresAtMillis))
                    InfoRow(
                        "Descuento aplicado",
                        if (window.discountUsed) Rules.TRANSFER_DISCOUNT_CENTS.soles() else "Pendiente",
                        valueColor = if (window.discountUsed) Success else InkMuted
                    )
                    InfoRow("Estado del beneficio", statusLabel, valueColor = statusColor)
                    if (!window.discountUsed && active) {
                        InfoRow("Operador requerido", window.originOperator.other.label)
                    }
                } else {
                    Text(
                        "Aún no hay viajes en curso. Paga un viaje con NFC para iniciar una ventana.",
                        style = MaterialTheme.typography.bodyMedium, color = InkMuted,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }

            Column(Modifier.fillMaxWidth().card().padding(18.dp)) {
                Text("Reglas del transbordo", style = MaterialTheme.typography.titleSmall, color = Ink)
                Spacer(Modifier.height(8.dp))
                Rule("Ventana de ${Rules.TRANSFER_WINDOW_MINUTES} minutos desde el primer viaje.")
                Rule("El segundo viaje debe ser en un operador diferente.")
                Rule("Descuento fijo de ${Rules.TRANSFER_DISCOUNT_CENTS.soles()} sobre la tarifa del perfil.")
                Rule("Un solo descuento por ventana.")
                Rule("El saldo nunca puede quedar negativo.")
            }

            Column(Modifier.fillMaxWidth().card().padding(18.dp)) {
                Text("Controles de demostración", style = MaterialTheme.typography.titleSmall, color = Ink)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Adelanta el reloj simulado para probar el vencimiento de la ventana.",
                    style = MaterialTheme.typography.bodySmall, color = InkMuted
                )
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DemoButton("+30 min", Modifier.weight(1f)) { vm.advanceClock(30) }
                    DemoButton("+2 h", Modifier.weight(1f)) { vm.advanceClock(120) }
                    DemoButton("Reiniciar", Modifier.weight(1f)) { vm.clearWindow() }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun Rule(text: String) {
    Row(Modifier.padding(vertical = 3.dp)) {
        Text("•", color = BrandBlue, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = InkMuted)
    }
}

@Composable
private fun DemoButton(text: String, modifier: Modifier, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Outline),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandBlue),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp)
    ) { Text(text, style = MaterialTheme.typography.labelLarge, fontSize = 13.sp) }
}
