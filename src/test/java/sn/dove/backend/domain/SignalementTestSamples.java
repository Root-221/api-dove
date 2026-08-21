package sn.dove.backend.domain;

import java.util.UUID;

public class SignalementTestSamples {

    public static Signalement getSignalementSample1() {
        return new Signalement().id(UUID.fromString("23d8dc04-a48b-45d9-a01d-4b728f0ad4aa"));
    }

    public static Signalement getSignalementSample2() {
        return new Signalement().id(UUID.fromString("ad79f240-3727-46c3-b89f-2cf6ebd74367"));
    }

    public static Signalement getSignalementRandomSampleGenerator() {
        return new Signalement().id(UUID.randomUUID());
    }
}
