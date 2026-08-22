# Authentification Keycloak Sonatel et autorisations DOVE

## Principe

Keycloak Sonatel authentifie l'employé ; DOVE autorise l'action.

```text
Angular (client public dove-web)
  → Authorization Code + PKCE S256
Keycloak Sonatel
  → access token JWT avec sub et aud=dove-api
Spring Security Resource Server
  → signature JWK + iss + exp/nbf + aud
DOVE
  → utilisateur local externalSubject == JWT.sub
  → statut + rôle + permissions + scope local
  → contrôle de méthode et filtrage des données
```

Le frontend ne possède aucun secret. Le backend n'utilise pas le login OAuth2 avec session : il
est stateless et accepte un Bearer token à chaque appel. Les tokens restent en mémoire dans
`keycloak-js` et ne sont écrits ni dans `localStorage`, ni dans les logs.

## Client Keycloak SPA

Demander à l'équipe IAM un client distinct par environnement avec :

| Paramètre            | Valeur                                              |
| -------------------- | --------------------------------------------------- |
| Type                 | OpenID Connect, client public                       |
| Client ID            | `dove-web` ou valeur fournie                        |
| Standard Flow        | activé                                              |
| PKCE                 | `S256` obligatoire                                  |
| Implicit Flow        | désactivé                                           |
| Direct Access Grants | désactivé                                           |
| Service Accounts     | désactivé pour la SPA                               |
| Redirect URIs        | URL DOVE exacte, incluant `/app/home` si nécessaire |
| Post logout URIs     | URL `/auth` exacte                                  |
| Web Origins          | origine DOVE exacte                                 |

Un client scope/audience Keycloak doit ajouter `dove-api` dans `aud` de l'access token. L'API
refuse un token destiné uniquement à un autre client.

Valeurs à obtenir pour DEV, RECETTE et PROD : issuer exact, realm, client ID, audience, claims,
durées de token/session, politique MFA, certificats, proxy, DNS et stratégie de provisioning.

## Configuration Angular

Le build de production charge `/runtime-config.json` avant d'initialiser Keycloak :

```json
{
  "apiUrl": "/api/v1",
  "keycloak": {
    "url": "https://sso.sonatel.sn",
    "realm": "sonatel",
    "clientId": "dove-web"
  }
}
```

Ce fichier est public et ne contient aucun secret. Il peut être remplacé au déploiement sans
reconstruire l'application Angular.

L'implémentation se trouve dans :

- `DoveFront/src/app/core/auth/oidc.service.ts` ;
- `DoveFront/src/app/core/data/api/api-auth.interceptor.ts` ;
- `DoveFront/src/app/core/config/runtime-config.service.ts`.

`keycloak.init` utilise `check-sso`, PKCE `S256` et `silent-check-sso.html`. Le bouton de connexion
appelle `login`; la déconnexion appelle le logout Keycloak. Avant chaque requête API,
`updateToken(30)` renouvelle un token proche de l'expiration et l'intercepteur ajoute
`Authorization: Bearer ...` uniquement aux URLs commençant par `apiUrl`.

Une URL signée de stockage située sur un autre domaine ne reçoit donc jamais le token Keycloak.

## Configuration Spring Boot

Variables de production :

```text
DOVE_KEYCLOAK_ISSUER_URI=https://sso.sonatel.sn/realms/sonatel
DOVE_KEYCLOAK_AUDIENCE=dove-api
DOVE_KEYCLOAK_WEB_CLIENT_ID=dove-web
DOVE_ALLOWED_ORIGINS=https://dove.sonatel.sn
```

Équivalent YAML :

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: ${DOVE_KEYCLOAK_ISSUER_URI}

jhipster:
  security:
    oauth2:
      audience:
        - ${DOVE_KEYCLOAK_AUDIENCE:dove-api}
  cors:
    allowed-origins: ${DOVE_ALLOWED_ORIGINS}

dove:
  auth:
    dev-header-enabled: false
    web-client-id: ${DOVE_KEYCLOAK_WEB_CLIENT_ID:dove-web}
```

Au démarrage, Spring découvre les clés JWK depuis l'issuer. `SecurityConfiguration` ajoute le
validateur d'audience, configure une session `STATELESS`, autorise uniquement `/api/v1/**`, ferme
l'ancienne API `/api/**` et active la sécurité de méthode.

## Provisioning local DOVE

Chaque utilisateur de production doit être pré-provisionné :

```json
{
  "externalSubject": "valeur-exacte-du-claim-sub",
  "email": "prenom.nom@sonatel.sn",
  "role": "USER_ENABLEMENT",
  "status": "ACTIVE",
  "accessScope": {
    "global": false,
    "applicationIds": ["…"],
    "businessUnitIds": ["…"],
    "businessJobIds": ["…"],
    "moduleIds": ["…"]
  }
}
```

En production, la résolution utilise exclusivement l'égalité
`utilisateur.externalSubject == jwt.sub`. Un email ou un `preferred_username` ne rattache jamais
automatiquement un compte. Un sujet inconnu ou un compte `DISABLED` reçoit `403`.

Flux recommandé :

1. l'Admin crée/import le compte et son périmètre ;
2. l'équipe IAM ou un flux de provisioning fournit le `sub` ;
3. `externalSubject` est enregistré de manière contrôlée ;
4. l'utilisateur se connecte ;
5. `GET /api/v1/me` renvoie les permissions DOVE effectives.

Ne jamais attribuer un rôle DOVE à partir de `realm_access.roles`, `resource_access` ou d'un rôle
porté par le JWT.

## Rôles, permissions et périmètres

La source de vérité fonctionnelle est le backend DOVE :

- l'affectation du rôle, les permissions nominatives et le scope sont persistés avec l'utilisateur ;
- la matrice standard des rôles est versionnée dans `DovePermissions` ;
- les écrans Admin utilisent `/api/v1/admin/users/**`, `/admin/roles` et `/admin/permissions` ;
- chaque contrôleur sensible applique `@PreAuthorize` ;
- les services vérifient aussi application, métier et module avant de lire ou muter.

Rôles standards :

| Rôle              | Accès                                                                |
| ----------------- | -------------------------------------------------------------------- |
| `BUSINESS_USER`   | contenus publics de son scope, progression, favoris, feedback        |
| `NANDITE`         | précédent + propositions et communauté                               |
| `USER_ENABLEMENT` | éditorial de son scope + lecture inter-métiers explicite             |
| `ADMIN`           | gouvernance, utilisateurs, référentiels, audit, sécurité, opérations |

Pour un contenu, la lecture normale exige l'application, le métier et le module affectés. User
Enablement peut lire un autre métier de ses applications avec `READ_CROSS_BUSINESS_JOB`, uniquement
lorsque la demande est explicite et uniquement pour les statuts publics. Ce droit est ignoré pour
les créations, modifications, transitions, feedbacks gérés et propositions revues.

## Profil local

Les profils `local` et `dev` activent `X-Dove-User-Id` pour rendre tous les parcours testables sans
un Keycloak local :

```bash
curl -H 'X-Dove-User-Id: <uuid>' http://localhost:8080/api/v1/me
```

Sans header, le compte métier Bakary est utilisé. Ce filtre est désactivé par
`dove.auth.dev-header-enabled=false` en production et ne doit jamais être réactivé dans un manifest
de production.

## Codes et comportement frontend

- `401` : token absent, invalide ou expiré ; le frontend peut relancer la connexion après échec du
  renouvellement ;
- `403` : compte local absent/désactivé ou permission manquante ; ne pas boucler sur le login ;
- `404` : ressource absente ou masquée par le scope ;
- `409` : conflit métier, doublon ou transition invalide.

Après authentification, l'interface doit toujours construire ses capacités depuis `/me`. Les
gardes Angular améliorent l'expérience, mais seule la décision backend constitue la frontière de
sécurité.

## Checklist IAM avant production

- issuer HTTPS et chaîne de certificats testés depuis le pod backend ;
- audience `dove-api` observée dans un vrai access token ;
- PKCE `S256`, implicit et password grants désactivés ;
- redirect URIs et Web Origins sans joker large ;
- aucun secret dans Angular ;
- comptes DOVE pré-provisionnés avec le bon `sub` ;
- tests `401`, mauvaise audience, mauvais issuer, token expiré, utilisateur inconnu et désactivé ;
- CORS testé sur l'origine réelle ;
- durée des tokens, MFA et logout validés avec l'équipe IAM ;
- endpoints `/management` limités au réseau d'exploitation.
