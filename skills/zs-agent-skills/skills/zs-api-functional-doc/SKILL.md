# ZS API Functional Document Skill

## Objetivo

Guiar al agente de IA cuando deba crear, completar, revisar o mejorar un documento funcional de API para ZS / Zurich Santander / cliente relacionado.

Esta skill define la estructura mínima, criterios de calidad, brechas típicas y forma correcta de documentar APIs sin inventar información no confirmada.

## Estado de la skill

Versión: v0.1  
Estado: Provisional  
Fecha de creación: pendiente de completar  
Responsable: pendiente de completar

Esta skill fue creada a partir de documentos corporativos recibidos progresivamente. Los estándares pueden actualizarse o requerir confirmación formal por parte del líder de integración o arquitecto.

## Cuándo usar esta skill

Usar esta skill cuando el usuario solicite:

- crear un documento funcional de API;
- completar un documento funcional incompleto;
- revisar documentación funcional existente;
- preparar documentación para un nuevo microservicio;
- describir endpoints;
- documentar parámetros y respuestas;
- documentar reglas de negocio;
- documentar autenticación y autorización;
- documentar escenarios de prueba;
- preparar entregables para el cliente.

## Reglas de prioridad

Antes de aplicar esta skill, revisar:

1. `zs-context/SKILL.md`
2. `zs-new-service-onboarding/SKILL.md`, si corresponde a un servicio nuevo
3. `zs-java-standards/SKILL.md`, si la API será implementada en Java

Cuando exista conflicto entre reglas, aplicar este orden:

1. Instrucción directa y vigente del líder de integración o arquitecto.
2. Última versión oficial confirmada por el cliente.
3. Documento funcional de API EjecutaEjemplo.
4. Onboarding Desarrollo Nuevos Servicios ZS.
5. Ejemplo Microservicio ZS.
6. Buenas prácticas generales de documentación de APIs.

Si existe contradicción o falta de información, marcarla como pendiente de validación.

## Regla principal

No inventar información funcional, técnica ni contractual.

Cuando falte información, el agente debe:

- marcar el punto como pendiente;
- proponer una pregunta concreta;
- indicar el impacto de no contar con esa información;
- usar etiquetas como `PENDIENTE`, `SUPUESTO` o `REQUIERE VALIDACIÓN`.

## Estructura mínima del documento funcional

El documento funcional de API debe considerar como mínimo las siguientes secciones:

1. Control de documento
2. Versionado
3. Introducción
4. Objetivo de la API
5. Usuarios o sistemas objetivo
6. Arquitectura y diseño
7. Diagrama de flujo
8. Funcionalidades principales
9. Endpoints
10. Parámetros de entrada
11. Respuestas esperadas
12. Autenticación y autorización
13. Gestión de errores
14. Escenarios de prueba
15. Consideraciones de despliegue
16. Gestión de versiones
17. Mantenimiento y soporte
18. Brechas o pendientes de validación

## Control de documento

Incluir una tabla con:

- nombre del documento;
- nombre de la API;
- cliente;
- proveedor;
- versión;
- fecha;
- autor;
- responsable funcional;
- responsable técnico;
- estado del documento.

Ejemplo de estados:

- Borrador
- En revisión
- Aprobado
- Obsoleto

## Versionado

Incluir historial de cambios:

| Versión | Fecha | Autor | Descripción |
|---|---|---|---|
| 0.1 | PENDIENTE | PENDIENTE | Creación inicial del documento |
| 1.0 | PENDIENTE | PENDIENTE | Versión aprobada |

No inventar fechas ni responsables si no están confirmados.

## Introducción

La introducción debe explicar brevemente:

- contexto general;
- necesidad que origina la API;
- sistema o proceso relacionado;
- alcance funcional;
- relación con otros sistemas si aplica.

Evitar lenguaje comercial excesivo. Mantener tono técnico, claro y funcional.

## Objetivo de la API

Debe responder:

- ¿Qué hace la API?
- ¿Qué problema resuelve?
- ¿Qué proceso habilita o automatiza?
- ¿Qué sistema la consume?
- ¿Qué sistema la expone?
- ¿Cuál es el resultado esperado?

Ejemplo:

```text
El objetivo de la API es permitir que el sistema consumidor consulte el estado de una solicitud mediante un identificador único, retornando la información necesaria para continuar el flujo operacional correspondiente.
```

## Usuarios o sistemas objetivo

Identificar quiénes interactúan con la API:

- sistema consumidor;
- sistema proveedor;
- usuarios internos;
- servicios intermedios;
- gateway;
- sistemas legacy;
- procesos batch si aplica.

Para cada actor, describir:

- nombre;
- tipo;
- responsabilidad;
- interacción con la API.

Ejemplo:
______________________________________________________________________________________
|        Actor       |       Tipo      |               Responsabilidad               |
|--------------------|-----------------|---------------------------------------------|
| Sistema consumidor | Sistema externo | Invoca la API para consultar información    |
| API    			 | Servicio        | Procesa solicitud y retorna respuesta       |
| Base de datos    	 | Persistencia    | Entrega datos requeridos por la operación   |


## Arquitectura y diseño

Describir la estructura general de la API.

Incluir, cuando exista información:

- componentes principales;
- capas de la solución;
- servicios involucrados;
- base de datos;
- sistemas externos;
- gateway o proxy;
- mecanismo de seguridad;
- flujo general.

No inventar arquitectura si no está confirmada. Si falta información, marcar:

```text
REQUIERE VALIDACIÓN: confirmar componentes de arquitectura con líder de integración o arquitecto.
```

## Diagrama de flujo

Cuando se documente el flujo, describir como mínimo:

1. sistema consumidor envía solicitud;
2. API recibe y valida parámetros;
3. API aplica reglas de negocio;
4. API consulta sistema o base de datos;
5. API construye respuesta;
6. sistema consumidor recibe resultado;
7. API registra logs o trazabilidad si aplica.

Si no existe diagrama formal, proponer uno como borrador y marcarlo como SUPUESTO.

## Funcionalidades principales

Listar cada funcionalidad de la API.

Por cada funcionalidad, indicar:

- nombre;
- descripción;
- endpoint asociado;
- método HTTP;
- entrada;
- salida;
- reglas funcionales;
- errores esperados;
- dependencias externas.

Ejemplo:

________________________________________________________________________________________________
|    Funcionalidad   |             Descripción             |         Endpoint         | Método |
|--------------------|-------------------------------------|--------------------------|--------|
|  Consultar estado  | Consulta el estado de una solicitud | /solicitudes/{id}/estado |   GET  |


## Endpoints

Para cada endpoint, documentar:

- nombre de la operación;
- método HTTP;
- ruta;
- descripción;
- headers requeridos;
- parámetros path;
- parámetros query;
- body request;
- body response;
- códigos HTTP;
- reglas de validación.

Plantilla:

### Endpoint: [Nombre de operación]

Método: PENDIENTE  
Ruta: PENDIENTE  
Descripción: PENDIENTE  

Headers:
- PENDIENTE

Path params:
- PENDIENTE

Query params:
- PENDIENTE

Request body:
- PENDIENTE

Response body:
- PENDIENTE

## Parámetros de entrada

Documentar cada parámetro:

| Campo | Tipo | Obligatorio | Descripción | Ejemplo | Validaciones |
| ----- | ---- | ----------- | ----------- | ------- | ------------ |

Reglas:

- No inventar tipos de datos.
- No inventar obligatoriedad.
- No inventar formatos.
- Si no existe definición, marcar como PENDIENTE.
- Diferenciar path params, query params, headers y body.

## Respuestas esperadas

Documentar respuestas exitosas y respuestas de error.

Para respuestas exitosas:

- código HTTP;
- descripción;
- estructura;
- campos retornados;
- ejemplo.

Para respuestas de error:

- código HTTP;
- causa;
- mensaje;
- estructura;
- acción recomendada.

Ejemplo:

| Código HTTP | Escenario           | Descripción                              |
| ----------- | ------------------- | ---------------------------------------- |
| 200         | Operación exitosa   | La solicitud fue procesada correctamente |
| 400         | Error de validación | Parámetros inválidos o incompletos       |
| 401         | No autenticado      | Falta credencial o token válido          |
| 403         | No autorizado       | El consumidor no tiene permiso           |
| 404         | No encontrado       | No existen datos para la solicitud       |
| 500         | Error interno       | Error técnico no controlado              |


## Autenticación y autorización

Documentar:

- mecanismo de autenticación;
- mecanismo de autorización;
- headers requeridos;
- token o credencial;
- vigencia del token si aplica;
- roles o permisos si aplica;
- comportamiento ante credenciales inválidas.

No asumir JWT, OAuth2, Basic Auth, API Key u otro mecanismo sin evidencia.

Si no está definido, indicar:

```text
REQUIERE VALIDACIÓN: confirmar mecanismo de autenticación y autorización.
```

## Gestión de errores

La sección de errores debe explicar:

- errores funcionales;
- errores técnicos;
- códigos HTTP;
- estructura estándar de error;
- mensajes esperados;
- trazabilidad;
- logs;
- errores de sistemas externos.

No exponer detalles internos como stacktrace al consumidor.

Plantilla:

```json
{
  "code": "PENDIENTE",
  "message": "PENDIENTE",
  "detail": "PENDIENTE",
  "timestamp": "PENDIENTE"
}
```

## Escenarios de prueba

Debe incluir casos para validar el funcionamiento de la API.

Mínimos recomendados:

- caso exitoso;
- parámetros obligatorios faltantes;
- parámetros con formato inválido;
- consumidor no autenticado;
- consumidor no autorizado;
- recurso no encontrado;
- error funcional esperado;
- error técnico simulado;
- error de sistema externo si aplica.

Plantilla:

| ID     | Escenario                      | Entrada   | Resultado esperado | Tipo     |
| ------ | ------------------------------ | --------- | ------------------ | -------- |
| CP-001 | Caso exitoso                   | PENDIENTE | HTTP 200           | Positivo |
| CP-002 | Parámetro obligatorio faltante | PENDIENTE | HTTP 400           | Negativo |


## Consideraciones de despliegue

Documentar:

- ambiente de desarrollo;
- ambiente de QA;
- ambiente productivo;
- variables de ambiente;
- dependencias externas;
- base de datos;
- configuración de seguridad;
- URL base por ambiente;
- pipeline o proceso de despliegue;
- consideraciones de rollback.

No inventar URLs ni ambientes.

## Gestión de versiones

Definir:

- versión inicial de la API;
- estrategia de versionado;
- compatibilidad hacia atrás;
- cambios breaking;
- forma de publicar nuevas versiones.

Ejemplo:

```text
REQUIERE VALIDACIÓN: confirmar estrategia de versionado de API con arquitectura.
```

## Mantenimiento y soporte

Documentar:

- responsable técnico;
- equipo de soporte;
- procedimiento de corrección;
- monitoreo;
- logs;
- contacto ante incidentes;
- frecuencia de revisión;
- dependencias críticas.

Si no existe definición, marcar como PENDIENTE.

## Brechas y pendientes

Toda documentación funcional debe cerrar con una sección de brechas.

Plantilla:

| ID    | Brecha                        | Impacto                             | Responsable sugerido | Estado    |
| ----- | ----------------------------- | ----------------------------------- | -------------------- | --------- |
| B-001 | Falta confirmar autenticación | No se puede cerrar contrato técnico | Arquitectura         | Pendiente |


## Checklist antes de entregar documento

Antes de entregar un documento funcional, validar:

- ¿Tiene control de documento?
- ¿Tiene versionado?
- ¿Tiene objetivo claro?
- ¿Identifica usuarios o sistemas consumidores?
- ¿Describe arquitectura?
- ¿Incluye flujo o diagrama?
- ¿Lista funcionalidades?
- ¿Documenta endpoints?
- ¿Documenta parámetros?
- ¿Documenta respuestas?
- ¿Documenta autenticación/autorización?
- ¿Documenta manejo de errores?
- ¿Incluye escenarios de prueba?
- ¿Incluye despliegue?
- ¿Incluye mantenimiento?
- ¿Incluye brechas pendientes?
- ¿Diferencia entre confirmado, supuesto y pendiente?
- ¿No inventa información no validada?

## Qué no debe hacer el agente

- No inventar endpoints.
- No inventar payloads.
- No inventar modelos de datos.
- No inventar códigos de error específicos del cliente.
- No inventar autenticación.
- No inventar URLs.
- No inventar ambientes.
- No eliminar brechas por intentar cerrar el documento.
- No presentar supuestos como hechos confirmados.
- No escribir documentación excesivamente comercial.
- No mezclar diseño técnico con reglas funcionales sin claridad.

## Salida esperada del agente

Cuando esta skill se use para crear un documento funcional, el agente debe entregar:

1. Estructura del documento.
2. Contenido redactado por sección.
3. Tablas funcionales cuando correspondan.
4. Supuestos declarados.
5. Brechas pendientes.
6. Preguntas para validación.

Cuando esta skill se use para revisar un documento existente, el agente debe entregar:

1. Resumen del estado.
2. Secciones completas.
3. Secciones incompletas.
4. Brechas críticas.
5. Riesgos de interpretación.
6. Recomendaciones de mejora.
