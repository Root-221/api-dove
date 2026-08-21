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
import sn.dove.backend.repository.CommunauteNanditeRepository;
import sn.dove.backend.service.CommunauteNanditeQueryService;
import sn.dove.backend.service.CommunauteNanditeService;
import sn.dove.backend.service.criteria.CommunauteNanditeCriteria;
import sn.dove.backend.service.dto.CommunauteNanditeDTO;
import sn.dove.backend.web.rest.errors.BadRequestAlertException;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link sn.dove.backend.domain.CommunauteNandite}.
 */
@RestController
@RequestMapping("/api/communaute-nandites")
public class CommunauteNanditeResource {

    private static final Logger LOG = LoggerFactory.getLogger(CommunauteNanditeResource.class);

    private static final String ENTITY_NAME = "communauteNandite";

    @Value("${jhipster.clientApp.name:doveBackend}")
    private String applicationName;

    private final CommunauteNanditeService communauteNanditeService;

    private final CommunauteNanditeRepository communauteNanditeRepository;

    private final CommunauteNanditeQueryService communauteNanditeQueryService;

    public CommunauteNanditeResource(
        CommunauteNanditeService communauteNanditeService,
        CommunauteNanditeRepository communauteNanditeRepository,
        CommunauteNanditeQueryService communauteNanditeQueryService
    ) {
        this.communauteNanditeService = communauteNanditeService;
        this.communauteNanditeRepository = communauteNanditeRepository;
        this.communauteNanditeQueryService = communauteNanditeQueryService;
    }

    /**
     * {@code POST  /communaute-nandites} : Create a new communauteNandite.
     *
     * @param communauteNanditeDTO the communauteNanditeDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new communauteNanditeDTO, or with status {@code 400 (Bad Request)} if the communauteNandite has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<CommunauteNanditeDTO> createCommunauteNandite(@Valid @RequestBody CommunauteNanditeDTO communauteNanditeDTO)
        throws URISyntaxException {
        LOG.debug("REST request to save CommunauteNandite : {}", communauteNanditeDTO);
        if (communauteNanditeDTO.getId() != null) {
            throw new BadRequestAlertException("A new communauteNandite cannot already have an ID", ENTITY_NAME, "idexists");
        }
        communauteNanditeDTO = communauteNanditeService.save(communauteNanditeDTO);
        return ResponseEntity.created(new URI("/api/communaute-nandites/" + communauteNanditeDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, communauteNanditeDTO.getId().toString()))
            .body(communauteNanditeDTO);
    }

    /**
     * {@code PUT  /communaute-nandites/:id} : Updates an existing communauteNandite.
     *
     * @param id the id of the communauteNanditeDTO to save.
     * @param communauteNanditeDTO the communauteNanditeDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated communauteNanditeDTO,
     * or with status {@code 400 (Bad Request)} if the communauteNanditeDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the communauteNanditeDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<CommunauteNanditeDTO> updateCommunauteNandite(
        @PathVariable(value = "id", required = false) final UUID id,
        @Valid @RequestBody CommunauteNanditeDTO communauteNanditeDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update CommunauteNandite : {}, {}", id, communauteNanditeDTO);
        if (communauteNanditeDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, communauteNanditeDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!communauteNanditeRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        communauteNanditeDTO = communauteNanditeService.update(communauteNanditeDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, communauteNanditeDTO.getId().toString()))
            .body(communauteNanditeDTO);
    }

    /**
     * {@code PATCH  /communaute-nandites/:id} : Partial updates given fields of an existing communauteNandite, field will ignore if it is null
     *
     * @param id the id of the communauteNanditeDTO to save.
     * @param communauteNanditeDTO the communauteNanditeDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated communauteNanditeDTO,
     * or with status {@code 400 (Bad Request)} if the communauteNanditeDTO is not valid,
     * or with status {@code 404 (Not Found)} if the communauteNanditeDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the communauteNanditeDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<CommunauteNanditeDTO> partialUpdateCommunauteNandite(
        @PathVariable(value = "id", required = false) final UUID id,
        @NotNull @RequestBody CommunauteNanditeDTO communauteNanditeDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update CommunauteNandite partially : {}, {}", id, communauteNanditeDTO);
        if (communauteNanditeDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, communauteNanditeDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!communauteNanditeRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<CommunauteNanditeDTO> result = communauteNanditeService.partialUpdate(communauteNanditeDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, communauteNanditeDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /communaute-nandites} : get all the Communaute Nandites.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Communaute Nandites in body.
     */
    @GetMapping("")
    public ResponseEntity<List<CommunauteNanditeDTO>> getAllCommunauteNandites(CommunauteNanditeCriteria criteria) {
        LOG.debug("REST request to get CommunauteNandites by criteria: {}", criteria);

        List<CommunauteNanditeDTO> entityList = communauteNanditeQueryService.findByCriteria(criteria);
        return ResponseEntity.ok().body(entityList);
    }

    /**
     * {@code GET  /communaute-nandites/count} : count all the communauteNandites.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countCommunauteNandites(CommunauteNanditeCriteria criteria) {
        LOG.debug("REST request to count CommunauteNandites by criteria: {}", criteria);
        return ResponseEntity.ok().body(communauteNanditeQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /communaute-nandites/:id} : get the "id" communauteNandite.
     *
     * @param id the id of the communauteNanditeDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the communauteNanditeDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<CommunauteNanditeDTO> getCommunauteNandite(@PathVariable("id") UUID id) {
        LOG.debug("REST request to get CommunauteNandite : {}", id);
        Optional<CommunauteNanditeDTO> communauteNanditeDTO = communauteNanditeService.findOne(id);
        return ResponseUtil.wrapOrNotFound(communauteNanditeDTO);
    }

    /**
     * {@code DELETE  /communaute-nandites/:id} : delete the "id" communauteNandite.
     *
     * @param id the id of the communauteNanditeDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCommunauteNandite(@PathVariable("id") UUID id) {
        LOG.debug("REST request to delete CommunauteNandite : {}", id);
        communauteNanditeService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
