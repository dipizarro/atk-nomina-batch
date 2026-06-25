# atk-nomina-batch

Servicio Spring Boot + Spring Batch para integrar nominas Artikos con Procurement CMP.

La aplicacion consulta nominas Artikos con `NOMFACTERP`, confirma recepcion con `NOMFACTCONFIR`, procesa documentos contra Procurement, genera `NOMFACTRES` y registra control funcional en Oracle mediante `CONTROL_NOMINA`.

## Stack

- Java 17
- Spring Boot 3.3.5
- Spring Batch
- Spring Data JPA
- Oracle JDBC
- Log4j2
- Spring Actuator
- Springdoc OpenAPI
- Maven

## Endpoints publicados

Para entrega inicial detras de CONC/Kong solo se deben publicar:

| Endpoint | Uso |
| --- | --- |
| `POST /api/v1/nominas/batch/start` | Dispara asincronicamente el batch de nominas. |
| `GET /actuator/health` | Health check para monitoreo interno. |

No quedan disponibles por defecto en QA/PROD:

- `/api/v1/nominas/batch/**` consultas operativas.
- `/api/v1/control-nomina/**`.
- `/api/v1/admin/**`.
- `/api/v1/dev/**`.
- Swagger/OpenAPI.

La matriz completa esta en [docs/gateway-endpoints.md](docs/gateway-endpoints.md).

## Configuracion

La configuracion productiva debe entregarse por variables de entorno, Azure App Configuration y Azure Key Vault. No se versionan passwords, tokens, connection strings ni `application-local.properties`.

Archivos incluidos:

- `src/main/resources/application.properties`: defaults seguros sin secretos.
- `src/main/resources/application-qa.properties`: placeholders QA.
- `src/main/resources/application-prod.properties`: placeholders PROD.
- `src/main/resources/application-local.example.properties`: plantilla local sin secretos reales.

`application-local.properties` esta ignorado por Git y excluido del empaquetado Maven.

Variables y permisos requeridos para Infra: [docs/infra-delivery.md](docs/infra-delivery.md).

## Profiles

QA/PROD usan modo remoto Artikos:

```properties
artikos.source.mode=remote
artikos.confirm.enabled=true
artikos.result.enabled=true
app.diagnostics.enabled=false
app.admin.enabled=false
app.endpoints.operations.enabled=false
```

Si las tablas Spring Batch viven en otro esquema, Infra debe definir:

```properties
SPRING_BATCH_JDBC_TABLE_PREFIX=BACHPROCESS.BATCH_
```

## Build local

```bash
mvn clean test
mvn clean package -DskipTests
```

El jar generado queda con el patron:

```text
target/atk-nomina-batch-*.jar
```

## Ejecucion local con properties externo

Crear un archivo local fuera del jar, por ejemplo:

```text
C:/deploy/atk-nomina-batch/config/application-local.properties
```

Usar como base `src/main/resources/application-local.example.properties` y completar valores locales.

Ejecucion:

```powershell
java -jar target/atk-nomina-batch-0.0.1-SNAPSHOT.jar `
  --spring.profiles.active=local `
  --spring.config.additional-location=file:C:/deploy/atk-nomina-batch/config/
```

## Docker

El `Dockerfile` usa build multi-stage con Maven + Java 17 y runtime Java 17 liviano:

```bash
docker build -t atk-nomina-batch:local .
```

## GitLab CI/CD

El archivo `.gitlab-ci.yml` deja una base para GitLab con etapas de validacion, test, build, calidad, package y deploy.

Valores pendientes de confirmar por Infra:

- `FLUX_RESOURCE_PATH`
- `IAC_GIT_REPO`
- componentes corporativos definitivos del repo modelo `artikos-integration`
- publicacion final de imagen/container registry
- estrategia Azure App Configuration / Key Vault

## Oracle

Scripts versionados:

```text
src/main/resources/db/oracle/V000__create_spring_batch_metadata.sql
src/main/resources/db/oracle/V001__create_control_nomina.sql
```

La aplicacion usa Oracle para:

- Metadata Spring Batch `BATCH_*`.
- Control funcional `CONTROL_NOMINA`.
- Lookup ASI `GRL_MAE_ITEM` y `GRL_MAE_ITEM_DET`.

Detalle de permisos: [docs/infra-delivery.md](docs/infra-delivery.md).

## Runbook

Documentacion operativa:

- [docs/runbook.md](docs/runbook.md)
- [docs/sql-queries.md](docs/sql-queries.md)
- [docs/error-handling.md](docs/error-handling.md)
- [docs/operational-hardening.md](docs/operational-hardening.md)
- [docs/artikos-remote-e2e.md](docs/artikos-remote-e2e.md)
- [docs/delivery-checklist.md](docs/delivery-checklist.md)
- [docs/release-notes.md](docs/release-notes.md)

## Validacion previa a entrega

```bash
mvn clean test
mvn clean package -DskipTests
docker build -t atk-nomina-batch:local .
```

Antes de publicar en GitLab revisar:

- No versionar `target/`, `logs/`, `.env`, zips locales ni jars generados.
- No versionar `src/main/resources/application-local.properties`.
- No incluir passwords, tokens ni URLs internas sensibles en commits.
- Confirmar que QA/PROD mantengan apagados diagnostico, admin, operaciones y Swagger.

Commit sugerido:

```bash
git commit -m "chore: prepare GitLab infrastructure delivery"
```
