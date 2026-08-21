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
import sn.dove.backend.repository.FAQRepository;
import sn.dove.backend.service.FAQQueryService;
import sn.dove.backend.service.FAQService;
import sn.dove.backend.service.criteria.FAQCriteria;
import sn.dove.backend.service.dto.FAQDTO;
import sn.dove.backend.web.rest.errors.BadRequestAlertException;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link sn.dove.backend.domain.FAQ}.
 */
@RestController
@RequestMapping("/api/faqs")
public class FAQResource {

    private static final Logger LOG = LoggerFactory.getLogger(FAQResource.class);

    private static final String ENTITY_NAME = "fAQ";

    @Value("${jhipster.clientApp.name:doveBackend}")
    private String applicationName;

    private final FAQService fAQService;

    private final FAQRepository fAQRepository;

    private final FAQQueryService fAQQueryService;

    public FAQResource(FAQService fAQService, FAQRepository fAQRepository, FAQQueryService fAQQueryService) {
        this.fAQService = fAQService;
        this.fAQRepository = fAQRepository;
        this.fAQQueryService = fAQQueryService;
    }

    /**
     * {@code POST  /faqs} : Create a new fAQ.
     *
     * @param fAQDTO the fAQDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new fAQDTO, or with status {@code 400 (Bad Request)} if the fAQ has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<FAQDTO> createFAQ(@Valid @RequestBody FAQDTO fAQDTO) throws URISyntaxException {
        LOG.debug("REST request to save FAQ : {}", fAQDTO);
        if (fAQDTO.getId() != null) {
            throw new BadRequestAlertException("A new fAQ cannot already have an ID", ENTITY_NAME, "idexists");
        }
        fAQDTO = fAQService.save(fAQDTO);
        return ResponseEntity.created(new URI("/api/faqs/" + fAQDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, fAQDTO.getId().toString()))
            .body(fAQDTO);
    }

    /**
     * {@code PUT  /faqs/:id} : Updates an existing fAQ.
     *
     * @param id the id of the fAQDTO to save.
     * @param fAQDTO the fAQDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated fAQDTO,
     * or with status {@code 400 (Bad Request)} if the fAQDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the fAQDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<FAQDTO> updateFAQ(@PathVariable(value = "id", required = false) final UUID id, @Valid @RequestBody FAQDTO fAQDTO)
        throws URISyntaxException {
        LOG.debug("REST request to update FAQ : {}, {}", id, fAQDTO);
        if (fAQDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, fAQDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!fAQRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        fAQDTO = fAQService.update(fAQDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, fAQDTO.getId().toString()))
            .body(fAQDTO);
    }

    /**
     * {@code PATCH  /faqs/:id} : Partial updates given fields of an existing fAQ, field will ignore if it is null
     *
     * @param id the id of the fAQDTO to save.
     * @param fAQDTO the fAQDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated fAQDTO,
     * or with status {@code 400 (Bad Request)} if the fAQDTO is not valid,
     * or with status {@code 404 (Not Found)} if the fAQDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the fAQDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<FAQDTO> partialUpdateFAQ(
        @PathVariable(value = "id", required = false) final UUID id,
        @NotNull @RequestBody FAQDTO fAQDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update FAQ partially : {}, {}", id, fAQDTO);
        if (fAQDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, fAQDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!fAQRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<FAQDTO> result = fAQService.partialUpdate(fAQDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, fAQDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /faqs} : get all the FAQS.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of FAQS in body.
     */
    @GetMapping("")
    public ResponseEntity<List<FAQDTO>> getAllFAQS(
        FAQCriteria criteria,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get FAQS by criteria: {}", criteria);

        Page<FAQDTO> page = fAQQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /faqs/count} : count all the fAQS.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countFAQS(FAQCriteria criteria) {
        LOG.debug("REST request to count FAQS by criteria: {}", criteria);
        return ResponseEntity.ok().body(fAQQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /faqs/:id} : get the "id" fAQ.
     *
     * @param id the id of the fAQDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the fAQDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<FAQDTO> getFAQ(@PathVariable("id") UUID id) {
        LOG.debug("REST request to get FAQ : {}", id);
        Optional<FAQDTO> fAQDTO = fAQService.findOne(id);
        return ResponseUtil.wrapOrNotFound(fAQDTO);
    }

    /**
     * {@code DELETE  /faqs/:id} : delete the "id" fAQ.
     *
     * @param id the id of the fAQDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFAQ(@PathVariable("id") UUID id) {
        LOG.debug("REST request to delete FAQ : {}", id);
        fAQService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
