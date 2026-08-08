CREATE TABLE auth_users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    email VARCHAR(320) NOT NULL,
    password_hash VARCHAR(60) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_auth_users PRIMARY KEY (id),
    CONSTRAINT uk_auth_users_email UNIQUE (email)
);
