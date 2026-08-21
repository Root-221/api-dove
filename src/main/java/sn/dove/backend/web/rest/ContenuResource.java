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
import sn.dove.backend.repository.ContenuRepository;
import sn.dove.backend.service.ContenuQueryService;
import sn.dove.backend.service.ContenuService;
import sn.dove.backend.service.criteria.ContenuCriteria;
import sn.dove.backend.service.dto.ContenuDTO;
import sn.dove.backend.web.rest.errors.BadRequestAlertException;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link sn.dove.backend.domain.Contenu}.
 */
@RestController
@RequestMapping("/api/contenus")
public class ContenuResource {

    private static final Logger LOG = LoggerFactory.getLogger(ContenuResource.class);

    private static final String ENTITY_NAME = "contenu";

    @Value("${jhipster.clientApp.name:doveBackend}")
    private String applicationName;

    private final ContenuService contenuService;

    private final ContenuRepository contenuRepository;

    private final ContenuQueryService contenuQueryService;

    public ContenuResource(ContenuService contenuService, ContenuRepository contenuRepository, ContenuQueryService contenuQueryService) {
        this.contenuService = contenuService;
        this.contenuRepository = contenuRepository;
        this.contenuQueryService = contenuQueryService;
    }

    /**
     * {@code POST  /contenus} : Create a new contenu.
     *
     * @param contenuDTO the contenuDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new contenuDTO, or with status {@code 400 (Bad Request)} if the contenu has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<ContenuDTO> createContenu(@Valid @RequestBody ContenuDTO contenuDTO) throws URISyntaxException {
        LOG.debug("REST request to save Contenu : {}", contenuDTO);
        if (contenuDTO.getId() != null) {
            throw new BadRequestAlertException("A new contenu cannot already have an ID", ENTITY_NAME, "idexists");
        }
        contenuDTO = contenuService.save(contenuDTO);
        return ResponseEntity.created(new URI("/api/contenus/" + contenuDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, contenuDTO.getId().toString()))
            .body(contenuDTO);
    }

    /**
     * {@code PUT  /contenus/:id} : Updates an existing contenu.
     *
     * @param id the id of the contenuDTO to save.
     * @param contenuDTO the contenuDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated contenuDTO,
     * or with status {@code 400 (Bad Request)} if the contenuDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the contenuDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ContenuDTO> updateContenu(
        @PathVariable(value = "id", required = false) final UUID id,
        @Valid @RequestBody ContenuDTO contenuDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Contenu : {}, {}", id, contenuDTO);
        if (contenuDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, contenuDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!contenuRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        contenuDTO = contenuService.update(contenuDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, contenuDTO.getId().toString()))
            .body(contenuDTO);
    }

    /**
     * {@code PATCH  /contenus/:id} : Partial updates given fields of an existing contenu, field will ignore if it is null
     *
     * @param id the id of the contenuDTO to save.
     * @param contenuDTO the contenuDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated contenuDTO,
     * or with status {@code 400 (Bad Request)} if the contenuDTO is not valid,
     * or with status {@code 404 (Not Found)} if the contenuDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the contenuDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<ContenuDTO> partialUpdateContenu(
        @PathVariable(value = "id", required = false) final UUID id,
        @NotNull @RequestBody ContenuDTO contenuDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Contenu partially : {}, {}", id, contenuDTO);
        if (contenuDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, contenuDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!contenuRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<ContenuDTO> result = contenuService.partialUpdate(contenuDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, contenuDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /contenus} : get all the Contenus.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Contenus in body.
     */
    @GetMapping("")
    public ResponseEntity<List<ContenuDTO>> getAllContenus(
        ContenuCriteria criteria,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get Contenus by criteria: {}", criteria);

        Page<ContenuDTO> page = contenuQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /contenus/count} : count all the contenus.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countContenus(ContenuCriteria criteria) {
        LOG.debug("REST request to count Contenus by criteria: {}", criteria);
        return ResponseEntity.ok().body(contenuQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /contenus/:id} : get the "id" contenu.
     *
     * @param id the id of the contenuDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the contenuDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ContenuDTO> getContenu(@PathVariable("id") UUID id) {
        LOG.debug("REST request to get Contenu : {}", id);
        Optional<ContenuDTO> contenuDTO = contenuService.findOne(id);
        return ResponseUtil.wrapOrNotFound(contenuDTO);
    }

    /**
     * {@code DELETE  /contenus/:id} : delete the "id" contenu.
     *
     * @param id the id of the contenuDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContenu(@PathVariable("id") UUID id) {
        LOG.debug("REST request to delete Contenu : {}", id);
        contenuService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
