-- User anlegen (Passwort später via ENV, nicht im Repo!)
CREATE USER '${PLUSER}' WITH PASSWORD '${PLUSER_PASSWORD}';

-- CREATE DATABASE tour_planner OWNER tpuser;

CREATE DATABASE paperless ;
CREATE DATABASE paperless_test;
\c paperless;

CREATE SCHEMA dms AUTHORIZATION '${PLUSER}';

-- Optional: Zugriff auf public-Schema einschränken
REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM '${PLUSER}';

-- Default-Schema für User setzen
ALTER ROLE '${PLUSER}' SET search_path TO dms;

GRANT CONNECT ON DATABASE paperless TO '${PLUSER}';
