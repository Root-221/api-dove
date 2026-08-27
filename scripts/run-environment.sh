#!/bin/sh
set -eu

environment=${1:-local}

load_environment_file() {
  environment_file=$1
  environment_example=$2
  if [ ! -f "$environment_file" ]; then
    echo "Fichier de configuration absent: $environment_file" >&2
    echo "Copiez $environment_example vers $environment_file puis renseignez les secrets localement." >&2
    exit 1
  fi
  set -a
  # shellcheck disable=SC1090
  . "$environment_file"
  set +a
}

require_variables() {
  for variable_name in $1; do
    eval "variable_value=\${$variable_name:-}"
    case "$variable_value" in
      ''|__*)
        echo "Variable obligatoire absente ou non remplacée: $variable_name" >&2
        exit 1
        ;;
    esac
  done
}

case "$environment" in
  local|local-keycloak)
    env_file=${DOVE_ENV_FILE:-./.env.local}
    load_environment_file "$env_file" ./.env.local.example
    require_variables 'DOVE_DATABASE_URL DOVE_DATABASE_USERNAME DOVE_DATABASE_PASSWORD'

    case "$DOVE_DATABASE_URL" in
      jdbc:mysql://10.137.21.115:6446/MYSDOVEDEV\?*) ;;
      *)
        echo "En local, DOVE_DATABASE_URL doit viser MYSDOVEDEV sur 10.137.21.115:6446." >&2
        exit 1
        ;;
    esac

    exec sh mvnw -ntp -Denforcer.skip=true -Dspring-boot.run.profiles="$environment" spring-boot:run
    ;;
  preprod|prod)
    env_file=${DOVE_ENV_FILE:-./.env.$environment.local}
    load_environment_file "$env_file" "./.env.$environment.example"
    ;;
  *)
    echo "Environnement invalide: $environment (local, local-keycloak, preprod ou prod)" >&2
    exit 2
    ;;
esac

required_variables='DOVE_DATABASE_URL DOVE_DATABASE_USERNAME DOVE_DATABASE_PASSWORD DOVE_KEYCLOAK_ISSUER_URI DOVE_ALLOWED_ORIGINS DOVE_STORAGE_PUBLIC_BASE_URL DOVE_STORAGE_S3_ENDPOINT DOVE_STORAGE_S3_BUCKET DOVE_STORAGE_S3_ACCESS_KEY DOVE_STORAGE_S3_SECRET_KEY'
require_variables "$required_variables"

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
