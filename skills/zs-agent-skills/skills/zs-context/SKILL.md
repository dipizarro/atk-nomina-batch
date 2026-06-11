# ZS Context Skill

## Objetivo

Guiar al agente de IA cuando trabaje en desarrollos asociados a ZS / Zurich Santander / cliente relacionado, usando documentos corporativos recibidos como referencia.

Esta skill define el contexto general, el nivel de confianza de las reglas y la forma correcta de resolver dudas o contradicciones.

## Estado de la skill

Versión: v0.1  
Estado: Provisional  
Fecha de creación: pendiente de completar  
Responsable: pendiente de completar

Los documentos fuente pueden no ser definitivos. Algunas reglas pueden cambiar o requerir validación formal por parte del líder de integración o arquitecto.

## Cuándo usar esta skill

Usar esta skill cuando el usuario solicite:

- crear o modificar código para el cliente;
- diseñar un nuevo servicio;
- revisar código Java;
- crear documentación funcional de API;
- revisar entregables técnicos;
- analizar arquitectura de microservicios;
- preparar una propuesta técnica;
- validar cumplimiento de estándares del cliente.

## Documentos fuente considerados

- Buenas Practicas Java.docx
- Manual de estandares Java v_1.1.docx
- Onboarding Desarrollo Nuevos Servicios ZS.pdf
- Documento funcional de API EjecutaEjemplo.pdf
- Ejemplo Microservicio ZS.pdf

## Regla principal

No asumir que una regla es definitiva si no está confirmada formalmente.

Cuando exista duda, contradicción o falta de información, el agente debe:

1. marcar la situación como pendiente de validación;
2. indicar qué documento origina la regla;
3. no inventar decisiones técnicas;
4. proponer preguntas concretas para el líder de integración o arquitecto.

## Niveles de confianza

Clasificar reglas, decisiones o recomendaciones con uno de estos niveles:

### CONFIRMADO

La regla aparece explícitamente en un documento fuente vigente o fue confirmada directamente por el líder de integración o arquitecto.

### PROVISIONAL

La regla aparece en un documento recibido, pero no existe confirmación de que sea la versión final.

### REQUIERE VALIDACIÓN

La regla depende de una definición del líder de integración, arquitecto, cliente o equipo técnico.

### SUPUESTO

La recomendación es una inferencia razonable, pero no aparece explícitamente en los documentos.

## Reglas de prioridad

Cuando exista conflicto entre fuentes, aplicar este orden:

1. Instrucción directa y vigente del líder de integración o arquitecto.
2. Última versión oficial confirmada por el cliente.
3. Manual de estándares Java v1.1.
4. Buenas Practicas Java.
5. Onboarding Desarrollo Nuevos Servicios ZS.
6. Documento funcional de API EjecutaEjemplo.
7. Ejemplo Microservicio ZS.
8. Buenas prácticas generales de Java, Spring Boot o arquitectura.

Si el conflicto no puede resolverse con evidencia, no decidir automáticamente. Marcar como pendiente de validación.

## Reglas de trabajo para el agente

- No tratar ejemplos como reglas obligatorias si el documento no lo indica.
- No asumir versión de Java sin confirmación.
- No asumir versión de Spring Boot sin confirmación.
- No asumir mecanismo de autenticación sin confirmación.
- No asumir estructura definitiva de repositorio sin confirmación.
- No inventar endpoints, payloads, parámetros ni códigos de error.
- No reemplazar el criterio del arquitecto o líder de integración.
- Siempre diferenciar entre regla, ejemplo, supuesto y brecha.

## Preguntas típicas que deben validarse

Antes de iniciar un desarrollo nuevo, confirmar:

- ¿Existe OK formal del líder de integración o arquitecto?
- ¿Qué versión de Java se usará?
- ¿Qué versión de Spring Boot se usará?
- ¿El servicio será RESTful?
- ¿Cuál será el mecanismo de autenticación?
- ¿Existe Swagger/OpenAPI esperado?
- ¿Existe documento funcional validado?
- ¿Existe modelo de datos entidad-relación?
- ¿Qué repositorio Git se usará?
- ¿Existe pipeline CI/CD?
- ¿Qué ambientes existen?
- ¿Cómo se manejarán logs y monitoreo?
- ¿Qué formato de README exige el cliente?
- ¿Qué reglas de ramas y commits se deben seguir?

## Resultado esperado

Cuando el agente use esta skill, sus respuestas deben ser:

- claras;
- prácticas;
- trazables;
- conservadoras frente a incertidumbre;
- alineadas al estándar disponible;
- explícitas respecto a brechas o validaciones pendientes.