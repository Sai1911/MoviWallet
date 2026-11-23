package pe.moviwallet.passenger.ui.screens.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import pe.moviwallet.passenger.domain.Transaction
import pe.moviwallet.passenger.domain.TxType
import pe.moviwallet.passenger.domain.formatDateTime
import pe.moviwallet.passenger.domain.soles
import pe.moviwallet.passenger.ui.components.DashedLine
import pe.moviwallet.passenger.ui.components.InfoRow
import pe.moviwallet.passenger.ui.components.PrimaryButton
import pe.moviwallet.passenger.ui.components.ScreenHeader
import pe.moviwallet.passenger.ui.components.card
import pe.moviwallet.passenger.ui.theme.BrandBlue
import pe.moviwallet.passenger.ui.theme.Ink
import pe.moviwallet.passenger.ui.theme.InkMuted
import pe.moviwallet.passenger.ui.theme.Success

/** Comprobante / detalle de una operación (recarga o viaje). */
@Composable
fun ReceiptScreen(
    tx: Transaction?,
    title: String,
    primaryLabel: String,
    onPrimary: () -> Unit,
    onBack: () -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        ScreenHeader(title, onBack)
        if (tx == null) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.ErrorOutline, null, tint = InkMuted, modifier = Modifier.size(40.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("No encontramos esta operación", color = InkMuted)
                }
            }
        } else {
            val isTopUp = tx.type == TxType.TOPUP
            val accent = if (isTopUp) BrandBlue else Success
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
                Spacer(Modifier.height(8.dp))
                Column(Modifier.fillMaxWidth().card()) {
                    Column(
                        Modifier.fillMaxWidth().background(accent).padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            Modifier.size(48.dp).background(Color.White.copy(alpha = 0.22f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(if (isTopUp) Icons.Filled.Add else Icons.Filled.Check, null, tint = Color.White)
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(
                            if (isTopUp) "Recarga exitosa" else "Pago aprobado",
                            color = Color.White.copy(alpha = 0.9f), style = MaterialTheme.typography.bodyMedium
                        )
                        Text(tx.amountCents.soles(), color = Color.White, style = MaterialTheme.typography.headlineLarge)
                    }
                    Column(Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
                        InfoRow("Código", tx.receiptCode)
                        InfoRow("Fecha y hora", formatDateTime(tx.timestampMillis))
                        InfoRow("Tipo", if (isTopUp) "Recarga de saldo" else "Viaje")
                        if (!isTopUp) {
                            InfoRow("Operador", tx.operator?.label ?: "-")
                            InfoRow("Perfil", tx.profile.label)
                        } else {
                            InfoRow("Medio de pago", "Tarjeta simulada •••• 4242")
                        }
                        DashedLine(Modifier.padding(vertical = 10.dp))
                        if (!isTopUp) {
                            InfoRow("Tarifa base", tx.baseFareCents.soles())
                            if (tx.profileDiscountCents > 0) {
                                InfoRow("Descuento universitario", "-" + tx.profileDiscountCents.soles(), valueColor = Success)
                            }
                            if (tx.transferDiscountCents > 0) {
                                InfoRow("Descuento por transbordo", "-" + tx.transferDiscountCents.soles(), valueColor = Success)
                            }
                            InfoRow("Total cobrado", tx.amountCents.soles(), bold = true)
                        } else {
                            InfoRow("Importe recargado", tx.amountCents.soles(), bold = true)
                        }
                        InfoRow("Saldo después", tx.balanceAfterCents.soles())
                        InfoRow("Estado", "Aprobado", valueColor = Success)
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    "Comprobante generado por el prototipo académico de MoviWallet. No tiene validez tributaria.",
                    style = MaterialTheme.typography.bodySmall, color = InkMuted, textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))
            }
            Box(Modifier.padding(20.dp)) { PrimaryButton(primaryLabel, onPrimary) }
        }
    }
}
