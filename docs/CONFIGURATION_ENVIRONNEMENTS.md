# Configuration des environnements DOVE

Ce document décrit la configuration du backend Spring Boot et du frontend Angular pour les environnements local, préproduction et production. Les secrets ne sont jamais stockés dans Git ni envoyés au navigateur.

## Architecture retenue

| Profil | Base de données | Authentification | Stockage |
|---|---|---|---|
| `local` | H2 fichier local | en-tête de développement | disque local |
| `local-keycloak` | H2 fichier dédié | Keycloak local, realm `DOVE` | disque local dédié |
| `preprod` | MySQL `MYSDOVEDEV` | Keycloak préproduction, realm `DOVE` | bucket S3 OpenShift |
| `prod` | MySQL `MYSDOVEPRD` | Keycloak production, realm `DOVE` | bucket S3 OpenShift |

Le profil `preprod` utilise la base dite « DEVELOPPEMENT » fournie, puisqu'aucune base de préproduction distincte n'a été communiquée.

Les profils `preprod` et `prod` incluent le profil interne `cloud`, qui impose MySQL, JWT Keycloak, S3, l'arrêt du seed et la désactivation de l'authentification de développement. Un validateur bloque le démarrage si le profil production vise `MYSDOVEDEV`, si la préproduction vise `MYSDOVEPRD`, ou si une URL sensible n'utilise pas HTTPS.

## Gestion des secrets

Les fichiers suivants sont versionnés et ne contiennent que des marqueurs :

- `.env.preprod.example`
- `.env.prod.example`
- `.env.example`, modèle générique

Les fichiers réels `.env.preprod.local` et `.env.prod.local` sont ignorés par Git. Pour préparer un poste autorisé :

```sh
cd api-dove
cp .env.preprod.example .env.preprod.local
# renseigner les marqueurs __...__ avec les valeurs du coffre-fort
```

Chaque valeur doit rester entre apostrophes dans le fichier local afin de préserver les caractères `!`, `$`, `#`, `@` et `/` des mots de passe.

En OpenShift, utiliser de préférence le coffre-fort d'entreprise ou un opérateur External Secrets. À défaut, créer un `Secret` DOVE depuis la CI sécurisée et l'injecter avec `envFrom` :

```yaml
envFrom:
  - secretRef:
      name: dove-backend-environment
```

Le Secret doit contenir les variables du modèle de l'environnement sélectionné. Ne jamais transformer ces valeurs en `ConfigMap`, argument d'image Docker ou variable Angular `environment.ts`.

## Lancer le backend

Local, sans secret externe :

```sh
cd api-dove
./scripts/run-environment.sh local
```

Local avec authentification Keycloak réelle :

```sh
cd ../DoveFront
npm run dev:keycloak
```

Cette commande orchestre les trois processus et les arrête ensemble. La procédure complète est
décrite dans [KEYCLOAK_LOCAL.md](KEYCLOAK_LOCAL.md).

Préproduction :

```sh
cd api-dove
./scripts/run-environment.sh preprod
```

Production :

```sh
cd api-dove
./scripts/run-environment.sh prod
```

Le script charge `.env.<profil>.local`, contrôle toutes les variables obligatoires et ne les affiche jamais. Un autre fichier peut être fourni sans modifier le projet :

```sh
DOVE_ENV_FILE=/chemin/securise/dove.env ./scripts/run-environment.sh prod
```

Dans OpenShift, définir directement `SPRING_PROFILES_ACTIVE=preprod` ou `SPRING_PROFILES_ACTIVE=prod` sur le Deployment.

## MySQL

Le backend utilise désormais MySQL Connector/J. Les migrations Liquibase ont été rendues compatibles MySQL et les deux recherches auparavant liées à PostgreSQL `jsonb` utilisent maintenant des colonnes indexées portables :

- `external_subject`, pour retrouver l'utilisateur Keycloak ;
- `owner_user_id`, pour charger les notifications d'un utilisateur.

Les URLs fournies dans les modèles utilisent `sslMode=REQUIRED`. Ce mode impose le chiffrement, mais ne valide pas l'identité du serveur. Dès que l'autorité de certification et un nom DNS correspondant au certificat MySQL sont disponibles, remplacer ce paramètre par `sslMode=VERIFY_IDENTITY` et installer la CA Sonatel dans le truststore Java. Voir la [documentation MySQL Connector/J sur `sslMode`](https://dev.mysql.com/doc/connector-j/en/connector-j-connp-props-security.html).

Liquibase s'exécute au démarrage en préproduction et production. Avant le premier déploiement production :

1. sauvegarder `MYSDOVEPRD` ;
2. tester la même version sur `MYSDOVEDEV` ;
3. vérifier le compte MySQL : `SELECT`, `INSERT`, `UPDATE`, `DELETE`, `CREATE`, `ALTER`, `INDEX` pendant la migration ;
4. réduire ensuite les droits DDL si la politique Sonatel sépare le compte de migration du compte applicatif.

## Stockage S3 OpenShift

Le stockage utilise AWS SDK for Java 2.x avec un endpoint S3 compatible, une région configurable et l'adressage path-style adapté au service OpenShift. Voir la [configuration officielle des endpoints S3 personnalisés](https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/endpoint-config.html).

L'endpoint fourni est un nom DNS interne au cluster. Pour cette raison, DOVE ne retourne jamais cet endpoint au navigateur :

```text
navigateur -> API DOVE authentifiée -> S3 interne OpenShift
```

Les uploads, lectures et suppressions sont donc effectués par le backend. Le compte S3 a besoin des droits objet `PutObject`, `GetObject`, `HeadObject` et `DeleteObject` uniquement sur le bucket DOVE. L'accès anonyme au bucket doit rester désactivé.

Variables principales :

| Variable | Rôle |
|---|---|
| `DOVE_STORAGE_S3_ENDPOINT` | endpoint HTTPS interne |
| `DOVE_STORAGE_S3_BUCKET` | bucket DOVE |
| `DOVE_STORAGE_S3_REGION` | région de signature, `us-east-1` par défaut |
| `DOVE_STORAGE_S3_ACCESS_KEY` | clé injectée par Secret |
| `DOVE_STORAGE_S3_SECRET_KEY` | secret injecté par Secret |
| `DOVE_STORAGE_PUBLIC_BASE_URL` | URL publique du backend, utilisée pour les URLs média |
| `DOVE_STORAGE_MAX_UPLOAD_BYTES` | limite d'upload, 512 Mio par défaut |

Le certificat TLS de l'endpoint S3 doit être reconnu par le JRE du pod. Monter la CA Sonatel/OpenShift dans le truststore système de l'image ; ne jamais désactiver la validation TLS.

## Keycloak

Le frontend utilise `keycloak-js` avec le flux Authorization Code, PKCE S256 et un client public. Le backend valide les JWT via l'issuer du realm et contrôle l'audience `dove-api`. La [documentation Keycloak](https://www.keycloak.org/securing-apps/javascript-adapter) confirme qu'un client navigateur doit être public et que ses URI de redirection et Web Origins doivent être restreints.

Configuration attendue dans chaque realm `DOVE` :

- client frontend public : `dove-web` par défaut, surchargeable avec `DOVE_KEYCLOAK_WEB_CLIENT_ID` lors de la génération frontend ;
- Client authentication : `Off` ;
- Standard flow : `On` ;
- PKCE : `S256` ;
- Valid Redirect URIs : uniquement les URL DOVE de l'environnement ;
- Web Origins : uniquement l'origine DOVE de l'environnement ;
- audience du token destiné à l'API : `dove-api`.

Le compte opérateur Keycloak communiqué n'est pas un identifiant d'exécution de l'application. Il ne doit être présent ni dans le frontend, ni dans le backend, ni dans un manifeste. Il sert uniquement à administrer Keycloak via le coffre-fort Sonatel.

Le endpoint OpenID de production a répondu correctement pendant la validation. Celui de préproduction présente, depuis le poste de développement, une chaîne de certificats non reconnue. Il faut installer la CA Sonatel sur les postes/pods concernés ; ne pas utiliser l'option `-k` et ne pas désactiver la validation TLS.

## Construire et changer le frontend

Le même code Angular est construit en mode production, puis un fichier `runtime-config.json` propre à l'environnement est placé dans l'artefact :

```sh
cd DoveFront
npm run build:preprod
npm run build:prod
```

Les modèles sont dans :

- `config/runtime/preprod.json`
- `config/runtime/prod.json`

Ils ne contiennent aucun secret. Les valeurs non sensibles peuvent être surchargées pendant le build :

```sh
DOVE_FRONT_API_URL=https://api.dove.example.sn/api/v1 \
DOVE_KEYCLOAK_WEB_CLIENT_ID=dove-web \
npm run build:prod
```

En déploiement immuable, il est également possible de monter le fichier correspondant par `ConfigMap` à l'emplacement `/runtime-config.json` servi par Nginx. Le frontend refuse maintenant de démarrer si cette configuration est absente ou invalide, afin d'éviter une connexion accidentelle au mauvais Keycloak.

## Contrôles avant mise en service

```sh
cd api-dove
./scripts/run-environment.sh preprod

cd ../DoveFront
npm run typecheck
npm run build:preprod
```

Puis vérifier :

1. connexion Keycloak et audience du JWT ;
2. endpoint `/management/health/readiness` ;
3. création, lecture et suppression d'un média test dans S3 ;
4. migrations présentes dans `DATABASECHANGELOG` ;
5. absence de secret avec le scanner de secrets de la CI ;
6. sauvegarde et procédure de rollback base de données.

## Rotation obligatoire

Les identifiants transmis dans une conversation doivent être considérés comme exposés. Avant la mise en production, régénérer les clés S3 et les mots de passe MySQL/Keycloak, mettre à jour le coffre-fort ou les Secrets OpenShift, redémarrer progressivement les pods, puis révoquer les anciennes valeurs.

Références générales : [configuration externalisée Spring Boot](https://docs.spring.io/spring-boot/reference/features/external-config.html), [profils Spring Boot](https://docs.spring.io/spring-boot/reference/features/profiles.html).
