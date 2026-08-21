package sn.dove.backend.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static sn.dove.backend.domain.FAQAsserts.*;
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
import sn.dove.backend.domain.Contenu;
import sn.dove.backend.domain.FAQ;
import sn.dove.backend.repository.FAQRepository;
import sn.dove.backend.service.FAQService;
import sn.dove.backend.service.dto.FAQDTO;
import sn.dove.backend.service.mapper.FAQMapper;

/**
 * Integration tests for the {@link FAQResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class FAQResourceIT {

    private static final String DEFAULT_TITRE = "AAAAAAAAAA";
    private static final String UPDATED_TITRE = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final String DEFAULT_QUESTION = "AAAAAAAAAA";
    private static final String UPDATED_QUESTION = "BBBBBBBBBB";

    private static final String DEFAULT_REPONSE = "AAAAAAAAAA";
    private static final String UPDATED_REPONSE = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/faqs";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    private ObjectMapper om;

    @Autowired
    private FAQRepository fAQRepository;

    @Mock
    private FAQRepository fAQRepositoryMock;

    @Autowired
    private FAQMapper fAQMapper;

    @Mock
    private FAQService fAQServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restFAQMockMvc;

    private FAQ fAQ;

    private FAQ insertedFAQ;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static FAQ createEntity(EntityManager em) {
        FAQ fAQ = new FAQ().titre(DEFAULT_TITRE).description(DEFAULT_DESCRIPTION).question(DEFAULT_QUESTION).reponse(DEFAULT_REPONSE);
        // Add required entity
        Contenu contenu;
        if (TestUtil.findAll(em, Contenu.class).isEmpty()) {
            contenu = ContenuResourceIT.createEntity(em);
            em.persist(contenu);
            em.flush();
        } else {
            contenu = TestUtil.findAll(em, Contenu.class).get(0);
        }
        fAQ.setContenu(contenu);
        return fAQ;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static FAQ createUpdatedEntity(EntityManager em) {
        FAQ updatedFAQ = new FAQ()
            .titre(UPDATED_TITRE)
            .description(UPDATED_DESCRIPTION)
            .question(UPDATED_QUESTION)
            .reponse(UPDATED_REPONSE);
        // Add required entity
        Contenu contenu;
        if (TestUtil.findAll(em, Contenu.class).isEmpty()) {
            contenu = ContenuResourceIT.createUpdatedEntity(em);
            em.persist(contenu);
            em.flush();
        } else {
            contenu = TestUtil.findAll(em, Contenu.class).get(0);
        }
        updatedFAQ.setContenu(contenu);
        return updatedFAQ;
    }

    @BeforeEach
    void initTest() {
        fAQ = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedFAQ != null) {
            fAQRepository.delete(insertedFAQ);
            insertedFAQ = null;
        }
    }

    @Test
    @Transactional
    void createFAQ() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the FAQ
        FAQDTO fAQDTO = fAQMapper.toDto(fAQ);
        var returnedFAQDTO = om.readValue(
            restFAQMockMvc
                .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(fAQDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            FAQDTO.class
        );

        // Validate the FAQ in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedFAQ = fAQMapper.toEntity(returnedFAQDTO);
        assertFAQUpdatableFieldsEquals(returnedFAQ, getPersistedFAQ(returnedFAQ));

        insertedFAQ = returnedFAQ;
    }

    @Test
    @Transactional
    void createFAQWithExistingId() throws Exception {
        // Create the FAQ with an existing ID
        insertedFAQ = fAQRepository.saveAndFlush(fAQ);
        FAQDTO fAQDTO = fAQMapper.toDto(fAQ);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restFAQMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(fAQDTO)))
            .andExpect(status().isBadRequest());

        // Validate the FAQ in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkTitreIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        fAQ.setTitre(null);

        // Create the FAQ, which fails.
        FAQDTO fAQDTO = fAQMapper.toDto(fAQ);

        restFAQMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(fAQDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllFAQS() throws Exception {
        // Initialize the database
        insertedFAQ = fAQRepository.saveAndFlush(fAQ);

        // Get all the fAQList
        restFAQMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(fAQ.getId().toString())))
            .andExpect(jsonPath("$.[*].titre").value(hasItem(DEFAULT_TITRE)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)))
            .andExpect(jsonPath("$.[*].question").value(hasItem(DEFAULT_QUESTION)))
            .andExpect(jsonPath("$.[*].reponse").value(hasItem(DEFAULT_REPONSE)));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllFAQSWithEagerRelationshipsIsEnabled() throws Exception {
        when(fAQServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restFAQMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(fAQServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllFAQSWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(fAQServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restFAQMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(fAQRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getFAQ() throws Exception {
        // Initialize the database
        insertedFAQ = fAQRepository.saveAndFlush(fAQ);

        // Get the fAQ
        restFAQMockMvc
            .perform(get(ENTITY_API_URL_ID, fAQ.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(fAQ.getId().toString()))
            .andExpect(jsonPath("$.titre").value(DEFAULT_TITRE))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION))
            .andExpect(jsonPath("$.question").value(DEFAULT_QUESTION))
            .andExpect(jsonPath("$.reponse").value(DEFAULT_REPONSE));
    }

    @Test
    @Transactional
    void getFAQSByIdFiltering() throws Exception {
        // Initialize the database
        insertedFAQ = fAQRepository.saveAndFlush(fAQ);

        UUID id = fAQ.getId();

        defaultFAQFiltering("id.equals=" + id, "id.notEquals=" + id);
    }

    @Test
    @Transactional
    void getAllFAQSByTitreIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedFAQ = fAQRepository.saveAndFlush(fAQ);

        // Get all the fAQList where titre equals to
        defaultFAQFiltering("titre.equals=" + DEFAULT_TITRE, "titre.equals=" + UPDATED_TITRE);
    }

    @Test
    @Transactional
    void getAllFAQSByTitreIsInShouldWork() throws Exception {
        // Initialize the database
        insertedFAQ = fAQRepository.saveAndFlush(fAQ);

        // Get all the fAQList where titre in
        defaultFAQFiltering("titre.in=" + DEFAULT_TITRE + "," + UPDATED_TITRE, "titre.in=" + UPDATED_TITRE);
    }

    @Test
    @Transactional
    void getAllFAQSByTitreIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedFAQ = fAQRepository.saveAndFlush(fAQ);

        // Get all the fAQList where titre is not null
        defaultFAQFiltering("titre.specified=true", "titre.specified=false");
    }

    @Test
    @Transactional
    void getAllFAQSByTitreContainsSomething() throws Exception {
        // Initialize the database
        insertedFAQ = fAQRepository.saveAndFlush(fAQ);

        // Get all the fAQList where titre contains
        defaultFAQFiltering("titre.contains=" + DEFAULT_TITRE, "titre.contains=" + UPDATED_TITRE);
    }

    @Test
    @Transactional
    void getAllFAQSByTitreNotContainsSomething() throws Exception {
        // Initialize the database
        insertedFAQ = fAQRepository.saveAndFlush(fAQ);

        // Get all the fAQList where titre does not contain
        defaultFAQFiltering("titre.doesNotContain=" + UPDATED_TITRE, "titre.doesNotContain=" + DEFAULT_TITRE);
    }

    @Test
    @Transactional
    void getAllFAQSByContenuIsEqualToSomething() throws Exception {
        Contenu contenu;
        if (TestUtil.findAll(em, Contenu.class).isEmpty()) {
            fAQRepository.saveAndFlush(fAQ);
            contenu = ContenuResourceIT.createEntity(em);
        } else {
            contenu = TestUtil.findAll(em, Contenu.class).get(0);
        }
        em.persist(contenu);
        em.flush();
        fAQ.setContenu(contenu);
        fAQRepository.saveAndFlush(fAQ);
        UUID contenuId = contenu.getId();
        // Get all the fAQList where contenu equals to contenuId
        defaultFAQShouldBeFound("contenuId.equals=" + contenuId);

        // Get all the fAQList where contenu equals to UUID.randomUUID()
        defaultFAQShouldNotBeFound("contenuId.equals=" + UUID.randomUUID());
    }

    private void defaultFAQFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultFAQShouldBeFound(shouldBeFound);
        defaultFAQShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultFAQShouldBeFound(String filter) throws Exception {
        restFAQMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(fAQ.getId().toString())))
            .andExpect(jsonPath("$.[*].titre").value(hasItem(DEFAULT_TITRE)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)))
            .andExpect(jsonPath("$.[*].question").value(hasItem(DEFAULT_QUESTION)))
            .andExpect(jsonPath("$.[*].reponse").value(hasItem(DEFAULT_REPONSE)));

        // Check, that the count call also returns 1
        restFAQMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultFAQShouldNotBeFound(String filter) throws Exception {
        restFAQMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restFAQMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingFAQ() throws Exception {
        // Get the fAQ
        restFAQMockMvc.perform(get(ENTITY_API_URL_ID, UUID.randomUUID().toString())).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingFAQ() throws Exception {
        // Initialize the database
        insertedFAQ = fAQRepository.saveAndFlush(fAQ);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the fAQ
        FAQ updatedFAQ = fAQRepository.findById(fAQ.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedFAQ are not directly saved in db
        em.detach(updatedFAQ);
        updatedFAQ.titre(UPDATED_TITRE).description(UPDATED_DESCRIPTION).question(UPDATED_QUESTION).reponse(UPDATED_REPONSE);
        FAQDTO fAQDTO = fAQMapper.toDto(updatedFAQ);

        restFAQMockMvc
            .perform(
                put(ENTITY_API_URL_ID, fAQDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(fAQDTO))
            )
            .andExpect(status().isOk());

        // Validate the FAQ in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedFAQToMatchAllProperties(updatedFAQ);
    }

    @Test
    @Transactional
    void putNonExistingFAQ() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        fAQ.setId(UUID.randomUUID());

        // Create the FAQ
        FAQDTO fAQDTO = fAQMapper.toDto(fAQ);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restFAQMockMvc
            .perform(
                put(ENTITY_API_URL_ID, fAQDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(fAQDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the FAQ in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchFAQ() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        fAQ.setId(UUID.randomUUID());

        // Create the FAQ
        FAQDTO fAQDTO = fAQMapper.toDto(fAQ);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restFAQMockMvc
            .perform(
                put(ENTITY_API_URL_ID, UUID.randomUUID())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(fAQDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the FAQ in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamFAQ() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        fAQ.setId(UUID.randomUUID());

        // Create the FAQ
        FAQDTO fAQDTO = fAQMapper.toDto(fAQ);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restFAQMockMvc
            .perform(put(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(fAQDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the FAQ in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateFAQWithPatch() throws Exception {
        // Initialize the database
        insertedFAQ = fAQRepository.saveAndFlush(fAQ);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the fAQ using partial update
        FAQ partialUpdatedFAQ = new FAQ();
        partialUpdatedFAQ.setId(fAQ.getId());

        partialUpdatedFAQ.titre(UPDATED_TITRE).description(UPDATED_DESCRIPTION).reponse(UPDATED_REPONSE);

        restFAQMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedFAQ.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedFAQ))
            )
            .andExpect(status().isOk());

        // Validate the FAQ in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertFAQUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedFAQ, fAQ), getPersistedFAQ(fAQ));
    }

    @Test
    @Transactional
    void fullUpdateFAQWithPatch() throws Exception {
        // Initialize the database
        insertedFAQ = fAQRepository.saveAndFlush(fAQ);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the fAQ using partial update
        FAQ partialUpdatedFAQ = new FAQ();
        partialUpdatedFAQ.setId(fAQ.getId());

        partialUpdatedFAQ.titre(UPDATED_TITRE).description(UPDATED_DESCRIPTION).question(UPDATED_QUESTION).reponse(UPDATED_REPONSE);

        restFAQMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedFAQ.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedFAQ))
            )
            .andExpect(status().isOk());

        // Validate the FAQ in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertFAQUpdatableFieldsEquals(partialUpdatedFAQ, getPersistedFAQ(partialUpdatedFAQ));
    }

    @Test
    @Transactional
    void patchNonExistingFAQ() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        fAQ.setId(UUID.randomUUID());

        // Create the FAQ
        FAQDTO fAQDTO = fAQMapper.toDto(fAQ);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restFAQMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, fAQDTO.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(fAQDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the FAQ in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchFAQ() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        fAQ.setId(UUID.randomUUID());

        // Create the FAQ
        FAQDTO fAQDTO = fAQMapper.toDto(fAQ);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restFAQMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, UUID.randomUUID())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(fAQDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the FAQ in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamFAQ() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        fAQ.setId(UUID.randomUUID());

        // Create the FAQ
        FAQDTO fAQDTO = fAQMapper.toDto(fAQ);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restFAQMockMvc
            .perform(patch(ENTITY_API_URL).with(csrf()).contentType("application/merge-patch+json").content(om.writeValueAsBytes(fAQDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the FAQ in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteFAQ() throws Exception {
        // Initialize the database
        insertedFAQ = fAQRepository.saveAndFlush(fAQ);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the fAQ
        restFAQMockMvc
            .perform(delete(ENTITY_API_URL_ID, fAQ.getId().toString()).with(csrf()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return fAQRepository.count();
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

    protected FAQ getPersistedFAQ(FAQ fAQ) {
        return fAQRepository.findById(fAQ.getId()).orElseThrow();
    }

    protected void assertPersistedFAQToMatchAllProperties(FAQ expectedFAQ) {
        assertFAQAllPropertiesEquals(expectedFAQ, getPersistedFAQ(expectedFAQ));
    }

    protected void assertPersistedFAQToMatchUpdatableProperties(FAQ expectedFAQ) {
        assertFAQAllUpdatablePropertiesEquals(expectedFAQ, getPersistedFAQ(expectedFAQ));
    }
}
