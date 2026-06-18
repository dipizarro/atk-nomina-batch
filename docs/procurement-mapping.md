# Procurement CMP mapping

## Alcance Sprint 9.0

Este sprint implementa el mapper Artikos -> Procurement CMP. No consume el endpoint real de Procurement.

Endpoint objetivo futuro:

```http
POST /api/v1/document
```

Decisiones confirmadas:

- Solo se genera `CMP`.
- `HNR` queda fuera de alcance y se envia como `null`.
- No se implementa bulk.
- No se consulta ASI.
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
- `CMP_DOCUMT_DET_RUT` queda como estructura minima hasta validar contrato final con Procurement.

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
| `CMP_DOCUMT.COD_TIP_DOCUMT` | `CMP` |
| `CMP_DOCUMT.COD_SISTEM` | `CM` |
| `HNR` | `null` |

Aunque Artikos trae `Tipo_Documento` y `Tipo_ERP`, para esta integracion se mantiene `CMP` como tipo de documento Procurement.

## Campos directos desde Artikos

| Artikos | Procurement | Regla |
| --- | --- | --- |
| `Documento.Rut_Proveedor` | `CMP_DOCUMT.NUM_RUT` | RUT sin digito verificador. Ejemplo: `96670840-9` -> `96670840`. |
| `Documento.Numero_Documento` | `CMP_DOCUMT.NUM_DOCCMP` | Valor directo. |
| `Documento.DocCurrency` | `CMP_DOCUMT.COD_MONEDA` | Si no viene, usar `procurement.mapping.default-currency`. |
| `Documento.Fecha_Emision` | `CMP_DOCUMT.FEC_EMIDCM` | Formato de salida `yyyy-MM-dd`. |
| `Documento.Fecha_Emision` | `CMP_DOCUMT.FEC_COMPRB` | Regla temporal hasta confirmar fuente ASI. |
| `Documento.Fecha_Vencimiento` | `CMP_DOCUMT.FEC_VNCCTA` | Si no viene, usar `Fecha_Emision`. |
| `Documento.Fecha_Recepcion` | `CMP_DOCUMT.FECHA_REC_FE` | Formato de salida `yyyy-MM-dd`. |
| `Documento.Monto_Neto` | `CMP_DOCUMT.MTO_TOT_NTODIG` | Valor directo, `0` si viene nulo. |
| `Documento.Monto_Exento` | `CMP_DOCUMT.MTO_TOT_EXNDIG` | Valor directo, `0` si viene nulo. |
| `Documento.Monto_IVA` | `CMP_DOCUMT.MTO_TOT_IVADIG` | Valor directo, `0` si viene nulo. |
| `Distribucion.Cod_CuentaContable` | `CMP_DOCUMT.COD_CUENTA` | Primera cuenta contable disponible en distribuciones. |
| `Distribucion.Cod_CentroCosto` | `CMP_DOCUMT_DET.COD_CCOSTO` | Valor directo. |
| `Distribucion.ItemDescription` | `CMP_DOCUMT_DET.GLS_LINEA` | Si no viene, usar glosa de documento. |
| `Distribucion.Monto_Neto` | `CMP_DOCUMT_DET.MTO_NETO` | Valor directo, `0` si viene nulo. |
| `Distribucion.Monto_Exento` | `CMP_DOCUMT_DET.MTO_EXENTO` | Valor directo, `0` si viene nulo. |
| `Distribucion.Monto_IVA` | `CMP_DOCUMT_DET.MTO_IVACLC` | Valor directo, `0` si viene nulo. |
| `Distribucion.Monto_Total` | `CMP_DOCUMT_DET.MTO_TOT_ITEM` | Valor directo, `0` si viene nulo. |

## Campos configurables por properties

| Property | Procurement | Nota |
| --- | --- | --- |
| `procurement.mapping.document-type` | `COD_TIP_DOCUMT`, `CMP_DOCUMT.COD_TIP_DOCUMT` | Default `CMP`. |
| `procurement.mapping.cod-sistem` | `CMP_DOCUMT.COD_SISTEM` | Default `CM`. |
| `procurement.mapping.num-periodo` | `CMP_DOCUMT.NUM_PERIODO` | Debe validarse contra periodo abierto ASI. |
| `procurement.mapping.cod-contbl` | `CMP_DOCUMT.COD_CONTBL` | Pendiente confirmar valor real. |
| `procurement.mapping.cod-tip-unid` | `CMP_DOCUMT_DET.COD_TIP_UNID` | Pendiente confirmar valor real. |
| `procurement.mapping.grl-cod-item` | `CMP_DOCUMT_DET.GRL_COD_ITEM` | Pendiente confirmar valor real. |
| `procurement.mapping.val-tip-cambio` | `CMP_DOCUMT_DET.VAL_TIP_CAMBIO` | Default `1`. |
| `procurement.mapping.pct-dscnto` | `CMP_DOCUMT_DET.PCT_DSCNTO` | Default `0`. |
| `procurement.mapping.mto-dscnto` | `CMP_DOCUMT_DET.MTO_DSCNTO` | Default `0`. |
| `procurement.mapping.pct-iva` | `CMP_DOCUMT_DET.PCT_IVA` | Default `19`. |
| `procurement.mapping.codigo-rec-iva` | `CMP_DOCUMT.CODIGO_REC_IVA` | Pendiente confirmar valor real. |
| `procurement.mapping.default-cantidad` | `CMP_DOCUMT_DET.NUM_CANTDD` | Default `1`. |
| `procurement.mapping.default-currency` | `CMP_DOCUMT.COD_MONEDA` | Default `CLP`. |

Si falta una property obligatoria, el mapper lanza `ProcurementMappingException` con el nombre de la property.

## Campos candidatos a consulta ASI futura

- `NUM_PERIODO`
- `COD_CONTBL`
- `COD_TIP_UNID`
- `GRL_COD_ITEM`
- `CODIGO_REC_IVA`
- Validacion de cuenta contable y centro de costo
- Periodo abierto

## Pendientes

- Validar contrato final de `CMP_DOCUMT_DET_RUT`.
- Validar valor definitivo de `COD_CONTBL`.
- Validar valor definitivo de `COD_TIP_UNID`.
- Validar valor definitivo de `GRL_COD_ITEM`.
- Validar `NUM_PERIODO` contra periodo abierto ASI.
- Definir idempotencia para documentos enviados a Procurement.
- Definir estrategia de reintentos y errores al consumir `POST /api/v1/document`.
- Evaluar endpoint bulk en sprint posterior.
