# Deuda Tecnica

## Endpoints diagnosticos

Pendiente para Sprint 8.2:

- Mover endpoints Artikos de diagnostico bajo `/api/v1/dev/artikos/...` o protegerlos por perfil `local`/`qa`.
- Mover endpoints `CONTROL_NOMINA` diagnosticos bajo `/api/v1/dev/control-nomina/...` o eliminarlos.
- Unificar los endpoints duplicados de configuracion Artikos enmascarada:
  - `/api/v1/artikos/qa/nominas/config/{profile}`
  - `/api/v1/artikos/qa/config/{profile}`
- Proteger endpoint administrativo de purga metadata con autenticacion y autorizacion.

## Namespace Java

El paquete base actual `cl.poc.atkbatch` conserva la historia inicial del proyecto. Debe migrarse a un namespace productivo, por ejemplo:

- `cl.atk.nomina.batch`
- `cl.zurich.artikos.nomina`

Este cambio afecta imports, paquetes, tests, configuracion de logs y posiblemente reglas de analisis estatico, por lo que se deja fuera de Sprint 8.1.

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

- `application-local.properties` contiene configuracion sensible local y no debe versionarse.
- Externalizar secretos hacia variables de entorno, vault corporativo o mecanismo administrado por plataforma.
- Revisar que logs y endpoints enmascarados nunca impriman tokens completos.
- Evitar incluir valores reales en archivos de ejemplo.

## Integracion Procurement

La integracion posterior con Procurement queda pendiente. Debe definirse contrato, responsabilidades, estados funcionales, reintentos y manejo de errores antes de conectar el flujo productivo.

## Skills y automatizacion

Revisar skills, scripts auxiliares y convenciones de operacion antes de consolidar el repositorio como base productiva.
