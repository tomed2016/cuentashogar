package com.householdfinance.identity.adapters;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

/**
 * Configuracion minima de Spring Boot usada exclusivamente por las pruebas de slice
 * ({@code @WebMvcTest}) de este modulo, ya que el modulo bootstrap real vive en
 * identity-bootstrap y no es una dependencia de identity-adapters. No se declara
 * {@code @ComponentScan}: cada prueba de slice debe importar explicitamente el
 * controlador bajo prueba mediante {@code @Import(...)} para evitar que se instancien
 * otros controladores del mismo paquete.
 */
@SpringBootConfiguration
@EnableAutoConfiguration
public class TestApplication {}
