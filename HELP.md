# Getting Started

### Reference Documentation
For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/4.1.1/maven-plugin)
* [Create an OCI image](https://docs.spring.io/spring-boot/4.1.1/maven-plugin/build-image.html)
* [Spring Web](https://docs.spring.io/spring-boot/4.1.1/reference/web/servlet.html)
* [Spring Data JPA](https://docs.spring.io/spring-boot/4.1.1/reference/data/sql.html#data.sql.jpa-and-spring-data)
* [Docker Compose Support](https://docs.spring.io/spring-boot/4.1.1/reference/features/dev-services.html#features.dev-services.docker-compose)

### Guides
The following guides illustrate how to use some features concretely:

* [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
* [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
* [Building REST services with Spring](https://spring.io/guides/tutorials/rest/)
* [Accessing Data with JPA](https://spring.io/guides/gs/accessing-data-jpa/)

### Docker Compose support
This project contains a Docker Compose file named `compose.yaml`.
In this file, the following services have been defined:

* postgres: [`postgres:latest`](https://hub.docker.com/_/postgres)

Please review the tags of the used images and set them to the same as you're running in production.

### Maven Parent overrides

Due to Maven's design, elements are inherited from the parent POM to the project POM.
While most of the inheritance is fine, it also inherits unwanted elements like `<license>` and `<developers>` from the parent.
To prevent this, the project POM contains empty overrides for these elements.
If you manually switch to a different parent and actually want the inheritance, you need to remove those overrides.


### Nota sobre errores de DataSource en tests

Si al ejecutar los tests ves un error como:

```
Failed to configure a DataSource: 'url' attribute is not specified and no embedded datasource could be configured.
Reason: Failed to determine a suitable driver class
```

Significa que Spring Boot intentó auto-configurar una fuente de datos (por ejemplo porque `spring-boot-starter-data-jpa` está en las dependencias) pero no encontró ni una URL ni un driver JDBC en el classpath.

Soluciones habituales:
- Añadir una base de datos embebida para tests (recomendado): añadir `com.h2database:h2` en scope `test` al `pom.xml` permite que Spring cree un DataSource en memoria durante las pruebas.
- Proveer `spring.datasource.*` en `src/test/resources/application.properties` apuntando a una BD de test.
- Evitar que los tests carguen el contexto de Spring si no lo necesitan (no usar `@SpringBootTest`).
- Excluir la auto-configuración de DataSource en tests concretos con `spring.autoconfigure.exclude` o `@EnableAutoConfiguration(exclude = ...)`.

En este repositorio se añadió H2 en scope `test` para que las pruebas unitarias y de contexto se ejecuten sin requerir PostgreSQL.

