package sn.dove.backend.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static sn.dove.backend.domain.ModuleAsserts.*;
import static sn.dove.backend.web.rest.TestUtil.createUpdateProxyForBean;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import sn.dove.backend.IntegrationTest;
import sn.dove.backend.domain.Application;
import sn.dove.backend.domain.Module;
import sn.dove.backend.repository.ModuleRepository;
import sn.dove.backend.service.dto.ModuleDTO;
import sn.dove.backend.service.mapper.ModuleMapper;

/**
 * Integration tests for the {@link ModuleResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class ModuleResourceIT {

    private static final String DEFAULT_LIBELLE = "AAAAAAAAAA";
    private static final String UPDATED_LIBELLE = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/modules";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    private ObjectMapper om;

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private ModuleMapper moduleMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restModuleMockMvc;

    private Module module;

    private Module insertedModule;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Module createEntity(EntityManager em) {
        Module module = new Module().libelle(DEFAULT_LIBELLE);
        // Add required entity
        Application application;
        if (TestUtil.findAll(em, Application.class).isEmpty()) {
            application = ApplicationResourceIT.createEntity();
            em.persist(application);
            em.flush();
        } else {
            application = TestUtil.findAll(em, Application.class).get(0);
        }
        module.setApplication(application);
        return module;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Module createUpdatedEntity(EntityManager em) {
        Module updatedModule = new Module().libelle(UPDATED_LIBELLE);
        // Add required entity
        Application application;
        if (TestUtil.findAll(em, Application.class).isEmpty()) {
            application = ApplicationResourceIT.createUpdatedEntity();
            em.persist(application);
            em.flush();
        } else {
            application = TestUtil.findAll(em, Application.class).get(0);
        }
        updatedModule.setApplication(application);
        return updatedModule;
    }

    @BeforeEach
    void initTest() {
        module = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedModule != null) {
            moduleRepository.delete(insertedModule);
            insertedModule = null;
        }
    }

    @Test
    @Transactional
    void createModule() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Module
        ModuleDTO moduleDTO = moduleMapper.toDto(module);
        var returnedModuleDTO = om.readValue(
            restModuleMockMvc
                .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(moduleDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            ModuleDTO.class
        );

        // Validate the Module in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedModule = moduleMapper.toEntity(returnedModuleDTO);
        assertModuleUpdatableFieldsEquals(returnedModule, getPersistedModule(returnedModule));

        insertedModule = returnedModule;
    }

    @Test
    @Transactional
    void createModuleWithExistingId() throws Exception {
        // Create the Module with an existing ID
        insertedModule = moduleRepository.saveAndFlush(module);
        ModuleDTO moduleDTO = moduleMapper.toDto(module);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restModuleMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(moduleDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Module in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkLibelleIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        module.setLibelle(null);

        // Create the Module, which fails.
        ModuleDTO moduleDTO = moduleMapper.toDto(module);

        restModuleMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(moduleDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllModules() throws Exception {
        // Initialize the database
        insertedModule = moduleRepository.saveAndFlush(module);

        // Get all the moduleList
        restModuleMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(module.getId().toString())))
            .andExpect(jsonPath("$.[*].libelle").value(hasItem(DEFAULT_LIBELLE)));
    }

    @Test
    @Transactional
    void getModule() throws Exception {
        // Initialize the database
        insertedModule = moduleRepository.saveAndFlush(module);

        // Get the module
        restModuleMockMvc
            .perform(get(ENTITY_API_URL_ID, module.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(module.getId().toString()))
            .andExpect(jsonPath("$.libelle").value(DEFAULT_LIBELLE));
    }

    @Test
    @Transactional
    void getModulesByIdFiltering() throws Exception {
        // Initialize the database
        insertedModule = moduleRepository.saveAndFlush(module);

        UUID id = module.getId();

        defaultModuleFiltering("id.equals=" + id, "id.notEquals=" + id);
    }

    @Test
    @Transactional
    void getAllModulesByLibelleIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedModule = moduleRepository.saveAndFlush(module);

        // Get all the moduleList where libelle equals to
        defaultModuleFiltering("libelle.equals=" + DEFAULT_LIBELLE, "libelle.equals=" + UPDATED_LIBELLE);
    }

    @Test
    @Transactional
    void getAllModulesByLibelleIsInShouldWork() throws Exception {
        // Initialize the database
        insertedModule = moduleRepository.saveAndFlush(module);

        // Get all the moduleList where libelle in
        defaultModuleFiltering("libelle.in=" + DEFAULT_LIBELLE + "," + UPDATED_LIBELLE, "libelle.in=" + UPDATED_LIBELLE);
    }

    @Test
    @Transactional
    void getAllModulesByLibelleIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedModule = moduleRepository.saveAndFlush(module);

        // Get all the moduleList where libelle is not null
        defaultModuleFiltering("libelle.specified=true", "libelle.specified=false");
    }

    @Test
    @Transactional
    void getAllModulesByLibelleContainsSomething() throws Exception {
        // Initialize the database
        insertedModule = moduleRepository.saveAndFlush(module);

        // Get all the moduleList where libelle contains
        defaultModuleFiltering("libelle.contains=" + DEFAULT_LIBELLE, "libelle.contains=" + UPDATED_LIBELLE);
    }

    @Test
    @Transactional
    void getAllModulesByLibelleNotContainsSomething() throws Exception {
        // Initialize the database
        insertedModule = moduleRepository.saveAndFlush(module);

        // Get all the moduleList where libelle does not contain
        defaultModuleFiltering("libelle.doesNotContain=" + UPDATED_LIBELLE, "libelle.doesNotContain=" + DEFAULT_LIBELLE);
    }

    @Test
    @Transactional
    void getAllModulesByApplicationIsEqualToSomething() throws Exception {
        Application application;
        if (TestUtil.findAll(em, Application.class).isEmpty()) {
            moduleRepository.saveAndFlush(module);
            application = ApplicationResourceIT.createEntity();
        } else {
            application = TestUtil.findAll(em, Application.class).get(0);
        }
        em.persist(application);
        em.flush();
        module.setApplication(application);
        moduleRepository.saveAndFlush(module);
        UUID applicationId = application.getId();
        // Get all the moduleList where application equals to applicationId
        defaultModuleShouldBeFound("applicationId.equals=" + applicationId);

        // Get all the moduleList where application equals to UUID.randomUUID()
        defaultModuleShouldNotBeFound("applicationId.equals=" + UUID.randomUUID());
    }

    private void defaultModuleFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultModuleShouldBeFound(shouldBeFound);
        defaultModuleShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultModuleShouldBeFound(String filter) throws Exception {
        restModuleMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(module.getId().toString())))
            .andExpect(jsonPath("$.[*].libelle").value(hasItem(DEFAULT_LIBELLE)));

        // Check, that the count call also returns 1
        restModuleMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultModuleShouldNotBeFound(String filter) throws Exception {
        restModuleMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restModuleMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingModule() throws Exception {
        // Get the module
        restModuleMockMvc.perform(get(ENTITY_API_URL_ID, UUID.randomUUID().toString())).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingModule() throws Exception {
        // Initialize the database
        insertedModule = moduleRepository.saveAndFlush(module);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the module
        Module updatedModule = moduleRepository.findById(module.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedModule are not directly saved in db
        em.detach(updatedModule);
        updatedModule.libelle(UPDATED_LIBELLE);
        ModuleDTO moduleDTO = moduleMapper.toDto(updatedModule);

        restModuleMockMvc
            .perform(
                put(ENTITY_API_URL_ID, moduleDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(moduleDTO))
            )
            .andExpect(status().isOk());

        // Validate the Module in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedModuleToMatchAllProperties(updatedModule);
    }

    @Test
    @Transactional
    void putNonExistingModule() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        module.setId(UUID.randomUUID());

        // Create the Module
        ModuleDTO moduleDTO = moduleMapper.toDto(module);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restModuleMockMvc
            .perform(
                put(ENTITY_API_URL_ID, moduleDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(moduleDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Module in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchModule() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        module.setId(UUID.randomUUID());

        // Create the Module
        ModuleDTO moduleDTO = moduleMapper.toDto(module);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restModuleMockMvc
            .perform(
                put(ENTITY_API_URL_ID, UUID.randomUUID())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(moduleDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Module in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamModule() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        module.setId(UUID.randomUUID());

        // Create the Module
        ModuleDTO moduleDTO = moduleMapper.toDto(module);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restModuleMockMvc
            .perform(put(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(moduleDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Module in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateModuleWithPatch() throws Exception {
        // Initialize the database
        insertedModule = moduleRepository.saveAndFlush(module);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the module using partial update
        Module partialUpdatedModule = new Module();
        partialUpdatedModule.setId(module.getId());

        partialUpdatedModule.libelle(UPDATED_LIBELLE);

        restModuleMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedModule.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedModule))
            )
            .andExpect(status().isOk());

        // Validate the Module in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertModuleUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedModule, module), getPersistedModule(module));
    }

    @Test
    @Transactional
    void fullUpdateModuleWithPatch() throws Exception {
        // Initialize the database
        insertedModule = moduleRepository.saveAndFlush(module);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the module using partial update
        Module partialUpdatedModule = new Module();
        partialUpdatedModule.setId(module.getId());

        partialUpdatedModule.libelle(UPDATED_LIBELLE);

        restModuleMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedModule.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedModule))
            )
            .andExpect(status().isOk());

        // Validate the Module in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertModuleUpdatableFieldsEquals(partialUpdatedModule, getPersistedModule(partialUpdatedModule));
    }

    @Test
    @Transactional
    void patchNonExistingModule() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        module.setId(UUID.randomUUID());

        // Create the Module
        ModuleDTO moduleDTO = moduleMapper.toDto(module);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restModuleMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, moduleDTO.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(moduleDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Module in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchModule() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        module.setId(UUID.randomUUID());

        // Create the Module
        ModuleDTO moduleDTO = moduleMapper.toDto(module);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restModuleMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, UUID.randomUUID())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(moduleDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Module in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamModule() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        module.setId(UUID.randomUUID());

        // Create the Module
        ModuleDTO moduleDTO = moduleMapper.toDto(module);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restModuleMockMvc
            .perform(
                patch(ENTITY_API_URL).with(csrf()).contentType("application/merge-patch+json").content(om.writeValueAsBytes(moduleDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the Module in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteModule() throws Exception {
        // Initialize the database
        insertedModule = moduleRepository.saveAndFlush(module);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the module
        restModuleMockMvc
            .perform(delete(ENTITY_API_URL_ID, module.getId().toString()).with(csrf()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return moduleRepository.count();
    }

    protected void assertIncrementedRepositoryCount(long countBefore) {
        assertThat(countBefore + 1).isEqualTo(getRepositoryCount());
    }

    protected void assertDecrementedRepositoryCount(long countBefore) {
        assertThat(countBefore - 1).isEqualTo(getRepositoryCount());
    }

    protected void assertSameRepositoryCount(long countBefore) {
        assertThat(countBefore).isEqualTo(getRepositoryCount());
    }

    protected Module getPersistedModule(Module module) {
        return moduleRepository.findById(module.getId()).orElseThrow();
    }

    protected void assertPersistedModuleToMatchAllProperties(Module expectedModule) {
        assertModuleAllPropertiesEquals(expectedModule, getPersistedModule(expectedModule));
    }

    protected void assertPersistedModuleToMatchUpdatableProperties(Module expectedModule) {
        assertModuleAllUpdatablePropertiesEquals(expectedModule, getPersistedModule(expectedModule));
    }
}
