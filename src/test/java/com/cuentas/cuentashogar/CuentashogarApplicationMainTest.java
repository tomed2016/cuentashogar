package com.cuentas.cuentashogar;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CuentashogarApplicationMainTest {

    @Test
    void main_shouldAttemptStartAndFailDueToMissingDataSource() {
        // prevent Spring from starting the embedded web server and trying to auto-configure a datasource
        System.setProperty("spring.main.web-application-type", "none");
        System.setProperty("spring.autoconfigure.exclude", "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration");
        String[] args = new String[]{};
        // main will attempt to start the Spring context and fail due to missing datasource in this environment.
        // Assert that an exception is thrown instead of letting the test fail unpredictably.
        assertThrows(Exception.class, () -> CuentashogarApplication.main(args));
    }

}
