# MoviWallet

Prototipo académico de una billetera para transporte público: saldo simulado, recargas, viajes con distintos operadores, descuentos por transbordo e historial.

## Estado actual

Proyecto de un equipo de cinco integrantes. La primera aplicación Android local está en `apps/passenger`, construida con Kotlin y Jetpack Compose.

**Versión local 0.2: integración del prototipo ampliado (22 de noviembre de 2026).** La app permite explorar una cuenta ficticia, recargar, simular viajes y transbordos, consultar movimientos y cambiar escenarios de prueba. Incluye login y registro simulados, operadores, transbordos, comprobantes y escenarios de error. El saldo y el historial se conservan por cuenta local al cerrar la app o cerrar sesión. Se migran los datos de la versión anterior.

El prototipo visual no acredita una integración NFC funcional. El PMV posterior contempla NFC real entre dos dispositivos Android, un backend y un panel administrativo.

## Estructura

| Carpeta | Propósito previsto |
|---|---|
| `apps/passenger` | Aplicación Android del pasajero |
| `apps/reader` | Lector NFC de laboratorio en Android |
| `apps/admin` | Panel administrativo |
| `services/api` | Backend modular y reglas de negocio |
| `docs` | Plan, alcance y documentación del prototipo |

La arquitectura del plan propone Kotlin y Jetpack Compose para Android, NestJS para la API, PostgreSQL para persistencia y Next.js para el panel. Solo la app del pasajero está implementada como prototipo; lector, API y panel siguen pendientes. No se requiere hosting.

## Documentación

- [Plan de referencia del PMV y equipo de cinco integrantes](docs/plan-pmv.md).
- [Alcance inicial del prototipo de alta fidelidad](docs/prototipo-alta-fidelidad.md).
- [Guía de colaboración](CONTRIBUTING.md).
- [Ejecutar la app Android y presentar la demo](apps/passenger/README.md).

## Empezar

```sh
git clone https://github.com/Sai1911/MoviWallet.git
cd MoviWallet
```

Abrir `apps/passenger` como proyecto en Android Studio, sincronizar Gradle y ejecutar la configuración `app` en un emulador o teléfono Android 8.0 o superior. Consultar la guía de la aplicación para compilar por terminal.

## Datos de demostración

No se procesa dinero real ni existe conexión oficial con operadores, bancos o la ATU. Las tarifas y los perfiles estudiantiles del plan son ejemplos académicos. No subir contraseñas, archivos de entorno, claves de firma ni datos personales reales.
