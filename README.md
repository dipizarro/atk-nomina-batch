# atk-nomina-batch

Servicio de integracion batch para procesar nominas de documentos contables desde Artikos mediante Spring Boot, Spring Batch y Oracle.

La aplicacion consulta nominas con `NOMFACTERP`, confirma recepcion con `NOMFACTCONFIR`, procesa documentos localmente o contra Procurement, envia resultados con `NOMFACTRES` y registra control funcional en `CONTROL_NOMINA`.

## Requisitos

- Java 17
- Maven 3.9+
- Oracle con metadata Spring Batch `BATCH_*`
- Tabla funcional `CONTROL_NOMINA`

## Stack

- Spring Boot Web
- Spring Batch
- Spring Validation
- Spring Data JPA
- Spring Actuator
- Oracle JDBC
- H2 para tests
- Log4j2
- Springdoc OpenAPI
- JUnit, Spring Boot Test y Spring Batch Test

## Ejecucion local

La aplicacion debe ejecutarse con el perfil `local` para desarrollo en la maquina del equipo. La configuracion local sensible vive en `src/main/resources/application-local.properties`, que esta ignorado por Git. Usa `src/main/resources/application-local.example.properties` como plantilla.

```powershell
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

Tambien puedes sobreescribir valores desde variables de entorno y luego copiarlos al archivo local si lo prefieres:

```powershell
$env:ATK_ORACLE_URL='jdbc:oracle:thin:@host:puerto:sid'
$env:ATK_ORACLE_USERNAME='usuario'
$env:ATK_ORACLE_PASSWORD='password'
```

## Endpoints principales

- Health propio: `GET /api/v1/health`
- Iniciar batch de nominas: `POST /api/v1/nominas/batch/start`
- Consultar estado batch: `GET /api/v1/nominas/batch/{jobExecutionId}`
- Consultar resumen batch: `GET /api/v1/nominas/batch/{jobExecutionId}/summary`
- Consultar resultado por nomina: `GET /api/v1/nominas/batch/{jobExecutionId}/results/{numeroNomina}`
- Consultar control por job: `GET /api/v1/control-nomina/jobs/{jobExecutionId}`
- Consultar control por nomina: `GET /api/v1/control-nomina/jobs/{jobExecutionId}/nominas/{numeroNomina}`
- Simular o ejecutar purga de metadata Spring Batch: `POST /api/v1/admin/batch-metadata/purge`
- Actuator health: `GET /actuator/health`
- Swagger UI: `GET /swagger-ui.html`

Los endpoints temporales de diagnostico Artikos y CONTROL_NOMINA viven bajo `/api/v1/dev/...` y solo se cargan si `app.diagnostics.enabled=true`.

## Configuracion principal

La configuracion base vive en `src/main/resources/application.properties`. La configuracion local sensible vive en `src/main/resources/application-local.properties`, que esta ignorado por Git.

- `atk.batch.simulation-nominas=1000`
- `atk.batch.default-max-nominas=50`
- `atk.batch.max-nominas-per-run=50`
- `atk.batch.simulation-iterations=100`
- `atk.batch.chunk-size=20`
- `atk.batch.real.chunk-size=1`
- `atk.batch.sample-file=classpath:samples/ZSVIDA_Nom15960.xml`
- `app.diagnostics.enabled=false`
- `app.admin.enabled=false`
- `app.config.validation.strict=false`
- `artikos.http.connect-timeout-ms=5000`
- `artikos.http.read-timeout-ms=30000`
- `artikos.retry.enabled=true`
- `artikos.retry.max-attempts=3`
- `artikos.retry.backoff-ms=1000`
- `procurement.client.enabled=false`
- `procurement.integration.enabled=false`

En Oracle, Spring Batch no crea su metadata automaticamente. La aplicacion usa `spring.batch.jdbc.initialize-schema=never`, por lo que los scripts SQL deben ejecutarse manualmente antes de disparar el endpoint de inicio.

## Configuration and secrets

La aplicacion separa configuracion por perfiles Spring:

- `application.properties`: base comun sin secretos.
- `application-local.properties`: desarrollo local, ignorado por Git.
- `application-local.example.properties`: plantilla segura para desarrollo local.
- `application-qa.properties`: QA con placeholders.
- `application-prod.properties`: produccion con placeholders y diagnostics deshabilitado.

QA y PROD deben resolver secretos desde Azure Key Vault, variables de entorno inyectadas por pipeline/runtime o un mecanismo administrado equivalente. Cuando la aplicacion corra en Azure, se debe preferir Managed Identity para acceder a Key Vault.

No subir passwords, tokens Artikos, connection strings ni archivos `.env` al repositorio. La lista de secretos esperados y nombres recomendados esta en `docs/secrets.md`.

## Diagnostics mode

El modo diagnostico esta deshabilitado por defecto:

```properties
app.diagnostics.enabled=false
```

Para pruebas locales puede habilitarse en `src/main/resources/application-local.properties`:

```properties
app.diagnostics.enabled=true
```

Cuando esta activo, la aplicacion expone endpoints bajo `/api/v1/dev/...` para probar operaciones SOAP Artikos y una insercion de prueba en `CONTROL_NOMINA`:

- `POST /api/v1/dev/artikos/nominas/fetch`
- `POST /api/v1/dev/artikos/nominas/confirm`
- `POST /api/v1/dev/artikos/nominas/result/test`
- `POST /api/v1/dev/procurement/documents/test`
- `GET /api/v1/dev/artikos/config/{profile}`
- `POST /api/v1/dev/control-nomina/test`

Este modo no debe estar habilitado en produccion. Los endpoints diagnosticos pueden consumir servicios Artikos QA o escribir datos de prueba en Oracle.

Para probar solo el mapeo Artikos XML -> Procurement CMP y el POST a Procurement, sin confirmar Artikos ni tocar `CONTROL_NOMINA`:

```powershell
curl -X POST "http://localhost:8080/api/v1/dev/procurement/documents/test" `
  -H "Content-Type: application/json" `
  -d "{\"profile\":\"VIDA\",\"documentIndex\":0}"
```

Si no se envia `rawXml`, el endpoint usa el XML configurado en `atk.batch.sample-file`.

## Gateway exposure

La aplicacion esta preparada para operar detras del API Gateway corporativo CONC/Kong. En este sprint no implementa Spring Security, OAuth2, Basic Auth ni JWT; la autenticacion y autorizacion deben ser aplicadas por el gateway.

El contrato productivo inicial publicado por gateway es solamente:

```http
POST /api/v1/nominas/batch/start
```

Los endpoints operativos de consulta, `CONTROL_NOMINA`, administracion y diagnostico quedan apagados por defecto en QA/PROD:

```properties
app.endpoints.operations.enabled=false
app.diagnostics.enabled=false
app.admin.enabled=false
```

La matriz de exposicion esta documentada en `docs/gateway-endpoints.md`.

## Procurement integration

La aplicacion cuenta con mapper JSON Artikos -> Procurement `CMP`, cliente HTTP configurable y procesamiento documental opcional dentro del batch para el endpoint objetivo:

```http
POST /api/v1/document
```

Por defecto la integracion batch permanece deshabilitada:

```properties
procurement.integration.enabled=false
```

Cuando `procurement.integration.enabled=true`, cada documento Artikos se envia individualmente a Procurement. `statusCode=0` se interpreta como `OK`; `statusCode!=0` se interpreta como `NOK` funcional y se informa a Artikos via `NOMFACTRES`. Timeouts, errores de conexion, HTTP `5xx`, respuestas no parseables, serializacion y errores de mapeo Artikos -> CMP detienen el job, dejan `CONTROL_NOMINA` en `ERROR` y no envian `NOMFACTRES`.

Si el job se inicia con `dryRun=true`, el procesamiento se fuerza a la ruta simulada/local y no llama Procurement aunque `procurement.integration.enabled=true`.

El mapeo actual construye el request CMP desde `DocumentoContable`, genera una linea por distribucion Artikos y deja `HNR` fuera de alcance. La aplicacion aun no consulta ASI.

El detalle esta documentado en `docs/procurement-mapping.md` y `docs/procurement-integration.md`.

## Local XML end-to-end testing

Cuando QA Artikos no tiene nominas disponibles, se puede ejecutar el batch usando el XML local Artikos como fuente:

```properties
artikos.source.mode=local-xml
artikos.source.local-xml-path=classpath:samples/artikos/ZSVIDA_Nom15960.xml
artikos.confirm.enabled=false
artikos.result.enabled=false
```

Este modo no consume `NOMFACTERP`, no confirma `NOMFACTCONFIR` y no envia `NOMFACTRES` real a Artikos. Si `procurement.integration.enabled=true`, si permite validar el envio tecnico a Procurement.

Guia completa: `docs/local-e2e-testing.md`.

## Oracle

Los scripts Oracle necesarios estan en:

```text
src/main/resources/db/oracle/V000__create_spring_batch_metadata.sql
src/main/resources/db/oracle/V001__create_control_nomina.sql
```

`V000` crea las tablas tecnicas `BATCH_*` que Spring Batch usa para instancias, ejecuciones, parametros y steps. `V001` crea `CONTROL_NOMINA`, que contiene el control funcional por nomina procesada.

## Purga de metadata Spring Batch

Las tablas `BATCH_*` son metadata tecnica de Spring Batch. No reemplazan la auditoria funcional de nominas, que vive en `CONTROL_NOMINA`.

Para evitar crecimiento indefinido de metadata tecnica existe un endpoint administrativo de purga controlada:

```http
POST /api/v1/admin/batch-metadata/purge
```

Ejemplo de simulacion:

```json
{
  "retentionDays": 30,
  "dryRun": true,
  "includeFailed": false
}
```

Reglas principales:

- `retentionDays` es obligatorio y debe ser mayor o igual a `1`.
- `dryRun` por defecto es `true`; en ese modo solo devuelve conteos candidatos por tabla.
- Por defecto considera ejecuciones finalizadas con status `COMPLETED` y `ABANDONED`.
- `FAILED` solo se considera si `includeFailed=true`.
- Nunca purga ejecuciones activas o sin `END_TIME`.
- La eliminacion respeta dependencias: contextos de step, steps, contextos de job, parametros, ejecuciones e instancias sin ejecuciones restantes.

Ejecutar con `dryRun=false` elimina registros reales de `BATCH_*`; en produccion este endpoint debe protegerse con autenticacion y autorizacion.

El endpoint de purga solo se carga si:

```properties
app.admin.enabled=true
```

## Operational logging

Los logs incluyen contexto MDC para trazabilidad operacional:

- `jobExecutionId`
- `profile`
- `numeroNomina`
- `operation`

Las operaciones SOAP se registran como `NOMFACTERP`, `NOMFACTCONFIR` y `NOMFACTRES`, con tiempos de ejecucion y status HTTP cuando aplica. Los tokens nunca deben imprimirse completos; se registran solo como presencia y valor enmascarado. El XML SOAP completo solo puede aparecer en `DEBUG` y con token enmascarado.

La convencion completa esta en `docs/logging.md`.

## Error handling policy

La politica de errores esta documentada en `docs/error-handling.md`.

Resumen operativo:

- Sin nominas disponibles en Artikos: el job termina `COMPLETED` sin registros en `CONTROL_NOMINA`.
- Error tecnico consultando Artikos: el job termina `FAILED`.
- Rechazo de `NOMFACTCONFIR`: la nomina queda `ERROR` en `CONTROL_NOMINA` y el job termina `FAILED`.
- Documento con validacion funcional NOK: el job continua, envia `NOMFACTRES` y la nomina queda `NOK`.
- Documento rechazado funcionalmente por Procurement: el job continua, envia `NOMFACTRES` y la nomina queda `NOK`.
- Error tecnico o mapping Procurement: la nomina queda `ERROR`, no se envia `NOMFACTRES` y el job termina `FAILED`.
- Rechazo o falla de `NOMFACTRES`: la nomina queda `ERROR` y el job termina `FAILED`.

Los endpoints de estado/resumen devuelven errores compactados; el stacktrace completo queda en logs y metadata Spring Batch.

## Operational hardening

Los controles operativos estan documentados en `docs/operational-hardening.md`.

Incluyen timeouts SOAP, retry tecnico, errores no reintentables, limite `maxNominas`, concurrencia por perfil, health checks y proteccion por property del endpoint administrativo.

## Operations

La documentacion operativa inicial esta en:

- Runbook operativo: `docs/runbook.md`
- Consultas SQL de soporte: `docs/sql-queries.md`
- Guia de soporte: `docs/support-guide.md`
- Manejo de errores: `docs/error-handling.md`
- Hardening operativo: `docs/operational-hardening.md`
- Secretos y ambientes: `docs/secrets.md`

El runbook explica como iniciar el batch, monitorear estado, revisar `CONTROL_NOMINA`, interpretar metadata `BATCH_*`, purgar metadata y actuar ante errores frecuentes.

## Architecture and package conventions

La revision estructural esta documentada en `docs/architecture-review.md`.

El namespace base actual es `cl.atk.nomina.batch`. La decision esta registrada en `docs/decisions/ADR-002-package-namespace.md`.

## Origen del proyecto

El servicio nacio como una POC para validar integracion SOAP Artikos, procesamiento Spring Batch y persistencia Oracle. A partir de Sprint 8.1 el nombre y la documentacion principal se normalizan como aplicacion de integracion batch, manteniendo compatibilidad con componentes diagnosticos hasta su limpieza posterior.

## Documentacion

- Arquitectura: `docs/architecture.md`
- Flujo batch: `docs/batch-flow.md`
- Endpoints: `docs/endpoints.md`
- Runbook operativo: `docs/runbook.md`
- Consultas SQL de soporte: `docs/sql-queries.md`
- Guia de soporte: `docs/support-guide.md`
- Matriz gateway: `docs/gateway-endpoints.md`
- Mapeo Procurement CMP: `docs/procurement-mapping.md`
- Integracion Procurement: `docs/procurement-integration.md`
- Logging: `docs/logging.md`
- Hardening operativo: `docs/operational-hardening.md`
- Manejo de errores: `docs/error-handling.md`
- Secretos y ambientes: `docs/secrets.md`
- Revision de arquitectura: `docs/architecture-review.md`
- Deuda tecnica: `docs/technical-debt.md`
- Decisiones: `docs/decisions`

## Validacion

```bash
mvn clean test
```

## Commit sugerido

```bash
git commit -m "docs: add operational runbook and support guide"
```
