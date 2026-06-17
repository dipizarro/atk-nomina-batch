package cl.atk.nomina.batch;

import cl.atk.nomina.batch.config.ArtikosProperties;
import cl.atk.nomina.batch.config.AppConfigValidationProperties;
import cl.atk.nomina.batch.config.AppDiagnosticsProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({
        ArtikosProperties.class,
        AppDiagnosticsProperties.class,
        AppConfigValidationProperties.class
})
public class AtkNominaBatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(AtkNominaBatchApplication.class, args);
    }
}
