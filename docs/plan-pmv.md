# MoviWallet: producto mínimo viable y plan de ejecución

## 1. Objetivo y supuestos

Construir un prototipo académico Android que permita usar una misma billetera de saldo simulado para pagar viajes de distintos operadores mediante NFC, aplicando tarifas y descuentos por transbordo y mostrando un historial verificable.

Base: 2doEntregable_MoviWallet_DAM_G2.pdf, especialmente objetivos, alcance, limitaciones y fases técnicas. Este plan propone un recorte del documento; no afirma que ya exista implementación. Las tarifas, operadores y reglas son datos de demostración, sin conexión oficial con la ATU o concesionarias.

Supuestos de planificación: cinco integrantes, 10 semanas, 10–12 horas semanales por persona, aproximadamente 500–600 horas de equipo. Es una estimación inicial que se revisará al cerrar la semana 2. Si disponen de menos horas o experiencia inicial, ampliar el calendario. No incluye despliegue comercial, dinero real ni publicación en Google Play.

## 2. Resultado que debe poder demostrarse

Un usuario se registra, recarga S/ 10 simulados y acerca su teléfono a un segundo Android que actúa como lector. El backend valida la credencial, registra el viaje y descuenta el importe. Al viajar con otro operador dentro de la ventana configurada, aplica el descuento de transbordo. La app y el panel muestran el saldo y las operaciones. Repetir una solicitud no produce un segundo cobro.

La lectura NFC es real; el banco, los operadores, la elegibilidad universitaria y el torniquete son simulados. El lector necesita internet para autorizar el viaje. El teléfono del pasajero debe tener una credencial previamente obtenida; consultar el saldo actualizado necesita conexión. No se promete pago offline ni autorización de extremo a extremo inferior a 150 ms.

## 3. Alcance cerrado del PMV

| ID | Entregable | Criterio de aceptación |
|---|---|---|
| PMV-01 | Registro, login y sesión | Usuario con correo y contraseña puede ingresar; rutas privadas rechazan sesiones inválidas; no se requiere DNI real. |
| PMV-02 | Billetera e historial | Saldo inicial cero, importes en céntimos enteros e historial de recargas y viajes; el cliente no modifica el saldo directamente. |
| PMV-03 | Recarga simulada | Permite éxito, rechazo y timeout; repetir la misma clave de idempotencia no vuelve a acreditar. |
| PMV-04 | Operadores y tarifas | Dos operadores de prueba configurados como datos dentro de un backend; tarifa resuelta en servidor. |
| PMV-05 | Transbordo | Ventana de 120 minutos desde el primer viaje pagado, un descuento máximo por ventana y cambio obligatorio de operador. |
| PMV-06 | Medio pasaje simulado | Un administrador asigna o revoca un perfil de demostración; no se consultan entidades oficiales. |
| PMV-07 | Credencial NFC | Android HCE transmite identificador de credencial y una firma sobre el desafío emitido por el backend; el lector reenvía la respuesta para validarla. |
| PMV-08 | Lector de laboratorio | Segundo Android con modo lector NFC muestra autorizado, saldo insuficiente, credencial inválida o servidor no disponible; registra latencia. |
| PMV-09 | Panel administrativo | Administrador consulta viajes y recargas, configura perfil estudiantil y revoca credenciales; usuario común no accede. |
| PMV-10 | Cobro consistente | Débito y registro se confirman en una transacción de BD; no hay saldo negativo ni cobro repetido por reintento. |
| PMV-11 | Operación reproducible | README, datos de demostración, migraciones, configuración de ejemplo y guía para instalar ambas APK. |

## 4. Fuera del PMV

Se posponen: tarjetas físicas y SAM, liquidación bancaria real, conciliación de pagos offline, bases separadas por concesionaria, Redis/Redlock/Sentinel, agente IA, recargas autónomas, GTFS/MQTT, aforo, SOS, gamificación, huella de carbono, Play Integrity, FIDO2 y plataforma completa de observabilidad. También quedan fuera integración oficial con transporte público, validación SUNEDU/MINEDU y publicación en tienda.

La seguridad básica sí es obligatoria: contraseñas con hash, autorización por rol, HTTPS para comunicaciones fuera del entorno local controlado, claves privadas en Keystore, validación en servidor y secretos fuera del repositorio.

## 5. Arquitectura propuesta

- App pasajero: Kotlin, Jetpack Compose, ViewModel, corrutinas, cliente HTTP y Android Keystore; HCE mediante HostApduService.
- App lector: Kotlin, modo lector NFC y comunicación HTTP con el backend. Muestra visualmente el resultado del pase.
- Backend único modular: NestJS con módulos auth, wallets, topups, fares, trips, credentials y admin.
- Persistencia: una instancia PostgreSQL, migraciones y transacciones. Elegir un solo ORM al iniciar, según experiencia del equipo.
- Panel: Next.js con login administrativo, tablas y formularios sencillos.
- Infraestructura: Docker Compose para API y BD; logs estructurados y endpoint de salud. El simulador bancario puede ser un adaptador dentro del backend.

No implementar un servicio independiente por operador: usar operator_id y configuraciones de tarifa. En el PMV la liquidación se limita a un reporte de importes por operador, sin pagos reales.

### Datos mínimos

users, wallets, wallet_entries, operators, fare_rules, trips, topup_requests, credentials, tap_challenges y audit_events. El historial financiero debe conservarse; corregir una operación mediante un movimiento compensatorio, sin borrar sus registros.

### Contratos iniciales de API

| Método y ruta | Uso | Responsable principal |
|---|---|---|
| POST /api/v1/auth/register y /login | Crear cuenta y sesión | P2 |
| GET /api/v1/wallet y /wallet/entries | Consultar saldo e historial | P2 |
| POST /api/v1/topups | Recarga con Idempotency-Key | P2 |
| POST /api/v1/credentials | Vincular clave pública del pasajero autenticado | P3 + P2 |
| POST /api/v1/reader/challenges | Crear desafío breve vinculado al lector y operador | P3 + P2 |
| POST /api/v1/taps | Verificar firma y autorizar cobro con Idempotency-Key | P2 + P3 |
| GET /api/v1/admin/trips y /topups | Consultas administrativas | P2 + P5 |
| PATCH /api/v1/admin/users/:id/profile | Perfil estudiantil simulado | P2 + P5 |
| POST /api/v1/admin/credentials/:id/revoke | Revocación de credencial | P2 + P5 |

P1 publica OpenAPI y ejemplos compartidos durante la semana 1. Congelar nombres de campos, errores e importes antes de conectar las aplicaciones. IDs, timestamps UTC y códigos de error deben tener el mismo formato en todos los clientes.

## 6. Reglas de negocio de demostración

Estas reglas son propuestas para el PMV y no representan tarifas oficiales:

| Regla | Definición |
|---|---|
| Operador A | Tarifa normal S/ 2.00. |
| Operador B | Tarifa normal S/ 2.50. |
| Perfil universitario simulado | 50 % sobre la tarifa base. |
| Transbordo | S/ 0.50 de descuento sobre la tarifa del perfil; nunca genera importe negativo. |
| Ventana | 120 minutos desde el primer viaje autorizado; no se reinicia con el transbordo. |
| Elegibilidad de transbordo | Operador distinto del viaje inicial y descuento no utilizado en esa ventana. |
| Mismo operador | Sin descuento de transbordo. |
| Saldo insuficiente | Se rechaza el viaje sin débito ni registro de viaje autorizado. |
| Lectura repetida accidental | Un nuevo tap del mismo usuario y operador dentro de 30 segundos se rechaza sin cobro; tiempo configurable para pruebas. |
| Recargas | S/ 5, S/ 10 y S/ 20 simulados; escenarios controlados de éxito, rechazo y timeout. |

Ejemplo: usuario normal recarga S/ 10, viaja en A por S/ 2 y después en B dentro de la ventana por S/ 2. Saldo final: S/ 6. Para un universitario, esos viajes cuestan S/ 1 y S/ 0.75; saldo final: S/ 8.25. Un tercer viaje no obtiene otro descuento en la misma ventana.

## 7. Flujo NFC y consistencia

1. La app del pasajero genera una clave en Android Keystore y registra su clave pública después del login. La clave privada no se envía al servidor.
2. El lector autenticado solicita un desafío al backend, vinculado a su operador. Para el PMV, expira a los 30 segundos.
3. Durante la lectura, el lector transmite el desafío y la app firma una representación canónica y versionada del desafío, su identificador y la credencial. No se realizan llamadas de red desde HostApduService.
4. El lector remite firma, credencial e identificador del desafío al endpoint de taps, con una clave de idempotencia estable para todos sus reintentos de esa operación.
5. El backend valida firma, expiración, revocación y vínculo con el lector. Dentro de una transacción bloquea el estado financiero del usuario, comprueba repetición, calcula tarifa, debita y registra el viaje y movimiento. El desafío se consume atómicamente y no puede autorizar dos viajes.
6. El lector abre visualmente el pase solo después de una autorización confirmada. La app pasajero actualiza su saldo al consultar al backend; para el PMV basta actualización manual o polling breve mientras la pantalla está abierta.

El formato firmado, la fragmentación de mensajes APDU y el comportamiento ante interrupción se definen y documentan en la prueba técnica de la semana 2. No se promete compatibilidad con lectores comerciales.

Si una respuesta de cobro se pierde, el lector conserva la misma clave y reintenta: el servidor devuelve el resultado previo. No debe crear otra solicitud con otra clave. Un UNIQUE de transaction_id por sí solo no evita que dos operaciones distintas gasten el mismo saldo: la transacción de BD y el bloqueo de billetera son obligatorios. Los cambios de ventana de transbordo también deben quedar bajo el mismo control de concurrencia.

## 8. Reparto entre cinco personas

Asignación propuesta siguiendo el orden de la portada; puede intercambiarse según experiencia, conservando las responsabilidades.

| Persona | Integrante | Responsabilidad | Entregables propios | Revisor |
|---|---|---|---|---|
| P1 | Said Andre Alferez Andia | Coordinación técnica, contratos e integración | Repositorio, Compose, CI básica, OpenAPI, entornos, prueba integrada y guía de ejecución | P2 |
| P2 | Axel Chunga Chevez | Backend y consistencia financiera | Auth, esquema BD, billetera, recargas, tarifas, taps, transbordos y APIs administrativas | P1 |
| P3 | Axl Mikel Crisóstomo Altamirano | NFC y seguridad del pase | Prueba de hardware, Keystore, servicio HCE, app lector y verificación de firmas coordinada con P2 | P2 |
| P4 | Bruce Javier Córdova Sandoval | Aplicación Android del pasajero | Login, inicio, recargas, historial, perfil y conexión con módulo HCE de P3 | P3 |
| P5 | Francess Jeicoll Vasquez Pelaez | Panel y verificación funcional | Panel administrativo, datos de demostración, matriz de pruebas y evidencias de aceptación | P4 |

La asignación usa los nombres de la portada, sujetos a confirmar su escritura antes de presentar el documento.

### Cómo evitar sobrecargar al responsable del backend

P1 implementa, con revisión de P2, los endpoints administrativos de consulta y el adaptador de recarga simulada. P3 implementa la verificación criptográfica en el backend con revisión de P2. P2 conserva la responsabilidad de la transacción financiera y las reglas tarifarias. P5 verifica interfaces y permisos; cada autor sigue siendo responsable de probar su módulo.

Presupuesto orientativo: P1 100–110 h; P2 110–120 h; P3 110–120 h; P4 100–110 h; P5 100–110 h. Total: 520–570 h, con margen dentro del supuesto de disponibilidad. Si una tarea supera su estimación, recortar mejoras visuales o ampliar plazo antes de retirar controles de cobro.

## 9. Calendario de diez semanas

| Semana | P1 | P2 | P3 | P4 | P5 | Hito común |
|---|---|---|---|---|---|---|
| 1 | Repositorio, Compose y contratos | Esquema y auth | Inventario NFC y protocolo | Navegación y pantallas base | Wireframes del panel y casos de aceptación | Alcance y contratos acordados |
| 2 | CI y entorno accesible | Wallet y migraciones | HCE y lector intercambian un desafío firmado | Login conectado | Panel base y datos de prueba | NFC real validado en hardware |
| 3 | Adaptador de banco simulado | Débito atómico y movimientos | Firma validada por backend | Saldo e historial | Login admin y consultas iniciales | Cobro básico desde lector |
| 4 | Apoyo a endpoints admin | Tarifas y transbordos | Manejo de errores y reintentos | Recargas conectadas | Perfil universitario y revocación | Flujo completo de primer viaje |
| 5 | Integración y entorno de demo | Idempotencia y concurrencia | Desafíos, expiración y revocación | Pase NFC y estados de cobro | Reportes por operador | Primera demostración integrada |
| 6 | Corrección de integración | Casos límite de tarifas | Lecturas interrumpidas y repetidas | Errores, sesión y reconexión | Pruebas funcionales completas | Funciones del PMV terminadas |
| 7 | Verificación de permisos | Pruebas financieras | Pruebas NFC en hardware disponible | Correcciones de Android | Regresión y evidencias | Pruebas críticas aprobadas |
| 8 | Instalación reproducible | Migraciones y recuperación | Medición de latencias | APK de pasajero candidata | Panel candidato y guía de pruebas | Versión candidata |
| 9 | Ensayo y cierre de incidencias | Correcciones críticas | Correcciones críticas | Accesibilidad y presentación | Manual y evidencias | Ensayo completo aprobado |
| 10 | Empaquetado y entrega | Soporte y revisión final | APK lector y guía | APK pasajero final | Informe de aceptación | Entrega final |

No esperar a terminar todo el backend para iniciar Android. P4 y P5 usan respuestas de ejemplo del contrato mientras P2 implementa. P3 prueba hardware desde el inicio. Las semanas 7–10 incluyen estabilización y margen; no se destinan a agregar módulos nuevos.

## 10. Dependencias y puntos de control

- Semana 1: P1, P2 y P3 acuerdan credenciales, payload firmado y errores. P4 y P5 reciben ejemplos JSON.
- Semana 2: deben existir dos dispositivos compatibles o lector de laboratorio y un intercambio HCE real. Si no funciona, atender este bloqueo antes de seguir ampliando funcionalidades.
- Semana 3: P2 ofrece cobro básico; P3 conecta lector; P4 consulta saldo. Una persona con saldo suficiente debe completar un viaje de extremo a extremo.
- Semana 5: demo conjunta con dos operadores, recarga, transbordo e historial. Si está atrasada, ampliar plazo o revisar alcance, sin incluir extras.
- Semana 6: congelar funciones. Después solo corregir, probar y preparar entrega.

Si no consiguen hardware NFC, puede usarse un botón de tap simulado para seguir desarrollando backend y UI, pero no sustituye el criterio PMV-07: presentar una versión sin NFC requiere cambiar explícitamente el alcance y declarar la simulación.

## 11. Pruebas mínimas de aceptación

| ID | Prueba | Resultado esperado | Lidera |
|---|---|---|---|
| A01 | Registro, login y token inválido | Acceso válido solo con sesión correcta | P2 + P4 |
| A02 | Usuario normal consulta API admin | Acceso denegado por backend | P1 + P5 |
| A03 | Recarga S/ 10 repetida con misma clave | Saldo aumenta una sola vez | P2 |
| A04 | Recarga rechazada | Sin abono ni cambio de saldo | P1 + P2 |
| A05 | Timeout después de procesar recarga y reintento | Resultado recuperado, sin segundo abono | P1 + P2 |
| A06 | Viajes A y B dentro de ventana | Débitos S/ 2 y S/ 2; saldo S/ 6 desde S/ 10 | P2 + P5 |
| A07 | Perfil universitario, viajes A y B | Débitos S/ 1 y S/ 0.75 | P2 + P5 |
| A08 | Viaje exactamente a 120 minutos | Sin descuento; ventana válida si diferencia es menor a 120 minutos | P2 |
| A09 | Mismo operador o descuento ya utilizado | No aplica nuevo descuento | P2 |
| A10 | Saldo insuficiente | Viaje denegado sin saldo negativo | P2 + P3 |
| A11 | Dos cobros concurrentes de S/ 2 con saldo S/ 2 | Solo uno autorizado; saldo cero | P2 |
| A12 | Tap confirmado cuya respuesta se pierde | Reintento con misma clave devuelve el resultado sin cobrar de nuevo | P2 + P3 |
| A13 | Firma alterada, desafío vencido o reutilizado | No autoriza un nuevo cobro | P3 |
| A14 | Credencial revocada | Rechazo incluso con firma criptográfica válida | P3 + P5 |
| A15 | Caída de red antes de autorizar | Lector informa falta de confirmación; no muestra pase autorizado | P3 |
| A16 | Reinicio de API y app | Saldo e historial permanecen y pueden consultarse de nuevo | P1 + P4 |
| A17 | Instalación en entorno limpio | Otro integrante ejecuta README e instala ambas APK | P1 + P5 |
| A18 | Dos lecturas accidentales próximas | Segunda denegada sin nuevo cobro | P2 + P3 |

Probar las reglas de tiempo con reloj controlable o datos preparados, sin esperar dos horas. Medir por separado tiempo NFC y tiempo total de autorización. Presentar resultados observados y condiciones de red/dispositivo; no declarar cumplimiento de 150 ms sin evidencia. Como exploración, registrar al menos 30 lecturas por combinación disponible, diferenciando fallos y lecturas exitosas.

## 12. Forma de trabajo

- Un repositorio con apps/passenger, apps/reader, apps/admin, services/api y docs.
- Tablero con Pendiente, En curso, En revisión y Terminado; cada tarjeta tiene responsable y criterio verificable.
- Ramas cortas por tarea; una revisión antes de integrar. P2 revisa cambios financieros y P3 revisa cambios del protocolo NFC.
- Dos sincronizaciones breves semanales y una demo integrada al final de cada semana.
- No subir secretos, datos personales reales ni llaves de firma al repositorio. Distribuir credenciales de demo por un canal del equipo y documentar cómo regenerarlas.
- Una tarea termina cuando funciona, maneja su error principal, tiene pruebas pertinentes, está revisada e integrada y su contrato/documentación están actualizados.
- Documentación compartida durante todo el proyecto; P5 consolida evidencias, pero cada persona aporta las de su módulo.

## 13. Entrega final y guion de demostración

Entregar código, README, migraciones, configuración de ejemplo, datos ficticios, APK pasajero, APK lector, panel, contrato API, matriz de pruebas con resultados y lista de limitaciones.

Demo de 8–10 minutos:

1. Mostrar arquitectura y declarar qué integraciones son simuladas.
2. Ingresar y recargar S/ 10; mostrar la operación en el panel.
3. Pagar por NFC en operador A: saldo S/ 8.
4. Pagar en B dentro de la ventana: saldo S/ 6 y descuento registrado.
5. Reenviar la misma operación: no cambia el saldo.
6. Intentar un viaje sin saldo suficiente y mostrar rechazo.
7. Revocar credencial e intentar un nuevo pase: rechazo.
8. Mostrar pruebas concurrentes, latencias medidas y limitaciones.

## 14. Primeras tareas para comenzar

| Orden | Responsable | Tarea concreta |
|---|---|---|
| 1 | Todos | Confirmar horas disponibles, reparto y alcance; usar datos ficticios. |
| 2 | P3 | Verificar ambos dispositivos, HCE y modo lector; registrar modelos y versiones Android. |
| 3 | P1 | Crear estructura, tablero y Compose con API y PostgreSQL. |
| 4 | P2 | Diseñar esquema, migración inicial y contrato de transacción financiera. |
| 5 | P4 | Crear proyecto Android y navegación con respuestas de ejemplo. |
| 6 | P5 | Crear panel base y checklist A01–A18. |
| 7 | P1 + P2 + P3 | Publicar contrato API y payload NFC; acordar errores e idempotencia. |

El PMV se considera completado cuando todas las funciones PMV-01 a PMV-11 y pruebas A01 a A18 están verificadas, y otra persona puede ejecutar el sistema siguiendo la guía. Las funciones pospuestas no forman parte de ese compromiso.
