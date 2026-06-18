package cl.atk.nomina.batch;

import cl.atk.nomina.batch.config.ArtikosProperties;
import cl.atk.nomina.batch.config.ArtikosHttpProperties;
import cl.atk.nomina.batch.config.ArtikosRetryProperties;
import cl.atk.nomina.batch.config.AppConfigValidationProperties;
import cl.atk.nomina.batch.config.AppDiagnosticsProperties;
import cl.atk.nomina.batch.config.BatchExecutionProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({
        ArtikosProperties.class,
        ArtikosHttpProperties.class,
        ArtikosRetryProperties.class,
        AppDiagnosticsProperties.class,
        AppConfigValidationProperties.class,
        BatchExecutionProperties.class
})
public class AtkNominaBatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(AtkNominaBatchApplication.class, args);
    }
}
