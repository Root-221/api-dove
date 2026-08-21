package sn.dove.backend.service.mapper;

import static sn.dove.backend.domain.SignalementMessageAsserts.*;
import static sn.dove.backend.domain.SignalementMessageTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SignalementMessageMapperTest {

    private SignalementMessageMapper signalementMessageMapper;

    @BeforeEach
    void setUp() {
        signalementMessageMapper = new SignalementMessageMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getSignalementMessageSample1();
        var actual = signalementMessageMapper.toEntity(signalementMessageMapper.toDto(expected));
        assertSignalementMessageAllPropertiesEquals(expected, actual);
    }
}
