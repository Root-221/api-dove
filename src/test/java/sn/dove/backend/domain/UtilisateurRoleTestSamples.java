package sn.dove.backend.domain;

import java.util.UUID;

public class UtilisateurRoleTestSamples {

    public static UtilisateurRole getUtilisateurRoleSample1() {
        return new UtilisateurRole().id(UUID.fromString("23d8dc04-a48b-45d9-a01d-4b728f0ad4aa"));
    }

    public static UtilisateurRole getUtilisateurRoleSample2() {
        return new UtilisateurRole().id(UUID.fromString("ad79f240-3727-46c3-b89f-2cf6ebd74367"));
    }

    public static UtilisateurRole getUtilisateurRoleRandomSampleGenerator() {
        return new UtilisateurRole().id(UUID.randomUUID());
    }
}
