package sn.dove.backend.dove.web;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sn.dove.backend.dove.service.DuplicateResourceException;

/**
 * Test-only controller used by {@code DoveExceptionMappingIT} to verify that
 * {@code ExceptionTranslator} (the shared, global {@code @ControllerAdvice} that also applies to
 * every sn.dove.backend.dove.web controller) maps duplicate-resource errors to HTTP 409.
 *
 * <p>Deliberately mapped under {@code /api/v1/**}: SecurityConfiguration.filterChain denies every
 * request under {@code /api/**} that isn't {@code /api/v1/**}, so a route outside that prefix
 * (like the legacy {@code sn.dove.backend.web.rest.errors.ExceptionTranslatorTestController}, at
 * {@code /api/exception-translator-test}) can never actually be exercised.
 */
@RestController
@RequestMapping("/api/v1/exception-mapping-test")
public class DoveExceptionMappingTestController {

    @GetMapping("/data-integrity-violation")
    public void dataIntegrityViolation() {
        throw new DataIntegrityViolationException("test duplicate key");
    }

    @GetMapping("/duplicate-resource")
    public void duplicateResource() {
        throw new DuplicateResourceException("test resource already exists");
    }
}
