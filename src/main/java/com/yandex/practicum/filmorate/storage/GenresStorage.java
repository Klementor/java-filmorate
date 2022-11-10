package com.yandex.practicum.filmorate.storage;

import com.yandex.practicum.filmorate.model.Genre;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public interface GenresStorage {
    List<Genre> getGenres();

    Optional<Genre> getGenreById(int id);

    Map<Integer, Genre> getGenresByIds(List<Integer> ids);

    void removeFilmGenres(int filmId, List<Integer> genreIds);

    void addFilmGenres(int filmId, List<Integer> genreIds);

    List<Genre> getFilmGenres(int filmId);
}