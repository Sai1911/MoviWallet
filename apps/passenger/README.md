# MoviWallet · app del pasajero

Prototipo Android nativo con Kotlin y Jetpack Compose. Funciona sin servidor y sin hosting. Usa una cuenta ficticia (Alex Luna) y almacena el saldo y movimientos en las preferencias privadas de la app. No solicita permisos de internet ni NFC.

## Abrir y ejecutar

1. En Android Studio, abrir esta carpeta (`apps/passenger`), no la raíz del monorepositorio.
2. Usar el JDK integrado de Android Studio para Gradle y tener instalado Android SDK 36.
3. Sincronizar Gradle. La primera sincronización necesita internet para las dependencias; la demo funciona sin conexión después.
4. Elegir un emulador o teléfono con Android 8.0 (API 26) o superior y pulsar Run en el módulo `app`.

Versiones fijadas: AGP 9.3.1, Gradle 9.5.0, compilador Compose 2.2.10 (alineado con el Kotlin integrado de AGP), Compose BOM 2025.08.01. Se usa el SDK 36 instalado en el entorno de desarrollo.

Por terminal, configurar `JAVA_HOME` con un JDK compatible (el integrado de Android Studio) y `ANDROID_HOME` con la carpeta del SDK, o dejar que Android Studio genere `local.properties`:

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

APK generado: `app/build/outputs/apk/debug/app-debug.apk`.

En Windows también se puede usar `./local.ps1 run` desde esta carpeta con un emulador abierto o un teléfono autorizado. El script busca el JDK de Android Studio y el SDK local, compila, instala y abre la app. `./local.ps1 build` solo compila y `./local.ps1 test` ejecuta pruebas unitarias y lint. No inicia ni configura dispositivos automáticamente.

Con un emulador encendido, la prueba de interfaz se ejecuta con `./gradlew.bat :app:connectedDebugAndroidTest`. Esta prueba reinicia los datos ficticios de la aplicación y comprueba recarga, viaje, transbordo, historial y persistencia.

## Guion breve de presentación

1. Pulsar **Explorar demo**. La billetera inicia en S/ 0.
2. Recargar S/ 10 y confirmar: saldo S/ 10.
3. Abrir **Pase**, elegir **Operador A** y simular el viaje: saldo S/ 8.
4. Elegir **Operador B** y simular el viaje dentro de 120 minutos: descuento de S/ 0.50 y saldo S/ 6.
5. Abrir **Actividad**, filtrar viajes o recargas y tocar un movimiento para ver su comprobante.
6. En **Perfil**, probar falta de conexión o recarga rechazada. El saldo permanece igual al fallar.
7. Reiniciar la demostración desde Perfil, activar el perfil estudiantil y repetir la recarga y los viajes: saldo final S/ 8.25.

Una segunda lectura consecutiva del mismo operador antes de 30 segundos se rechaza. Solo se permite un descuento de transbordo por ventana. La ventana empieza con el primer viaje y vence exactamente a los 120 minutos.

## Límites de esta versión

- Ingreso directo a una cuenta demo; registro y autenticación real pendientes.
- El botón de viaje simula NFC. No hay lectura ni firma criptográfica implementadas.
- Las opciones del perfil son controles de presentación, no permisos administrativos reales.
- Persistencia local para la demo; no es una base financiera ni implementa garantías transaccionales del backend.
- El modo sin conexión se simula explícitamente con un interruptor; la app no depende de la conectividad real.
- El historial persiste al reiniciar la app, pero se borra al reinstalar o reiniciar la demo. Los interruptores de error se restablecen al iniciar un proceso nuevo.

Las reglas de demostración se prueban con JUnit. La API, la autenticación real, la consistencia financiera y el lector NFC se incorporarán en etapas posteriores.
