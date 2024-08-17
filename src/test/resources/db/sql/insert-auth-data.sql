INSERT INTO oauth_client_details (client_id, client_secret, web_server_redirect_uri, scope, access_token_validity, refresh_token_validity, resource_ids, authorized_grant_types, authorities, additional_information, autoapprove) 
VALUES ('mobile', '{bcrypt}$2a$10$gPhlXZfms0EpNHX0.HHptOhoFD1AoxSr/yUIdTqA8vtjeP4zi0DDu', 'http://localhost:8080/code', 'READ,WRITE', 3600, 10000, 'microservice', 'authorization_code,password,refresh_token,implicit', NULL, '{}', NULL);

INSERT INTO users (id, email, enabled, password, username) 
VALUES (1, 'user@gmail.com', '1', '{bcrypt}$2a$12$udISUXbLy9ng5wuFsrCMPeQIYzaKtAEXNJqzeprSuaty86N4m6emW', 'user');

INSERT INTO roles (id, name, level) VALUES (1, 'user', 1);
INSERT INTO roles (id, name, level) VALUES (2, 'mod', 5);
INSERT INTO roles (id, name, level) VALUES (3, 'alternative_mod', 5); -- at the same level, for different authorization from mod
INSERT INTO roles (id, name, level) VALUES (4, 'admin', 10);
INSERT INTO roles (id, name) VALUES (5, 'chosen'); -- independent of level hierarchy

INSERT INTO user_roles (user_id, role_id) VALUES (1, 1); -- user gets 'user' role