# ZS Java Standards Skill

## Objetivo

Guiar al agente de IA cuando cree, modifique, revise o refactorice código Java para desarrollos asociados a ZS / Zurich Santander / cliente relacionado.

Esta skill consolida reglas de buenas prácticas Java, estándares de codificación, documentación, conexión a base de datos, logging y validación de calidad.

## Estado de la skill

Versión: v0.1  
Estado: Provisional  
Fecha de creación: pendiente de completar  
Responsable: pendiente de completar

Esta skill fue creada a partir de documentos corporativos recibidos progresivamente. Los estándares pueden actualizarse o requerir confirmación formal por parte del líder de integración o arquitecto.

## Cuándo usar esta skill

Usar esta skill cuando el usuario solicite:

- crear código Java;
- modificar código Java existente;
- revisar cumplimiento de estándares;
- refactorizar clases Java;
- crear o modificar servicios Spring Boot;
- crear controladores REST;
- crear DTOs, services, repositories o clases JDBC;
- revisar conexión a base de datos;
- revisar manejo de excepciones;
- revisar documentación Swagger/OpenAPI;
- preparar código para entrega al cliente.

## Reglas de prioridad

Antes de aplicar esta skill, considerar también `zs-context/SKILL.md`.

Cuando exista conflicto entre reglas, aplicar este orden:

1. Instrucción directa y vigente del líder de integración o arquitecto.
2. Última versión oficial confirmada por el cliente.
3. Manual de estándares Java v1.1.
4. Buenas Practicas Java.
5. Onboarding Desarrollo Nuevos Servicios ZS.
6. Buenas prácticas generales de Java y Spring Boot.

Si existe contradicción o falta de información, marcarla como pendiente de validación.

## Nivel de confianza

Clasificar recomendaciones críticas usando:

- CONFIRMADO: aparece explícitamente en documento fuente vigente o fue confirmado por el cliente.
- PROVISIONAL: aparece en documento recibido, pero puede cambiar.
- REQUIERE VALIDACIÓN: depende del líder de integración o arquitecto.
- SUPUESTO: inferencia técnica razonable, pero no explícita en documentos.

## Reglas generales de codificación Java

Aplicar las siguientes reglas al crear o modificar código:

- Usar nombres claros y descriptivos.
- Usar nombres en inglés para componentes generados.
- Usar camelCase para variables y métodos.
- Usar PascalCase para nombres de clases.
- Usar paquetes completamente en minúsculas.
- No usar guiones entre palabras.
- No usar caracteres especiales en nombres.
- Evitar abreviaturas innecesarias.
- Evitar variables booleanas negadas como `isNotError` o `isNotFound`.
- Preferir nombres positivos como `isError`, `isFound`, `isActive`.
- Mantener campos de clase como privados.
- Acceder a campos mediante métodos o mecanismos permitidos por el framework.
- Evitar inicializaciones redundantes con valores por defecto como `0` o `null`.
- Usar literales numéricos con guiones bajos cuando mejore la legibilidad.
- Usar `StringBuilder` o `StringBuffer` para concatenaciones extensas.
- Preferir `for-each` sobre `for` con contador cuando corresponda.
- Segregar cambios en commits separados y coherentes.

## Constantes

Cuando existan valores repetidos o literales relevantes:

- Declarar constantes con `final` cuando corresponda.
- Escribir constantes en mayúsculas.
- Usar `_` como separador en nombres de constantes.
- Evitar duplicar strings repetidos entre métodos.
- Para llamadas a packages, procedimientos o identificadores repetidos, preferir constantes de clase.

Ejemplo:

```java
private static final String PACKAGE_NAME = "PAC_IAX_AVISO";
private static final String PROCEDURE_NAME = "F_GET_AVISOS";
```

## Documentación del código fuente

Todo servicio debe contar con documentación interna clara.

Cuando se cree o modifique código que exponga APIs:

- Actualizar la documentación Swagger/OpenAPI.
- Documentar operaciones.
- Documentar respuestas posibles.
- Documentar modelos de entrada y salida.
- Indicar campos requeridos cuando aplique.
- Mantener la documentación como parte del entregable.

Si el proyecto usa Swagger clásico:

- Documentar operación con @ApiOperation.
- Documentar respuestas con @ApiResponses.
- Documentar modelos con @ApiModel y @ApiModelProperty.

Si el proyecto usa Spring Boot moderno:

- Validar si corresponde usar SpringDoc OpenAPI.
- No mezclar Swagger clásico y SpringDoc sin validación técnica.
- Confirmar estándar vigente con líder de integración o arquitecto.

## Arquitectura de servicios

En servicios Java / Spring Boot:

- Separar responsabilidades por capa.
- Evitar lógica de negocio excesiva en controladores.
- Mantener controladores enfocados en entrada, validación básica, orquestación y respuesta.
- Mantener lógica de negocio en services.
- Mantener acceso a datos en repositories, DAOs o clases JDBC según el tipo de proyecto.
- No acceder directamente a base de datos desde controladores.
- No hardcodear credenciales, URLs, tokens ni strings de conexión.
- Usar configuración externa para valores por ambiente.

## Base de datos con ORM

Cuando el desarrollo sea nuevo y el estándar lo permita:

- Los servicios que accedan a base de datos deben hacerlo mediante ORM.
- Mapear estructuras relacionales a entidades.
- Evitar SQL disperso en capas no correspondientes.
- Validar con el arquitecto si se usará JPA, Hibernate u otro mecanismo.

## Base de datos con JDBC legacy

Cuando el proyecto sea legacy o use clases JDBC:

- No heredar conexiones desde constructores.
- No mantener conexiones abiertas esperando reutilización.
- Crear la conexión en el método que ejecuta la operación contra base de datos.
- Cerrar correctamente Connection, CallableStatement, PreparedStatement y ResultSet.
- Preferir try-with-resources cuando sea compatible.
- Si no se puede usar try-with-resources, cerrar recursos en finally.
- Evitar fugas de conexión.
- No retornar conexiones abiertas.
- No compartir conexiones entre métodos sin una razón explícita y validada.

Ejemplo recomendado con try-with-resources:

```java
try (
    Connection connection = ConnectionFactory.getInstance().getConnection();
    CallableStatement statement = connection.prepareCall(PROCEDURE_NAME)
) {
    // ejecutar operación
} catch (SQLException exception) {
    logger.error("Error ejecutando operación en base de datos", exception);
}
```

Ejemplo alternativo si el proyecto no permite try-with-resources:

```java
Connection connection = null;
CallableStatement statement = null;

try {
    connection = ConnectionFactory.getInstance().getConnection();
    statement = connection.prepareCall(PROCEDURE_NAME);

    // ejecutar operación
} catch (SQLException exception) {
    logger.error("Error ejecutando operación en base de datos", exception);
} finally {
    // cerrar statement y connection validando null
}
```

## ConnectionFactory

Si el proyecto usa ConnectionFactory:

- Revisar si existe una clase estándar del cliente.
- No crear una nueva implementación sin validar la existente.
- Centralizar la obtención de conexiones.
- Evitar duplicidad de lógica de conexión.
- Validar que no genere conexiones imposibles de cerrar.
- Validar que sea compatible con el datasource o configuración vigente.

## Manejo de excepciones

- No dejar bloques catch vacíos.
- No ocultar excepciones sin registro.
- Registrar errores con contexto suficiente.
- Evitar printStackTrace() en código productivo salvo que el estándar vigente lo permita explícitamente.
- Registrar stacktrace con logger.
- Incluir mensaje claro del error.
- Incluir parámetros relevantes cuando no expongan datos sensibles.
- No exponer detalles internos innecesarios al consumidor de la API.
- Diferenciar errores funcionales de errores técnicos.

Ejemplo no permitido:

```java
try {
    // operación
} catch (Exception exception) {
}
```

Ejemplo aceptable:

```java
try {
    // operación
} catch (Exception exception) {
    logger.error("Error al procesar solicitud", exception);
}
```

## Logging

Cuando aplique:

- Preferir logging mediante AOP si el proyecto lo permite.
- Registrar parámetros de entrada y salida relevantes.
- Registrar tiempo de ejecución de operaciones relevantes.
- Registrar errores con descripción, stacktrace, fecha, hora y contexto.
- No registrar datos sensibles.
- Validar si el proyecto usa Log4j, Logback u otro estándar.
- Si no se puede implementar AOP, usar logging explícito siguiendo el estándar del proyecto.

Niveles sugeridos:

- debug: datos técnicos útiles para diagnóstico.
- info: eventos relevantes de negocio o flujo.
- warn: situaciones inesperadas recuperables.
- error: errores técnicos o funcionales relevantes.

## SonarLint e IDE

El estándar recibido recomienda Eclipse y SonarLint.

Cuando se trabaje con el proyecto:

- Validar que el proyecto compile en el IDE recomendado si el cliente lo exige.
- Revisar advertencias del IDE.
- Revisar sugerencias de SonarLint.
- No aplicar cambios automáticos sin validar impacto.
- En proyectos legacy, revisar impacto entre JSP, Actions, Services y JDBC.
- Si se usa VS Code, IntelliJ o Codex, mantener compatibilidad con el estándar del cliente.

## Codex y asistentes de IA

Cuando se use Codex u otro agente IA:

- Leer primero zs-context/SKILL.md.
- Aplicar esta skill antes de generar código.
- No inventar reglas del cliente.
- No cambiar arquitectura sin justificación.
- No modernizar código legacy si el alcance solo pide corrección puntual.
- Explicar los cambios propuestos.
- Marcar riesgos y pendientes de validación.
- Ejecutar compilación o pruebas si el entorno lo permite.
- Si no se puede compilar, indicarlo claramente.

## Checklist antes de entregar código

Antes de considerar terminado un cambio, validar:

- ¿El código compila?
- ¿Respeta naming Java?
- ¿Los paquetes están en minúsculas?
- ¿Las clases tienen nombres claros?
- ¿Las variables y métodos usan camelCase?
- ¿No hay abreviaturas innecesarias?
- ¿No hay booleanos negados confusos?
- ¿No hay campos públicos innecesarios?
- ¿No hay bloques catch vacíos?
- ¿Los errores quedan registrados?
- ¿No hay credenciales hardcodeadas?
- ¿No hay URLs o tokens hardcodeados?
- ¿Se cerraron conexiones y statements?
- ¿Se evitó heredar conexiones por constructor?
- ¿Se actualizó Swagger/OpenAPI si corresponde?
- ¿Se revisó SonarLint si corresponde?
- ¿Los commits están separados por cambio lógico?
- ¿Quedaron pendientes documentados?

## Qué no debe hacer el agente

- No asumir que todos los proyectos son Spring Boot modernos.
- No reemplazar JDBC legacy por ORM sin confirmación.
- No cambiar Java 17/21 sin validación.
- No cambiar versión de Spring Boot sin validación.
- No eliminar código legacy sin entender dependencias.
- No dejar TODOs ocultos sin informarlos.
- No inventar endpoints, parámetros ni payloads.
- No asumir autenticación/autorización sin documentación.
- No tratar ejemplos como reglas obligatorias si no están declaradas como estándar.

## Salida esperada del agente

Cuando esta skill se use para revisar código, responder con:

1. Resumen corto del hallazgo.
2. Reglas aplicadas.
3. Problemas detectados.
4. Cambios sugeridos.
5. Riesgos o pendientes de validación.
6. Checklist final.

Cuando esta skill se use para generar código, responder con:

1. Archivos o clases propuestas.
2. Código generado.
3. Supuestos declarados.
4. Validaciones pendientes.
5. Comandos sugeridos para compilar o probar.