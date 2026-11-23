# MoviWallet · app del pasajero

Demo Android local con Kotlin y Jetpack Compose. La versión 0.2 integra las pantallas de `MoviWallet.zip` con la persistencia y las verificaciones del proyecto original. No necesita hosting ni servidor.

## Ejecutar en Windows

Abrir esta carpeta en Android Studio; instalar SDK 36 y usar su JDK integrado. Encender un emulador o conectar un teléfono autorizado (Android 8.0 o superior).

```powershell
.\local.ps1 run
```

El script compila, instala sobre la versión anterior y abre la app. No desinstales la aplicación si quieres conservar sus datos. Para compilar solamente: `./local.ps1 build`. APK: `app/build/outputs/apk/debug/app-debug.apk`.

## Verificaciones automáticas

```powershell
.\local.ps1 test
.\local.ps1 ui-test
```

`test` ejecuta JUnit y lint. `ui-test` necesita un emulador y ejecuta el recorrido visual y las pruebas de persistencia y errores. Estas pruebas usan **MoviWallet Pruebas** (`pe.moviwallet.passenger.qa`) y reinician únicamente sus datos, sin borrar la billetera habitual.

Tareas equivalentes de Gradle: `:app:assembleDebug`, `:app:testQaUnitTest`, `:app:lintDebug`, `:app:connectedQaAndroidTest`. En Android Studio elegir la variante `debug` para uso habitual y `qa` para instrumentación.

## Recorrido para corroborar el funcionamiento

1. Entrar con **Explorar con cuenta demo**. Si ya había datos, se restauran; para empezar en cero usar **Perfil → Reiniciar demostración**.
2. Con perfil normal, recargar S/ 10 y confirmar. Debe aparecer **Recarga exitosa** y saldo S/ 10.
3. En **Pagar**, elegir Operador A y acercar el teléfono mediante el botón simulado. Resultado: S/ 8.
4. Pagar con Operador B dentro de 120 minutos: descuento de S/ 0.50, cobro S/ 2 y saldo S/ 6.
5. Abrir el comprobante y la billetera: revisar movimientos, descuentos y saldo después de cada operación.
6. Repetir inmediatamente el último operador: se rechaza durante 30 segundos y no cambia el saldo.
7. Cerrar y abrir la app: deben conservarse sesión, saldo, historial, perfil y ventana de transbordo. Cerrar sesión y entrar con la misma cuenta también conserva la billetera.
8. Reiniciar la demo, elegir perfil universitario y repetir recarga y viajes A/B: saldo final S/ 8.25.
9. En los controles de escenario de recarga y pago, probar rechazo, timeout, sin conexión, credencial inválida y saldo insuficiente. No deben generar movimientos ni modificar saldo.
10. En transbordos, avanzar el reloj de demostración para verificar el vencimiento. El descuento se aplica una sola vez por ventana y vence a los 120 minutos.

## Datos y límites

- Cada correo ficticio tiene una billetera local separada. Login, registro y recuperación son demostrativos: no verifican identidad ni guardan contraseñas.
- La cuenta anterior se migra una sola vez a `demo@moviwallet.pe`, conservando saldo, movimientos, perfil y estado de transbordo. Las preferencias originales permanecen intactas.
- Reiniciar demo borra la billetera activa; cerrar sesión la conserva. Desinstalar borra los datos locales.
- NFC, recargas y errores de conectividad son simulados. No se envía dinero ni existe conexión con la ATU.
- Biometría y notificaciones son preferencias visuales, sin funciones reales asociadas.
- El almacenamiento local sirve para demostraciones; no implementa seguridad ni garantías financieras de producción.
- Backend, lector NFC, panel administrativo y autenticación real siguen pendientes.

Versiones: AGP 9.3.1, Gradle 9.5.0, compilador Compose 2.2.10, Compose BOM 2025.08.01 y SDK 36. La primera compilación requiere conexión para descargar dependencias.
