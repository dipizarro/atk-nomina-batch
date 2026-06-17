# Deuda Tecnica

## Endpoints diagnosticos

Resuelto parcialmente en Sprint 8.2:

- Endpoints Artikos de diagnostico movidos bajo `/api/v1/dev/artikos/...`.
- Endpoint `CONTROL_NOMINA` de prueba movido bajo `/api/v1/dev/control-nomina/test`.
- Endpoints diagnosticos protegidos con `app.diagnostics.enabled=true`.
- Endpoint duplicado de configuracion Artikos unificado en `/api/v1/dev/artikos/config/{profile}`.

Pendiente:

- Eliminar definitivamente endpoints diagnosticos si el proyecto no los requiere.
- Proteger endpoint administrativo de purga metadata con autenticacion y autorizacion.
- Definir mecanismo de autenticacion/autorizacion para endpoints productivos.
- Definir si `POST /api/v1/nominas/batch/start` debe exigir siempre body con `profile` o mantener compatibilidad de dry-run sin body.

## Namespace Java

El paquete base actual `cl.poc.atkbatch` conserva la historia inicial del proyecto. Debe migrarse a un namespace productivo, por ejemplo:

- `cl.atk.nomina.batch`
- `cl.zurich.artikos.nomina`

Este cambio afecta imports, paquetes, tests, configuracion de logs y posiblemente reglas de analisis estatico, por lo que se deja fuera de Sprint 8.1 y Sprint 8.2.

Sprint 8.6 agrega `docs/decisions/ADR-002-package-namespace.md` y mantiene este cambio como REQUIERE VALIDACION hasta confirmar el namespace corporativo definitivo.

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

La integracion posterior con Procurement queda pendiente para el final del roadmap. Debe definirse contrato, responsabilidades, estados funcionales, reintentos y manejo de errores antes de conectar el flujo productivo.

## Contrato REST y errores

- Definir estructura corporativa de error REST si existe.
- Evaluar si `NominaResultResponse.nomfactresXml` debe seguir expuesto en endpoint productivo o moverse a diagnostico/auditoria.
- Documentar codigos HTTP esperados por endpoint en un documento funcional si el cliente lo exige.

## Skills y automatizacion

Revisar skills, scripts auxiliares y convenciones de operacion antes de consolidar el repositorio como base productiva.
