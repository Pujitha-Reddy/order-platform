#!/bin/bash
set -e

for DB in order_db inventory_db payment_db; do
  echo "Creating database: $DB"
  psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE DATABASE $DB;
EOSQL
done