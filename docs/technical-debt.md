# Deuda Tecnica

## Endpoints diagnosticos

Resuelto parcialmente en Sprint 8.2:

- Endpoints Artikos de diagnostico movidos bajo `/api/v1/dev/artikos/...`.
- Endpoint `CONTROL_NOMINA` de prueba movido bajo `/api/v1/dev/control-nomina/test`.
- Endpoints diagnosticos protegidos con `app.diagnostics.enabled=true`.
- Endpoint duplicado de configuracion Artikos unificado en `/api/v1/dev/artikos/config/{profile}`.

Abordado en Sprint 8.8:

- Documentacion operativa inicial creada en `docs/runbook.md`.
- Consultas SQL de soporte creadas en `docs/sql-queries.md`.
- Guia de escenarios de soporte creada en `docs/support-guide.md`.
- README enlaza la documentacion operacional principal.

Abordado en Sprint 8.9:

- Contrato gateway minimo documentado en `docs/gateway-endpoints.md`.
- `POST /api/v1/nominas/batch/start` queda como unico endpoint productivo inicial para CONC/Kong.
- Endpoints operativos `GET /api/v1/nominas/batch/**` y `GET /api/v1/control-nomina/**` quedan condicionados por `app.endpoints.operations.enabled=true`.
- Headers de trazabilidad de gateway se agregan al MDC sin capturar `Authorization`.

Pendiente:

- Eliminar definitivamente endpoints diagnosticos si el proyecto no los requiere.
- El endpoint administrativo de purga metadata ya queda condicionado por `app.admin.enabled=true`, pero falta protegerlo con autenticacion y autorizacion corporativa.
- Definir si la aplicacion necesitara validaciones defensivas adicionales ademas de la autenticacion/autorizacion aplicada por CONC/Kong.
- Definir si `POST /api/v1/nominas/batch/start` debe exigir siempre body con `profile` o mantener compatibilidad de dry-run sin body.

## Namespace Java

Resuelto en Sprint 8.6.1:

- El paquete base fue migrado desde `cl.poc.atkbatch` hacia `cl.atk.nomina.batch`.
- `docs/decisions/ADR-002-package-namespace.md` queda en estado Accepted.
- Nuevas clases deben mantenerse bajo `cl.atk.nomina.batch`.

## Componentes simulados

Revisar y decidir si se eliminan, se mueven a test fixtures o se protegen por perfil:

- `SimulatedNomina`
- `SimulatedDocumentoContable`
- `NominaItemReader`
- `NominaDocumentoItemReader`
- `NominaItemProcessor`
- `NominaDocumentoItemProcessor`
- `NominaResultItemWriter`

Algunos componentes simulados siguen aportando valor para tests unitarios y escenarios locales, pero no deben confundirse con el flujo operacional real.

## Configuracion y secretos

Abordado parcialmente en Sprint 8.5:

- `application-local.properties` contiene configuracion sensible local y no debe versionarse.
- `application-qa.properties` y `application-prod.properties` usan placeholders sin secretos reales.
- `application-local.example.properties` queda como plantilla segura.
- `docs/secrets.md` documenta nombres logicos, propiedades Spring y nombres sugeridos para Azure Key Vault.
- `app.config.validation.strict=true` queda definido para QA/PROD.

Pendiente:

- Implementar integracion directa con Azure Key Vault si la plataforma no inyecta secretos como variables.
- Definir el mecanismo final de Managed Identity, permisos y rotacion de secretos.
- Revisar que logs y endpoints enmascarados nunca impriman tokens completos.
- Evitar incluir valores reales en archivos de ejemplo.

## Integracion Procurement

Abordado parcialmente en Sprint 9.0:

- Mapper Artikos -> Procurement CMP implementado.
- DTOs Procurement serializan con nombres JSON esperados.
- Campos ASI iniciales quedan configurables por properties.
- `HNR` queda fuera de alcance.

Pendiente:

- Implementar integracion HTTP real con Procurement `POST /api/v1/document`.
- Definir idempotencia de documento Procurement.
- Evaluar endpoint bulk futuro.
- Obtener desde ASI los campos que hoy quedan configurables.
- Validar estructura final de `CMP_DOCUMT_DET_RUT`.
- Validar valores definitivos para `COD_CONTBL`, `COD_TIP_UNID`, `GRL_COD_ITEM` y `NUM_PERIODO`.
- Definir estados funcionales, reintentos y manejo de errores al conectar Procurement al flujo batch.

## Contrato REST y errores

- Definir estructura corporativa de error REST si existe.
- Evaluar si `NominaResultResponse.nomfactresXml` debe seguir expuesto en endpoint productivo o moverse a diagnostico/auditoria.
- Documentar codigos HTTP esperados por endpoint en un documento funcional si el cliente lo exige.

## Skills y automatizacion

Revisar skills, scripts auxiliares y convenciones de operacion antes de consolidar el repositorio como base productiva.
