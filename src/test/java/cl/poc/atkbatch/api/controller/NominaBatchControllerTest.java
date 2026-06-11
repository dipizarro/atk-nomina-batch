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
    void startBatchRespondsBeforeTaskletFinishes() throws Exception {
        long startedAt = System.nanoTime();

        mockMvc.perform(post("/api/v1/nominas/batch/start"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.jobExecutionId", notNullValue()))
                .andExpect(jsonPath("$.jobName", is("nominaDocumentosContablesJob")))
                .andExpect(jsonPath("$.status", is("STARTING")))
                .andExpect(jsonPath("$.message", is("Batch iniciado correctamente")));

        long elapsedMillis = (System.nanoTime() - startedAt) / 1_000_000;
        assertThat(elapsedMillis).isLessThan(2_000L);
    }

    @Test
    void getBatchStatusReturnsExecutionMetadata() throws Exception {
        MvcResult startResult = mockMvc.perform(post("/api/v1/nominas/batch/start"))
                .andExpect(status().isAccepted())
                .andReturn();

        String responseBody = startResult.getResponse().getContentAsString();
        String jobExecutionId = responseBody.replaceAll(".*\"jobExecutionId\":(\\d+).*", "$1");

        mockMvc.perform(get("/api/v1/nominas/batch/{jobExecutionId}", jobExecutionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobExecutionId").value(Integer.parseInt(jobExecutionId)))
                .andExpect(jsonPath("$.jobName", is("nominaDocumentosContablesJob")))
                .andExpect(jsonPath("$.status", notNullValue()))
                .andExpect(jsonPath("$.exitStatus", notNullValue()));
    }
}
