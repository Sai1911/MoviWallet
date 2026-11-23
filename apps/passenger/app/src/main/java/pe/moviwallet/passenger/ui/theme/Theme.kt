package pe.moviwallet.passenger.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Train
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import pe.moviwallet.passenger.domain.Operator

private val LightColors = lightColorScheme(
    primary = BrandBlue,
    onPrimary = Color.White,
    secondary = Mint,
    onSecondary = Color.White,
    background = AppBackground,
    onBackground = Ink,
    surface = SurfaceWhite,
    onSurface = Ink,
    surfaceVariant = Color(0xFFEDF1FA),
    onSurfaceVariant = InkMuted,
    outline = Outline,
    error = ErrorRed
)

private val AppShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp)
)

@Composable
fun MoviWalletTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = LightColors, typography = AppTypography, shapes = AppShapes, content = content)
}

fun Operator.color(): Color = when (this) {
    Operator.A -> OperatorAColor
    Operator.B -> OperatorBColor
}

fun Operator.icon(): ImageVector = when (this) {
    Operator.A -> Icons.Filled.Train
    Operator.B -> Icons.Filled.DirectionsBus
}
