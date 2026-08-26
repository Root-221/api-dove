# Configuration complète de Keycloak local pour DOVE

Ce guide permet de démarrer la configuration fournie avec le projet ou de la reproduire
manuellement dans la console Keycloak.

## Responsabilités

La séparation retenue est volontaire :

| Composant | Responsabilité |
|---|---|
| Keycloak | authentifier l’utilisateur et émettre un JWT signé |
| Backend DOVE | retrouver ou créer automatiquement le profil à partir du `JWT.sub` |
| Backend DOVE | gérer rôle, permissions, statut, Business Unit, métier, applications et modules |
| Frontend DOVE | afficher les capacités renvoyées par `GET /api/v1/me` |

Keycloak ne contient donc aucun rôle fonctionnel `BUSINESS_USER`, `NANDITE`, `USER_ENABLEMENT` ou
`ADMIN`. Créer un Realm Role portant l’un de ces noms ne donne aucun droit dans DOVE.

### Où sont gérés les rôles et permissions

La matrice d’autorisation qui fait foi est
[`DovePermissions.java`](../src/main/java/sn/dove/backend/dove/security/DovePermissions.java). Elle
associe chaque rôle DOVE aux permissions accordées par le backend. Les règles de cohérence des
périmètres sont dans
[`DoveScopeConsistencyService.java`](../src/main/java/sn/dove/backend/dove/service/DoveScopeConsistencyService.java),
et les types affichés par Angular sont déclarés dans
[`user.ts`](../../DoveFront/src/app/core/domain/user.ts).

Le rôle choisi pour une personne et son périmètre sont enregistrés dans son profil DOVE. Modifier
la matrice de permissions exige donc une modification de code et des tests ; affecter un rôle à un
utilisateur se fait depuis **Administration → Utilisateurs**. Aucune de ces deux opérations ne se
fait dans Keycloak.

Le token attendu contient essentiellement :

```json
{
  "sub": "identifiant-stable-de-l-utilisateur",
  "preferred_username": "admin.dove",
  "email": "admin@dove.local",
  "aud": "dove-api",
  "iss": "http://localhost:9080/realms/DOVE"
}
```

## Configuration déjà fournie

Le fichier [`DOVE-realm.json`](../src/main/docker/realm-config/DOVE-realm.json) importe :

- le realm `DOVE` ;
- le client public `dove-web` ;
- Authorization Code Flow ;
- PKCE `S256` obligatoire ;
- l’audience API `dove-api` ;
- les URLs locales autorisées ;
- le thème de connexion `dove` et la langue française ;
- une identité locale initiale `admin.dove` ;
- aucun rôle métier Keycloak.

Le fichier [`keycloak.yml`](../src/main/docker/keycloak.yml) expose Keycloak uniquement sur
`127.0.0.1` et conserve sa base locale dans un volume Docker nommé. Les utilisateurs ajoutés dans
la console survivent donc à `npm run keycloak:stop`.

## Page de connexion DOVE

La page générique de Keycloak est remplacée en local par le thème
[`themes/dove`](../src/main/docker/themes/dove). Il reprend la page de connexion DOVE du frontend :

- le formulaire `Login` / `Mot de passe` ;
- le bouton orange **Se connecter** ;
- le logo et l’animation visuelle DOVE ;
- le même rendu pour les actions Keycloak, notamment le changement de mot de passe temporaire ;
- une mise en page adaptée aux écrans mobiles.

Le navigateur est toujours redirigé techniquement vers `http://localhost:9080` pendant
l’authentification. C’est volontaire : le formulaire affiché est celui de DOVE, mais le mot de
passe est envoyé directement à Keycloak. Angular et le backend DOVE ne voient, ne transportent et
ne stockent jamais ce mot de passe.

Il ne faut pas activer **Direct access grants** pour essayer de poster le login et le mot de passe
depuis Angular. Ce flux supprimerait plusieurs protections d’OpenID Connect et compliquerait le
MFA. Le thème Keycloak permet de conserver l’interface DOVE tout en gardant Authorization Code +
PKCE `S256`.

### Modifier le thème en local

Les fichiers se trouvent ici :

```text
src/main/docker/themes/dove/login/
├── login.ftl
├── template.ftl
├── theme.properties
├── messages/
└── resources/
    ├── css/dove-login.css
    ├── img/logoDove.png
    └── js/dove-login.js
```

Le cache des thèmes est désactivé uniquement dans `keycloak.yml` pour faciliter le développement.
Après une modification, actualiser la page de connexion. Si nécessaire :

```bash
cd /Users/mac/Documents/ProjetDove/DoveFront
npm run keycloak:stop
npm run keycloak:start
```

Ne pas lancer `npm run keycloak:reset` pour une simple modification visuelle : cette commande
efface les utilisateurs locaux.

### Préproduction et production

Le thème placé dans ce dépôt configure le conteneur Keycloak **local**. Pour afficher la même page
sur le Keycloak mutualisé Sonatel, l’équipe IAM doit :

1. empaqueter ou monter le dossier `themes/dove` dans les pods Keycloak ;
2. redémarrer proprement les pods Keycloak selon sa procédure ;
3. sélectionner **Realm settings → Themes → Login theme → dove** dans le realm `DOVE` ;
4. vérifier l’écran de connexion, le mot de passe oublié, le MFA et les actions obligatoires.

Les options de désactivation du cache présentes dans `keycloak.yml` sont réservées au local et ne
doivent pas être reprises telles quelles en production.

### Première migration depuis l’ancienne configuration

Sur un poste qui possède déjà l’ancien conteneur sans volume, `npm run keycloak:start` s’arrête
volontairement avec le message **Migration Keycloak locale requise**. Cette protection évite de
perdre silencieusement les identités créées dans la console, par exemple `backary`.

Avant la migration :

1. ouvrir la liste **Users** du realm `DOVE` ;
2. noter les usernames et emails des utilisateurs à recréer ;
3. lancer `npm run keycloak:reset` depuis `DoveFront` ;
4. recréer uniquement les identités nécessaires en suivant la section
   [Créer une identité Keycloak](#créer-une-identité-keycloak).
5. demander à chaque utilisateur de se connecter une première fois à DOVE.

La réinitialisation retire aussi les anciens Realm Roles fonctionnels devenus inutiles. Le profil,
le rôle et le périmètre DOVE ne sont pas supprimés de la base applicative.

## Démarrage recommandé

Prérequis :

- Docker Desktop démarré ;
- Java 21 ;
- Node 22.22.3, 24.15 ou une version paire plus récente prise en charge ;
- ports `4200`, `8080` et `9080` disponibles.

```bash
cd /Users/mac/Documents/ProjetDove/DoveFront
npm ci
npm run dev:keycloak
```

Services disponibles :

| Service | URL |
|---|---|
| Frontend DOVE | `http://localhost:4200` |
| Backend DOVE | `http://localhost:8080` |
| Santé backend | `http://localhost:8080/management/health/readiness` |
| Keycloak | `http://localhost:9080` |
| Console Keycloak | `http://localhost:9080/admin/` |
| OpenID Discovery | `http://localhost:9080/realms/DOVE/.well-known/openid-configuration` |

Compte applicatif initial :

```text
Utilisateur : admin.dove
Mot de passe : DoveLocal123!
```

Compte de la console Keycloak :

```text
Utilisateur : admin
Mot de passe : admin
```

Ces mots de passe sont réservés au poste local et ne doivent jamais être réutilisés ailleurs.

## Commandes utiles

Depuis `DoveFront` :

```bash
npm run keycloak:start
npm run keycloak:status
npm run keycloak:logs
npm run keycloak:stop
```

Pour remettre Keycloak exactement dans l’état décrit par `DOVE-realm.json` :

```bash
npm run keycloak:reset
```

Attention : `keycloak:reset` supprime le volume local Keycloak. Tous les utilisateurs et réglages
créés uniquement dans la console sont effacés, puis le realm source est réimporté.

## Création manuelle du realm

Cette partie sert à comprendre ou reproduire la configuration. Avec le projet, l’import JSON le
fait déjà automatiquement.

1. Ouvrir `http://localhost:9080/admin/`.
2. Se connecter au realm `master` avec le compte administrateur local.
3. Ouvrir le sélecteur de realm en haut à gauche.
4. Cliquer sur **Create realm**.
5. Saisir `DOVE` dans **Realm name**.
6. Laisser **Enabled** activé puis créer le realm.

Dans **Realm settings → Login** :

```text
User registration        Off
Forgot password          Off en local
Remember me              On
Login with email         On
Duplicate emails         Off
Verify email             Off en local
```

En préproduction et production, les politiques MFA, email et mot de passe sont pilotées par
l’équipe IAM Sonatel.

## Création manuelle du client `dove-web`

Dans le realm `DOVE` :

1. Ouvrir **Clients**.
2. Cliquer sur **Create client**.
3. Choisir **OpenID Connect**.
4. Saisir `dove-web` comme **Client ID**.
5. Continuer avec les réglages suivants.

### Capability config

```text
Client authentication   Off
Authorization           Off
Standard flow           On
Direct access grants    Off
Implicit flow           Off
Service accounts roles  Off
```

`Client authentication` doit absolument rester sur `Off`. Angular est une application publique et
ne peut pas protéger un secret client. Si cette option est activée, le endpoint token répond
`401 invalid_client_credentials`.

### Login settings

```text
Root URL:
http://localhost:4200

Home URL:
http://localhost:4200

Valid Redirect URIs:
http://localhost:4200/*
http://127.0.0.1:4200/*

Valid Post Logout Redirect URIs:
http://localhost:4200/*
http://127.0.0.1:4200/*

Web Origins:
http://localhost:4200
http://127.0.0.1:4200
```

Ne jamais utiliser un joker global `*` hors d’un test jetable.

### PKCE

Dans les paramètres avancés du client :

```text
Proof Key for Code Exchange Code Challenge Method : S256
```

## Ajouter l’audience `dove-api`

Le backend refuse un token qui n’est pas destiné à l’API DOVE.

1. Ouvrir **Clients → dove-web**.
2. Ouvrir **Client scopes**.
3. Ouvrir le scope dédié `dove-web-dedicated`.
4. Ouvrir **Mappers**.
5. Choisir **Configure a new mapper → Audience**.
6. Utiliser les valeurs suivantes :

```text
Name                       dove-api-audience
Included Custom Audience   dove-api
Add to access token        On
Add to ID token            Off
Add to userinfo            Off
```

Il n’est pas nécessaire de créer un rôle `dove-api`. Il s’agit d’une audience technique du token.

## Créer une identité Keycloak

Dans le realm `DOVE` :

> Avant de créer l’utilisateur, vérifier que le sélecteur de realm en haut à gauche affiche bien
> **DOVE** et non **master**. Un utilisateur créé dans `master` ne peut pas se connecter à
> l’application DOVE.

1. Ouvrir **Users**.
2. Cliquer sur **Create new user**.
3. Renseigner `Username`, `Email`, `First name` et `Last name`.
4. Activer **Enabled**.
5. En local, activer **Email verified** pour éviter un parcours email inutile.
6. Enregistrer.
7. Ouvrir l’onglet **Credentials**.
8. Cliquer sur **Set password**.
9. Saisir un mot de passe uniquement local.
10. Désactiver **Temporary** si le changement au premier login n’est pas souhaité.

Ne rien ajouter dans **Role mapping** pour les rôles métier DOVE. Keycloak authentifie seulement
l’identité.

## Première connexion et création automatique du profil DOVE

Il n’y a aucun `sub` à copier et aucun profil à créer manuellement dans DOVE.

Lors de la première connexion :

1. Keycloak authentifie l’utilisateur et émet le JWT ;
2. le backend vérifie la signature, l’issuer, l’expiration et l’audience ;
3. si le `sub` n’existe pas encore, DOVE crée automatiquement un profil actif ;
4. le rôle est toujours forcé à `BUSINESS_USER`, quels que soient les rôles présents dans Keycloak ;
5. le profil est créé sans métier ni Business Unit ;
6. l’utilisateur peut entrer dans DOVE, mais son catalogue reste vide jusqu’à son rattachement ;
7. l’administrateur ouvre **Administration → Utilisateurs → Gérer** et lui attribue le rôle et le périmètre adaptés.

La liaison interne, invisible dans le formulaire, est créée par le backend :

```text
Keycloak user.id / token.sub
        =
DOVE utilisateur.externalSubject
```

L’email, le prénom et le nom viennent du JWT. L’email n’est jamais utilisé comme mécanisme de
liaison de sécurité.

## Exemple de répartition

| Keycloak | DOVE |
|---|---|
| username, email, prénom, nom, mot de passe | rôle fonctionnel |
| `sub` stable | permissions supplémentaires |
| session SSO et MFA | Business Unit et métier |
| signature et durée du JWT | applications et modules visibles |
| audience `dove-api` | statut actif/désactivé |

Un changement de rôle dans DOVE est effectif sans modifier Keycloak. Désactiver un utilisateur dans
DOVE bloque ses accès métier même si sa session Keycloak reste techniquement valide.

## Parcours technique de connexion

```text
Navigateur
  → Authorization Code + PKCE S256
Keycloak /realms/DOVE
  → JWT signé : sub + email + aud=dove-api
Angular
  → Authorization: Bearer <JWT>
Spring Security
  → signature + issuer + expiration + audience
DoveCurrentUserService
  → recherche externalSubject == sub
  → absent : création ACTIVE + BUSINESS_USER + périmètre vide
DOVE
  → statut + rôle + permissions + périmètre
```

## Démarrage séparé

Terminal 1 :

```bash
cd /Users/mac/Documents/ProjetDove/DoveFront
npm run keycloak:start
```

Terminal 2 :

```bash
cd /Users/mac/Documents/ProjetDove/api-dove
./scripts/run-environment.sh local-keycloak
```

Terminal 3 :

```bash
cd /Users/mac/Documents/ProjetDove/DoveFront
npm run start:keycloak
```

## Diagnostic

```bash
curl http://localhost:9080/realms/DOVE/.well-known/openid-configuration
curl http://localhost:8080/management/health/readiness
```

Comportements attendus :

- santé backend : `200` et `UP` ;
- `/api/v1/me` sans JWT : `401` ;
- token invalide, expiré ou mauvaise audience : `401` ;
- identité Keycloak valide et inconnue : création automatique en `BUSINESS_USER` ;
- compte DOVE désactivé ou hors périmètre : `403` ;
- après connexion : `/api/v1/me` renvoie le rôle et les permissions DOVE.

Erreurs courantes :

| Erreur | Cause probable | Correction |
|---|---|---|
| `invalid_client_credentials` | Client authentication activé | remettre `dove-web` en client public |
| `invalid_redirect_uri` | URL Angular absente | ajouter l’URL exacte dans Valid Redirect URIs |
| `client_not_found` | mauvais Client ID | utiliser exactement `dove-web` |
| `invalid_token`, audience manquante | mapper absent | ajouter l’audience `dove-api` |
| catalogue vide au premier login | aucun métier encore attribué | l’Admin attribue le métier ou le nouveau rôle dans DOVE |
| port déjà utilisé | ancien processus actif | arrêter le processus sur `4200`, `8080` ou `9080` |

L’avertissement navigateur relatif au sandbox de `silent-check-sso.html` n’est pas un échec
d’authentification. Le diagnostic doit se baser sur le statut du endpoint token et les événements
Keycloak.

## Passage à la préproduction

En préproduction, conserver exactement le même principe mais remplacer :

- l’issuer local par l’issuer Sonatel du realm `DOVE` ;
- les URLs `localhost` par les routes publiques préproduction ;
- les comptes locaux par les identités Sonatel ;
- les mots de passe locaux par les politiques IAM/MFA.

Ne jamais copier le compte administrateur local, les mots de passe locaux ou la base du conteneur
vers OpenShift.
