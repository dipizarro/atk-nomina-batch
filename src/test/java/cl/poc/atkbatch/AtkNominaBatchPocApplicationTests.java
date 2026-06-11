package cl.poc.atkbatch;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
class AtkNominaBatchPocApplicationTests {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void contextLoads() {
    }

    @Test
    void springBatchMetadataTablesAreCreated() {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'BATCH_JOB_INSTANCE'",
                Integer.class);

        assertThat(count).isEqualTo(1);
    }
}
