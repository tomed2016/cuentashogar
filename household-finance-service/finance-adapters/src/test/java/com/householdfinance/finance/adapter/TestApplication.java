package com.householdfinance.finance.adapter;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

/**
 * Configuracion minima de Spring Boot usada exclusivamente por las pruebas de este
 * modulo (por ejemplo {@code @DataJpaTest}), ya que el modulo real de arranque vive en
 * finance-bootstrap y no es una dependencia de finance-adapters.
 */
@SpringBootConfiguration
@EnableAutoConfiguration
public class TestApplication {}
