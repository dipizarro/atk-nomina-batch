# ZS Microservice Template Skill

## Objetivo

Guiar al agente de IA cuando deba crear, revisar, comparar o preparar un microservicio para ZS / Zurich Santander / cliente relacionado, tomando como referencia el ejemplo de microservicio recibido y los documentos de onboarding y estándares Java.

Esta skill no asume que el ejemplo entregado sea una regla absoluta. Lo trata como referencia técnica inicial, sujeta a validación con líder de integración o arquitecto.

## Estado de la skill

Versión: v0.1  
Estado: Provisional  
Fecha de creación: pendiente de completar  
Responsable: pendiente de completar

Esta skill fue creada a partir de documentos corporativos recibidos progresivamente. Los estándares pueden actualizarse o requerir confirmación formal por parte del líder de integración o arquitecto.

## Cuándo usar esta skill

Usar esta skill cuando el usuario solicite:

- crear un microservicio nuevo;
- revisar estructura de un microservicio;
- comparar un proyecto contra el ejemplo ZS;
- preparar README técnico;
- validar estructura de carpetas;
- revisar build del proyecto;
- revisar configuración local;
- revisar exposición de Swagger/OpenAPI;
- preparar checklist técnico antes de entrega;
- generar una base inicial para un servicio REST.

## Reglas de prioridad

Antes de aplicar esta skill, revisar:

1. `zs-context/SKILL.md`
2. `zs-new-service-onboarding/SKILL.md`
3. `zs-java-standards/SKILL.md`
4. `zs-api-functional-doc/SKILL.md`, si corresponde documentación funcional

Cuando exista conflicto entre reglas, aplicar este orden:

1. Instrucción directa y vigente del líder de integración o arquitecto.
2. Última versión oficial confirmada por el cliente.
3. Onboarding Desarrollo Nuevos Servicios ZS.
4. Manual de estándares Java v1.1.
5. Buenas Practicas Java.
6. Ejemplo Microservicio ZS.
7. Buenas prácticas generales de Spring Boot y microservicios.

Si existe contradicción o falta de información, marcarla como pendiente de validación.

## Regla principal

No asumir que el ejemplo de microservicio define todos los estándares obligatorios.

El agente debe distinguir entre:

- regla explícita del cliente;
- estructura observada en el ejemplo;
- buena práctica general;
- supuesto técnico;
- brecha pendiente de validación.

## Uso del ejemplo de microservicio

El ejemplo de microservicio debe usarse como referencia para:

- estructura general del proyecto;
- estilo de README;
- forma de ejecución local;
- forma de documentación técnica;
- configuración base;
- organización del código;
- exposición de endpoints;
- configuración de Swagger/OpenAPI;
- comandos de build;
- dependencias iniciales;
- criterios de empaquetado.

No se debe copiar ciegamente sin validar:

- nombre del servicio;
- package base;
- versiones exactas;
- endpoints;
- payloads;
- reglas de negocio;
- mecanismo de seguridad;
- configuración de ambientes;
- dependencias externas;
- credenciales o URLs.

## Tecnología

Para nuevos microservicios Java / Spring Boot:

- Confirmar versión de Java.
- Confirmar versión de Spring Boot.
- Confirmar si se usará Maven o Gradle.
- Confirmar gestor de dependencias.
- Confirmar si se usará Spring Web.
- Confirmar si se usará Spring Data / JPA.
- Confirmar si se usará JDBC.
- Confirmar si se usará Spring Security.
- Confirmar librería de Swagger/OpenAPI.
- Confirmar estrategia de logs.
- Confirmar estrategia de configuración por ambiente.

No asumir versiones sin validación formal.

## Estructura base esperada

Cuando se proponga una estructura de microservicio Spring Boot, usar una separación clara por responsabilidad.

Estructura sugerida:

```text
src/
  main/
    java/
      cl/
        empresa/
          servicio/
            Application.java
            config/
            controller/
            service/
            repository/
            dto/
            model/
            exception/
            mapper/
            util/
    resources/
      application.yml
      application-dev.yml
      application-qa.yml
      application-prod.yml
  test/
    java/
      cl/
        empresa/
          servicio/
```
Esta estructura es una propuesta base. Debe ajustarse al estándar real del cliente si existe una estructura oficial.

## Package base

Validar el package base antes de generar código.

Reglas:

- Usar paquetes en minúsculas.
- No usar caracteres especiales.
- No inventar dominio corporativo si no está confirmado.
- Mantener consistencia con el repositorio o ejemplo existente.
- No cambiar package base de un proyecto legacy sin justificación.

Ejemplo genérico:

```text
cl.zs.nombreservicio
```

Si el package oficial no está confirmado, marcar como:

```text
REQUIERE VALIDACIÓN: confirmar package base corporativo.
```

## Clase principal

La clase principal debe ser clara y representativa del servicio.

Ejemplo:

```Java
@SpringBootApplication
public class NombreServicioApplication {

    public static void main(String[] args) {
        SpringApplication.run(NombreServicioApplication.class, args);
    }
}
```

Reglas:

- No agregar lógica de negocio en la clase principal.
- No hardcodear configuración.
- Mantenerla enfocada en el arranque de la aplicación.

## Controllers

Los controllers deben:

- Exponer endpoints REST.
- Recibir parámetros.
- Validar entrada básica.
- Delegar lógica de negocio al service.
- Retornar respuestas claras.
- Mantener documentación OpenAPI/Swagger si aplica.
- No acceder directamente a base de datos.
- No contener lógica de negocio compleja.

Ejemplo de estructura:

```Java
@RestController
@RequestMapping("/api/v1/recursos")
public class RecursoController {

    private final RecursoService recursoService;

    public RecursoController(RecursoService recursoService) {
        this.recursoService = recursoService;
    }
}
```

## Services

Los services deben:

- Contener lógica de negocio.
- Orquestar llamadas a repositorios, DAOs, adaptadores o clientes externos.
- Validar reglas funcionales.
- Manejar errores funcionales controlados.
- No depender directamente de detalles de infraestructura cuando sea evitable.
- Mantener métodos claros y testeables.

## Repositories / DAOs / JDBC

La capa de acceso a datos debe:

- Encapsular consultas a base de datos.
- No estar mezclada con lógica de controlador.
- Usar ORM si el estándar y el tipo de proyecto lo permiten.
- Usar JDBC solo cuando el proyecto o integración lo requiera.
- Cerrar correctamente conexiones y statements.
- Evitar fugas de conexión.
- No heredar conexiones desde constructores en código JDBC legacy.

Si el proyecto usa ConnectionFactory, validar la implementación existente antes de crear una nueva.

## DTOs

Los DTOs deben:

- Representar entrada o salida de la API.
- Evitar exponer entidades internas si no corresponde.
- Tener nombres claros.
- Usar validaciones cuando aplique.
- Mantener documentación OpenAPI/Swagger si aplica.
- Separar request y response cuando corresponda.

Ejemplo:

```Java
public class ConsultaRequest {
    private String identificador;
}

public class ConsultaResponse {
    private String estado;
    private String descripcion;
}
```

## Manejo de errores

El microservicio debe definir una estrategia clara de errores.

Validar si existe una estructura estándar corporativa.

Si no existe, proponer una estructura base como borrador:

```Json
{
  "code": "ERROR_CODE",
  "message": "Descripción del error",
  "timestamp": "2026-01-01T10:00:00"
}
```

Reglas:

- No exponer stacktrace al consumidor.
- Registrar errores internamente.
- Diferenciar errores funcionales de técnicos.
- Usar códigos HTTP coherentes.
- Documentar errores en OpenAPI/Swagger.
- No inventar códigos internos del cliente si no existen.

## Logs

El microservicio debe tener logging consistente.

Reglas:

- Preferir estrategia definida por el cliente.
- No registrar datos sensibles.
- Registrar eventos relevantes.
- Registrar errores con stacktrace.
- Registrar contexto suficiente para diagnóstico.
- Validar si se requiere AOP.
- Validar si se usa Log4j, Logback u otro estándar.

## Configuración

La configuración debe estar externalizada.

Evitar:

- credenciales hardcodeadas;
- URLs hardcodeadas;
- tokens hardcodeados;
- rutas absolutas locales;
- datos sensibles en el código.

Usar:

- variables de ambiente;
- perfiles Spring;
- archivos application.yml;
- configuración por ambiente;
- secretos administrados por plataforma si aplica.

Ejemplo:

```YAML
server:
  port: ${SERVER_PORT:8080}

spring:
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}
```

## Perfiles de ambiente

Validar si el cliente exige ambientes específicos.

Propuesta base:

dev
qa
prod

No inventar URLs ni credenciales por ambiente.

Si falta información:

```text
REQUIERE VALIDACIÓN: confirmar ambientes, URLs y variables requeridas.
```

## Swagger / OpenAPI

El microservicio debe documentar sus endpoints.

Reglas:

- Documentar rutas.
- Documentar métodos HTTP.
- Documentar parámetros.
- Documentar request body.
- Documentar response body.
- Documentar códigos de error.
- Confirmar si se usará Swagger clásico o SpringDoc OpenAPI.
- Evitar mezclar librerías sin validación.

El onboarding indica que el contrato/documentación Swagger forma parte de los entregables esperados para nuevos servicios. También se menciona la entrega de documento funcional, modelo de datos y README.

## README del microservicio

El README debe permitir que otro desarrollador pueda levantar y entender el servicio.

Debe incluir:

```markdown
# Nombre del servicio

## Descripción

## Objetivo

## Tecnología

## Requisitos previos

## Instalación

## Ejecución local

## Variables de ambiente

## Endpoints principales

## Swagger / OpenAPI

## Base de datos

## Pruebas

## Build

## Despliegue

## Estructura del proyecto

## Troubleshooting

## Contacto / Responsable
```

### Comandos de build

Validar herramienta antes de proponer comandos.

Para Maven:

```Bash
mvn clean install
mvn test
mvn spring-boot:run
```

Para Gradle:

```Bash
./gradlew clean build
./gradlew test
./gradlew bootRun
```

En Windows:

```Bash
gradlew.bat clean build
gradlew.bat test
gradlew.bat bootRun
```

No asumir Maven o Gradle sin revisar el proyecto.

## Pruebas

Antes de entregar un microservicio, validar:

- pruebas unitarias;
- pruebas de integración si aplica;
- prueba de endpoint saludable;
- prueba de caso exitoso;
- prueba de errores funcionales;
- prueba de parámetros inválidos;
- prueba de autenticación/autorización si aplica;
- evidencia de ejecución.

## Healthcheck

Validar si el cliente exige healthcheck.

Si se usa Spring Boot Actuator, confirmar dependencia y exposición de endpoint.

Ejemplo posible:

```text
GET /actuator/health
```

No exponer endpoints de actuator sensibles sin configuración de seguridad.

## Seguridad

No asumir mecanismo de seguridad.

Validar:

- JWT;
- OAuth2;
- Basic Auth;
- API Key;
- gateway externo;
- integración con sistema corporativo;
- headers obligatorios;
- roles o permisos;
- endpoints públicos y privados.

Si falta información, marcar como:

```text
REQUIERE VALIDACIÓN: confirmar mecanismo de seguridad.
```

## Integraciones externas

Cuando el microservicio consuma otros sistemas:

- identificar sistema destino;
- identificar endpoint;
- identificar contrato;
- identificar autenticación;
- identificar timeouts;
- identificar reintentos;
- identificar errores esperados;
- identificar mapeo de campos;
- identificar trazabilidad.

No inventar contratos externos.

## Checklist técnico inicial

Antes de crear el microservicio:

- ¿Está confirmado que debe ser un servicio nuevo?
- ¿Existe OK del líder de integración o arquitecto?
- ¿Existe objetivo funcional?
- ¿Existe contrato API?
- ¿Existe documento funcional?
- ¿Existe modelo de datos?
- ¿Existe definición de seguridad?
- ¿Existe definición de tecnología?
- ¿Existe repositorio?
- ¿Existe estructura esperada?
- ¿Existe ejemplo oficial aplicable?
- ¿Existe definición de ambientes?
- ¿Existe pipeline?

## Checklist de estructura

Validar que el proyecto tenga:

- clase principal Spring Boot;
- estructura de packages clara;
- controller;
- service;
- repository / DAO si aplica;
- DTOs;
- configuración externalizada;
- manejo de errores;
- logging;
- README;
- Swagger/OpenAPI;
- pruebas;
- build file Maven o Gradle;
- .gitignore;
- configuración por ambiente si aplica.

## Checklist antes de entregar

Antes de considerar el microservicio listo:

- ¿Compila correctamente?
- ¿Ejecuta localmente?
- ¿Tiene README completo?
- ¿Tiene Swagger/OpenAPI?
- ¿Tiene documento funcional asociado?
- ¿Tiene modelo de datos si aplica?
- ¿Tiene variables externalizadas?
- ¿No tiene secretos hardcodeados?
- ¿Tiene manejo de errores?
- ¿Tiene logging?
- ¿Tiene pruebas?
- ¿Tiene estructura clara?
- ¿Respeta estándares Java?
- ¿Se revisó SonarLint si aplica?
- ¿Se validó con líder de integración o arquitecto?
- ¿Se documentaron brechas pendientes?

## Qué no debe hacer el agente

- No asumir que el ejemplo de microservicio es obligatorio en todos sus detalles.
- No copiar nombres, paquetes o endpoints del ejemplo sin adaptarlos.
- No inventar reglas de negocio.
- No inventar payloads.
- No inventar mecanismo de autenticación.
- No inventar modelo de datos.
- No inventar variables de ambiente.
- No dejar credenciales en código.
- No crear arquitectura sobredimensionada sin necesidad.
- No cambiar tecnología sin validación.
- No modernizar código legacy si el alcance no lo pide.
- No presentar supuestos como decisiones confirmadas.

## Salida esperada del agente

Cuando esta skill se use para crear una base de microservicio, el agente debe entregar:

1. Supuestos declarados.
2. Brechas pendientes.
3. Estructura propuesta.
4. Archivos principales.
5. Comandos de ejecución.
6. Checklist de validación.

Cuando esta skill se use para revisar un microservicio existente, el agente debe entregar:

1. Resumen del estado.
2. Cumplimientos detectados.
3. Incumplimientos detectados.
4. Riesgos.
5. Pendientes de validación.
6. Recomendaciones concretas.
