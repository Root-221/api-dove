#!/bin/sh
set -eu

environment=${1:-local}

case "$environment" in
  local)
    exec sh mvnw -ntp -Denforcer.skip=true -Dspring-boot.run.profiles=local spring-boot:run
    ;;
  local-keycloak)
    exec sh mvnw -ntp -Denforcer.skip=true -Dspring-boot.run.profiles=local-keycloak spring-boot:run
    ;;
  preprod|prod)
    env_file=${DOVE_ENV_FILE:-.env.$environment.local}
    if [ ! -f "$env_file" ]; then
      echo "Fichier de configuration absent: $env_file" >&2
      echo "Copiez .env.$environment.example vers $env_file et renseignez les secrets." >&2
      exit 1
    fi
    set -a
    # shellcheck disable=SC1090
    . "$env_file"
    set +a
    ;;
  *)
    echo "Environnement invalide: $environment (local, local-keycloak, preprod ou prod)" >&2
    exit 2
    ;;
esac

required_variables='DOVE_DATABASE_URL DOVE_DATABASE_USERNAME DOVE_DATABASE_PASSWORD DOVE_KEYCLOAK_ISSUER_URI DOVE_ALLOWED_ORIGINS DOVE_STORAGE_PUBLIC_BASE_URL DOVE_STORAGE_S3_ENDPOINT DOVE_STORAGE_S3_BUCKET DOVE_STORAGE_S3_ACCESS_KEY DOVE_STORAGE_S3_SECRET_KEY'
for variable_name in $required_variables; do
  eval "variable_value=\${$variable_name:-}"
  case "$variable_value" in
    ''|__*)
      echo "Variable obligatoire absente ou non remplacée: $variable_name" >&2
      exit 1
      ;;
  esac
done

case "$DOVE_DATABASE_URL" in
  jdbc:mysql://*) ;;
  *) echo "DOVE_DATABASE_URL doit être une URL JDBC MySQL." >&2; exit 1 ;;
esac
case "$DOVE_KEYCLOAK_ISSUER_URI" in
  https://*/realms/DOVE) ;;
  *) echo "DOVE_KEYCLOAK_ISSUER_URI doit viser le realm DOVE en HTTPS." >&2; exit 1 ;;
esac
case "$DOVE_STORAGE_S3_ENDPOINT" in
  https://*) ;;
  *) echo "DOVE_STORAGE_S3_ENDPOINT doit utiliser HTTPS." >&2; exit 1 ;;
esac

exec sh mvnw -ntp -Denforcer.skip=true -Dspring-boot.run.profiles="$environment" spring-boot:run
