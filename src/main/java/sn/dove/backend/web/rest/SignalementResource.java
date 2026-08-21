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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import sn.dove.backend.repository.SignalementRepository;
import sn.dove.backend.service.SignalementQueryService;
import sn.dove.backend.service.SignalementService;
import sn.dove.backend.service.criteria.SignalementCriteria;
import sn.dove.backend.service.dto.SignalementDTO;
import sn.dove.backend.web.rest.errors.BadRequestAlertException;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link sn.dove.backend.domain.Signalement}.
 */
@RestController
@RequestMapping("/api/signalements")
public class SignalementResource {

    private static final Logger LOG = LoggerFactory.getLogger(SignalementResource.class);

    private static final String ENTITY_NAME = "signalement";

    @Value("${jhipster.clientApp.name:doveBackend}")
    private String applicationName;

    private final SignalementService signalementService;

    private final SignalementRepository signalementRepository;

    private final SignalementQueryService signalementQueryService;

    public SignalementResource(
        SignalementService signalementService,
        SignalementRepository signalementRepository,
        SignalementQueryService signalementQueryService
    ) {
        this.signalementService = signalementService;
        this.signalementRepository = signalementRepository;
        this.signalementQueryService = signalementQueryService;
    }

    /**
     * {@code POST  /signalements} : Create a new signalement.
     *
     * @param signalementDTO the signalementDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new signalementDTO, or with status {@code 400 (Bad Request)} if the signalement has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<SignalementDTO> createSignalement(@Valid @RequestBody SignalementDTO signalementDTO) throws URISyntaxException {
        LOG.debug("REST request to save Signalement : {}", signalementDTO);
        if (signalementDTO.getId() != null) {
            throw new BadRequestAlertException("A new signalement cannot already have an ID", ENTITY_NAME, "idexists");
        }
        signalementDTO = signalementService.save(signalementDTO);
        return ResponseEntity.created(new URI("/api/signalements/" + signalementDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, signalementDTO.getId().toString()))
            .body(signalementDTO);
    }

    /**
     * {@code PUT  /signalements/:id} : Updates an existing signalement.
     *
     * @param id the id of the signalementDTO to save.
     * @param signalementDTO the signalementDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated signalementDTO,
     * or with status {@code 400 (Bad Request)} if the signalementDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the signalementDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<SignalementDTO> updateSignalement(
        @PathVariable(value = "id", required = false) final UUID id,
        @Valid @RequestBody SignalementDTO signalementDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Signalement : {}, {}", id, signalementDTO);
        if (signalementDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, signalementDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!signalementRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        signalementDTO = signalementService.update(signalementDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, signalementDTO.getId().toString()))
            .body(signalementDTO);
    }

    /**
     * {@code PATCH  /signalements/:id} : Partial updates given fields of an existing signalement, field will ignore if it is null
     *
     * @param id the id of the signalementDTO to save.
     * @param signalementDTO the signalementDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated signalementDTO,
     * or with status {@code 400 (Bad Request)} if the signalementDTO is not valid,
     * or with status {@code 404 (Not Found)} if the signalementDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the signalementDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<SignalementDTO> partialUpdateSignalement(
        @PathVariable(value = "id", required = false) final UUID id,
        @NotNull @RequestBody SignalementDTO signalementDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Signalement partially : {}, {}", id, signalementDTO);
        if (signalementDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, signalementDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!signalementRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<SignalementDTO> result = signalementService.partialUpdate(signalementDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, signalementDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /signalements} : get all the Signalements.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Signalements in body.
     */
    @GetMapping("")
    public ResponseEntity<List<SignalementDTO>> getAllSignalements(
        SignalementCriteria criteria,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get Signalements by criteria: {}", criteria);

        Page<SignalementDTO> page = signalementQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /signalements/count} : count all the signalements.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countSignalements(SignalementCriteria criteria) {
        LOG.debug("REST request to count Signalements by criteria: {}", criteria);
        return ResponseEntity.ok().body(signalementQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /signalements/:id} : get the "id" signalement.
     *
     * @param id the id of the signalementDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the signalementDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<SignalementDTO> getSignalement(@PathVariable("id") UUID id) {
        LOG.debug("REST request to get Signalement : {}", id);
        Optional<SignalementDTO> signalementDTO = signalementService.findOne(id);
        return ResponseUtil.wrapOrNotFound(signalementDTO);
    }

    /**
     * {@code DELETE  /signalements/:id} : delete the "id" signalement.
     *
     * @param id the id of the signalementDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSignalement(@PathVariable("id") UUID id) {
        LOG.debug("REST request to delete Signalement : {}", id);
        signalementService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
