package pe.moviwallet.passenger.domain

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

fun Int.soles(): String {
    val a = abs(this)
    val s = "S/ ${a / 100}.${(a % 100).toString().padStart(2, '0')}"
    return if (this < 0) "-$s" else s
}

fun Int.solesSigned(positive: Boolean): String = (if (positive) "+" else "-") + abs(this).soles()

private val esPe = Locale("es", "PE")

fun formatDateTime(millis: Long): String =
    SimpleDateFormat("d MMM yyyy · HH:mm", esPe).format(Date(millis))

fun formatShortDate(millis: Long): String =
    SimpleDateFormat("d MMM · HH:mm", esPe).format(Date(millis))

fun formatClock(millis: Long): String =
    SimpleDateFormat("HH:mm", esPe).format(Date(millis))

fun formatHms(ms: Long): String {
    val total = ms / 1000
    return String.format(Locale.US, "%d:%02d:%02d", total / 3600, (total % 3600) / 60, total % 60)
}

fun formatRemaining(ms: Long): String {
    val totalMin = ms / 60_000
    val h = totalMin / 60
    val m = totalMin % 60
    return if (h > 0) "$h h $m min" else "$m min"
}
