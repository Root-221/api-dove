package sn.dove.backend.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static sn.dove.backend.domain.MediaAsserts.*;
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
import sn.dove.backend.domain.Media;
import sn.dove.backend.repository.MediaRepository;
import sn.dove.backend.service.MediaService;
import sn.dove.backend.service.dto.MediaDTO;
import sn.dove.backend.service.mapper.MediaMapper;

/**
 * Integration tests for the {@link MediaResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class MediaResourceIT {

    private static final String DEFAULT_TITRE = "AAAAAAAAAA";
    private static final String UPDATED_TITRE = "BBBBBBBBBB";

    private static final Integer DEFAULT_ETAPE = 0;
    private static final Integer UPDATED_ETAPE = 1;
    private static final Integer SMALLER_ETAPE = 0 - 1;

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final String DEFAULT_URL = "AAAAAAAAAA";
    private static final String UPDATED_URL = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/media";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    private ObjectMapper om;

    @Autowired
    private MediaRepository mediaRepository;

    @Mock
    private MediaRepository mediaRepositoryMock;

    @Autowired
    private MediaMapper mediaMapper;

    @Mock
    private MediaService mediaServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restMediaMockMvc;

    private Media media;

    private Media insertedMedia;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Media createEntity() {
        return new Media().titre(DEFAULT_TITRE).etape(DEFAULT_ETAPE).description(DEFAULT_DESCRIPTION).url(DEFAULT_URL);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Media createUpdatedEntity() {
        return new Media().titre(UPDATED_TITRE).etape(UPDATED_ETAPE).description(UPDATED_DESCRIPTION).url(UPDATED_URL);
    }

    @BeforeEach
    void initTest() {
        media = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedMedia != null) {
            mediaRepository.delete(insertedMedia);
            insertedMedia = null;
        }
    }

    @Test
    @Transactional
    void createMedia() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Media
        MediaDTO mediaDTO = mediaMapper.toDto(media);
        var returnedMediaDTO = om.readValue(
            restMediaMockMvc
                .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(mediaDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            MediaDTO.class
        );

        // Validate the Media in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedMedia = mediaMapper.toEntity(returnedMediaDTO);
        assertMediaUpdatableFieldsEquals(returnedMedia, getPersistedMedia(returnedMedia));

        insertedMedia = returnedMedia;
    }

    @Test
    @Transactional
    void createMediaWithExistingId() throws Exception {
        // Create the Media with an existing ID
        insertedMedia = mediaRepository.saveAndFlush(media);
        MediaDTO mediaDTO = mediaMapper.toDto(media);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restMediaMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(mediaDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Media in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkTitreIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        media.setTitre(null);

        // Create the Media, which fails.
        MediaDTO mediaDTO = mediaMapper.toDto(media);

        restMediaMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(mediaDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkUrlIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        media.setUrl(null);

        // Create the Media, which fails.
        MediaDTO mediaDTO = mediaMapper.toDto(media);

        restMediaMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(mediaDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllMedias() throws Exception {
        // Initialize the database
        insertedMedia = mediaRepository.saveAndFlush(media);

        // Get all the mediaList
        restMediaMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(media.getId().toString())))
            .andExpect(jsonPath("$.[*].titre").value(hasItem(DEFAULT_TITRE)))
            .andExpect(jsonPath("$.[*].etape").value(hasItem(DEFAULT_ETAPE)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)))
            .andExpect(jsonPath("$.[*].url").value(hasItem(DEFAULT_URL)));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllMediasWithEagerRelationshipsIsEnabled() throws Exception {
        when(mediaServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restMediaMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(mediaServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllMediasWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(mediaServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restMediaMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(mediaRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getMedia() throws Exception {
        // Initialize the database
        insertedMedia = mediaRepository.saveAndFlush(media);

        // Get the media
        restMediaMockMvc
            .perform(get(ENTITY_API_URL_ID, media.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(media.getId().toString()))
            .andExpect(jsonPath("$.titre").value(DEFAULT_TITRE))
            .andExpect(jsonPath("$.etape").value(DEFAULT_ETAPE))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION))
            .andExpect(jsonPath("$.url").value(DEFAULT_URL));
    }

    @Test
    @Transactional
    void getMediasByIdFiltering() throws Exception {
        // Initialize the database
        insertedMedia = mediaRepository.saveAndFlush(media);

        UUID id = media.getId();

        defaultMediaFiltering("id.equals=" + id, "id.notEquals=" + id);
    }

    @Test
    @Transactional
    void getAllMediasByTitreIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedMedia = mediaRepository.saveAndFlush(media);

        // Get all the mediaList where titre equals to
        defaultMediaFiltering("titre.equals=" + DEFAULT_TITRE, "titre.equals=" + UPDATED_TITRE);
    }

    @Test
    @Transactional
    void getAllMediasByTitreIsInShouldWork() throws Exception {
        // Initialize the database
        insertedMedia = mediaRepository.saveAndFlush(media);

        // Get all the mediaList where titre in
        defaultMediaFiltering("titre.in=" + DEFAULT_TITRE + "," + UPDATED_TITRE, "titre.in=" + UPDATED_TITRE);
    }

    @Test
    @Transactional
    void getAllMediasByTitreIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedMedia = mediaRepository.saveAndFlush(media);

        // Get all the mediaList where titre is not null
        defaultMediaFiltering("titre.specified=true", "titre.specified=false");
    }

    @Test
    @Transactional
    void getAllMediasByTitreContainsSomething() throws Exception {
        // Initialize the database
        insertedMedia = mediaRepository.saveAndFlush(media);

        // Get all the mediaList where titre contains
        defaultMediaFiltering("titre.contains=" + DEFAULT_TITRE, "titre.contains=" + UPDATED_TITRE);
    }

    @Test
    @Transactional
    void getAllMediasByTitreNotContainsSomething() throws Exception {
        // Initialize the database
        insertedMedia = mediaRepository.saveAndFlush(media);

        // Get all the mediaList where titre does not contain
        defaultMediaFiltering("titre.doesNotContain=" + UPDATED_TITRE, "titre.doesNotContain=" + DEFAULT_TITRE);
    }

    @Test
    @Transactional
    void getAllMediasByEtapeIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedMedia = mediaRepository.saveAndFlush(media);

        // Get all the mediaList where etape equals to
        defaultMediaFiltering("etape.equals=" + DEFAULT_ETAPE, "etape.equals=" + UPDATED_ETAPE);
    }

    @Test
    @Transactional
    void getAllMediasByEtapeIsInShouldWork() throws Exception {
        // Initialize the database
        insertedMedia = mediaRepository.saveAndFlush(media);

        // Get all the mediaList where etape in
        defaultMediaFiltering("etape.in=" + DEFAULT_ETAPE + "," + UPDATED_ETAPE, "etape.in=" + UPDATED_ETAPE);
    }

    @Test
    @Transactional
    void getAllMediasByEtapeIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedMedia = mediaRepository.saveAndFlush(media);

        // Get all the mediaList where etape is not null
        defaultMediaFiltering("etape.specified=true", "etape.specified=false");
    }

    @Test
    @Transactional
    void getAllMediasByEtapeIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedMedia = mediaRepository.saveAndFlush(media);

        // Get all the mediaList where etape is greater than or equal to
        defaultMediaFiltering("etape.greaterThanOrEqual=" + DEFAULT_ETAPE, "etape.greaterThanOrEqual=" + UPDATED_ETAPE);
    }

    @Test
    @Transactional
    void getAllMediasByEtapeIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedMedia = mediaRepository.saveAndFlush(media);

        // Get all the mediaList where etape is less than or equal to
        defaultMediaFiltering("etape.lessThanOrEqual=" + DEFAULT_ETAPE, "etape.lessThanOrEqual=" + SMALLER_ETAPE);
    }

    @Test
    @Transactional
    void getAllMediasByEtapeIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedMedia = mediaRepository.saveAndFlush(media);

        // Get all the mediaList where etape is less than
        defaultMediaFiltering("etape.lessThan=" + UPDATED_ETAPE, "etape.lessThan=" + DEFAULT_ETAPE);
    }

    @Test
    @Transactional
    void getAllMediasByEtapeIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedMedia = mediaRepository.saveAndFlush(media);

        // Get all the mediaList where etape is greater than
        defaultMediaFiltering("etape.greaterThan=" + SMALLER_ETAPE, "etape.greaterThan=" + DEFAULT_ETAPE);
    }

    @Test
    @Transactional
    void getAllMediasByUrlIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedMedia = mediaRepository.saveAndFlush(media);

        // Get all the mediaList where url equals to
        defaultMediaFiltering("url.equals=" + DEFAULT_URL, "url.equals=" + UPDATED_URL);
    }

    @Test
    @Transactional
    void getAllMediasByUrlIsInShouldWork() throws Exception {
        // Initialize the database
        insertedMedia = mediaRepository.saveAndFlush(media);

        // Get all the mediaList where url in
        defaultMediaFiltering("url.in=" + DEFAULT_URL + "," + UPDATED_URL, "url.in=" + UPDATED_URL);
    }

    @Test
    @Transactional
    void getAllMediasByUrlIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedMedia = mediaRepository.saveAndFlush(media);

        // Get all the mediaList where url is not null
        defaultMediaFiltering("url.specified=true", "url.specified=false");
    }

    @Test
    @Transactional
    void getAllMediasByUrlContainsSomething() throws Exception {
        // Initialize the database
        insertedMedia = mediaRepository.saveAndFlush(media);

        // Get all the mediaList where url contains
        defaultMediaFiltering("url.contains=" + DEFAULT_URL, "url.contains=" + UPDATED_URL);
    }

    @Test
    @Transactional
    void getAllMediasByUrlNotContainsSomething() throws Exception {
        // Initialize the database
        insertedMedia = mediaRepository.saveAndFlush(media);

        // Get all the mediaList where url does not contain
        defaultMediaFiltering("url.doesNotContain=" + UPDATED_URL, "url.doesNotContain=" + DEFAULT_URL);
    }

    @Test
    @Transactional
    void getAllMediasByContenuIsEqualToSomething() throws Exception {
        Contenu contenu;
        if (TestUtil.findAll(em, Contenu.class).isEmpty()) {
            mediaRepository.saveAndFlush(media);
            contenu = ContenuResourceIT.createEntity(em);
        } else {
            contenu = TestUtil.findAll(em, Contenu.class).get(0);
        }
        em.persist(contenu);
        em.flush();
        media.setContenu(contenu);
        mediaRepository.saveAndFlush(media);
        UUID contenuId = contenu.getId();
        // Get all the mediaList where contenu equals to contenuId
        defaultMediaShouldBeFound("contenuId.equals=" + contenuId);

        // Get all the mediaList where contenu equals to UUID.randomUUID()
        defaultMediaShouldNotBeFound("contenuId.equals=" + UUID.randomUUID());
    }

    private void defaultMediaFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultMediaShouldBeFound(shouldBeFound);
        defaultMediaShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultMediaShouldBeFound(String filter) throws Exception {
        restMediaMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(media.getId().toString())))
            .andExpect(jsonPath("$.[*].titre").value(hasItem(DEFAULT_TITRE)))
            .andExpect(jsonPath("$.[*].etape").value(hasItem(DEFAULT_ETAPE)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)))
            .andExpect(jsonPath("$.[*].url").value(hasItem(DEFAULT_URL)));

        // Check, that the count call also returns 1
        restMediaMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultMediaShouldNotBeFound(String filter) throws Exception {
        restMediaMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restMediaMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingMedia() throws Exception {
        // Get the media
        restMediaMockMvc.perform(get(ENTITY_API_URL_ID, UUID.randomUUID().toString())).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingMedia() throws Exception {
        // Initialize the database
        insertedMedia = mediaRepository.saveAndFlush(media);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the media
        Media updatedMedia = mediaRepository.findById(media.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedMedia are not directly saved in db
        em.detach(updatedMedia);
        updatedMedia.titre(UPDATED_TITRE).etape(UPDATED_ETAPE).description(UPDATED_DESCRIPTION).url(UPDATED_URL);
        MediaDTO mediaDTO = mediaMapper.toDto(updatedMedia);

        restMediaMockMvc
            .perform(
                put(ENTITY_API_URL_ID, mediaDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(mediaDTO))
            )
            .andExpect(status().isOk());

        // Validate the Media in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedMediaToMatchAllProperties(updatedMedia);
    }

    @Test
    @Transactional
    void putNonExistingMedia() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        media.setId(UUID.randomUUID());

        // Create the Media
        MediaDTO mediaDTO = mediaMapper.toDto(media);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restMediaMockMvc
            .perform(
                put(ENTITY_API_URL_ID, mediaDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(mediaDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Media in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchMedia() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        media.setId(UUID.randomUUID());

        // Create the Media
        MediaDTO mediaDTO = mediaMapper.toDto(media);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMediaMockMvc
            .perform(
                put(ENTITY_API_URL_ID, UUID.randomUUID())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(mediaDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Media in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamMedia() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        media.setId(UUID.randomUUID());

        // Create the Media
        MediaDTO mediaDTO = mediaMapper.toDto(media);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMediaMockMvc
            .perform(put(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(mediaDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Media in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateMediaWithPatch() throws Exception {
        // Initialize the database
        insertedMedia = mediaRepository.saveAndFlush(media);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the media using partial update
        Media partialUpdatedMedia = new Media();
        partialUpdatedMedia.setId(media.getId());

        partialUpdatedMedia.titre(UPDATED_TITRE).etape(UPDATED_ETAPE).description(UPDATED_DESCRIPTION).url(UPDATED_URL);

        restMediaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedMedia.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedMedia))
            )
            .andExpect(status().isOk());

        // Validate the Media in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertMediaUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedMedia, media), getPersistedMedia(media));
    }

    @Test
    @Transactional
    void fullUpdateMediaWithPatch() throws Exception {
        // Initialize the database
        insertedMedia = mediaRepository.saveAndFlush(media);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the media using partial update
        Media partialUpdatedMedia = new Media();
        partialUpdatedMedia.setId(media.getId());

        partialUpdatedMedia.titre(UPDATED_TITRE).etape(UPDATED_ETAPE).description(UPDATED_DESCRIPTION).url(UPDATED_URL);

        restMediaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedMedia.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedMedia))
            )
            .andExpect(status().isOk());

        // Validate the Media in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertMediaUpdatableFieldsEquals(partialUpdatedMedia, getPersistedMedia(partialUpdatedMedia));
    }

    @Test
    @Transactional
    void patchNonExistingMedia() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        media.setId(UUID.randomUUID());

        // Create the Media
        MediaDTO mediaDTO = mediaMapper.toDto(media);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restMediaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, mediaDTO.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(mediaDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Media in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchMedia() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        media.setId(UUID.randomUUID());

        // Create the Media
        MediaDTO mediaDTO = mediaMapper.toDto(media);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMediaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, UUID.randomUUID())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(mediaDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Media in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamMedia() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        media.setId(UUID.randomUUID());

        // Create the Media
        MediaDTO mediaDTO = mediaMapper.toDto(media);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMediaMockMvc
            .perform(patch(ENTITY_API_URL).with(csrf()).contentType("application/merge-patch+json").content(om.writeValueAsBytes(mediaDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Media in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteMedia() throws Exception {
        // Initialize the database
        insertedMedia = mediaRepository.saveAndFlush(media);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the media
        restMediaMockMvc
            .perform(delete(ENTITY_API_URL_ID, media.getId().toString()).with(csrf()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return mediaRepository.count();
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

    protected Media getPersistedMedia(Media media) {
        return mediaRepository.findById(media.getId()).orElseThrow();
    }

    protected void assertPersistedMediaToMatchAllProperties(Media expectedMedia) {
        assertMediaAllPropertiesEquals(expectedMedia, getPersistedMedia(expectedMedia));
    }

    protected void assertPersistedMediaToMatchUpdatableProperties(Media expectedMedia) {
        assertMediaAllUpdatablePropertiesEquals(expectedMedia, getPersistedMedia(expectedMedia));
    }
}
