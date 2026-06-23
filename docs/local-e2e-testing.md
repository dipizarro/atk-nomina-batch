# Local XML end-to-end testing

## Objetivo

El modo `local-xml` permite ejecutar el batch usando un XML Artikos local como fuente de nomina cuando QA Artikos no tiene nominas disponibles para `NOMFACTERP`.

Este modo valida la cadena tecnica:

```text
XML local -> parser -> processor -> Procurement -> ResultadoNomina -> NOMFACTRES generado -> CONTROL_NOMINA
```

## Que no prueba

- No consulta `NOMFACTERP` real.
- No envia `NOMFACTCONFIR` real.
- No envia `NOMFACTRES` real.
- No valida reglas funcionales finales de Artikos QA.

## Properties

Para activar el modo local:

```properties
artikos.source.mode=local-xml
artikos.source.local-xml-path=classpath:samples/ZSGRALES_Nom15961_v2.xml
artikos.confirm.enabled=false
artikos.result.enabled=false
```

Tambien puede usarse el XML historico, pero requiere que ASI tenga homologada la cuenta `6130401000`:

```properties
artikos.source.local-xml-path=classpath:samples/artikos/ZSVIDA_Nom15960.xml
```

Aunque `artikos.confirm.enabled` o `artikos.result.enabled` queden en `true`, la aplicacion fuerza skip de llamadas Artikos reales cuando `artikos.source.mode=local-xml`.

Para llamar Procurement real durante la prueba:

```properties
procurement.client.enabled=true
procurement.integration.enabled=true
procurement.client.base-url=http://localhost:8081
```

Desde Sprint 9.5 el mapper Procurement consulta `ASI.GRL_MAE_ITEM` y `ASI.GRL_MAE_ITEM_DET` para homologar item por distribucion. Por eso, si `procurement.integration.enabled=true`, la prueba local requiere conectividad a Oracle ASI y datos vigentes para las cuentas del XML.

## Ejecucion

Levantar local:

```powershell
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

Iniciar batch:

```bash
curl -X POST http://localhost:8080/api/v1/nominas/batch/start \
  -H "Content-Type: application/json" \
  -d '{
    "profile": "VIDA",
    "maxNominas": 1,
    "dryRun": false
  }'
```

## Verificacion

Revisar logs:

- `sourceMode=local-xml`
- `Skipping Artikos NOMFACTCONFIR`
- `PROCUREMENT_POST_DOCUMENT`
- `Skipping Artikos NOMFACTRES send`

Revisar `CONTROL_NOMINA`:

```sql
SELECT *
FROM CONTROL_NOMINA
WHERE NUMERO_NOMINA = 15960
ORDER BY CREATED_AT DESC;
```

## Resultado esperado

- Procurement `statusCode=0`: documento OK.
- Procurement `statusCode=-20`: documento OK idempotente.
- Procurement funcional distinto a duplicado: documento NOK.
- Error tecnico Procurement: job FAILED y `CONTROL_NOMINA` ERROR.
