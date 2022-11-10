package com.yandex.practicum.filmorate.storage.dao;

import com.yandex.practicum.filmorate.model.Director;
import com.yandex.practicum.filmorate.storage.DirectorStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Component("directorStorage")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DirectorDbStorage implements DirectorStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Director> getDirectors() {
        String sqlQuery = "SELECT director_id, director_name FROM director";
        return jdbcTemplate.query(sqlQuery, new DirectorMapper());
    }

    @Override
    public Director getDirectorById(int id) {
        String sqlQuery = "SELECT director_id, director_name FROM director WHERE director_id = ?";
        return jdbcTemplate.queryForObject(sqlQuery, new DirectorMapper(), id);
    }

    @Override
    public Set<Director> getDirectorByFilmId(int id) {
        return new HashSet<>(jdbcTemplate.query(String.format("SELECT director.director_id, director.director_name " +
                        "FROM director " +
                        "JOIN directors ON director.director_id = directors.director_id " +
                        "WHERE directors.film_id=%d " +
                "ORDER BY director.director_id",id), new DirectorMapperForSort()));
    }

    @Override
    public Director addDirector(Director director) {
        String sqlQuery = "INSERT INTO director (DIRECTOR_NAME) VALUES (?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sqlQuery, new String[]{"director_id"});
            ps.setString(1, director.getName());
            return ps;
        }, keyHolder);
        return getDirectorById(Objects.requireNonNull(keyHolder.getKey()).intValue());
    }

    @Override
    public Director updateDirector(Director director) {
        String sqlQuery = "UPDATE director SET director_name=? WHERE director_id=?";
        jdbcTemplate.update(sqlQuery, director.getName(), director.getId());
        return getDirectorById(director.getId());
    }

    @Override
    public void deleteDirector(int id) {
        String sqlQuery = "DELETE FROM director WHERE director_id=?";
        jdbcTemplate.update(sqlQuery, id);
    }

    public void addDirectors(int id, Set<Integer> directorIds) {
        for (int directorId : directorIds) {
            jdbcTemplate.update("INSERT INTO DIRECTORS(film_id, director_id) " +
                    "values ( ?, ? )", id, directorId);
        }
    }

    public void removeDirectors(int id) {
            jdbcTemplate.update("DELETE FROM DIRECTORS " +
                    "WHERE FILM_ID=?", id);
    }

    public static class DirectorMapper implements RowMapper<Director> {

        @Override
        public Director mapRow(ResultSet rs, int rowNum) throws SQLException {
            Director director = new Director();
            director.setId(rs.getInt("director_id"));
            director.setName(rs.getString("director_name"));
            return director;
        }
    }

    public static class DirectorMapperForSort implements RowMapper<Director> {

        @Override
        public Director mapRow(ResultSet rs, int rowNum) throws SQLException {
            Director director = new Director();
            director.setId(rs.getInt("director_id"));
            director.setName(rs.getString("director_name"));
            return director;
        }
    }
}
