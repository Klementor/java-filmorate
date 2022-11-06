DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS genre CASCADE;
DROP TABLE IF EXISTS mpa CASCADE;
DROP TABLE IF EXISTS film CASCADE;
DROP TABLE IF EXISTS user_friends CASCADE;
DROP TABLE IF EXISTS film_likes CASCADE;
DROP TABLE IF EXISTS film_genre CASCADE;
DROP TABLE IF EXISTS director CASCADE;
DROP TABLE IF EXISTS directors CASCADE;
DROP TABLE IF EXISTS reviews CASCADE;
DROP TABLE IF EXISTS review_reactions CASCADE;
DROP TABLE IF EXISTS history_event CASCADE;

CREATE TABLE IF NOT EXISTS users
(
    id       INTEGER PRIMARY KEY,
    email    varchar(40)  NOT NULL,
    login    varchar(100) NOT NULL,
    name     varchar(100) NOT NULL,
    birthday date         NOT NULL,
    CONSTRAINT constr_birthday CHECK (birthday < now())
);

CREATE TABLE IF NOT EXISTS genre
(
    id   INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    name varchar(40) NOT NULL
);

CREATE TABLE IF NOT EXISTS mpa
(
    id   INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    name varchar(40) NOT NULL
);

CREATE TABLE IF NOT EXISTS film
(
    id           INTEGER PRIMARY KEY,
    name         varchar(100) NOT NULL,
    description  varchar(200) NOT NULL,
    release_date date         NOT NULL,
    duration     INTEGER      NOT NULL,
    mpa          INTEGER REFERENCES mpa (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS director
(
    director_id   INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    director_name varchar(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS directors
(
    film_id     INTEGER REFERENCES film (id) ON DELETE CASCADE,
    director_id INTEGER REFERENCES director (director_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS user_friends
(
    user_id    INTEGER REFERENCES users (id) ON DELETE CASCADE,
    friends_id INTEGER REFERENCES users (id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, friends_id)
);


CREATE TABLE IF NOT EXISTS film_likes
(
    user_id INTEGER REFERENCES users (id) ON DELETE CASCADE,
    film_id INTEGER REFERENCES film (id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, film_id)
);


CREATE TABLE IF NOT EXISTS film_genre
(
    film_id  INTEGER REFERENCES film (id) ON DELETE CASCADE,
    genre_id INTEGER REFERENCES genre (id) ON DELETE CASCADE,
    PRIMARY KEY (film_id, genre_id)
);

CREATE TABLE IF NOT EXISTS reviews
(
    id         INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    content    varchar(200) NOT NULL,
    positive BOOLEAN      NOT NULL,
    film_id    INTEGER REFERENCES film (id) ON DELETE CASCADE,
    user_id    INTEGER REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS review_reactions
(
    id        INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    user_id   INTEGER REFERENCES users (id) ON DELETE CASCADE,
    review_id INTEGER REFERENCES reviews (id) ON DELETE CASCADE,
    reaction  INTEGER DEFAULT 0
);

CREATE TABLE IF NOT EXISTS history_event (
    event_id INTEGER PRIMARY KEY AUTO_INCREMENT,
    user_id INTEGER REFERENCES users (id) ON DELETE CASCADE,
    event_type varchar(10) NOT NULL,
    operation varchar(10) NOT NULL,
    entity_id INTEGER NOT NULL,
    timestamp INTEGER NOT NULL
);