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
import sn.dove.backend.repository.ReactionRepository;
import sn.dove.backend.service.ReactionQueryService;
import sn.dove.backend.service.ReactionService;
import sn.dove.backend.service.criteria.ReactionCriteria;
import sn.dove.backend.service.dto.ReactionDTO;
import sn.dove.backend.web.rest.errors.BadRequestAlertException;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link sn.dove.backend.domain.Reaction}.
 */
@RestController
@RequestMapping("/api/reactions")
public class ReactionResource {

    private static final Logger LOG = LoggerFactory.getLogger(ReactionResource.class);

    private static final String ENTITY_NAME = "reaction";

    @Value("${jhipster.clientApp.name:doveBackend}")
    private String applicationName;

    private final ReactionService reactionService;

    private final ReactionRepository reactionRepository;

    private final ReactionQueryService reactionQueryService;

    public ReactionResource(
        ReactionService reactionService,
        ReactionRepository reactionRepository,
        ReactionQueryService reactionQueryService
    ) {
        this.reactionService = reactionService;
        this.reactionRepository = reactionRepository;
        this.reactionQueryService = reactionQueryService;
    }

    /**
     * {@code POST  /reactions} : Create a new reaction.
     *
     * @param reactionDTO the reactionDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new reactionDTO, or with status {@code 400 (Bad Request)} if the reaction has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<ReactionDTO> createReaction(@Valid @RequestBody ReactionDTO reactionDTO) throws URISyntaxException {
        LOG.debug("REST request to save Reaction : {}", reactionDTO);
        if (reactionDTO.getId() != null) {
            throw new BadRequestAlertException("A new reaction cannot already have an ID", ENTITY_NAME, "idexists");
        }
        reactionDTO = reactionService.save(reactionDTO);
        return ResponseEntity.created(new URI("/api/reactions/" + reactionDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, reactionDTO.getId().toString()))
            .body(reactionDTO);
    }

    /**
     * {@code PUT  /reactions/:id} : Updates an existing reaction.
     *
     * @param id the id of the reactionDTO to save.
     * @param reactionDTO the reactionDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated reactionDTO,
     * or with status {@code 400 (Bad Request)} if the reactionDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the reactionDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ReactionDTO> updateReaction(
        @PathVariable(value = "id", required = false) final UUID id,
        @Valid @RequestBody ReactionDTO reactionDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Reaction : {}, {}", id, reactionDTO);
        if (reactionDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, reactionDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!reactionRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        reactionDTO = reactionService.update(reactionDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, reactionDTO.getId().toString()))
            .body(reactionDTO);
    }

    /**
     * {@code PATCH  /reactions/:id} : Partial updates given fields of an existing reaction, field will ignore if it is null
     *
     * @param id the id of the reactionDTO to save.
     * @param reactionDTO the reactionDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated reactionDTO,
     * or with status {@code 400 (Bad Request)} if the reactionDTO is not valid,
     * or with status {@code 404 (Not Found)} if the reactionDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the reactionDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<ReactionDTO> partialUpdateReaction(
        @PathVariable(value = "id", required = false) final UUID id,
        @NotNull @RequestBody ReactionDTO reactionDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Reaction partially : {}, {}", id, reactionDTO);
        if (reactionDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, reactionDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!reactionRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<ReactionDTO> result = reactionService.partialUpdate(reactionDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, reactionDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /reactions} : get all the Reactions.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Reactions in body.
     */
    @GetMapping("")
    public ResponseEntity<List<ReactionDTO>> getAllReactions(
        ReactionCriteria criteria,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get Reactions by criteria: {}", criteria);

        Page<ReactionDTO> page = reactionQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /reactions/count} : count all the reactions.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countReactions(ReactionCriteria criteria) {
        LOG.debug("REST request to count Reactions by criteria: {}", criteria);
        return ResponseEntity.ok().body(reactionQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /reactions/:id} : get the "id" reaction.
     *
     * @param id the id of the reactionDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the reactionDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReactionDTO> getReaction(@PathVariable("id") UUID id) {
        LOG.debug("REST request to get Reaction : {}", id);
        Optional<ReactionDTO> reactionDTO = reactionService.findOne(id);
        return ResponseUtil.wrapOrNotFound(reactionDTO);
    }

    /**
     * {@code DELETE  /reactions/:id} : delete the "id" reaction.
     *
     * @param id the id of the reactionDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReaction(@PathVariable("id") UUID id) {
        LOG.debug("REST request to delete Reaction : {}", id);
        reactionService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
