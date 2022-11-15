package com.yandex.practicum.filmorate.storage.dao;

import com.yandex.practicum.filmorate.model.Mpa;
import com.yandex.practicum.filmorate.storage.MpaStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Component("mpaStorage")
@RequiredArgsConstructor
public class MpaDbStorage implements MpaStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Mpa> getMpa() {
        String select = "SELECT * FROM mpa";
        return jdbcTemplate.query(select, (rs, rowNum) -> makeMpa(rs));
    }

    @Override
    public Optional<Mpa> getMpaById(int id) {
        String select = "SELECT * FROM mpa WHERE id = ?";
        SqlRowSet mpaRow = jdbcTemplate.queryForRowSet(select, id);
        if (mpaRow.next()) {
            Mpa mpa = new Mpa(
                    mpaRow.getInt("id"),
                    mpaRow.getString("name"));
            return Optional.of(mpa);
        } else {
            return Optional.empty();
        }
    }

    private Mpa makeMpa(ResultSet rs) throws SQLException {
        return new Mpa(rs.getInt("id"), rs.getString("name"));
    }
}