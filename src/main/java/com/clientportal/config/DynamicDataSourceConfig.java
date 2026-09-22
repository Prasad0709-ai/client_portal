package com.clientportal.config;

import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;

@Configuration
@Profile("!test")
public class DynamicDataSourceConfig {

    private static final Logger log = LoggerFactory.getLogger(DynamicDataSourceConfig.class);

    @Value("${spring.datasource.url:jdbc:mysql://localhost:3306/clientportal_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true}")
    private String mysqlUrl;

    @Value("${spring.datasource.username:root}")
    private String mysqlUsername;

    @Value("${spring.datasource.password:}")
    private String mysqlPassword;

    @Bean
    @Primary
    public DataSource dataSource() {
        boolean mysqlAvailable = isMySqlConnectable(mysqlUrl, mysqlUsername, mysqlPassword);

        HikariDataSource ds = new HikariDataSource();
        if (mysqlAvailable) {
            log.info("[DATA-LAYER] MySQL 8.x engine successfully authenticated on localhost:3306. Connecting to MySQL...");
            ds.setDriverClassName("com.mysql.cj.jdbc.Driver");
            ds.setJdbcUrl(mysqlUrl);
            ds.setUsername(mysqlUsername);
            ds.setPassword(mysqlPassword);
            ds.setMaximumPoolSize(10);
            ds.setConnectionTimeout(3000);
        } else {
            log.warn("[DATA-LAYER] MySQL 8.x not ready or credentials rejected on localhost:3306. Gracefully engaging resilient embedded database engine with MySQL compatibility mode.");
            ds.setDriverClassName("org.h2.Driver");
            ds.setJdbcUrl("jdbc:h2:mem:clientportal_db;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=MySQL");
            ds.setUsername("sa");
            ds.setPassword("");
            ds.setMaximumPoolSize(10);
        }
        return ds;
    }

    private boolean isMySqlConnectable(String url, String username, String password) {
        if (!isPortOpen("localhost", 3306, 1000)) {
            return false;
        }
        try {
            DriverManager.setLoginTimeout(2);
            try (Connection conn = DriverManager.getConnection(url, username, password)) {
                return conn != null && !conn.isClosed();
            }
        } catch (Exception e) {
            log.warn("[DATA-LAYER] MySQL port 3306 is open, but JDBC authentication/connection failed ({}). Falling back to embedded H2 database.", e.getMessage());
            return false;
        }
    }

    private boolean isPortOpen(String host, int port, int timeoutMs) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeoutMs);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
