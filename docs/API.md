# Contrat REST DOVE `/api/v1`

Ce document décrit l'API réellement implémentée dans `sn.dove.backend.dove.web`. Le frontend
Angular utilise cette version ; les anciennes routes JHipster `/api/*` sont refusées.

## Conventions

```text
Base locale       http://localhost:8080/api/v1
Base production   https://<hôte-dove>/api/v1
Authentification  Authorization: Bearer <access_token> en production
Développement     X-Dove-User-Id: <UUID>, profils local/dev uniquement
Corps JSON        Content-Type: application/json
Dates             ISO-8601 UTC
Identifiants      UUID, sauf la clé composite interne de progression
```

Toutes les routes sont authentifiées sauf `GET /media/content/{storageKey}`, dont l'URL est créée
par le service de stockage. Le serveur détermine toujours l'utilisateur à partir de la sécurité :
un `userId` reçu dans un corps métier ne peut pas changer l'acteur.

Les erreurs de validation ou d'autorisation utilisent les réponses Problem Details de Spring avec
les codes HTTP usuels : `400`, `401`, `403`, `404`, `409`, `413` et `415`. Une ressource hors
périmètre est généralement présentée comme absente (`404`) afin de ne pas révéler son existence.

`GET /contents` retourne :

```json
{
  "items": [],
  "page": 0,
  "size": 20,
  "pageSize": 20,
  "totalItems": 0,
  "total": 0,
  "totalPages": 0
}
```

`page` commence à zéro, `size` est borné entre 1 et 100 et `sort` accepte `title`, `viewCount` ou
`updatedAt` suivi de `asc`/`desc`.

## Session et annuaire visible

| Méthode | Route                               | Permission     | Description                                           |
| ------- | ----------------------------------- | -------------- | ----------------------------------------------------- |
| GET     | `/me`                               | authentifié    | Profil courant enrichi avec `permissions`             |
| PATCH   | `/me/preferences`                   | authentifié    | Met à jour `language`, `theme`, `compactMode`         |
| GET     | `/dev/users`                        | local/dev      | Comptes du sélecteur de démonstration ; `404` en prod |
| GET     | `/users?availableForBrowsing=false` | `READ_CONTENT` | Profils visibles dans le périmètre                    |
| GET     | `/users/{id}`                       | `READ_CONTENT` | Profil public visible, sans sujet Keycloak            |

Forme principale de `/me` :

```json
{
  "id": "…",
  "externalSubject": "sujet-keycloak-en-production",
  "firstName": "Aminata",
  "lastName": "Diallo",
  "email": "aminata.diallo@sonatel.sn",
  "role": "USER_ENABLEMENT",
  "permissions": ["READ_CONTENT", "CREATE_CONTENT", "EDIT_CONTENT"],
  "status": "ACTIVE",
  "contentValidatorId": "…",
  "accessScope": {
    "global": false,
    "applicationIds": ["…"],
    "businessUnitIds": ["…"],
    "businessJobIds": ["…"],
    "moduleIds": ["…"]
  }
}
```

## Référentiels accessibles

| Méthode | Route                 | Paramètres                                                    |
| ------- | --------------------- | ------------------------------------------------------------- |
| GET     | `/applications`       | aucun                                                         |
| GET     | `/applications/{id}`  | —                                                             |
| GET     | `/business-units`     | aucun                                                         |
| GET     | `/business-jobs`      | `availableForBrowsing`                                        |
| GET     | `/business-jobs/{id}` | —                                                             |
| GET     | `/modules`            | `applicationId`, `businessJobId`, `availableForBrowsing`, `q` |
| GET     | `/modules/{id}`       | —                                                             |

Un utilisateur métier ou Nandité ne reçoit que ses référentiels affectés. User Enablement peut
demander `availableForBrowsing=true` pour consulter les métiers de ses applications grâce à
`READ_CROSS_BUSINESS_JOB`. Ce mode de consultation ne change jamais son scope de mutation.

## Contenu agrégé et recherche

Une ressource `content` représente une seule réponse métier. Ses `formats` peuvent contenir
simultanément `VIDEO`, `FICHEPRATIQUE` et `FAQ`. Les champs associés sont `videoItems`, `steps` et
`faqItems`. Lorsqu'un ancien jeu de données contient une ligne par format pour le même titre et le
même contexte, la liste les expose comme un seul contenu canonique.

| Méthode | Route                        | Permission                         | Description                       |
| ------- | ---------------------------- | ---------------------------------- | --------------------------------- |
| GET     | `/contents`                  | `READ_CONTENT`                     | Recherche et catalogue filtrés    |
| GET     | `/contents/similar`          | `CREATE_CONTENT` ou `EDIT_CONTENT` | Suggestions anti-doublon          |
| GET     | `/contents/{id}`             | `READ_CONTENT`                     | Détail complet                    |
| POST    | `/contents`                  | `CREATE_CONTENT` ou `CONTRIBUTE`   | Création d'un brouillon           |
| PUT     | `/contents/{id}`             | `EDIT_CONTENT`                     | Remplacement des champs éditables |
| PATCH   | `/contents/{id}`             | `EDIT_CONTENT`                     | Mise à jour partielle             |
| POST    | `/contents/{id}/transitions` | selon la cible                     | Transition éditoriale             |
| PUT     | `/contents/{id}/featured`    | `EDIT_CONTENT`                     | `{ "featured": true               | false }` |
| GET     | `/content-validation-queue`  | `VALIDATE_CONTENT`                 | Contenus affectés au validateur   |

Paramètres de `GET /contents` :

```text
q
applicationId          répétable
businessJobId          répétable
moduleId               répétable
format                  VIDEO | FICHEPRATIQUE | FAQ, répétable
status                  répétable
ownerId
validatorId
featured
browseOtherBusinessJobs=false
page=0
size=20
sort=updatedAt,desc
```

La recherche exacte « Créer une commande » retourne une seule entrée si elle existe. Sans droit
éditorial, seuls les statuts publics `PUBLIER` et `A_REVISER` sont visibles, sauf les contenus de
l'auteur courant. User Enablement doit demander explicitement un autre métier ou
`browseOtherBusinessJobs=true` pour une lecture inter-métiers publique.

`GET /contents/similar` accepte `title` (au moins 3 caractères), `applicationId`, `businessJobId`,
`moduleId`, `excludeContentId` et `limit` (1 à 10). Il recherche uniquement dans le scope éditable
et renvoie notamment `similarityScore`, `formats`, `missingFormats` et `canEdit`. Une création avec
un titre normalisé identique dans le même contexte retourne `409` et inclut l'ID existant dans le
message `CONTENT_ALREADY_EXISTS:<id>`.

Exemple minimal de création :

```json
{
  "title": "Créer une commande",
  "description": "Procédure complète",
  "applicationId": "…",
  "businessJobId": "…",
  "moduleId": "…",
  "ownerId": "…",
  "validatorId": "…",
  "formats": ["VIDEO", "FICHEPRATIQUE", "FAQ"],
  "videoItems": [{ "order": 1, "title": "Démonstration", "mediaId": "…" }],
  "steps": [{ "order": 1, "title": "Ouvrir le module", "description": "…" }],
  "faqItems": [{ "order": 1, "question": "Que vérifier ?", "answer": "…" }],
  "summary": ["…"],
  "tags": ["commande"],
  "warnings": [],
  "featured": false
}
```

Le serveur impose `BROUILLON`, l'auteur courant, les dates, les compteurs et la version initiale.
Le titre, la description, l'application, le métier, le module et au moins un format sont requis.

Transitions autorisées :

```text
BROUILLON              → EN_ATTENTE_VALIDATION
EN_ATTENTE_VALIDATION  → BROUILLON | VALIDER
VALIDER                → BROUILLON | PUBLIER
PUBLIER                → A_REVISER | ARCHIVER
A_REVISER              → BROUILLON | ARCHIVER
ARCHIVER               → BROUILLON
```

Corps : `{ "targetStatus": "PUBLIER" }`. Le serveur demande la permission correspondant à la
cible. Pour `VALIDER`, l'acteur doit être le validateur affecté et différent du propriétaire.

## Médias et stockage objet

| Méthode | Route                                | Permission       | Description                            |
| ------- | ------------------------------------ | ---------------- | -------------------------------------- |
| GET     | `/media`                             | `READ_CONTENT`   | Métadonnées des médias                 |
| GET     | `/media/{id}`                        | `READ_CONTENT`   | Métadonnées + URL de lecture           |
| POST    | `/media/uploads`                     | `CREATE_CONTENT` | Prépare un upload                      |
| PUT     | `/media/uploads/{uploadId}/content`  | `CREATE_CONTENT` | Corps binaire, profil local uniquement |
| POST    | `/media/uploads/{uploadId}/complete` | `CREATE_CONTENT` | Finalise l'objet                       |
| POST    | `/media/external`                    | `CREATE_CONTENT` | Enregistre une URL HTTP(S) existante   |
| GET     | `/media/{id}/playback-url`           | `READ_CONTENT`   | URL de lecture courte                  |
| GET     | `/media/content/{storageKey}`        | public           | Lecture du fichier local uniquement    |
| DELETE  | `/media/{id}`                        | `EDIT_CONTENT`   | Média non référencé uniquement         |

Préparation :

```json
{
  "type": "VIDEO",
  "fileName": "commande.mp4",
  "mimeType": "video/mp4",
  "sizeBytes": 10485760,
  "title": "Créer une commande"
}
```

Types : `VIDEO`, `IMAGE`, `DOCUMENT`. MIME acceptés : MP4, QuickTime, PNG, JPEG, WebP, PDF et
octet-stream. La taille maximale par défaut est 512 Mio. La réponse `201` contient `uploadId`,
`mediaId`, `uploadUrl`, `requiredHeaders`, `expiresAt` et `status`.

Le navigateur envoie ensuite le binaire directement à `uploadUrl` avec les headers exigés, puis
appelle `complete`. En production, `uploadUrl` et l'URL de lecture sont signées par la passerelle
cloud ; le token Keycloak ne doit pas être envoyé à ce domaine.

## Favoris, progression, feedback et notifications

| Méthode | Route                                | Permission        |
| ------- | ------------------------------------ | ----------------- |
| GET     | `/me/favorites`                      | `READ_CONTENT`    |
| PUT     | `/me/favorites/{contentId}`          | `READ_CONTENT`    |
| DELETE  | `/me/favorites/{contentId}`          | `READ_CONTENT`    |
| GET     | `/me/progress`                       | `READ_CONTENT`    |
| GET     | `/me/contents/{contentId}/progress`  | `READ_CONTENT`    |
| PUT     | `/me/contents/{contentId}/progress`  | `READ_CONTENT`    |
| GET     | `/me/feedback`                       | `SUBMIT_FEEDBACK` |
| POST    | `/feedback`                          | `SUBMIT_FEEDBACK` |
| GET     | `/feedback`                          | `MANAGE_FEEDBACK` |
| POST    | `/feedback/{id}/start-processing`    | `MANAGE_FEEDBACK` |
| POST    | `/feedback/{id}/resolve`             | `MANAGE_FEEDBACK` |
| GET     | `/me/notifications?unreadOnly=false` | authentifié       |
| PATCH   | `/me/notifications/{id}/read`        | propriétaire      |
| POST    | `/me/notifications/mark-all-read`    | authentifié       |

Progression :

```json
{
  "progressPercent": 67,
  "lastPositionSeconds": 42
}
```

La progression ne diminue pas et passe à `completed=true` à 100 %. Un feedback exige
`contentId`; il peut porter `kind`, `rating`, `reason` et `comment`. Un signalement d'obsolescence
place automatiquement le contenu en `A_REVISER` et notifie son propriétaire.

## Nandité et communauté

| Méthode    | Route                            | Permission                  |
| ---------- | -------------------------------- | --------------------------- |
| GET        | `/me/proposals`                  | `CONTRIBUTE`                |
| POST       | `/proposals`                     | `CONTRIBUTE`                |
| GET        | `/proposals/{id}`                | auteur ou reviewer du scope |
| GET        | `/proposals/review-queue`        | `REVIEW_PROPOSALS`          |
| POST       | `/proposals/{id}/start-review`   | `REVIEW_PROPOSALS`          |
| POST       | `/proposals/{id}/accept`         | `REVIEW_PROPOSALS`          |
| POST       | `/proposals/{id}/reject`         | `REVIEW_PROPOSALS`          |
| GET        | `/community/posts`               | `CONTRIBUTE`                |
| GET        | `/community/posts/{id}`          | `CONTRIBUTE`                |
| POST       | `/community/posts`               | `CONTRIBUTE`                |
| PUT/DELETE | `/community/posts/{id}/like`     | `CONTRIBUTE`                |
| PUT/DELETE | `/community/posts/{id}/saved`    | `CONTRIBUTE`                |
| POST       | `/community/posts/{id}/comments` | `CONTRIBUTE`                |

Une proposition exige `title`, `situation`, `treatment`, `applicationId`, `businessJobId` et
`moduleId`. Elle suit `SUBMITTED → UNDER_REVIEW → CONVERTED|REJECTED`. L'acceptation crée dans la
même transaction un contenu `FICHEPRATIQUE` en brouillon. Le refus exige `{ "reason": "…" }`.

## Analytics

| Méthode | Route                         | Permission       |
| ------- | ----------------------------- | ---------------- |
| GET     | `/analytics/summary`          | `VIEW_ANALYTICS` |
| GET     | `/analytics/popular-searches` | `VIEW_ANALYTICS` |
| POST    | `/events/searches`            | `READ_CONTENT`   |
| POST    | `/events/content-views`       | `READ_CONTENT`   |

L'acteur et la date d'un événement sont fixés côté serveur. Les agrégats User Enablement restent
dans son scope de mutation ; Admin voit les métriques globales.

## Administration

| Méthode   | Route                                          | Permission              |
| --------- | ---------------------------------------------- | ----------------------- |
| GET/POST  | `/admin/users`                                 | `MANAGE_USERS`          |
| GET/PATCH | `/admin/users/{id}`                            | `MANAGE_USERS`          |
| PUT       | `/admin/users/{id}/access`                     | `MANAGE_USERS`          |
| POST      | `/admin/users/imports`                         | `MANAGE_USERS`          |
| GET       | `/admin/roles`                                 | `MANAGE_RBAC`           |
| GET       | `/admin/permissions`                           | `MANAGE_RBAC`           |
| GET/POST  | `/admin/reference/{type}`                      | `MANAGE_REFERENCE_DATA` |
| GET/PUT   | `/admin/reference/{type}/{id}`                 | `MANAGE_REFERENCE_DATA` |
| POST      | `/admin/reference/{type}/{id}/archive`         | `MANAGE_REFERENCE_DATA` |
| GET       | `/admin/audit-events`                          | `VIEW_AUDIT`            |
| GET       | `/admin/audit-events/{id}`                     | `VIEW_AUDIT`            |
| GET       | `/admin/authentication/status`                 | `MANAGE_AUTHENTICATION` |
| POST      | `/admin/authentication/tests`                  | `MANAGE_AUTHENTICATION` |
| GET       | `/admin/operations/health`                     | `MANAGE_OPERATIONS`     |
| GET       | `/admin/operations/incidents`                  | `MANAGE_OPERATIONS`     |
| GET/POST  | `/admin/operations/backups`                    | `MANAGE_OPERATIONS`     |
| POST      | `/admin/operations/backups/{id}/restore-tests` | `MANAGE_OPERATIONS`     |

`{type}` accepte `applications`, `business-units`, `business-jobs` ou `modules`. La mise à jour est
un upsert afin de supporter les identifiants générés par les formulaires actuels. L'API protège le
dernier administrateur actif contre une désactivation ou un changement de rôle.

`PUT /admin/users/{id}/access` accepte `role`, `status`, `accessScope`,
`additionalPermissions` et `contentValidatorId`. En production, `externalSubject` doit contenir le
claim Keycloak `sub` pré-provisionné ; il ne doit jamais être choisi depuis un rôle du token.

## Permissions standards

| Rôle              | Permissions principales                                                                    |
| ----------------- | ------------------------------------------------------------------------------------------ |
| `BUSINESS_USER`   | `READ_CONTENT`, `SUBMIT_FEEDBACK`                                                          |
| `NANDITE`         | précédent + `CONTRIBUTE`                                                                   |
| `USER_ENABLEMENT` | lecture inter-métiers, contenu, validation, publication, feedback, propositions, analytics |
| `ADMIN`           | utilisateurs, RBAC, référentiels, authentification, opérations, audit, analytics           |

Les permissions effectives sont calculées par le backend depuis le rôle DOVE et les permissions
nominatives. Les rôles Keycloak ne donnent aucune permission fonctionnelle.

## Exploitation

| Méthode | Route                          | Accès                           |
| ------- | ------------------------------ | ------------------------------- |
| GET     | `/management/health`           | public, détail limité           |
| GET     | `/management/health/liveness`  | public                          |
| GET     | `/management/health/readiness` | public                          |
| GET     | `/management/info`             | autorité technique `ROLE_ADMIN` |
| GET     | `/management/prometheus`       | autorité technique `ROLE_ADMIN` |

Les autres endpoints management exigent l'autorité technique `ROLE_ADMIN`. Le reverse proxy de
production doit limiter toutes les routes management au réseau d'exploitation ; seules les probes
de santé peuvent rester accessibles à l'ingress.
