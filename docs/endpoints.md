# Endpoints

## Operacionales

| Metodo | Ruta | Uso |
| --- | --- | --- |
| `GET` | `/api/v1/health` | Health propio de la aplicacion |
| `POST` | `/api/v1/nominas/batch/start` | Inicia asincronicamente el job de nominas |
| `GET` | `/api/v1/nominas/batch/{jobExecutionId}` | Consulta estado Spring Batch |
| `GET` | `/api/v1/nominas/batch/{jobExecutionId}/summary` | Consulta resumen funcional del job |
| `GET` | `/api/v1/nominas/batch/{jobExecutionId}/results/{numeroNomina}` | Consulta resultado funcional por nomina |

## Administrativos

| Metodo | Ruta | Uso |
| --- | --- | --- |
| `POST` | `/api/v1/admin/batch-metadata/purge` | Simula o ejecuta purga controlada de metadata `BATCH_*` |
| `GET` | `/actuator/health` | Health Spring Actuator |
| `GET` | `/swagger-ui.html` | Documentacion OpenAPI |

El endpoint de purga debe protegerse con autenticacion y autorizacion antes de uso productivo.

## Diagnosticos temporales

Estos endpoints se conservan por compatibilidad y soporte de pruebas, pero no son productivos. En Sprint 8.2 deben moverse bajo `/api/v1/dev/...`, protegerse por perfil o eliminarse si ya no son necesarios.

| Metodo | Ruta | Uso |
| --- | --- | --- |
| `POST` | `/api/v1/artikos/qa/nominas/fetch` | Prueba directa `NOMFACTERP` |
| `POST` | `/api/v1/artikos/qa/nominas/confirm` | Prueba directa `NOMFACTCONFIR` |
| `POST` | `/api/v1/artikos/qa/nominas/result/test` | Prueba directa `NOMFACTRES` con payload manual |
| `GET` | `/api/v1/artikos/qa/nominas/config/{profile}` | Muestra configuracion Artikos enmascarada |
| `GET` | `/api/v1/artikos/qa/config/{profile}` | Muestra configuracion Artikos enmascarada |
| `POST` | `/api/v1/control-nomina/test` | Inserta y actualiza un registro diagnostico en `CONTROL_NOMINA` |
| `GET` | `/api/v1/control-nomina/jobs/{jobExecutionId}` | Consulta diagnostica de `CONTROL_NOMINA` por job |
| `GET` | `/api/v1/control-nomina/jobs/{jobExecutionId}/nominas/{numeroNomina}` | Consulta diagnostica de un registro `CONTROL_NOMINA` |

La configuracion enmascarada nunca debe exponer tokens completos. Los valores sensibles deben salir solo como presencia y mascara parcial.
