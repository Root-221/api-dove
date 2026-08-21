package sn.dove.backend.domain;

import java.util.UUID;

public class CommunauteNanditeTestSamples {

    public static CommunauteNandite getCommunauteNanditeSample1() {
        return new CommunauteNandite().id(UUID.fromString("23d8dc04-a48b-45d9-a01d-4b728f0ad4aa")).nom("nom1");
    }

    public static CommunauteNandite getCommunauteNanditeSample2() {
        return new CommunauteNandite().id(UUID.fromString("ad79f240-3727-46c3-b89f-2cf6ebd74367")).nom("nom2");
    }

    public static CommunauteNandite getCommunauteNanditeRandomSampleGenerator() {
        return new CommunauteNandite().id(UUID.randomUUID()).nom(UUID.randomUUID().toString());
    }
}
