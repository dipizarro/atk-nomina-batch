# Arquitectura

## Objetivo

`atk-nomina-batch-poc` es una POC para validar procesamiento batch de nominas ATK usando Spring Boot 3, Spring Batch y un disparador REST.

## Componentes iniciales

- API REST: expone endpoints operacionales bajo `/api/v1`.
- Batch: concentra configuracion, readers, processors y writers de Spring Batch.
- Dominio: alojara modelos de negocio independientes de infraestructura.
- Servicios: coordinara casos de uso entre API, dominio y batch.
- Shared exception: centralizara excepciones y manejo transversal.

## Disparador REST

El endpoint `POST /api/v1/nominas/batch/start` actua como interruptor del job `nominaDocumentosContablesJob`.
El lanzamiento usa un `JobLauncher` con `TaskExecutor`, por lo que la respuesta se entrega inmediatamente con el
`jobExecutionId` mientras el step `processNominaDocumentosStep` continua ejecutandose en segundo plano.

El endpoint `GET /api/v1/nominas/batch/{jobExecutionId}` consulta la metadata persistida por Spring Batch mediante
`JobExplorer`.

El endpoint `GET /api/v1/nominas/batch/{jobExecutionId}/summary` expone los totales funcionales almacenados en memoria
para la POC.

El endpoint `GET /api/v1/nominas/batch/{jobExecutionId}/results/{numeroNomina}` expone el resumen de una nomina
procesada y el XML NOMFACTRES generado.

## Procesamiento chunk

El job procesa nominas completas mediante `ItemReader`, `ItemProcessor` e `ItemWriter`.

- Reader real: consulta Artikos QA con `NOMFACTERP`, parsea la respuesta SOAP y entrega una nomina por item.
- Processor real: registra `CONTROL_NOMINA` en `PROCESSING`, confirma recepcion con `NOMFACTCONFIR` cuando `dryRun=false`, procesa documentos y genera el XML `NOMFACTRES`.
- Writer real: envia `NOMFACTRES` cuando `dryRun=false`, actualiza `CONTROL_NOMINA` con `OK`, `NOK` o `ERROR`, y agrega resultados por `jobExecutionId` y `numeroNomina` al store en memoria.
- Componentes simulados: se conservan para pruebas locales y evolucion de la POC, pero el job principal usa el flujo Artikos.

Para SOAP real, el tamano de chunk se configura con `atk.batch.real.chunk-size` y se recomienda `1`, porque cada nomina implica confirmacion y envio de resultado.

## Estados funcionales Artikos

Las pruebas contra Artikos QA y SoapUI mostraron que `NOMFACTERP` puede seguir devolviendo una nomina aunque no este apta para avanzar en las operaciones siguientes.

Validaciones funcionales observadas:

- `NOMFACTCONFIR` requiere que la nomina este en estado `En Integracion`.
- `NOMFACTRES` requiere que la nomina este en estado `Recibida`.

Si Artikos rechaza una operacion por estado, la respuesta llega con `MsgStatus=1` y debe registrarse como error funcional en `CONTROL_NOMINA`.

La decision queda documentada en:

```text
docs/decisions/ADR-003-artikos-nomina-state-transitions.md
```

## Persistencia y metadata batch

Los tests usan H2 en memoria con modo compatible Oracle:

```yaml
spring.datasource.url: jdbc:h2:mem:atk_nomina_batch;MODE=Oracle;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
```

En tests, Spring Batch crea sus tablas de metadata automaticamente mediante:

```yaml
spring.batch.jdbc.initialize-schema: always
```

En ejecucion local contra Oracle, la aplicacion usa `spring.batch.jdbc.initialize-schema=never`. Las tablas de metadata
`BATCH_*` deben existir antes de invocar `POST /api/v1/nominas/batch/start`, ejecutando:

```text
src/main/resources/db/oracle/V000__create_spring_batch_metadata.sql
```

La tabla funcional de control por nomina se crea con:

```text
src/main/resources/db/oracle/V001__create_control_nomina.sql
```

## Paquetes base

- `cl.poc.atkbatch.api.controller`
- `cl.poc.atkbatch.api.dto`
- `cl.poc.atkbatch.batch.config`
- `cl.poc.atkbatch.batch.reader`
- `cl.poc.atkbatch.batch.processor`
- `cl.poc.atkbatch.batch.writer`
- `cl.poc.atkbatch.domain`
- `cl.poc.atkbatch.service`
- `cl.poc.atkbatch.shared.exception`
