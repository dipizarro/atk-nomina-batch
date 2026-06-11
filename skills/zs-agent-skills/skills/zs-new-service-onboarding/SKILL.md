# ZS New Service Onboarding Skill

## Objetivo

Guiar al agente de IA cuando se deba iniciar, diseñar, documentar, revisar o preparar un nuevo servicio web / microservicio para ZS / Zurich Santander / cliente relacionado.

Esta skill se enfoca en el proceso previo al desarrollo, validaciones necesarias, entregables mínimos y criterios de arquitectura para nuevos servicios.

## Estado de la skill

Versión: v0.1  
Estado: Provisional  
Fecha de creación: pendiente de completar  
Responsable: pendiente de completar

Esta skill fue creada a partir de documentos corporativos recibidos progresivamente. Los estándares pueden actualizarse o requerir confirmación formal por parte del líder de integración o arquitecto.

## Cuándo usar esta skill

Usar esta skill cuando el usuario solicite:

- iniciar un nuevo servicio;
- diseñar un microservicio;
- preparar una propuesta técnica;
- analizar factibilidad técnica;
- validar entregables antes de desarrollo;
- crear estructura base de un servicio;
- revisar si falta documentación;
- preparar onboarding técnico;
- definir checklist de inicio;
- revisar cumplimiento previo a producción.

## Reglas de prioridad

Antes de aplicar esta skill, revisar:

1. `zs-context/SKILL.md`
2. `zs-java-standards/SKILL.md`, si el servicio es Java

Cuando exista conflicto entre reglas, aplicar este orden:

1. Instrucción directa y vigente del líder de integración o arquitecto.
2. Última versión oficial confirmada por el cliente.
3. Onboarding Desarrollo Nuevos Servicios ZS.
4. Manual de estándares Java v1.1.
5. Buenas Practicas Java.
6. Ejemplo Microservicio ZS.
7. Buenas prácticas generales de arquitectura y Spring Boot.

Si existe contradicción o falta de información, marcarla como pendiente de validación.

## Regla principal

No iniciar ni proponer un desarrollo como definitivo sin validar previamente con el líder de integración o arquitecto.

Cuando falte información, el agente debe marcar brechas explícitamente y proponer preguntas concretas.

## Validaciones previas obligatorias

Antes de iniciar un nuevo servicio, validar:

- ¿Existe OK formal del líder de integración o arquitecto?
- ¿El servicio realmente debe ser nuevo o puede extender uno existente?
- ¿Cuál es el objetivo funcional del servicio?
- ¿Qué problema o necesidad resuelve?
- ¿Qué sistemas consumirán el servicio?
- ¿Qué sistemas serán consumidos por el servicio?
- ¿Existe documento funcional?
- ¿Existe contrato Swagger/OpenAPI?
- ¿Existe modelo de datos?
- ¿Existe definición de autenticación/autorización?
- ¿Existe definición de ambientes?
- ¿Existe repositorio Git?
- ¿Existe pipeline CI/CD?
- ¿Existe estándar de logs y monitoreo?
- ¿Existe definición de manejo de errores?
- ¿Existe definición de versionado de API?

## Tecnología

Para nuevos servicios Java / Spring Boot:

- Confirmar versión de Java con líder de integración o arquitecto.
- Confirmar versión de Spring Boot con líder de integración o arquitecto.
- No asumir versión final sin validación.
- Validar si se usará Maven o Gradle.
- Validar si se usará Spring Web, Spring Data, Spring Security u otras dependencias.
- Validar si se usará JPA/ORM o JDBC.
- Validar si se requiere conexión a base de datos.
- Validar si se requiere consumo de APIs externas.
- Validar si se requiere mensajería, colas o integración batch.

## Arquitectura esperada

Cuando se trate de nuevos servicios:

- Preferir arquitectura RESTful si el estándar del cliente lo indica.
- Separar responsabilidades por capas.
- Evitar lógica de negocio en controladores.
- Mantener controladores simples.
- Mantener lógica de negocio en services.
- Mantener acceso a datos en repositories, DAOs o adaptadores.
- Mantener DTOs separados de entidades cuando corresponda.
- Externalizar configuración.
- Evitar credenciales hardcodeadas.
- Evitar URLs hardcodeadas.
- Mantener trazabilidad de errores.
- Mantener documentación actualizada.

## Entregables mínimos esperados

Antes de pasar a producción o presentar el servicio como completo, validar si existen:

- Código fuente en repositorio Git.
- README.md del proyecto.
- Swagger/OpenAPI.
- Documento funcional.
- Modelo de datos entidad-relación, si aplica.
- Definición de endpoints.
- Definición de parámetros de entrada.
- Definición de respuestas.
- Definición de códigos HTTP.
- Definición de errores funcionales y técnicos.
- Escenarios de prueba.
- Instrucciones de despliegue.
- Variables de ambiente requeridas.
- Evidencia de pruebas.
- Validación de SonarLint o herramienta equivalente si aplica.

## README del servicio

El README debe permitir que otro desarrollador entienda, configure y ejecute el servicio.

Debe considerar:

- Nombre del servicio.
- Objetivo.
- Descripción funcional breve.
- Tecnología usada.
- Versión de Java.
- Versión de Spring Boot.
- Herramienta de build: Maven o Gradle.
- Cómo ejecutar localmente.
- Variables de ambiente.
- Endpoints principales.
- Ruta de Swagger/OpenAPI.
- Dependencias externas.
- Base de datos, si aplica.
- Comandos de compilación.
- Comandos de pruebas.
- Consideraciones de despliegue.
- Contacto o responsable técnico, si aplica.

## Swagger / OpenAPI

Cuando se cree un nuevo servicio:

- Debe existir contrato Swagger/OpenAPI.
- Deben documentarse endpoints.
- Deben documentarse parámetros de entrada.
- Deben documentarse respuestas exitosas.
- Deben documentarse errores esperados.
- Deben documentarse modelos de entrada y salida.
- No inventar payloads si no están confirmados.
- Marcar supuestos si el contrato no está completo.

## Documento funcional

El documento funcional debe cubrir como mínimo:

- Versionado.
- Introducción.
- Objetivo de la API.
- Usuarios o sistemas objetivo.
- Arquitectura y diseño.
- Flujo de datos.
- Funcionalidades principales.
- Parámetros y respuestas.
- Autenticación y autorización.
- Gestión de errores.
- Escenarios de prueba.
- Consideraciones de despliegue.
- Gestión de versiones.
- Mantenimiento y soporte.

## Modelo de datos

Si el servicio usa base de datos:

- Identificar entidades.
- Identificar tablas.
- Identificar relaciones.
- Identificar claves primarias.
- Identificar claves foráneas.
- Identificar campos obligatorios.
- Identificar tipos de datos.
- Identificar restricciones.
- Identificar índices relevantes si aplica.
- Validar si existe modelo entidad-relación formal.

## Seguridad

Antes de implementar seguridad, validar:

- Mecanismo de autenticación.
- Mecanismo de autorización.
- Manejo de tokens.
- Headers requeridos.
- Integración con gateway o sistema externo.
- Reglas de acceso por endpoint.
- Manejo de datos sensibles.
- Reglas de logging para no exponer información sensible.

No asumir JWT, OAuth2, Basic Auth, API Key u otro mecanismo sin confirmación.

## Manejo de errores

El servicio debe definir:

- Errores funcionales.
- Errores técnicos.
- Códigos HTTP esperados.
- Mensajes de error.
- Estructura estándar de respuesta, si existe.
- Trazabilidad interna.
- Logs asociados.

No exponer stacktrace ni detalles internos al consumidor de la API.

## Pruebas

Antes de entregar, validar:

- Casos felices.
- Casos con parámetros inválidos.
- Casos sin autorización.
- Casos de recurso no encontrado.
- Casos de error funcional.
- Casos de error técnico.
- Pruebas de integración si aplica.
- Evidencia de ejecución.

## Checklist previo al desarrollo

Antes de escribir código, responder:

- ¿Qué servicio se debe crear?
- ¿Cuál es su objetivo?
- ¿Quién lo consume?
- ¿Qué sistema externo consume?
- ¿Hay contrato de entrada?
- ¿Hay contrato de salida?
- ¿Hay Swagger/OpenAPI?
- ¿Hay documento funcional?
- ¿Hay modelo de datos?
- ¿Hay definición de seguridad?
- ¿Hay definición de errores?
- ¿Hay repositorio?
- ¿Hay estándar de ramas?
- ¿Hay tecnología confirmada?
- ¿Hay arquitectura validada?

## Checklist antes de entrega

Antes de considerar el servicio listo:

- ¿Compila correctamente?
- ¿Ejecuta localmente?
- ¿Tiene README?
- ¿Tiene Swagger/OpenAPI?
- ¿Tiene documento funcional?
- ¿Tiene modelo de datos si aplica?
- ¿Tiene configuración externalizada?
- ¿No tiene secretos hardcodeados?
- ¿Tiene manejo de errores?
- ¿Tiene logs?
- ¿Tiene pruebas?
- ¿Se revisó SonarLint si aplica?
- ¿Está validado por líder de integración o arquitecto?
- ¿Quedan brechas documentadas?

## Qué no debe hacer el agente

- No iniciar diseño definitivo sin validar contexto.
- No asumir que el ejemplo de microservicio es obligatorio en todos sus detalles.
- No asumir versión de Java.
- No asumir versión de Spring Boot.
- No asumir Maven o Gradle.
- No asumir autenticación.
- No inventar estructura de payload.
- No inventar modelo de datos.
- No inventar reglas de negocio.
- No proponer fechas o estimaciones cerradas sin brechas identificadas.
- No modernizar tecnología sin confirmación del cliente.
- No omitir entregables documentales.

## Salida esperada del agente

Cuando esta skill se use para iniciar un nuevo servicio, el agente debe responder con:

1. Resumen del objetivo del servicio.
2. Información disponible.
3. Brechas detectadas.
4. Preguntas para líder de integración o arquitecto.
5. Propuesta técnica preliminar.
6. Entregables necesarios.
7. Checklist de inicio.

Cuando esta skill se use para revisar un servicio existente, el agente debe responder con:

1. Estado actual.
2. Cumplimientos detectados.
3. Incumplimientos detectados.
4. Riesgos.
5. Pendientes de validación.
6. Próximos pasos recomendados.