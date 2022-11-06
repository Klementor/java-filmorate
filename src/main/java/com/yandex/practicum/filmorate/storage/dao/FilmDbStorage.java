package com.yandex.practicum.filmorate.storage.dao;

import com.yandex.practicum.filmorate.model.Film;
import com.yandex.practicum.filmorate.model.Genre;
import com.yandex.practicum.filmorate.model.Like;
import com.yandex.practicum.filmorate.model.Mpa;
import com.yandex.practicum.filmorate.storage.FilmStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Component("filmStorage")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
    private final GenresDbStorage genresDbStorage;


    @Override
    public List<Film> getFilms() {
        String select = "SELECT f.*, m.id AS mpa_id, m.name AS mpa_name " +
                "FROM film AS f " +
                "INNER JOIN mpa AS m ON f.mpa = m.id";
        return jdbcTemplate.query(select, (rs, rowNum) -> makeFilm(rs));
    }

    @Override
    public Film create(Film film) {
        String insert = "INSERT INTO film (id, name, description, release_date, duration, mpa) VALUES ( ?, ?, ?, ?,?,?)";
        jdbcTemplate.update(insert, film.getId(), film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration(), film.getMpa().getId());
        if (film.getGenres() != null) {
            List<Integer> genreIds = film.getGenres()
                    .stream()
                    .map(Genre::getId)
                    .collect(Collectors.toList());
            genresDbStorage.addFilmGenres(film.getId(), genreIds);
        }
        return film;
    }

    @Override
    public Film update(Film film) {
        String update = "UPDATE film SET name = ?, description = ?, release_date = ?, duration = ?, mpa = ?" +
                "WHERE id = ?";
        jdbcTemplate.update(update, film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration(), film.getMpa().getId(), film.getId());
        List<Genre> existFilmGenres = genresDbStorage.getFilmGenres(film.getId());
        List<Integer> removed = existFilmGenres.stream()
                .filter(genre -> !film.getGenres().contains(genre))
                .map(Genre::getId)
                .collect(Collectors.toList());
        genresDbStorage.removeFilmGenres(film.getId(), removed);
        List<Integer> added = film.getGenres().stream()
                .filter(genre -> !existFilmGenres.contains(genre))
                .map(Genre::getId)
                .collect(Collectors.toList());
        genresDbStorage.addFilmGenres(film.getId(), added);
        return film;
    }

    @Override
    public Optional<Film> get(int filmId) {
        String select = "SELECT f.*, m.name AS mpa_name " +
                "FROM film AS f " +
                "INNER JOIN mpa AS m ON f.mpa = m.id " +
                "AND f.id = ?";
        SqlRowSet filmRow = jdbcTemplate.queryForRowSet(select, filmId);
        if (filmRow.next()) {
            Film film = Film.builder()
                    .id(filmRow.getInt("id"))
                    .name(filmRow.getString("name"))
                    .description(filmRow.getString("description"))
                    .duration(filmRow.getInt("duration"))
                    .releaseDate(filmRow.getDate("release_date").toLocalDate())
                    .mpa(new Mpa(filmRow.getInt("mpa"), filmRow.getString("MPA_NAME")))
                    .build();
            film.getGenres().addAll(genresDbStorage.getFilmGenres(filmId));
            film.getLikes().addAll(getUserLikes(filmId));
            return Optional.of(film);
        } else {
            return Optional.empty();
        }

    }

    @Override
    public List<Film> getMostPopularFilms(int count) {
        String select = "SELECT f.*, m.id AS mpa_id, m.name AS mpa_name\n" +
                "FROM film AS f " +
                "INNER JOIN mpa AS m ON f.mpa = m.id\n" +
                "LEFT OUTER JOIN film_likes AS fl ON f.id = fl.film_id " +
                "GROUP BY f.id, fl.user_id " +
                "ORDER BY COUNT(fl.user_id) DESC " +
                "LIMIT ?";
        List<Film> films = jdbcTemplate.query(select, (rs, rowNum) -> makeFilm(rs), count);
        films.forEach(film -> {
            film.getGenres().addAll(genresDbStorage.getFilmGenres(film.getId()));
            film.getLikes().addAll(getUserLikes(film.getId()));
        });
        return films;
    }

    @Override
    public void likeFilm(Film film, int userId) {
        String insert = "INSERT INTO film_likes (user_id, film_id) VALUES ( ?, ?)";
        jdbcTemplate.update(insert, userId, film.getId());
    }

    @Override
    public void unlikeFilm(Film film, int userId) {
        String delete = "DELETE FROM film_likes WHERE user_id = ? AND film_id = ?";
        jdbcTemplate.update(delete, userId, film.getId());
    }

    private List<Integer> getUserLikes(int filmId) {
        String select = "SELECT user_id " +
                "FROM film_likes " +
                "WHERE film_id = ?";
        return jdbcTemplate.query(select, (rs, rowNum) -> rs.getInt("user_id"), filmId);
    }

    public List<Like> getAllLikes() {
        return jdbcTemplate.query("SELECT *" +
                "FROM film_likes", new BeanPropertyRowMapper<>());
    }

    private Film makeFilm(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        Film film = Film.builder()
                .id(id)
                .name(rs.getString("name"))
                .description(rs.getString("description"))
                .duration(rs.getInt("duration"))
                .releaseDate(rs.getDate("release_date").toLocalDate())
                .mpa(new Mpa(rs.getInt("mpa_id"), rs.getString("mpa_name")))
                .build();
        film.getLikes().addAll(getUserLikes(id));
        film.getGenres().addAll(genresDbStorage.getFilmGenres(id));
        return film;
    }

}
