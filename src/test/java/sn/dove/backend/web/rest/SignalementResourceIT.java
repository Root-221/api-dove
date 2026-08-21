package sn.dove.backend.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static sn.dove.backend.domain.SignalementAsserts.*;
import static sn.dove.backend.web.rest.TestUtil.createUpdateProxyForBean;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.time.Instant;
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
import sn.dove.backend.domain.Contenu;
import sn.dove.backend.domain.Signalement;
import sn.dove.backend.domain.Utilisateur;
import sn.dove.backend.domain.enumeration.StatutSignalement;
import sn.dove.backend.repository.SignalementRepository;
import sn.dove.backend.service.SignalementService;
import sn.dove.backend.service.dto.SignalementDTO;
import sn.dove.backend.service.mapper.SignalementMapper;

/**
 * Integration tests for the {@link SignalementResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class SignalementResourceIT {

    private static final String DEFAULT_MOTIF = "AAAAAAAAAA";
    private static final String UPDATED_MOTIF = "BBBBBBBBBB";

    private static final Instant DEFAULT_DATE_SIGNALEMENT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_DATE_SIGNALEMENT = Instant.ofEpochMilli(1787238072967L);

    private static final StatutSignalement DEFAULT_STATUT = StatutSignalement.ATTENTE;
    private static final StatutSignalement UPDATED_STATUT = StatutSignalement.A_TRAITER;

    private static final String ENTITY_API_URL = "/api/signalements";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    private ObjectMapper om;

    @Autowired
    private SignalementRepository signalementRepository;

    @Mock
    private SignalementRepository signalementRepositoryMock;

    @Autowired
    private SignalementMapper signalementMapper;

    @Mock
    private SignalementService signalementServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restSignalementMockMvc;

    private Signalement signalement;

    private Signalement insertedSignalement;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Signalement createEntity(EntityManager em) {
        Signalement signalement = new Signalement().motif(DEFAULT_MOTIF).dateSignalement(DEFAULT_DATE_SIGNALEMENT).statut(DEFAULT_STATUT);
        // Add required entity
        Utilisateur utilisateur;
        if (TestUtil.findAll(em, Utilisateur.class).isEmpty()) {
            utilisateur = UtilisateurResourceIT.createEntity();
            em.persist(utilisateur);
            em.flush();
        } else {
            utilisateur = TestUtil.findAll(em, Utilisateur.class).get(0);
        }
        signalement.setUtilisateur(utilisateur);
        // Add required entity
        Contenu contenu;
        if (TestUtil.findAll(em, Contenu.class).isEmpty()) {
            contenu = ContenuResourceIT.createEntity(em);
            em.persist(contenu);
            em.flush();
        } else {
            contenu = TestUtil.findAll(em, Contenu.class).get(0);
        }
        signalement.setContenu(contenu);
        return signalement;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Signalement createUpdatedEntity(EntityManager em) {
        Signalement updatedSignalement = new Signalement()
            .motif(UPDATED_MOTIF)
            .dateSignalement(UPDATED_DATE_SIGNALEMENT)
            .statut(UPDATED_STATUT);
        // Add required entity
        Utilisateur utilisateur;
        if (TestUtil.findAll(em, Utilisateur.class).isEmpty()) {
            utilisateur = UtilisateurResourceIT.createUpdatedEntity();
            em.persist(utilisateur);
            em.flush();
        } else {
            utilisateur = TestUtil.findAll(em, Utilisateur.class).get(0);
        }
        updatedSignalement.setUtilisateur(utilisateur);
        // Add required entity
        Contenu contenu;
        if (TestUtil.findAll(em, Contenu.class).isEmpty()) {
            contenu = ContenuResourceIT.createUpdatedEntity(em);
            em.persist(contenu);
            em.flush();
        } else {
            contenu = TestUtil.findAll(em, Contenu.class).get(0);
        }
        updatedSignalement.setContenu(contenu);
        return updatedSignalement;
    }

    @BeforeEach
    void initTest() {
        signalement = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedSignalement != null) {
            signalementRepository.delete(insertedSignalement);
            insertedSignalement = null;
        }
    }

    @Test
    @Transactional
    void createSignalement() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Signalement
        SignalementDTO signalementDTO = signalementMapper.toDto(signalement);
        var returnedSignalementDTO = om.readValue(
            restSignalementMockMvc
                .perform(
                    post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(signalementDTO))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            SignalementDTO.class
        );

        // Validate the Signalement in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedSignalement = signalementMapper.toEntity(returnedSignalementDTO);
        assertSignalementUpdatableFieldsEquals(returnedSignalement, getPersistedSignalement(returnedSignalement));

        insertedSignalement = returnedSignalement;
    }

    @Test
    @Transactional
    void createSignalementWithExistingId() throws Exception {
        // Create the Signalement with an existing ID
        insertedSignalement = signalementRepository.saveAndFlush(signalement);
        SignalementDTO signalementDTO = signalementMapper.toDto(signalement);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restSignalementMockMvc
            .perform(
                post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(signalementDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Signalement in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkDateSignalementIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        signalement.setDateSignalement(null);

        // Create the Signalement, which fails.
        SignalementDTO signalementDTO = signalementMapper.toDto(signalement);

        restSignalementMockMvc
            .perform(
                post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(signalementDTO))
            )
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkStatutIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        signalement.setStatut(null);

        // Create the Signalement, which fails.
        SignalementDTO signalementDTO = signalementMapper.toDto(signalement);

        restSignalementMockMvc
            .perform(
                post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(signalementDTO))
            )
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllSignalements() throws Exception {
        // Initialize the database
        insertedSignalement = signalementRepository.saveAndFlush(signalement);

        // Get all the signalementList
        restSignalementMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(signalement.getId().toString())))
            .andExpect(jsonPath("$.[*].motif").value(hasItem(DEFAULT_MOTIF)))
            .andExpect(jsonPath("$.[*].dateSignalement").value(hasItem(DEFAULT_DATE_SIGNALEMENT.toString())))
            .andExpect(jsonPath("$.[*].statut").value(hasItem(DEFAULT_STATUT.toString())));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllSignalementsWithEagerRelationshipsIsEnabled() throws Exception {
        when(signalementServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restSignalementMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(signalementServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllSignalementsWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(signalementServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restSignalementMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(signalementRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getSignalement() throws Exception {
        // Initialize the database
        insertedSignalement = signalementRepository.saveAndFlush(signalement);

        // Get the signalement
        restSignalementMockMvc
            .perform(get(ENTITY_API_URL_ID, signalement.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(signalement.getId().toString()))
            .andExpect(jsonPath("$.motif").value(DEFAULT_MOTIF))
            .andExpect(jsonPath("$.dateSignalement").value(DEFAULT_DATE_SIGNALEMENT.toString()))
            .andExpect(jsonPath("$.statut").value(DEFAULT_STATUT.toString()));
    }

    @Test
    @Transactional
    void getSignalementsByIdFiltering() throws Exception {
        // Initialize the database
        insertedSignalement = signalementRepository.saveAndFlush(signalement);

        UUID id = signalement.getId();

        defaultSignalementFiltering("id.equals=" + id, "id.notEquals=" + id);
    }

    @Test
    @Transactional
    void getAllSignalementsByDateSignalementIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedSignalement = signalementRepository.saveAndFlush(signalement);

        // Get all the signalementList where dateSignalement equals to
        defaultSignalementFiltering(
            "dateSignalement.equals=" + DEFAULT_DATE_SIGNALEMENT,
            "dateSignalement.equals=" + UPDATED_DATE_SIGNALEMENT
        );
    }

    @Test
    @Transactional
    void getAllSignalementsByDateSignalementIsInShouldWork() throws Exception {
        // Initialize the database
        insertedSignalement = signalementRepository.saveAndFlush(signalement);

        // Get all the signalementList where dateSignalement in
        defaultSignalementFiltering(
            "dateSignalement.in=" + DEFAULT_DATE_SIGNALEMENT + "," + UPDATED_DATE_SIGNALEMENT,
            "dateSignalement.in=" + UPDATED_DATE_SIGNALEMENT
        );
    }

    @Test
    @Transactional
    void getAllSignalementsByDateSignalementIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedSignalement = signalementRepository.saveAndFlush(signalement);

        // Get all the signalementList where dateSignalement is not null
        defaultSignalementFiltering("dateSignalement.specified=true", "dateSignalement.specified=false");
    }

    @Test
    @Transactional
    void getAllSignalementsByStatutIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedSignalement = signalementRepository.saveAndFlush(signalement);

        // Get all the signalementList where statut equals to
        defaultSignalementFiltering("statut.equals=" + DEFAULT_STATUT, "statut.equals=" + UPDATED_STATUT);
    }

    @Test
    @Transactional
    void getAllSignalementsByStatutIsInShouldWork() throws Exception {
        // Initialize the database
        insertedSignalement = signalementRepository.saveAndFlush(signalement);

        // Get all the signalementList where statut in
        defaultSignalementFiltering("statut.in=" + DEFAULT_STATUT + "," + UPDATED_STATUT, "statut.in=" + UPDATED_STATUT);
    }

    @Test
    @Transactional
    void getAllSignalementsByStatutIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedSignalement = signalementRepository.saveAndFlush(signalement);

        // Get all the signalementList where statut is not null
        defaultSignalementFiltering("statut.specified=true", "statut.specified=false");
    }

    @Test
    @Transactional
    void getAllSignalementsByUtilisateurIsEqualToSomething() throws Exception {
        Utilisateur utilisateur;
        if (TestUtil.findAll(em, Utilisateur.class).isEmpty()) {
            signalementRepository.saveAndFlush(signalement);
            utilisateur = UtilisateurResourceIT.createEntity();
        } else {
            utilisateur = TestUtil.findAll(em, Utilisateur.class).get(0);
        }
        em.persist(utilisateur);
        em.flush();
        signalement.setUtilisateur(utilisateur);
        signalementRepository.saveAndFlush(signalement);
        UUID utilisateurId = utilisateur.getId();
        // Get all the signalementList where utilisateur equals to utilisateurId
        defaultSignalementShouldBeFound("utilisateurId.equals=" + utilisateurId);

        // Get all the signalementList where utilisateur equals to UUID.randomUUID()
        defaultSignalementShouldNotBeFound("utilisateurId.equals=" + UUID.randomUUID());
    }

    @Test
    @Transactional
    void getAllSignalementsByContenuIsEqualToSomething() throws Exception {
        Contenu contenu;
        if (TestUtil.findAll(em, Contenu.class).isEmpty()) {
            signalementRepository.saveAndFlush(signalement);
            contenu = ContenuResourceIT.createEntity(em);
        } else {
            contenu = TestUtil.findAll(em, Contenu.class).get(0);
        }
        em.persist(contenu);
        em.flush();
        signalement.setContenu(contenu);
        signalementRepository.saveAndFlush(signalement);
        UUID contenuId = contenu.getId();
        // Get all the signalementList where contenu equals to contenuId
        defaultSignalementShouldBeFound("contenuId.equals=" + contenuId);

        // Get all the signalementList where contenu equals to UUID.randomUUID()
        defaultSignalementShouldNotBeFound("contenuId.equals=" + UUID.randomUUID());
    }

    private void defaultSignalementFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultSignalementShouldBeFound(shouldBeFound);
        defaultSignalementShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultSignalementShouldBeFound(String filter) throws Exception {
        restSignalementMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(signalement.getId().toString())))
            .andExpect(jsonPath("$.[*].motif").value(hasItem(DEFAULT_MOTIF)))
            .andExpect(jsonPath("$.[*].dateSignalement").value(hasItem(DEFAULT_DATE_SIGNALEMENT.toString())))
            .andExpect(jsonPath("$.[*].statut").value(hasItem(DEFAULT_STATUT.toString())));

        // Check, that the count call also returns 1
        restSignalementMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultSignalementShouldNotBeFound(String filter) throws Exception {
        restSignalementMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restSignalementMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingSignalement() throws Exception {
        // Get the signalement
        restSignalementMockMvc.perform(get(ENTITY_API_URL_ID, UUID.randomUUID().toString())).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingSignalement() throws Exception {
        // Initialize the database
        insertedSignalement = signalementRepository.saveAndFlush(signalement);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the signalement
        Signalement updatedSignalement = signalementRepository.findById(signalement.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedSignalement are not directly saved in db
        em.detach(updatedSignalement);
        updatedSignalement.motif(UPDATED_MOTIF).dateSignalement(UPDATED_DATE_SIGNALEMENT).statut(UPDATED_STATUT);
        SignalementDTO signalementDTO = signalementMapper.toDto(updatedSignalement);

        restSignalementMockMvc
            .perform(
                put(ENTITY_API_URL_ID, signalementDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(signalementDTO))
            )
            .andExpect(status().isOk());

        // Validate the Signalement in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedSignalementToMatchAllProperties(updatedSignalement);
    }

    @Test
    @Transactional
    void putNonExistingSignalement() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        signalement.setId(UUID.randomUUID());

        // Create the Signalement
        SignalementDTO signalementDTO = signalementMapper.toDto(signalement);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restSignalementMockMvc
            .perform(
                put(ENTITY_API_URL_ID, signalementDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(signalementDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Signalement in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchSignalement() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        signalement.setId(UUID.randomUUID());

        // Create the Signalement
        SignalementDTO signalementDTO = signalementMapper.toDto(signalement);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSignalementMockMvc
            .perform(
                put(ENTITY_API_URL_ID, UUID.randomUUID())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(signalementDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Signalement in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamSignalement() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        signalement.setId(UUID.randomUUID());

        // Create the Signalement
        SignalementDTO signalementDTO = signalementMapper.toDto(signalement);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSignalementMockMvc
            .perform(put(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(signalementDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Signalement in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateSignalementWithPatch() throws Exception {
        // Initialize the database
        insertedSignalement = signalementRepository.saveAndFlush(signalement);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the signalement using partial update
        Signalement partialUpdatedSignalement = new Signalement();
        partialUpdatedSignalement.setId(signalement.getId());

        partialUpdatedSignalement.dateSignalement(UPDATED_DATE_SIGNALEMENT);

        restSignalementMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedSignalement.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedSignalement))
            )
            .andExpect(status().isOk());

        // Validate the Signalement in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertSignalementUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedSignalement, signalement),
            getPersistedSignalement(signalement)
        );
    }

    @Test
    @Transactional
    void fullUpdateSignalementWithPatch() throws Exception {
        // Initialize the database
        insertedSignalement = signalementRepository.saveAndFlush(signalement);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the signalement using partial update
        Signalement partialUpdatedSignalement = new Signalement();
        partialUpdatedSignalement.setId(signalement.getId());

        partialUpdatedSignalement.motif(UPDATED_MOTIF).dateSignalement(UPDATED_DATE_SIGNALEMENT).statut(UPDATED_STATUT);

        restSignalementMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedSignalement.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedSignalement))
            )
            .andExpect(status().isOk());

        // Validate the Signalement in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertSignalementUpdatableFieldsEquals(partialUpdatedSignalement, getPersistedSignalement(partialUpdatedSignalement));
    }

    @Test
    @Transactional
    void patchNonExistingSignalement() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        signalement.setId(UUID.randomUUID());

        // Create the Signalement
        SignalementDTO signalementDTO = signalementMapper.toDto(signalement);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restSignalementMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, signalementDTO.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(signalementDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Signalement in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchSignalement() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        signalement.setId(UUID.randomUUID());

        // Create the Signalement
        SignalementDTO signalementDTO = signalementMapper.toDto(signalement);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSignalementMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, UUID.randomUUID())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(signalementDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Signalement in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamSignalement() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        signalement.setId(UUID.randomUUID());

        // Create the Signalement
        SignalementDTO signalementDTO = signalementMapper.toDto(signalement);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSignalementMockMvc
            .perform(
                patch(ENTITY_API_URL).with(csrf()).contentType("application/merge-patch+json").content(om.writeValueAsBytes(signalementDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the Signalement in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteSignalement() throws Exception {
        // Initialize the database
        insertedSignalement = signalementRepository.saveAndFlush(signalement);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the signalement
        restSignalementMockMvc
            .perform(delete(ENTITY_API_URL_ID, signalement.getId().toString()).with(csrf()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return signalementRepository.count();
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

    protected Signalement getPersistedSignalement(Signalement signalement) {
        return signalementRepository.findById(signalement.getId()).orElseThrow();
    }

    protected void assertPersistedSignalementToMatchAllProperties(Signalement expectedSignalement) {
        assertSignalementAllPropertiesEquals(expectedSignalement, getPersistedSignalement(expectedSignalement));
    }

    protected void assertPersistedSignalementToMatchUpdatableProperties(Signalement expectedSignalement) {
        assertSignalementAllUpdatablePropertiesEquals(expectedSignalement, getPersistedSignalement(expectedSignalement));
    }
}
