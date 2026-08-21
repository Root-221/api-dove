package sn.dove.backend.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static sn.dove.backend.domain.UtilisateurRoleAsserts.*;
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
import sn.dove.backend.domain.Role;
import sn.dove.backend.domain.Utilisateur;
import sn.dove.backend.domain.UtilisateurRole;
import sn.dove.backend.repository.UtilisateurRoleRepository;
import sn.dove.backend.service.UtilisateurRoleService;
import sn.dove.backend.service.dto.UtilisateurRoleDTO;
import sn.dove.backend.service.mapper.UtilisateurRoleMapper;

/**
 * Integration tests for the {@link UtilisateurRoleResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class UtilisateurRoleResourceIT {

    private static final String ENTITY_API_URL = "/api/utilisateur-roles";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    private ObjectMapper om;

    @Autowired
    private UtilisateurRoleRepository utilisateurRoleRepository;

    @Mock
    private UtilisateurRoleRepository utilisateurRoleRepositoryMock;

    @Autowired
    private UtilisateurRoleMapper utilisateurRoleMapper;

    @Mock
    private UtilisateurRoleService utilisateurRoleServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restUtilisateurRoleMockMvc;

    private UtilisateurRole utilisateurRole;

    private UtilisateurRole insertedUtilisateurRole;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static UtilisateurRole createEntity(EntityManager em) {
        UtilisateurRole utilisateurRole = new UtilisateurRole();
        // Add required entity
        Utilisateur utilisateur;
        if (TestUtil.findAll(em, Utilisateur.class).isEmpty()) {
            utilisateur = UtilisateurResourceIT.createEntity();
            em.persist(utilisateur);
            em.flush();
        } else {
            utilisateur = TestUtil.findAll(em, Utilisateur.class).get(0);
        }
        utilisateurRole.setUtilisateur(utilisateur);
        // Add required entity
        Role role;
        if (TestUtil.findAll(em, Role.class).isEmpty()) {
            role = RoleResourceIT.createEntity();
            em.persist(role);
            em.flush();
        } else {
            role = TestUtil.findAll(em, Role.class).get(0);
        }
        utilisateurRole.setRole(role);
        return utilisateurRole;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static UtilisateurRole createUpdatedEntity(EntityManager em) {
        UtilisateurRole updatedUtilisateurRole = new UtilisateurRole();
        // Add required entity
        Utilisateur utilisateur;
        if (TestUtil.findAll(em, Utilisateur.class).isEmpty()) {
            utilisateur = UtilisateurResourceIT.createUpdatedEntity();
            em.persist(utilisateur);
            em.flush();
        } else {
            utilisateur = TestUtil.findAll(em, Utilisateur.class).get(0);
        }
        updatedUtilisateurRole.setUtilisateur(utilisateur);
        // Add required entity
        Role role;
        if (TestUtil.findAll(em, Role.class).isEmpty()) {
            role = RoleResourceIT.createUpdatedEntity();
            em.persist(role);
            em.flush();
        } else {
            role = TestUtil.findAll(em, Role.class).get(0);
        }
        updatedUtilisateurRole.setRole(role);
        return updatedUtilisateurRole;
    }

    @BeforeEach
    void initTest() {
        utilisateurRole = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedUtilisateurRole != null) {
            utilisateurRoleRepository.delete(insertedUtilisateurRole);
            insertedUtilisateurRole = null;
        }
    }

    @Test
    @Transactional
    void createUtilisateurRole() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the UtilisateurRole
        UtilisateurRoleDTO utilisateurRoleDTO = utilisateurRoleMapper.toDto(utilisateurRole);
        var returnedUtilisateurRoleDTO = om.readValue(
            restUtilisateurRoleMockMvc
                .perform(
                    post(ENTITY_API_URL)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(utilisateurRoleDTO))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            UtilisateurRoleDTO.class
        );

        // Validate the UtilisateurRole in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedUtilisateurRole = utilisateurRoleMapper.toEntity(returnedUtilisateurRoleDTO);
        assertUtilisateurRoleUpdatableFieldsEquals(returnedUtilisateurRole, getPersistedUtilisateurRole(returnedUtilisateurRole));

        insertedUtilisateurRole = returnedUtilisateurRole;
    }

    @Test
    @Transactional
    void createUtilisateurRoleWithExistingId() throws Exception {
        // Create the UtilisateurRole with an existing ID
        insertedUtilisateurRole = utilisateurRoleRepository.saveAndFlush(utilisateurRole);
        UtilisateurRoleDTO utilisateurRoleDTO = utilisateurRoleMapper.toDto(utilisateurRole);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restUtilisateurRoleMockMvc
            .perform(
                post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(utilisateurRoleDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the UtilisateurRole in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void getAllUtilisateurRoles() throws Exception {
        // Initialize the database
        insertedUtilisateurRole = utilisateurRoleRepository.saveAndFlush(utilisateurRole);

        // Get all the utilisateurRoleList
        restUtilisateurRoleMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(utilisateurRole.getId().toString())));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllUtilisateurRolesWithEagerRelationshipsIsEnabled() throws Exception {
        when(utilisateurRoleServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restUtilisateurRoleMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(utilisateurRoleServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllUtilisateurRolesWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(utilisateurRoleServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restUtilisateurRoleMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(utilisateurRoleRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getUtilisateurRole() throws Exception {
        // Initialize the database
        insertedUtilisateurRole = utilisateurRoleRepository.saveAndFlush(utilisateurRole);

        // Get the utilisateurRole
        restUtilisateurRoleMockMvc
            .perform(get(ENTITY_API_URL_ID, utilisateurRole.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(utilisateurRole.getId().toString()));
    }

    @Test
    @Transactional
    void getUtilisateurRolesByIdFiltering() throws Exception {
        // Initialize the database
        insertedUtilisateurRole = utilisateurRoleRepository.saveAndFlush(utilisateurRole);

        UUID id = utilisateurRole.getId();

        defaultUtilisateurRoleFiltering("id.equals=" + id, "id.notEquals=" + id);
    }

    @Test
    @Transactional
    void getAllUtilisateurRolesByUtilisateurIsEqualToSomething() throws Exception {
        Utilisateur utilisateur;
        if (TestUtil.findAll(em, Utilisateur.class).isEmpty()) {
            utilisateurRoleRepository.saveAndFlush(utilisateurRole);
            utilisateur = UtilisateurResourceIT.createEntity();
        } else {
            utilisateur = TestUtil.findAll(em, Utilisateur.class).get(0);
        }
        em.persist(utilisateur);
        em.flush();
        utilisateurRole.setUtilisateur(utilisateur);
        utilisateurRoleRepository.saveAndFlush(utilisateurRole);
        UUID utilisateurId = utilisateur.getId();
        // Get all the utilisateurRoleList where utilisateur equals to utilisateurId
        defaultUtilisateurRoleShouldBeFound("utilisateurId.equals=" + utilisateurId);

        // Get all the utilisateurRoleList where utilisateur equals to UUID.randomUUID()
        defaultUtilisateurRoleShouldNotBeFound("utilisateurId.equals=" + UUID.randomUUID());
    }

    @Test
    @Transactional
    void getAllUtilisateurRolesByRoleIsEqualToSomething() throws Exception {
        Role role;
        if (TestUtil.findAll(em, Role.class).isEmpty()) {
            utilisateurRoleRepository.saveAndFlush(utilisateurRole);
            role = RoleResourceIT.createEntity();
        } else {
            role = TestUtil.findAll(em, Role.class).get(0);
        }
        em.persist(role);
        em.flush();
        utilisateurRole.setRole(role);
        utilisateurRoleRepository.saveAndFlush(utilisateurRole);
        UUID roleId = role.getId();
        // Get all the utilisateurRoleList where role equals to roleId
        defaultUtilisateurRoleShouldBeFound("roleId.equals=" + roleId);

        // Get all the utilisateurRoleList where role equals to UUID.randomUUID()
        defaultUtilisateurRoleShouldNotBeFound("roleId.equals=" + UUID.randomUUID());
    }

    private void defaultUtilisateurRoleFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultUtilisateurRoleShouldBeFound(shouldBeFound);
        defaultUtilisateurRoleShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultUtilisateurRoleShouldBeFound(String filter) throws Exception {
        restUtilisateurRoleMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(utilisateurRole.getId().toString())));

        // Check, that the count call also returns 1
        restUtilisateurRoleMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultUtilisateurRoleShouldNotBeFound(String filter) throws Exception {
        restUtilisateurRoleMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restUtilisateurRoleMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingUtilisateurRole() throws Exception {
        // Get the utilisateurRole
        restUtilisateurRoleMockMvc.perform(get(ENTITY_API_URL_ID, UUID.randomUUID().toString())).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingUtilisateurRole() throws Exception {
        // Initialize the database
        insertedUtilisateurRole = utilisateurRoleRepository.saveAndFlush(utilisateurRole);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the utilisateurRole
        UtilisateurRole updatedUtilisateurRole = utilisateurRoleRepository.findById(utilisateurRole.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedUtilisateurRole are not directly saved in db
        em.detach(updatedUtilisateurRole);
        UtilisateurRoleDTO utilisateurRoleDTO = utilisateurRoleMapper.toDto(updatedUtilisateurRole);

        restUtilisateurRoleMockMvc
            .perform(
                put(ENTITY_API_URL_ID, utilisateurRoleDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(utilisateurRoleDTO))
            )
            .andExpect(status().isOk());

        // Validate the UtilisateurRole in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedUtilisateurRoleToMatchAllProperties(updatedUtilisateurRole);
    }

    @Test
    @Transactional
    void putNonExistingUtilisateurRole() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        utilisateurRole.setId(UUID.randomUUID());

        // Create the UtilisateurRole
        UtilisateurRoleDTO utilisateurRoleDTO = utilisateurRoleMapper.toDto(utilisateurRole);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restUtilisateurRoleMockMvc
            .perform(
                put(ENTITY_API_URL_ID, utilisateurRoleDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(utilisateurRoleDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the UtilisateurRole in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchUtilisateurRole() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        utilisateurRole.setId(UUID.randomUUID());

        // Create the UtilisateurRole
        UtilisateurRoleDTO utilisateurRoleDTO = utilisateurRoleMapper.toDto(utilisateurRole);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restUtilisateurRoleMockMvc
            .perform(
                put(ENTITY_API_URL_ID, UUID.randomUUID())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(utilisateurRoleDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the UtilisateurRole in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamUtilisateurRole() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        utilisateurRole.setId(UUID.randomUUID());

        // Create the UtilisateurRole
        UtilisateurRoleDTO utilisateurRoleDTO = utilisateurRoleMapper.toDto(utilisateurRole);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restUtilisateurRoleMockMvc
            .perform(
                put(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(utilisateurRoleDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the UtilisateurRole in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateUtilisateurRoleWithPatch() throws Exception {
        // Initialize the database
        insertedUtilisateurRole = utilisateurRoleRepository.saveAndFlush(utilisateurRole);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the utilisateurRole using partial update
        UtilisateurRole partialUpdatedUtilisateurRole = new UtilisateurRole();
        partialUpdatedUtilisateurRole.setId(utilisateurRole.getId());

        restUtilisateurRoleMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedUtilisateurRole.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedUtilisateurRole))
            )
            .andExpect(status().isOk());

        // Validate the UtilisateurRole in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertUtilisateurRoleUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedUtilisateurRole, utilisateurRole),
            getPersistedUtilisateurRole(utilisateurRole)
        );
    }

    @Test
    @Transactional
    void fullUpdateUtilisateurRoleWithPatch() throws Exception {
        // Initialize the database
        insertedUtilisateurRole = utilisateurRoleRepository.saveAndFlush(utilisateurRole);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the utilisateurRole using partial update
        UtilisateurRole partialUpdatedUtilisateurRole = new UtilisateurRole();
        partialUpdatedUtilisateurRole.setId(utilisateurRole.getId());

        restUtilisateurRoleMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedUtilisateurRole.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedUtilisateurRole))
            )
            .andExpect(status().isOk());

        // Validate the UtilisateurRole in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertUtilisateurRoleUpdatableFieldsEquals(
            partialUpdatedUtilisateurRole,
            getPersistedUtilisateurRole(partialUpdatedUtilisateurRole)
        );
    }

    @Test
    @Transactional
    void patchNonExistingUtilisateurRole() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        utilisateurRole.setId(UUID.randomUUID());

        // Create the UtilisateurRole
        UtilisateurRoleDTO utilisateurRoleDTO = utilisateurRoleMapper.toDto(utilisateurRole);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restUtilisateurRoleMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, utilisateurRoleDTO.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(utilisateurRoleDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the UtilisateurRole in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchUtilisateurRole() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        utilisateurRole.setId(UUID.randomUUID());

        // Create the UtilisateurRole
        UtilisateurRoleDTO utilisateurRoleDTO = utilisateurRoleMapper.toDto(utilisateurRole);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restUtilisateurRoleMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, UUID.randomUUID())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(utilisateurRoleDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the UtilisateurRole in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamUtilisateurRole() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        utilisateurRole.setId(UUID.randomUUID());

        // Create the UtilisateurRole
        UtilisateurRoleDTO utilisateurRoleDTO = utilisateurRoleMapper.toDto(utilisateurRole);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restUtilisateurRoleMockMvc
            .perform(
                patch(ENTITY_API_URL)
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(utilisateurRoleDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the UtilisateurRole in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteUtilisateurRole() throws Exception {
        // Initialize the database
        insertedUtilisateurRole = utilisateurRoleRepository.saveAndFlush(utilisateurRole);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the utilisateurRole
        restUtilisateurRoleMockMvc
            .perform(delete(ENTITY_API_URL_ID, utilisateurRole.getId().toString()).with(csrf()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return utilisateurRoleRepository.count();
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

    protected UtilisateurRole getPersistedUtilisateurRole(UtilisateurRole utilisateurRole) {
        return utilisateurRoleRepository.findById(utilisateurRole.getId()).orElseThrow();
    }

    protected void assertPersistedUtilisateurRoleToMatchAllProperties(UtilisateurRole expectedUtilisateurRole) {
        assertUtilisateurRoleAllPropertiesEquals(expectedUtilisateurRole, getPersistedUtilisateurRole(expectedUtilisateurRole));
    }

    protected void assertPersistedUtilisateurRoleToMatchUpdatableProperties(UtilisateurRole expectedUtilisateurRole) {
        assertUtilisateurRoleAllUpdatablePropertiesEquals(expectedUtilisateurRole, getPersistedUtilisateurRole(expectedUtilisateurRole));
    }
}
