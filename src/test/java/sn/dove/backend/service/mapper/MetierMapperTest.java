package sn.dove.backend.service.mapper;

import static sn.dove.backend.domain.MetierAsserts.*;
import static sn.dove.backend.domain.MetierTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MetierMapperTest {

    private MetierMapper metierMapper;

    @BeforeEach
    void setUp() {
        metierMapper = new MetierMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getMetierSample1();
        var actual = metierMapper.toEntity(metierMapper.toDto(expected));
        assertMetierAllPropertiesEquals(expected, actual);
    }
}
