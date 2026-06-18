# Endpoints

## Productive Endpoints

Estos endpoints forman parte del contrato REST productivo de la aplicacion.

| Metodo | Ruta | Uso |
| --- | --- | --- |
| `GET` | `/api/v1/health` | Health propio de la aplicacion |
| `POST` | `/api/v1/nominas/batch/start` | Inicia asincronicamente el job de nominas |
| `GET` | `/api/v1/nominas/batch/{jobExecutionId}` | Consulta estado Spring Batch |
| `GET` | `/api/v1/nominas/batch/{jobExecutionId}/summary` | Consulta resumen funcional del job |
| `GET` | `/api/v1/nominas/batch/{jobExecutionId}/results/{numeroNomina}` | Consulta resultado funcional por nomina |
| `GET` | `/api/v1/control-nomina/jobs/{jobExecutionId}` | Consulta registros `CONTROL_NOMINA` por job |
| `GET` | `/api/v1/control-nomina/jobs/{jobExecutionId}/nominas/{numeroNomina}` | Consulta un registro `CONTROL_NOMINA` especifico |
| `POST` | `/api/v1/admin/batch-metadata/purge` | Simula o ejecuta purga controlada de metadata `BATCH_*`; requiere `app.admin.enabled=true` |
| `GET` | `/actuator/health` | Health Spring Actuator |
| `GET` | `/swagger-ui.html` | Documentacion OpenAPI |

El endpoint de purga solo se carga si `app.admin.enabled=true`. Ademas, debe protegerse con autenticacion y autorizacion antes de uso productivo.

## Diagnostic Endpoints

Los endpoints diagnosticos solo se cargan si:

```properties
app.diagnostics.enabled=true
```

Por defecto la propiedad esta deshabilitada en `application.properties`. Puede habilitarse en `application-local.properties` o en ambientes QA controlados. No debe estar activa en produccion.

| Metodo | Ruta | Uso |
| --- | --- | --- |
| `POST` | `/api/v1/dev/artikos/nominas/fetch` | Prueba directa `NOMFACTERP` |
| `POST` | `/api/v1/dev/artikos/nominas/confirm` | Prueba directa `NOMFACTCONFIR` |
| `POST` | `/api/v1/dev/artikos/nominas/result/test` | Prueba directa `NOMFACTRES` con payload manual |
| `GET` | `/api/v1/dev/artikos/config/{profile}` | Muestra configuracion Artikos enmascarada |
| `POST` | `/api/v1/dev/control-nomina/test` | Inserta y actualiza un registro diagnostico en `CONTROL_NOMINA` |

La configuracion enmascarada nunca debe exponer tokens completos. Los valores sensibles deben salir solo como presencia y mascara parcial.
