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

```bash
mvn spring-boot:run
```

La aplicacion expone:

- Health propio: `GET /api/v1/health`
- Iniciar batch de nominas: `POST /api/v1/nominas/batch/start`
- Consultar batch de nominas: `GET /api/v1/nominas/batch/{jobExecutionId}`
- Consultar resumen del batch: `GET /api/v1/nominas/batch/{jobExecutionId}/summary`
- Consultar resultado por nomina: `GET /api/v1/nominas/batch/{jobExecutionId}/results/{numeroNomina}`
- Actuator health: `GET /actuator/health`
- Consola H2: `GET /h2-console`
- Swagger UI: `GET /swagger-ui.html`

## Configuracion principal

La configuracion vive en `src/main/resources/application.yml`.

- `atk.batch.simulation-nominas=1000`
- `atk.batch.chunk-size=20`
- `atk.batch.sample-file=classpath:samples/ZSVIDA_Nom15960.xml`

Spring Batch inicializa su metadata con `spring.batch.jdbc.initialize-schema=always`.

## Oracle control table

El script Oracle para crear la tabla de control de nominas esta en:

```text
src/main/resources/db/oracle/V001__create_control_nomina.sql
```

Para esta etapa de la POC, el script debe ejecutarse manualmente en SQL Developer.

## Validacion

```bash
mvn clean test
```

## Commit sugerido

```bash
git commit -m "chore: add Oracle control_nomina DDL script"
```
