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
import sn.dove.backend.repository.FeedBackRepository;
import sn.dove.backend.service.FeedBackQueryService;
import sn.dove.backend.service.FeedBackService;
import sn.dove.backend.service.criteria.FeedBackCriteria;
import sn.dove.backend.service.dto.FeedBackDTO;
import sn.dove.backend.web.rest.errors.BadRequestAlertException;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link sn.dove.backend.domain.FeedBack}.
 */
@RestController
@RequestMapping("/api/feed-backs")
public class FeedBackResource {

    private static final Logger LOG = LoggerFactory.getLogger(FeedBackResource.class);

    private static final String ENTITY_NAME = "feedBack";

    @Value("${jhipster.clientApp.name:doveBackend}")
    private String applicationName;

    private final FeedBackService feedBackService;

    private final FeedBackRepository feedBackRepository;

    private final FeedBackQueryService feedBackQueryService;

    public FeedBackResource(
        FeedBackService feedBackService,
        FeedBackRepository feedBackRepository,
        FeedBackQueryService feedBackQueryService
    ) {
        this.feedBackService = feedBackService;
        this.feedBackRepository = feedBackRepository;
        this.feedBackQueryService = feedBackQueryService;
    }

    /**
     * {@code POST  /feed-backs} : Create a new feedBack.
     *
     * @param feedBackDTO the feedBackDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new feedBackDTO, or with status {@code 400 (Bad Request)} if the feedBack has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<FeedBackDTO> createFeedBack(@Valid @RequestBody FeedBackDTO feedBackDTO) throws URISyntaxException {
        LOG.debug("REST request to save FeedBack : {}", feedBackDTO);
        if (feedBackDTO.getId() != null) {
            throw new BadRequestAlertException("A new feedBack cannot already have an ID", ENTITY_NAME, "idexists");
        }
        feedBackDTO = feedBackService.save(feedBackDTO);
        return ResponseEntity.created(new URI("/api/feed-backs/" + feedBackDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, feedBackDTO.getId().toString()))
            .body(feedBackDTO);
    }

    /**
     * {@code PUT  /feed-backs/:id} : Updates an existing feedBack.
     *
     * @param id the id of the feedBackDTO to save.
     * @param feedBackDTO the feedBackDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated feedBackDTO,
     * or with status {@code 400 (Bad Request)} if the feedBackDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the feedBackDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<FeedBackDTO> updateFeedBack(
        @PathVariable(value = "id", required = false) final UUID id,
        @Valid @RequestBody FeedBackDTO feedBackDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update FeedBack : {}, {}", id, feedBackDTO);
        if (feedBackDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, feedBackDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!feedBackRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        feedBackDTO = feedBackService.update(feedBackDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, feedBackDTO.getId().toString()))
            .body(feedBackDTO);
    }

    /**
     * {@code PATCH  /feed-backs/:id} : Partial updates given fields of an existing feedBack, field will ignore if it is null
     *
     * @param id the id of the feedBackDTO to save.
     * @param feedBackDTO the feedBackDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated feedBackDTO,
     * or with status {@code 400 (Bad Request)} if the feedBackDTO is not valid,
     * or with status {@code 404 (Not Found)} if the feedBackDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the feedBackDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<FeedBackDTO> partialUpdateFeedBack(
        @PathVariable(value = "id", required = false) final UUID id,
        @NotNull @RequestBody FeedBackDTO feedBackDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update FeedBack partially : {}, {}", id, feedBackDTO);
        if (feedBackDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, feedBackDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!feedBackRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<FeedBackDTO> result = feedBackService.partialUpdate(feedBackDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, feedBackDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /feed-backs} : get all the Feed Backs.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Feed Backs in body.
     */
    @GetMapping("")
    public ResponseEntity<List<FeedBackDTO>> getAllFeedBacks(
        FeedBackCriteria criteria,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get FeedBacks by criteria: {}", criteria);

        Page<FeedBackDTO> page = feedBackQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /feed-backs/count} : count all the feedBacks.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countFeedBacks(FeedBackCriteria criteria) {
        LOG.debug("REST request to count FeedBacks by criteria: {}", criteria);
        return ResponseEntity.ok().body(feedBackQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /feed-backs/:id} : get the "id" feedBack.
     *
     * @param id the id of the feedBackDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the feedBackDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<FeedBackDTO> getFeedBack(@PathVariable("id") UUID id) {
        LOG.debug("REST request to get FeedBack : {}", id);
        Optional<FeedBackDTO> feedBackDTO = feedBackService.findOne(id);
        return ResponseUtil.wrapOrNotFound(feedBackDTO);
    }

    /**
     * {@code DELETE  /feed-backs/:id} : delete the "id" feedBack.
     *
     * @param id the id of the feedBackDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFeedBack(@PathVariable("id") UUID id) {
        LOG.debug("REST request to delete FeedBack : {}", id);
        feedBackService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
