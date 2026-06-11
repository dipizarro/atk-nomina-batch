Sprint 1 — REST como interruptor del batch

Objetivo: crear endpoint que inicie el batch y responda inmediatamente con jobExecutionId.

Commits:

git commit -m "feat: add REST endpoint to launch nomina batch job"
git commit -m "feat: add batch execution status endpoint"
git commit -m "test: add batch launcher service tests"

Entregables:

POST /api/v1/nominas/batch/start
GET /api/v1/nominas/batch/{jobExecutionId}