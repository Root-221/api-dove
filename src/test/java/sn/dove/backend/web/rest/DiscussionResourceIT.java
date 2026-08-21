package sn.dove.backend.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static sn.dove.backend.domain.DiscussionAsserts.*;
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
import sn.dove.backend.domain.CommunauteNandite;
import sn.dove.backend.domain.Discussion;
import sn.dove.backend.domain.Utilisateur;
import sn.dove.backend.repository.DiscussionRepository;
import sn.dove.backend.service.DiscussionService;
import sn.dove.backend.service.dto.DiscussionDTO;
import sn.dove.backend.service.mapper.DiscussionMapper;

/**
 * Integration tests for the {@link DiscussionResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class DiscussionResourceIT {

    private static final String DEFAULT_TITRE = "AAAAAAAAAA";
    private static final String UPDATED_TITRE = "BBBBBBBBBB";

    private static final Instant DEFAULT_DATE_CREATION = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_DATE_CREATION = Instant.ofEpochMilli(1787238072967L);

    private static final String ENTITY_API_URL = "/api/discussions";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    private ObjectMapper om;

    @Autowired
    private DiscussionRepository discussionRepository;

    @Mock
    private DiscussionRepository discussionRepositoryMock;

    @Autowired
    private DiscussionMapper discussionMapper;

    @Mock
    private DiscussionService discussionServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restDiscussionMockMvc;

    private Discussion discussion;

    private Discussion insertedDiscussion;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Discussion createEntity(EntityManager em) {
        Discussion discussion = new Discussion().titre(DEFAULT_TITRE).dateCreation(DEFAULT_DATE_CREATION);
        // Add required entity
        Utilisateur utilisateur;
        if (TestUtil.findAll(em, Utilisateur.class).isEmpty()) {
            utilisateur = UtilisateurResourceIT.createEntity();
            em.persist(utilisateur);
            em.flush();
        } else {
            utilisateur = TestUtil.findAll(em, Utilisateur.class).get(0);
        }
        discussion.setCreateur(utilisateur);
        // Add required entity
        CommunauteNandite communauteNandite;
        if (TestUtil.findAll(em, CommunauteNandite.class).isEmpty()) {
            communauteNandite = CommunauteNanditeResourceIT.createEntity();
            em.persist(communauteNandite);
            em.flush();
        } else {
            communauteNandite = TestUtil.findAll(em, CommunauteNandite.class).get(0);
        }
        discussion.setCommunauteNandite(communauteNandite);
        return discussion;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Discussion createUpdatedEntity(EntityManager em) {
        Discussion updatedDiscussion = new Discussion().titre(UPDATED_TITRE).dateCreation(UPDATED_DATE_CREATION);
        // Add required entity
        Utilisateur utilisateur;
        if (TestUtil.findAll(em, Utilisateur.class).isEmpty()) {
            utilisateur = UtilisateurResourceIT.createUpdatedEntity();
            em.persist(utilisateur);
            em.flush();
        } else {
            utilisateur = TestUtil.findAll(em, Utilisateur.class).get(0);
        }
        updatedDiscussion.setCreateur(utilisateur);
        // Add required entity
        CommunauteNandite communauteNandite;
        if (TestUtil.findAll(em, CommunauteNandite.class).isEmpty()) {
            communauteNandite = CommunauteNanditeResourceIT.createUpdatedEntity();
            em.persist(communauteNandite);
            em.flush();
        } else {
            communauteNandite = TestUtil.findAll(em, CommunauteNandite.class).get(0);
        }
        updatedDiscussion.setCommunauteNandite(communauteNandite);
        return updatedDiscussion;
    }

    @BeforeEach
    void initTest() {
        discussion = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedDiscussion != null) {
            discussionRepository.delete(insertedDiscussion);
            insertedDiscussion = null;
        }
    }

    @Test
    @Transactional
    void createDiscussion() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Discussion
        DiscussionDTO discussionDTO = discussionMapper.toDto(discussion);
        var returnedDiscussionDTO = om.readValue(
            restDiscussionMockMvc
                .perform(
                    post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(discussionDTO))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            DiscussionDTO.class
        );

        // Validate the Discussion in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedDiscussion = discussionMapper.toEntity(returnedDiscussionDTO);
        assertDiscussionUpdatableFieldsEquals(returnedDiscussion, getPersistedDiscussion(returnedDiscussion));

        insertedDiscussion = returnedDiscussion;
    }

    @Test
    @Transactional
    void createDiscussionWithExistingId() throws Exception {
        // Create the Discussion with an existing ID
        insertedDiscussion = discussionRepository.saveAndFlush(discussion);
        DiscussionDTO discussionDTO = discussionMapper.toDto(discussion);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restDiscussionMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(discussionDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Discussion in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkTitreIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        discussion.setTitre(null);

        // Create the Discussion, which fails.
        DiscussionDTO discussionDTO = discussionMapper.toDto(discussion);

        restDiscussionMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(discussionDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkDateCreationIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        discussion.setDateCreation(null);

        // Create the Discussion, which fails.
        DiscussionDTO discussionDTO = discussionMapper.toDto(discussion);

        restDiscussionMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(discussionDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllDiscussions() throws Exception {
        // Initialize the database
        insertedDiscussion = discussionRepository.saveAndFlush(discussion);

        // Get all the discussionList
        restDiscussionMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(discussion.getId().toString())))
            .andExpect(jsonPath("$.[*].titre").value(hasItem(DEFAULT_TITRE)))
            .andExpect(jsonPath("$.[*].dateCreation").value(hasItem(DEFAULT_DATE_CREATION.toString())));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllDiscussionsWithEagerRelationshipsIsEnabled() throws Exception {
        when(discussionServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restDiscussionMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(discussionServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllDiscussionsWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(discussionServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restDiscussionMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(discussionRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getDiscussion() throws Exception {
        // Initialize the database
        insertedDiscussion = discussionRepository.saveAndFlush(discussion);

        // Get the discussion
        restDiscussionMockMvc
            .perform(get(ENTITY_API_URL_ID, discussion.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(discussion.getId().toString()))
            .andExpect(jsonPath("$.titre").value(DEFAULT_TITRE))
            .andExpect(jsonPath("$.dateCreation").value(DEFAULT_DATE_CREATION.toString()));
    }

    @Test
    @Transactional
    void getDiscussionsByIdFiltering() throws Exception {
        // Initialize the database
        insertedDiscussion = discussionRepository.saveAndFlush(discussion);

        UUID id = discussion.getId();

        defaultDiscussionFiltering("id.equals=" + id, "id.notEquals=" + id);
    }

    @Test
    @Transactional
    void getAllDiscussionsByTitreIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedDiscussion = discussionRepository.saveAndFlush(discussion);

        // Get all the discussionList where titre equals to
        defaultDiscussionFiltering("titre.equals=" + DEFAULT_TITRE, "titre.equals=" + UPDATED_TITRE);
    }

    @Test
    @Transactional
    void getAllDiscussionsByTitreIsInShouldWork() throws Exception {
        // Initialize the database
        insertedDiscussion = discussionRepository.saveAndFlush(discussion);

        // Get all the discussionList where titre in
        defaultDiscussionFiltering("titre.in=" + DEFAULT_TITRE + "," + UPDATED_TITRE, "titre.in=" + UPDATED_TITRE);
    }

    @Test
    @Transactional
    void getAllDiscussionsByTitreIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedDiscussion = discussionRepository.saveAndFlush(discussion);

        // Get all the discussionList where titre is not null
        defaultDiscussionFiltering("titre.specified=true", "titre.specified=false");
    }

    @Test
    @Transactional
    void getAllDiscussionsByTitreContainsSomething() throws Exception {
        // Initialize the database
        insertedDiscussion = discussionRepository.saveAndFlush(discussion);

        // Get all the discussionList where titre contains
        defaultDiscussionFiltering("titre.contains=" + DEFAULT_TITRE, "titre.contains=" + UPDATED_TITRE);
    }

    @Test
    @Transactional
    void getAllDiscussionsByTitreNotContainsSomething() throws Exception {
        // Initialize the database
        insertedDiscussion = discussionRepository.saveAndFlush(discussion);

        // Get all the discussionList where titre does not contain
        defaultDiscussionFiltering("titre.doesNotContain=" + UPDATED_TITRE, "titre.doesNotContain=" + DEFAULT_TITRE);
    }

    @Test
    @Transactional
    void getAllDiscussionsByDateCreationIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedDiscussion = discussionRepository.saveAndFlush(discussion);

        // Get all the discussionList where dateCreation equals to
        defaultDiscussionFiltering("dateCreation.equals=" + DEFAULT_DATE_CREATION, "dateCreation.equals=" + UPDATED_DATE_CREATION);
    }

    @Test
    @Transactional
    void getAllDiscussionsByDateCreationIsInShouldWork() throws Exception {
        // Initialize the database
        insertedDiscussion = discussionRepository.saveAndFlush(discussion);

        // Get all the discussionList where dateCreation in
        defaultDiscussionFiltering(
            "dateCreation.in=" + DEFAULT_DATE_CREATION + "," + UPDATED_DATE_CREATION,
            "dateCreation.in=" + UPDATED_DATE_CREATION
        );
    }

    @Test
    @Transactional
    void getAllDiscussionsByDateCreationIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedDiscussion = discussionRepository.saveAndFlush(discussion);

        // Get all the discussionList where dateCreation is not null
        defaultDiscussionFiltering("dateCreation.specified=true", "dateCreation.specified=false");
    }

    @Test
    @Transactional
    void getAllDiscussionsByCreateurIsEqualToSomething() throws Exception {
        Utilisateur createur;
        if (TestUtil.findAll(em, Utilisateur.class).isEmpty()) {
            discussionRepository.saveAndFlush(discussion);
            createur = UtilisateurResourceIT.createEntity();
        } else {
            createur = TestUtil.findAll(em, Utilisateur.class).get(0);
        }
        em.persist(createur);
        em.flush();
        discussion.setCreateur(createur);
        discussionRepository.saveAndFlush(discussion);
        UUID createurId = createur.getId();
        // Get all the discussionList where createur equals to createurId
        defaultDiscussionShouldBeFound("createurId.equals=" + createurId);

        // Get all the discussionList where createur equals to UUID.randomUUID()
        defaultDiscussionShouldNotBeFound("createurId.equals=" + UUID.randomUUID());
    }

    @Test
    @Transactional
    void getAllDiscussionsByCommunauteNanditeIsEqualToSomething() throws Exception {
        CommunauteNandite communauteNandite;
        if (TestUtil.findAll(em, CommunauteNandite.class).isEmpty()) {
            discussionRepository.saveAndFlush(discussion);
            communauteNandite = CommunauteNanditeResourceIT.createEntity();
        } else {
            communauteNandite = TestUtil.findAll(em, CommunauteNandite.class).get(0);
        }
        em.persist(communauteNandite);
        em.flush();
        discussion.setCommunauteNandite(communauteNandite);
        discussionRepository.saveAndFlush(discussion);
        UUID communauteNanditeId = communauteNandite.getId();
        // Get all the discussionList where communauteNandite equals to communauteNanditeId
        defaultDiscussionShouldBeFound("communauteNanditeId.equals=" + communauteNanditeId);

        // Get all the discussionList where communauteNandite equals to UUID.randomUUID()
        defaultDiscussionShouldNotBeFound("communauteNanditeId.equals=" + UUID.randomUUID());
    }

    private void defaultDiscussionFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultDiscussionShouldBeFound(shouldBeFound);
        defaultDiscussionShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultDiscussionShouldBeFound(String filter) throws Exception {
        restDiscussionMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(discussion.getId().toString())))
            .andExpect(jsonPath("$.[*].titre").value(hasItem(DEFAULT_TITRE)))
            .andExpect(jsonPath("$.[*].dateCreation").value(hasItem(DEFAULT_DATE_CREATION.toString())));

        // Check, that the count call also returns 1
        restDiscussionMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultDiscussionShouldNotBeFound(String filter) throws Exception {
        restDiscussionMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restDiscussionMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingDiscussion() throws Exception {
        // Get the discussion
        restDiscussionMockMvc.perform(get(ENTITY_API_URL_ID, UUID.randomUUID().toString())).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingDiscussion() throws Exception {
        // Initialize the database
        insertedDiscussion = discussionRepository.saveAndFlush(discussion);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the discussion
        Discussion updatedDiscussion = discussionRepository.findById(discussion.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedDiscussion are not directly saved in db
        em.detach(updatedDiscussion);
        updatedDiscussion.titre(UPDATED_TITRE).dateCreation(UPDATED_DATE_CREATION);
        DiscussionDTO discussionDTO = discussionMapper.toDto(updatedDiscussion);

        restDiscussionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, discussionDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(discussionDTO))
            )
            .andExpect(status().isOk());

        // Validate the Discussion in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedDiscussionToMatchAllProperties(updatedDiscussion);
    }

    @Test
    @Transactional
    void putNonExistingDiscussion() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        discussion.setId(UUID.randomUUID());

        // Create the Discussion
        DiscussionDTO discussionDTO = discussionMapper.toDto(discussion);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restDiscussionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, discussionDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(discussionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Discussion in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchDiscussion() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        discussion.setId(UUID.randomUUID());

        // Create the Discussion
        DiscussionDTO discussionDTO = discussionMapper.toDto(discussion);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDiscussionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, UUID.randomUUID())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(discussionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Discussion in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamDiscussion() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        discussion.setId(UUID.randomUUID());

        // Create the Discussion
        DiscussionDTO discussionDTO = discussionMapper.toDto(discussion);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDiscussionMockMvc
            .perform(put(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(discussionDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Discussion in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateDiscussionWithPatch() throws Exception {
        // Initialize the database
        insertedDiscussion = discussionRepository.saveAndFlush(discussion);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the discussion using partial update
        Discussion partialUpdatedDiscussion = new Discussion();
        partialUpdatedDiscussion.setId(discussion.getId());

        restDiscussionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedDiscussion.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedDiscussion))
            )
            .andExpect(status().isOk());

        // Validate the Discussion in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertDiscussionUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedDiscussion, discussion),
            getPersistedDiscussion(discussion)
        );
    }

    @Test
    @Transactional
    void fullUpdateDiscussionWithPatch() throws Exception {
        // Initialize the database
        insertedDiscussion = discussionRepository.saveAndFlush(discussion);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the discussion using partial update
        Discussion partialUpdatedDiscussion = new Discussion();
        partialUpdatedDiscussion.setId(discussion.getId());

        partialUpdatedDiscussion.titre(UPDATED_TITRE).dateCreation(UPDATED_DATE_CREATION);

        restDiscussionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedDiscussion.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedDiscussion))
            )
            .andExpect(status().isOk());

        // Validate the Discussion in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertDiscussionUpdatableFieldsEquals(partialUpdatedDiscussion, getPersistedDiscussion(partialUpdatedDiscussion));
    }

    @Test
    @Transactional
    void patchNonExistingDiscussion() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        discussion.setId(UUID.randomUUID());

        // Create the Discussion
        DiscussionDTO discussionDTO = discussionMapper.toDto(discussion);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restDiscussionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, discussionDTO.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(discussionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Discussion in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchDiscussion() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        discussion.setId(UUID.randomUUID());

        // Create the Discussion
        DiscussionDTO discussionDTO = discussionMapper.toDto(discussion);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDiscussionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, UUID.randomUUID())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(discussionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Discussion in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamDiscussion() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        discussion.setId(UUID.randomUUID());

        // Create the Discussion
        DiscussionDTO discussionDTO = discussionMapper.toDto(discussion);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDiscussionMockMvc
            .perform(
                patch(ENTITY_API_URL).with(csrf()).contentType("application/merge-patch+json").content(om.writeValueAsBytes(discussionDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the Discussion in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteDiscussion() throws Exception {
        // Initialize the database
        insertedDiscussion = discussionRepository.saveAndFlush(discussion);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the discussion
        restDiscussionMockMvc
            .perform(delete(ENTITY_API_URL_ID, discussion.getId().toString()).with(csrf()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return discussionRepository.count();
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

    protected Discussion getPersistedDiscussion(Discussion discussion) {
        return discussionRepository.findById(discussion.getId()).orElseThrow();
    }

    protected void assertPersistedDiscussionToMatchAllProperties(Discussion expectedDiscussion) {
        assertDiscussionAllPropertiesEquals(expectedDiscussion, getPersistedDiscussion(expectedDiscussion));
    }

    protected void assertPersistedDiscussionToMatchUpdatableProperties(Discussion expectedDiscussion) {
        assertDiscussionAllUpdatablePropertiesEquals(expectedDiscussion, getPersistedDiscussion(expectedDiscussion));
    }
}
