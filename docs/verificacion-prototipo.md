# Verificación del prototipo local

Fecha: 10 de noviembre de 2026.

## Resultados

- APK de depuración compilado con `:app:assembleDebug`.
- Cuatro pruebas JUnit aprobadas: viaje y transbordo regular, perfil estudiantil, vencimiento exacto y conservación de ventana, rechazo por saldo insuficiente y lectura repetida.
- Una prueba de interfaz aprobada en `Medium_Phone`, Android 17: entrada a demo, recarga S/ 10, viaje A, transbordo B, comprobantes, historial y reapertura con saldo S/ 6.
- `:app:lintDebug` completado sin errores. Mantiene advertencias de actualización de dependencias y configuración que no bloquean el prototipo.
- Pantalla de inicio inspeccionada visualmente en el emulador. La ventana se reubicó a una posición visible y se redujo a escala 0.25 en este equipo.
- Script `local.ps1` verificado con el analizador de sintaxis de PowerShell.

Los reportes generados están en `apps/passenger/app/build/reports/` y no se versionan. El APK está en `apps/passenger/app/build/outputs/apk/debug/app-debug.apk`.

## Alcance de la evidencia

Estas verificaciones corresponden a una demo local. No validan NFC real, autenticación, una API financiera ni todos los dispositivos Android. Falta revisar con el equipo el diseño de todas las pantallas, tamaños de texto ampliados y el guion definitivo de presentación.

La prueba de interfaz usa Espresso 3.7.0 para evitar la reflexión antigua de `InputManager`, corregida por AndroidX en esa versión: [notas oficiales de AndroidX Test](https://developer.android.com/jetpack/androidx/releases/test#espresso-3.7.0).
