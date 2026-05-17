package me.nikonorov.clients;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ModulithStructureTest {

    @Test
    void verifiesApplicationModuleBoundaries() {
        ApplicationModules.of(ClientArchitectureSkeletonApplication.class).verify();
    }
}
