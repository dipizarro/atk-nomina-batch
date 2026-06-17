# ADR-002: Package namespace

## Status

Proposed

## Context

El package base actual del proyecto es:

```text
cl.poc.atkbatch
```

El servicio ya evoluciono desde una prueba inicial hacia una aplicacion batch real para integracion Artikos. El nombre del artefacto y la documentacion principal ya fueron normalizados, pero el namespace Java conserva `poc`.

Las skills ZS revisadas indican que no se debe inventar un dominio corporativo ni cambiar un package base sin confirmacion del lider de integracion o arquitecto.

## Decision

No renombrar masivamente el package base en Sprint 8.6.

La decision actual es mantener `cl.poc.atkbatch` hasta confirmar el namespace corporativo definitivo.

## Consequences

- Se evita un refactor masivo sin confirmacion formal.
- Se reduce el riesgo de romper imports, tests, configuracion de logs y paquetes escaneados por Spring.
- Queda deuda tecnica visible porque el namespace conserva una referencia historica a POC.

## Next Step

Validar con arquitectura o lider de integracion el namespace definitivo antes de migrar.

Opciones a validar:

- `cl.atk.nomina.batch`
- `cl.zurich.artikos.nomina`
- otro namespace corporativo oficial.
