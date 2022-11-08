INSERT INTO "USERS"
(ID, NAME, LOGIN, EMAIL, BIRTHDAY)
VALUES (1, 'Anton', 'agabov', 'agabov@gmail.com', '1984-01-21'),
       (2, 'Dmitriy', 'dima', 'dima@gmail.com', '1985-05-15'),
       (3, 'Ivan', 'ivan', 'ivan@gmail.com', '1986-12-02'),
       (4, 'Konstantin', 'kostya', 'kostya@gmail.com', '1985-02-28'),
       (5, 'Petr', 'petya', 'petya@gmail.com', '1990-08-07');

INSERT INTO "USER_FRIENDS" (USER_ID, FRIENDS_ID)
VALUES(2,1);

INSERT INTO "USER_FRIENDS" (USER_ID, FRIENDS_ID)
VALUES(2,4);

INSERT INTO "USER_FRIENDS" (USER_ID, FRIENDS_ID)
VALUES(2,5);

INSERT INTO "USER_FRIENDS" (USER_ID, FRIENDS_ID)
VALUES(3,2);

INSERT INTO "USER_FRIENDS" (USER_ID, FRIENDS_ID)
VALUES(4,2);

INSERT INTO "USER_FRIENDS" (USER_ID, FRIENDS_ID)
VALUES(4,1);


INSERT INTO mpa (name) VALUES ('G'), ('PG'), ('PG-13'), ('R'), ('NC-17');
INSERT INTO genre (name) VALUES ('Комедия'), ('Драма'), ('Мультфильм'), ('Триллер'), ('Документальный'), ('Боевик');

INSERT INTO "FILM_GENRE" -- добавляем одновременно жанр фильма и сам фильм
(GENRE_ID, FILM_ID)
VALUES(SELECT ID FROM "GENRE"  WHERE NAME ='Драма',
          SELECT ID FROM FINAL TABLE(INSERT INTO "FILM"
          (ID, NAME, DESCRIPTION, DURATION, RELEASE_DATE,MPA)
              VALUES(1, 'Титаник', 'Грустная история', 90, '2006-01-20',1)));

INSERT INTO "FILM_GENRE"
(GENRE_ID, FILM_ID)
VALUES(SELECT ID FROM "GENRE"  WHERE NAME ='Мультфильм',
          SELECT ID FROM FINAL TABLE(INSERT INTO "FILM"
          (ID, NAME, DESCRIPTION, DURATION, RELEASE_DATE,MPA)
              VALUES(2, 'Босс-молокосос', 'детский мультик про ребенка из корпорации детей', 100, '2008-02-23',2)));

INSERT INTO "FILM_GENRE"
(GENRE_ID, FILM_ID)
VALUES(SELECT ID FROM "GENRE"  WHERE NAME ='Боевик',
          SELECT ID FROM FINAL TABLE(INSERT INTO "FILM"
          (ID, NAME, DESCRIPTION, DURATION, RELEASE_DATE,MPA)
              VALUES(3, 'Агент 007', 'экшн про секретного агента', 120, '2020-05-17',2)));

INSERT INTO "FILM_GENRE"
(GENRE_ID, FILM_ID)
VALUES(SELECT ID FROM "GENRE"  WHERE NAME ='Документальный',
          SELECT ID FROM FINAL TABLE(INSERT INTO "FILM"
          (ID, NAME, DESCRIPTION, DURATION, RELEASE_DATE,MPA)
              VALUES(4, 'Дикая природа Амазонки', 'документальный фильм про природу', 120, '2022-02-25',3)));

BEGIN TRANSACTION;
INSERT INTO "FILM_GENRE"  -- Добавляем фильм с двумя жанрами
(GENRE_ID, FILM_ID)
VALUES(SELECT ID FROM "GENRE"  WHERE NAME ='Комедия',
          SELECT ID FROM FINAL TABLE(INSERT INTO "FILM"
          (ID, NAME, DESCRIPTION, DURATION, RELEASE_DATE,MPA)
              VALUES(5, 'Я иду искать', 'фильм про невесту и ее будущих родтсвенников', 120, '2020-02-25',1)));

INSERT INTO  "FILM_GENRE" -- добавляем второй жанр фильма  для фильма "Я иду искать"
(GENRE_ID, FILM_ID)
VALUES(SELECT ID FROM "GENRE"  WHERE NAME ='Триллер',
          SELECT ID FROM "FILM" WHERE NAME ='Я иду искать');

COMMIT;

INSERT INTO "FILM_LIKES" -- добавляем лайк фильму титаник
(USER_ID, FILM_ID)
VALUES(SELECT ID FROM "USERS"  WHERE EMAIL = 'kostya@gmail.com',
          SELECT ID FROM "FILM"  WHERE NAME = 'Титаник');

INSERT INTO "FILM_LIKES" -- добавляем лайк фильму Я иду искать
(USER_ID, FILM_ID)
VALUES(SELECT ID FROM "USERS"  WHERE EMAIL = 'agabov@gmail.com',
          SELECT ID FROM "FILM"  WHERE NAME = 'Я иду искать');

INSERT INTO "FILM_LIKES" -- добавляем лайк фильму титаник
(USER_ID, FILM_ID)
VALUES(SELECT ID FROM "USERS"  WHERE EMAIL = 'agabov@gmail.com',
          SELECT ID FROM "FILM"  WHERE NAME = 'Титаник');

INSERT INTO "FILM_LIKES" -- добавляем лайк фильму Дикая природа Амазонки
(USER_ID, FILM_ID)
VALUES(SELECT ID FROM "USERS"  WHERE EMAIL = 'petya@gmail.com',
          SELECT ID FROM "FILM"  WHERE NAME = 'Дикая природа Амазонки');

INSERT INTO "DIRECTOR"
(DIRECTOR_NAME)
VALUES('НоЛан'), ('Петров'), ('Иванов');

INSERT INTO "DIRECTORS"
(FILM_ID, DIRECTOR_ID)
VALUES(1, 1), (2,2), (3,3), (4,3), (5,1);

