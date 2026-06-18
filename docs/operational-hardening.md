# Operational Hardening

## Batch queue consumption

The real Artikos flow consumes nominas as a queue.

`ArtikosNominaItemReader` calls `NOMFACTERP` repeatedly:

- when Artikos returns a nomina, the reader emits one item;
- when Artikos returns `No hay nominas para procesar`, the reader returns `null` and the step finishes normally;
- when `maxNominas` is reached, the reader returns `null` and logs that the operational safety limit was reached.

`maxNominas` is not the business completion rule. It is a guardrail for controlled runs, QA tests and unexpected long loops.

## Processing unit

The batch item is a nomina, not a document.

Each nomina can contain a variable number of documents, conciliations and distributions. `ResultadoNomina` totals must always be calculated from the actual parsed XML.

## Failure policy

The job should finish `COMPLETED` when the queue is empty or when the safety limit is reached without errors.

The job should finish `FAILED` for:

- technical `NOMFACTERP` fetch failures;
- rejected `NOMFACTCONFIR`;
- rejected or failed `NOMFACTRES`;
- Oracle failures while writing `CONTROL_NOMINA`.

## Operational controls

Recommended production controls:

- set `atk.batch.max-nominas` to a safe upper bound for the execution window;
- keep `atk.batch.real.chunk-size=1` while Artikos state transitions are one nomina at a time;
- monitor logs for the `maxNominas` safety-limit message;
- review `CONTROL_NOMINA` after failed jobs before retrying.
