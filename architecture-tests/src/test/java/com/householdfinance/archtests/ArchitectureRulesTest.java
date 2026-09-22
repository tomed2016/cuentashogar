package com.householdfinance.archtests;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Pruebas de arquitectura que validan que todos los servicios (identity-service,
 * household-finance-service) respeten las reglas de arquitectura hexagonal / DDD:
 *
 * <ul>
 *   <li>El dominio no depende de Spring, JPA, Hibernate ni Jackson.</li>
 *   <li>La capa de aplicacion no depende de los adaptadores.</li>
 *   <li>Los controladores REST no acceden directamente a repositorios JPA/persistencia.</li>
 *   <li>No existen dependencias ciclicas entre los bounded contexts de primer nivel.</li>
 *   <li>Los contextos (identity, finance) no se acoplan entre si.</li>
 * </ul>
 */
class ArchitectureRulesTest {

    private static JavaClasses allClasses;

    @BeforeAll
    static void importClasses() {
        allClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.householdfinance");
    }

    @Test
    void domainShouldNotDependOnSpringFramework() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "org.springframework..",
                        "org.springframework.boot..");

        rule.check(allClasses);
    }

    @Test
    void domainShouldNotDependOnJpaOrHibernate() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "jakarta.persistence..",
                        "org.hibernate..");

        rule.check(allClasses);
    }

    @Test
    void domainShouldNotDependOnJackson() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAnyPackage("com.fasterxml.jackson..");

        rule.check(allClasses);
    }

    @Test
    void domainShouldNotDependOnHttpOrServletApi() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAnyPackage("jakarta.servlet..", "jakarta.ws.rs..");

        rule.check(allClasses);
    }

    @Test
    void applicationShouldNotDependOnAdapters() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..application..")
                .should().dependOnClassesThat().resideInAPackage("..adapters..");

        rule.check(allClasses);
    }

    @Test
    void applicationShouldNotDependOnSpringWebOrJpa() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..application..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "org.springframework.web..",
                        "jakarta.persistence..",
                        "org.hibernate..");

        rule.check(allClasses);
    }

    @Test
    void restControllersShouldNotAccessPersistenceAdaptersDirectly() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..adapters.in.rest..")
                .should().dependOnClassesThat().resideInAPackage("..adapters.out.persistence..");

        rule.check(allClasses);
    }

    @Test
    void restControllersShouldNotDependOnSpringDataJpaRepositories() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..adapters.in.rest..")
                .should().dependOnClassesThat().resideInAnyPackage("org.springframework.data.jpa..");

        rule.check(allClasses);
    }

    @Test
    void jpaEntitiesShouldOnlyLiveInPersistenceAdapterPackage() {
        ArchRule rule = classes()
                .that().areAnnotatedWith("jakarta.persistence.Entity")
                .should().resideInAPackage("..adapters.out.persistence..");

        rule.check(allClasses);
    }

    @Test
    void boundedContextsShouldBeFreeOfCycles() {
        ArchRule rule = slices()
                .matching("com.householdfinance.(*)..")
                .should().beFreeOfCycles();

        rule.check(allClasses);
    }

    @Test
    void identityAndFinanceContextsShouldNotDependOnEachOthersInternals() {
        ArchRule identityDoesNotDependOnFinance = noClasses()
                .that().resideInAPackage("com.householdfinance.identity..")
                .should().dependOnClassesThat().resideInAPackage("com.householdfinance.finance..")
                .allowEmptyShould(true);

        ArchRule financeDoesNotDependOnIdentity = noClasses()
                .that().resideInAPackage("com.householdfinance.finance..")
                .should().dependOnClassesThat().resideInAPackage("com.householdfinance.identity..")
                .allowEmptyShould(true);

        identityDoesNotDependOnFinance.check(allClasses);
        financeDoesNotDependOnIdentity.check(allClasses);
    }
}

