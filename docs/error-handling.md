# Error handling policy

La aplicacion distingue errores tecnicos de integracion, rechazos funcionales de Artikos y resultados NOK de documentos. La regla general es detener el job cuando no se puede garantizar consistencia con Artikos o con `CONTROL_NOMINA`.

## Estados

| Caso | Estado job | CONTROL_NOMINA | Accion |
| --- | --- | --- | --- |
| No hay nominas | COMPLETED | no aplica | Termina normal |
| Se alcanza maxNominas | COMPLETED | filas procesadas hasta el limite | Termina normal con log de limite operacional |
| Fetch SOAP error | FAILED | no aplica | Detener |
| Confirm error | FAILED | ERROR | Detener |
| Documento NOK | COMPLETED | NOK | Informar resultado |
| NOMFACTRES error | FAILED | ERROR | Detener |
| Oracle error | FAILED | incierto | Detener |

## Tipos de error

Los errores de integracion se clasifican con `IntegrationErrorType`:

- `ARTIKOS_FETCH_ERROR`: falla tecnica consultando `NOMFACTERP`.
- `ARTIKOS_NO_NOMINAS`: Artikos responde correctamente, pero no hay nominas disponibles.
- `XML_PARSING_ERROR`: la respuesta SOAP no puede parsearse como nomina valida.
- `NOMINA_CONFIRM_ERROR`: Artikos rechaza `NOMFACTCONFIR`.
- `NOMINA_PROCESSING_ERROR`: falla interna procesando la nomina.
- `NOMINA_RESULT_ERROR`: Artikos rechaza `NOMFACTRES` o falla su envio.
- `ORACLE_CONTROL_ERROR`: falla persistiendo estado funcional en `CONTROL_NOMINA`.
- `UNKNOWN_ERROR`: fallback para errores no clasificados.

## Termino controlado

El job termina `COMPLETED` cuando Artikos responde que no hay mas nominas para procesar. Tambien termina `COMPLETED` si se alcanza `maxNominas`, porque ese parametro es un limite operativo de seguridad y no un error funcional.

El job termina `FAILED` ante errores tecnicos de fetch, rechazo de confirmacion, rechazo o falla de `NOMFACTRES`, y errores Oracle que impiden registrar o actualizar `CONTROL_NOMINA`.

## Respuestas REST

Los endpoints de estado y resumen de batch exponen `exitDescription` y `error` compactados. No se devuelve stacktrace completo en la respuesta REST; la traza detallada queda en logs y metadata Spring Batch.

## CONTROL_NOMINA

`CONTROL_NOMINA` registra el estado funcional por nomina:

- `PROCESSING`: nomina tomada para procesamiento.
- `OK`: documentos procesados sin NOK y resultado enviado correctamente.
- `NOK`: documentos procesados con observaciones funcionales, resultado enviado correctamente.
- `ERROR`: error que detuvo confirmacion, procesamiento tecnico o envio de resultado.

Los mensajes de error se truncan a 500 caracteres para respetar el largo de la columna Oracle.
