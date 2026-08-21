package sn.dove.backend.service.mapper;

import static sn.dove.backend.domain.CommunauteNanditeAsserts.*;
import static sn.dove.backend.domain.CommunauteNanditeTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CommunauteNanditeMapperTest {

    private CommunauteNanditeMapper communauteNanditeMapper;

    @BeforeEach
    void setUp() {
        communauteNanditeMapper = new CommunauteNanditeMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getCommunauteNanditeSample1();
        var actual = communauteNanditeMapper.toEntity(communauteNanditeMapper.toDto(expected));
        assertCommunauteNanditeAllPropertiesEquals(expected, actual);
    }
}
