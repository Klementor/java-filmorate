package com.yandex.practicum.filmorate.service;

import com.yandex.practicum.filmorate.exeption.NotFoundException;
import com.yandex.practicum.filmorate.exeption.ValidationException;
import com.yandex.practicum.filmorate.model.Film;
import com.yandex.practicum.filmorate.model.Genre;
import com.yandex.practicum.filmorate.model.Mpa;
import com.yandex.practicum.filmorate.storage.*;
import com.yandex.practicum.filmorate.utils.Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FilmService {
    private static final LocalDate CINEMA_BIRTHDAY = LocalDate.of(1895, Month.DECEMBER, 28);
    private static final int MAX_FILM_DESCRIPTION_SIZE = 200;
    private static final int DEFAULT_COUNT_POPULAR_FILMS = 10;

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    private final MpaStorage mpaStorage;
    private final GenresStorage genresStorage;

    private final DirectorStorage directorStorage;
    private int idGenerator = 0;

    public Film createFilm(Film film) {
        if (film == null) {
            throw new ValidationException("Фильм не может быть создан.");
        }
        validationFilm(film);
        Mpa mpa = mpaStorage.getMpaById(film.getMpa().getId())
                .orElseThrow(() -> {
                    throw new NotFoundException("Рейтинг не существует.");
                });

        fillFilmGenres(film);
        film.setId(generatedId());
        film.setMpa(mpa);
        return filmStorage.create(film);
    }

    public List<Film> search(String query, String by) {
        Map<String, Boolean> queryParams = parseQueryBy(by);
        if (query != null && !query.isEmpty()) {
            return filmStorage.search(query, queryParams.get("director"), queryParams.get("title"));
        } else {
            log.warn("film search query is empty.");
            return new ArrayList<>();
        }
    }

    private  Map<String,Boolean>  parseQueryBy(String by) {
        int maximumParametersSize =2;
        Map<String,Boolean> parameters = new HashMap<>();
        if (by != null) {
            String[] strings = by.split(",");
            if ( strings.length > maximumParametersSize) {
                throw new ValidationException("chosen search fields contains more parameters than expected");
            }
            parameters.put("director", Arrays.stream(strings).anyMatch(x->x.equals("director")));
            parameters.put("title", Arrays.stream(strings).anyMatch(x->x.equals("title")));
        }
        else {
            parameters.put("director", false);
            parameters.put("title", false);
        }
        return parameters;
    }
    public Film updateFilm(Film film) {
        if (film == null) {
            throw new ValidationException("Фильм не может быть обновлен.");
        }

        if (filmStorage.get(film.getId()).isEmpty()) {
            log.warn("Фильм с id {} не существует.", film.getId());
            throw new NotFoundException("Фильм не существует.");
        }
        validationFilm(film);
        Mpa mpa = mpaStorage.getMpaById(film.getMpa().getId())
                .orElseThrow(() -> {
                    throw new NotFoundException("Рейтинг не существует.");
                });
        film.setMpa(mpa);
        fillFilmGenres(film);
        return filmStorage.update(film);
    }

    public Film getFilm(int id) {
        return filmStorage.get(id).orElseThrow(() -> {
            throw new NotFoundException("Фильм с id = " + id + " не существует.");
        });
    }

    public void likeFilm(int userId, int filmId) {
        userStorage.getUserById(userId).orElseThrow(() -> {
            throw new NotFoundException("Пользователя с id = " + userId + " не существует.");
        });

        Film film = filmStorage.get(filmId).orElseThrow(() -> {
            throw new NotFoundException("Фильм с id = " + filmId + " не существует.");
        });
        filmStorage.likeFilm(film, userId);

    }

    public void unlikeFilm(int userId, int filmId) {
        userStorage.getUserById(userId).orElseThrow(() -> {
            throw new NotFoundException("Пользователя с id = " + userId + " не существует.");
        });

        Film film = filmStorage.get(filmId).orElseThrow(() -> {
            throw new NotFoundException("Фильм с id = " + filmId + " не существует.");
        });

        filmStorage.unlikeFilm(film, userId);

    }

    public List<Film> getFilms() {
        List<Film> films = filmStorage.getFilms();
        for (Film film : films) {
            film.getGenres().addAll(genresStorage.getFilmGenres(film.getId()));
            film.getDirectors().addAll(directorStorage.getDirectorByFilmId(film.getId()));
        }
        return films;
    }

    public List<Film> getMostPopularFilms(String countStr) {
        int count = DEFAULT_COUNT_POPULAR_FILMS;
        if (countStr != null) {
            count = Integer.parseInt(countStr);
        }
        return filmStorage.getMostPopularFilms(count);
    }

    private void validationFilm(Film film) {
        if (film.getName().isBlank()) {
            log.warn("Название фильма пустое.");
            throw new ValidationException("Название фильма пустое.");
        }

        if (film.getDescription().length() >= MAX_FILM_DESCRIPTION_SIZE) {
            log.warn("Описание фильма слишком длинная. Максимальная длина - 200 символов.");
            throw new ValidationException("Описание фильма слишком длинная. Максимальная длина - 200 символов.");
        }


        if (film.getReleaseDate().isBefore(CINEMA_BIRTHDAY)) {
            log.warn("Релиз фильма раньше {}.", CINEMA_BIRTHDAY.format(Util.DATE_FORMAT));
            throw new ValidationException("Некорректная дата выхода фильма");
        }

        if (film.getDuration() <= 0) {
            log.warn("Некорректная продолжительность фильма {}.", film.getDuration());
            throw new ValidationException("Некорректная продолжительность фильма " + film.getDuration() + ".");
        }

        if (film.getMpa() == null) {
            log.warn("Некорректный рейтинг фильма {}.", film.getMpa());
            throw new ValidationException("Некорректный рейтинг фильма " + film.getMpa() + ".");
        }
    }

    private void fillFilmGenres(Film film) {
        if (film.getGenres() != null) {
            List<Integer> genresIds = film.getGenres()
                    .stream()
                    .map(Genre::getId)
                    .distinct()
                    .collect(Collectors.toList());
            Map<Integer, Genre> genres = genresStorage.getGenresByIds(genresIds);
            if (genres.size() != genresIds.size()) {
                throw new NotFoundException("Жанра не существует.");
            }
            film.getGenres().clear();
            film.getGenres().addAll(genres.values());
        }
    }

    public List<Film> getSortedFilms(int directorId, String sortBy) {
        try {
            directorStorage.getDirectorById(directorId);
            return filmStorage.getSortedFilms(directorId, sortBy);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Режиссера с таким id не существует");
        }
    }
    private int generatedId() {
        return ++idGenerator;
    }
}
