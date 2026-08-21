# Guide de Test et Endpoints - Backend DOVE

Ce document récapitule toutes les informations nécessaires pour démarrer, authentifier et tester l'API Backend **DOVE** (Spring Boot 4 / JHipster 9.2 / Java 21).

---

## 1. Démarrage des Services & du Backend

### Étape 1 : Démarrer la base PostgreSQL et Keycloak (Docker)

Dans le dossier du backend (`Dove`) :

```powershell
docker compose -f src/main/docker/postgresql.yml up -d
docker compose -f src/main/docker/keycloak.yml up -d
```

### Étape 2 : Lancer le Backend Spring Boot

Sous Windows (PowerShell) :

```powershell
.\mvnw.cmd
```

Le serveur démarre sur le port **8080** : `http://localhost:8080`

---

## 2. Documentation interactive & Swagger

| Service                          | Méthode | URL                                                                                        | Description                                          |
| :------------------------------- | :-----: | :----------------------------------------------------------------------------------------- | :--------------------------------------------------- |
| **Swagger UI**                   |  `GET`  | [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html) | Interface interactive pour tester les endpoints REST |
| **Swagger UI (Court)**           |  `GET`  | [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)             | Redirection vers Swagger UI                          |
| **Spécification OpenAPI (JSON)** |  `GET`  | [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)                     | Schéma OpenAPI v3 complet du backend                 |

> **Note :** En profil `dev`, `api-docs` est activé par défaut.

---

## 3. Authentification Keycloak (OAuth2 / OIDC)

Le backend délègue l'authentification et les rôles à **Keycloak** sur le port **9080**.

| Service                    | URL                                                                                                                                              | Identifiants par défaut          |
| :------------------------- | :----------------------------------------------------------------------------------------------------------------------------------------------- | :------------------------------- |
| **Console Admin Keycloak** | [http://localhost:9080](http://localhost:9080)                                                                                                   | `admin` / `admin`                |
| **Realm JHipster (DOVE)**  | [http://localhost:9080/realms/jhipster](http://localhost:9080/realms/jhipster)                                                                   | `admin`/`admin` ou `user`/`user` |
| **Discovery OIDC**         | [http://localhost:9080/realms/jhipster/.well-known/openid-configuration](http://localhost:9080/realms/jhipster/.well-known/openid-configuration) | Public                           |

### Obtenir un Token JWT pour vos tests API (Postman / cURL)

```bash
curl -X POST "http://localhost:9080/realms/jhipster/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=web_app" \
  -d "client_secret=web_app" \
  -d "username=admin" \
  -d "password=admin"
```

Utilisez le `access_token` retourné dans le header `Authorization: Bearer <TOKEN>` lors de vos requêtes REST.

---

## 4. Endpoints Publics & Santé (Actuator)

| Description                         | Méthode | URL                                                                                        |
| :---------------------------------- | :-----: | :----------------------------------------------------------------------------------------- |
| **Santé de l'application (Health)** |  `GET`  | [http://localhost:8080/management/health](http://localhost:8080/management/health)         |
| **Informations de version & build** |  `GET`  | [http://localhost:8080/management/info](http://localhost:8080/management/info)             |
| **Métriques Prometheus**            |  `GET`  | [http://localhost:8080/management/prometheus](http://localhost:8080/management/prometheus) |
| **Configuration OIDC exposée**      |  `GET`  | [http://localhost:8080/api/auth-info](http://localhost:8080/api/auth-info)                 |

---

## 5. Endpoints REST Métiers (`/api`)

_Requiert l'en-tête `Authorization: Bearer <TOKEN>`_

| Ressource                 | Endpoint                                         |           Méthodes supportées           | Description                         |
| :------------------------ | :----------------------------------------------- | :-------------------------------------: | :---------------------------------- |
| **Utilisateurs**          | `http://localhost:8080/api/utilisateurs`         | `GET`, `POST`, `PUT`, `PATCH`, `DELETE` | Gestion des comptes et périmètres   |
| **Rôles**                 | `http://localhost:8080/api/roles`                | `GET`, `POST`, `PUT`, `PATCH`, `DELETE` | Rôles applicatifs                   |
| **Utilisateur-Rôles**     | `http://localhost:8080/api/utilisateur-roles`    | `GET`, `POST`, `PUT`, `PATCH`, `DELETE` | Associations utilisateurs - rôles   |
| **Contenus**              | `http://localhost:8080/api/contenus`             | `GET`, `POST`, `PUT`, `PATCH`, `DELETE` | Fiches pratiques, vidéos, contenus  |
| **Applications**          | `http://localhost:8080/api/applications`         | `GET`, `POST`, `PUT`, `PATCH`, `DELETE` | Référentiel des applications        |
| **Modules**               | `http://localhost:8080/api/modules`              | `GET`, `POST`, `PUT`, `PATCH`, `DELETE` | Modules applicatifs                 |
| **Métiers**               | `http://localhost:8080/api/metiers`              | `GET`, `POST`, `PUT`, `PATCH`, `DELETE` | Référentiel des métiers             |
| **Médias**                | `http://localhost:8080/api/media`                | `GET`, `POST`, `PUT`, `PATCH`, `DELETE` | Vidéos, images, miniatures          |
| **FAQ**                   | `http://localhost:8080/api/faqs`                 | `GET`, `POST`, `PUT`, `PATCH`, `DELETE` | Questions / réponses associées      |
| **Discussions**           | `http://localhost:8080/api/discussions`          | `GET`, `POST`, `PUT`, `PATCH`, `DELETE` | Fils d'échange Nandité              |
| **Messages**              | `http://localhost:8080/api/messages`             | `GET`, `POST`, `PUT`, `PATCH`, `DELETE` | Messages et réponses                |
| **Communautés Nandité**   | `http://localhost:8080/api/communaute-nandites`  | `GET`, `POST`, `PUT`, `PATCH`, `DELETE` | Espaces communautaires              |
| **Feedbacks**             | `http://localhost:8080/api/feed-backs`           | `GET`, `POST`, `PUT`, `PATCH`, `DELETE` | Retours et avis des utilisateurs    |
| **Réactions**             | `http://localhost:8080/api/reactions`            | `GET`, `POST`, `PUT`, `PATCH`, `DELETE` | Likes et réactions                  |
| **Signalements**          | `http://localhost:8080/api/signalements`         | `GET`, `POST`, `PUT`, `PATCH`, `DELETE` | Signalements de contenus            |
| **Signalements Messages** | `http://localhost:8080/api/signalement-messages` | `GET`, `POST`, `PUT`, `PATCH`, `DELETE` | Signalements de messages            |
| **Compte connecté**       | `http://localhost:8080/api/account`              |                  `GET`                  | Données de session de l'utilisateur |

---

## 6. Exemples de tests rapides en ligne de commande

### Tester la santé du service :

```powershell
curl.exe -i http://localhost:8080/management/health
```

### Tester les informations d'authentification :

```powershell
curl.exe -i http://localhost:8080/api/auth-info
```
