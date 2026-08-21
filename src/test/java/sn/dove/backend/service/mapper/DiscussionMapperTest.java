package sn.dove.backend.service.mapper;

import static sn.dove.backend.domain.DiscussionAsserts.*;
import static sn.dove.backend.domain.DiscussionTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DiscussionMapperTest {

    private DiscussionMapper discussionMapper;

    @BeforeEach
    void setUp() {
        discussionMapper = new DiscussionMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getDiscussionSample1();
        var actual = discussionMapper.toEntity(discussionMapper.toDto(expected));
        assertDiscussionAllPropertiesEquals(expected, actual);
    }
}
