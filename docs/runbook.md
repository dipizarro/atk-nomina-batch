# Runbook operativo

## Proposito del servicio

`atk-nomina-batch` procesa nominas de documentos contables disponibles en Artikos. El servicio consulta nominas con `NOMFACTERP`, confirma recepcion con `NOMFACTCONFIR`, procesa documentos localmente o contra Procurement segun configuracion, envia resultados con `NOMFACTRES` y registra el control funcional en `CONTROL_NOMINA`.

La metadata tecnica del job queda en las tablas Spring Batch `BATCH_*`.

## Flujo funcional resumido

1. Un operador o scheduler inicia el batch por REST.
2. El reader consulta `NOMFACTERP`.
3. Si Artikos devuelve una nomina, el batch procesa esa nomina como item.
4. El processor confirma recepcion con `NOMFACTCONFIR`.
5. El processor valida los documentos de la nomina.
6. El writer envia un `NOMFACTRES` por nomina.
7. El writer actualiza `CONTROL_NOMINA`.
8. El reader vuelve a consultar Artikos.
9. Cuando Artikos responde `No hay nominas para procesar`, el step termina normalmente.

`maxNominas` no es la condicion funcional de termino. Es solo un limite operativo de seguridad para evitar ejecuciones demasiado largas.

## Perfiles soportados

- `VIDA`
- `GENERALES`

Cada perfil usa configuracion Artikos propia: token, direcciones, RUT emisor y operaciones SOAP.

## Como iniciar el batch

Endpoint:

```http
POST /api/v1/nominas/batch/start
```

Ejemplo:

```json
{
  "profile": "GENERALES",
  "dryRun": false
}
```

Ejemplo con limite operativo:

```json
{
  "profile": "VIDA",
  "maxNominas": 10,
  "dryRun": false
}
```

Respuesta esperada:

```json
{
  "jobExecutionId": 123,
  "jobName": "nominaDocumentosContablesJob",
  "status": "STARTING",
  "message": "Batch iniciado correctamente",
  "profile": "GENERALES",
  "maxNominas": 50,
  "dryRun": false
}
```

Notas operativas:

- El endpoint responde inmediatamente.
- El job sigue ejecutando en segundo plano.
- El proceso consulta Artikos hasta que no haya mas nominas.
- La cantidad de documentos por nomina es variable.
- Cada nomina procesada genera una fila en `CONTROL_NOMINA`.
- Cada nomina procesada genera un `NOMFACTRES`.
- Si existe una ejecucion activa para el mismo perfil, el endpoint responde HTTP `409`.
- Si `maxNominas` supera `atk.batch.max-nominas-per-run`, el endpoint responde HTTP `400`.

## Como consultar estado del batch

Endpoint:

```http
GET /api/v1/nominas/batch/{jobExecutionId}
```

En QA/PROD este endpoint puede no estar disponible porque los endpoints operativos se cargan solo si:

```properties
app.endpoints.operations.enabled=true
```

El seguimiento normal en QA/PROD debe realizarse por logs y Oracle usando las consultas de `docs/sql-queries.md`.

Revisar principalmente:

- `status`
- `exitCode`
- `exitDescription`
- `startTime`
- `endTime`

Estados esperados:

- `STARTING`: el job fue solicitado y esta iniciando.
- `STARTED`: el job esta ejecutando.
- `COMPLETED`: el job termino correctamente.
- `FAILED`: el job fallo por error tecnico, rechazo Artikos u Oracle.
- `STOPPING` o `STOPPED`: detencion controlada.

## Como consultar summary

Endpoint:

```http
GET /api/v1/nominas/batch/{jobExecutionId}/summary
```

En QA/PROD este endpoint no queda disponible por defecto. Para soporte interno puede habilitarse temporalmente con `app.endpoints.operations.enabled=true`, solo con autorizacion operativa.

El summary consolida resultados funcionales del job:

- total de nominas procesadas;
- total de documentos;
- total OK;
- total NOK;
- total conciliaciones;
- total distribuciones;
- errores asociados.

Los totales se calculan segun el XML real recibido desde Artikos. No se asume una cantidad fija de documentos, conciliaciones o distribuciones.

## Como consultar resultado por nomina

Endpoint:

```http
GET /api/v1/nominas/batch/{jobExecutionId}/results/{numeroNomina}
```

Usar este endpoint para revisar el resultado funcional de una nomina especifica dentro de una ejecucion.

## Como revisar CONTROL_NOMINA

Endpoints productivos:

```http
GET /api/v1/control-nomina/jobs/{jobExecutionId}
GET /api/v1/control-nomina/jobs/{jobExecutionId}/nominas/{numeroNomina}
```

En QA/PROD estos endpoints no quedan disponibles por defecto. Usar SQL sobre Oracle como mecanismo normal de soporte. Las consultas estan en `docs/sql-queries.md`.

`CONTROL_NOMINA` es la tabla funcional de control por nomina. Debe revisarse cuando:

- el job queda `FAILED`;
- una nomina queda `ERROR`;
- se necesita validar cuantas nominas fueron procesadas;
- se quiere confirmar si una nomina quedo `OK` o `NOK`.

Estados funcionales:

- `PROCESSING`: nomina tomada para procesamiento.
- `OK`: nomina procesada y resultado enviado sin documentos NOK.
- `NOK`: nomina procesada y resultado enviado con documentos NOK.
- `ERROR`: error tecnico o rechazo que impidio cerrar la nomina correctamente.

## Como revisar metadata Spring Batch

Las tablas `BATCH_*` son metadata tecnica. Usarlas para revisar:

- ejecuciones recientes;
- parametros usados;
- estado del job;
- steps ejecutados;
- stacktrace o exit message compacto.

Tablas principales:

- `BATCH_JOB_INSTANCE`
- `BATCH_JOB_EXECUTION`
- `BATCH_JOB_EXECUTION_PARAMS`
- `BATCH_JOB_EXECUTION_CONTEXT`
- `BATCH_STEP_EXECUTION`
- `BATCH_STEP_EXECUTION_CONTEXT`

Las consultas sugeridas estan en `docs/sql-queries.md`.

## Como interpretar estados

| Capa | Estado | Significado |
| --- | --- | --- |
| Spring Batch | `COMPLETED` | El job termino sin error tecnico. Puede ser porque no habia mas nominas o porque alcanzo `maxNominas`. |
| Spring Batch | `FAILED` | El job fallo por error tecnico, rechazo Artikos u Oracle. |
| `CONTROL_NOMINA` | `OK` | Nomina procesada correctamente. |
| `CONTROL_NOMINA` | `NOK` | Nomina procesada con observaciones funcionales en documentos. |
| `CONTROL_NOMINA` | `ERROR` | Nomina no pudo cerrarse correctamente. |
| Artikos | `MsgStatus=0` | Operacion aceptada. |
| Artikos | `MsgStatus!=0` | Respuesta funcional rechazada o con observacion. Revisar `MessageOut.LogMessage.MessageText`. |

## Que hacer ante errores

1. Consultar estado del job por REST.
2. Revisar `BATCH_JOB_EXECUTION.EXIT_MESSAGE`.
3. Buscar logs por `jobExecutionId`.
4. Revisar `CONTROL_NOMINA` para el job.
5. Si hay `numeroNomina`, revisar detalle de esa nomina.
6. Clasificar si el error es:
   - Artikos `NOMFACTERP`;
   - Artikos `NOMFACTCONFIR`;
   - Artikos `NOMFACTRES`;
   - XML o parseo;
   - Oracle;
   - concurrencia operativa.
7. Aplicar el escenario correspondiente en `docs/support-guide.md`.

## Como purgar metadata

Endpoint:

```http
POST /api/v1/admin/batch-metadata/purge
```

Este endpoint solo se carga si:

```properties
app.admin.enabled=true
```

Primero ejecutar siempre en modo simulacion:

```json
{
  "retentionDays": 30,
  "dryRun": true,
  "includeFailed": false
}
```

Si el resultado es correcto y existe autorizacion operativa, ejecutar:

```json
{
  "retentionDays": 30,
  "dryRun": false,
  "includeFailed": false
}
```

Reglas:

- Nunca purga ejecuciones activas.
- Por defecto no purga `FAILED`.
- `FAILED` solo se considera con `includeFailed=true`.
- Solo afecta tablas `BATCH_*`.
- No elimina registros de `CONTROL_NOMINA`.

## Como revisar logs

Los logs usan contexto MDC:

- `jobExecutionId`
- `profile`
- `numeroNomina`
- `operation`

Operaciones Artikos:

- `NOMFACTERP`
- `NOMFACTCONFIR`
- `NOMFACTRES`

Buscar primero por:

```text
jobExecutionId=123
numeroNomina=15960
operation=NOMFACTRES
```

Los tokens no deben aparecer completos. Si se detecta un token completo en logs, se debe tratar como incidente de seguridad.

## Consideraciones de seguridad

- No versionar `application-local.properties`.
- No subir tokens, passwords ni connection strings.
- Mantener `app.diagnostics.enabled=false` en produccion.
- Mantener `app.admin.enabled=false` salvo que exista control corporativo de acceso.
- El endpoint de purga debe protegerse con autenticacion y autorizacion antes de uso productivo.
- Los secretos deben resolverse mediante Azure Key Vault, variables de entorno seguras o mecanismo administrado equivalente.

## Checklist de operacion diaria

- Verificar `/actuator/health`.
- Confirmar que Oracle esta disponible.
- Confirmar que no hay jobs activos inesperados.
- Iniciar batch para el perfil requerido.
- Registrar `jobExecutionId`.
- Monitorear logs por `jobExecutionId`.
- Confirmar estado final `COMPLETED`.
- Revisar summary.
- Revisar `CONTROL_NOMINA` por status.
- Escalar si hay registros `ERROR` o job `FAILED`.

## Checklist post-error

- Registrar `jobExecutionId`, `profile` y `numeroNomina` si existe.
- Consultar `GET /api/v1/nominas/batch/{jobExecutionId}`.
- Consultar `CONTROL_NOMINA`.
- Revisar `BATCH_JOB_EXECUTION.EXIT_MESSAGE`.
- Revisar logs filtrando por `jobExecutionId`.
- Identificar operacion fallida: `NOMFACTERP`, `NOMFACTCONFIR`, `NOMFACTRES` u Oracle.
- Revisar si hubo retry tecnico.
- Confirmar si el error es funcional Artikos o tecnico.
- No reintentar manualmente sin confirmar el estado de la nomina en Artikos.
- Documentar accion tomada y resultado.
