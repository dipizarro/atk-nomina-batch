package cl.poc.atkbatch.batch.writer;

import cl.poc.atkbatch.domain.ResultadoDocumento;
import cl.poc.atkbatch.service.BatchResultStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

public class NominaDocumentoItemWriter implements ItemWriter<ResultadoDocumento> {

    private static final Logger LOGGER = LoggerFactory.getLogger(NominaDocumentoItemWriter.class);

    private final BatchResultStore batchResultStore;
    private final Long jobExecutionId;

    public NominaDocumentoItemWriter(BatchResultStore batchResultStore, Long jobExecutionId) {
        this.batchResultStore = batchResultStore;
        this.jobExecutionId = jobExecutionId;
    }

    @Override
    public void write(Chunk<? extends ResultadoDocumento> chunk) {
        long okCount = chunk.getItems().stream().filter(ResultadoDocumento::isOk).count();
        long nokCount = chunk.size() - okCount;

        LOGGER.info(
                "Chunk recibido para jobExecutionId={}: total={}, ok={}, nok={}",
                jobExecutionId,
                chunk.size(),
                okCount,
                nokCount);

        batchResultStore.addResults(jobExecutionId, chunk.getItems());
    }
}
