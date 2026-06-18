# Procurement integration

## Alcance actual

La aplicacion ya cuenta con:

- mapper Artikos -> Procurement CMP;
- DTOs JSON para `CMP`;
- cliente HTTP configurable para `POST /api/v1/document`.

La integracion todavia no esta conectada al processor batch. El batch no envia documentos reales a Procurement en este sprint.

## Endpoint objetivo

```http
POST /api/v1/document
```

## Properties

Configuracion base:

```properties
procurement.client.enabled=false
procurement.client.base-url=
procurement.client.document-path=/api/v1/document
procurement.client.connect-timeout-ms=5000
procurement.client.read-timeout-ms=30000
```

QA/PROD:

```properties
procurement.client.enabled=true
procurement.client.base-url=${PROCUREMENT_BASE_URL}
procurement.client.document-path=/api/v1/document
procurement.client.connect-timeout-ms=${PROCUREMENT_CONNECT_TIMEOUT_MS:5000}
procurement.client.read-timeout-ms=${PROCUREMENT_READ_TIMEOUT_MS:30000}
```

Si `procurement.client.enabled=false`, el cliente no llama Procurement y lanza una excepcion controlada al intentar usarse.

## Request

El request se construye desde `ProcurementDocumentMapper`.

Estructura:

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

## Response esperado

Contrato base esperado:

```json
{
  "payload": {},
  "statusCode": 0,
  "message": "OK",
  "error": null
}
```

Reglas:

- `statusCode=0`: OK funcional.
- `statusCode!=0`: NOK funcional si HTTP fue valido o el body es parseable.
- `payload.externalDocumentId`, `payload.documentId` o `payload.id` puede usarse como identificador externo si Procurement lo entrega.

## Errores tecnicos

Se consideran errores tecnicos y lanzan `ProcurementClientException`:

- timeout;
- error de conexion;
- HTTP `5xx`;
- error de serializacion;
- respuesta no parseable.

HTTP `4xx` con body parseable se trata como NOK funcional inicial, no como excepcion tecnica.

## Logging

En `INFO` se registra:

- inicio de llamada;
- endpoint sanitizado;
- HTTP status;
- `statusCode` funcional;
- duracion.

No se loguea el JSON completo en `INFO`. Request y response completos quedan reservados para `DEBUG`.

## Fuera de alcance

- Integrar Procurement al processor batch.
- Reemplazar validacion/procesamiento actual del batch.
- Implementar bulk.
- Implementar retry Procurement.
- Consultar ASI.
- Modificar Artikos SOAP.
- Cambiar `NOMFACTRES`.
