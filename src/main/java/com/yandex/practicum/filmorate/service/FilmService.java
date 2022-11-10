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

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FilmService {
    private static final LocalDate CINEMA_BIRTHDAY = LocalDate.of(1895, Month.DECEMBER, 28);
    private static final int MAX_FILM_DESCRIPTION_SIZE = 200;

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
        return filmStorage.createFilm(film);
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

    public Film updateFilm(Film film) {
        if (film == null) {
            throw new ValidationException("Фильм не может быть обновлен.");
        }

        if (filmStorage.getFilmById(film.getId()).isEmpty()) {
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
        return filmStorage.updateFilm(film);
    }

    public Film getFilmById(int id) {
        return filmStorage.getFilmById(id).orElseThrow(() -> {
            throw new NotFoundException("Фильм с id = " + id + " не существует.");
        });
    }

    public void likeFilm(int userId, int filmId) {
        userStorage.getUserById(userId).orElseThrow(() -> {
            throw new NotFoundException("Пользователя с id = " + userId + " не существует.");
        });

        Film film = filmStorage.getFilmById(filmId).orElseThrow(() -> {
            throw new NotFoundException("Фильм с id = " + filmId + " не существует.");
        });
        filmStorage.likeFilm(film, userId);
        userStorage.addHistoryEvent(userId, "LIKE", "ADD", filmId);
    }

    public void unlikeFilm(int userId, int filmId) {
        userStorage.getUserById(userId).orElseThrow(() -> {
            throw new NotFoundException("Пользователя с id = " + userId + " не существует.");
        });

        Film film = filmStorage.getFilmById(filmId).orElseThrow(() -> {
            throw new NotFoundException("Фильм с id = " + filmId + " не существует.");
        });

        filmStorage.unlikeFilm(film, userId);
        userStorage.addHistoryEvent(userId, "LIKE", "REMOVE", filmId);

    }

    public List<Film> getFilms() {
        List<Film> films = filmStorage.getFilms();
        for (Film film : films) {
            film.getGenres().addAll(genresStorage.getFilmGenres(film.getId()));
            film.getDirectors().addAll(directorStorage.getDirectorByFilmId(film.getId()));
        }
        return films;
    }

    public List<Film> getMostPopularFilms(Integer count, Integer genreId, Integer year) {
        if (genreId == null && year == null) {
            return filmStorage.getMostPopularFilms(count);
        } else if (genreId == null) {
            return filmStorage.getMostPopularFilmsWithYear(count, year);
        } else if (year == null) {
            return filmStorage.getMostPopularFilmsWithGenre(count, genreId);
        } else {
            if (genresStorage.getGenres().get(genreId) == null) {
                log.warn("Жанр не найден.");
                throw new ValidationException("Жанр не найден.");
            }
            if (year < CINEMA_BIRTHDAY.getYear() || year > LocalDate.now().getYear()) {
                log.warn("Год меньше {} или больше {}.", CINEMA_BIRTHDAY.getYear(), LocalDate.now().getYear());
                throw new ValidationException("Год меньше " + CINEMA_BIRTHDAY.getYear() + " или больше " + LocalDate.now().getYear() + ".");
            }
            return filmStorage.getMostPopularFilmsWithGenreAndYear(count, genreId, year);
        }
    }

    public TreeSet<Film> getCommonFilms(int userId, int friendId) {
        return filmStorage.getCommonFilms(userId, friendId);
    }

    public List<Film> getSortedFilmsByParameter(int directorId, String sortBy) {
        try {
            directorStorage.getDirectorById(directorId);
            return filmStorage.getSortedFilms(directorId, sortBy);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Режиссера с таким id не существует");
        }
    }

    public void deleteFilmById(int id) {
        filmStorage.getFilmById(id).orElseThrow(() -> {
            throw new NotFoundException("Фильма с id = " + id + " не существует.");
        });
        filmStorage.removeFilmById(id);
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

    private int generatedId() {
        return ++idGenerator;
    }

    private Map<String, Boolean> parseQueryBy(String by) {
        int maximumParametersSize = 2;
        Map<String, Boolean> parameters = new HashMap<>();
        if (by != null) {
            String[] strings = by.split(",");
            if (strings.length > maximumParametersSize) {
                throw new ValidationException("chosen search fields contains more parameters than expected");
            }
            parameters.put("director", Arrays.asList(strings).contains("director"));
            parameters.put("title", Arrays.asList(strings).contains("title"));
        } else {
            parameters.put("director", false);
            parameters.put("title", false);
        }
        return parameters;
    }
}