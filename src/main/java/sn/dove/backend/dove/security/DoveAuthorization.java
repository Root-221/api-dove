package sn.dove.backend.dove.security;

import org.springframework.stereotype.Component;

@Component("doveAuthorization")
public class DoveAuthorization {

    private final DoveCurrentUserService currentUserService;

    public DoveAuthorization(DoveCurrentUserService currentUserService) {
        this.currentUserService = currentUserService;
    }

    public boolean has(String permission) {
        return currentUserService.hasPermission(permission);
    }
}
