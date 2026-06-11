# ADR-002: Resultados batch en memoria para la POC

## Estado

Aceptada

## Contexto

El Sprint 3 necesita exponer un resumen de procesamiento masivo simulado sin incorporar todavia una base de datos propia para resultados funcionales. Spring Batch mantiene su metadata tecnica en H2, pero los totales de documentos, conciliaciones y distribuciones son informacion de salida de la POC.

## Decision

Los resultados funcionales del procesamiento se guardaran temporalmente en un store en memoria, indexado por `jobExecutionId`. El writer agrega resultados por chunk y el job limpia el store al iniciar una nueva ejecucion.

## Consecuencias

- La implementacion se mantiene local y simple.
- El endpoint de summary puede consultar resultados sin crear tablas propias.
- Los resultados se pierden al reiniciar la aplicacion.
- En una etapa posterior, este store deberia reemplazarse por persistencia transaccional si se requiere auditoria o consulta historica.
