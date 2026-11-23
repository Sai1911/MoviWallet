package pe.moviwallet.passenger.ui.screens.profile

import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.moviwallet.passenger.domain.UserProfile
import pe.moviwallet.passenger.ui.components.ChoiceChip
import pe.moviwallet.passenger.ui.components.Hairline
import pe.moviwallet.passenger.ui.components.InfoRow
import pe.moviwallet.passenger.ui.components.Pill
import pe.moviwallet.passenger.ui.components.ScreenHeader
import pe.moviwallet.passenger.ui.components.card
import pe.moviwallet.passenger.ui.theme.BrandBlue
import pe.moviwallet.passenger.ui.theme.ErrorRed
import pe.moviwallet.passenger.ui.theme.Ink
import pe.moviwallet.passenger.ui.theme.InkMuted
import pe.moviwallet.passenger.ui.theme.Success
import pe.moviwallet.passenger.ui.theme.Warning
import pe.moviwallet.passenger.viewmodel.AppViewModel
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxHeight

@Composable
fun ProfileScreen(vm: AppViewModel, onLogout: () -> Unit) {
    val state by vm.state.collectAsStateWithLifecycle()
    val user = state.user
    var confirmLogout by remember { mutableStateOf(false) }
    var confirmReset by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Perfil", onBack = null)
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(Modifier.fillMaxWidth().card().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(56.dp).background(BrandBlue.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        (user?.name ?: "U").take(1).uppercase(),
                        style = MaterialTheme.typography.titleLarge, color = BrandBlue
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(user?.name ?: "Usuario", style = MaterialTheme.typography.titleMedium, color = Ink)
                    Text(user?.email ?: "", style = MaterialTheme.typography.bodySmall, color = InkMuted)
                }
                Pill(state.profile.label, BrandBlue)
            }

            Column(Modifier.fillMaxWidth().card().padding(18.dp)) {
                Text("Tipo de perfil", style = MaterialTheme.typography.titleSmall, color = Ink)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Cambia el perfil para probar las tarifas en la demostración.",
                    style = MaterialTheme.typography.bodySmall, color = InkMuted
                )
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    UserProfile.values().forEach { p ->
                        ChoiceChip(p.label, state.profile == p, { vm.setProfile(p) })
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    if (state.profile == UserProfile.UNIVERSITARIO) "50 % de descuento sobre la tarifa base (vigencia simulada)."
                    else "Tarifa completa del operador.",
                    style = MaterialTheme.typography.bodySmall, color = InkMuted
                )
            }

            Column(Modifier.fillMaxWidth().card().padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(44.dp).background(
                            (if (state.credentialActive) Success else Warning).copy(alpha = 0.14f), CircleShape
                        ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Nfc, null, tint = if (state.credentialActive) Success else Warning)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Credencial NFC", style = MaterialTheme.typography.titleSmall, color = Ink)
                        Text(
                            if (state.credentialActive) "Activa · lista para pagar" else "Desactivada · los pagos serán rechazados",
                            style = MaterialTheme.typography.bodySmall, color = InkMuted
                        )
                    }
                    Switch(
                        checked = state.credentialActive,
                        onCheckedChange = { vm.setCredentialActive(it) },
                        colors = SwitchDefaults.colors(checkedTrackColor = Success)
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "Prototipo: el pago NFC es una simulación visual; no se emula una tarjeta real.",
                    style = MaterialTheme.typography.bodySmall, color = InkMuted
                )
            }

            Column(Modifier.fillMaxWidth().card().padding(horizontal = 18.dp, vertical = 8.dp)) {
                Text("Configuración", style = MaterialTheme.typography.titleSmall, color = Ink, modifier = Modifier.padding(top = 10.dp))
                SettingSwitch(Icons.Filled.Notifications, "Notificaciones (demo)", "Preferencia visual. No envía avisos reales.", state.pushEnabled) { vm.setPush(it) }
                Hairline()
                SettingSwitch(Icons.Filled.Fingerprint, "Biometría (demo)", "Preferencia visual. No activa el lector de huella.", state.biometricsEnabled) { vm.setBiometrics(it) }
                Hairline()
                InfoRow("DNI", user?.dni?.let { maskDni(it) } ?: "-")
                InfoRow("Versión", "Prototipo local 0.2")
            }

            Row(
                Modifier.fillMaxWidth().card().clickable { confirmReset = true }.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Refresh, null, tint = BrandBlue)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Reiniciar demostración", style = MaterialTheme.typography.titleSmall, color = Ink)
                    Text("Saldo en S/ 0.00 y sin historial", style = MaterialTheme.typography.bodySmall, color = InkMuted)
                }
            }

            Row(
                Modifier.fillMaxWidth().card().clickable { confirmLogout = true }.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, null, tint = ErrorRed)
                Spacer(Modifier.width(12.dp))
                Text("Cerrar sesión", style = MaterialTheme.typography.titleSmall, color = ErrorRed)
            }
            Spacer(Modifier.height(8.dp))
        }
    }

    if (confirmLogout) {
        AlertDialog(
            onDismissRequest = { confirmLogout = false },
            title = { Text("Cerrar sesión") },
            text = { Text("Tu saldo e historial se conservarán en este dispositivo. Puedes volver a la misma cuenta demo.") },
            confirmButton = {
                TextButton(onClick = { confirmLogout = false; onLogout() }) { Text("Cerrar sesión", color = ErrorRed) }
            },
            dismissButton = { TextButton(onClick = { confirmLogout = false }) { Text("Cancelar") } }
        )
    }
    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text("Reiniciar demostración") },
            text = { Text("El saldo volverá a S/ 0.00 y se borrará el historial y la ventana de transbordo.") },
            confirmButton = {
                TextButton(onClick = { confirmReset = false; vm.resetDemo() }) { Text("Reiniciar") }
            },
            dismissButton = { TextButton(onClick = { confirmReset = false }) { Text("Cancelar") } }
        )
    }
}

private fun maskDni(dni: String): String =
    if (dni.length >= 8) "••••" + dni.takeLast(4) else dni

@Composable
private fun SettingSwitch(icon: ImageVector, title: String, subtitle: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = InkMuted)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = Ink)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = InkMuted)
        }
        Switch(checked = checked, onCheckedChange = onChange, colors = SwitchDefaults.colors(checkedTrackColor = BrandBlue))
    }
}
