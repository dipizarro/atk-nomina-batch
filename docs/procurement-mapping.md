# Procurement CMP mapping

## Estado actual

Desde Sprint 9.5 el mapper soporta XML Artikos v2 y consulta homologaciones ASI para completar campos Procurement por linea de distribucion.

Fuentes principales:

- XML Artikos v2: `Tipo_ERP`, `Msg_To`, `USO_IVA`, `DocCurrency`, fechas y distribuciones.
- ASI Oracle: `GRL_MAE_ITEM_DET` por `COD_CUENTA`, `COD_SISTEM` y `COD_IMPSTO`, mas validacion de maestro vigente en `GRL_MAE_ITEM`, para resolver `GRL_COD_ITEM`, `COD_TIP_UNID` y `COD_CONTBL`.
- Properties: constantes operativas como `document-type`, `cod-sistem`, `num-periodo`, `cod-tip-cuenta`, descuentos, IVA y cantidad.

Reglas nuevas:

- Raiz `COD_TIP_DOCUMT` sigue siendo `CMP`.
- `CMP_DOCUMT.COD_TIP_DOCUMT` se obtiene desde `Tipo_ERP`: `FEC/33`, `FCE/34`, `NDC/56`, `ECC/61`.
- `CMP_DOCUMT.COD_EMPRES` se resuelve desde `Msg_To`: `001/ZSGVIDA` -> `001`, `002/ZSGRALES` -> `002`; si falta, se usa fallback por profile.
- `CMP_DOCUMT.CODIGO_REC_IVA` se obtiene desde `USO_IVA`; valores permitidos `U`, `R`, `N`; si viene vacio, default `U`.
- `DocCurrency=CLP` o `$` se envia como `$`; otra moneda usa `procurement.mapping.default-currency`.
- Cada distribucion consulta `GRL_MAE_ITEM_DET` por `COD_CUENTA`, `COD_SISTEM=CM` y `COD_IMPSTO`. Si `Monto_Neto > 0`, `COD_IMPSTO=IVA`; si `Monto_Neto = 0`, `COD_IMPSTO=EXE`. Luego valida el maestro vigente en `GRL_MAE_ITEM` usando `COD_EMPRES`, `NUM_PERIODO` y `GRL_COD_ITEM` devueltos por el detalle.
- Si las lineas devuelven distintos `COD_CONTBL`, el mapper falla con `ProcurementMappingException`.

## Alcance Sprint 9.0

Sprint 9.0 implementa el mapper Artikos -> Procurement CMP.

Sprint 9.1 agrega el cliente HTTP configurable para consumir Procurement, pero todavia no lo integra al flujo batch.

Endpoint objetivo futuro:

```http
POST /api/v1/document
```

Decisiones confirmadas:

- Solo se genera `CMP`.
- `HNR` queda fuera de alcance y se envia como `null`.
- No se implementa bulk.
- El alcance inicial no consultaba ASI; desde Sprint 9.5 el lookup ASI queda integrado para homologaciones de item.
- No se modifica el flujo SOAP Artikos.
- No se modifica `NOMFACTRES`.

## Estructura JSON

```json
{
  "COD_TIP_DOCUMT": "CMP",
  "CMP": {
    "CMP_DOCUMT": {},
    "CMP_DOCUMT_DET": [],
    "CMP_DOCUMT_DET_RUT": {}
  },
  "HNR": null
}
```

## Estrategia inicial

La estrategia inicial es uno a uno:

- un `DocumentoContable` Artikos genera un request Procurement CMP;
- cada `DistribucionContable` Artikos genera una linea `CMP_DOCUMT_DET`;
- `CMP_DOCUMT_DET_RUT` se completa con el RUT proveedor y vigencia activa para cumplir el contrato real de Procurement.

## Campos por profile

| Profile Artikos | COD_EMPRES |
| --- | --- |
| `GENERALES` | `002` |
| `VIDA` | `001` |

La configuracion vive en:

```properties
procurement.mapping.company-by-profile.GENERALES=002
procurement.mapping.company-by-profile.VIDA=001
```

## Campos constantes

| Procurement | Valor |
| --- | --- |
| Raiz `COD_TIP_DOCUMT` | `CMP` |
| `CMP_DOCUMT.COD_TIP_DOCUMT` | `FEC` |
| `CMP_DOCUMT.COD_SISTEM` | `CM` |
| `CMP_DOCUMT.COD_TIP_CUENTA` | `2` por defecto |
| `CMP_DOCUMT_DET.COD_TIP_CUENTA` | `2` por defecto |
| `CMP_DOCUMT_DET_RUT.A_IND_VIGE` | `V` |
| `HNR` | `null` |

Aunque Artikos trae `Tipo_Documento` y `Tipo_ERP`, para esta integracion la raiz indica el tipo de payload `CMP`, mientras `CMP_DOCUMT.COD_TIP_DOCUMT` identifica el tipo interno `FEC`.

## Campos directos desde Artikos

| Artikos | Procurement | Regla |
| --- | --- | --- |
| `Documento.Rut_Proveedor` | `CMP_DOCUMT.NUM_RUT` | RUT sin digito verificador. Ejemplo: `96670840-9` -> `96670840`. |
| `Documento.Rut_Proveedor` | `CMP_DOCUMT_DET_RUT.CMP_NUM_RUT` | Se envia el mismo RUT proveedor sin digito verificador. |
| `Documento.Rut_Proveedor` | `CMP_DOCUMT_DET_RUT.NUM_RUT` | Se envia el mismo RUT proveedor sin digito verificador. |
| `Documento.Numero_Documento` | `CMP_DOCUMT.NUM_DOCCMP` | Valor directo. |
| `Documento.Fecha_Emision` | `CMP_DOCUMT.FEC_EMIDCM` | Formato de salida `yyyy-MM-dd`. |
| `Documento.Fecha_Emision` | `CMP_DOCUMT.FEC_COMPRB` | Regla temporal hasta confirmar fuente ASI. |
| `Documento.Fecha_Vencimiento` | `CMP_DOCUMT.FEC_VNCCTA` | Si no viene, usar `Fecha_Emision`. |
| `Documento.Fecha_Recepcion` | `CMP_DOCUMT.FECHA_REC_FE` | Formato de salida `yyyy-MM-dd`. |
| `Documento.Monto_Neto` | `CMP_DOCUMT.MTO_TOT_NTODIG` | Valor directo, `0` si viene nulo. |
| `Documento.Monto_Exento` | `CMP_DOCUMT.MTO_TOT_EXNDIG` | Valor directo, `0` si viene nulo. |
| `Documento.Monto_IVA` | `CMP_DOCUMT.MTO_TOT_IVADIG` | Valor directo, `0` si viene nulo. |
| `Documento.Monto_Total` | `CMP_DOCUMT.MTO_TOT_DOCDIG` | Valor directo, `0` si viene nulo. |
| `Documento.Numero_Documento` | `CMP_DOCUMT.NUM_FOL_DOCUMT` | Valor numerico del folio/documento. |
| `Distribucion.Cod_CuentaContable` | `CMP_DOCUMT.COD_CUENTA` | Primera cuenta contable disponible en distribuciones. |
| `Distribucion.Cod_CuentaContable` | `CMP_DOCUMT_DET.COD_CUENTA` | Valor directo por linea; requerido por `ASI.CMP_DOCUMT_DET`. |
| `Distribucion.Cod_CentroCosto` | `CMP_DOCUMT_DET.COD_CCOSTO` | Valor directo. |
| `Distribucion.Monto_Neto` | `CMP_DOCUMT_DET.MTO_NETO` | Valor directo, `0` si viene nulo. |
| `Distribucion.Monto_Exento` | `CMP_DOCUMT_DET.MTO_EXENTO` | Valor directo, `0` si viene nulo. |
| `Distribucion.Monto_IVA` | `CMP_DOCUMT_DET.MTO_IVACLC` | Valor directo, `0` si viene nulo. |
| `Distribucion.Monto_Total` | `CMP_DOCUMT_DET.MTO_TOT_ITEM` | Valor directo, `0` si viene nulo. |

## Campos configurables por properties

| Property | Procurement | Nota |
| --- | --- | --- |
| `procurement.mapping.document-type` | Raiz `COD_TIP_DOCUMT` | Default `CMP`. |
| `procurement.mapping.cmp-document-type` | `CMP_DOCUMT.COD_TIP_DOCUMT` | Default `FEC`. |
| `procurement.mapping.cod-sistem` | `CMP_DOCUMT.COD_SISTEM` | Default `CM`. |
| `procurement.mapping.num-periodo` | `CMP_DOCUMT.NUM_PERIODO` | Debe validarse contra periodo abierto ASI. |
| `procurement.mapping.cod-contbl` | `CMP_DOCUMT.COD_CONTBL` | Pendiente confirmar valor real. |
| `procurement.mapping.cod-tip-cuenta` | `CMP_DOCUMT.COD_TIP_CUENTA`, `CMP_DOCUMT_DET.COD_TIP_CUENTA` | Default `2`, alineado con el helper actual de Procurement. |
| `procurement.mapping.cod-tip-unid` | `CMP_DOCUMT_DET.COD_TIP_UNID` | Pendiente confirmar valor real. |
| `procurement.mapping.grl-cod-item` | `CMP_DOCUMT_DET.GRL_COD_ITEM` | Pendiente confirmar valor real. |
| `procurement.mapping.line-gloss` | `CMP_DOCUMT_DET.GLS_LINEA` | Default `BENEFICIOS AL PERSONAL`. Siempre se usa este valor para respetar largo maximo Oracle. |
| `procurement.mapping.val-tip-cambio` | `CMP_DOCUMT_DET.VAL_TIP_CAMBIO` | Default `1`. |
| `procurement.mapping.pct-dscnto` | `CMP_DOCUMT_DET.PCT_DSCNTO` | Default `0`. |
| `procurement.mapping.mto-dscnto` | `CMP_DOCUMT_DET.MTO_DSCNTO` | Default `0`. |
| `procurement.mapping.pct-iva` | `CMP_DOCUMT_DET.PCT_IVA` | Default `19`. |
| `procurement.mapping.codigo-rec-iva` | `CMP_DOCUMT.CODIGO_REC_IVA` | Pendiente confirmar valor real. |
| `procurement.mapping.default-cantidad` | `CMP_DOCUMT_DET.NUM_CANTDD` | Default `1`. |
| `procurement.mapping.default-currency` | `CMP_DOCUMT.COD_MONEDA` | Default `CLP`. Siempre se usa este valor, aunque Artikos informe `DocCurrency`. |

Si falta una property obligatoria, el mapper lanza `ProcurementMappingException` con el nombre de la property.

## Campos candidatos a consulta ASI futura

Esta seccion queda como referencia historica. Desde Sprint 9.5 ya se resuelven desde ASI:

- `COD_CONTBL`
- `COD_TIP_UNID`
- `GRL_COD_ITEM`

Siguen pendientes de definicion funcional:

- `NUM_PERIODO`
- Validacion de cuenta contable y centro de costo
- Periodo abierto

## Pendientes

- Validar valor definitivo de `COD_CONTBL`.
- Validar valor definitivo de `COD_TIP_CUENTA`.
- Validar valor definitivo de `COD_TIP_UNID`.
- Validar valor definitivo de `GRL_COD_ITEM`.
- Validar `NUM_PERIODO` contra periodo abierto ASI.
- Definir idempotencia para documentos enviados a Procurement.
- Definir estrategia de reintentos y errores al consumir `POST /api/v1/document`.
- Evaluar endpoint bulk en sprint posterior.

## HTTP client

El cliente HTTP Procurement vive en `ProcurementClient` y usa `RestClient`.

Properties:

```properties
procurement.client.enabled=false
procurement.client.base-url=
procurement.client.document-path=/api/v1/document
procurement.client.connect-timeout-ms=5000
procurement.client.read-timeout-ms=30000
```

Reglas iniciales:

- `enabled=false`: el cliente falla de forma controlada al intentar usarlo.
- HTTP `2xx` con `statusCode=0`: resultado exitoso.
- HTTP `2xx` con `statusCode!=0`: NOK funcional.
- HTTP `4xx` con body parseable: NOK funcional.
- HTTP `5xx`: error tecnico.
- Timeout/conexion: error tecnico.

El cliente no loguea el JSON completo en `INFO`. Request y response completos quedan reservados para `DEBUG`.
