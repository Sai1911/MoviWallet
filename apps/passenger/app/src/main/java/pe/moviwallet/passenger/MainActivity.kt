package pe.moviwallet.passenger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.AltRoute
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Forest = Color(0xFF103F36)
private val Lime = Color(0xFFD6F880)
private val Paper = Color(0xFFF5F7F2)
private val Muted = Color(0xFF66766E)
private val Line = Color(0xFFE3E9E1)
private val Coral = Color(0xFFB54732)
private val Shape = RoundedCornerShape(24.dp)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme(colorScheme = lightColorScheme(primary = Forest, onPrimary = Color.White, secondary = Forest,
                background = Paper, surface = Color.White, onSurface = Forest, onBackground = Forest,
                primaryContainer = Lime, onPrimaryContainer = Forest, outline = Muted)) {
                MoviWallet()
            }
        }
    }
}

@Composable
private fun MoviWallet(vm: WalletViewModel = viewModel()) {
    var page by rememberSaveable { mutableStateOf("Inicio") }
    var receipt by remember { mutableStateOf<Movement?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    val wallet = vm.wallet
    val pageScroll = remember(page) { ScrollState(0) }
    fun result(value: Result<Movement>) { value.onSuccess { receipt = it }.onFailure { error = it.message } }
    BackHandler(enabled = vm.signedIn && page != "Inicio") { page = "Inicio" }
    if (!vm.signedIn) Welcome { vm.enter(); page = "Inicio" }
    else Scaffold(containerColor = Paper, bottomBar = {
        NavigationBar(containerColor = Color.White, tonalElevation = 0.dp) {
            listOf("Inicio" to Icons.Rounded.Home, "Pase" to Icons.Rounded.Contactless,
                "Actividad" to Icons.AutoMirrored.Rounded.ReceiptLong, "Perfil" to Icons.Rounded.PersonOutline).forEach { (label, icon) ->
                NavigationBarItem(selected = page == label, onClick = { page = label },
                    icon = { Icon(icon, label) }, label = { Text(label, fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(indicatorColor = Lime))
            }
        }
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(pageScroll).padding(horizontal = 24.dp).padding(top = 20.dp, bottom = 28.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
            when (page) {
                "Inicio" -> {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) { Text("QUÉ BUENO VERTE", color = Muted, fontSize = 10.sp, letterSpacing = 2.sp); Text("Hola, Alex", fontSize = 28.sp, fontWeight = FontWeight.Bold) }
                        Surface(onClick = { page = "Perfil" }, shape = CircleShape, color = Lime) { Text("AL", Modifier.padding(16.dp), fontWeight = FontWeight.Bold) }
                    }
                    BalanceCard(wallet.balance) { page = "Recargar" }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ActionCard("Mi pase", "Listo para viajar", Icons.Rounded.Contactless, Modifier.weight(1f)) { page = "Pase" }
                        ActionCard("Movimientos", "Todo en un lugar", Icons.AutoMirrored.Rounded.ReceiptLong, Modifier.weight(1f)) { page = "Actividad" }
                    }
                    Surface(shape = Shape, color = Color(0xFFE8EEDC)) {
                        Row(Modifier.padding(20.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            Icon(Icons.AutoMirrored.Rounded.AltRoute, null, tint = Forest)
                            Column { Text("Conecta tus viajes. Ahorra más.", fontWeight = FontWeight.Bold, fontSize = 15.sp); Spacer(Modifier.height(5.dp)); Text("Cambia de operador en menos de 120 min y recibe S/ 0.50 de descuento una vez por ventana.", color = Muted, fontSize = 12.sp, lineHeight = 18.sp) }
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) { Text("Tu actividad", Modifier.weight(1f), fontSize = 20.sp, fontWeight = FontWeight.Bold); TextButton(onClick = { page = "Actividad" }) { Text("Ver todo") } }
                    if (wallet.movements.isEmpty()) EmptyState("Tu próximo viaje empieza aquí", "Recarga tu billetera y estrena tu pase.", Icons.Rounded.DirectionsBus)
                    else EntryList(wallet.movements.take(3)) { receipt = it }
                    DemoLabel()
                }
                "Recargar" -> Topup(wallet.balance, onBack = { page = "Inicio" }) { amount -> result(vm.topup(amount)) }
                "Pase" -> Pass(wallet, onTopup = { page = "Recargar" }) { operator -> result(vm.travel(operator)) }
                "Actividad" -> {
                    Heading("Tu actividad", "Cada movimiento, siempre a la mano.")
                    var filter by rememberSaveable { mutableStateOf("Todos") }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { listOf("Todos", "Viajes", "Recargas").forEach { value -> FilterChip(selected = filter == value, onClick = { filter = value }, label = { Text(value) }) } }
                    val entries = wallet.movements.filter { filter == "Todos" || if (filter == "Viajes") it.cents < 0 else it.cents > 0 }
                    if (entries.isEmpty()) EmptyState("Aún no hay movimientos", "Tus viajes y recargas aparecerán aquí.", Icons.AutoMirrored.Rounded.ReceiptLong)
                    else EntryList(entries) { receipt = it }
                }
                "Perfil" -> Profile(vm) { page = "Inicio" }
            }
        }
    }
    receipt?.let { entry ->
        AlertDialog(onDismissRequest = { receipt = null }, containerColor = Color.White,
            icon = { Surface(shape = CircleShape, color = Lime) { Icon(Icons.Rounded.Check, null, Modifier.padding(16.dp).size(30.dp)) } },
            title = { Text(if (entry.cents > 0) "Recarga realizada" else "Viaje autorizado", fontWeight = FontWeight.Bold) },
            text = { Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(money(kotlin.math.abs(entry.cents)), fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Forest)
                Text(entry.title); Text(entry.subtitle, color = Muted)
                if (entry.discount > 0) Text("Ahorraste ${money(entry.discount)}", color = Forest, fontWeight = FontWeight.Bold)
                HorizontalDivider(color = Line)
                Text(date(entry.timestamp), fontSize = 12.sp)
                Text("Saldo actual: ${money(wallet.balance)}", fontWeight = FontWeight.SemiBold)
                Text("Operación ${entry.id.take(8).uppercase()} · Simulada", fontSize = 10.sp, color = Muted)
            } },
            confirmButton = { TextButton(onClick = { receipt = null }) { Text("Listo") } })
    }
    error?.let { message -> AlertDialog(onDismissRequest = { error = null }, icon = { Icon(Icons.Rounded.Info, null, tint = Coral) }, title = { Text("No se pudo completar") }, text = { Text(message) }, confirmButton = { TextButton(onClick = { error = null }) { Text("Entendido") } }) }
}

@Composable
private fun Welcome(onEnter: () -> Unit) {
    Column(Modifier.fillMaxSize().background(Forest).safeDrawingPadding().verticalScroll(rememberScrollState()).padding(28.dp), verticalArrangement = Arrangement.spacedBy(28.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) { Icon(Icons.AutoMirrored.Rounded.AltRoute, null, tint = Lime, modifier = Modifier.size(30.dp)); Text("moviwallet", fontWeight = FontWeight.Bold, fontSize = 25.sp, color = Color.White) }
        Spacer(Modifier.height(25.dp))
        Surface(shape = RoundedCornerShape(36.dp), color = Lime, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(28.dp), verticalArrangement = Arrangement.spacedBy(25.dp)) {
                Text("TU CIUDAD.\nA TU RITMO.", fontWeight = FontWeight.Black, fontSize = 40.sp, lineHeight = 44.sp, color = Forest)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.DirectionsBus, null, Modifier.size(60.dp)); Text("•  —  •", fontSize = 25.sp); Icon(Icons.Rounded.Contactless, null, Modifier.size(60.dp))
                }
                Text("Un saldo. Más conexiones.", fontSize = 17.sp, fontWeight = FontWeight.Medium)
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Muévete fácil.\nLlega más lejos.", color = Color.White, fontSize = 36.sp, lineHeight = 40.sp, fontWeight = FontWeight.Bold)
            Text("Tu billetera de transporte, tus viajes y tus transbordos. Todo en un solo lugar.", color = Color(0xFFBCD0C3), fontSize = 16.sp, lineHeight = 24.sp)
        }
        Button(onClick = onEnter, modifier = Modifier.fillMaxWidth().heightIn(min = 58.dp), colors = ButtonDefaults.buttonColors(containerColor = Lime, contentColor = Forest), shape = RoundedCornerShape(18.dp)) { Text("Explorar demo", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f)); Icon(Icons.AutoMirrored.Rounded.ArrowForward, null) }
        Text("Prototipo académico · Sin dinero real\nIngresarás con una cuenta ficticia, sin contraseña.", color = Color(0xFFBCD0C3), textAlign = TextAlign.Center, fontSize = 12.sp, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun BalanceCard(balance: Int, onTopup: () -> Unit) {
    Column(Modifier.fillMaxWidth().clip(Shape).background(Brush.linearGradient(listOf(Forest, Color(0xFF226350)))).padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) { Text("SALDO DISPONIBLE", Modifier.weight(1f), color = Color(0xFFC4DACC), fontSize = 11.sp, letterSpacing = 1.5.sp); Icon(Icons.Rounded.Contactless, null, tint = Lime, modifier = Modifier.size(28.dp)) }
        Text(money(balance), color = Color.White, fontSize = 44.sp, fontWeight = FontWeight.Bold, letterSpacing = (-1).sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) { Text("MOVIWALLET", color = Lime, letterSpacing = 2.sp, fontSize = 10.sp); Text("Cuenta demo · 0001", color = Color(0xFFC4DACC), fontSize = 12.sp) }
            FilledTonalButton(onClick = onTopup, colors = ButtonDefaults.filledTonalButtonColors(containerColor = Lime, contentColor = Forest)) { Icon(Icons.Rounded.Add, null, Modifier.size(18.dp)); Spacer(Modifier.width(4.dp)); Text("Recargar", fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
private fun ActionCard(title: String, subtitle: String, icon: ImageVector, modifier: Modifier, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = modifier, shape = Shape, color = Color.White, border = BorderStroke(1.dp, Line)) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { Icon(icon, null, tint = Forest, modifier = Modifier.size(28.dp)); Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp); Text(subtitle, color = Muted, fontSize = 11.sp) }
    }
}

@Composable
private fun Topup(balance: Int, onBack: () -> Unit, onTopup: (Int) -> Unit) {
    var amount by rememberSaveable { mutableIntStateOf(1000) }
    var confirm by rememberSaveable { mutableStateOf(false) }
    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Volver al inicio") }
    Heading("Un impulso a tu saldo", "Recarga hoy. Muévete a tu ritmo.")
    Text("Saldo actual: ${money(balance)}", color = Muted)
    Text("¿Cuánto quieres recargar?", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        listOf(500, 1000, 2000).forEach { cents ->
            Surface(onClick = { amount = cents }, modifier = Modifier.weight(1f), color = if (amount == cents) Forest else Color.White, contentColor = if (amount == cents) Color.White else Forest, shape = RoundedCornerShape(18.dp), border = BorderStroke(1.dp, if (amount == cents) Forest else Line)) {
                Text("S/ ${cents / 100}", Modifier.padding(vertical = 24.dp), textAlign = TextAlign.Center, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
    Surface(shape = Shape, color = Color.White) {
        Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            DetailRow("Método", "Recarga de demostración")
            DetailRow("Monto", money(amount))
            DetailRow("Comisión", "S/ 0.00")
            HorizontalDivider(color = Line)
            DetailRow("Nuevo saldo", money(balance + amount))
        }
    }
    PrimaryButton("Recargar ${money(amount)}", Icons.Rounded.Add) { confirm = true }
    DemoLabel("Recarga simulada. No se solicitan tarjetas ni se realizan cargos reales.")
    if (confirm) AlertDialog(onDismissRequest = { confirm = false }, title = { Text("Confirmar recarga") }, text = { Text("Se agregarán ${money(amount)} de saldo simulado a tu billetera.") }, confirmButton = { TextButton(onClick = { confirm = false; onTopup(amount) }) { Text("Confirmar") } }, dismissButton = { TextButton(onClick = { confirm = false }) { Text("Cancelar") } })
}

@Composable
private fun Pass(wallet: Wallet, onTopup: () -> Unit, onTravel: (String) -> Unit) {
    var operator by rememberSaveable { mutableStateOf("A") }
    Heading("Tu pase, siempre contigo", "Una billetera para conectar tus viajes.")
    Surface(shape = RoundedCornerShape(32.dp), color = Forest, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Text("PASE DIGITAL", color = Lime, fontSize = 11.sp, letterSpacing = 3.sp)
            Surface(shape = CircleShape, color = Color(0xFF2F5B49)) { Icon(Icons.Rounded.Contactless, null, Modifier.padding(24.dp).size(64.dp), tint = Lime) }
            Text("Listo para moverte", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("Alex · ${if (wallet.student) "Perfil estudiantil" else "Perfil regular"}", color = Color(0xFFC4DACC))
            HorizontalDivider(color = Color(0xFF3B6856))
            Text("Saldo ${money(wallet.balance)}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
        }
    }
    Text("Elige un operador de prueba", fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { listOf("A", "B").forEach { name -> FilterChip(selected = operator == name, onClick = { operator = name }, label = { Text("Operador $name") }, leadingIcon = { Icon(Icons.Rounded.DirectionsBus, null, Modifier.size(18.dp)) }) } }
    Text("Tarifa base: ${money(if (operator == "A") 200 else 250)}. Los descuentos se calculan al confirmar el viaje.", color = Muted, fontSize = 13.sp, lineHeight = 20.sp)
    PrimaryButton("Simular viaje · Operador $operator", Icons.Rounded.Contactless) { onTravel(operator) }
    TextButton(onClick = onTopup, modifier = Modifier.fillMaxWidth()) { Text("Recargar mi billetera") }
    DemoLabel("Modo demostración: este botón representa la lectura NFC. No usa el NFC del teléfono.")
}

@Composable
private fun Profile(vm: WalletViewModel, onReset: () -> Unit) {
    var resetConfirm by remember { mutableStateOf(false) }
    Heading("Tu perfil", "Todo listo para tu próximo destino.")
    Surface(shape = Shape, color = Color.White, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(18.dp)) {
            Surface(shape = CircleShape, color = Lime) { Text("AL", Modifier.padding(18.dp), fontSize = 24.sp, fontWeight = FontWeight.Bold) }
            Column { Text("Alex Luna", fontSize = 22.sp, fontWeight = FontWeight.Bold); Text("Cuenta ficticia · 0001", color = Muted, fontSize = 13.sp) }
        }
    }
    Text("Laboratorio de demostración", fontSize = 18.sp, fontWeight = FontWeight.Bold)
    Surface(shape = Shape, color = Color.White) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            ToggleRow("Perfil estudiantil", "50 % de la tarifa base. Asignación simulada de administrador.", vm.wallet.student) { vm.student(it) }
            HorizontalDivider(color = Line)
            ToggleRow("Simular falta de conexión", "Las operaciones se rechazan sin modificar el saldo.", vm.offline) { vm.offline = it }
            HorizontalDivider(color = Line)
            ToggleRow("Simular recarga rechazada", "Permite presentar el estado de error.", vm.rejectTopup) { vm.rejectTopup = it }
        }
    }
    OutlinedButton(onClick = { resetConfirm = true }, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) { Icon(Icons.Rounded.RestartAlt, null); Spacer(Modifier.width(8.dp)); Text("Reiniciar demostración") }
    TextButton(onClick = { vm.logout() }, modifier = Modifier.fillMaxWidth()) { Icon(Icons.AutoMirrored.Rounded.Logout, null); Spacer(Modifier.width(8.dp)); Text("Salir de la cuenta demo") }
    DemoLabel("MoviWallet 0.1 · Prototipo local\nSin conexión con bancos, ATU u operadores reales.")
    if (resetConfirm) AlertDialog(onDismissRequest = { resetConfirm = false }, title = { Text("¿Empezar de nuevo?") }, text = { Text("Se borrarán únicamente el saldo y los movimientos ficticios de esta demostración.") }, confirmButton = { TextButton(onClick = { vm.reset(); resetConfirm = false; onReset() }) { Text("Reiniciar") } }, dismissButton = { TextButton(onClick = { resetConfirm = false }) { Text("Cancelar") } })
}

@Composable
private fun ToggleRow(title: String, subtitle: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) { Column(Modifier.weight(1f)) { Text(title, fontWeight = FontWeight.SemiBold); Spacer(Modifier.height(4.dp)); Text(subtitle, fontSize = 12.sp, color = Muted, lineHeight = 18.sp) }; Switch(checked = checked, onCheckedChange = onChange) }
}

@Composable
private fun EntryList(entries: List<Movement>, onClick: (Movement) -> Unit) {
    Surface(shape = Shape, color = Color.White) {
        Column { entries.forEachIndexed { index, entry ->
            Row(Modifier.fillMaxWidth().clickable { onClick(entry) }.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(shape = RoundedCornerShape(14.dp), color = if (entry.cents > 0) Color(0xFFEEF6DD) else Paper) { Icon(if (entry.cents > 0) Icons.Rounded.Add else Icons.Rounded.DirectionsBus, null, Modifier.padding(12.dp).size(22.dp)) }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) { Text(entry.title, fontSize = 13.sp, fontWeight = FontWeight.Bold); Text(entry.subtitle, color = Muted, fontSize = 11.sp); Text(date(entry.timestamp), color = Muted, fontSize = 10.sp) }
                Text((if (entry.cents > 0) "+" else "−") + money(kotlin.math.abs(entry.cents)), fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
            if (index < entries.lastIndex) HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = Line)
        } }
    }
}

@Composable
private fun Heading(title: String, subtitle: String) { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { Text(title, fontSize = 30.sp, lineHeight = 35.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.7).sp); Text(subtitle, color = Muted, fontSize = 14.sp, lineHeight = 21.sp) } }
@Composable
private fun PrimaryButton(text: String, icon: ImageVector, onClick: () -> Unit) { Button(onClick = onClick, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp), shape = RoundedCornerShape(18.dp)) { Icon(icon, null, Modifier.size(22.dp)); Spacer(Modifier.width(10.dp)); Text(text, fontWeight = FontWeight.Bold) } }
@Composable
private fun DetailRow(label: String, value: String) { Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { Text(label, Modifier.weight(1f), color = Muted, fontSize = 13.sp); Text(value, fontWeight = FontWeight.SemiBold, fontSize = 13.sp) } }
@Composable
private fun DemoLabel(text: String = "Prototipo local · Saldo y operaciones simulados") { Text(text, color = Muted, fontSize = 11.sp, lineHeight = 17.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) }
@Composable
private fun EmptyState(title: String, subtitle: String, icon: ImageVector) { Column(Modifier.fillMaxWidth().padding(vertical = 20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) { Icon(icon, null, tint = Muted, modifier = Modifier.size(36.dp)); Text(title, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center); Text(subtitle, color = Muted, fontSize = 12.sp, textAlign = TextAlign.Center) } }
private fun date(timestamp: Long): String = SimpleDateFormat("dd MMM · HH:mm", Locale.forLanguageTag("es-PE")).format(Date(timestamp))
