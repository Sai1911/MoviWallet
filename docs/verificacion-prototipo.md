# Verificación del prototipo local

## Versión 0.2 — 22 de noviembre de 2026

- APK de depuración compilado con `:app:assembleDebug`.
- Cinco pruebas JUnit de tarifas aprobadas: viaje y transbordo regular, perfil universitario, vencimiento de ventana, restricciones del descuento y límite de lectura repetida.
- `:app:lintDebug` completado sin errores; quedan advertencias no bloqueantes de dependencias y APIs obsoletas.
- Seis pruebas instrumentadas aprobadas en Medium_Phone (Android 17): recorrido visual de recarga, viajes A/B, comprobante, reapertura y lectura repetida; persistencia del perfil universitario; errores de recarga; errores de pago; cuentas separadas/cierre de sesión/reinicio; migración de datos antiguos sin repetirse.
- Los comandos `local.ps1 test` y `local.ps1 ui-test` completaron correctamente.
- Las pruebas instrumentadas usan una aplicación separada (`pe.moviwallet.passenger.qa`) y solo borran sus propios datos.
- La captura visual mediante la herramienta de escritorio no pudo completarse: el emulador devolvió un error de recorte fuera del monitor. No se considera validado el aspecto visual en esta revisión.

Los reportes generados están en `apps/passenger/app/build/reports/`. El APK está en `apps/passenger/app/build/outputs/apk/debug/app-debug.apk`. No se versionan estos archivos generados.

## Alcance

Las verificaciones corresponden a la demo local. No validan NFC real, autenticación, una API financiera ni todos los dispositivos Android. Queda revisar visualmente las pantallas, tamaños de texto ampliados y el guion de presentación con el equipo.

La versión anterior fue verificada el 10 de noviembre con cuatro pruebas unitarias y una de interfaz; la suite actual sustituye esa evidencia para el código integrado.
