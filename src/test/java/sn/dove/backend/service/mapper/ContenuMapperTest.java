package sn.dove.backend.service.mapper;

import static sn.dove.backend.domain.ContenuAsserts.*;
import static sn.dove.backend.domain.ContenuTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ContenuMapperTest {

    private ContenuMapper contenuMapper;

    @BeforeEach
    void setUp() {
        contenuMapper = new ContenuMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getContenuSample1();
        var actual = contenuMapper.toEntity(contenuMapper.toDto(expected));
        assertContenuAllPropertiesEquals(expected, actual);
    }
}
