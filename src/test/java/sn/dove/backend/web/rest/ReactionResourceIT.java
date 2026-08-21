package sn.dove.backend.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static sn.dove.backend.domain.ReactionAsserts.*;
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
import sn.dove.backend.domain.Message;
import sn.dove.backend.domain.Reaction;
import sn.dove.backend.domain.Utilisateur;
import sn.dove.backend.repository.ReactionRepository;
import sn.dove.backend.service.ReactionService;
import sn.dove.backend.service.dto.ReactionDTO;
import sn.dove.backend.service.mapper.ReactionMapper;

/**
 * Integration tests for the {@link ReactionResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class ReactionResourceIT {

    private static final String DEFAULT_TYPE = "AAAAAAAAAA";
    private static final String UPDATED_TYPE = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/reactions";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    private ObjectMapper om;

    @Autowired
    private ReactionRepository reactionRepository;

    @Mock
    private ReactionRepository reactionRepositoryMock;

    @Autowired
    private ReactionMapper reactionMapper;

    @Mock
    private ReactionService reactionServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restReactionMockMvc;

    private Reaction reaction;

    private Reaction insertedReaction;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Reaction createEntity(EntityManager em) {
        Reaction reaction = new Reaction().type(DEFAULT_TYPE);
        // Add required entity
        Utilisateur utilisateur;
        if (TestUtil.findAll(em, Utilisateur.class).isEmpty()) {
            utilisateur = UtilisateurResourceIT.createEntity();
            em.persist(utilisateur);
            em.flush();
        } else {
            utilisateur = TestUtil.findAll(em, Utilisateur.class).get(0);
        }
        reaction.setUtilisateur(utilisateur);
        // Add required entity
        Message message;
        if (TestUtil.findAll(em, Message.class).isEmpty()) {
            message = MessageResourceIT.createEntity(em);
            em.persist(message);
            em.flush();
        } else {
            message = TestUtil.findAll(em, Message.class).get(0);
        }
        reaction.setMessage(message);
        return reaction;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Reaction createUpdatedEntity(EntityManager em) {
        Reaction updatedReaction = new Reaction().type(UPDATED_TYPE);
        // Add required entity
        Utilisateur utilisateur;
        if (TestUtil.findAll(em, Utilisateur.class).isEmpty()) {
            utilisateur = UtilisateurResourceIT.createUpdatedEntity();
            em.persist(utilisateur);
            em.flush();
        } else {
            utilisateur = TestUtil.findAll(em, Utilisateur.class).get(0);
        }
        updatedReaction.setUtilisateur(utilisateur);
        // Add required entity
        Message message;
        if (TestUtil.findAll(em, Message.class).isEmpty()) {
            message = MessageResourceIT.createUpdatedEntity(em);
            em.persist(message);
            em.flush();
        } else {
            message = TestUtil.findAll(em, Message.class).get(0);
        }
        updatedReaction.setMessage(message);
        return updatedReaction;
    }

    @BeforeEach
    void initTest() {
        reaction = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedReaction != null) {
            reactionRepository.delete(insertedReaction);
            insertedReaction = null;
        }
    }

    @Test
    @Transactional
    void createReaction() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Reaction
        ReactionDTO reactionDTO = reactionMapper.toDto(reaction);
        var returnedReactionDTO = om.readValue(
            restReactionMockMvc
                .perform(
                    post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(reactionDTO))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            ReactionDTO.class
        );

        // Validate the Reaction in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedReaction = reactionMapper.toEntity(returnedReactionDTO);
        assertReactionUpdatableFieldsEquals(returnedReaction, getPersistedReaction(returnedReaction));

        insertedReaction = returnedReaction;
    }

    @Test
    @Transactional
    void createReactionWithExistingId() throws Exception {
        // Create the Reaction with an existing ID
        insertedReaction = reactionRepository.saveAndFlush(reaction);
        ReactionDTO reactionDTO = reactionMapper.toDto(reaction);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restReactionMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(reactionDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Reaction in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkTypeIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        reaction.setType(null);

        // Create the Reaction, which fails.
        ReactionDTO reactionDTO = reactionMapper.toDto(reaction);

        restReactionMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(reactionDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllReactions() throws Exception {
        // Initialize the database
        insertedReaction = reactionRepository.saveAndFlush(reaction);

        // Get all the reactionList
        restReactionMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(reaction.getId().toString())))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE)));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllReactionsWithEagerRelationshipsIsEnabled() throws Exception {
        when(reactionServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restReactionMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(reactionServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllReactionsWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(reactionServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restReactionMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(reactionRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getReaction() throws Exception {
        // Initialize the database
        insertedReaction = reactionRepository.saveAndFlush(reaction);

        // Get the reaction
        restReactionMockMvc
            .perform(get(ENTITY_API_URL_ID, reaction.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(reaction.getId().toString()))
            .andExpect(jsonPath("$.type").value(DEFAULT_TYPE));
    }

    @Test
    @Transactional
    void getReactionsByIdFiltering() throws Exception {
        // Initialize the database
        insertedReaction = reactionRepository.saveAndFlush(reaction);

        UUID id = reaction.getId();

        defaultReactionFiltering("id.equals=" + id, "id.notEquals=" + id);
    }

    @Test
    @Transactional
    void getAllReactionsByTypeIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedReaction = reactionRepository.saveAndFlush(reaction);

        // Get all the reactionList where type equals to
        defaultReactionFiltering("type.equals=" + DEFAULT_TYPE, "type.equals=" + UPDATED_TYPE);
    }

    @Test
    @Transactional
    void getAllReactionsByTypeIsInShouldWork() throws Exception {
        // Initialize the database
        insertedReaction = reactionRepository.saveAndFlush(reaction);

        // Get all the reactionList where type in
        defaultReactionFiltering("type.in=" + DEFAULT_TYPE + "," + UPDATED_TYPE, "type.in=" + UPDATED_TYPE);
    }

    @Test
    @Transactional
    void getAllReactionsByTypeIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedReaction = reactionRepository.saveAndFlush(reaction);

        // Get all the reactionList where type is not null
        defaultReactionFiltering("type.specified=true", "type.specified=false");
    }

    @Test
    @Transactional
    void getAllReactionsByTypeContainsSomething() throws Exception {
        // Initialize the database
        insertedReaction = reactionRepository.saveAndFlush(reaction);

        // Get all the reactionList where type contains
        defaultReactionFiltering("type.contains=" + DEFAULT_TYPE, "type.contains=" + UPDATED_TYPE);
    }

    @Test
    @Transactional
    void getAllReactionsByTypeNotContainsSomething() throws Exception {
        // Initialize the database
        insertedReaction = reactionRepository.saveAndFlush(reaction);

        // Get all the reactionList where type does not contain
        defaultReactionFiltering("type.doesNotContain=" + UPDATED_TYPE, "type.doesNotContain=" + DEFAULT_TYPE);
    }

    @Test
    @Transactional
    void getAllReactionsByUtilisateurIsEqualToSomething() throws Exception {
        Utilisateur utilisateur;
        if (TestUtil.findAll(em, Utilisateur.class).isEmpty()) {
            reactionRepository.saveAndFlush(reaction);
            utilisateur = UtilisateurResourceIT.createEntity();
        } else {
            utilisateur = TestUtil.findAll(em, Utilisateur.class).get(0);
        }
        em.persist(utilisateur);
        em.flush();
        reaction.setUtilisateur(utilisateur);
        reactionRepository.saveAndFlush(reaction);
        UUID utilisateurId = utilisateur.getId();
        // Get all the reactionList where utilisateur equals to utilisateurId
        defaultReactionShouldBeFound("utilisateurId.equals=" + utilisateurId);

        // Get all the reactionList where utilisateur equals to UUID.randomUUID()
        defaultReactionShouldNotBeFound("utilisateurId.equals=" + UUID.randomUUID());
    }

    @Test
    @Transactional
    void getAllReactionsByMessageIsEqualToSomething() throws Exception {
        Message message;
        if (TestUtil.findAll(em, Message.class).isEmpty()) {
            reactionRepository.saveAndFlush(reaction);
            message = MessageResourceIT.createEntity(em);
        } else {
            message = TestUtil.findAll(em, Message.class).get(0);
        }
        em.persist(message);
        em.flush();
        reaction.setMessage(message);
        reactionRepository.saveAndFlush(reaction);
        UUID messageId = message.getId();
        // Get all the reactionList where message equals to messageId
        defaultReactionShouldBeFound("messageId.equals=" + messageId);

        // Get all the reactionList where message equals to UUID.randomUUID()
        defaultReactionShouldNotBeFound("messageId.equals=" + UUID.randomUUID());
    }

    private void defaultReactionFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultReactionShouldBeFound(shouldBeFound);
        defaultReactionShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultReactionShouldBeFound(String filter) throws Exception {
        restReactionMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(reaction.getId().toString())))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE)));

        // Check, that the count call also returns 1
        restReactionMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultReactionShouldNotBeFound(String filter) throws Exception {
        restReactionMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restReactionMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingReaction() throws Exception {
        // Get the reaction
        restReactionMockMvc.perform(get(ENTITY_API_URL_ID, UUID.randomUUID().toString())).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingReaction() throws Exception {
        // Initialize the database
        insertedReaction = reactionRepository.saveAndFlush(reaction);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the reaction
        Reaction updatedReaction = reactionRepository.findById(reaction.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedReaction are not directly saved in db
        em.detach(updatedReaction);
        updatedReaction.type(UPDATED_TYPE);
        ReactionDTO reactionDTO = reactionMapper.toDto(updatedReaction);

        restReactionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, reactionDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(reactionDTO))
            )
            .andExpect(status().isOk());

        // Validate the Reaction in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedReactionToMatchAllProperties(updatedReaction);
    }

    @Test
    @Transactional
    void putNonExistingReaction() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        reaction.setId(UUID.randomUUID());

        // Create the Reaction
        ReactionDTO reactionDTO = reactionMapper.toDto(reaction);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restReactionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, reactionDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(reactionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Reaction in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchReaction() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        reaction.setId(UUID.randomUUID());

        // Create the Reaction
        ReactionDTO reactionDTO = reactionMapper.toDto(reaction);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restReactionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, UUID.randomUUID())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(reactionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Reaction in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamReaction() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        reaction.setId(UUID.randomUUID());

        // Create the Reaction
        ReactionDTO reactionDTO = reactionMapper.toDto(reaction);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restReactionMockMvc
            .perform(put(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(reactionDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Reaction in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateReactionWithPatch() throws Exception {
        // Initialize the database
        insertedReaction = reactionRepository.saveAndFlush(reaction);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the reaction using partial update
        Reaction partialUpdatedReaction = new Reaction();
        partialUpdatedReaction.setId(reaction.getId());

        partialUpdatedReaction.type(UPDATED_TYPE);

        restReactionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedReaction.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedReaction))
            )
            .andExpect(status().isOk());

        // Validate the Reaction in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertReactionUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedReaction, reaction), getPersistedReaction(reaction));
    }

    @Test
    @Transactional
    void fullUpdateReactionWithPatch() throws Exception {
        // Initialize the database
        insertedReaction = reactionRepository.saveAndFlush(reaction);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the reaction using partial update
        Reaction partialUpdatedReaction = new Reaction();
        partialUpdatedReaction.setId(reaction.getId());

        partialUpdatedReaction.type(UPDATED_TYPE);

        restReactionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedReaction.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedReaction))
            )
            .andExpect(status().isOk());

        // Validate the Reaction in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertReactionUpdatableFieldsEquals(partialUpdatedReaction, getPersistedReaction(partialUpdatedReaction));
    }

    @Test
    @Transactional
    void patchNonExistingReaction() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        reaction.setId(UUID.randomUUID());

        // Create the Reaction
        ReactionDTO reactionDTO = reactionMapper.toDto(reaction);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restReactionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, reactionDTO.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(reactionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Reaction in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchReaction() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        reaction.setId(UUID.randomUUID());

        // Create the Reaction
        ReactionDTO reactionDTO = reactionMapper.toDto(reaction);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restReactionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, UUID.randomUUID())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(reactionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Reaction in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamReaction() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        reaction.setId(UUID.randomUUID());

        // Create the Reaction
        ReactionDTO reactionDTO = reactionMapper.toDto(reaction);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restReactionMockMvc
            .perform(
                patch(ENTITY_API_URL).with(csrf()).contentType("application/merge-patch+json").content(om.writeValueAsBytes(reactionDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the Reaction in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteReaction() throws Exception {
        // Initialize the database
        insertedReaction = reactionRepository.saveAndFlush(reaction);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the reaction
        restReactionMockMvc
            .perform(delete(ENTITY_API_URL_ID, reaction.getId().toString()).with(csrf()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return reactionRepository.count();
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

    protected Reaction getPersistedReaction(Reaction reaction) {
        return reactionRepository.findById(reaction.getId()).orElseThrow();
    }

    protected void assertPersistedReactionToMatchAllProperties(Reaction expectedReaction) {
        assertReactionAllPropertiesEquals(expectedReaction, getPersistedReaction(expectedReaction));
    }

    protected void assertPersistedReactionToMatchUpdatableProperties(Reaction expectedReaction) {
        assertReactionAllUpdatablePropertiesEquals(expectedReaction, getPersistedReaction(expectedReaction));
    }
}
