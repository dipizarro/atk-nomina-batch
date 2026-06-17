# Operational Logging

## Standard Fields

Every operational log line includes MDC fields from Log4j:

- `jobExecutionId`
- `profile`
- `numeroNomina`
- `operation`

The pattern is configured in `src/main/resources/log4j2-spring.xml`.

## Operations

Artikos SOAP operations use these standard operation values:

- `NOMFACTERP`
- `NOMFACTCONFIR`
- `NOMFACTRES`

## Successful Flow Example

```text
INFO ... jobExecutionId=12 profile=VIDA numeroNomina=15960 operation=NOMFACTERP - Artikos nomina received profile=VIDA numeroNomina=15960 tipoNomina=ZSVAYP cantidadDocumentos=1
INFO ... jobExecutionId=12 profile=VIDA numeroNomina=15960 operation=NOMFACTCONFIR - Artikos confirmation OK profile=VIDA numeroNomina=15960
INFO ... jobExecutionId=12 profile=VIDA numeroNomina=15960 operation=NOMFACTRES - Artikos NOMFACTRES OK profile=VIDA jobExecutionId=12 numeroNomina=15960
```

## Functional Error Example

```text
WARN ... jobExecutionId=12 profile=VIDA numeroNomina=15960 operation=NOMFACTRES - Artikos NOMFACTRES error profile=VIDA jobExecutionId=12 numeroNomina=15960 msgStatus=1 message=Solo se pueden procesar nominas en estado "Recibida"
INFO ... jobExecutionId=12 profile= numeroNomina=15960 operation= - CONTROL_NOMINA error updated jobExecutionId=12 numeroNomina=15960 status=ERROR error=NOMFACTRES Artikos rechazado: ...
```

## Technical Error Example

```text
WARN ... jobExecutionId=12 profile=VIDA numeroNomina=15960 operation=NOMFACTCONFIR - Artikos SOAP technical error operation=NOMFACTCONFIR profile=VIDA endpoint=https://... elapsedMs=1200 exceptionClass=ResourceAccessException exceptionMessage=...
```

## Sensitive Data

- Never print complete Artikos tokens.
- Token logs must use `tokenPresent` and `tokenMasked`.
- SOAP XML must not be printed at `INFO`.
- SOAP XML may be printed only at `DEBUG` and only after applying `maskToken(...)`.

## CONTROL_NOMINA Traceability

`jobExecutionId` links Spring Batch metadata, operational logs and `CONTROL_NOMINA`.

`CONTROL_NOMINA` stores one functional status per processed nomina:

- `PROCESSING`
- `OK`
- `NOK`
- `ERROR`

When a batch fails, `CONTROL_NOMINA.ERROR_MESSAGE` keeps the functional or technical error message for the affected `numeroNomina`.
