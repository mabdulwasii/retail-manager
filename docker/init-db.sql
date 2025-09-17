-- Initialize databases for Shop Manager and Keycloak

-- Create Shop Manager database (shopdb is already created as default DB)
-- But we need to ensure the shop user has proper permissions

-- Create shop user if it doesn't exist (it's created by POSTGRES_USER but let's ensure permissions)
-- The shop user is already created by Docker, so we just need to ensure it has access to shopdb

-- Create Keycloak database and user
CREATE DATABASE keycloak;
CREATE USER keycloak WITH PASSWORD 'keycloak';
GRANT ALL PRIVILEGES ON DATABASE keycloak TO keycloak;

-- Set ownership and permissions for Keycloak
\c keycloak
GRANT ALL ON SCHEMA public TO keycloak;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO keycloak;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO keycloak;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO keycloak;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO keycloak;

-- Switch back to shopdb and ensure shop user has proper permissions
\c shopdb
GRANT ALL ON SCHEMA public TO shop;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO shop;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO shop;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO shop;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO shop;