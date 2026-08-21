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
import sn.dove.backend.repository.ModuleRepository;
import sn.dove.backend.service.ModuleQueryService;
import sn.dove.backend.service.ModuleService;
import sn.dove.backend.service.criteria.ModuleCriteria;
import sn.dove.backend.service.dto.ModuleDTO;
import sn.dove.backend.web.rest.errors.BadRequestAlertException;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link sn.dove.backend.domain.Module}.
 */
@RestController
@RequestMapping("/api/modules")
public class ModuleResource {

    private static final Logger LOG = LoggerFactory.getLogger(ModuleResource.class);

    private static final String ENTITY_NAME = "module";

    @Value("${jhipster.clientApp.name:doveBackend}")
    private String applicationName;

    private final ModuleService moduleService;

    private final ModuleRepository moduleRepository;

    private final ModuleQueryService moduleQueryService;

    public ModuleResource(ModuleService moduleService, ModuleRepository moduleRepository, ModuleQueryService moduleQueryService) {
        this.moduleService = moduleService;
        this.moduleRepository = moduleRepository;
        this.moduleQueryService = moduleQueryService;
    }

    /**
     * {@code POST  /modules} : Create a new module.
     *
     * @param moduleDTO the moduleDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new moduleDTO, or with status {@code 400 (Bad Request)} if the module has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<ModuleDTO> createModule(@Valid @RequestBody ModuleDTO moduleDTO) throws URISyntaxException {
        LOG.debug("REST request to save Module : {}", moduleDTO);
        if (moduleDTO.getId() != null) {
            throw new BadRequestAlertException("A new module cannot already have an ID", ENTITY_NAME, "idexists");
        }
        moduleDTO = moduleService.save(moduleDTO);
        return ResponseEntity.created(new URI("/api/modules/" + moduleDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, moduleDTO.getId().toString()))
            .body(moduleDTO);
    }

    /**
     * {@code PUT  /modules/:id} : Updates an existing module.
     *
     * @param id the id of the moduleDTO to save.
     * @param moduleDTO the moduleDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated moduleDTO,
     * or with status {@code 400 (Bad Request)} if the moduleDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the moduleDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ModuleDTO> updateModule(
        @PathVariable(value = "id", required = false) final UUID id,
        @Valid @RequestBody ModuleDTO moduleDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Module : {}, {}", id, moduleDTO);
        if (moduleDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, moduleDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!moduleRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        moduleDTO = moduleService.update(moduleDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, moduleDTO.getId().toString()))
            .body(moduleDTO);
    }

    /**
     * {@code PATCH  /modules/:id} : Partial updates given fields of an existing module, field will ignore if it is null
     *
     * @param id the id of the moduleDTO to save.
     * @param moduleDTO the moduleDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated moduleDTO,
     * or with status {@code 400 (Bad Request)} if the moduleDTO is not valid,
     * or with status {@code 404 (Not Found)} if the moduleDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the moduleDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<ModuleDTO> partialUpdateModule(
        @PathVariable(value = "id", required = false) final UUID id,
        @NotNull @RequestBody ModuleDTO moduleDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Module partially : {}, {}", id, moduleDTO);
        if (moduleDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, moduleDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!moduleRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<ModuleDTO> result = moduleService.partialUpdate(moduleDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, moduleDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /modules} : get all the Modules.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Modules in body.
     */
    @GetMapping("")
    public ResponseEntity<List<ModuleDTO>> getAllModules(ModuleCriteria criteria) {
        LOG.debug("REST request to get Modules by criteria: {}", criteria);

        List<ModuleDTO> entityList = moduleQueryService.findByCriteria(criteria);
        return ResponseEntity.ok().body(entityList);
    }

    /**
     * {@code GET  /modules/count} : count all the modules.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countModules(ModuleCriteria criteria) {
        LOG.debug("REST request to count Modules by criteria: {}", criteria);
        return ResponseEntity.ok().body(moduleQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /modules/:id} : get the "id" module.
     *
     * @param id the id of the moduleDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the moduleDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ModuleDTO> getModule(@PathVariable("id") UUID id) {
        LOG.debug("REST request to get Module : {}", id);
        Optional<ModuleDTO> moduleDTO = moduleService.findOne(id);
        return ResponseUtil.wrapOrNotFound(moduleDTO);
    }

    /**
     * {@code DELETE  /modules/:id} : delete the "id" module.
     *
     * @param id the id of the moduleDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteModule(@PathVariable("id") UUID id) {
        LOG.debug("REST request to delete Module : {}", id);
        moduleService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
