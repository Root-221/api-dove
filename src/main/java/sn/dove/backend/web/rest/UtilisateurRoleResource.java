package sn.dove.backend.web.rest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.dove.backend.repository.UtilisateurRoleRepository;
import sn.dove.backend.service.UtilisateurRoleQueryService;
import sn.dove.backend.service.UtilisateurRoleService;
import sn.dove.backend.service.criteria.UtilisateurRoleCriteria;
import sn.dove.backend.service.dto.UtilisateurRoleDTO;
import sn.dove.backend.web.rest.errors.BadRequestAlertException;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link sn.dove.backend.domain.UtilisateurRole}.
 */
@RestController
@RequestMapping("/api/utilisateur-roles")
public class UtilisateurRoleResource {

    private static final Logger LOG = LoggerFactory.getLogger(UtilisateurRoleResource.class);

    private static final String ENTITY_NAME = "utilisateurRole";

    @Value("${jhipster.clientApp.name:doveBackend}")
    private String applicationName;

    private final UtilisateurRoleService utilisateurRoleService;

    private final UtilisateurRoleRepository utilisateurRoleRepository;

    private final UtilisateurRoleQueryService utilisateurRoleQueryService;

    public UtilisateurRoleResource(
        UtilisateurRoleService utilisateurRoleService,
        UtilisateurRoleRepository utilisateurRoleRepository,
        UtilisateurRoleQueryService utilisateurRoleQueryService
    ) {
        this.utilisateurRoleService = utilisateurRoleService;
        this.utilisateurRoleRepository = utilisateurRoleRepository;
        this.utilisateurRoleQueryService = utilisateurRoleQueryService;
    }

    /**
     * {@code POST  /utilisateur-roles} : Create a new utilisateurRole.
     *
     * @param utilisateurRoleDTO the utilisateurRoleDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new utilisateurRoleDTO, or with status {@code 400 (Bad Request)} if the utilisateurRole has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<UtilisateurRoleDTO> createUtilisateurRole(@Valid @RequestBody UtilisateurRoleDTO utilisateurRoleDTO)
        throws URISyntaxException {
        LOG.debug("REST request to save UtilisateurRole : {}", utilisateurRoleDTO);
        if (utilisateurRoleDTO.getId() != null) {
            throw new BadRequestAlertException("A new utilisateurRole cannot already have an ID", ENTITY_NAME, "idexists");
        }
        utilisateurRoleDTO = utilisateurRoleService.save(utilisateurRoleDTO);
        return ResponseEntity.created(new URI("/api/utilisateur-roles/" + utilisateurRoleDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, utilisateurRoleDTO.getId().toString()))
            .body(utilisateurRoleDTO);
    }

    /**
     * {@code PUT  /utilisateur-roles/:id} : Updates an existing utilisateurRole.
     *
     * @param id the id of the utilisateurRoleDTO to save.
     * @param utilisateurRoleDTO the utilisateurRoleDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated utilisateurRoleDTO,
     * or with status {@code 400 (Bad Request)} if the utilisateurRoleDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the utilisateurRoleDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<UtilisateurRoleDTO> updateUtilisateurRole(
        @PathVariable(value = "id", required = false) final UUID id,
        @Valid @RequestBody UtilisateurRoleDTO utilisateurRoleDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update UtilisateurRole : {}, {}", id, utilisateurRoleDTO);
        if (utilisateurRoleDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, utilisateurRoleDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!utilisateurRoleRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        utilisateurRoleDTO = utilisateurRoleService.update(utilisateurRoleDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, utilisateurRoleDTO.getId().toString()))
            .body(utilisateurRoleDTO);
    }

    /**
     * {@code PATCH  /utilisateur-roles/:id} : Partial updates given fields of an existing utilisateurRole, field will ignore if it is null
     *
     * @param id the id of the utilisateurRoleDTO to save.
     * @param utilisateurRoleDTO the utilisateurRoleDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated utilisateurRoleDTO,
     * or with status {@code 400 (Bad Request)} if the utilisateurRoleDTO is not valid,
     * or with status {@code 404 (Not Found)} if the utilisateurRoleDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the utilisateurRoleDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<UtilisateurRoleDTO> partialUpdateUtilisateurRole(
        @PathVariable(value = "id", required = false) final UUID id,
        @NotNull @RequestBody UtilisateurRoleDTO utilisateurRoleDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update UtilisateurRole partially : {}, {}", id, utilisateurRoleDTO);
        if (utilisateurRoleDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, utilisateurRoleDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!utilisateurRoleRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<UtilisateurRoleDTO> result = utilisateurRoleService.partialUpdate(utilisateurRoleDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, utilisateurRoleDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /utilisateur-roles} : get all the Utilisateur Roles.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Utilisateur Roles in body.
     */
    @GetMapping("")
    public ResponseEntity<List<UtilisateurRoleDTO>> getAllUtilisateurRoles(UtilisateurRoleCriteria criteria) {
        LOG.debug("REST request to get UtilisateurRoles by criteria: {}", criteria);

        List<UtilisateurRoleDTO> entityList = utilisateurRoleQueryService.findByCriteria(criteria);
        return ResponseEntity.ok().body(entityList);
    }

    /**
     * {@code GET  /utilisateur-roles/count} : count all the utilisateurRoles.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countUtilisateurRoles(UtilisateurRoleCriteria criteria) {
        LOG.debug("REST request to count UtilisateurRoles by criteria: {}", criteria);
        return ResponseEntity.ok().body(utilisateurRoleQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /utilisateur-roles/:id} : get the "id" utilisateurRole.
     *
     * @param id the id of the utilisateurRoleDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the utilisateurRoleDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<UtilisateurRoleDTO> getUtilisateurRole(@PathVariable("id") UUID id) {
        LOG.debug("REST request to get UtilisateurRole : {}", id);
        Optional<UtilisateurRoleDTO> utilisateurRoleDTO = utilisateurRoleService.findOne(id);
        return ResponseUtil.wrapOrNotFound(utilisateurRoleDTO);
    }

    /**
     * {@code DELETE  /utilisateur-roles/:id} : delete the "id" utilisateurRole.
     *
     * @param id the id of the utilisateurRoleDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUtilisateurRole(@PathVariable("id") UUID id) {
        LOG.debug("REST request to delete UtilisateurRole : {}", id);
        utilisateurRoleService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
