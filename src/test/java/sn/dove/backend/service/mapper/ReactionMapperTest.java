package sn.dove.backend.service.mapper;

import static sn.dove.backend.domain.ReactionAsserts.*;
import static sn.dove.backend.domain.ReactionTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ReactionMapperTest {

    private ReactionMapper reactionMapper;

    @BeforeEach
    void setUp() {
        reactionMapper = new ReactionMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getReactionSample1();
        var actual = reactionMapper.toEntity(reactionMapper.toDto(expected));
        assertReactionAllPropertiesEquals(expected, actual);
    }
}
