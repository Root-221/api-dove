package sn.dove.backend.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static sn.dove.backend.domain.ContenuAsserts.*;
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
import sn.dove.backend.domain.Application;
import sn.dove.backend.domain.Contenu;
import sn.dove.backend.domain.Metier;
import sn.dove.backend.domain.Module;
import sn.dove.backend.domain.Utilisateur;
import sn.dove.backend.domain.enumeration.StatutContenu;
import sn.dove.backend.domain.enumeration.TypeContenu;
import sn.dove.backend.repository.ContenuRepository;
import sn.dove.backend.service.ContenuService;
import sn.dove.backend.service.dto.ContenuDTO;
import sn.dove.backend.service.mapper.ContenuMapper;

/**
 * Integration tests for the {@link ContenuResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class ContenuResourceIT {

    private static final String DEFAULT_TITRE = "AAAAAAAAAA";
    private static final String UPDATED_TITRE = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final TypeContenu DEFAULT_TYPE_CONTENU = TypeContenu.FAQ;
    private static final TypeContenu UPDATED_TYPE_CONTENU = TypeContenu.FICHEPRATIQUE;

    private static final StatutContenu DEFAULT_STATUT = StatutContenu.BROUILLON;
    private static final StatutContenu UPDATED_STATUT = StatutContenu.EN_ATTENTE_VALIDATION;

    private static final Integer DEFAULT_VERSION = 1;
    private static final Integer UPDATED_VERSION = 2;
    private static final Integer SMALLER_VERSION = 1 - 1;

    private static final Instant DEFAULT_DATE_PUBLICATION = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_DATE_PUBLICATION = Instant.ofEpochMilli(1787238072967L);

    private static final Instant DEFAULT_DERNIERE_MISE_A_JOUR = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_DERNIERE_MISE_A_JOUR = Instant.ofEpochMilli(1787238072967L);

    private static final Instant DEFAULT_PROCHAINE_REVUE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_PROCHAINE_REVUE = Instant.ofEpochMilli(1787238072967L);

    private static final String DEFAULT_MOTS_CLES = "AAAAAAAAAA";
    private static final String UPDATED_MOTS_CLES = "BBBBBBBBBB";

    private static final String DEFAULT_TRANSCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_TRANSCRIPTION = "BBBBBBBBBB";

    private static final Integer DEFAULT_PROGRESSION = 0;
    private static final Integer UPDATED_PROGRESSION = 1;
    private static final Integer SMALLER_PROGRESSION = 0 - 1;

    private static final Instant DEFAULT_DATE_CREATION = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_DATE_CREATION = Instant.ofEpochMilli(1787238072967L);

    private static final String ENTITY_API_URL = "/api/contenus";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    private ObjectMapper om;

    @Autowired
    private ContenuRepository contenuRepository;

    @Mock
    private ContenuRepository contenuRepositoryMock;

    @Autowired
    private ContenuMapper contenuMapper;

    @Mock
    private ContenuService contenuServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restContenuMockMvc;

    private Contenu contenu;

    private Contenu insertedContenu;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Contenu createEntity(EntityManager em) {
        Contenu contenu = new Contenu()
            .titre(DEFAULT_TITRE)
            .description(DEFAULT_DESCRIPTION)
            .typeContenu(DEFAULT_TYPE_CONTENU)
            .statut(DEFAULT_STATUT)
            .version(DEFAULT_VERSION)
            .datePublication(DEFAULT_DATE_PUBLICATION)
            .derniereMiseAJour(DEFAULT_DERNIERE_MISE_A_JOUR)
            .prochaineRevue(DEFAULT_PROCHAINE_REVUE)
            .motsCles(DEFAULT_MOTS_CLES)
            .transcription(DEFAULT_TRANSCRIPTION)
            .progression(DEFAULT_PROGRESSION)
            .dateCreation(DEFAULT_DATE_CREATION);
        // Add required entity
        Application application;
        if (TestUtil.findAll(em, Application.class).isEmpty()) {
            application = ApplicationResourceIT.createEntity();
            em.persist(application);
            em.flush();
        } else {
            application = TestUtil.findAll(em, Application.class).get(0);
        }
        contenu.setApplication(application);
        // Add required entity
        Module module;
        if (TestUtil.findAll(em, Module.class).isEmpty()) {
            module = ModuleResourceIT.createEntity(em);
            em.persist(module);
            em.flush();
        } else {
            module = TestUtil.findAll(em, Module.class).get(0);
        }
        contenu.setModule(module);
        // Add required entity
        Metier metier;
        if (TestUtil.findAll(em, Metier.class).isEmpty()) {
            metier = MetierResourceIT.createEntity();
            em.persist(metier);
            em.flush();
        } else {
            metier = TestUtil.findAll(em, Metier.class).get(0);
        }
        contenu.setMetier(metier);
        // Add required entity
        Utilisateur utilisateur;
        if (TestUtil.findAll(em, Utilisateur.class).isEmpty()) {
            utilisateur = UtilisateurResourceIT.createEntity();
            em.persist(utilisateur);
            em.flush();
        } else {
            utilisateur = TestUtil.findAll(em, Utilisateur.class).get(0);
        }
        contenu.setAuteur(utilisateur);
        return contenu;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Contenu createUpdatedEntity(EntityManager em) {
        Contenu updatedContenu = new Contenu()
            .titre(UPDATED_TITRE)
            .description(UPDATED_DESCRIPTION)
            .typeContenu(UPDATED_TYPE_CONTENU)
            .statut(UPDATED_STATUT)
            .version(UPDATED_VERSION)
            .datePublication(UPDATED_DATE_PUBLICATION)
            .derniereMiseAJour(UPDATED_DERNIERE_MISE_A_JOUR)
            .prochaineRevue(UPDATED_PROCHAINE_REVUE)
            .motsCles(UPDATED_MOTS_CLES)
            .transcription(UPDATED_TRANSCRIPTION)
            .progression(UPDATED_PROGRESSION)
            .dateCreation(UPDATED_DATE_CREATION);
        // Add required entity
        Application application;
        if (TestUtil.findAll(em, Application.class).isEmpty()) {
            application = ApplicationResourceIT.createUpdatedEntity();
            em.persist(application);
            em.flush();
        } else {
            application = TestUtil.findAll(em, Application.class).get(0);
        }
        updatedContenu.setApplication(application);
        // Add required entity
        Module module;
        if (TestUtil.findAll(em, Module.class).isEmpty()) {
            module = ModuleResourceIT.createUpdatedEntity(em);
            em.persist(module);
            em.flush();
        } else {
            module = TestUtil.findAll(em, Module.class).get(0);
        }
        updatedContenu.setModule(module);
        // Add required entity
        Metier metier;
        if (TestUtil.findAll(em, Metier.class).isEmpty()) {
            metier = MetierResourceIT.createUpdatedEntity();
            em.persist(metier);
            em.flush();
        } else {
            metier = TestUtil.findAll(em, Metier.class).get(0);
        }
        updatedContenu.setMetier(metier);
        // Add required entity
        Utilisateur utilisateur;
        if (TestUtil.findAll(em, Utilisateur.class).isEmpty()) {
            utilisateur = UtilisateurResourceIT.createUpdatedEntity();
            em.persist(utilisateur);
            em.flush();
        } else {
            utilisateur = TestUtil.findAll(em, Utilisateur.class).get(0);
        }
        updatedContenu.setAuteur(utilisateur);
        return updatedContenu;
    }

    @BeforeEach
    void initTest() {
        contenu = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedContenu != null) {
            contenuRepository.delete(insertedContenu);
            insertedContenu = null;
        }
    }

    @Test
    @Transactional
    void createContenu() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Contenu
        ContenuDTO contenuDTO = contenuMapper.toDto(contenu);
        var returnedContenuDTO = om.readValue(
            restContenuMockMvc
                .perform(
                    post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(contenuDTO))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            ContenuDTO.class
        );

        // Validate the Contenu in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedContenu = contenuMapper.toEntity(returnedContenuDTO);
        assertContenuUpdatableFieldsEquals(returnedContenu, getPersistedContenu(returnedContenu));

        insertedContenu = returnedContenu;
    }

    @Test
    @Transactional
    void createContenuWithExistingId() throws Exception {
        // Create the Contenu with an existing ID
        insertedContenu = contenuRepository.saveAndFlush(contenu);
        ContenuDTO contenuDTO = contenuMapper.toDto(contenu);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restContenuMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(contenuDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Contenu in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkTitreIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        contenu.setTitre(null);

        // Create the Contenu, which fails.
        ContenuDTO contenuDTO = contenuMapper.toDto(contenu);

        restContenuMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(contenuDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkTypeContenuIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        contenu.setTypeContenu(null);

        // Create the Contenu, which fails.
        ContenuDTO contenuDTO = contenuMapper.toDto(contenu);

        restContenuMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(contenuDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkStatutIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        contenu.setStatut(null);

        // Create the Contenu, which fails.
        ContenuDTO contenuDTO = contenuMapper.toDto(contenu);

        restContenuMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(contenuDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkVersionIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        contenu.setVersion(null);

        // Create the Contenu, which fails.
        ContenuDTO contenuDTO = contenuMapper.toDto(contenu);

        restContenuMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(contenuDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkDerniereMiseAJourIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        contenu.setDerniereMiseAJour(null);

        // Create the Contenu, which fails.
        ContenuDTO contenuDTO = contenuMapper.toDto(contenu);

        restContenuMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(contenuDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkDateCreationIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        contenu.setDateCreation(null);

        // Create the Contenu, which fails.
        ContenuDTO contenuDTO = contenuMapper.toDto(contenu);

        restContenuMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(contenuDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllContenus() throws Exception {
        // Initialize the database
        insertedContenu = contenuRepository.saveAndFlush(contenu);

        // Get all the contenuList
        restContenuMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(contenu.getId().toString())))
            .andExpect(jsonPath("$.[*].titre").value(hasItem(DEFAULT_TITRE)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)))
            .andExpect(jsonPath("$.[*].typeContenu").value(hasItem(DEFAULT_TYPE_CONTENU.toString())))
            .andExpect(jsonPath("$.[*].statut").value(hasItem(DEFAULT_STATUT.toString())))
            .andExpect(jsonPath("$.[*].version").value(hasItem(DEFAULT_VERSION)))
            .andExpect(jsonPath("$.[*].datePublication").value(hasItem(DEFAULT_DATE_PUBLICATION.toString())))
            .andExpect(jsonPath("$.[*].derniereMiseAJour").value(hasItem(DEFAULT_DERNIERE_MISE_A_JOUR.toString())))
            .andExpect(jsonPath("$.[*].prochaineRevue").value(hasItem(DEFAULT_PROCHAINE_REVUE.toString())))
            .andExpect(jsonPath("$.[*].motsCles").value(hasItem(DEFAULT_MOTS_CLES)))
            .andExpect(jsonPath("$.[*].transcription").value(hasItem(DEFAULT_TRANSCRIPTION)))
            .andExpect(jsonPath("$.[*].progression").value(hasItem(DEFAULT_PROGRESSION)))
            .andExpect(jsonPath("$.[*].dateCreation").value(hasItem(DEFAULT_DATE_CREATION.toString())));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllContenusWithEagerRelationshipsIsEnabled() throws Exception {
        when(contenuServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restContenuMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(contenuServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllContenusWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(contenuServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restContenuMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(contenuRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getContenu() throws Exception {
        // Initialize the database
        insertedContenu = contenuRepository.saveAndFlush(contenu);

        // Get the contenu
        restContenuMockMvc
            .perform(get(ENTITY_API_URL_ID, contenu.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(contenu.getId().toString()))
            .andExpect(jsonPath("$.titre").value(DEFAULT_TITRE))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION))
            .andExpect(jsonPath("$.typeContenu").value(DEFAULT_TYPE_CONTENU.toString()))
            .andExpect(jsonPath("$.statut").value(DEFAULT_STATUT.toString()))
            .andExpect(jsonPath("$.version").value(DEFAULT_VERSION))
            .andExpect(jsonPath("$.datePublication").value(DEFAULT_DATE_PUBLICATION.toString()))
            .andExpect(jsonPath("$.derniereMiseAJour").value(DEFAULT_DERNIERE_MISE_A_JOUR.toString()))
            .andExpect(jsonPath("$.prochaineRevue").value(DEFAULT_PROCHAINE_REVUE.toString()))
            .andExpect(jsonPath("$.motsCles").value(DEFAULT_MOTS_CLES))
            .andExpect(jsonPath("$.transcription").value(DEFAULT_TRANSCRIPTION))
            .andExpect(jsonPath("$.progression").value(DEFAULT_PROGRESSION))
            .andExpect(jsonPath("$.dateCreation").value(DEFAULT_DATE_CREATION.toString()));
    }

    @Test
    @Transactional
    void getContenusByIdFiltering() throws Exception {
        // Initialize the database
        insertedContenu = contenuRepository.saveAndFlush(contenu);

        UUID id = contenu.getId();

        defaultContenuFiltering("id.equals=" + id, "id.notEquals=" + id);
    }

    @Test
    @Transactional
    void getAllContenusByTitreIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedContenu = contenuRepository.saveAndFlush(contenu);

        // Get all the contenuList where titre equals to
        defaultContenuFiltering("titre.equals=" + DEFAULT_TITRE, "titre.equals=" + UPDATED_TITRE);
    }

    @Test
    @Transactional
    void getAllContenusByTitreIsInShouldWork() throws Exception {
        // Initialize the database
        insertedContenu = contenuRepository.saveAndFlush(contenu);

        // Get all the contenuList where titre in
        defaultContenuFiltering("titre.in=" + DEFAULT_TITRE + "," + UPDATED_TITRE, "titre.in=" + UPDATED_TITRE);
    }

    @Test
    @Transactional
    void getAllContenusByTitreIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedContenu = contenuRepository.saveAndFlush(contenu);

        // Get all the contenuList where titre is not null
        defaultContenuFiltering("titre.specified=true", "titre.specified=false");
    }

    @Test
    @Transactional
    void getAllContenusByTitreContainsSomething() throws Exception {
        // Initialize the database
        insertedContenu = contenuRepository.saveAndFlush(contenu);

        // Get all the contenuList where titre contains
        defaultContenuFiltering("titre.contains=" + DEFAULT_TITRE, "titre.contains=" + UPDATED_TITRE);
    }

    @Test
    @Transactional
    void getAllContenusByTitreNotContainsSomething() throws Exception {
        // Initialize the database
        insertedContenu = contenuRepository.saveAndFlush(contenu);

        // Get all the contenuList where titre does not contain
        defaultContenuFiltering("titre.doesNotContain=" + UPDATED_TITRE, "titre.doesNotContain=" + DEFAULT_TITRE);
    }

    @Test
    @Transactional
    void getAllContenusByTypeContenuIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedContenu = contenuRepository.saveAndFlush(contenu);

        // Get all the contenuList where typeContenu equals to
        defaultContenuFiltering("typeContenu.equals=" + DEFAULT_TYPE_CONTENU, "typeContenu.equals=" + UPDATED_TYPE_CONTENU);
    }

    @Test
    @Transactional
    void getAllContenusByTypeContenuIsInShouldWork() throws Exception {
        // Initialize the database
        insertedContenu = contenuRepository.saveAndFlush(contenu);

        // Get all the contenuList where typeContenu in
        defaultContenuFiltering(
            "typeContenu.in=" + DEFAULT_TYPE_CONTENU + "," + UPDATED_TYPE_CONTENU,
            "typeContenu.in=" + UPDATED_TYPE_CONTENU
        );
    }

    @Test
    @Transactional
    void getAllContenusByTypeContenuIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedContenu = contenuRepository.saveAndFlush(contenu);

        // Get all the contenuList where typeContenu is not null
        defaultContenuFiltering("typeContenu.specified=true", "typeContenu.specified=false");
    }

    @Test
    @Transactional
    void getAllContenusByStatutIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedContenu = contenuRepository.saveAndFlush(contenu);

        // Get all the contenuList where statut equals to
        defaultContenuFiltering("statut.equals=" + DEFAULT_STATUT, "statut.equals=" + UPDATED_STATUT);
    }

    @Test
    @Transactional
    void getAllContenusByStatutIsInShouldWork() throws Exception {
        // Initialize the database
        insertedContenu = contenuRepository.saveAndFlush(contenu);

        // Get all the contenuList where statut in
        defaultContenuFiltering("statut.in=" + DEFAULT_STATUT + "," + UPDATED_STATUT, "statut.in=" + UPDATED_STATUT);
    }

    @Test
    @Transactional
    void getAllContenusByStatutIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedContenu = contenuRepository.saveAndFlush(contenu);

        // Get all the contenuList where statut is not null
        defaultContenuFiltering("statut.specified=true", "statut.specified=false");
    }

    @Test
    @Transactional
    void getAllContenusByVersionIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedContenu = contenuRepository.saveAndFlush(contenu);

        // Get all the contenuList where version equals to
        defaultContenuFiltering("version.equals=" + DEFAULT_VERSION, "version.equals=" + UPDATED_VERSION);
    }

    @Test
    @Transactional
    void getAllContenusByVersionIsInShouldWork() throws Exception {
        // Initialize the database
        insertedContenu = contenuRepository.saveAndFlush(contenu);

        // Get all the contenuList where version in
        defaultContenuFiltering("version.in=" + DEFAULT_VERSION + "," + UPDATED_VERSION, "version.in=" + UPDATED_VERSION);
    }

    @Test
    @Transactional
    void getAllContenusByVersionIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedContenu = contenuRepository.saveAndFlush(contenu);

        // Get all the contenuList where version is not null
        defaultContenuFiltering("version.specified=true", "version.specified=false");
    }

    @Test
    @Transactional
    void getAllContenusByVersionIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedContenu = contenuRepository.saveAndFlush(contenu);

        // Get all the contenuList where version is greater than or equal to
        defaultContenuFiltering("version.greaterThanOrEqual=" + DEFAULT_VERSION, "version.greaterThanOrEqual=" + UPDATED_VERSION);
    }

    @Test
    @Transactional
    void getAllContenusByVersionIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedContenu = contenuRepository.saveAndFlush(contenu);

        // Get all the contenuList where version is less than or equal to
        defaultContenuFiltering("version.lessThanOrEqual=" + DEFAULT_VERSION, "version.lessThanOrEqual=" + SMALLER_VERSION);
    }

    @Test
    @Transactional
    void getAllContenusByVersionIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedContenu = contenuRepository.saveAndFlush(contenu);

        // Get all the contenuList where version is less than
        defaultContenuFiltering("version.lessThan=" + UPDATED_VERSION, "version.lessThan=" + DEFAULT_VERSION);
    }

    @Test
    @Transactional
    void getAllContenusByVersionIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedContenu = contenuRepository.saveAndFlush(contenu);

        // Get all the contenuList where version is greater than
        defaultContenuFiltering("version.greaterThan=" + SMALLER_VERSION, "version.greaterThan=" + DEFAULT_VERSION);
    }

    @Test
    @Transactional
    void getAllContenusByDatePublicationIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedContenu = contenuRepository.saveAndFlush(contenu);

        // Get all the contenuList where datePublication equals to
        defaultContenuFiltering("datePublication.equals=" + DEFAULT_DATE_PUBLICATION, "datePublication.equals=" + UPDATED_DATE_PUBLICATION);
    }

    @Test
    @Transactional
    void getAllContenusByDatePublicationIsInShouldWork() throws Exception {
        // Initialize the database
        insertedContenu = contenuRepository.saveAndFlush(contenu);

        // Get all the contenuList where datePublication in
        defaultContenuFiltering(
            "datePublication.in=" + DEFAULT_DATE_PUBLICATION + "," + UPDATED_DATE_PUBLICATION,
            "datePublication.in=" + UPDATED_DATE_PUBLICATION
        );
    }

    @Test
    @Transactional
    void getAllContenusByDatePublicationIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedContenu = contenuRepository.saveAndFlush(contenu);

        // Get all the contenuList where datePublication is not null
        defaultContenuFiltering("datePublication.specified=true", "datePublication.specified=false");
    }

    @Test
    @Transactional
    void getAllContenusByDerniereMiseAJourIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedContenu = contenuRepository.saveAndFlush(contenu);

        // Get all the contenuList where derniereMiseAJour equals to
        defaultContenuFiltering(
            "derniereMiseAJour.equals=" + DEFAULT_DERNIERE_MISE_A_JOUR,
            "derniereMiseAJour.equals=" + UPDATED_DERNIERE_MISE_A_JOUR
        );
    }

    @Test
    @Transactional
    void getAllContenusByDerniereMiseAJourIsInShouldWork() throws Exception {
        // Initialize the database
        insertedContenu = contenuRepository.saveAndFlush(contenu);

        // Get all the contenuList where derniereMiseAJour in
        defaultContenuFiltering(
            "derniereMiseAJour.in=" + DEFAULT_DERNIERE_MISE_A_JOUR + "," + UPDATED_DERNIERE_MISE_A_JOUR,
            "derniereMiseAJour.in=" + UPDATED_DERNIERE_MISE_A_JOUR
        );
    }

    @Test
    @Transactional
    void getAllContenusByDerniereMiseAJourIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedContenu = contenuRepository.saveAndFlush(contenu);

        // Get all the contenuList where derniereMiseAJour is not null
        defaultContenuFiltering("derniereMiseAJour.specified=true", "derniereMiseAJour.specified=false");
    }

    @Test
    @Transactional
    void getAllContenusByProchaineRevueIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedContenu = contenuRepository.saveAndFlush(contenu);

        // Get all the contenuList where prochaineRevue equals to
        defaultContenuFiltering("prochaineRevue.equals=" + DEFAULT_PROCHAINE_REVUE, "prochaineRevue.equals=" + UPDATED_PROCHAINE_REVUE);
    }

    @Test
    @Transactional
    void getAllContenusByProchaineRevueIsInShouldWork() throws Exception {
        // Initialize the database
        insertedContenu = contenuRepository.saveAndFlush(contenu);

        // Get all the contenuList where prochaineRevue in
        defaultContenuFiltering(
            "prochaineRevue.in=" + DEFAULT_PROCHAINE_REVUE + "," + UPDATED_PROCHAINE_REVUE,
            "prochaineRevue.in=" + UPDATED_PROCHAINE_REVUE
        );
    }

    @Test
    @Transactional
    void getAllContenusByProchaineRevueIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedContenu = contenuRepository.saveAndFlush(contenu);

        // Get all the contenuList where prochaineRevue is not null
        defaultContenuFiltering("prochaineRevue.specified=true", "prochaineRevue.specified=false");
    }

    @Test
    @Transactional
    void getAllContenusByProgressionIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedContenu = contenuRepository.saveAndFlush(contenu);

        // Get all the contenuList where progression equals to
        defaultContenuFiltering("progression.equals=" + DEFAULT_PROGRESSION, "progression.equals=" + UPDATED_PROGRESSION);
    }

    @Test
    @Transactional
    void getAllContenusByProgressionIsInShouldWork() throws Exception {
        // Initialize the database
        insertedContenu = contenuRepository.saveAndFlush(contenu);

        // Get all the contenuList where progression in
        defaultContenuFiltering(
            "progression.in=" + DEFAULT_PROGRESSION + "," + UPDATED_PROGRESSION,
            "progression.in=" + UPDATED_PROGRESSION
        );
    }

    @Test
    @Transactional
    void getAllContenusByProgressionIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedContenu = contenuRepository.saveAndFlush(contenu);

        // Get all the contenuList where progression is not null
        defaultContenuFiltering("progression.specified=true", "progression.specified=false");
    }

    @Test
    @Transactional
    void getAllContenusByProgressionIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedContenu = contenuRepository.saveAndFlush(contenu);

        // Get all the contenuList where progression is greater than or equal to
        defaultContenuFiltering(
            "progression.greaterThanOrEqual=" + DEFAULT_PROGRESSION,
            "progression.greaterThanOrEqual=" + (DEFAULT_PROGRESSION + 1)
        );
    }

    @Test
    @Transactional
    void getAllContenusByProgressionIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedContenu = contenuRepository.saveAndFlush(contenu);

        // Get all the contenuList where progression is less than or equal to
        defaultContenuFiltering("progression.lessThanOrEqual=" + DEFAULT_PROGRESSION, "progression.lessThanOrEqual=" + SMALLER_PROGRESSION);
    }

    @Test
    @Transactional
    void getAllContenusByProgressionIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedContenu = contenuRepository.saveAndFlush(contenu);

        // Get all the contenuList where progression is less than
        defaultContenuFiltering("progression.lessThan=" + (DEFAULT_PROGRESSION + 1), "progression.lessThan=" + DEFAULT_PROGRESSION);
    }

    @Test
    @Transactional
    void getAllContenusByProgressionIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedContenu = contenuRepository.saveAndFlush(contenu);

        // Get all the contenuList where progression is greater than
        defaultContenuFiltering("progression.greaterThan=" + SMALLER_PROGRESSION, "progression.greaterThan=" + DEFAULT_PROGRESSION);
    }

    @Test
    @Transactional
    void getAllContenusByDateCreationIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedContenu = contenuRepository.saveAndFlush(contenu);

        // Get all the contenuList where dateCreation equals to
        defaultContenuFiltering("dateCreation.equals=" + DEFAULT_DATE_CREATION, "dateCreation.equals=" + UPDATED_DATE_CREATION);
    }

    @Test
    @Transactional
    void getAllContenusByDateCreationIsInShouldWork() throws Exception {
        // Initialize the database
        insertedContenu = contenuRepository.saveAndFlush(contenu);

        // Get all the contenuList where dateCreation in
        defaultContenuFiltering(
            "dateCreation.in=" + DEFAULT_DATE_CREATION + "," + UPDATED_DATE_CREATION,
            "dateCreation.in=" + UPDATED_DATE_CREATION
        );
    }

    @Test
    @Transactional
    void getAllContenusByDateCreationIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedContenu = contenuRepository.saveAndFlush(contenu);

        // Get all the contenuList where dateCreation is not null
        defaultContenuFiltering("dateCreation.specified=true", "dateCreation.specified=false");
    }

    @Test
    @Transactional
    void getAllContenusByApplicationIsEqualToSomething() throws Exception {
        Application application;
        if (TestUtil.findAll(em, Application.class).isEmpty()) {
            contenuRepository.saveAndFlush(contenu);
            application = ApplicationResourceIT.createEntity();
        } else {
            application = TestUtil.findAll(em, Application.class).get(0);
        }
        em.persist(application);
        em.flush();
        contenu.setApplication(application);
        contenuRepository.saveAndFlush(contenu);
        UUID applicationId = application.getId();
        // Get all the contenuList where application equals to applicationId
        defaultContenuShouldBeFound("applicationId.equals=" + applicationId);

        // Get all the contenuList where application equals to UUID.randomUUID()
        defaultContenuShouldNotBeFound("applicationId.equals=" + UUID.randomUUID());
    }

    @Test
    @Transactional
    void getAllContenusByModuleIsEqualToSomething() throws Exception {
        Module module;
        if (TestUtil.findAll(em, Module.class).isEmpty()) {
            contenuRepository.saveAndFlush(contenu);
            module = ModuleResourceIT.createEntity(em);
        } else {
            module = TestUtil.findAll(em, Module.class).get(0);
        }
        em.persist(module);
        em.flush();
        contenu.setModule(module);
        contenuRepository.saveAndFlush(contenu);
        UUID moduleId = module.getId();
        // Get all the contenuList where module equals to moduleId
        defaultContenuShouldBeFound("moduleId.equals=" + moduleId);

        // Get all the contenuList where module equals to UUID.randomUUID()
        defaultContenuShouldNotBeFound("moduleId.equals=" + UUID.randomUUID());
    }

    @Test
    @Transactional
    void getAllContenusByMetierIsEqualToSomething() throws Exception {
        Metier metier;
        if (TestUtil.findAll(em, Metier.class).isEmpty()) {
            contenuRepository.saveAndFlush(contenu);
            metier = MetierResourceIT.createEntity();
        } else {
            metier = TestUtil.findAll(em, Metier.class).get(0);
        }
        em.persist(metier);
        em.flush();
        contenu.setMetier(metier);
        contenuRepository.saveAndFlush(contenu);
        UUID metierId = metier.getId();
        // Get all the contenuList where metier equals to metierId
        defaultContenuShouldBeFound("metierId.equals=" + metierId);

        // Get all the contenuList where metier equals to UUID.randomUUID()
        defaultContenuShouldNotBeFound("metierId.equals=" + UUID.randomUUID());
    }

    @Test
    @Transactional
    void getAllContenusByAuteurIsEqualToSomething() throws Exception {
        Utilisateur auteur;
        if (TestUtil.findAll(em, Utilisateur.class).isEmpty()) {
            contenuRepository.saveAndFlush(contenu);
            auteur = UtilisateurResourceIT.createEntity();
        } else {
            auteur = TestUtil.findAll(em, Utilisateur.class).get(0);
        }
        em.persist(auteur);
        em.flush();
        contenu.setAuteur(auteur);
        contenuRepository.saveAndFlush(contenu);
        UUID auteurId = auteur.getId();
        // Get all the contenuList where auteur equals to auteurId
        defaultContenuShouldBeFound("auteurId.equals=" + auteurId);

        // Get all the contenuList where auteur equals to UUID.randomUUID()
        defaultContenuShouldNotBeFound("auteurId.equals=" + UUID.randomUUID());
    }

    @Test
    @Transactional
    void getAllContenusByValidateurIsEqualToSomething() throws Exception {
        Utilisateur validateur;
        if (TestUtil.findAll(em, Utilisateur.class).isEmpty()) {
            contenuRepository.saveAndFlush(contenu);
            validateur = UtilisateurResourceIT.createEntity();
        } else {
            validateur = TestUtil.findAll(em, Utilisateur.class).get(0);
        }
        em.persist(validateur);
        em.flush();
        contenu.setValidateur(validateur);
        contenuRepository.saveAndFlush(contenu);
        UUID validateurId = validateur.getId();
        // Get all the contenuList where validateur equals to validateurId
        defaultContenuShouldBeFound("validateurId.equals=" + validateurId);

        // Get all the contenuList where validateur equals to UUID.randomUUID()
        defaultContenuShouldNotBeFound("validateurId.equals=" + UUID.randomUUID());
    }

    private void defaultContenuFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultContenuShouldBeFound(shouldBeFound);
        defaultContenuShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultContenuShouldBeFound(String filter) throws Exception {
        restContenuMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(contenu.getId().toString())))
            .andExpect(jsonPath("$.[*].titre").value(hasItem(DEFAULT_TITRE)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)))
            .andExpect(jsonPath("$.[*].typeContenu").value(hasItem(DEFAULT_TYPE_CONTENU.toString())))
            .andExpect(jsonPath("$.[*].statut").value(hasItem(DEFAULT_STATUT.toString())))
            .andExpect(jsonPath("$.[*].version").value(hasItem(DEFAULT_VERSION)))
            .andExpect(jsonPath("$.[*].datePublication").value(hasItem(DEFAULT_DATE_PUBLICATION.toString())))
            .andExpect(jsonPath("$.[*].derniereMiseAJour").value(hasItem(DEFAULT_DERNIERE_MISE_A_JOUR.toString())))
            .andExpect(jsonPath("$.[*].prochaineRevue").value(hasItem(DEFAULT_PROCHAINE_REVUE.toString())))
            .andExpect(jsonPath("$.[*].motsCles").value(hasItem(DEFAULT_MOTS_CLES)))
            .andExpect(jsonPath("$.[*].transcription").value(hasItem(DEFAULT_TRANSCRIPTION)))
            .andExpect(jsonPath("$.[*].progression").value(hasItem(DEFAULT_PROGRESSION)))
            .andExpect(jsonPath("$.[*].dateCreation").value(hasItem(DEFAULT_DATE_CREATION.toString())));

        // Check, that the count call also returns 1
        restContenuMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultContenuShouldNotBeFound(String filter) throws Exception {
        restContenuMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restContenuMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingContenu() throws Exception {
        // Get the contenu
        restContenuMockMvc.perform(get(ENTITY_API_URL_ID, UUID.randomUUID().toString())).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingContenu() throws Exception {
        // Initialize the database
        insertedContenu = contenuRepository.saveAndFlush(contenu);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the contenu
        Contenu updatedContenu = contenuRepository.findById(contenu.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedContenu are not directly saved in db
        em.detach(updatedContenu);
        updatedContenu
            .titre(UPDATED_TITRE)
            .description(UPDATED_DESCRIPTION)
            .typeContenu(UPDATED_TYPE_CONTENU)
            .statut(UPDATED_STATUT)
            .version(UPDATED_VERSION)
            .datePublication(UPDATED_DATE_PUBLICATION)
            .derniereMiseAJour(UPDATED_DERNIERE_MISE_A_JOUR)
            .prochaineRevue(UPDATED_PROCHAINE_REVUE)
            .motsCles(UPDATED_MOTS_CLES)
            .transcription(UPDATED_TRANSCRIPTION)
            .progression(UPDATED_PROGRESSION)
            .dateCreation(UPDATED_DATE_CREATION);
        ContenuDTO contenuDTO = contenuMapper.toDto(updatedContenu);

        restContenuMockMvc
            .perform(
                put(ENTITY_API_URL_ID, contenuDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(contenuDTO))
            )
            .andExpect(status().isOk());

        // Validate the Contenu in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedContenuToMatchAllProperties(updatedContenu);
    }

    @Test
    @Transactional
    void putNonExistingContenu() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        contenu.setId(UUID.randomUUID());

        // Create the Contenu
        ContenuDTO contenuDTO = contenuMapper.toDto(contenu);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restContenuMockMvc
            .perform(
                put(ENTITY_API_URL_ID, contenuDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(contenuDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Contenu in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchContenu() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        contenu.setId(UUID.randomUUID());

        // Create the Contenu
        ContenuDTO contenuDTO = contenuMapper.toDto(contenu);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restContenuMockMvc
            .perform(
                put(ENTITY_API_URL_ID, UUID.randomUUID())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(contenuDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Contenu in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamContenu() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        contenu.setId(UUID.randomUUID());

        // Create the Contenu
        ContenuDTO contenuDTO = contenuMapper.toDto(contenu);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restContenuMockMvc
            .perform(put(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(contenuDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Contenu in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateContenuWithPatch() throws Exception {
        // Initialize the database
        insertedContenu = contenuRepository.saveAndFlush(contenu);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the contenu using partial update
        Contenu partialUpdatedContenu = new Contenu();
        partialUpdatedContenu.setId(contenu.getId());

        partialUpdatedContenu
            .titre(UPDATED_TITRE)
            .description(UPDATED_DESCRIPTION)
            .version(UPDATED_VERSION)
            .datePublication(UPDATED_DATE_PUBLICATION)
            .prochaineRevue(UPDATED_PROCHAINE_REVUE)
            .transcription(UPDATED_TRANSCRIPTION)
            .progression(UPDATED_PROGRESSION);

        restContenuMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedContenu.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedContenu))
            )
            .andExpect(status().isOk());

        // Validate the Contenu in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertContenuUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedContenu, contenu), getPersistedContenu(contenu));
    }

    @Test
    @Transactional
    void fullUpdateContenuWithPatch() throws Exception {
        // Initialize the database
        insertedContenu = contenuRepository.saveAndFlush(contenu);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the contenu using partial update
        Contenu partialUpdatedContenu = new Contenu();
        partialUpdatedContenu.setId(contenu.getId());

        partialUpdatedContenu
            .titre(UPDATED_TITRE)
            .description(UPDATED_DESCRIPTION)
            .typeContenu(UPDATED_TYPE_CONTENU)
            .statut(UPDATED_STATUT)
            .version(UPDATED_VERSION)
            .datePublication(UPDATED_DATE_PUBLICATION)
            .derniereMiseAJour(UPDATED_DERNIERE_MISE_A_JOUR)
            .prochaineRevue(UPDATED_PROCHAINE_REVUE)
            .motsCles(UPDATED_MOTS_CLES)
            .transcription(UPDATED_TRANSCRIPTION)
            .progression(UPDATED_PROGRESSION)
            .dateCreation(UPDATED_DATE_CREATION);

        restContenuMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedContenu.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedContenu))
            )
            .andExpect(status().isOk());

        // Validate the Contenu in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertContenuUpdatableFieldsEquals(partialUpdatedContenu, getPersistedContenu(partialUpdatedContenu));
    }

    @Test
    @Transactional
    void patchNonExistingContenu() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        contenu.setId(UUID.randomUUID());

        // Create the Contenu
        ContenuDTO contenuDTO = contenuMapper.toDto(contenu);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restContenuMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, contenuDTO.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(contenuDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Contenu in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchContenu() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        contenu.setId(UUID.randomUUID());

        // Create the Contenu
        ContenuDTO contenuDTO = contenuMapper.toDto(contenu);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restContenuMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, UUID.randomUUID())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(contenuDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Contenu in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamContenu() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        contenu.setId(UUID.randomUUID());

        // Create the Contenu
        ContenuDTO contenuDTO = contenuMapper.toDto(contenu);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restContenuMockMvc
            .perform(
                patch(ENTITY_API_URL).with(csrf()).contentType("application/merge-patch+json").content(om.writeValueAsBytes(contenuDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the Contenu in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteContenu() throws Exception {
        // Initialize the database
        insertedContenu = contenuRepository.saveAndFlush(contenu);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the contenu
        restContenuMockMvc
            .perform(delete(ENTITY_API_URL_ID, contenu.getId().toString()).with(csrf()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return contenuRepository.count();
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

    protected Contenu getPersistedContenu(Contenu contenu) {
        return contenuRepository.findById(contenu.getId()).orElseThrow();
    }

    protected void assertPersistedContenuToMatchAllProperties(Contenu expectedContenu) {
        assertContenuAllPropertiesEquals(expectedContenu, getPersistedContenu(expectedContenu));
    }

    protected void assertPersistedContenuToMatchUpdatableProperties(Contenu expectedContenu) {
        assertContenuAllUpdatablePropertiesEquals(expectedContenu, getPersistedContenu(expectedContenu));
    }
}
