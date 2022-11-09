package com.yandex.practicum.filmorate.storage;

import com.yandex.practicum.filmorate.model.Film;
import com.yandex.practicum.filmorate.model.Like;

import java.util.List;
import java.util.Optional;

public interface FilmStorage {
    List<Film> getFilms();

    Film create(Film film);

    Film update(Film film);

    Optional<Film> get(int filmId);

    List<Film> getMostPopularFilms(int count);

    void likeFilm(Film film, int userId);

    void unlikeFilm(Film film, int userId);

    List<Film> getSortedFilms(int directorId, String sortBy);

    void removeFilm(int filmId);

    List<Like> getAllLikes();
}
