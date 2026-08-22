# Tests rapides du backend DOVE

Le contrat complet est dans [docs/API.md](docs/API.md). Les anciennes routes JHipster `/api/*`
sont fermées ; tester uniquement `/api/v1/*`.

## Démarrer

```bash
./mvnw -Dspring-boot.run.profiles=local spring-boot:run
```

Le profil local utilise H2, le stockage `.dove-storage` et le header de développement. Exemple avec
le compte métier par défaut :

```bash
curl -H 'X-Dove-User-Id: 85006628-0dd6-5586-a35c-b1cb40b9de1f' \
  http://localhost:8080/api/v1/me
```

## Vérifications essentielles

```bash
curl http://localhost:8080/management/health

curl -H 'X-Dove-User-Id: 85006628-0dd6-5586-a35c-b1cb40b9de1f' \
  'http://localhost:8080/api/v1/contents?q=cr%C3%A9er%20une%20commande'

curl -H 'X-Dove-User-Id: ea73e434-1b6f-5926-b73a-034abc289ee2' \
  'http://localhost:8080/api/v1/contents?size=100&browseOtherBusinessJobs=true'
```

Comptes utiles du seed :

| Profil          | UUID                                   |
| --------------- | -------------------------------------- |
| Métier          | `85006628-0dd6-5586-a35c-b1cb40b9de1f` |
| Nandité         | `6528b0b9-933f-5b5e-850f-afee220eebf3` |
| User Enablement | `ea73e434-1b6f-5926-b73a-034abc289ee2` |
| Admin           | `deb21cf4-5317-5065-9952-bd129b531a1b` |

## Qualité

```bash
./mvnw -DskipTests compile
./mvnw verify
./mvnw -Pprod clean verify
```

`verify` utilise les tests d'intégration JHipster/Testcontainers et nécessite un daemon Docker.
Pour la production, utiliser Java 21 et ne pas passer `-Denforcer.skip=true`.

L'authentification Keycloak réelle ne doit pas utiliser le password grant. Le flux navigateur
supporté est Authorization Code + PKCE `S256`, décrit dans
[docs/AUTHENTICATION.md](docs/AUTHENTICATION.md).
