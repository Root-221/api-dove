package sn.dove.backend.dove.service;

/**
 * Raised when a caller tries to create a DOVE resource whose (resourceType, externalId) pair
 * already exists. Distinct from a generic {@link IllegalStateException} so that
 * {@code sn.dove.backend.web.rest.errors.ExceptionTranslator} can map it to HTTP 409 Conflict
 * without having to guess at the meaning of arbitrary IllegalStateExceptions thrown elsewhere.
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
