# Prompt maestro para Codex - Proyecto ZS

Estás trabajando en un proyecto asociado a ZS / Zurich Santander / cliente relacionado.

Antes de responder, generar código, refactorizar, revisar archivos o proponer arquitectura, debes leer y aplicar las skills disponibles en este repositorio.

## Skills obligatorias iniciales

Lee siempre primero:

1. `skills/zs-context/SKILL.md`

Luego, según la tarea, lee una o más de estas skills:

- Para código Java:
  - `skills/zs-java-standards/SKILL.md`

- Para iniciar o revisar un nuevo servicio:
  - `skills/zs-new-service-onboarding/SKILL.md`

- Para documentación funcional de API:
  - `skills/zs-api-functional-doc/SKILL.md`

- Para estructura de microservicio:
  - `skills/zs-microservice-template/SKILL.md`

## Reglas generales

- No inventes reglas del cliente.
- No asumas que los documentos son definitivos.
- Diferencia entre CONFIRMADO, PROVISIONAL, REQUIERE VALIDACIÓN y SUPUESTO.
- Si hay contradicción entre documentos, marca la brecha.
- No trates ejemplos como reglas obligatorias.
- Antes de cambiar arquitectura, justifica el cambio.
- Antes de generar código, declara supuestos.
- Si falta información, propón preguntas concretas para el líder de integración o arquitecto.
- Si puedes compilar o ejecutar pruebas, hazlo.
- Si no puedes compilar, indícalo claramente.

## Forma de responder

Cuando revises código, responde con:

1. Resumen corto.
2. Skill aplicada.
3. Problemas detectados.
4. Cambios sugeridos.
5. Riesgos o pendientes.
6. Comandos sugeridos para validar.

Cuando generes código, responde con:

1. Supuestos.
2. Archivos afectados.
3. Código propuesto.
4. Explicación breve.
5. Validaciones pendientes.
6. Comandos para compilar o probar.