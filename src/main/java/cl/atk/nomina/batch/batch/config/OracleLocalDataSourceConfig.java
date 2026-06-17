package cl.atk.nomina.batch.batch.config;

import java.sql.SQLException;
import java.util.Properties;
import javax.sql.DataSource;
import oracle.jdbc.pool.OracleDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("local")
public class OracleLocalDataSourceConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(OracleLocalDataSourceConfig.class);

    @Bean
    public DataSource dataSource(
            @Value("${atk.oracle.url}") String url,
            @Value("${atk.oracle.username}") String username,
            @Value("${atk.oracle.password}") String password,
            @Value("${atk.oracle.driver-class-name}") String driverClassName,
            @Value("${atk.oracle.tns-admin:}") String tnsAdmin) throws SQLException {
        LOGGER.info("Configuring Oracle DataSource url={} username={} driver={} tnsAdmin={} passwordPresent={}",
                url, username, driverClassName, tnsAdmin, password != null && !password.isBlank());

        if (tnsAdmin != null && !tnsAdmin.isBlank()) {
            System.setProperty("oracle.net.tns_admin", tnsAdmin);
        }

        OracleDataSource dataSource = new OracleDataSource();
        dataSource.setUser(username);
        dataSource.setPassword(password);
        dataSource.setURL(url);

        Properties properties = new Properties();
        properties.setProperty("MinLimit", "10");
        properties.setProperty("MaxLimit", "20");
        properties.setProperty("InitialLimit", "10");
        properties.setProperty("ValidateConnection", "true");
        dataSource.setConnectionProperties(properties);
        dataSource.setImplicitCachingEnabled(true);
        dataSource.setFastConnectionFailoverEnabled(true);
        return dataSource;
    }
}
