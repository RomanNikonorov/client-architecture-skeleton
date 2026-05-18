package me.nikonorov.clients.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import me.nikonorov.clients.application.fanout.AsyncProperties;
import me.nikonorov.clients.application.fanout.FanOutExecutor;
import me.nikonorov.clients.client.application.port.ExternalSystemAClient;
import me.nikonorov.clients.client.application.usecase.ClientAggregationUseCase;
import me.nikonorov.clients.credit.application.port.CreditScoringClient;
import me.nikonorov.clients.credit.application.usecase.CreditDecisionUseCase;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class ArchitectureRulesTest {

    private final JavaClasses classes = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("me.nikonorov.clients");

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
                .isEqualTo("me.nikonorov.clients.client.application.usecase");
        assertThat(classes.get(ExternalSystemAClient.class).getPackageName())
                .isEqualTo("me.nikonorov.clients.client.application.port");
        assertThat(classes.get(FanOutExecutor.class).getPackageName())
                .isEqualTo("me.nikonorov.clients.application.fanout");
        assertThat(classes.get(AsyncProperties.class).getPackageName())
                .isEqualTo("me.nikonorov.clients.application.fanout");
        assertThat(classes.get(CreditDecisionUseCase.class).getPackageName())
                .isEqualTo("me.nikonorov.clients.credit.application.usecase");
        assertThat(classes.get(CreditScoringClient.class).getPackageName())
                .isEqualTo("me.nikonorov.clients.credit.application.port");
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
                        "me.nikonorov.clients.client.api..",
                        "me.nikonorov.clients.client.domain..",
                        "me.nikonorov.clients.client.application..",
                        "me.nikonorov.clients.client.infrastructure..")
                .allowEmptyShould(true)
                .check(classes);
    }

    @Test
    void clientContextDoesNotDependOnCreditLayers() {
        noClasses()
                .that().resideInAPackage("..client..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "me.nikonorov.clients.credit.api..",
                        "me.nikonorov.clients.credit.domain..",
                        "me.nikonorov.clients.credit.application..",
                        "me.nikonorov.clients.credit.infrastructure..")
                .allowEmptyShould(true)
                .check(classes);
    }
}
