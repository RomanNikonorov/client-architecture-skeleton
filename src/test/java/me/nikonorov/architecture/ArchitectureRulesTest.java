package me.nikonorov.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import me.nikonorov.ClientArchitectureSkeletonApplication;
import me.nikonorov.fanout.AsyncProperties;
import me.nikonorov.fanout.FanOutExecutor;
import me.nikonorov.clients.application.port.ExternalSystemAClient;
import me.nikonorov.clients.application.usecase.ClientAggregationUseCase;
import me.nikonorov.credit.application.port.CreditScoringClient;
import me.nikonorov.credit.application.usecase.CreditDecisionUseCase;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class ArchitectureRulesTest {

    private final JavaClasses classes = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("me.nikonorov");

    @Test
    void apiDoesNotDependOnInfrastructure() {
        noClasses()
                .that().resideInAPackage("..api..")
                .should().dependOnClassesThat().resideInAPackage("..infrastructure..")
                .allowEmptyShould(true)
                .check(classes);
    }

    @Test
    void applicationDoesNotDependOnAdapterTechnologies() {
        noClasses()
                .that().resideInAPackage("..application..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "jakarta.persistence..",
                        "org.springframework.web..",
                        "org.springframework.grpc..",
                        "io.grpc..",
                        "..infrastructure..")
                .allowEmptyShould(true)
                .check(classes);
    }

    @Test
    void useCasesDoNotOwnConcurrencyPrimitives() {
        noClasses()
                .that().resideInAPackage("..application..")
                .and().haveSimpleNameEndingWith("UseCase")
                .should().dependOnClassesThat().resideInAnyPackage("java.util.concurrent..")
                .allowEmptyShould(true)
                .check(classes);
    }

    @Test
    void domainDoesNotDependOnFrameworkOrAdapterTechnologies() {
        noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "jakarta.persistence..",
                        "org.springframework..",
                        "io.grpc..",
                        "..infrastructure..",
                        "..api..")
                .allowEmptyShould(true)
                .check(classes);
    }

    @Test
    void applicationResponsibilitiesStayInDedicatedPackages() {
        assertThat(classes.get(ClientAggregationUseCase.class).getPackageName())
                .isEqualTo("me.nikonorov.clients.application.usecase");
        assertThat(classes.get(ExternalSystemAClient.class).getPackageName())
                .isEqualTo("me.nikonorov.clients.application.port");
        assertThat(classes.get(FanOutExecutor.class).getPackageName())
                .isEqualTo("me.nikonorov.fanout");
        assertThat(classes.get(AsyncProperties.class).getPackageName())
                .isEqualTo("me.nikonorov.fanout");
        assertThat(classes.get(CreditDecisionUseCase.class).getPackageName())
                .isEqualTo("me.nikonorov.credit.application.usecase");
        assertThat(classes.get(CreditScoringClient.class).getPackageName())
                .isEqualTo("me.nikonorov.credit.application.port");
    }

    @Test
    void inboundAdaptersDependOnApplicationLayer() {
        classes()
                .that().resideInAnyPackage("..api.rest..", "..api.grpc..")
                .and().areTopLevelClasses()
                .and().resideOutsideOfPackage("..generated..")
                .should().dependOnClassesThat().resideInAPackage("..application..")
                .allowEmptyShould(true)
                .check(classes);
    }

    @Test
    void creditContextDoesNotDependOnClientAggregationLayers() {
        noClasses()
                .that().resideInAPackage("..credit..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "me.nikonorov.clients.api..",
                        "me.nikonorov.clients.domain..",
                        "me.nikonorov.clients.application..",
                        "me.nikonorov.clients.infrastructure..")
                .allowEmptyShould(true)
                .check(classes);
    }

    @Test
    void clientContextDoesNotDependOnCreditLayers() {
        noClasses()
                .that().resideInAPackage("..clients..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "me.nikonorov.credit.api..",
                        "me.nikonorov.credit.domain..",
                        "me.nikonorov.credit.application..",
                        "me.nikonorov.credit.infrastructure..")
                .allowEmptyShould(true)
                .check(classes);
    }
}
