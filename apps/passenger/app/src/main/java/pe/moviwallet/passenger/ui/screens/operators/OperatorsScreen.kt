package pe.moviwallet.passenger.ui.screens.operators

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.clickable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.unit.dp
import pe.moviwallet.passenger.domain.FareEngine
import pe.moviwallet.passenger.domain.Operator
import pe.moviwallet.passenger.domain.Rules
import pe.moviwallet.passenger.domain.UserProfile
import pe.moviwallet.passenger.domain.soles
import pe.moviwallet.passenger.ui.components.ChoiceChip
import pe.moviwallet.passenger.ui.components.Hairline
import pe.moviwallet.passenger.ui.components.InfoRow
import pe.moviwallet.passenger.ui.components.OperatorAvatar
import pe.moviwallet.passenger.ui.components.Pill
import pe.moviwallet.passenger.ui.components.ScreenHeader
import pe.moviwallet.passenger.ui.components.card
import pe.moviwallet.passenger.ui.theme.BrandBlue
import pe.moviwallet.passenger.ui.theme.Ink
import pe.moviwallet.passenger.ui.theme.InkMuted
import pe.moviwallet.passenger.ui.theme.Mint
import pe.moviwallet.passenger.ui.theme.Success
import pe.moviwallet.passenger.ui.theme.color
import pe.moviwallet.passenger.viewmodel.AppViewModel

@Composable
fun OperatorsScreen(vm: AppViewModel, onTransfer: () -> Unit) {
    val state by vm.state.collectAsStateWithLifecycle()
    val profile = state.profile

    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Operadores y tarifas", onBack = null)
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Ver tarifas como", style = MaterialTheme.typography.titleSmall, color = InkMuted)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                UserProfile.values().forEach { p ->
                    ChoiceChip(p.label, profile == p, { vm.setProfile(p) })
                }
            }

            Operator.values().forEach { op ->
                Column(Modifier.fillMaxWidth().card().padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OperatorAvatar(op, 48.dp)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(op.label, style = MaterialTheme.typography.titleMedium, color = Ink)
                            Text(op.mode, style = MaterialTheme.typography.bodySmall, color = InkMuted)
                        }
                        Pill("Disponible", Success)
                    }
                    Spacer(Modifier.height(12.dp))
                    Hairline()
                    Spacer(Modifier.height(6.dp))
                    InfoRow("Tarifa normal", op.baseFareCents.soles(), valueColor = if (profile == UserProfile.NORMAL) op.color() else Ink, bold = profile == UserProfile.NORMAL)
                    InfoRow(
                        "Tarifa universitaria (50 %)",
                        FareEngine.profileFare(op, UserProfile.UNIVERSITARIO).soles(),
                        valueColor = if (profile == UserProfile.UNIVERSITARIO) op.color() else Ink,
                        bold = profile == UserProfile.UNIVERSITARIO
                    )
                    InfoRow(
                        "Con transbordo (${profile.label.lowercase()})",
                        FareEngine.transferFare(op, profile).soles(),
                        valueColor = Success
                    )
                }
            }

            Column(Modifier.fillMaxWidth().card(color = Mint.copy(alpha = 0.10f)).clickable(onClick = onTransfer).padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.SwapHoriz, null, tint = Success)
                    Spacer(Modifier.width(10.dp))
                    Text("Transbordos", style = MaterialTheme.typography.titleMedium, color = Ink, modifier = Modifier.weight(1f))
                    Icon(Icons.Filled.ChevronRight, null, tint = InkMuted)
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "Si tras un viaje cambias a un operador diferente dentro de ${Rules.TRANSFER_WINDOW_MINUTES} minutos, " +
                        "el segundo viaje tiene ${Rules.TRANSFER_DISCOUNT_CENTS.soles()} de descuento. " +
                        "Solo se aplica un descuento por ventana.",
                    style = MaterialTheme.typography.bodyMedium, color = InkMuted
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    "Ejemplo (${profile.label.lowercase()}): A ${FareEngine.profileFare(Operator.A, profile).soles()} → " +
                        "B con transbordo ${FareEngine.transferFare(Operator.B, profile).soles()}",
                    style = MaterialTheme.typography.titleSmall, color = Ink
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}
