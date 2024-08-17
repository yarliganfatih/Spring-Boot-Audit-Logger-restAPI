DELETE FROM audit_update_logs;
DELETE FROM audit_entity_logs;
DELETE FROM user_roles;
DELETE FROM roles;
ALTER TABLE roles ALTER COLUMN id RESTART WITH 1;
DELETE FROM users;
ALTER TABLE users ALTER COLUMN id RESTART WITH 1;
DELETE FROM oauth_client_details;
