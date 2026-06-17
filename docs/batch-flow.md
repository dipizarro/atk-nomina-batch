# Flujo Batch

## Inicio

El flujo inicia con:

```http
POST /api/v1/nominas/batch/start
```

Request tipico:

```json
{
  "profile": "VIDA",
  "maxNominas": 1,
  "dryRun": false
}
```

La respuesta es inmediata e incluye `jobExecutionId`, `jobName`, `status`, `profile` y `dryRun`.

## Job

- Job: `nominaDocumentosContablesJob`
- Step real: `processNominaDocumentosStep`
- Chunk real recomendado: `atk.batch.real.chunk-size=1`

## Flujo operacional

1. `ArtikosNominaItemReader` consulta Artikos con `NOMFACTERP`.
2. `ArtikosSoapResponseParser` extrae la nomina desde la respuesta SOAP.
3. `ArtikosNominaItemProcessor` registra `CONTROL_NOMINA` en `PROCESSING`.
4. Si `dryRun=false`, se confirma recepcion con `NOMFACTCONFIR`.
5. `NominaProcessingService` procesa documentos, conciliaciones y distribuciones.
6. `NominaResultXmlService` genera el XML `NOMFACTRES`.
7. `ArtikosNominaResultItemWriter` envia `NOMFACTRES` si `dryRun=false`.
8. `CONTROL_NOMINA` se actualiza con `OK`, `NOK` o `ERROR`.
9. `BatchResultStore` mantiene resultados en memoria para consultas operacionales del job.

## Dry run

Con `dryRun=true`, el servicio consulta Artikos y procesa localmente, pero no confirma recepcion ni envia resultado. Este modo sirve para validar parsing, reglas internas y resumen batch sin alterar estado en Artikos.

## Errores funcionales

Los rechazos SOAP con `MsgStatus=1` se tratan como errores funcionales. El detalle devuelto por Artikos se conserva en la respuesta del endpoint o en `CONTROL_NOMINA.ERROR_MESSAGE`, segun el punto del flujo.

## Estados Artikos relevantes

- `NOMFACTCONFIR` requiere nomina en estado `En Integracion`.
- `NOMFACTRES` requiere nomina en estado `Recibida`.

Una nomina puede seguir apareciendo en `NOMFACTERP` aunque no este lista para confirmacion o resultado.
