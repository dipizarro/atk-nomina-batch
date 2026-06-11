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
- Actuator health: `GET /actuator/health`
- Consola H2: `GET /h2-console`
- Swagger UI: `GET /swagger-ui.html`

## Configuracion principal

La configuracion vive en `src/main/resources/application.yml`.

- `atk.batch.simulation-iterations=100`
- `atk.batch.chunk-size=20`
- `atk.batch.sample-file=classpath:samples/nomina-soap-local.xml`

Spring Batch inicializa su metadata con `spring.batch.jdbc.initialize-schema=always`.

## Validacion

```bash
mvn clean test
```

## Commit sugerido

```bash
git commit -m "feat: add REST endpoint to launch nomina batch job"
```
