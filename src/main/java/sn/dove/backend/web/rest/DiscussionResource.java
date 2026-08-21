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
import sn.dove.backend.repository.DiscussionRepository;
import sn.dove.backend.service.DiscussionQueryService;
import sn.dove.backend.service.DiscussionService;
import sn.dove.backend.service.criteria.DiscussionCriteria;
import sn.dove.backend.service.dto.DiscussionDTO;
import sn.dove.backend.web.rest.errors.BadRequestAlertException;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link sn.dove.backend.domain.Discussion}.
 */
@RestController
@RequestMapping("/api/discussions")
public class DiscussionResource {

    private static final Logger LOG = LoggerFactory.getLogger(DiscussionResource.class);

    private static final String ENTITY_NAME = "discussion";

    @Value("${jhipster.clientApp.name:doveBackend}")
    private String applicationName;

    private final DiscussionService discussionService;

    private final DiscussionRepository discussionRepository;

    private final DiscussionQueryService discussionQueryService;

    public DiscussionResource(
        DiscussionService discussionService,
        DiscussionRepository discussionRepository,
        DiscussionQueryService discussionQueryService
    ) {
        this.discussionService = discussionService;
        this.discussionRepository = discussionRepository;
        this.discussionQueryService = discussionQueryService;
    }

    /**
     * {@code POST  /discussions} : Create a new discussion.
     *
     * @param discussionDTO the discussionDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new discussionDTO, or with status {@code 400 (Bad Request)} if the discussion has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<DiscussionDTO> createDiscussion(@Valid @RequestBody DiscussionDTO discussionDTO) throws URISyntaxException {
        LOG.debug("REST request to save Discussion : {}", discussionDTO);
        if (discussionDTO.getId() != null) {
            throw new BadRequestAlertException("A new discussion cannot already have an ID", ENTITY_NAME, "idexists");
        }
        discussionDTO = discussionService.save(discussionDTO);
        return ResponseEntity.created(new URI("/api/discussions/" + discussionDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, discussionDTO.getId().toString()))
            .body(discussionDTO);
    }

    /**
     * {@code PUT  /discussions/:id} : Updates an existing discussion.
     *
     * @param id the id of the discussionDTO to save.
     * @param discussionDTO the discussionDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated discussionDTO,
     * or with status {@code 400 (Bad Request)} if the discussionDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the discussionDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<DiscussionDTO> updateDiscussion(
        @PathVariable(value = "id", required = false) final UUID id,
        @Valid @RequestBody DiscussionDTO discussionDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Discussion : {}, {}", id, discussionDTO);
        if (discussionDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, discussionDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!discussionRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        discussionDTO = discussionService.update(discussionDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, discussionDTO.getId().toString()))
            .body(discussionDTO);
    }

    /**
     * {@code PATCH  /discussions/:id} : Partial updates given fields of an existing discussion, field will ignore if it is null
     *
     * @param id the id of the discussionDTO to save.
     * @param discussionDTO the discussionDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated discussionDTO,
     * or with status {@code 400 (Bad Request)} if the discussionDTO is not valid,
     * or with status {@code 404 (Not Found)} if the discussionDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the discussionDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<DiscussionDTO> partialUpdateDiscussion(
        @PathVariable(value = "id", required = false) final UUID id,
        @NotNull @RequestBody DiscussionDTO discussionDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Discussion partially : {}, {}", id, discussionDTO);
        if (discussionDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, discussionDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!discussionRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<DiscussionDTO> result = discussionService.partialUpdate(discussionDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, discussionDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /discussions} : get all the Discussions.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Discussions in body.
     */
    @GetMapping("")
    public ResponseEntity<List<DiscussionDTO>> getAllDiscussions(
        DiscussionCriteria criteria,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get Discussions by criteria: {}", criteria);

        Page<DiscussionDTO> page = discussionQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /discussions/count} : count all the discussions.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countDiscussions(DiscussionCriteria criteria) {
        LOG.debug("REST request to count Discussions by criteria: {}", criteria);
        return ResponseEntity.ok().body(discussionQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /discussions/:id} : get the "id" discussion.
     *
     * @param id the id of the discussionDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the discussionDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<DiscussionDTO> getDiscussion(@PathVariable("id") UUID id) {
        LOG.debug("REST request to get Discussion : {}", id);
        Optional<DiscussionDTO> discussionDTO = discussionService.findOne(id);
        return ResponseUtil.wrapOrNotFound(discussionDTO);
    }

    /**
     * {@code DELETE  /discussions/:id} : delete the "id" discussion.
     *
     * @param id the id of the discussionDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDiscussion(@PathVariable("id") UUID id) {
        LOG.debug("REST request to delete Discussion : {}", id);
        discussionService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
