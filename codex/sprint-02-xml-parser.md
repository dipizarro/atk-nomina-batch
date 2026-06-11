Sprint 2 — Parser SOAP Artikos real

Objetivo: leer ZSVIDA_Nom15960.xml, extraer la nómina y mapearla a modelo interno.

Commit sugerido:

git commit -m "feat: parse Artikos SOAP nomina XML sample"

Criterios de aceptación:

- El XML SOAP local se lee desde resources/samples/ZSVIDA_Nom15960.xml
- Se extrae MessageId.MsgStatus
- Se valida que MsgStatus = 0
- Se extrae Nomina.Cabecera.Numero_Nomina = 15960
- Se extrae Cantidad_Documentos = 1
- Se extrae 1 Documento
- Se extraen 2 Conciliaciones
- Se extraen 2 Distribuciones en total
- El parser tiene test unitario