package cl.poc.atkbatch.api.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class NominaBatchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void startBatchRespondsImmediately() throws Exception {
        long startedAt = System.nanoTime();

        MvcResult startResult = mockMvc.perform(post("/api/v1/nominas/batch/start"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.jobExecutionId", notNullValue()))
                .andExpect(jsonPath("$.jobName", is("nominaDocumentosContablesJob")))
                .andExpect(jsonPath("$.status", is("STARTING")))
                .andExpect(jsonPath("$.message", is("Batch iniciado correctamente")))
                .andReturn();

        long elapsedMillis = (System.nanoTime() - startedAt) / 1_000_000;
        assertThat(elapsedMillis).isLessThan(2_000L);

        String jobExecutionId = extractJobExecutionId(startResult);
        waitForJobStatus(jobExecutionId, "COMPLETED");
    }

    @Test
    void getBatchStatusReturnsExecutionMetadata() throws Exception {
        MvcResult startResult = mockMvc.perform(post("/api/v1/nominas/batch/start"))
                .andExpect(status().isAccepted())
                .andReturn();

        String jobExecutionId = extractJobExecutionId(startResult);

        mockMvc.perform(get("/api/v1/nominas/batch/{jobExecutionId}", jobExecutionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobExecutionId").value(Integer.parseInt(jobExecutionId)))
                .andExpect(jsonPath("$.jobName", is("nominaDocumentosContablesJob")))
                .andExpect(jsonPath("$.status", notNullValue()))
                .andExpect(jsonPath("$.exitStatus", notNullValue()));

        waitForJobStatus(jobExecutionId, "COMPLETED");
    }

    @Test
    void getBatchSummaryReturnsChunkProcessingTotals() throws Exception {
        MvcResult startResult = mockMvc.perform(post("/api/v1/nominas/batch/start"))
                .andExpect(status().isAccepted())
                .andReturn();

        String jobExecutionId = extractJobExecutionId(startResult);

        waitForJobStatus(jobExecutionId, "COMPLETED");

        mockMvc.perform(get("/api/v1/nominas/batch/{jobExecutionId}/summary", jobExecutionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobExecutionId").value(Integer.parseInt(jobExecutionId)))
                .andExpect(jsonPath("$.status", is("COMPLETED")))
                .andExpect(jsonPath("$.totalNominas").value(1000))
                .andExpect(jsonPath("$.totalDocuments").value(1000))
                .andExpect(jsonPath("$.totalOk").value(1000))
                .andExpect(jsonPath("$.totalNok").value(0))
                .andExpect(jsonPath("$.totalConciliaciones").value(2000))
                .andExpect(jsonPath("$.totalDistribuciones").value(2000))
                .andExpect(jsonPath("$.nomfactresGenerated").value(1000));
    }

    @Test
    void getNominaResultReturnsGeneratedNomfactresXml() throws Exception {
        MvcResult startResult = mockMvc.perform(post("/api/v1/nominas/batch/start"))
                .andExpect(status().isAccepted())
                .andReturn();

        String jobExecutionId = extractJobExecutionId(startResult);

        waitForJobStatus(jobExecutionId, "COMPLETED");

        mockMvc.perform(get("/api/v1/nominas/batch/{jobExecutionId}/results/{numeroNomina}", jobExecutionId, 15960))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobExecutionId").value(Integer.parseInt(jobExecutionId)))
                .andExpect(jsonPath("$.numeroNomina").value(15960))
                .andExpect(jsonPath("$.totalDocuments").value(1))
                .andExpect(jsonPath("$.totalOk").value(1))
                .andExpect(jsonPath("$.totalNok").value(0))
                .andExpect(jsonPath("$.nomfactresXml", org.hamcrest.Matchers.containsString("NOMFACTRES")));
    }

    private String extractJobExecutionId(MvcResult result) throws Exception {
        String responseBody = result.getResponse().getContentAsString();
        return responseBody.replaceAll(".*\"jobExecutionId\":(\\d+).*", "$1");
    }

    private void waitForJobStatus(String jobExecutionId, String expectedStatus) throws Exception {
        long deadline = System.currentTimeMillis() + 15_000L;
        String currentStatus = null;

        while (System.currentTimeMillis() < deadline) {
            MvcResult result = mockMvc.perform(get("/api/v1/nominas/batch/{jobExecutionId}", jobExecutionId))
                    .andExpect(status().isOk())
                    .andReturn();
            String responseBody = result.getResponse().getContentAsString();
            currentStatus = responseBody.replaceAll(".*\"status\":\"([^\"]+)\".*", "$1");
            if (expectedStatus.equals(currentStatus)) {
                return;
            }
            Thread.sleep(250L);
        }

        assertThat(currentStatus).isEqualTo(expectedStatus);
    }
}
