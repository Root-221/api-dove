package sn.dove.backend.service.mapper;

import static sn.dove.backend.domain.FAQAsserts.*;
import static sn.dove.backend.domain.FAQTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FAQMapperTest {

    private FAQMapper fAQMapper;

    @BeforeEach
    void setUp() {
        fAQMapper = new FAQMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getFAQSample1();
        var actual = fAQMapper.toEntity(fAQMapper.toDto(expected));
        assertFAQAllPropertiesEquals(expected, actual);
    }
}
