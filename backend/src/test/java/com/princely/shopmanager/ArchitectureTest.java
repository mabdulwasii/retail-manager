package com.princely.shopmanager;

import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

@AnalyzeClasses(
    packages = "com.princely.shopmanager",
    importOptions = ImportOption.DoNotIncludeTests.class
)
class ArchitectureTest {

    @ArchTest
    static final ArchRule modules_should_respect_boundaries =
        noClasses()
            .that().resideInAPackage("com.princely.shopmanager.core..")
            .should().dependOnClassesThat()
            .resideInAnyPackage(
                "com.princely.shopmanager.sales..",
                "com.princely.shopmanager.investment..",
                "com.princely.shopmanager.analytics.."
            );

    @ArchTest
    static final ArchRule auth_module_should_only_depend_on_core_and_shared =
        classes()
            .that().resideInAPackage("com.princely.shopmanager.auth..")
            .should().onlyDependOnClassesThat()
            .resideInAnyPackage(
                "com.princely.shopmanager.auth..",
                "com.princely.shopmanager.core..",
                "com.princely.shopmanager.shared..",
                "java..",
                "javax..",
                "jakarta..",
                "org.springframework..",
                "org.keycloak..",
                "org.slf4j..",
                "lombok..",
                "com.fasterxml.jackson.."
            );

    @ArchTest
    static final ArchRule investment_module_dependencies =
        classes()
            .that().resideInAPackage("com.princely.shopmanager.investment..")
            .should().onlyDependOnClassesThat()
            .resideInAnyPackage(
                "com.princely.shopmanager.investment..",
                "com.princely.shopmanager.core..",
                "com.princely.shopmanager.shared..",
                "com.princely.shopmanager.sales.domain..",
                "com.princely.shopmanager.sales.repository..",
                "java..",
                "javax..",
                "jakarta..",
                "org.springframework..",
                "org.slf4j..",
                "lombok..",
                "io.swagger.v3.oas.annotations.."
            );

     @ArchTest
     static final ArchRule layered_architecture = layeredArchitecture()
         .consideringOnlyDependenciesInLayers()
         .layer("Controller").definedBy("..controller..")
         .layer("Service").definedBy("..service..")
         .layer("Repository").definedBy("..repository..")
         .whereLayer("Controller").mayNotBeAccessedByAnyLayer()
         .whereLayer("Service").mayOnlyBeAccessedByLayers("Controller", "Service")
         .whereLayer("Repository").mayOnlyBeAccessedByLayers("Service");

    @ArchTest
    static final ArchRule entities_should_be_in_domain_package =
        classes()
            .that().areAnnotatedWith(jakarta.persistence.Entity.class)
            .should().resideInAPackage("..domain..");

     @ArchTest
     static final ArchRule repositories_should_be_interfaces =
         classes()
             .that().resideInAPackage("..repository..")
             .and().resideOutsideOfPackage("..repository.base..")
             .should().beInterfaces();

     @ArchTest
     static final ArchRule services_should_be_annotated =
         classes()
             .that().resideInAPackage("..service..")
             .and().areNotInterfaces()
             .and().areTopLevelClasses()
             .and().doNotHaveModifier(JavaModifier.ABSTRACT)
             .should().beAnnotatedWith(org.springframework.stereotype.Service.class);

     @ArchTest
     static final ArchRule controllers_should_be_annotated =
         classes()
             .that().resideInAPackage("..controller..")
             .and().areTopLevelClasses()
             .should().beAnnotatedWith(org.springframework.web.bind.annotation.RestController.class)
             .orShould().beAnnotatedWith(org.springframework.stereotype.Controller.class);
}