package sn.dove.backend.domain;

import java.util.UUID;

public class ReactionTestSamples {

    public static Reaction getReactionSample1() {
        return new Reaction().id(UUID.fromString("23d8dc04-a48b-45d9-a01d-4b728f0ad4aa")).type("type1");
    }

    public static Reaction getReactionSample2() {
        return new Reaction().id(UUID.fromString("ad79f240-3727-46c3-b89f-2cf6ebd74367")).type("type2");
    }

    public static Reaction getReactionRandomSampleGenerator() {
        return new Reaction().id(UUID.randomUUID()).type(UUID.randomUUID().toString());
    }
}
