package sn.dove.backend.dove.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import sn.dove.backend.IntegrationTest;

/**
 * Verifies the item-8 production-readiness fix: a unique-constraint race hitting
 * DoveResourceStore.create() (DataIntegrityViolationException) and its explicit
 * check-then-insert duplicate (DuplicateResourceException) are both mapped by ExceptionTranslator
 * to HTTP 409 Conflict, instead of the generic 500 they produced previously.
 */
@WithMockUser
@AutoConfigureMockMvc
@IntegrationTest
class DoveExceptionMappingIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void dataIntegrityViolationMapsTo409() throws Exception {
        mockMvc.perform(get("/api/v1/exception-mapping-test/data-integrity-violation").with(csrf())).andExpect(status().isConflict());
    }

    @Test
    void duplicateResourceMapsTo409() throws Exception {
        mockMvc.perform(get("/api/v1/exception-mapping-test/duplicate-resource").with(csrf())).andExpect(status().isConflict());
    }
}
