Estás trabajando en una POC corporativa Java/Spring Boot llamada `atk-nomina-batch-poc`.

Objetivo funcional:
Crear una aplicación Spring Boot con Spring Batch y capa REST. La capa REST expone un endpoint que actúa como interruptor del batch. Al recibir una solicitud, debe iniciar un proceso batch asincrónico, responder inmediatamente con el ID de ejecución y estado inicial, y permitir consultar luego el estado de ejecución.

Contexto de integración:
La POC simula la integración Artikos SAF / ERP descrita en `docs/reference/Definición_Integración_Nominas_ATK_v1_4_1.pdf`.

Flujo funcional resumido:

1. El ERP solicita una nómina de documentos.
2. Artikos responde con una nómina o con mensaje de que no hay nóminas.
3. Si existe nómina, ERP acusa recibo mediante `NOMFACTCONFIR`.
4. ERP procesa internamente los documentos.
5. ERP informa resultado mediante `NOMFACTRES`, indicando documentos OK y NOK.

Alcance de la POC:

* No consumir servicios reales Artikos.
* Usar un XML SOAP local en `src/main/resources/samples/nomina-soap-local.xml`.
* Parsear la nómina y sus documentos.
* Simular procesamiento masivo iterando 100 veces sobre los documentos del XML.
* Mantener estado de ejecución usando metadata de Spring Batch.
* Exponer endpoints REST para iniciar y consultar estado.
* Generar resumen final del procesamiento.

Skills obligatorias:
Antes de generar código, lee y aplica:

1. `skills/zs-agent-skills/skills/zs-context/SKILL.md`
2. `skills/zs-agent-skills/skills/zs-java-standards/SKILL.md`
3. `skills/zs-agent-skills/skills/zs-new-service-onboarding/SKILL.md`
4. `skills/zs-agent-skills/skills/zs-microservice-template/SKILL.md`

Reglas:

* No inventes reglas del cliente.
* Diferencia CONFIRMADO, PROVISIONAL, SUPUESTO y REQUIERE VALIDACIÓN.
* Usa nombres claros en inglés para clases, métodos y paquetes.
* Mantén paquetes en minúscula.
* Expón OpenAPI/SpringDoc para los endpoints.
* Agrega tests cuando crees lógica de negocio.
* Cada sprint debe dejar el proyecto compilando.
* Al terminar cambios, ejecutar `mvn clean test`.
* Si no puedes ejecutar un comando, decláralo explícitamente.

Formato de respuesta esperado:

1. Supuestos.
2. Archivos modificados.
3. Cambios realizados.
4. Comandos ejecutados.
5. Resultado de validación.
6. Próximo commit sugerido.
