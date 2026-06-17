# atk-nomina-batch-poc

POC de procesamiento batch para nominas ATK, construido con Java 17, Spring Boot 3 y Spring Batch.

## Requisitos

- Java 17
- Maven 3.9+

## Stack base

- Spring Boot Web
- Spring Batch
- Spring Validation
- Spring Actuator
- H2 en memoria
- Springdoc OpenAPI
- JUnit, Spring Boot Test y Spring Batch Test

## Ejecucion local

La aplicacion usa el perfil `local` por defecto y se conecta a Oracle. La configuracion local vive en `src/main/resources/application-local.properties`, que esta ignorado por Git. Usa `src/main/resources/application-local.example.properties` como plantilla.

Antes de levantarla, valida que el archivo tenga la password:

```powershell
mvn spring-boot:run
```

Tambien puedes sobreescribir valores desde variables de entorno y luego copiarlos al archivo local si lo prefieres:

```powershell
$env:ATK_ORACLE_URL='jdbc:oracle:thin:@172.18.10.208:1521:mettti'
$env:ATK_ORACLE_USERNAME='ASI'
$env:ATK_ORACLE_PASSWORD='tu_password'
```

Para ejecutar usando la configuracion local:

```powershell
mvn spring-boot:run
```

```bash
mvn spring-boot:run
```

La aplicacion expone:

- Health propio: `GET /api/v1/health`
- Iniciar batch de nominas: `POST /api/v1/nominas/batch/start`
- Consultar batch de nominas: `GET /api/v1/nominas/batch/{jobExecutionId}`
- Consultar resumen del batch: `GET /api/v1/nominas/batch/{jobExecutionId}/summary`
- Consultar resultado por nomina: `GET /api/v1/nominas/batch/{jobExecutionId}/results/{numeroNomina}`
- Simular o ejecutar purga de metadata Spring Batch: `POST /api/v1/admin/batch-metadata/purge`
- Actuator health: `GET /actuator/health`
- Consola H2: `GET /h2-console`
- Swagger UI: `GET /swagger-ui.html`

## Configuracion principal

La configuracion base vive en `src/main/resources/application.properties`. La configuracion local sensible vive en `src/main/resources/application-local.properties`, que esta ignorado por Git.

- `atk.batch.simulation-nominas=1000`
- `atk.batch.chunk-size=20`
- `atk.batch.sample-file=classpath:samples/ZSVIDA_Nom15960.xml`

En Oracle, Spring Batch no crea su metadata automaticamente. La aplicacion usa
`spring.batch.jdbc.initialize-schema=never`, por lo que los scripts SQL deben ejecutarse manualmente antes de disparar
el endpoint de inicio.

## Oracle control table

Los scripts Oracle necesarios para esta etapa estan en:

```text
src/main/resources/db/oracle/V000__create_spring_batch_metadata.sql
src/main/resources/db/oracle/V001__create_control_nomina.sql
```

Para esta etapa de la POC, ambos scripts deben ejecutarse manualmente en SQL Developer con el usuario/esquema de la
aplicacion. `V000` crea las tablas `BATCH_*` que Spring Batch consulta antes de iniciar el job; `V001` crea
`CONTROL_NOMINA`.

## Purga de metadata Spring Batch

Las tablas `BATCH_*` son metadata tecnica de Spring Batch: instancias, ejecuciones, parametros, steps y contextos. No son
la auditoria funcional de nominas. La trazabilidad funcional vive en `CONTROL_NOMINA`.

Para evitar crecimiento indefinido de metadata tecnica, existe un endpoint administrativo de purga controlada:

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

Ejecutar con `dryRun=false` elimina registros reales de `BATCH_*`; en produccion este endpoint debe protegerse con
autenticacion y autorizacion.

## Validacion

```bash
mvn clean test
```

## Commit sugerido

```bash
git commit -m "chore: add Oracle control_nomina DDL script"
```
