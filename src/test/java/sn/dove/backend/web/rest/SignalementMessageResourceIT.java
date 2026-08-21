package sn.dove.backend.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static sn.dove.backend.domain.SignalementMessageAsserts.*;
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
import sn.dove.backend.domain.Message;
import sn.dove.backend.domain.SignalementMessage;
import sn.dove.backend.domain.Utilisateur;
import sn.dove.backend.repository.SignalementMessageRepository;
import sn.dove.backend.service.SignalementMessageService;
import sn.dove.backend.service.dto.SignalementMessageDTO;
import sn.dove.backend.service.mapper.SignalementMessageMapper;

/**
 * Integration tests for the {@link SignalementMessageResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class SignalementMessageResourceIT {

    private static final String DEFAULT_MOTIF = "AAAAAAAAAA";
    private static final String UPDATED_MOTIF = "BBBBBBBBBB";

    private static final Instant DEFAULT_DATE_SIGNALEMENT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_DATE_SIGNALEMENT = Instant.ofEpochMilli(1787238072967L);

    private static final String ENTITY_API_URL = "/api/signalement-messages";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    private ObjectMapper om;

    @Autowired
    private SignalementMessageRepository signalementMessageRepository;

    @Mock
    private SignalementMessageRepository signalementMessageRepositoryMock;

    @Autowired
    private SignalementMessageMapper signalementMessageMapper;

    @Mock
    private SignalementMessageService signalementMessageServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restSignalementMessageMockMvc;

    private SignalementMessage signalementMessage;

    private SignalementMessage insertedSignalementMessage;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static SignalementMessage createEntity(EntityManager em) {
        SignalementMessage signalementMessage = new SignalementMessage().motif(DEFAULT_MOTIF).dateSignalement(DEFAULT_DATE_SIGNALEMENT);
        // Add required entity
        Utilisateur utilisateur;
        if (TestUtil.findAll(em, Utilisateur.class).isEmpty()) {
            utilisateur = UtilisateurResourceIT.createEntity();
            em.persist(utilisateur);
            em.flush();
        } else {
            utilisateur = TestUtil.findAll(em, Utilisateur.class).get(0);
        }
        signalementMessage.setUtilisateur(utilisateur);
        // Add required entity
        Message message;
        if (TestUtil.findAll(em, Message.class).isEmpty()) {
            message = MessageResourceIT.createEntity(em);
            em.persist(message);
            em.flush();
        } else {
            message = TestUtil.findAll(em, Message.class).get(0);
        }
        signalementMessage.setMessage(message);
        return signalementMessage;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static SignalementMessage createUpdatedEntity(EntityManager em) {
        SignalementMessage updatedSignalementMessage = new SignalementMessage()
            .motif(UPDATED_MOTIF)
            .dateSignalement(UPDATED_DATE_SIGNALEMENT);
        // Add required entity
        Utilisateur utilisateur;
        if (TestUtil.findAll(em, Utilisateur.class).isEmpty()) {
            utilisateur = UtilisateurResourceIT.createUpdatedEntity();
            em.persist(utilisateur);
            em.flush();
        } else {
            utilisateur = TestUtil.findAll(em, Utilisateur.class).get(0);
        }
        updatedSignalementMessage.setUtilisateur(utilisateur);
        // Add required entity
        Message message;
        if (TestUtil.findAll(em, Message.class).isEmpty()) {
            message = MessageResourceIT.createUpdatedEntity(em);
            em.persist(message);
            em.flush();
        } else {
            message = TestUtil.findAll(em, Message.class).get(0);
        }
        updatedSignalementMessage.setMessage(message);
        return updatedSignalementMessage;
    }

    @BeforeEach
    void initTest() {
        signalementMessage = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedSignalementMessage != null) {
            signalementMessageRepository.delete(insertedSignalementMessage);
            insertedSignalementMessage = null;
        }
    }

    @Test
    @Transactional
    void createSignalementMessage() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the SignalementMessage
        SignalementMessageDTO signalementMessageDTO = signalementMessageMapper.toDto(signalementMessage);
        var returnedSignalementMessageDTO = om.readValue(
            restSignalementMessageMockMvc
                .perform(
                    post(ENTITY_API_URL)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(signalementMessageDTO))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            SignalementMessageDTO.class
        );

        // Validate the SignalementMessage in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedSignalementMessage = signalementMessageMapper.toEntity(returnedSignalementMessageDTO);
        assertSignalementMessageUpdatableFieldsEquals(
            returnedSignalementMessage,
            getPersistedSignalementMessage(returnedSignalementMessage)
        );

        insertedSignalementMessage = returnedSignalementMessage;
    }

    @Test
    @Transactional
    void createSignalementMessageWithExistingId() throws Exception {
        // Create the SignalementMessage with an existing ID
        insertedSignalementMessage = signalementMessageRepository.saveAndFlush(signalementMessage);
        SignalementMessageDTO signalementMessageDTO = signalementMessageMapper.toDto(signalementMessage);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restSignalementMessageMockMvc
            .perform(
                post(ENTITY_API_URL)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(signalementMessageDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the SignalementMessage in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkDateSignalementIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        signalementMessage.setDateSignalement(null);

        // Create the SignalementMessage, which fails.
        SignalementMessageDTO signalementMessageDTO = signalementMessageMapper.toDto(signalementMessage);

        restSignalementMessageMockMvc
            .perform(
                post(ENTITY_API_URL)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(signalementMessageDTO))
            )
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllSignalementMessages() throws Exception {
        // Initialize the database
        insertedSignalementMessage = signalementMessageRepository.saveAndFlush(signalementMessage);

        // Get all the signalementMessageList
        restSignalementMessageMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(signalementMessage.getId().toString())))
            .andExpect(jsonPath("$.[*].motif").value(hasItem(DEFAULT_MOTIF)))
            .andExpect(jsonPath("$.[*].dateSignalement").value(hasItem(DEFAULT_DATE_SIGNALEMENT.toString())));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllSignalementMessagesWithEagerRelationshipsIsEnabled() throws Exception {
        when(signalementMessageServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restSignalementMessageMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(signalementMessageServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllSignalementMessagesWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(signalementMessageServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restSignalementMessageMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(signalementMessageRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getSignalementMessage() throws Exception {
        // Initialize the database
        insertedSignalementMessage = signalementMessageRepository.saveAndFlush(signalementMessage);

        // Get the signalementMessage
        restSignalementMessageMockMvc
            .perform(get(ENTITY_API_URL_ID, signalementMessage.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(signalementMessage.getId().toString()))
            .andExpect(jsonPath("$.motif").value(DEFAULT_MOTIF))
            .andExpect(jsonPath("$.dateSignalement").value(DEFAULT_DATE_SIGNALEMENT.toString()));
    }

    @Test
    @Transactional
    void getSignalementMessagesByIdFiltering() throws Exception {
        // Initialize the database
        insertedSignalementMessage = signalementMessageRepository.saveAndFlush(signalementMessage);

        UUID id = signalementMessage.getId();

        defaultSignalementMessageFiltering("id.equals=" + id, "id.notEquals=" + id);
    }

    @Test
    @Transactional
    void getAllSignalementMessagesByDateSignalementIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedSignalementMessage = signalementMessageRepository.saveAndFlush(signalementMessage);

        // Get all the signalementMessageList where dateSignalement equals to
        defaultSignalementMessageFiltering(
            "dateSignalement.equals=" + DEFAULT_DATE_SIGNALEMENT,
            "dateSignalement.equals=" + UPDATED_DATE_SIGNALEMENT
        );
    }

    @Test
    @Transactional
    void getAllSignalementMessagesByDateSignalementIsInShouldWork() throws Exception {
        // Initialize the database
        insertedSignalementMessage = signalementMessageRepository.saveAndFlush(signalementMessage);

        // Get all the signalementMessageList where dateSignalement in
        defaultSignalementMessageFiltering(
            "dateSignalement.in=" + DEFAULT_DATE_SIGNALEMENT + "," + UPDATED_DATE_SIGNALEMENT,
            "dateSignalement.in=" + UPDATED_DATE_SIGNALEMENT
        );
    }

    @Test
    @Transactional
    void getAllSignalementMessagesByDateSignalementIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedSignalementMessage = signalementMessageRepository.saveAndFlush(signalementMessage);

        // Get all the signalementMessageList where dateSignalement is not null
        defaultSignalementMessageFiltering("dateSignalement.specified=true", "dateSignalement.specified=false");
    }

    @Test
    @Transactional
    void getAllSignalementMessagesByUtilisateurIsEqualToSomething() throws Exception {
        Utilisateur utilisateur;
        if (TestUtil.findAll(em, Utilisateur.class).isEmpty()) {
            signalementMessageRepository.saveAndFlush(signalementMessage);
            utilisateur = UtilisateurResourceIT.createEntity();
        } else {
            utilisateur = TestUtil.findAll(em, Utilisateur.class).get(0);
        }
        em.persist(utilisateur);
        em.flush();
        signalementMessage.setUtilisateur(utilisateur);
        signalementMessageRepository.saveAndFlush(signalementMessage);
        UUID utilisateurId = utilisateur.getId();
        // Get all the signalementMessageList where utilisateur equals to utilisateurId
        defaultSignalementMessageShouldBeFound("utilisateurId.equals=" + utilisateurId);

        // Get all the signalementMessageList where utilisateur equals to UUID.randomUUID()
        defaultSignalementMessageShouldNotBeFound("utilisateurId.equals=" + UUID.randomUUID());
    }

    @Test
    @Transactional
    void getAllSignalementMessagesByMessageIsEqualToSomething() throws Exception {
        Message message;
        if (TestUtil.findAll(em, Message.class).isEmpty()) {
            signalementMessageRepository.saveAndFlush(signalementMessage);
            message = MessageResourceIT.createEntity(em);
        } else {
            message = TestUtil.findAll(em, Message.class).get(0);
        }
        em.persist(message);
        em.flush();
        signalementMessage.setMessage(message);
        signalementMessageRepository.saveAndFlush(signalementMessage);
        UUID messageId = message.getId();
        // Get all the signalementMessageList where message equals to messageId
        defaultSignalementMessageShouldBeFound("messageId.equals=" + messageId);

        // Get all the signalementMessageList where message equals to UUID.randomUUID()
        defaultSignalementMessageShouldNotBeFound("messageId.equals=" + UUID.randomUUID());
    }

    private void defaultSignalementMessageFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultSignalementMessageShouldBeFound(shouldBeFound);
        defaultSignalementMessageShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultSignalementMessageShouldBeFound(String filter) throws Exception {
        restSignalementMessageMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(signalementMessage.getId().toString())))
            .andExpect(jsonPath("$.[*].motif").value(hasItem(DEFAULT_MOTIF)))
            .andExpect(jsonPath("$.[*].dateSignalement").value(hasItem(DEFAULT_DATE_SIGNALEMENT.toString())));

        // Check, that the count call also returns 1
        restSignalementMessageMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultSignalementMessageShouldNotBeFound(String filter) throws Exception {
        restSignalementMessageMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restSignalementMessageMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingSignalementMessage() throws Exception {
        // Get the signalementMessage
        restSignalementMessageMockMvc.perform(get(ENTITY_API_URL_ID, UUID.randomUUID().toString())).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingSignalementMessage() throws Exception {
        // Initialize the database
        insertedSignalementMessage = signalementMessageRepository.saveAndFlush(signalementMessage);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the signalementMessage
        SignalementMessage updatedSignalementMessage = signalementMessageRepository.findById(signalementMessage.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedSignalementMessage are not directly saved in db
        em.detach(updatedSignalementMessage);
        updatedSignalementMessage.motif(UPDATED_MOTIF).dateSignalement(UPDATED_DATE_SIGNALEMENT);
        SignalementMessageDTO signalementMessageDTO = signalementMessageMapper.toDto(updatedSignalementMessage);

        restSignalementMessageMockMvc
            .perform(
                put(ENTITY_API_URL_ID, signalementMessageDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(signalementMessageDTO))
            )
            .andExpect(status().isOk());

        // Validate the SignalementMessage in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedSignalementMessageToMatchAllProperties(updatedSignalementMessage);
    }

    @Test
    @Transactional
    void putNonExistingSignalementMessage() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        signalementMessage.setId(UUID.randomUUID());

        // Create the SignalementMessage
        SignalementMessageDTO signalementMessageDTO = signalementMessageMapper.toDto(signalementMessage);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restSignalementMessageMockMvc
            .perform(
                put(ENTITY_API_URL_ID, signalementMessageDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(signalementMessageDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the SignalementMessage in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchSignalementMessage() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        signalementMessage.setId(UUID.randomUUID());

        // Create the SignalementMessage
        SignalementMessageDTO signalementMessageDTO = signalementMessageMapper.toDto(signalementMessage);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSignalementMessageMockMvc
            .perform(
                put(ENTITY_API_URL_ID, UUID.randomUUID())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(signalementMessageDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the SignalementMessage in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamSignalementMessage() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        signalementMessage.setId(UUID.randomUUID());

        // Create the SignalementMessage
        SignalementMessageDTO signalementMessageDTO = signalementMessageMapper.toDto(signalementMessage);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSignalementMessageMockMvc
            .perform(
                put(ENTITY_API_URL)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(signalementMessageDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the SignalementMessage in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateSignalementMessageWithPatch() throws Exception {
        // Initialize the database
        insertedSignalementMessage = signalementMessageRepository.saveAndFlush(signalementMessage);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the signalementMessage using partial update
        SignalementMessage partialUpdatedSignalementMessage = new SignalementMessage();
        partialUpdatedSignalementMessage.setId(signalementMessage.getId());

        restSignalementMessageMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedSignalementMessage.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedSignalementMessage))
            )
            .andExpect(status().isOk());

        // Validate the SignalementMessage in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertSignalementMessageUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedSignalementMessage, signalementMessage),
            getPersistedSignalementMessage(signalementMessage)
        );
    }

    @Test
    @Transactional
    void fullUpdateSignalementMessageWithPatch() throws Exception {
        // Initialize the database
        insertedSignalementMessage = signalementMessageRepository.saveAndFlush(signalementMessage);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the signalementMessage using partial update
        SignalementMessage partialUpdatedSignalementMessage = new SignalementMessage();
        partialUpdatedSignalementMessage.setId(signalementMessage.getId());

        partialUpdatedSignalementMessage.motif(UPDATED_MOTIF).dateSignalement(UPDATED_DATE_SIGNALEMENT);

        restSignalementMessageMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedSignalementMessage.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedSignalementMessage))
            )
            .andExpect(status().isOk());

        // Validate the SignalementMessage in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertSignalementMessageUpdatableFieldsEquals(
            partialUpdatedSignalementMessage,
            getPersistedSignalementMessage(partialUpdatedSignalementMessage)
        );
    }

    @Test
    @Transactional
    void patchNonExistingSignalementMessage() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        signalementMessage.setId(UUID.randomUUID());

        // Create the SignalementMessage
        SignalementMessageDTO signalementMessageDTO = signalementMessageMapper.toDto(signalementMessage);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restSignalementMessageMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, signalementMessageDTO.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(signalementMessageDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the SignalementMessage in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchSignalementMessage() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        signalementMessage.setId(UUID.randomUUID());

        // Create the SignalementMessage
        SignalementMessageDTO signalementMessageDTO = signalementMessageMapper.toDto(signalementMessage);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSignalementMessageMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, UUID.randomUUID())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(signalementMessageDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the SignalementMessage in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamSignalementMessage() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        signalementMessage.setId(UUID.randomUUID());

        // Create the SignalementMessage
        SignalementMessageDTO signalementMessageDTO = signalementMessageMapper.toDto(signalementMessage);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSignalementMessageMockMvc
            .perform(
                patch(ENTITY_API_URL)
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(signalementMessageDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the SignalementMessage in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteSignalementMessage() throws Exception {
        // Initialize the database
        insertedSignalementMessage = signalementMessageRepository.saveAndFlush(signalementMessage);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the signalementMessage
        restSignalementMessageMockMvc
            .perform(delete(ENTITY_API_URL_ID, signalementMessage.getId().toString()).with(csrf()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return signalementMessageRepository.count();
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

    protected SignalementMessage getPersistedSignalementMessage(SignalementMessage signalementMessage) {
        return signalementMessageRepository.findById(signalementMessage.getId()).orElseThrow();
    }

    protected void assertPersistedSignalementMessageToMatchAllProperties(SignalementMessage expectedSignalementMessage) {
        assertSignalementMessageAllPropertiesEquals(expectedSignalementMessage, getPersistedSignalementMessage(expectedSignalementMessage));
    }

    protected void assertPersistedSignalementMessageToMatchUpdatableProperties(SignalementMessage expectedSignalementMessage) {
        assertSignalementMessageAllUpdatablePropertiesEquals(
            expectedSignalementMessage,
            getPersistedSignalementMessage(expectedSignalementMessage)
        );
    }
}
