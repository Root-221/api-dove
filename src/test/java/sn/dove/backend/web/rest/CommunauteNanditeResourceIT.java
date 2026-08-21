package sn.dove.backend.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static sn.dove.backend.domain.CommunauteNanditeAsserts.*;
import static sn.dove.backend.web.rest.TestUtil.createUpdateProxyForBean;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import sn.dove.backend.IntegrationTest;
import sn.dove.backend.domain.CommunauteNandite;
import sn.dove.backend.domain.Utilisateur;
import sn.dove.backend.repository.CommunauteNanditeRepository;
import sn.dove.backend.service.CommunauteNanditeService;
import sn.dove.backend.service.dto.CommunauteNanditeDTO;
import sn.dove.backend.service.mapper.CommunauteNanditeMapper;

/**
 * Integration tests for the {@link CommunauteNanditeResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class CommunauteNanditeResourceIT {

    private static final String DEFAULT_NOM = "AAAAAAAAAA";
    private static final String UPDATED_NOM = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/communaute-nandites";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    private ObjectMapper om;

    @Autowired
    private CommunauteNanditeRepository communauteNanditeRepository;

    @Mock
    private CommunauteNanditeRepository communauteNanditeRepositoryMock;

    @Autowired
    private CommunauteNanditeMapper communauteNanditeMapper;

    @Mock
    private CommunauteNanditeService communauteNanditeServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restCommunauteNanditeMockMvc;

    private CommunauteNandite communauteNandite;

    private CommunauteNandite insertedCommunauteNandite;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static CommunauteNandite createEntity() {
        return new CommunauteNandite().nom(DEFAULT_NOM).description(DEFAULT_DESCRIPTION);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static CommunauteNandite createUpdatedEntity() {
        return new CommunauteNandite().nom(UPDATED_NOM).description(UPDATED_DESCRIPTION);
    }

    @BeforeEach
    void initTest() {
        communauteNandite = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedCommunauteNandite != null) {
            communauteNanditeRepository.delete(insertedCommunauteNandite);
            insertedCommunauteNandite = null;
        }
    }

    @Test
    @Transactional
    void createCommunauteNandite() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the CommunauteNandite
        CommunauteNanditeDTO communauteNanditeDTO = communauteNanditeMapper.toDto(communauteNandite);
        var returnedCommunauteNanditeDTO = om.readValue(
            restCommunauteNanditeMockMvc
                .perform(
                    post(ENTITY_API_URL)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(communauteNanditeDTO))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            CommunauteNanditeDTO.class
        );

        // Validate the CommunauteNandite in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedCommunauteNandite = communauteNanditeMapper.toEntity(returnedCommunauteNanditeDTO);
        assertCommunauteNanditeUpdatableFieldsEquals(returnedCommunauteNandite, getPersistedCommunauteNandite(returnedCommunauteNandite));

        insertedCommunauteNandite = returnedCommunauteNandite;
    }

    @Test
    @Transactional
    void createCommunauteNanditeWithExistingId() throws Exception {
        // Create the CommunauteNandite with an existing ID
        insertedCommunauteNandite = communauteNanditeRepository.saveAndFlush(communauteNandite);
        CommunauteNanditeDTO communauteNanditeDTO = communauteNanditeMapper.toDto(communauteNandite);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restCommunauteNanditeMockMvc
            .perform(
                post(ENTITY_API_URL)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(communauteNanditeDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the CommunauteNandite in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkNomIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        communauteNandite.setNom(null);

        // Create the CommunauteNandite, which fails.
        CommunauteNanditeDTO communauteNanditeDTO = communauteNanditeMapper.toDto(communauteNandite);

        restCommunauteNanditeMockMvc
            .perform(
                post(ENTITY_API_URL)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(communauteNanditeDTO))
            )
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllCommunauteNandites() throws Exception {
        // Initialize the database
        insertedCommunauteNandite = communauteNanditeRepository.saveAndFlush(communauteNandite);

        // Get all the communauteNanditeList
        restCommunauteNanditeMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(communauteNandite.getId().toString())))
            .andExpect(jsonPath("$.[*].nom").value(hasItem(DEFAULT_NOM)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllCommunauteNanditesWithEagerRelationshipsIsEnabled() throws Exception {
        when(communauteNanditeServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restCommunauteNanditeMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(communauteNanditeServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllCommunauteNanditesWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(communauteNanditeServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restCommunauteNanditeMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(communauteNanditeRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getCommunauteNandite() throws Exception {
        // Initialize the database
        insertedCommunauteNandite = communauteNanditeRepository.saveAndFlush(communauteNandite);

        // Get the communauteNandite
        restCommunauteNanditeMockMvc
            .perform(get(ENTITY_API_URL_ID, communauteNandite.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(communauteNandite.getId().toString()))
            .andExpect(jsonPath("$.nom").value(DEFAULT_NOM))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION));
    }

    @Test
    @Transactional
    void getCommunauteNanditesByIdFiltering() throws Exception {
        // Initialize the database
        insertedCommunauteNandite = communauteNanditeRepository.saveAndFlush(communauteNandite);

        UUID id = communauteNandite.getId();

        defaultCommunauteNanditeFiltering("id.equals=" + id, "id.notEquals=" + id);
    }

    @Test
    @Transactional
    void getAllCommunauteNanditesByNomIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedCommunauteNandite = communauteNanditeRepository.saveAndFlush(communauteNandite);

        // Get all the communauteNanditeList where nom equals to
        defaultCommunauteNanditeFiltering("nom.equals=" + DEFAULT_NOM, "nom.equals=" + UPDATED_NOM);
    }

    @Test
    @Transactional
    void getAllCommunauteNanditesByNomIsInShouldWork() throws Exception {
        // Initialize the database
        insertedCommunauteNandite = communauteNanditeRepository.saveAndFlush(communauteNandite);

        // Get all the communauteNanditeList where nom in
        defaultCommunauteNanditeFiltering("nom.in=" + DEFAULT_NOM + "," + UPDATED_NOM, "nom.in=" + UPDATED_NOM);
    }

    @Test
    @Transactional
    void getAllCommunauteNanditesByNomIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedCommunauteNandite = communauteNanditeRepository.saveAndFlush(communauteNandite);

        // Get all the communauteNanditeList where nom is not null
        defaultCommunauteNanditeFiltering("nom.specified=true", "nom.specified=false");
    }

    @Test
    @Transactional
    void getAllCommunauteNanditesByNomContainsSomething() throws Exception {
        // Initialize the database
        insertedCommunauteNandite = communauteNanditeRepository.saveAndFlush(communauteNandite);

        // Get all the communauteNanditeList where nom contains
        defaultCommunauteNanditeFiltering("nom.contains=" + DEFAULT_NOM, "nom.contains=" + UPDATED_NOM);
    }

    @Test
    @Transactional
    void getAllCommunauteNanditesByNomNotContainsSomething() throws Exception {
        // Initialize the database
        insertedCommunauteNandite = communauteNanditeRepository.saveAndFlush(communauteNandite);

        // Get all the communauteNanditeList where nom does not contain
        defaultCommunauteNanditeFiltering("nom.doesNotContain=" + UPDATED_NOM, "nom.doesNotContain=" + DEFAULT_NOM);
    }

    @Test
    @Transactional
    void getAllCommunauteNanditesByMembresIsEqualToSomething() throws Exception {
        Utilisateur membres;
        if (TestUtil.findAll(em, Utilisateur.class).isEmpty()) {
            communauteNanditeRepository.saveAndFlush(communauteNandite);
            membres = UtilisateurResourceIT.createEntity();
        } else {
            membres = TestUtil.findAll(em, Utilisateur.class).get(0);
        }
        em.persist(membres);
        em.flush();
        communauteNandite.addMembres(membres);
        communauteNanditeRepository.saveAndFlush(communauteNandite);
        UUID membresId = membres.getId();
        // Get all the communauteNanditeList where membres equals to membresId
        defaultCommunauteNanditeShouldBeFound("membresId.equals=" + membresId);

        // Get all the communauteNanditeList where membres equals to UUID.randomUUID()
        defaultCommunauteNanditeShouldNotBeFound("membresId.equals=" + UUID.randomUUID());
    }

    private void defaultCommunauteNanditeFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultCommunauteNanditeShouldBeFound(shouldBeFound);
        defaultCommunauteNanditeShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultCommunauteNanditeShouldBeFound(String filter) throws Exception {
        restCommunauteNanditeMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(communauteNandite.getId().toString())))
            .andExpect(jsonPath("$.[*].nom").value(hasItem(DEFAULT_NOM)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)));

        // Check, that the count call also returns 1
        restCommunauteNanditeMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultCommunauteNanditeShouldNotBeFound(String filter) throws Exception {
        restCommunauteNanditeMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restCommunauteNanditeMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingCommunauteNandite() throws Exception {
        // Get the communauteNandite
        restCommunauteNanditeMockMvc.perform(get(ENTITY_API_URL_ID, UUID.randomUUID().toString())).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingCommunauteNandite() throws Exception {
        // Initialize the database
        insertedCommunauteNandite = communauteNanditeRepository.saveAndFlush(communauteNandite);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the communauteNandite
        CommunauteNandite updatedCommunauteNandite = communauteNanditeRepository.findById(communauteNandite.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedCommunauteNandite are not directly saved in db
        em.detach(updatedCommunauteNandite);
        updatedCommunauteNandite.nom(UPDATED_NOM).description(UPDATED_DESCRIPTION);
        CommunauteNanditeDTO communauteNanditeDTO = communauteNanditeMapper.toDto(updatedCommunauteNandite);

        restCommunauteNanditeMockMvc
            .perform(
                put(ENTITY_API_URL_ID, communauteNanditeDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(communauteNanditeDTO))
            )
            .andExpect(status().isOk());

        // Validate the CommunauteNandite in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedCommunauteNanditeToMatchAllProperties(updatedCommunauteNandite);
    }

    @Test
    @Transactional
    void putNonExistingCommunauteNandite() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        communauteNandite.setId(UUID.randomUUID());

        // Create the CommunauteNandite
        CommunauteNanditeDTO communauteNanditeDTO = communauteNanditeMapper.toDto(communauteNandite);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCommunauteNanditeMockMvc
            .perform(
                put(ENTITY_API_URL_ID, communauteNanditeDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(communauteNanditeDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the CommunauteNandite in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchCommunauteNandite() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        communauteNandite.setId(UUID.randomUUID());

        // Create the CommunauteNandite
        CommunauteNanditeDTO communauteNanditeDTO = communauteNanditeMapper.toDto(communauteNandite);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCommunauteNanditeMockMvc
            .perform(
                put(ENTITY_API_URL_ID, UUID.randomUUID())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(communauteNanditeDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the CommunauteNandite in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamCommunauteNandite() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        communauteNandite.setId(UUID.randomUUID());

        // Create the CommunauteNandite
        CommunauteNanditeDTO communauteNanditeDTO = communauteNanditeMapper.toDto(communauteNandite);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCommunauteNanditeMockMvc
            .perform(
                put(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(communauteNanditeDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the CommunauteNandite in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateCommunauteNanditeWithPatch() throws Exception {
        // Initialize the database
        insertedCommunauteNandite = communauteNanditeRepository.saveAndFlush(communauteNandite);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the communauteNandite using partial update
        CommunauteNandite partialUpdatedCommunauteNandite = new CommunauteNandite();
        partialUpdatedCommunauteNandite.setId(communauteNandite.getId());

        restCommunauteNanditeMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedCommunauteNandite.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedCommunauteNandite))
            )
            .andExpect(status().isOk());

        // Validate the CommunauteNandite in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertCommunauteNanditeUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedCommunauteNandite, communauteNandite),
            getPersistedCommunauteNandite(communauteNandite)
        );
    }

    @Test
    @Transactional
    void fullUpdateCommunauteNanditeWithPatch() throws Exception {
        // Initialize the database
        insertedCommunauteNandite = communauteNanditeRepository.saveAndFlush(communauteNandite);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the communauteNandite using partial update
        CommunauteNandite partialUpdatedCommunauteNandite = new CommunauteNandite();
        partialUpdatedCommunauteNandite.setId(communauteNandite.getId());

        partialUpdatedCommunauteNandite.nom(UPDATED_NOM).description(UPDATED_DESCRIPTION);

        restCommunauteNanditeMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedCommunauteNandite.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedCommunauteNandite))
            )
            .andExpect(status().isOk());

        // Validate the CommunauteNandite in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertCommunauteNanditeUpdatableFieldsEquals(
            partialUpdatedCommunauteNandite,
            getPersistedCommunauteNandite(partialUpdatedCommunauteNandite)
        );
    }

    @Test
    @Transactional
    void patchNonExistingCommunauteNandite() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        communauteNandite.setId(UUID.randomUUID());

        // Create the CommunauteNandite
        CommunauteNanditeDTO communauteNanditeDTO = communauteNanditeMapper.toDto(communauteNandite);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCommunauteNanditeMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, communauteNanditeDTO.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(communauteNanditeDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the CommunauteNandite in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchCommunauteNandite() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        communauteNandite.setId(UUID.randomUUID());

        // Create the CommunauteNandite
        CommunauteNanditeDTO communauteNanditeDTO = communauteNanditeMapper.toDto(communauteNandite);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCommunauteNanditeMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, UUID.randomUUID())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(communauteNanditeDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the CommunauteNandite in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamCommunauteNandite() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        communauteNandite.setId(UUID.randomUUID());

        // Create the CommunauteNandite
        CommunauteNanditeDTO communauteNanditeDTO = communauteNanditeMapper.toDto(communauteNandite);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCommunauteNanditeMockMvc
            .perform(
                patch(ENTITY_API_URL)
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(communauteNanditeDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the CommunauteNandite in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteCommunauteNandite() throws Exception {
        // Initialize the database
        insertedCommunauteNandite = communauteNanditeRepository.saveAndFlush(communauteNandite);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the communauteNandite
        restCommunauteNanditeMockMvc
            .perform(delete(ENTITY_API_URL_ID, communauteNandite.getId().toString()).with(csrf()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return communauteNanditeRepository.count();
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

    protected CommunauteNandite getPersistedCommunauteNandite(CommunauteNandite communauteNandite) {
        return communauteNanditeRepository.findById(communauteNandite.getId()).orElseThrow();
    }

    protected void assertPersistedCommunauteNanditeToMatchAllProperties(CommunauteNandite expectedCommunauteNandite) {
        assertCommunauteNanditeAllPropertiesEquals(expectedCommunauteNandite, getPersistedCommunauteNandite(expectedCommunauteNandite));
    }

    protected void assertPersistedCommunauteNanditeToMatchUpdatableProperties(CommunauteNandite expectedCommunauteNandite) {
        assertCommunauteNanditeAllUpdatablePropertiesEquals(
            expectedCommunauteNandite,
            getPersistedCommunauteNandite(expectedCommunauteNandite)
        );
    }
}
