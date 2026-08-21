package sn.dove.backend.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class FeedBackTestSamples {

    private static final Random random = new Random();
    private static final AtomicInteger intCount = new AtomicInteger(random.nextInt() + 2 * Short.MAX_VALUE);

    public static FeedBack getFeedBackSample1() {
        return new FeedBack().id(UUID.fromString("23d8dc04-a48b-45d9-a01d-4b728f0ad4aa")).note(1).statutTraitement("statutTraitement1");
    }

    public static FeedBack getFeedBackSample2() {
        return new FeedBack().id(UUID.fromString("ad79f240-3727-46c3-b89f-2cf6ebd74367")).note(2).statutTraitement("statutTraitement2");
    }

    public static FeedBack getFeedBackRandomSampleGenerator() {
        return new FeedBack().id(UUID.randomUUID()).note(intCount.incrementAndGet()).statutTraitement(UUID.randomUUID().toString());
    }
}
