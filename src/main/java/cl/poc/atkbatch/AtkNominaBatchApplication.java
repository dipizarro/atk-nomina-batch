package cl.poc.atkbatch;

import cl.poc.atkbatch.config.ArtikosProperties;
import cl.poc.atkbatch.config.AppConfigValidationProperties;
import cl.poc.atkbatch.config.AppDiagnosticsProperties;
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
