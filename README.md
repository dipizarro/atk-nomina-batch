# atk-nomina-batch

Servicio de integracion batch para procesar nominas de documentos contables desde Artikos mediante Spring Boot, Spring Batch y Oracle.

La aplicacion consulta nominas con `NOMFACTERP`, confirma recepcion con `NOMFACTCONFIR`, procesa documentos localmente, envia resultados con `NOMFACTRES` y registra control funcional en `CONTROL_NOMINA`.

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

La aplicacion usa el perfil `local` por defecto y se conecta a Oracle. La configuracion local sensible vive en `src/main/resources/application-local.properties`, que esta ignorado por Git. Usa `src/main/resources/application-local.example.properties` como plantilla.

```powershell
mvn spring-boot:run
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
- `atk.batch.simulation-iterations=100`
- `atk.batch.chunk-size=20`
- `atk.batch.real.chunk-size=1`
- `atk.batch.sample-file=classpath:samples/ZSVIDA_Nom15960.xml`
- `app.diagnostics.enabled=false`

En Oracle, Spring Batch no crea su metadata automaticamente. La aplicacion usa `spring.batch.jdbc.initialize-schema=never`, por lo que los scripts SQL deben ejecutarse manualmente antes de disparar el endpoint de inicio.

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
- `GET /api/v1/dev/artikos/config/{profile}`
- `POST /api/v1/dev/control-nomina/test`

Este modo no debe estar habilitado en produccion. Los endpoints diagnosticos pueden consumir servicios Artikos QA o escribir datos de prueba en Oracle.

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

## Origen del proyecto

El servicio nacio como una POC para validar integracion SOAP Artikos, procesamiento Spring Batch y persistencia Oracle. A partir de Sprint 8.1 el nombre y la documentacion principal se normalizan como aplicacion de integracion batch, manteniendo compatibilidad con componentes diagnosticos hasta su limpieza posterior.

## Documentacion

- Arquitectura: `docs/architecture.md`
- Flujo batch: `docs/batch-flow.md`
- Endpoints: `docs/endpoints.md`
- Deuda tecnica: `docs/technical-debt.md`
- Decisiones: `docs/decisions`

## Validacion

```bash
mvn clean test
```

## Commit sugerido

```bash
git commit -m "chore: isolate diagnostic endpoints behind configuration"
```
