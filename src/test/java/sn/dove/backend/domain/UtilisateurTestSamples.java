package sn.dove.backend.domain;

import java.util.UUID;

public class UtilisateurTestSamples {

    public static Utilisateur getUtilisateurSample1() {
        return new Utilisateur()
            .id(UUID.fromString("23d8dc04-a48b-45d9-a01d-4b728f0ad4aa"))
            .nom("nom1")
            .prenom("prenom1")
            .email("email1")
            .login("login1");
    }

    public static Utilisateur getUtilisateurSample2() {
        return new Utilisateur()
            .id(UUID.fromString("ad79f240-3727-46c3-b89f-2cf6ebd74367"))
            .nom("nom2")
            .prenom("prenom2")
            .email("email2")
            .login("login2");
    }

    public static Utilisateur getUtilisateurRandomSampleGenerator() {
        return new Utilisateur()
            .id(UUID.randomUUID())
            .nom(UUID.randomUUID().toString())
            .prenom(UUID.randomUUID().toString())
            .email(UUID.randomUUID().toString())
            .login(UUID.randomUUID().toString());
    }
}
