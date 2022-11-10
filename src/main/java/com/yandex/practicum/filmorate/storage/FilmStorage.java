package com.yandex.practicum.filmorate.storage;

import com.yandex.practicum.filmorate.model.Film;
import com.yandex.practicum.filmorate.model.Like;

import java.util.List;
import java.util.Optional;
import java.util.TreeSet;

public interface FilmStorage {
    List<Film> getFilms();

    Film createFilm(Film film);

    Film updateFilm(Film film);

    List<Film> search(String query, Boolean director, Boolean title);

    Optional<Film> getFilmById(int filmId);

    List<Film> getMostPopularFilms(Integer count);

    List<Film> getMostPopularFilmsWithGenreAndYear(Integer count, Integer genreId, Integer year);

    List<Film> getMostPopularFilmsWithGenre(Integer count, Integer genreId);

    List<Film> getMostPopularFilmsWithYear(Integer count, Integer year);

    void likeFilm(Film film, int userId);

    void unlikeFilm(Film film, int userId);

    TreeSet<Film> getCommonFilms(int userId, int friendId);

    List<Film> getSortedFilms(int directorId, String sortBy);

    void removeFilmById(int filmId);

    List<Like> getAllLikes();
}
