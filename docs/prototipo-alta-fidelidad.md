# Prototipo de alta fidelidad

## Hito inmediato

Presentación prevista: lunes 16 de noviembre de 2026. Se inicia el trabajo anticipadamente; la participación y asignación de tareas de los cinco integrantes se coordinará después.

Este documento propone el recorte visual inicial a partir del plan del PMV. No representa funcionalidad ya implementada ni cambia el alcance del PMV completo.

## Recorrido principal propuesto

1. Ingresar a una cuenta de demostración.
2. Ver el inicio con saldo y accesos a recarga, pase e historial.
3. Elegir una recarga simulada de S/ 10 y ver su confirmación.
4. Abrir el pase y representar un viaje con el operador A por S/ 2.
5. Mostrar saldo de S/ 8 y el comprobante del viaje.
6. Representar un transbordo al operador B dentro de 120 minutos: tarifa de S/ 2.50 menos S/ 0.50 de descuento.
7. Mostrar saldo final de S/ 6 e historial de ambos viajes.

## Pantallas propuestas

- Bienvenida e ingreso; registro si se incluye en el recorrido de presentación.
- Inicio y billetera.
- Selección de monto y confirmación de recarga.
- Pase NFC y resultado del viaje, identificando la simulación visual.
- Historial y detalle de movimiento.
- Perfil con estado estudiantil de demostración.

Incluir estados de saldo insuficiente, recarga rechazada y conexión no disponible. Evitar datos personales reales.

## Criterios de revisión

- El recorrido principal puede completarse sin pantallas sin salida.
- Importes, saldo e historial coinciden entre pantallas.
- Tipografía, colores, espaciado y componentes siguen un mismo sistema visual.
- Los botones principales son legibles y fáciles de identificar.
- La presentación distingue el prototipo visual de las funciones pendientes del PMV.

## Primera implementación local

Se eligió una app Android nativa con Kotlin y Jetpack Compose en `apps/passenger`. Incluye bienvenida con cuenta demo, inicio, recarga con confirmación, pase simulado, historial con filtros y detalles, y perfil con controles de demostración. Identidad visual inicial: verde petróleo, lima y fondo claro.

El registro y login reales no forman parte de esta primera versión. Los datos se guardan en el dispositivo. Consultar la [guía de ejecución](../apps/passenger/README.md).

## Decisiones pendientes

- Validar la identidad visual inicial y ajustar el recorrido según la revisión del equipo.
- Inclusión de vistas del lector o del panel en esta primera presentación.

El plan original se conserva en [plan-pmv.md](plan-pmv.md) como referencia; sus tareas y asignaciones no implican que ya estén ejecutadas o aprobadas por el equipo.

## Integración de la versión 0.2 (22 de noviembre de 2026)

Se incorporaron desde el ZIP las pantallas y componentes Compose de bienvenida, login, registro, recuperación, inicio, billetera, operadores, perfil, recarga, pago, transbordos y comprobantes. Se mantuvieron la configuración Android, el identificador de aplicación y las herramientas de ejecución del repositorio.

Se agregó persistencia por cuenta demo y migración de datos de la versión anterior. Se conservó la protección ante lectura repetida, se añadió el escenario de recarga sin conexión y se separó la aplicación de pruebas para no borrar los datos de la demo habitual. No se importaron cachés ni carpetas de compilación del ZIP.

Login, recuperación y NFC siguen siendo simulaciones. El alcance completo del PMV continúa pendiente. La guía de ejecución contiene el recorrido actualizado y los comandos de prueba.
