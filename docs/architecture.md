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
`jobExecutionId` mientras el step `initialNominaTaskletStep` continua ejecutandose en segundo plano.

El endpoint `GET /api/v1/nominas/batch/{jobExecutionId}` consulta la metadata persistida por Spring Batch mediante
`JobExplorer`.

## Persistencia y metadata batch

La POC usa H2 en memoria con modo compatible Oracle:

```yaml
spring.datasource.url: jdbc:h2:mem:atk_nomina_batch;MODE=Oracle;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
```

Spring Batch puede crear sus tablas de metadata automaticamente mediante:

```yaml
spring.batch.jdbc.initialize-schema: always
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
