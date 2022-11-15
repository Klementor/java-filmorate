package com.yandex.practicum.filmorate.storage.dao;

import com.yandex.practicum.filmorate.model.*;
import com.yandex.practicum.filmorate.storage.FilmStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.time.LocalDate;
import java.util.stream.Collectors;

@Component("filmStorage")
@RequiredArgsConstructor
@Slf4j
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
    private final GenresDbStorage genresDbStorage;
    private final DirectorDbStorage directorDbStorage;

    @Override
    public List<Film> getFilms() {
        String select = "SELECT f.*, m.id AS mpa_id, m.name AS mpa_name " +
                "FROM film AS f " +
                "INNER JOIN mpa AS m ON f.mpa = m.id";
        return jdbcTemplate.query(select, (rs, rowNum) -> makeFilm(rs));
    }

    @Override
    public Film createFilm(Film film) {
        String insert = "INSERT INTO film (id, name, description, release_date, duration, mpa) VALUES ( ?, ?, ?, ?,?,?)";
        jdbcTemplate.update(insert, film.getId(), film.getName(), film.getDescription(), film.getReleaseDate(),
                film.getDuration(), film.getMpa().getId());
        if (film.getGenres() != null) {
            List<Integer> genreIds = film.getGenres()
                    .stream()
                    .map(Genre::getId)
                    .collect(Collectors.toList());
            genresDbStorage.addFilmGenres(film.getId(), genreIds);
        }
        if (film.getDirectors() != null) {
            Set<Integer> directors = film.getDirectors()
                    .stream()
                    .map(Director::getId)
                    .collect(Collectors.toSet());
            directorDbStorage.addDirectors(film.getId(), directors);
        }
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        String update = "UPDATE film SET name = ?, description = ?, release_date = ?, duration = ?, mpa = ?" +
                "WHERE id = ?";
        jdbcTemplate.update(update, film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration(), film.getMpa().getId(), film.getId());
        List<Genre> existFilmGenres = genresDbStorage.getFilmGenres(film.getId());
        if (existFilmGenres != null) {
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
        }
        if (film.getDirectors() != null) {
            directorDbStorage.removeDirectorsById(film.getId());
            Set<Integer> directors = film.getDirectors()
                    .stream()
                    .map(Director::getId)
                    .collect(Collectors.toSet());
            directorDbStorage.addDirectors(film.getId(), directors);
        }
        return film;
    }

    @Override
    public List<Film> search(String query, Boolean director, Boolean title) {

        query = "%" + query + "%";

        String select = "SELECT f.*, m.id AS mpa_id, m.name AS mpa_name " +
                "FROM FILM AS f " +
                "INNER JOIN MPA AS m ON f.mpa = m.id " +
                "LEFT JOIN DIRECTORS d ON d.FILM_ID = f.ID " +
                "LEFT JOIN DIRECTOR d2 ON d2.DIRECTOR_ID = d.DIRECTOR_ID " +
                ((director && title) ? ("WHERE LOWER(f.name) LIKE LOWER(?) OR LOWER(d2.DIRECTOR_NAME) LIKE LOWER(?) ") :
                (title) ? ("WHERE LOWER(f.name) LIKE LOWER(?) ") :
                        ("WHERE LOWER(d2.DIRECTOR_NAME) LIKE LOWER(?) ")) +
                "ORDER BY d.FILM_ID DESC";

        List<Film> films = director && title ?
                jdbcTemplate.query(select, (rs, rowNum) -> makeFilm(rs), query, query) :
                jdbcTemplate.query(select, (rs, rowNum) -> makeFilm(rs), query);

        films.forEach(film -> {
            film.getGenres().addAll(genresDbStorage.getFilmGenres(film.getId()));
            film.getLikes().addAll(getUserLikes(film.getId()));
            film.getDirectors().addAll(directorDbStorage.getDirectorByFilmId(film.getId()));
        });
        return films;
    }

    @Override
    public Optional<Film> getFilmById(int filmId) {
        String select = "SELECT f.*, m.name AS mpa_name " +
                "FROM film AS f " +
                "INNER JOIN mpa AS m ON f.mpa = m.id " +
                "AND f.id = ?";
        SqlRowSet filmRow = jdbcTemplate.queryForRowSet(select, filmId);
        if (filmRow.next()) {
            Film film = new Film();
            film.setId(filmRow.getInt("id"));
            film.setName(filmRow.getString("name"));
            film.setDescription(filmRow.getString("description"));
            film.setDuration(filmRow.getInt("duration"));
            film.setReleaseDate(filmRow.getDate("release_date").toLocalDate());
            film.setMpa(new Mpa(filmRow.getInt("mpa"), filmRow.getString("MPA_NAME")));
            film.getGenres().addAll(genresDbStorage.getFilmGenres(filmId));
            film.getLikes().addAll(getUserLikes(filmId));
            film.getDirectors().addAll(directorDbStorage.getDirectorByFilmId(filmId));
            return Optional.of(film);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public List<Film> getMostPopularFilms(Integer count) {
        String select = "SELECT f.*, m.id AS mpa_id, m.name AS mpa_name\n" +
                "FROM film AS f " +
                "INNER JOIN mpa AS m ON f.mpa = m.id\n" +
                "LEFT OUTER JOIN film_likes AS fl ON f.id = fl.film_id " +
                "GROUP BY f.id, fl.user_id " +
                "ORDER BY COUNT(fl.user_id) DESC " +
                "LIMIT ?";

        List<Film> films = jdbcTemplate.query(select, (rs, rowNum) -> makeFilm(rs), count);
        films.forEach(film -> film.getLikes().addAll(getUserLikes(film.getId())));
        return films;
    }

    @Override
    public List<Film> getMostPopularFilmsWithGenreAndYear(Integer count, Integer genreId, Integer year) {
        String select = "SELECT f.*, m.id AS mpa_id, m.name AS mpa_name " +
                "FROM film AS f " +
                "INNER JOIN mpa AS m ON f.mpa = m.id " +
                "LEFT JOIN film_likes AS lk ON f.id = lk.film_id " +
                "LEFT JOIN film_genre AS fg ON f.id = fg.film_id " +
                "WHERE f.release_date >= ? AND f.release_date < ? AND fg.genre_id = ? " +
                "GROUP BY f.id " +
                "ORDER BY COUNT(lk.film_id) DESC " +
                "LIMIT ?";
        List<Film> films = jdbcTemplate.query(select, (rs, rowNum) -> makeFilm(rs),
                LocalDate.of(year, 1, 1),
                LocalDate.of(year, 1, 1).plusYears(1),
                genreId,
                count);
        films.forEach(film -> {
            film.getGenres().addAll(genresDbStorage.getFilmGenres(film.getId()));
            film.getLikes().addAll(getUserLikes(film.getId()));
        });
        return films;
    }

    @Override
    public List<Film> getMostPopularFilmsWithGenre(Integer count, Integer genreId) {
        String select = "SELECT f.*, m.id AS mpa_id, m.name AS mpa_name " +
                "FROM film AS f " +
                "INNER JOIN mpa AS m ON f.mpa = m.id " +
                "LEFT JOIN film_likes AS lk ON f.id = lk.film_id " +
                "LEFT JOIN film_genre AS fg ON f.id = fg.film_id " +
                "WHERE fg.genre_id = ? " +
                "GROUP BY f.id " +
                "ORDER BY COUNT(lk.film_id) DESC " +
                "LIMIT ?";
        List<Film> films = jdbcTemplate.query(select, (rs, rowNum) -> makeFilm(rs), genreId,
                count);
        films.forEach(film -> {
            film.getGenres().addAll(genresDbStorage.getFilmGenres(film.getId()));
            film.getLikes().addAll(getUserLikes(film.getId()));
        });
        return films;
    }

    @Override
    public List<Film> getMostPopularFilmsWithYear(Integer count, Integer year) {
        String select = "SELECT f.*, m.id AS mpa_id, m.name AS mpa_name " +
                "FROM film AS f " +
                "INNER JOIN mpa AS m ON f.mpa = m.id " +
                "LEFT JOIN film_likes AS lk ON f.id = lk.film_id " +
                "LEFT JOIN film_genre AS fg ON f.id = fg.film_id " +
                "WHERE f.release_date >= ? AND f.release_date < ? " +
                "GROUP BY f.id " +
                "ORDER BY COUNT(lk.film_id) DESC " +
                "LIMIT ?";
        List<Film> films = jdbcTemplate.query(select, (rs, rowNum) -> makeFilm(rs),
                LocalDate.of(year, 1, 1),
                LocalDate.of(year, 1, 1).plusYears(1),
                count);
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

    @Override
    public TreeSet<Film> getCommonFilms(int userId, int friendId) {
        Set<Integer> usersId = new HashSet<>(getFilmsIdByUserLikes(userId));
        Set<Integer> friendsId = new HashSet<>(getFilmsIdByUserLikes(userId));
        usersId.retainAll(friendsId);
        String parameterId = usersId.stream().map(String::valueOf).collect(Collectors.joining(","));
        String select = String.format("SELECT f.*, m.id AS mpa_id, m.name AS mpa_name " +
                "FROM film AS f " +
                "INNER JOIN mpa AS m ON f.mpa = m.id WHERE f.id IN (%s)", parameterId);
        TreeSet<Film> films = new TreeSet<>(((o1, o2) -> o2.getLikes().size() - o1.getLikes().size()));
        films.addAll(jdbcTemplate.query(select, (rs, rowNum) -> makeFilm(rs)));
        return films;
    }

    private List<Integer> getFilmsIdByUserLikes(int userId) {
        String select = "SELECT film_id FROM film_likes WHERE user_id = ?";
        return jdbcTemplate.query(select, (rs, rowNum) -> rs.getInt("film_id"), userId);
    }

    public List<Film> getSortedFilms(int directorId, String sortBy) {
        if ("year".equals(sortBy)) {
            String sqlQuery = "SELECT FILM.*, " +
                    "MPA.* " +
                    "FROM film " +
                    "JOIN mpa ON mpa.id=FILM.MPA " +
                    "JOIN directors ON FILM.id=DIRECTORS.film_id " +
                    "WHERE DIRECTORS.DIRECTOR_ID =?" +
                    "ORDER BY FILM.release_date";
            List<Film> films = jdbcTemplate.query(sqlQuery, new SortedFilmsMapper(), directorId);
            for (Film film : films) {
                film.getGenres().addAll(genresDbStorage.getFilmGenres(film.getId()));
                film.getLikes().addAll(getUserLikes(film.getId()));
                film.getDirectors().addAll(directorDbStorage.getDirectorByFilmId(film.getId()));
            }
            return films;
        } else if ("likes".equals(sortBy)) {
            String sqlQuery = "SELECT FILM.*, " +
                    "MPA.*, " +
                    "COUNT(FILM_LIKES.user_id) " +
                    "FROM FILM " +
                    "Left JOIN FILM_LIKES ON FILM.id=FILM_LIKES.film_id " +
                    "Left JOIN MPA ON MPA.id=FILM.MPA " +
                    "Left JOIN directors ON FILM.id=DIRECTORS.film_id " +
                    "WHERE DIRECTORS.DIRECTOR_ID=? " +
                    "GROUP BY FILM.id " +
                    "ORDER BY COUNT(FILM_LIKES.user_id) DESC ";
            List<Film> films = jdbcTemplate.query(sqlQuery, new SortedFilmsMapper(), directorId);
            for (Film film : films) {
                film.getGenres().addAll(genresDbStorage.getFilmGenres(film.getId()));
                film.getLikes().addAll(getUserLikes(film.getId()));
                film.getDirectors().addAll(directorDbStorage.getDirectorByFilmId(film.getId()));
            }
            return films;
        } else {
            throw new RuntimeException("Такого варианта сортировки нет");
        }
    }

    public static class SortedFilmsMapper implements RowMapper<Film> {
        @Override
        public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
            Film film = new Film();
            film.setId(rs.getInt("film.id"));
            film.setName(rs.getString("film.name"));
            film.setDuration(rs.getInt("film.duration"));
            film.setDescription(rs.getString("film.description"));
            film.setReleaseDate(rs.getDate("film.release_date").toLocalDate());
            Mpa mpa = new Mpa();
            mpa.setId(rs.getInt("mpa.id"));
            mpa.setName(rs.getString("mpa.name"));
            film.setMpa(mpa);
            return film;
        }
    }

    public List<Like> getAllLikes() {
        return jdbcTemplate.query("SELECT *" +
                "FROM film_likes", new LikeMapper());
    }

    @Override
    public void removeFilmById(int filmId) {
        jdbcTemplate.update("DELETE FROM film WHERE ID=?", filmId);
    }

    private List<Integer> getUserLikes(int filmId) {
        String select = "SELECT user_id " +
                "FROM film_likes " +
                "WHERE film_id = ?";
        return jdbcTemplate.query(select, (rs, rowNum) -> rs.getInt("user_id"), filmId);
    }

    private Film makeFilm(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        Film film = new Film();
        film.setId(rs.getInt("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setDuration(rs.getInt("duration"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        film.setMpa(new Mpa(rs.getInt("mpa"), rs.getString("mpa_name")));
        film.getLikes().addAll(getUserLikes(id));
        film.getGenres().addAll(genresDbStorage.getFilmGenres(id));
        return film;

    }

    public static class LikeMapper implements RowMapper<Like> {
        @Override
        public Like mapRow(ResultSet rs, int rowNum) throws SQLException {
            Like like = new Like();
            like.setFilmId(rs.getInt("film_id"));
            like.setUserId(rs.getInt("user_id"));
            return like;
        }
    }
}