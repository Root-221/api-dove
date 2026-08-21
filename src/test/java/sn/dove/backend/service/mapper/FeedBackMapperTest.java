package sn.dove.backend.service.mapper;

import static sn.dove.backend.domain.FeedBackAsserts.*;
import static sn.dove.backend.domain.FeedBackTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FeedBackMapperTest {

    private FeedBackMapper feedBackMapper;

    @BeforeEach
    void setUp() {
        feedBackMapper = new FeedBackMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getFeedBackSample1();
        var actual = feedBackMapper.toEntity(feedBackMapper.toDto(expected));
        assertFeedBackAllPropertiesEquals(expected, actual);
    }
}
