package sn.dove.backend.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static sn.dove.backend.domain.MetierAsserts.*;
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
import sn.dove.backend.domain.Metier;
import sn.dove.backend.repository.MetierRepository;
import sn.dove.backend.service.dto.MetierDTO;
import sn.dove.backend.service.mapper.MetierMapper;

/**
 * Integration tests for the {@link MetierResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class MetierResourceIT {

    private static final String DEFAULT_LIBELLE = "AAAAAAAAAA";
    private static final String UPDATED_LIBELLE = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/metiers";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    private ObjectMapper om;

    @Autowired
    private MetierRepository metierRepository;

    @Autowired
    private MetierMapper metierMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restMetierMockMvc;

    private Metier metier;

    private Metier insertedMetier;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Metier createEntity() {
        return new Metier().libelle(DEFAULT_LIBELLE);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Metier createUpdatedEntity() {
        return new Metier().libelle(UPDATED_LIBELLE);
    }

    @BeforeEach
    void initTest() {
        metier = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedMetier != null) {
            metierRepository.delete(insertedMetier);
            insertedMetier = null;
        }
    }

    @Test
    @Transactional
    void createMetier() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Metier
        MetierDTO metierDTO = metierMapper.toDto(metier);
        var returnedMetierDTO = om.readValue(
            restMetierMockMvc
                .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(metierDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            MetierDTO.class
        );

        // Validate the Metier in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedMetier = metierMapper.toEntity(returnedMetierDTO);
        assertMetierUpdatableFieldsEquals(returnedMetier, getPersistedMetier(returnedMetier));

        insertedMetier = returnedMetier;
    }

    @Test
    @Transactional
    void createMetierWithExistingId() throws Exception {
        // Create the Metier with an existing ID
        insertedMetier = metierRepository.saveAndFlush(metier);
        MetierDTO metierDTO = metierMapper.toDto(metier);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restMetierMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(metierDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Metier in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkLibelleIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        metier.setLibelle(null);

        // Create the Metier, which fails.
        MetierDTO metierDTO = metierMapper.toDto(metier);

        restMetierMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(metierDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllMetiers() throws Exception {
        // Initialize the database
        insertedMetier = metierRepository.saveAndFlush(metier);

        // Get all the metierList
        restMetierMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(metier.getId().toString())))
            .andExpect(jsonPath("$.[*].libelle").value(hasItem(DEFAULT_LIBELLE)));
    }

    @Test
    @Transactional
    void getMetier() throws Exception {
        // Initialize the database
        insertedMetier = metierRepository.saveAndFlush(metier);

        // Get the metier
        restMetierMockMvc
            .perform(get(ENTITY_API_URL_ID, metier.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(metier.getId().toString()))
            .andExpect(jsonPath("$.libelle").value(DEFAULT_LIBELLE));
    }

    @Test
    @Transactional
    void getMetiersByIdFiltering() throws Exception {
        // Initialize the database
        insertedMetier = metierRepository.saveAndFlush(metier);

        UUID id = metier.getId();

        defaultMetierFiltering("id.equals=" + id, "id.notEquals=" + id);
    }

    @Test
    @Transactional
    void getAllMetiersByLibelleIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedMetier = metierRepository.saveAndFlush(metier);

        // Get all the metierList where libelle equals to
        defaultMetierFiltering("libelle.equals=" + DEFAULT_LIBELLE, "libelle.equals=" + UPDATED_LIBELLE);
    }

    @Test
    @Transactional
    void getAllMetiersByLibelleIsInShouldWork() throws Exception {
        // Initialize the database
        insertedMetier = metierRepository.saveAndFlush(metier);

        // Get all the metierList where libelle in
        defaultMetierFiltering("libelle.in=" + DEFAULT_LIBELLE + "," + UPDATED_LIBELLE, "libelle.in=" + UPDATED_LIBELLE);
    }

    @Test
    @Transactional
    void getAllMetiersByLibelleIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedMetier = metierRepository.saveAndFlush(metier);

        // Get all the metierList where libelle is not null
        defaultMetierFiltering("libelle.specified=true", "libelle.specified=false");
    }

    @Test
    @Transactional
    void getAllMetiersByLibelleContainsSomething() throws Exception {
        // Initialize the database
        insertedMetier = metierRepository.saveAndFlush(metier);

        // Get all the metierList where libelle contains
        defaultMetierFiltering("libelle.contains=" + DEFAULT_LIBELLE, "libelle.contains=" + UPDATED_LIBELLE);
    }

    @Test
    @Transactional
    void getAllMetiersByLibelleNotContainsSomething() throws Exception {
        // Initialize the database
        insertedMetier = metierRepository.saveAndFlush(metier);

        // Get all the metierList where libelle does not contain
        defaultMetierFiltering("libelle.doesNotContain=" + UPDATED_LIBELLE, "libelle.doesNotContain=" + DEFAULT_LIBELLE);
    }

    private void defaultMetierFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultMetierShouldBeFound(shouldBeFound);
        defaultMetierShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultMetierShouldBeFound(String filter) throws Exception {
        restMetierMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(metier.getId().toString())))
            .andExpect(jsonPath("$.[*].libelle").value(hasItem(DEFAULT_LIBELLE)));

        // Check, that the count call also returns 1
        restMetierMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultMetierShouldNotBeFound(String filter) throws Exception {
        restMetierMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restMetierMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingMetier() throws Exception {
        // Get the metier
        restMetierMockMvc.perform(get(ENTITY_API_URL_ID, UUID.randomUUID().toString())).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingMetier() throws Exception {
        // Initialize the database
        insertedMetier = metierRepository.saveAndFlush(metier);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the metier
        Metier updatedMetier = metierRepository.findById(metier.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedMetier are not directly saved in db
        em.detach(updatedMetier);
        updatedMetier.libelle(UPDATED_LIBELLE);
        MetierDTO metierDTO = metierMapper.toDto(updatedMetier);

        restMetierMockMvc
            .perform(
                put(ENTITY_API_URL_ID, metierDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(metierDTO))
            )
            .andExpect(status().isOk());

        // Validate the Metier in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedMetierToMatchAllProperties(updatedMetier);
    }

    @Test
    @Transactional
    void putNonExistingMetier() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        metier.setId(UUID.randomUUID());

        // Create the Metier
        MetierDTO metierDTO = metierMapper.toDto(metier);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restMetierMockMvc
            .perform(
                put(ENTITY_API_URL_ID, metierDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(metierDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Metier in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchMetier() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        metier.setId(UUID.randomUUID());

        // Create the Metier
        MetierDTO metierDTO = metierMapper.toDto(metier);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMetierMockMvc
            .perform(
                put(ENTITY_API_URL_ID, UUID.randomUUID())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(metierDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Metier in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamMetier() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        metier.setId(UUID.randomUUID());

        // Create the Metier
        MetierDTO metierDTO = metierMapper.toDto(metier);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMetierMockMvc
            .perform(put(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(metierDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Metier in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateMetierWithPatch() throws Exception {
        // Initialize the database
        insertedMetier = metierRepository.saveAndFlush(metier);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the metier using partial update
        Metier partialUpdatedMetier = new Metier();
        partialUpdatedMetier.setId(metier.getId());

        partialUpdatedMetier.libelle(UPDATED_LIBELLE);

        restMetierMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedMetier.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedMetier))
            )
            .andExpect(status().isOk());

        // Validate the Metier in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertMetierUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedMetier, metier), getPersistedMetier(metier));
    }

    @Test
    @Transactional
    void fullUpdateMetierWithPatch() throws Exception {
        // Initialize the database
        insertedMetier = metierRepository.saveAndFlush(metier);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the metier using partial update
        Metier partialUpdatedMetier = new Metier();
        partialUpdatedMetier.setId(metier.getId());

        partialUpdatedMetier.libelle(UPDATED_LIBELLE);

        restMetierMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedMetier.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedMetier))
            )
            .andExpect(status().isOk());

        // Validate the Metier in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertMetierUpdatableFieldsEquals(partialUpdatedMetier, getPersistedMetier(partialUpdatedMetier));
    }

    @Test
    @Transactional
    void patchNonExistingMetier() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        metier.setId(UUID.randomUUID());

        // Create the Metier
        MetierDTO metierDTO = metierMapper.toDto(metier);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restMetierMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, metierDTO.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(metierDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Metier in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchMetier() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        metier.setId(UUID.randomUUID());

        // Create the Metier
        MetierDTO metierDTO = metierMapper.toDto(metier);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMetierMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, UUID.randomUUID())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(metierDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Metier in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamMetier() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        metier.setId(UUID.randomUUID());

        // Create the Metier
        MetierDTO metierDTO = metierMapper.toDto(metier);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMetierMockMvc
            .perform(
                patch(ENTITY_API_URL).with(csrf()).contentType("application/merge-patch+json").content(om.writeValueAsBytes(metierDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the Metier in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteMetier() throws Exception {
        // Initialize the database
        insertedMetier = metierRepository.saveAndFlush(metier);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the metier
        restMetierMockMvc
            .perform(delete(ENTITY_API_URL_ID, metier.getId().toString()).with(csrf()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return metierRepository.count();
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

    protected Metier getPersistedMetier(Metier metier) {
        return metierRepository.findById(metier.getId()).orElseThrow();
    }

    protected void assertPersistedMetierToMatchAllProperties(Metier expectedMetier) {
        assertMetierAllPropertiesEquals(expectedMetier, getPersistedMetier(expectedMetier));
    }

    protected void assertPersistedMetierToMatchUpdatableProperties(Metier expectedMetier) {
        assertMetierAllUpdatablePropertiesEquals(expectedMetier, getPersistedMetier(expectedMetier));
    }
}
