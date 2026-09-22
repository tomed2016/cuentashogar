package com.householdfinance.identity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada del microservicio de identidad. El escaneo de componentes parte de este
 * paquete raiz para incluir los adaptadores de entrada/salida y las configuraciones del modulo
 * {@code identity-adapters}, sin necesidad de anotaciones de framework en el dominio ni en la
 * capa de aplicacion.
 */
@SpringBootApplication
public class IdentityServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(IdentityServiceApplication.class, args);
    }
}
