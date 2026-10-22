package com.torquegestao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.torquegestao.dto.VehicleRequest;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class VehicleRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    private VehicleRequest with(String plate) {
        return new VehicleRequest(plate, "Fiat", "Uno", 2015, 1000, "Flex", UUID.randomUUID());
    }

    @Test
    void normalizaPlacaParaMaiusculasSemHifen() {
        assertEquals("ABC1D23", with(" abc-1d23 ").plate());
        assertEquals("ABC1234", with("abc-1234").plate());
        assertNull(with(null).plate());
    }

    @Test
    void aceitaPlacaAntigaEMercosul() {
        assertTrue(validator.validate(with("ABC1234")).isEmpty());
        assertTrue(validator.validate(with("abc1d23")).isEmpty());
    }

    @Test
    void recusaPlacaInvalida() {
        assertFalse(validator.validate(with("AB12345")).isEmpty());
        assertFalse(validator.validate(with("ABCDEFG")).isEmpty());
        assertFalse(validator.validate(with("")).isEmpty());
    }
}
