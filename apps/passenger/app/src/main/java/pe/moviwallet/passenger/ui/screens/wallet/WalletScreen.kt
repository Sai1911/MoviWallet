package pe.moviwallet.passenger.ui.screens.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.moviwallet.passenger.domain.TxType
import pe.moviwallet.passenger.domain.soles
import pe.moviwallet.passenger.ui.components.ChoiceChip
import pe.moviwallet.passenger.ui.components.EmptyState
import pe.moviwallet.passenger.ui.components.PrimaryButton
import pe.moviwallet.passenger.ui.components.ScreenHeader
import pe.moviwallet.passenger.ui.components.TransactionRow
import pe.moviwallet.passenger.ui.components.card
import pe.moviwallet.passenger.ui.theme.BrandBlue
import pe.moviwallet.passenger.ui.theme.BrandBlueDark
import pe.moviwallet.passenger.ui.theme.Ink
import pe.moviwallet.passenger.ui.theme.InkMuted
import pe.moviwallet.passenger.ui.theme.Navy
import pe.moviwallet.passenger.ui.theme.Success
import pe.moviwallet.passenger.viewmodel.AppViewModel
import androidx.compose.ui.graphics.Color

@Composable
fun WalletScreen(vm: AppViewModel, onTopUp: () -> Unit, onTransaction: (String) -> Unit) {
    val state by vm.state.collectAsStateWithLifecycle()
    var filter by remember { mutableStateOf(0) } // 0 todos, 1 recargas, 2 viajes
    val list = when (filter) {
        1 -> state.transactions.filter { it.type == TxType.TOPUP }
        2 -> state.transactions.filter { it.type == TxType.TRIP }
        else -> state.transactions
    }
    val topped = state.transactions.filter { it.type == TxType.TOPUP }.sumOf { it.amountCents }
    val spent = state.transactions.filter { it.type == TxType.TRIP }.sumOf { it.amountCents }
    val saved = state.transactions.filter { it.type == TxType.TRIP }
        .sumOf { it.profileDiscountCents + it.transferDiscountCents }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { ScreenHeader("Billetera", onBack = null, modifier = Modifier.padding(horizontal = 0.dp)) }
        item {
            Column(
                Modifier.fillMaxWidth()
                    .background(Brush.linearGradient(listOf(Navy, BrandBlueDark, BrandBlue)), RoundedCornerShape(24.dp))
                    .padding(22.dp)
            ) {
                Text("Saldo disponible", color = Color.White.copy(alpha = 0.75f), style = MaterialTheme.typography.bodyMedium)
                Text(state.balanceCents.soles(), color = Color.White, style = MaterialTheme.typography.headlineLarge)
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Stat("Recargado", topped.soles())
                    Stat("Gastado", spent.soles())
                    Stat("Ahorrado", saved.soles())
                }
                Spacer(Modifier.height(18.dp))
                PrimaryButtonOnDark(onTopUp)
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                ChoiceChip("Todos", filter == 0, { filter = 0 })
                ChoiceChip("Recargas", filter == 1, { filter = 1 })
                ChoiceChip("Viajes", filter == 2, { filter = 2 })
            }
        }
        if (list.isEmpty()) {
            item {
                EmptyState(
                    Icons.Filled.History,
                    "Sin operaciones",
                    if (state.transactions.isEmpty()) "Tus recargas y viajes aparecerán aquí."
                    else "No hay operaciones para este filtro."
                )
            }
        } else {
            items(list, key = { it.id }) { tx ->
                TransactionRow(tx, onClick = { onTransaction(tx.id) })
            }
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun Stat(label: String, value: String) {
    Column {
        Text(label, color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.labelMedium)
        Text(value, color = Color.White, style = MaterialTheme.typography.titleSmall)
    }
}

@Composable
private fun PrimaryButtonOnDark(onClick: () -> Unit) {
    androidx.compose.material3.Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(50.dp),
        shape = RoundedCornerShape(16.dp),
        colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Navy)
    ) {
        androidx.compose.material3.Icon(Icons.Filled.Add, null)
        Text("  Recargar saldo", style = MaterialTheme.typography.labelLarge)
    }
}
