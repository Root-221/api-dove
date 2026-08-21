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
import sn.dove.backend.repository.SignalementMessageRepository;
import sn.dove.backend.service.SignalementMessageQueryService;
import sn.dove.backend.service.SignalementMessageService;
import sn.dove.backend.service.criteria.SignalementMessageCriteria;
import sn.dove.backend.service.dto.SignalementMessageDTO;
import sn.dove.backend.web.rest.errors.BadRequestAlertException;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link sn.dove.backend.domain.SignalementMessage}.
 */
@RestController
@RequestMapping("/api/signalement-messages")
public class SignalementMessageResource {

    private static final Logger LOG = LoggerFactory.getLogger(SignalementMessageResource.class);

    private static final String ENTITY_NAME = "signalementMessage";

    @Value("${jhipster.clientApp.name:doveBackend}")
    private String applicationName;

    private final SignalementMessageService signalementMessageService;

    private final SignalementMessageRepository signalementMessageRepository;

    private final SignalementMessageQueryService signalementMessageQueryService;

    public SignalementMessageResource(
        SignalementMessageService signalementMessageService,
        SignalementMessageRepository signalementMessageRepository,
        SignalementMessageQueryService signalementMessageQueryService
    ) {
        this.signalementMessageService = signalementMessageService;
        this.signalementMessageRepository = signalementMessageRepository;
        this.signalementMessageQueryService = signalementMessageQueryService;
    }

    /**
     * {@code POST  /signalement-messages} : Create a new signalementMessage.
     *
     * @param signalementMessageDTO the signalementMessageDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new signalementMessageDTO, or with status {@code 400 (Bad Request)} if the signalementMessage has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<SignalementMessageDTO> createSignalementMessage(@Valid @RequestBody SignalementMessageDTO signalementMessageDTO)
        throws URISyntaxException {
        LOG.debug("REST request to save SignalementMessage : {}", signalementMessageDTO);
        if (signalementMessageDTO.getId() != null) {
            throw new BadRequestAlertException("A new signalementMessage cannot already have an ID", ENTITY_NAME, "idexists");
        }
        signalementMessageDTO = signalementMessageService.save(signalementMessageDTO);
        return ResponseEntity.created(new URI("/api/signalement-messages/" + signalementMessageDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, signalementMessageDTO.getId().toString()))
            .body(signalementMessageDTO);
    }

    /**
     * {@code PUT  /signalement-messages/:id} : Updates an existing signalementMessage.
     *
     * @param id the id of the signalementMessageDTO to save.
     * @param signalementMessageDTO the signalementMessageDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated signalementMessageDTO,
     * or with status {@code 400 (Bad Request)} if the signalementMessageDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the signalementMessageDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<SignalementMessageDTO> updateSignalementMessage(
        @PathVariable(value = "id", required = false) final UUID id,
        @Valid @RequestBody SignalementMessageDTO signalementMessageDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update SignalementMessage : {}, {}", id, signalementMessageDTO);
        if (signalementMessageDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, signalementMessageDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!signalementMessageRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        signalementMessageDTO = signalementMessageService.update(signalementMessageDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, signalementMessageDTO.getId().toString()))
            .body(signalementMessageDTO);
    }

    /**
     * {@code PATCH  /signalement-messages/:id} : Partial updates given fields of an existing signalementMessage, field will ignore if it is null
     *
     * @param id the id of the signalementMessageDTO to save.
     * @param signalementMessageDTO the signalementMessageDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated signalementMessageDTO,
     * or with status {@code 400 (Bad Request)} if the signalementMessageDTO is not valid,
     * or with status {@code 404 (Not Found)} if the signalementMessageDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the signalementMessageDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<SignalementMessageDTO> partialUpdateSignalementMessage(
        @PathVariable(value = "id", required = false) final UUID id,
        @NotNull @RequestBody SignalementMessageDTO signalementMessageDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update SignalementMessage partially : {}, {}", id, signalementMessageDTO);
        if (signalementMessageDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, signalementMessageDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!signalementMessageRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<SignalementMessageDTO> result = signalementMessageService.partialUpdate(signalementMessageDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, signalementMessageDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /signalement-messages} : get all the Signalement Messages.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Signalement Messages in body.
     */
    @GetMapping("")
    public ResponseEntity<List<SignalementMessageDTO>> getAllSignalementMessages(
        SignalementMessageCriteria criteria,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get SignalementMessages by criteria: {}", criteria);

        Page<SignalementMessageDTO> page = signalementMessageQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /signalement-messages/count} : count all the signalementMessages.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countSignalementMessages(SignalementMessageCriteria criteria) {
        LOG.debug("REST request to count SignalementMessages by criteria: {}", criteria);
        return ResponseEntity.ok().body(signalementMessageQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /signalement-messages/:id} : get the "id" signalementMessage.
     *
     * @param id the id of the signalementMessageDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the signalementMessageDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<SignalementMessageDTO> getSignalementMessage(@PathVariable("id") UUID id) {
        LOG.debug("REST request to get SignalementMessage : {}", id);
        Optional<SignalementMessageDTO> signalementMessageDTO = signalementMessageService.findOne(id);
        return ResponseUtil.wrapOrNotFound(signalementMessageDTO);
    }

    /**
     * {@code DELETE  /signalement-messages/:id} : delete the "id" signalementMessage.
     *
     * @param id the id of the signalementMessageDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSignalementMessage(@PathVariable("id") UUID id) {
        LOG.debug("REST request to delete SignalementMessage : {}", id);
        signalementMessageService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
