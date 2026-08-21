package sn.dove.backend.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class ContenuTestSamples {

    private static final Random random = new Random();
    private static final AtomicInteger intCount = new AtomicInteger(random.nextInt() + 2 * Short.MAX_VALUE);

    public static Contenu getContenuSample1() {
        return new Contenu().id(UUID.fromString("23d8dc04-a48b-45d9-a01d-4b728f0ad4aa")).titre("titre1").version(1).progression(1);
    }

    public static Contenu getContenuSample2() {
        return new Contenu().id(UUID.fromString("ad79f240-3727-46c3-b89f-2cf6ebd74367")).titre("titre2").version(2).progression(2);
    }

    public static Contenu getContenuRandomSampleGenerator() {
        return new Contenu()
            .id(UUID.randomUUID())
            .titre(UUID.randomUUID().toString())
            .version(intCount.incrementAndGet())
            .progression(intCount.incrementAndGet());
    }
}
