package sn.dove.backend.domain;

import java.util.UUID;

public class SignalementMessageTestSamples {

    public static SignalementMessage getSignalementMessageSample1() {
        return new SignalementMessage().id(UUID.fromString("23d8dc04-a48b-45d9-a01d-4b728f0ad4aa"));
    }

    public static SignalementMessage getSignalementMessageSample2() {
        return new SignalementMessage().id(UUID.fromString("ad79f240-3727-46c3-b89f-2cf6ebd74367"));
    }

    public static SignalementMessage getSignalementMessageRandomSampleGenerator() {
        return new SignalementMessage().id(UUID.randomUUID());
    }
}
