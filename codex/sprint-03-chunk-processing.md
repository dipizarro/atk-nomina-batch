Sprint 3 — Procesamiento masivo simulado

Objetivo: iterar 100 veces sobre los documentos del XML para simular carga masiva.

Commits:

git commit -m "feat: process nomina documents using Spring Batch chunks"
git commit -m "feat: simulate massive processing with configurable iteration count"

Configuración:

atk:
  batch:
    simulation-iterations: 100
    chunk-size: 20