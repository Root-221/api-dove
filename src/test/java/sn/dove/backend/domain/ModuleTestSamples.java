package sn.dove.backend.domain;

import java.util.UUID;

public class ModuleTestSamples {

    public static Module getModuleSample1() {
        return new Module().id(UUID.fromString("23d8dc04-a48b-45d9-a01d-4b728f0ad4aa")).libelle("libelle1");
    }

    public static Module getModuleSample2() {
        return new Module().id(UUID.fromString("ad79f240-3727-46c3-b89f-2cf6ebd74367")).libelle("libelle2");
    }

    public static Module getModuleRandomSampleGenerator() {
        return new Module().id(UUID.randomUUID()).libelle(UUID.randomUUID().toString());
    }
}
