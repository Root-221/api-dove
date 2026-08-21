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
import sn.dove.backend.repository.MetierRepository;
import sn.dove.backend.service.MetierQueryService;
import sn.dove.backend.service.MetierService;
import sn.dove.backend.service.criteria.MetierCriteria;
import sn.dove.backend.service.dto.MetierDTO;
import sn.dove.backend.web.rest.errors.BadRequestAlertException;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link sn.dove.backend.domain.Metier}.
 */
@RestController
@RequestMapping("/api/metiers")
public class MetierResource {

    private static final Logger LOG = LoggerFactory.getLogger(MetierResource.class);

    private static final String ENTITY_NAME = "metier";

    @Value("${jhipster.clientApp.name:doveBackend}")
    private String applicationName;

    private final MetierService metierService;

    private final MetierRepository metierRepository;

    private final MetierQueryService metierQueryService;

    public MetierResource(MetierService metierService, MetierRepository metierRepository, MetierQueryService metierQueryService) {
        this.metierService = metierService;
        this.metierRepository = metierRepository;
        this.metierQueryService = metierQueryService;
    }

    /**
     * {@code POST  /metiers} : Create a new metier.
     *
     * @param metierDTO the metierDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new metierDTO, or with status {@code 400 (Bad Request)} if the metier has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<MetierDTO> createMetier(@Valid @RequestBody MetierDTO metierDTO) throws URISyntaxException {
        LOG.debug("REST request to save Metier : {}", metierDTO);
        if (metierDTO.getId() != null) {
            throw new BadRequestAlertException("A new metier cannot already have an ID", ENTITY_NAME, "idexists");
        }
        metierDTO = metierService.save(metierDTO);
        return ResponseEntity.created(new URI("/api/metiers/" + metierDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, metierDTO.getId().toString()))
            .body(metierDTO);
    }

    /**
     * {@code PUT  /metiers/:id} : Updates an existing metier.
     *
     * @param id the id of the metierDTO to save.
     * @param metierDTO the metierDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated metierDTO,
     * or with status {@code 400 (Bad Request)} if the metierDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the metierDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<MetierDTO> updateMetier(
        @PathVariable(value = "id", required = false) final UUID id,
        @Valid @RequestBody MetierDTO metierDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Metier : {}, {}", id, metierDTO);
        if (metierDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, metierDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!metierRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        metierDTO = metierService.update(metierDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, metierDTO.getId().toString()))
            .body(metierDTO);
    }

    /**
     * {@code PATCH  /metiers/:id} : Partial updates given fields of an existing metier, field will ignore if it is null
     *
     * @param id the id of the metierDTO to save.
     * @param metierDTO the metierDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated metierDTO,
     * or with status {@code 400 (Bad Request)} if the metierDTO is not valid,
     * or with status {@code 404 (Not Found)} if the metierDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the metierDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<MetierDTO> partialUpdateMetier(
        @PathVariable(value = "id", required = false) final UUID id,
        @NotNull @RequestBody MetierDTO metierDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Metier partially : {}, {}", id, metierDTO);
        if (metierDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, metierDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!metierRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<MetierDTO> result = metierService.partialUpdate(metierDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, metierDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /metiers} : get all the Metiers.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Metiers in body.
     */
    @GetMapping("")
    public ResponseEntity<List<MetierDTO>> getAllMetiers(MetierCriteria criteria) {
        LOG.debug("REST request to get Metiers by criteria: {}", criteria);

        List<MetierDTO> entityList = metierQueryService.findByCriteria(criteria);
        return ResponseEntity.ok().body(entityList);
    }

    /**
     * {@code GET  /metiers/count} : count all the metiers.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countMetiers(MetierCriteria criteria) {
        LOG.debug("REST request to count Metiers by criteria: {}", criteria);
        return ResponseEntity.ok().body(metierQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /metiers/:id} : get the "id" metier.
     *
     * @param id the id of the metierDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the metierDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<MetierDTO> getMetier(@PathVariable("id") UUID id) {
        LOG.debug("REST request to get Metier : {}", id);
        Optional<MetierDTO> metierDTO = metierService.findOne(id);
        return ResponseUtil.wrapOrNotFound(metierDTO);
    }

    /**
     * {@code DELETE  /metiers/:id} : delete the "id" metier.
     *
     * @param id the id of the metierDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMetier(@PathVariable("id") UUID id) {
        LOG.debug("REST request to delete Metier : {}", id);
        metierService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
