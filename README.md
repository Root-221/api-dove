# DOVE Backend

> Guide local, préproduction et production : [configuration MySQL, S3 et Keycloak](docs/CONFIGURATION_ENVIRONNEMENTS.md).

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
- MySQL pour tous les profils applicatifs locaux et distants ; le local pointe vers
  `MYSDOVEDEV` sans enregistrer le mot de passe dans Git.

Les anciens contrôleurs générés par JHipster sous `/api/**` ne font pas partie du contrat et sont
fermés par Spring Security. Seule l'API `/api/v1/**` doit être consommée.

## Démarrage local complet

Prérequis recommandés : Java 21, Node 24.15, npm 11 et accès réseau/VPN à
`10.137.21.115:6446`.

Préparer une seule fois la configuration privée MySQL :

```bash
cp .env.local.example .env.local
```

Ouvrir ensuite `.env.local` et remplacer uniquement `__SECRET_MYSQL_DEVELOPPEMENT__` par le mot de
passe transmis séparément. Ce fichier est ignoré par Git.

Depuis le frontend :

```bash
cd ../DoveFront
npm install
npm run dev
```

`npm run dev` lance Angular sur `http://localhost:4200` et ce backend sur
`http://localhost:8080`. Le profil local utilise directement la base MySQL partagée
`MYSDOVEDEV`. Il n’importe pas `DoveFront/db.json` et n’utilise pas H2. Liquibase applique les
migrations versionnées au démarrage, puis Hibernate `ddl-auto=update` complète les éventuels écarts
de schéma propres à cet environnement de développement.

Pour lancer uniquement l'API :

```bash
./scripts/run-environment.sh local
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

### Authentification Keycloak locale

Pour tester le véritable flux OpenID Connect et les JWT sans contacter Sonatel :

```bash
cd ../DoveFront
npm run dev:keycloak
```

Cette commande démarre Keycloak, Angular et le backend avec le profil `local-keycloak`. Le guide
détaillé, les URLs et les comptes locaux sont dans
[KEYCLOAK_LOCAL.md](docs/KEYCLOAK_LOCAL.md). Un `Ctrl+C` arrête les trois services.

Keycloak authentifie uniquement l’identité. Au premier login, DOVE crée automatiquement un profil
actif `BUSINESS_USER` à partir du `JWT.sub`. Les rôles, permissions et périmètres sont ensuite
gérés exclusivement dans DOVE.

## Profils et données

| Profil Spring | Base | Authentification | Stockage | Seed |
|---|---|---|---|---|
| `local` | MySQL `MYSDOVEDEV` | header de développement | dossier `.dove-storage` | non |
| `local-keycloak` | MySQL `MYSDOVEDEV` | Bearer JWT Keycloak local | dossier `.dove-storage-keycloak` | non |
| `dev` | MySQL Docker local | header de développement | local | oui si base vide |
| `preprod` | MySQL `MYSDOVEDEV` | Bearer JWT Keycloak | S3 OpenShift | non |
| `prod` | MySQL `MYSDOVEPRD` | Bearer JWT Keycloak | S3 OpenShift | non |

Le fichier `.env.local` et les dossiers de stockage local sont ignorés par Git.

## Validation

```bash
./mvnw -DskipTests compile
./mvnw verify
./mvnw -Pprod clean verify
```

Les tests d'intégration JHipster utilisent Testcontainers : Docker doit être démarré pour
`./mvnw verify`. Le build de production requiert Java 21 à 25 selon l'enforcer du projet.

## Configuration de production

Les variables, modèles et procédures OpenShift sont décrits dans le
[guide des environnements](docs/CONFIGURATION_ENVIRONNEMENTS.md). Aucun secret ne doit être placé
dans Git, l'image Angular ou `runtime-config.json`.

Construction et lancement :

```bash
./mvnw -Pprod clean verify
java -jar target/dove-backend-*.jar --spring.profiles.active=prod
```

Liquibase applique les migrations au démarrage. Avant exposition publique, placer l'API derrière
le reverse proxy Sonatel avec TLS, limites de taille et de débit, observabilité, sauvegardes
MySQL testées et accès réseau privé au stockage S3.

## Documentation

- [Authentification Keycloak et rôles DOVE](docs/AUTHENTICATION.md)
- [Tester Keycloak en local](docs/KEYCLOAK_LOCAL.md)
- [Contrat complet de l'API v1](docs/API.md)
- [Stockage local et S3 OpenShift](docs/STORAGE.md)
- [Environnements et secrets](docs/CONFIGURATION_ENVIRONNEMENTS.md)
- [Configuration de production](src/main/resources/config/application-prod.yml)
- [Migration Liquibase DOVE](src/main/resources/config/liquibase/changelog/20260822160000_added_dove_api_resources.xml)

## Structure ajoutée pour DOVE

```text
src/main/java/sn/dove/backend/dove/
├── config/       configuration, profil et import initial
├── domain/       document persistant versionné
├── repository/   accès MySQL
├── security/     utilisateur courant, permissions et périmètres
├── service/      magasin transactionnel, audit et notifications
├── storage/      contrat local/cloud
└── web/          contrôleurs REST /api/v1
```

Le schéma `dove_resource` permet de raccorder immédiatement tout le frontend avec des documents
JSON versionnés. Pour une montée en charge importante, les agrégats les plus sollicités pourront
être normalisés progressivement derrière le même contrat `/api/v1`, sans réintroduire un stockage
client ni changer les écrans Angular.
