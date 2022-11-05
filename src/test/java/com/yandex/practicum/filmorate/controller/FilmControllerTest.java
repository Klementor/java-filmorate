package com.yandex.practicum.filmorate.controller;

import com.yandex.practicum.filmorate.exeption.NotFoundException;
import com.yandex.practicum.filmorate.exeption.ValidationException;
import com.yandex.practicum.filmorate.model.Film;
import com.yandex.practicum.filmorate.model.Mpa;
import com.yandex.practicum.filmorate.model.User;
import com.yandex.practicum.filmorate.service.UserService;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmControllerTest {
    @Autowired
    private FilmController filmController;
    private Film template;
    private User user;

    @Autowired
    private UserService userService;

    @BeforeEach
    public void createFilm() {
        user = userService.createUser(new User(0, "sasadas@dfsdfd.com", "login", "name",
                LocalDate.of(2000, 10, 10)));
        template = new Film(0, "name", "descr",
                LocalDate.of(2000, 10, 10), 10, new Mpa(1, "G"));
    }

    @Test
    void shouldExceptionWithNull() {
        ValidationException ex = assertThrows(ValidationException.class, () -> filmController.create(null));
        Assertions.assertEquals("Фильм не может быть создан.", ex.getMessage());
    }

    @Test
    void shouldExceptionWithEmptyName() {
        template.setName("");
        ValidationException ex = assertThrows(ValidationException.class, () -> filmController.create(template));
        Assertions.assertEquals("Название фильма пустое.", ex.getMessage());

    }

    @Test
    void shouldExceptionWithTooLongDescription() {
        template.setDescription("d".repeat(201));
        ValidationException ex = assertThrows(ValidationException.class, () -> filmController.create(template));
        Assertions.assertEquals("Описание фильма слишком длинная. Максимальная длина - 200 символов.", ex.getMessage());

    }

    @Test
    void shouldExceptionWithNegativeDuration() {
        template.setDuration(-100);
        ValidationException ex = assertThrows(ValidationException.class, () -> filmController.create(template));
        Assertions.assertEquals("Некорректная продолжительность фильма -100.", ex.getMessage());

    }

    @Test
    void shouldExceptionWithIncorrectReleaseDay() {
        template.setReleaseDate(LocalDate.of(1600,10,10));
        ValidationException ex = assertThrows(ValidationException.class, () -> filmController.create(template));
        Assertions.assertEquals("Некорректная дата выхода фильма", ex.getMessage());

    }

    @Test
    void shouldExceptionUpdateWithNonContainsId() {
        template.setId(-1);
        NotFoundException ex = assertThrows(NotFoundException.class, () -> filmController.update(template));
        Assertions.assertEquals("Фильм не существует.", ex.getMessage());

    }


    @Test
    void shouldCorrectLikeFilm() {
        Film film = filmController.create(template);
        filmController.likeFilm(film.getId(), user.getId());
        var returned = filmController.getFilm(film.getId());
        assertEquals(returned.getLikes().size(), 1);
        assertTrue(returned.getLikes().contains(user.getId()));
    }

    @Test
    void shouldEmptyLikeFilm() {
        Film film = filmController.create(template);
        var returned = filmController.getFilm(film.getId());
        assertNotNull(returned.getLikes());
        assertEquals(returned.getLikes().size(), 0);
    }

    @Test
    void shouldExceptionLikeFilmNotFoundUser() {
        Film film = filmController.create(template);
        NotFoundException ex = assertThrows(NotFoundException.class, () -> filmController.likeFilm(film.getId(), -1));
        Assertions.assertEquals("Пользователя с id = " + -1 + " не существует.", ex.getMessage());
    }

    @Test
    void shouldExceptionLikeFilmNotFoundFilm() {
        NotFoundException ex = assertThrows(NotFoundException.class, () -> filmController.likeFilm(-1, user.getId()));
        Assertions.assertEquals("Фильм с id = " + -1 + " не существует.", ex.getMessage());
    }

    @Test
    void shouldCorrectUnlikeFilm() {
        Film film = filmController.create(template);
        filmController.likeFilm(film.getId(), user.getId());
        filmController.unlikeFilm(film.getId(), user.getId());
        assertNotNull(film.getLikes());
        assertEquals(film.getLikes().size(), 0);
    }

    @Test
    void shouldExceptionUnlikeFilmNotFoundFilm() {
        NotFoundException ex = assertThrows(NotFoundException.class, () -> filmController.unlikeFilm(-1, user.getId()));
        Assertions.assertEquals("Фильм с id = " + -1 + " не существует.", ex.getMessage());
    }

    @Test
    void shouldExceptionUnlikeFilmNotFoundUser() {
        Film film = filmController.create(template);
        NotFoundException ex = assertThrows(NotFoundException.class, () -> filmController.unlikeFilm(film.getId(), -1));
        Assertions.assertEquals("Пользователя с id = " + -1 + " не существует.", ex.getMessage());
    }

    @Test
    void shouldReturnPopularFilmsWithNullCount() {
        Film film = filmController.create(template);
        Film withoutLike = filmController.create(template);

        filmController.likeFilm(film.getId(), user.getId());
        List<Film> films = filmController.getPopularFilms(null, null, null);

        assertNotNull(films);
        assertFalse(films.isEmpty());
    }

    @Test
    void shouldReturnPopularFilmsWithNonNullCount() {
        Film film = filmController.create(template);
        Film withoutLike = filmController.create(template);

        filmController.likeFilm(film.getId(), user.getId());
        List<Film> films = filmController.getPopularFilms(1, null, null);

        assertEquals(1, films.size());
        assertFalse(films.contains(withoutLike));
    }


}