package com.yandex.practicum.filmorate.storage.dao;

import com.yandex.practicum.filmorate.model.HistoryEvent;
import com.yandex.practicum.filmorate.model.User;
import com.yandex.practicum.filmorate.storage.UserStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Component("userStorage")
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<User> getUsers() {
        String select = "SELECT * FROM users";
        return jdbcTemplate.query(select, (rs, rowNum) -> makeUser(rs));
    }

    @Override
    public Optional<User> getUserById(int id) {
        String select = "SELECT * FROM users WHERE id = ?";
        SqlRowSet userRow = jdbcTemplate.queryForRowSet(select, id);
        if (userRow.next()) {
            User user = User.builder()
                    .id(userRow.getInt("id"))
                    .email(userRow.getString("email"))
                    .login(userRow.getString("login"))
                    .name(userRow.getString("name"))
                    .birthday(userRow.getDate("birthday").toLocalDate())
                    .build();
            user.getFriends().addAll(getUserFriendsByUserId(id));
            return Optional.of(user);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public User createUser(User user) {
        String insert = "INSERT INTO users (ID, EMAIL, LOGIN, NAME, BIRTHDAY) VALUES ( ?, ?, ?, ?, ?)";
        jdbcTemplate.update(insert,
                user.getId(),
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                Date.valueOf(user.getBirthday()));
        return user;
    }

    @Override
    public Optional<User> updateUser(User user) {
        String delete = "DELETE FROM users WHERE id = ?";
        String insert = "INSERT INTO users (id, email, login, name, birthday) VALUES ( ?, ?, ?, ?,?)";
        jdbcTemplate.update(delete, user.getId());
        jdbcTemplate.update(insert, user.getId(), user.getEmail(), user.getLogin(), user.getName(), user.getBirthday());
        return getUserById(user.getId());
    }

    private List<Integer> getUserFriendsByUserId(int userId) {
        String select = "SELECT friends_id " +
                "FROM user_friends " +
                "WHERE user_id = ?";
        return jdbcTemplate.query(select, (rs, rowNum) -> rs.getInt("friends_id"), userId);
    }

    @Override
    public void addToFriend(User targetUser, User friend) {
        String insert = "INSERT INTO user_friends (user_id, friends_id) VALUES ( ?, ?)";
        jdbcTemplate.update(insert, targetUser.getId(), friend.getId());
    }

    @Override
    public void removeFromFriend(User targetUser, User friend) {
        String remove = "DELETE FROM user_friends WHERE user_id = ? AND friends_id = ?";
        jdbcTemplate.update(remove, targetUser.getId(), friend.getId());
    }

    @Override
    public List<HistoryEvent> getFeedsByUserId(int id) {
        String select = "SELECT * " +
                "FROM history_event " +
                "WHERE user_id = ?";
        return jdbcTemplate.query(select, (rs, rowNum) -> makeHistoryEvent(rs), id);
    }

    @Override
    public void addHistoryEvent(int userId, String eventType, String operation, int entityId) {
        String insert = "INSERT INTO history_event (user_id, event_type, operation, entity_id, timestamp) " +
                "VALUES ( ?, ?, ?, ?, ?)";
        jdbcTemplate.update(insert, userId, eventType, operation, entityId, System.currentTimeMillis());
    }

    @Override
    public void deleteUserById(int id) {
        jdbcTemplate.update("DELETE FROM users WHERE ID=?", id);
    }

    private User makeUser(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        User user = User.builder().id(id)
                .login(rs.getString("login"))
                .email(rs.getString("email"))
                .name(rs.getString("name"))
                .birthday(rs.getDate("birthday").toLocalDate())
                .build();
        user.getFriends().addAll(getUserFriendsByUserId(id));
        return user;
    }

    private HistoryEvent makeHistoryEvent(ResultSet rs) throws SQLException {
        return new HistoryEvent(rs.getInt("event_id"),
                rs.getInt("user_id"),
                rs.getString("event_type"),
                rs.getString("operation"),
                rs.getInt("entity_id"),
                rs.getLong("timestamp"));
    }
}