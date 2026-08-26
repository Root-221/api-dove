# Stockage des vidéos, images et documents DOVE

DOVE ne stocke pas les octets dans MySQL. La base conserve l'identifiant du média, son état, son type MIME, sa taille et sa `storageKey`. `ObjectStorageService` isole le reste de l'application du stockage réel.

## Modes disponibles

### Local

Le profil `local` écrit dans `.dove-storage`. Ce mode sert au développement et aux tests ; il n'est pas destiné à un cluster multi-instance.

### S3 OpenShift

Les profils `preprod` et `prod` activent `S3ObjectStorageService`. Le client AWS SDK Java 2.x utilise :

- l'endpoint HTTPS S3 compatible OpenShift ;
- l'adressage path-style ;
- des credentials statiques injectés exclusivement par Secret OpenShift ;
- un bucket privé ;
- l'API DOVE comme proxy, car le DNS `*.svc` n'est pas joignable depuis le navigateur.

Les variables complètes figurent dans [CONFIGURATION_ENVIRONNEMENTS.md](CONFIGURATION_ENVIRONNEMENTS.md).

`GatewayObjectStorageService` reste disponible uniquement pour une future passerelle Sonatel, avec `DOVE_STORAGE_PROVIDER=gateway`. Il n'est utilisé par aucun profil cloud actuel.

## Flux d'upload S3

```text
Angular
  1. POST /api/v1/media/uploads avec les métadonnées et le Bearer DOVE
DOVE API
  2. génère uploadId, mediaId et une storageKey contrôlée
  3. retourne l'URL authentifiée PUT /api/v1/media/uploads/{uploadId}/content
Angular
  4. envoie le binaire à l'API DOVE
DOVE API
  5. vérifie Content-Length, MIME, propriétaire et limite de taille
  6. transmet le flux au bucket S3 interne
Angular
  7. POST /api/v1/media/uploads/{uploadId}/complete
DOVE API
  8. vérifie l'objet par HeadObject et passe le média à READY
```

Ce proxy est volontaire : exposer une URL présignée contenant `s3.openshift-storage.svc` produirait une URL inutilisable hors du cluster. Si une route S3 publique sécurisée est fournie plus tard, le service peut évoluer vers des URLs présignées sans changer le contrat Angular.

## Lecture et suppression

`GET /api/v1/media/{id}/playback-url` retourne une URL DOVE. `GET /api/v1/media/content/{storageKey}` diffuse ensuite l'objet depuis S3. Le bucket reste privé et son endpoint n'apparaît jamais côté client.

La suppression via DOVE appelle `DeleteObject`. Elle est refusée avec `409` si le média est encore référencé par un contenu.

## Sécurité attendue

- bucket privé, aucun accès ou listing anonyme ;
- identité S3 limitée au bucket DOVE et aux opérations `PutObject`, `GetObject`, `HeadObject`, `DeleteObject` ;
- clé objet UUID générée par le backend, jamais un chemin fourni par le navigateur ;
- HTTPS obligatoire et CA OpenShift/Sonatel installée dans le JRE ;
- credentials uniquement dans un Secret ou coffre-fort, avec rotation ;
- contrôle de taille préparée et reçue ;
- liste blanche MIME ;
- scan antivirus à ajouter pour les documents et images avant une exposition large ;
- journalisation des créations et suppressions ;
- politique de rétention/versioning alignée sur le RPO/RTO Sonatel.

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

La limite par défaut est 512 Mio et se configure avec `DOVE_STORAGE_MAX_UPLOAD_BYTES`. Le routeur OpenShift doit accepter une taille cohérente et un timeout suffisant pour les vidéos.

## Recette recommandée

Pour chaque environnement :

1. charger une image, un PDF et une vidéo ;
2. vérifier le refus d'un MIME interdit, d'une taille incorrecte et d'un `Content-Length` absent ;
3. finaliser et relire chaque objet ;
4. supprimer un média non référencé ;
5. vérifier le `409` d'un média encore référencé ;
6. confirmer dans les logs réseau que le navigateur ne contacte jamais le domaine S3 interne ;
7. contrôler la rotation des credentials, les métriques, alertes et sauvegardes.
