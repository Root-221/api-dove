# DOVE Backend

API Spring Boot sécurisée de DOVE. Elle remplace le serveur JSON de démonstration du frontend,
persiste les données applicatives, applique les permissions et les périmètres métier côté serveur,
et délègue l'authentification de production à Keycloak Sonatel.

## Ce qui est opérationnel

- API REST versionnée sous `/api/v1` ;
- recherche de contenus avec une réponse canonique pouvant contenir `VIDEO`, `FICHEPRATIQUE` et
  `FAQ` ;
- création/modification dans le même modèle, détection de titres similaires et refus des doublons
  exacts ;
- filtrage par application, métier et module pour les utilisateurs métier et Nandité ;
- consultation inter-métiers explicite pour User Enablement, sans élargir son périmètre d'écriture ;
- workflow brouillon, validation, publication, révision et archivage ;
- favoris, progression, feedback, propositions Nandité, communauté, notifications et analytics ;
- administration des utilisateurs, périmètres, référentiels, RBAC, audit et opérations ;
- validation JWT (`iss`, signature, durée et `aud`) et autorisations DOVE locales ;
- stockage média local en développement et adaptateur de passerelle cloud en production ;
- PostgreSQL + Liquibase en `dev`/`prod`, H2 persistant en profil `local`.

Les anciens contrôleurs générés par JHipster sous `/api/**` ne font pas partie du contrat et sont
fermés par Spring Security. Seule l'API `/api/v1/**` doit être consommée.

## Démarrage local complet

Prérequis recommandés : Java 21, Node 24.15 et npm 11.

Depuis le frontend :

```bash
cd ../DoveFront
npm install
npm run dev
```

`npm run dev` lance Angular sur `http://localhost:4200` et ce backend sur
`http://localhost:8080`. Le profil local utilise `.dove-local/database` et importe `DoveFront/db.json`
une seule fois lorsque la base est vide. Ce JSON est donc uniquement une fixture d'initialisation :
il n'est jamais servi par JSON Server.

Pour lancer uniquement l'API :

```bash
./mvnw -Dspring-boot.run.profiles=local spring-boot:run
```

Sur une machine utilisant provisoirement une version Java hors de la plage du projet, ajouter
`-Denforcer.skip=true`. Cette option ne doit pas masquer la vérification Java 21 en CI.

Sondes utiles :

```text
GET http://localhost:8080/management/health
GET http://localhost:8080/api/v1/me
```

La documentation interactive locale est disponible sur
`http://localhost:8080/swagger-ui/index.html`. Le contrat OpenAPI JSON est exposé sur
`http://localhost:8080/v3/api-docs`. Ces deux routes sont publiques uniquement lorsque
`dove.auth.dev-header-enabled=true`; elles restent réservées au rôle administrateur dans les
environnements sécurisés.

En local uniquement, Angular ajoute `X-Dove-User-Id`. Le compte par défaut est Bakary Diassy.
Le sélecteur de profils de la page de connexion obtient ses comptes via `/api/v1/dev/users`.

## Profils et données

| Profil Spring | Base       | Authentification        | Stockage                | Seed             |
| ------------- | ---------- | ----------------------- | ----------------------- | ---------------- |
| `local`       | H2 fichier | header de développement | dossier `.dove-storage` | oui si base vide |
| `dev`         | PostgreSQL | header de développement | local                   | oui si base vide |
| `prod`        | PostgreSQL | Bearer JWT Keycloak     | passerelle cloud        | non              |

Les dossiers `.dove-local` et `.dove-storage` sont ignorés par Git.

## Validation

```bash
./mvnw -DskipTests compile
./mvnw verify
./mvnw -Pprod clean verify
```

Les tests d'intégration JHipster utilisent Testcontainers : Docker doit être démarré pour
`./mvnw verify`. Le build de production requiert Java 21 à 25 selon l'enforcer du projet.

## Configuration de production

Variables obligatoires ou à confirmer avec les équipes Sonatel :

```text
DOVE_DATABASE_URL=jdbc:postgresql://postgres:5432/dove
DOVE_DATABASE_USERNAME=dove
DOVE_DATABASE_PASSWORD=<secret injecté>
DOVE_KEYCLOAK_ISSUER_URI=https://sso.sonatel.sn/realms/sonatel
DOVE_KEYCLOAK_AUDIENCE=dove-api
DOVE_KEYCLOAK_WEB_CLIENT_ID=dove-web
DOVE_ALLOWED_ORIGINS=https://dove.sonatel.sn
DOVE_STORAGE_PROVIDER=gateway
DOVE_STORAGE_GATEWAY_URL=https://storage-gateway.internal
DOVE_STORAGE_GATEWAY_API_KEY=<secret injecté>
DOVE_STORAGE_MAX_UPLOAD_BYTES=536870912
```

Ne mettre aucun secret dans Git, l'image Angular ou `runtime-config.json`. Les secrets backend
doivent venir du gestionnaire de secrets de la plateforme.

Construction et lancement :

```bash
./mvnw -Pprod clean verify
java -jar target/dove-backend-*.jar --spring.profiles.active=prod
```

Liquibase applique les migrations au démarrage. Avant exposition publique, placer l'API derrière
le reverse proxy Sonatel avec TLS, limites de taille et de débit, observabilité, sauvegardes
PostgreSQL testées et accès réseau privé à la passerelle de stockage.

## Documentation

- [Authentification Keycloak et rôles DOVE](docs/AUTHENTICATION.md)
- [Contrat complet de l'API v1](docs/API.md)
- [Stockage local et passerelle cloud](docs/STORAGE.md)
- [Configuration de production](src/main/resources/config/application-prod.yml)
- [Migration Liquibase DOVE](src/main/resources/config/liquibase/changelog/20260822160000_added_dove_api_resources.xml)

## Structure ajoutée pour DOVE

```text
src/main/java/sn/dove/backend/dove/
├── config/       configuration, profil et import initial
├── domain/       document persistant versionné
├── repository/   accès PostgreSQL/H2
├── security/     utilisateur courant, permissions et périmètres
├── service/      magasin transactionnel, audit et notifications
├── storage/      contrat local/cloud
└── web/          contrôleurs REST /api/v1
```

Le schéma `dove_resource` permet de raccorder immédiatement tout le frontend avec des documents
JSON versionnés. Pour une montée en charge importante, les agrégats les plus sollicités pourront
être normalisés progressivement derrière le même contrat `/api/v1`, sans réintroduire un stockage
client ni changer les écrans Angular.
