# Stockage des vidéos, images et fiches DOVE

DOVE ne stocke pas les octets dans PostgreSQL. La base conserve uniquement l'identifiant du média,
son état, son type MIME, sa taille et sa `storageKey`. `ObjectStorageService` isole le backend du
fournisseur réel.

## Modes disponibles

### Local

Le profil `local` écrit dans `.dove-storage` et expose temporairement :

```text
PUT /api/v1/media/uploads/{uploadId}/content
GET /api/v1/media/content/{storageKey}
```

Ce mode sert au développement et aux tests ; il n'est pas destiné à un cluster de production.
Les chemins sont normalisés pour empêcher une sortie du répertoire de stockage.

### Passerelle cloud

Le profil `prod` utilise `GatewayObjectStorageService`. La passerelle Sonatel peut ensuite cibler
S3, OpenStack Swift, Azure Blob ou un autre service sans modifier le frontend ni les contrôleurs
DOVE.

```text
DOVE_STORAGE_PROVIDER=gateway
DOVE_STORAGE_GATEWAY_URL=https://storage-gateway.internal
DOVE_STORAGE_GATEWAY_API_KEY=<secret>
DOVE_STORAGE_MAX_UPLOAD_BYTES=536870912
```

La clé d'API appartient uniquement au backend et doit être injectée par le gestionnaire de secrets.

## Flux d'upload

```text
Angular
  1. POST /api/v1/media/uploads (métadonnées + Bearer DOVE)
DOVE API
  2. demande un ticket à POST {gateway}/uploads
  3. retourne uploadId, mediaId, uploadUrl et requiredHeaders
Angular
  4. PUT direct du binaire vers uploadUrl, sans Bearer Keycloak
  5. POST /api/v1/media/uploads/{uploadId}/complete
DOVE API
  6. confirme à POST {gateway}/uploads/{uploadId}/complete
  7. passe le média à READY
```

Le transfert direct évite de faire transiter de grandes vidéos par la JVM en production.

Contrat attendu de la passerelle pour `POST /uploads` :

```json
{
  "uploadId": "…",
  "mediaId": "…",
  "fileName": "commande.mp4",
  "mimeType": "video/mp4",
  "sizeBytes": 10485760
}
```

Réponse :

```json
{
  "uploadUrl": "https://object-storage/signed-put-url",
  "requiredHeaders": { "Content-Type": "video/mp4" },
  "expiresAt": "2026-08-22T18:00:00Z",
  "storageKey": "dove/medias/…/commande.mp4"
}
```

Autres appels de passerelle :

| Méthode | Route passerelle               | Usage                             |
| ------- | ------------------------------ | --------------------------------- |
| POST    | `/uploads/{uploadId}/complete` | corps `{ "storageKey": "…" }`     |
| GET     | `/objects/playback-url?key=…`  | retourne `{ "url": "https://…" }` |
| DELETE  | `/objects?key=…`               | supprime un objet non référencé   |

## Lecture

`GET /api/v1/media/{id}/playback-url` demande à la passerelle une URL de lecture signée. Le
frontend l'utilise directement. Une URL doit avoir une durée courte, être limitée à un seul objet
et être servie en HTTPS.

Pour les vidéos, la plateforme de stockage/CDN doit supporter les requêtes `Range`, un
`Content-Type` correct et, si nécessaire, la diffusion HLS/DASH après transcodage. Le modèle actuel
accepte une URL prête à lire ; une chaîne asynchrone peut conserver `PROCESSING` jusqu'à la fin du
scan antivirus et du transcodage, puis passer à `READY`.

## Règles de sécurité à appliquer à la passerelle

- bucket/conteneur privé, aucune liste publique ;
- URL signées courtes pour upload et lecture ;
- clé objet générée côté service, jamais un chemin fourni tel quel par le navigateur ;
- limite de taille vérifiée avant signature et par le stockage ;
- liste blanche MIME et vérification réelle du contenu après upload ;
- scan antivirus pour PDF/images et contrôle codec/conteneur pour vidéo ;
- chiffrement TLS en transit et chiffrement géré au repos ;
- CORS du bucket limité à l'origine DOVE et aux méthodes/headers nécessaires ;
- journalisation des créations, lectures administratives et suppressions ;
- politique de rétention et suppression conforme aux règles Sonatel ;
- lifecycle pour supprimer les uploads incomplets/expirés ;
- réplication, sauvegarde ou versioning selon le RPO/RTO attendu.

## Limites applicatives actuelles

Types logiques acceptés : `VIDEO`, `IMAGE`, `DOCUMENT`.

MIME acceptés :

```text
video/mp4
video/quicktime
image/png
image/jpeg
image/webp
application/pdf
application/octet-stream
```

La limite par défaut est 512 Mio et se configure avec `DOVE_STORAGE_MAX_UPLOAD_BYTES`. Le reverse
proxy, la passerelle et le fournisseur objet doivent appliquer une limite cohérente.

La suppression via DOVE est refusée avec `409` si le média est encore référencé par un contenu.
La base doit être mise à jour uniquement après confirmation de la passerelle ; les échecs réseau
doivent être supervisés et rejoués de manière contrôlée par la plateforme.

## Test de recette recommandé

Pour chaque environnement :

1. préparer une image, un PDF et une vidéo proches des tailles limites ;
2. vérifier le refus d'un MIME et d'une taille interdits ;
3. laisser expirer une URL et vérifier son refus ;
4. uploader, finaliser puis lire l'objet ;
5. vérifier `Range` sur une vidéo ;
6. tester une suppression non référencée et le `409` d'un média référencé ;
7. confirmer qu'aucun header `Authorization: Bearer <Keycloak>` n'arrive au domaine objet ;
8. contrôler les logs, métriques, alarmes et le nettoyage des uploads incomplets.
