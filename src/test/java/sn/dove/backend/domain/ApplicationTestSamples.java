package sn.dove.backend.domain;

import java.util.UUID;

public class ApplicationTestSamples {

    public static Application getApplicationSample1() {
        return new Application().id(UUID.fromString("23d8dc04-a48b-45d9-a01d-4b728f0ad4aa")).nom("nom1");
    }

    public static Application getApplicationSample2() {
        return new Application().id(UUID.fromString("ad79f240-3727-46c3-b89f-2cf6ebd74367")).nom("nom2");
    }

    public static Application getApplicationRandomSampleGenerator() {
        return new Application().id(UUID.randomUUID()).nom(UUID.randomUUID().toString());
    }
}
