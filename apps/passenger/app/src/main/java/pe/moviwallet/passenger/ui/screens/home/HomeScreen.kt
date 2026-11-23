package pe.moviwallet.passenger.ui.screens.home

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.moviwallet.passenger.domain.FareEngine
import pe.moviwallet.passenger.domain.Operator
import pe.moviwallet.passenger.domain.formatRemaining
import pe.moviwallet.passenger.domain.soles
import pe.moviwallet.passenger.ui.components.EmptyState
import pe.moviwallet.passenger.ui.components.MoviCard
import pe.moviwallet.passenger.ui.components.OperatorAvatar
import pe.moviwallet.passenger.ui.components.SectionTitle
import pe.moviwallet.passenger.ui.components.TransactionRow
import pe.moviwallet.passenger.ui.components.card
import pe.moviwallet.passenger.ui.components.rememberNow
import pe.moviwallet.passenger.ui.theme.BrandBlue
import pe.moviwallet.passenger.ui.theme.BrandBlueDark
import pe.moviwallet.passenger.ui.theme.Ink
import pe.moviwallet.passenger.ui.theme.InkMuted
import pe.moviwallet.passenger.ui.theme.Mint
import pe.moviwallet.passenger.ui.theme.Navy
import pe.moviwallet.passenger.ui.theme.Success
import pe.moviwallet.passenger.ui.theme.color
import pe.moviwallet.passenger.viewmodel.AppViewModel
import java.util.Calendar

private fun greeting(): String {
    val h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when {
        h < 12 -> "Buenos días"
        h < 19 -> "Buenas tardes"
        else -> "Buenas noches"
    }
}

@Composable
fun HomeScreen(
    vm: AppViewModel,
    onPay: () -> Unit,
    onTopUp: () -> Unit,
    onWallet: () -> Unit,
    onOperators: () -> Unit,
    onTransfer: () -> Unit,
    onTransaction: (String) -> Unit
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val now by rememberNow(vm, state.clockOffsetMillis)
    val user = state.user
    val fullName = user?.name ?: "Usuario"
    val firstName = fullName.substringBefore(" ")
    val window = state.window
    val windowActive = window != null && window.isActive(now)

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("${greeting()},", style = MaterialTheme.typography.bodyLarge, color = InkMuted)
                Text(firstName, style = MaterialTheme.typography.headlineMedium, color = Ink)
            }
            Box(
                Modifier.size(46.dp).background(BrandBlue.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    firstName.take(1).uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    color = BrandBlue
                )
            }
        }
        Spacer(Modifier.height(20.dp))
        MoviCard(fullName, state.balanceCents, state.profile)
        Spacer(Modifier.height(18.dp))
        PayHeroButton(onPay)
        Spacer(Modifier.height(18.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickAction("Recargar", Icons.Filled.Add, onTopUp, Modifier.weight(1f))
            QuickAction("Transbordo", Icons.Filled.SwapHoriz, onTransfer, Modifier.weight(1f))
            QuickAction("Historial", Icons.Filled.History, onWallet, Modifier.weight(1f))
            QuickAction("Tarifas", Icons.Filled.Receipt, onOperators, Modifier.weight(1f))
        }

        if (windowActive && window != null) {
            Spacer(Modifier.height(18.dp))
            val remaining = formatRemaining(window.remainingMillis(now))
            Row(
                Modifier.fillMaxWidth().card(color = Mint.copy(alpha = 0.10f)).clickable(onClick = onTransfer).padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.SwapHoriz, null, tint = Success)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        if (window.discountUsed) "Descuento de transbordo aplicado" else "Transbordo activo",
                        style = MaterialTheme.typography.titleSmall, color = Ink
                    )
                    Text(
                        if (window.discountUsed) "La ventana vence en $remaining"
                        else "Quedan $remaining para viajar en ${window.originOperator.other.label} con S/ 0.50 de descuento",
                        style = MaterialTheme.typography.bodySmall, color = InkMuted
                    )
                }
                Icon(Icons.Filled.ChevronRight, null, tint = InkMuted)
            }
        }

        Spacer(Modifier.height(24.dp))
        SectionTitle("Operadores disponibles", action = "Ver tarifas", onAction = onOperators)
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Operator.values().forEach { op ->
                val fare = FareEngine.profileFare(op, state.profile)
                Column(
                    Modifier.weight(1f).card().clickable(onClick = onOperators).padding(14.dp)
                ) {
                    OperatorAvatar(op)
                    Spacer(Modifier.height(10.dp))
                    Text(op.label, style = MaterialTheme.typography.titleSmall, color = Ink)
                    Text(op.mode, style = MaterialTheme.typography.bodySmall, color = InkMuted)
                    Spacer(Modifier.height(8.dp))
                    Text(fare.soles(), style = MaterialTheme.typography.titleMedium, color = op.color())
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        SectionTitle("Últimos movimientos", action = "Ver todo", onAction = onWallet)
        Spacer(Modifier.height(10.dp))
        if (state.transactions.isEmpty()) {
            EmptyState(
                Icons.Filled.History,
                "Aún no tienes movimientos",
                "Recarga tu saldo para empezar a viajar."
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                state.transactions.take(3).forEach { tx ->
                    TransactionRow(tx, onClick = { onTransaction(tx.id) })
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun QuickAction(label: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier.card().clickable(onClick = onClick).padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier.size(40.dp).background(BrandBlue.copy(alpha = 0.10f), CircleShape),
            contentAlignment = Alignment.Center
        ) { Icon(icon, null, tint = BrandBlue, modifier = Modifier.size(22.dp)) }
        Spacer(Modifier.height(8.dp))
        Text(label, style = MaterialTheme.typography.labelMedium, color = Ink)
    }
}

@Composable
private fun PayHeroButton(onClick: () -> Unit) {
    val t = rememberInfiniteTransition(label = "hero")
    val pulse by t.animateFloat(
        initialValue = 1f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(tween(1600, easing = LinearEasing), RepeatMode.Restart),
        label = "heroPulse"
    )
    val alpha = (1.35f - pulse).coerceIn(0f, 0.35f)
    val shape = RoundedCornerShape(24.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Brush.horizontalGradient(listOf(Mint, BrandBlue)), shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(64.dp), contentAlignment = Alignment.Center) {
            Box(Modifier.size(52.dp).scale(pulse).background(Color.White.copy(alpha = alpha), CircleShape))
            Box(
                Modifier.size(52.dp).background(Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Filled.Nfc, null, tint = BrandBlueDark, modifier = Modifier.size(28.dp)) }
        }
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text("Pagar con NFC", style = MaterialTheme.typography.titleLarge, color = Color.White)
            Text("Acerca tu teléfono al lector", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.9f))
        }
        Icon(Icons.Filled.ChevronRight, null, tint = Color.White)
    }
}
