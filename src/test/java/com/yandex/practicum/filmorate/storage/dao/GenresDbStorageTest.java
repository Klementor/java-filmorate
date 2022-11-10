package com.yandex.practicum.filmorate.storage.dao;

import com.yandex.practicum.filmorate.controller.FilmController;
import com.yandex.practicum.filmorate.model.Film;
import com.yandex.practicum.filmorate.model.Genre;
import com.yandex.practicum.filmorate.model.Mpa;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class GenresDbStorageTest {
    private final GenresDbStorage genresDbStorage;
    private final FilmController filmController;
    private Film template;

    @BeforeEach
    public void createFilm() {
        template = new Film(0, "name", "descr",
                LocalDate.of(2000, 10, 10), 10, new Mpa(1, "G"));
        template = filmController.createFilm(template);
    }

    @Test
    void testGetGenres() {
        List<Genre> genres = genresDbStorage.getGenres();

        assertFalse(genres.isEmpty());
        assertEquals(6, genres.size());
        assertEquals(genres.get(0).getName(), "Комедия");

    }

    @Test
    void testGetGenreById() {
        Optional<Genre> genreOptional = genresDbStorage.getGenreById(3);

        assertThat(genreOptional)
                .isPresent()
                .hasValueSatisfying(genre ->
                        assertThat(genre).hasFieldOrPropertyWithValue("name", "Мультфильм"));
    }

    @Test
    void testGetGenresByIds() {
        Map<Integer, Genre> genres = genresDbStorage.getGenresByIds(List.of(2, 3));

        assertFalse(genres.isEmpty());
        assertEquals(2, genres.size());
        assertEquals(genres.get(2).getName(), "Драма");
    }

    @Test
    void testAddFilmGenre() {
        assertThatNoException().isThrownBy(() -> genresDbStorage.addFilmGenres(template.getId(), List.of(1)));
    }

    @Test
    void testRemoveFilmGenre() {
        genresDbStorage.addFilmGenres(template.getId(), List.of(1));
        assertThatNoException().isThrownBy(() -> genresDbStorage.removeFilmGenres(template.getId(), List.of(1)));
    }

    @Test
    void testGetFilmGenres() {
        genresDbStorage.addFilmGenres(template.getId(), List.of(1));
        List<Genre> genres = genresDbStorage.getFilmGenres(template.getId());

        assertFalse(genres.isEmpty());
        assertEquals(1, genres.size());
        assertEquals(genres.get(0).getName(), "Комедия");
    }
}