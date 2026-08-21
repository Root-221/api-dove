package sn.dove.backend.service.mapper;

import static sn.dove.backend.domain.ModuleAsserts.*;
import static sn.dove.backend.domain.ModuleTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ModuleMapperTest {

    private ModuleMapper moduleMapper;

    @BeforeEach
    void setUp() {
        moduleMapper = new ModuleMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getModuleSample1();
        var actual = moduleMapper.toEntity(moduleMapper.toDto(expected));
        assertModuleAllPropertiesEquals(expected, actual);
    }
}
