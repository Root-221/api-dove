package sn.dove.backend.domain;

import java.util.UUID;

public class FAQTestSamples {

    public static FAQ getFAQSample1() {
        return new FAQ().id(UUID.fromString("23d8dc04-a48b-45d9-a01d-4b728f0ad4aa")).titre("titre1");
    }

    public static FAQ getFAQSample2() {
        return new FAQ().id(UUID.fromString("ad79f240-3727-46c3-b89f-2cf6ebd74367")).titre("titre2");
    }

    public static FAQ getFAQRandomSampleGenerator() {
        return new FAQ().id(UUID.randomUUID()).titre(UUID.randomUUID().toString());
    }
}
