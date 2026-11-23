package pe.moviwallet.passenger.ui.screens.auth

import android.util.Patterns
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import pe.moviwallet.passenger.domain.UserProfile
import pe.moviwallet.passenger.ui.components.MoviTextField
import pe.moviwallet.passenger.ui.components.PrimaryButton
import pe.moviwallet.passenger.ui.components.ScreenHeader
import pe.moviwallet.passenger.ui.components.SecondaryButton
import pe.moviwallet.passenger.ui.components.card
import pe.moviwallet.passenger.ui.theme.BrandBlue
import pe.moviwallet.passenger.ui.theme.Ink
import pe.moviwallet.passenger.ui.theme.InkMuted
import pe.moviwallet.passenger.ui.theme.Mint
import pe.moviwallet.passenger.ui.theme.Navy
import pe.moviwallet.passenger.ui.theme.NavyDeep
import pe.moviwallet.passenger.ui.theme.Success

private fun validEmail(e: String) = Patterns.EMAIL_ADDRESS.matcher(e.trim()).matches()

@Composable
fun LogoMark(size: Int = 64) {
    Box(
        Modifier.size(size.dp).background(Brush.linearGradient(listOf(Mint, BrandBlue)), RoundedCornerShape((size * 0.3f).dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Filled.Nfc, null, tint = Color.White, modifier = Modifier.size((size * 0.5f).dp))
    }
}

@Composable
fun WelcomeScreen(onLogin: () -> Unit, onRegister: () -> Unit, onDemo: () -> Unit) {
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Navy, NavyDeep, BrandBlue)))) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(Color.White.copy(alpha = 0.06f), radius = size.width * 0.7f, center = Offset(size.width, size.height * 0.12f))
            drawCircle(Mint.copy(alpha = 0.14f), radius = size.width * 0.42f, center = Offset(0f, size.height * 0.6f))
        }
        Column(
            Modifier.fillMaxSize().padding(horizontal = 28.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                LogoMark(48)
                Spacer(Modifier.width(12.dp))
                Text("MoviWallet", color = Color.White, style = MaterialTheme.typography.titleLarge)
            }
            Column {
                Text(
                    "Un solo saldo.\nTodo el transporte.",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineLarge,
                    fontSize = 36.sp,
                    lineHeight = 42.sp
                )
                Spacer(Modifier.height(14.dp))
                Text(
                    "Recarga, paga tus viajes con un toque y disfruta de descuentos por transbordo entre operadores.",
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(Modifier.height(24.dp))
                WelcomeFeature(Icons.Filled.Nfc, "Pago por aproximación (simulado)")
                WelcomeFeature(Icons.Filled.SwapHoriz, "S/ 0.50 de descuento por transbordo")
                WelcomeFeature(Icons.Filled.School, "Tarifa universitaria al 50 %")
            }
            Column {
                Button(
                    onClick = onRegister,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Navy)
                ) { Text("Crear cuenta", style = MaterialTheme.typography.labelLarge, fontSize = 16.sp) }
                Spacer(Modifier.height(10.dp))
                OutlinedButton(
                    onClick = onLogin,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) { Text("Iniciar sesión", style = MaterialTheme.typography.labelLarge, fontSize = 16.sp) }
                TextButton(onClick = onDemo, modifier = Modifier.fillMaxWidth()) {
                    Text("Explorar con cuenta demo", color = Color.White.copy(alpha = 0.9f))
                }
            }
        }
    }
}

@Composable
private fun WelcomeFeature(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(Modifier.padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(34.dp).background(Color.White.copy(alpha = 0.14f), CircleShape),
            contentAlignment = Alignment.Center
        ) { Icon(icon, null, tint = Color.White, modifier = Modifier.size(18.dp)) }
        Spacer(Modifier.width(12.dp))
        Text(text, color = Color.White, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun LoginScreen(
    onBack: () -> Unit,
    onLogin: (String) -> Unit,
    onDemo: () -> Unit,
    onForgot: () -> Unit,
    onRegister: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }
    val emailError = if (submitted && !validEmail(email)) "Ingresa un correo válido" else null
    val passError = if (submitted && password.length < 6) "Mínimo 6 caracteres" else null

    Column(Modifier.fillMaxSize().imePadding()) {
        ScreenHeader("", onBack)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp)) {
            LogoMark(56)
            Spacer(Modifier.height(20.dp))
            Text("Bienvenido de vuelta", style = MaterialTheme.typography.headlineMedium, color = Ink)
            Spacer(Modifier.height(6.dp))
            Text("Acceso simulado. Usa datos ficticios: la contraseña no se verifica ni se guarda. Cada correo identifica una billetera demo local.", style = MaterialTheme.typography.bodyLarge, color = InkMuted)
            Spacer(Modifier.height(28.dp))
            MoviTextField(email, { email = it }, "Correo electrónico", Icons.Filled.Email, keyboardType = KeyboardType.Email, error = emailError)
            Spacer(Modifier.height(8.dp))
            MoviTextField(password, { password = it }, "Contraseña", Icons.Filled.Lock, isPassword = true, keyboardType = KeyboardType.Password, error = passError, imeAction = ImeAction.Done)
            TextButton(onClick = onForgot, modifier = Modifier.align(Alignment.End)) {
                Text("¿Olvidaste tu contraseña?", color = BrandBlue)
            }
            Spacer(Modifier.height(8.dp))
            PrimaryButton("Iniciar sesión", onClick = {
                submitted = true
                if (validEmail(email) && password.length >= 6) onLogin(email.trim())
            })
            Spacer(Modifier.height(12.dp))
            SecondaryButton("Entrar con cuenta demo", onDemo)
            Spacer(Modifier.height(20.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Text("¿No tienes cuenta?", color = InkMuted, style = MaterialTheme.typography.bodyMedium)
                TextButton(onClick = onRegister) { Text("Regístrate", color = BrandBlue) }
            }
        }
    }
}

@Composable
fun RegisterScreen(onBack: () -> Unit, onRegister: (name: String, dni: String, email: String, profile: UserProfile) -> Unit, onLogin: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var dni by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var profile by remember { mutableStateOf(UserProfile.NORMAL) }
    var submitted by remember { mutableStateOf(false) }

    val nameError = if (submitted && name.trim().length < 3) "Ingresa tu nombre completo" else null
    val dniError = if (submitted && !(dni.length == 8 && dni.all { it.isDigit() })) "El DNI tiene 8 dígitos" else null
    val emailError = if (submitted && !validEmail(email)) "Ingresa un correo válido" else null
    val passError = if (submitted && password.length < 6) "Mínimo 6 caracteres" else null

    Column(Modifier.fillMaxSize().imePadding()) {
        ScreenHeader("Crear cuenta", onBack)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp)) {
            Text("Regístrate en un minuto", style = MaterialTheme.typography.headlineSmall, color = Ink)
            Spacer(Modifier.height(4.dp))
            Text("Registro de demostración. Usa nombre, DNI, correo y contraseña ficticios. No hay autenticación real; la contraseña no se guarda.", style = MaterialTheme.typography.bodyMedium, color = InkMuted)
            Spacer(Modifier.height(20.dp))
            MoviTextField(name, { name = it }, "Nombres y apellidos", Icons.Filled.Person, error = nameError)
            Spacer(Modifier.height(6.dp))
            MoviTextField(dni, { if (it.length <= 8) dni = it.filter { c -> c.isDigit() } }, "DNI", Icons.Filled.Badge, keyboardType = KeyboardType.Number, error = dniError)
            Spacer(Modifier.height(6.dp))
            MoviTextField(email, { email = it }, "Correo electrónico", Icons.Filled.Email, keyboardType = KeyboardType.Email, error = emailError)
            Spacer(Modifier.height(6.dp))
            MoviTextField(password, { password = it }, "Contraseña", Icons.Filled.Lock, isPassword = true, keyboardType = KeyboardType.Password, error = passError, imeAction = ImeAction.Done)
            Spacer(Modifier.height(14.dp))
            Text("Tipo de perfil", style = MaterialTheme.typography.titleSmall, color = Ink)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ProfileOption("Regular", "Tarifa completa", UserProfile.NORMAL == profile, { profile = UserProfile.NORMAL }, Modifier.weight(1f))
                ProfileOption("Universitario", "50 % de descuento", UserProfile.UNIVERSITARIO == profile, { profile = UserProfile.UNIVERSITARIO }, Modifier.weight(1f))
            }
            if (profile == UserProfile.UNIVERSITARIO) {
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Verified, null, tint = Success, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Perfil universitario de prueba. No se verifica un carné real.", style = MaterialTheme.typography.bodySmall, color = InkMuted)
                }
            }
            Spacer(Modifier.height(20.dp))
            PrimaryButton("Crear cuenta", onClick = {
                submitted = true
                val ok = name.trim().length >= 3 && dni.length == 8 && validEmail(email) && password.length >= 6
                if (ok) onRegister(name, dni, email, profile)
            })
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Text("¿Ya tienes cuenta?", color = InkMuted, style = MaterialTheme.typography.bodyMedium)
                TextButton(onClick = onLogin) { Text("Inicia sesión", color = BrandBlue) }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ProfileOption(title: String, subtitle: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier
            .clip(shape)
            .background(if (selected) BrandBlue.copy(alpha = 0.08f) else Color.White, shape)
            .border(if (selected) 2.dp else 1.dp, if (selected) BrandBlue else pe.moviwallet.passenger.ui.theme.Outline, shape)
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Text(title, style = MaterialTheme.typography.titleSmall, color = if (selected) BrandBlue else Ink)
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = InkMuted)
    }
}

@Composable
fun RecoverScreen(onBack: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }
    var sent by remember { mutableStateOf(false) }
    val emailError = if (submitted && !validEmail(email)) "Ingresa un correo válido" else null

    Column(Modifier.fillMaxSize().imePadding()) {
        ScreenHeader("Recuperar contraseña", onBack)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp)) {
            Spacer(Modifier.height(8.dp))
            Text("¿Olvidaste tu contraseña?", style = MaterialTheme.typography.headlineSmall, color = Ink)
            Spacer(Modifier.height(6.dp))
            Text(
                "Ingresa tu correo y te enviaremos un enlace para crear una nueva contraseña.",
                style = MaterialTheme.typography.bodyLarge, color = InkMuted
            )
            Spacer(Modifier.height(24.dp))
            if (!sent) {
                MoviTextField(email, { email = it }, "Correo electrónico", Icons.Filled.Email, keyboardType = KeyboardType.Email, error = emailError, imeAction = ImeAction.Done)
                Spacer(Modifier.height(20.dp))
                PrimaryButton("Enviar enlace", onClick = {
                    submitted = true
                    if (validEmail(email)) sent = true
                })
            } else {
                Column(Modifier.fillMaxWidth().card().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.CheckCircle, null, tint = Success, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(10.dp))
                    Text("Enlace enviado", style = MaterialTheme.typography.titleMedium, color = Ink)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Simulamos el envío a ${email.trim()}. En el prototipo no se envía ningún correo real.",
                        style = MaterialTheme.typography.bodyMedium, color = InkMuted, textAlign = TextAlign.Center
                    )
                }
                Spacer(Modifier.height(20.dp))
                PrimaryButton("Volver a iniciar sesión", onBack)
            }
        }
    }
}
