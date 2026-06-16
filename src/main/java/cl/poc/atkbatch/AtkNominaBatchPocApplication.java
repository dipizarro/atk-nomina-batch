package cl.poc.atkbatch;

import cl.poc.atkbatch.config.ArtikosProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(ArtikosProperties.class)
public class AtkNominaBatchPocApplication {

    public static void main(String[] args) {
        SpringApplication.run(AtkNominaBatchPocApplication.class, args);
    }
}
