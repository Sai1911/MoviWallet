package pe.moviwallet.passenger.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import pe.moviwallet.passenger.ui.theme.BrandBlue
import pe.moviwallet.passenger.ui.theme.InkMuted
import pe.moviwallet.passenger.ui.theme.Mint
import pe.moviwallet.passenger.ui.theme.SurfaceWhite

private data class Tab(val route: String, val label: String, val icon: ImageVector)

private val leftTabs = listOf(
    Tab(Routes.HOME, "Inicio", Icons.Filled.Home),
    Tab(Routes.WALLET, "Billetera", Icons.Filled.AccountBalanceWallet)
)
private val rightTabs = listOf(
    Tab(Routes.OPERATORS, "Tarifas", Icons.Filled.Receipt),
    Tab(Routes.PROFILE, "Perfil", Icons.Filled.Person)
)

@Composable
fun MoviBottomBar(currentRoute: String?, onTab: (String) -> Unit, onPay: () -> Unit) {
    Surface(
        color = SurfaceWhite,
        shadowElevation = 12.dp,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            leftTabs.forEach { TabItem(it, currentRoute == it.route, Modifier.weight(1f)) { onTab(it.route) } }
            Column(
                Modifier.weight(1f).clip(RoundedCornerShape(16.dp)).clickable(onClick = onPay).padding(vertical = 2.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    Modifier.size(52.dp).background(Brush.linearGradient(listOf(Mint, BrandBlue)), CircleShape),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Filled.Nfc, contentDescription = "Pagar con NFC", tint = Color.White) }
                Text("Pagar", style = MaterialTheme.typography.labelSmall, color = BrandBlue)
            }
            rightTabs.forEach { TabItem(it, currentRoute == it.route, Modifier.weight(1f)) { onTab(it.route) } }
        }
    }
}

@Composable
private fun TabItem(tab: Tab, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val color = if (selected) BrandBlue else InkMuted
    Column(
        modifier.clip(RoundedCornerShape(16.dp)).clickable(onClick = onClick).padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(tab.icon, contentDescription = tab.label, tint = color, modifier = Modifier.size(24.dp))
        Text(tab.label, style = MaterialTheme.typography.labelSmall, color = color)
    }
}
