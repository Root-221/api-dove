package sn.dove.backend.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static sn.dove.backend.domain.FeedBackAsserts.*;
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
import sn.dove.backend.domain.FeedBack;
import sn.dove.backend.domain.Utilisateur;
import sn.dove.backend.repository.FeedBackRepository;
import sn.dove.backend.service.FeedBackService;
import sn.dove.backend.service.dto.FeedBackDTO;
import sn.dove.backend.service.mapper.FeedBackMapper;

/**
 * Integration tests for the {@link FeedBackResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class FeedBackResourceIT {

    private static final String DEFAULT_COMMENTAIRE = "AAAAAAAAAA";
    private static final String UPDATED_COMMENTAIRE = "BBBBBBBBBB";

    private static final Integer DEFAULT_NOTE = 0;
    private static final Integer UPDATED_NOTE = 1;
    private static final Integer SMALLER_NOTE = 0 - 1;

    private static final Instant DEFAULT_DATE_CREATION = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_DATE_CREATION = Instant.ofEpochMilli(1787238072967L);

    private static final String DEFAULT_STATUT_TRAITEMENT = "AAAAAAAAAA";
    private static final String UPDATED_STATUT_TRAITEMENT = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/feed-backs";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    private ObjectMapper om;

    @Autowired
    private FeedBackRepository feedBackRepository;

    @Mock
    private FeedBackRepository feedBackRepositoryMock;

    @Autowired
    private FeedBackMapper feedBackMapper;

    @Mock
    private FeedBackService feedBackServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restFeedBackMockMvc;

    private FeedBack feedBack;

    private FeedBack insertedFeedBack;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static FeedBack createEntity(EntityManager em) {
        FeedBack feedBack = new FeedBack()
            .commentaire(DEFAULT_COMMENTAIRE)
            .note(DEFAULT_NOTE)
            .dateCreation(DEFAULT_DATE_CREATION)
            .statutTraitement(DEFAULT_STATUT_TRAITEMENT);
        // Add required entity
        Utilisateur utilisateur;
        if (TestUtil.findAll(em, Utilisateur.class).isEmpty()) {
            utilisateur = UtilisateurResourceIT.createEntity();
            em.persist(utilisateur);
            em.flush();
        } else {
            utilisateur = TestUtil.findAll(em, Utilisateur.class).get(0);
        }
        feedBack.setUtilisateur(utilisateur);
        // Add required entity
        Contenu contenu;
        if (TestUtil.findAll(em, Contenu.class).isEmpty()) {
            contenu = ContenuResourceIT.createEntity(em);
            em.persist(contenu);
            em.flush();
        } else {
            contenu = TestUtil.findAll(em, Contenu.class).get(0);
        }
        feedBack.setContenu(contenu);
        return feedBack;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static FeedBack createUpdatedEntity(EntityManager em) {
        FeedBack updatedFeedBack = new FeedBack()
            .commentaire(UPDATED_COMMENTAIRE)
            .note(UPDATED_NOTE)
            .dateCreation(UPDATED_DATE_CREATION)
            .statutTraitement(UPDATED_STATUT_TRAITEMENT);
        // Add required entity
        Utilisateur utilisateur;
        if (TestUtil.findAll(em, Utilisateur.class).isEmpty()) {
            utilisateur = UtilisateurResourceIT.createUpdatedEntity();
            em.persist(utilisateur);
            em.flush();
        } else {
            utilisateur = TestUtil.findAll(em, Utilisateur.class).get(0);
        }
        updatedFeedBack.setUtilisateur(utilisateur);
        // Add required entity
        Contenu contenu;
        if (TestUtil.findAll(em, Contenu.class).isEmpty()) {
            contenu = ContenuResourceIT.createUpdatedEntity(em);
            em.persist(contenu);
            em.flush();
        } else {
            contenu = TestUtil.findAll(em, Contenu.class).get(0);
        }
        updatedFeedBack.setContenu(contenu);
        return updatedFeedBack;
    }

    @BeforeEach
    void initTest() {
        feedBack = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedFeedBack != null) {
            feedBackRepository.delete(insertedFeedBack);
            insertedFeedBack = null;
        }
    }

    @Test
    @Transactional
    void createFeedBack() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the FeedBack
        FeedBackDTO feedBackDTO = feedBackMapper.toDto(feedBack);
        var returnedFeedBackDTO = om.readValue(
            restFeedBackMockMvc
                .perform(
                    post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(feedBackDTO))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            FeedBackDTO.class
        );

        // Validate the FeedBack in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedFeedBack = feedBackMapper.toEntity(returnedFeedBackDTO);
        assertFeedBackUpdatableFieldsEquals(returnedFeedBack, getPersistedFeedBack(returnedFeedBack));

        insertedFeedBack = returnedFeedBack;
    }

    @Test
    @Transactional
    void createFeedBackWithExistingId() throws Exception {
        // Create the FeedBack with an existing ID
        insertedFeedBack = feedBackRepository.saveAndFlush(feedBack);
        FeedBackDTO feedBackDTO = feedBackMapper.toDto(feedBack);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restFeedBackMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(feedBackDTO)))
            .andExpect(status().isBadRequest());

        // Validate the FeedBack in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkDateCreationIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        feedBack.setDateCreation(null);

        // Create the FeedBack, which fails.
        FeedBackDTO feedBackDTO = feedBackMapper.toDto(feedBack);

        restFeedBackMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(feedBackDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkStatutTraitementIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        feedBack.setStatutTraitement(null);

        // Create the FeedBack, which fails.
        FeedBackDTO feedBackDTO = feedBackMapper.toDto(feedBack);

        restFeedBackMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(feedBackDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllFeedBacks() throws Exception {
        // Initialize the database
        insertedFeedBack = feedBackRepository.saveAndFlush(feedBack);

        // Get all the feedBackList
        restFeedBackMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(feedBack.getId().toString())))
            .andExpect(jsonPath("$.[*].commentaire").value(hasItem(DEFAULT_COMMENTAIRE)))
            .andExpect(jsonPath("$.[*].note").value(hasItem(DEFAULT_NOTE)))
            .andExpect(jsonPath("$.[*].dateCreation").value(hasItem(DEFAULT_DATE_CREATION.toString())))
            .andExpect(jsonPath("$.[*].statutTraitement").value(hasItem(DEFAULT_STATUT_TRAITEMENT)));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllFeedBacksWithEagerRelationshipsIsEnabled() throws Exception {
        when(feedBackServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restFeedBackMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(feedBackServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllFeedBacksWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(feedBackServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restFeedBackMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(feedBackRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getFeedBack() throws Exception {
        // Initialize the database
        insertedFeedBack = feedBackRepository.saveAndFlush(feedBack);

        // Get the feedBack
        restFeedBackMockMvc
            .perform(get(ENTITY_API_URL_ID, feedBack.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(feedBack.getId().toString()))
            .andExpect(jsonPath("$.commentaire").value(DEFAULT_COMMENTAIRE))
            .andExpect(jsonPath("$.note").value(DEFAULT_NOTE))
            .andExpect(jsonPath("$.dateCreation").value(DEFAULT_DATE_CREATION.toString()))
            .andExpect(jsonPath("$.statutTraitement").value(DEFAULT_STATUT_TRAITEMENT));
    }

    @Test
    @Transactional
    void getFeedBacksByIdFiltering() throws Exception {
        // Initialize the database
        insertedFeedBack = feedBackRepository.saveAndFlush(feedBack);

        UUID id = feedBack.getId();

        defaultFeedBackFiltering("id.equals=" + id, "id.notEquals=" + id);
    }

    @Test
    @Transactional
    void getAllFeedBacksByNoteIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedFeedBack = feedBackRepository.saveAndFlush(feedBack);

        // Get all the feedBackList where note equals to
        defaultFeedBackFiltering("note.equals=" + DEFAULT_NOTE, "note.equals=" + UPDATED_NOTE);
    }

    @Test
    @Transactional
    void getAllFeedBacksByNoteIsInShouldWork() throws Exception {
        // Initialize the database
        insertedFeedBack = feedBackRepository.saveAndFlush(feedBack);

        // Get all the feedBackList where note in
        defaultFeedBackFiltering("note.in=" + DEFAULT_NOTE + "," + UPDATED_NOTE, "note.in=" + UPDATED_NOTE);
    }

    @Test
    @Transactional
    void getAllFeedBacksByNoteIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedFeedBack = feedBackRepository.saveAndFlush(feedBack);

        // Get all the feedBackList where note is not null
        defaultFeedBackFiltering("note.specified=true", "note.specified=false");
    }

    @Test
    @Transactional
    void getAllFeedBacksByNoteIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedFeedBack = feedBackRepository.saveAndFlush(feedBack);

        // Get all the feedBackList where note is greater than or equal to
        defaultFeedBackFiltering("note.greaterThanOrEqual=" + DEFAULT_NOTE, "note.greaterThanOrEqual=" + (DEFAULT_NOTE + 1));
    }

    @Test
    @Transactional
    void getAllFeedBacksByNoteIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedFeedBack = feedBackRepository.saveAndFlush(feedBack);

        // Get all the feedBackList where note is less than or equal to
        defaultFeedBackFiltering("note.lessThanOrEqual=" + DEFAULT_NOTE, "note.lessThanOrEqual=" + SMALLER_NOTE);
    }

    @Test
    @Transactional
    void getAllFeedBacksByNoteIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedFeedBack = feedBackRepository.saveAndFlush(feedBack);

        // Get all the feedBackList where note is less than
        defaultFeedBackFiltering("note.lessThan=" + (DEFAULT_NOTE + 1), "note.lessThan=" + DEFAULT_NOTE);
    }

    @Test
    @Transactional
    void getAllFeedBacksByNoteIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedFeedBack = feedBackRepository.saveAndFlush(feedBack);

        // Get all the feedBackList where note is greater than
        defaultFeedBackFiltering("note.greaterThan=" + SMALLER_NOTE, "note.greaterThan=" + DEFAULT_NOTE);
    }

    @Test
    @Transactional
    void getAllFeedBacksByDateCreationIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedFeedBack = feedBackRepository.saveAndFlush(feedBack);

        // Get all the feedBackList where dateCreation equals to
        defaultFeedBackFiltering("dateCreation.equals=" + DEFAULT_DATE_CREATION, "dateCreation.equals=" + UPDATED_DATE_CREATION);
    }

    @Test
    @Transactional
    void getAllFeedBacksByDateCreationIsInShouldWork() throws Exception {
        // Initialize the database
        insertedFeedBack = feedBackRepository.saveAndFlush(feedBack);

        // Get all the feedBackList where dateCreation in
        defaultFeedBackFiltering(
            "dateCreation.in=" + DEFAULT_DATE_CREATION + "," + UPDATED_DATE_CREATION,
            "dateCreation.in=" + UPDATED_DATE_CREATION
        );
    }

    @Test
    @Transactional
    void getAllFeedBacksByDateCreationIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedFeedBack = feedBackRepository.saveAndFlush(feedBack);

        // Get all the feedBackList where dateCreation is not null
        defaultFeedBackFiltering("dateCreation.specified=true", "dateCreation.specified=false");
    }

    @Test
    @Transactional
    void getAllFeedBacksByStatutTraitementIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedFeedBack = feedBackRepository.saveAndFlush(feedBack);

        // Get all the feedBackList where statutTraitement equals to
        defaultFeedBackFiltering(
            "statutTraitement.equals=" + DEFAULT_STATUT_TRAITEMENT,
            "statutTraitement.equals=" + UPDATED_STATUT_TRAITEMENT
        );
    }

    @Test
    @Transactional
    void getAllFeedBacksByStatutTraitementIsInShouldWork() throws Exception {
        // Initialize the database
        insertedFeedBack = feedBackRepository.saveAndFlush(feedBack);

        // Get all the feedBackList where statutTraitement in
        defaultFeedBackFiltering(
            "statutTraitement.in=" + DEFAULT_STATUT_TRAITEMENT + "," + UPDATED_STATUT_TRAITEMENT,
            "statutTraitement.in=" + UPDATED_STATUT_TRAITEMENT
        );
    }

    @Test
    @Transactional
    void getAllFeedBacksByStatutTraitementIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedFeedBack = feedBackRepository.saveAndFlush(feedBack);

        // Get all the feedBackList where statutTraitement is not null
        defaultFeedBackFiltering("statutTraitement.specified=true", "statutTraitement.specified=false");
    }

    @Test
    @Transactional
    void getAllFeedBacksByStatutTraitementContainsSomething() throws Exception {
        // Initialize the database
        insertedFeedBack = feedBackRepository.saveAndFlush(feedBack);

        // Get all the feedBackList where statutTraitement contains
        defaultFeedBackFiltering(
            "statutTraitement.contains=" + DEFAULT_STATUT_TRAITEMENT,
            "statutTraitement.contains=" + UPDATED_STATUT_TRAITEMENT
        );
    }

    @Test
    @Transactional
    void getAllFeedBacksByStatutTraitementNotContainsSomething() throws Exception {
        // Initialize the database
        insertedFeedBack = feedBackRepository.saveAndFlush(feedBack);

        // Get all the feedBackList where statutTraitement does not contain
        defaultFeedBackFiltering(
            "statutTraitement.doesNotContain=" + UPDATED_STATUT_TRAITEMENT,
            "statutTraitement.doesNotContain=" + DEFAULT_STATUT_TRAITEMENT
        );
    }

    @Test
    @Transactional
    void getAllFeedBacksByUtilisateurIsEqualToSomething() throws Exception {
        Utilisateur utilisateur;
        if (TestUtil.findAll(em, Utilisateur.class).isEmpty()) {
            feedBackRepository.saveAndFlush(feedBack);
            utilisateur = UtilisateurResourceIT.createEntity();
        } else {
            utilisateur = TestUtil.findAll(em, Utilisateur.class).get(0);
        }
        em.persist(utilisateur);
        em.flush();
        feedBack.setUtilisateur(utilisateur);
        feedBackRepository.saveAndFlush(feedBack);
        UUID utilisateurId = utilisateur.getId();
        // Get all the feedBackList where utilisateur equals to utilisateurId
        defaultFeedBackShouldBeFound("utilisateurId.equals=" + utilisateurId);

        // Get all the feedBackList where utilisateur equals to UUID.randomUUID()
        defaultFeedBackShouldNotBeFound("utilisateurId.equals=" + UUID.randomUUID());
    }

    @Test
    @Transactional
    void getAllFeedBacksByContenuIsEqualToSomething() throws Exception {
        Contenu contenu;
        if (TestUtil.findAll(em, Contenu.class).isEmpty()) {
            feedBackRepository.saveAndFlush(feedBack);
            contenu = ContenuResourceIT.createEntity(em);
        } else {
            contenu = TestUtil.findAll(em, Contenu.class).get(0);
        }
        em.persist(contenu);
        em.flush();
        feedBack.setContenu(contenu);
        feedBackRepository.saveAndFlush(feedBack);
        UUID contenuId = contenu.getId();
        // Get all the feedBackList where contenu equals to contenuId
        defaultFeedBackShouldBeFound("contenuId.equals=" + contenuId);

        // Get all the feedBackList where contenu equals to UUID.randomUUID()
        defaultFeedBackShouldNotBeFound("contenuId.equals=" + UUID.randomUUID());
    }

    private void defaultFeedBackFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultFeedBackShouldBeFound(shouldBeFound);
        defaultFeedBackShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultFeedBackShouldBeFound(String filter) throws Exception {
        restFeedBackMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(feedBack.getId().toString())))
            .andExpect(jsonPath("$.[*].commentaire").value(hasItem(DEFAULT_COMMENTAIRE)))
            .andExpect(jsonPath("$.[*].note").value(hasItem(DEFAULT_NOTE)))
            .andExpect(jsonPath("$.[*].dateCreation").value(hasItem(DEFAULT_DATE_CREATION.toString())))
            .andExpect(jsonPath("$.[*].statutTraitement").value(hasItem(DEFAULT_STATUT_TRAITEMENT)));

        // Check, that the count call also returns 1
        restFeedBackMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultFeedBackShouldNotBeFound(String filter) throws Exception {
        restFeedBackMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restFeedBackMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingFeedBack() throws Exception {
        // Get the feedBack
        restFeedBackMockMvc.perform(get(ENTITY_API_URL_ID, UUID.randomUUID().toString())).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingFeedBack() throws Exception {
        // Initialize the database
        insertedFeedBack = feedBackRepository.saveAndFlush(feedBack);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the feedBack
        FeedBack updatedFeedBack = feedBackRepository.findById(feedBack.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedFeedBack are not directly saved in db
        em.detach(updatedFeedBack);
        updatedFeedBack
            .commentaire(UPDATED_COMMENTAIRE)
            .note(UPDATED_NOTE)
            .dateCreation(UPDATED_DATE_CREATION)
            .statutTraitement(UPDATED_STATUT_TRAITEMENT);
        FeedBackDTO feedBackDTO = feedBackMapper.toDto(updatedFeedBack);

        restFeedBackMockMvc
            .perform(
                put(ENTITY_API_URL_ID, feedBackDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(feedBackDTO))
            )
            .andExpect(status().isOk());

        // Validate the FeedBack in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedFeedBackToMatchAllProperties(updatedFeedBack);
    }

    @Test
    @Transactional
    void putNonExistingFeedBack() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        feedBack.setId(UUID.randomUUID());

        // Create the FeedBack
        FeedBackDTO feedBackDTO = feedBackMapper.toDto(feedBack);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restFeedBackMockMvc
            .perform(
                put(ENTITY_API_URL_ID, feedBackDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(feedBackDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the FeedBack in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchFeedBack() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        feedBack.setId(UUID.randomUUID());

        // Create the FeedBack
        FeedBackDTO feedBackDTO = feedBackMapper.toDto(feedBack);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restFeedBackMockMvc
            .perform(
                put(ENTITY_API_URL_ID, UUID.randomUUID())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(feedBackDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the FeedBack in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamFeedBack() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        feedBack.setId(UUID.randomUUID());

        // Create the FeedBack
        FeedBackDTO feedBackDTO = feedBackMapper.toDto(feedBack);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restFeedBackMockMvc
            .perform(put(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(feedBackDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the FeedBack in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateFeedBackWithPatch() throws Exception {
        // Initialize the database
        insertedFeedBack = feedBackRepository.saveAndFlush(feedBack);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the feedBack using partial update
        FeedBack partialUpdatedFeedBack = new FeedBack();
        partialUpdatedFeedBack.setId(feedBack.getId());

        partialUpdatedFeedBack.note(UPDATED_NOTE).dateCreation(UPDATED_DATE_CREATION).statutTraitement(UPDATED_STATUT_TRAITEMENT);

        restFeedBackMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedFeedBack.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedFeedBack))
            )
            .andExpect(status().isOk());

        // Validate the FeedBack in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertFeedBackUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedFeedBack, feedBack), getPersistedFeedBack(feedBack));
    }

    @Test
    @Transactional
    void fullUpdateFeedBackWithPatch() throws Exception {
        // Initialize the database
        insertedFeedBack = feedBackRepository.saveAndFlush(feedBack);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the feedBack using partial update
        FeedBack partialUpdatedFeedBack = new FeedBack();
        partialUpdatedFeedBack.setId(feedBack.getId());

        partialUpdatedFeedBack
            .commentaire(UPDATED_COMMENTAIRE)
            .note(UPDATED_NOTE)
            .dateCreation(UPDATED_DATE_CREATION)
            .statutTraitement(UPDATED_STATUT_TRAITEMENT);

        restFeedBackMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedFeedBack.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedFeedBack))
            )
            .andExpect(status().isOk());

        // Validate the FeedBack in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertFeedBackUpdatableFieldsEquals(partialUpdatedFeedBack, getPersistedFeedBack(partialUpdatedFeedBack));
    }

    @Test
    @Transactional
    void patchNonExistingFeedBack() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        feedBack.setId(UUID.randomUUID());

        // Create the FeedBack
        FeedBackDTO feedBackDTO = feedBackMapper.toDto(feedBack);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restFeedBackMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, feedBackDTO.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(feedBackDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the FeedBack in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchFeedBack() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        feedBack.setId(UUID.randomUUID());

        // Create the FeedBack
        FeedBackDTO feedBackDTO = feedBackMapper.toDto(feedBack);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restFeedBackMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, UUID.randomUUID())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(feedBackDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the FeedBack in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamFeedBack() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        feedBack.setId(UUID.randomUUID());

        // Create the FeedBack
        FeedBackDTO feedBackDTO = feedBackMapper.toDto(feedBack);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restFeedBackMockMvc
            .perform(
                patch(ENTITY_API_URL).with(csrf()).contentType("application/merge-patch+json").content(om.writeValueAsBytes(feedBackDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the FeedBack in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteFeedBack() throws Exception {
        // Initialize the database
        insertedFeedBack = feedBackRepository.saveAndFlush(feedBack);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the feedBack
        restFeedBackMockMvc
            .perform(delete(ENTITY_API_URL_ID, feedBack.getId().toString()).with(csrf()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return feedBackRepository.count();
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

    protected FeedBack getPersistedFeedBack(FeedBack feedBack) {
        return feedBackRepository.findById(feedBack.getId()).orElseThrow();
    }

    protected void assertPersistedFeedBackToMatchAllProperties(FeedBack expectedFeedBack) {
        assertFeedBackAllPropertiesEquals(expectedFeedBack, getPersistedFeedBack(expectedFeedBack));
    }

    protected void assertPersistedFeedBackToMatchUpdatableProperties(FeedBack expectedFeedBack) {
        assertFeedBackAllUpdatablePropertiesEquals(expectedFeedBack, getPersistedFeedBack(expectedFeedBack));
    }
}
