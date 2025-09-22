#!/bin/bash
set -e

# Create application user
psql -v ON_ERROR_STOP=1 -U "${POSTGRES_USER}" <<-EOSQL
    CREATE USER "${PLUSER}" WITH PASSWORD '${PLUSER_PASSWORD}';
EOSQL

# Create databases
psql -v ON_ERROR_STOP=1 -U "${POSTGRES_USER}" <<-EOSQL
    CREATE DATABASE paperless;
    CREATE DATABASE paperless_test;
EOSQL

# Connect to paperless database and create schema + grant rights
psql -v ON_ERROR_STOP=1 -U "${POSTGRES_USER}" -d paperless <<-EOSQL
    CREATE SCHEMA dms AUTHORIZATION "${PLUSER}";

    REVOKE ALL ON SCHEMA public FROM PUBLIC;
    REVOKE ALL ON SCHEMA public FROM "${PLUSER}";

    ALTER ROLE "${PLUSER}" SET search_path TO dms;

    GRANT CONNECT ON DATABASE paperless TO "${PLUSER}";
EOSQL
