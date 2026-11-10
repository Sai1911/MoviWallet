# MoviWallet

Prototipo académico de una billetera para transporte público: saldo simulado, recargas, viajes con distintos operadores, descuentos por transbordo e historial.

## Estado actual

Repositorio inicial de un equipo de cinco integrantes. Aún no contiene aplicaciones ejecutables.

**Primer hito: prototipo de alta fidelidad para el lunes 16 de noviembre de 2026.** La prioridad es definir las pantallas y demostrar el recorrido del pasajero con datos ficticios. El formato del prototipo y su herramienta de implementación están pendientes de definir.

El prototipo visual no acredita una integración NFC funcional. El PMV posterior contempla NFC real entre dos dispositivos Android, un backend y un panel administrativo.

## Estructura

| Carpeta | Propósito previsto |
|---|---|
| `apps/passenger` | Aplicación Android del pasajero |
| `apps/reader` | Lector NFC de laboratorio en Android |
| `apps/admin` | Panel administrativo |
| `services/api` | Backend modular y reglas de negocio |
| `docs` | Plan, alcance y documentación del prototipo |

La arquitectura del plan propone Kotlin y Jetpack Compose para Android, NestJS para la API, PostgreSQL para persistencia y Next.js para el panel. No se han instalado dependencias ni generado estos proyectos todavía.

## Documentación

- [Plan de referencia del PMV y equipo de cinco integrantes](docs/plan-pmv.md).
- [Alcance inicial del prototipo de alta fidelidad](docs/prototipo-alta-fidelidad.md).
- [Guía de colaboración](CONTRIBUTING.md).

## Empezar

```sh
git clone https://github.com/Sai1911/MoviWallet.git
cd MoviWallet
```

Los comandos de instalación y ejecución se incorporarán cuando se cree la primera aplicación.

## Datos de demostración

No se procesa dinero real ni existe conexión oficial con operadores, bancos o la ATU. Las tarifas y los perfiles estudiantiles del plan son ejemplos académicos. No subir contraseñas, archivos de entorno, claves de firma ni datos personales reales.
