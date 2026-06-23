# ASI lookup for Procurement mapping

## Objetivo

El lookup ASI completa campos requeridos por Procurement que no deben mantenerse como properties ni ser inferidos manualmente desde el adapter.

## Tablas usadas

- `ASI.GRL_MAE_ITEM`
- `ASI.GRL_MAE_ITEM_DET`

La aplicacion consulta estas tablas usando Spring Data JPA con entidades de solo lectura logica. No se usa SQL nativo para el lookup.

## Implementacion

Clases principales:

- `GrlMaeItemEntity`
- `GrlMaeItemDetEntity`
- `GrlMaeItemRepository`
- `GrlMaeItemDetRepository`
- `ProcurementMappingLookupService`

El lookup usa el mismo datasource Oracle de la aplicacion, donde tambien viven `CONTROL_NOMINA` y la metadata Spring Batch `BATCH_*`.

## Campos de busqueda actuales

| Campo | Fuente | Uso |
| --- | --- | --- |
| `COD_CUENTA` | `DistribucionContable.codCuentaContable` | Cuenta contable de la linea Artikos. |
| `COD_SISTEM` | `procurement.mapping.cod-sistem` | Filtro operativo actual, default `CM`. |
| `COD_IMPSTO` | Regla adapter | `IVA` si `Monto_Neto > 0`; si no, `EXE`. |
| `A_IND_VIGE` | Constante | Solo registros vigentes `V`. |

El detalle encontrado entrega `COD_EMPRES`, `NUM_PERIODO` y `GRL_COD_ITEM`, que luego se usan para validar que exista maestro vigente en `GRL_MAE_ITEM`.

## Campos obtenidos

| Campo ASI | Uso Procurement |
| --- | --- |
| `GRL_COD_ITEM` | `CMP_DOCUMT_DET.GRL_COD_ITEM` |
| `COD_TIP_UNID` | `CMP_DOCUMT_DET.COD_TIP_UNID` |
| `COD_TIP_CNTA_ITEMS` | `CMP_DOCUMT.COD_TIP_CUENTA` y `CMP_DOCUMT_DET.COD_TIP_CUENTA` |
| `COD_CONTBL` | `CMP_DOCUMT.COD_CONTBL` |
| `COD_SISTEM` | `CMP_DOCUMT.COD_SISTEM` |
| `NUM_PERIODO` | `CMP_DOCUMT.NUM_PERIODO` |
| `COD_MONEDA` | `CMP_DOCUMT.COD_MONEDA` |
| `COD_IMPSTO` | Trazabilidad del mapping aplicado |
| `COD_CUENTA` | Validacion de la cuenta homologada |

## Regla COD_IMPSTO

Regla implementada actualmente:

- `Monto_Neto > 0` -> `IVA`
- `Monto_Neto <= 0` o nulo -> `EXE`

Esta regla queda pendiente de validacion final con negocio. Si negocio confirma que debe depender de `Monto_IVA` o de otra combinacion de montos, debe ajustarse el componente `ProcurementTaxTypeResolver`.

## Validaciones

- Si no hay detalle vigente en `GRL_MAE_ITEM_DET`, el mapper falla con `ProcurementMappingException`.
- Si hay mas de un detalle vigente para la misma cuenta/sistema/impuesto, el mapper falla por ambiguedad.
- Si existe detalle, pero no existe maestro vigente en `GRL_MAE_ITEM`, el mapper falla.
- Si las lineas de un documento devuelven distintos `COD_CONTBL`, el mapper falla para evitar enviar un header inconsistente.

## Fuera de alcance actual

- Validar centro de costo contra catalogo ASI.
- Validar cuenta contra `CON_PLN_CUENTA` antes de llamar Procurement.
- Validar periodo abierto.
- Resolver ambiguedades automaticamente cuando existan multiples mappings vigentes.
