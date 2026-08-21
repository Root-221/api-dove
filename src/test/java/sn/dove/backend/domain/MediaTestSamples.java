package sn.dove.backend.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class MediaTestSamples {

    private static final Random random = new Random();
    private static final AtomicInteger intCount = new AtomicInteger(random.nextInt() + 2 * Short.MAX_VALUE);

    public static Media getMediaSample1() {
        return new Media().id(UUID.fromString("23d8dc04-a48b-45d9-a01d-4b728f0ad4aa")).titre("titre1").etape(1).url("url1");
    }

    public static Media getMediaSample2() {
        return new Media().id(UUID.fromString("ad79f240-3727-46c3-b89f-2cf6ebd74367")).titre("titre2").etape(2).url("url2");
    }

    public static Media getMediaRandomSampleGenerator() {
        return new Media()
            .id(UUID.randomUUID())
            .titre(UUID.randomUUID().toString())
            .etape(intCount.incrementAndGet())
            .url(UUID.randomUUID().toString());
    }
}
