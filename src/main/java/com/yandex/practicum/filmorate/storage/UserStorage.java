package com.yandex.practicum.filmorate.storage;

import com.yandex.practicum.filmorate.model.HistoryEvent;
import com.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Optional;

public interface UserStorage {
    List<User> getUsers();

    Optional<User> getUserById(int id);

    User createUser(User user);

    Optional<User> updateUser(User user);

    void addToFriend(User targetUser, User friend);

    void removeFromFriend(User targetUser, User friend);

    List<HistoryEvent> getFeedsByUserId(int id);

    void addHistoryEvent(int userId, String eventType, String operation, int entityId);

    void deleteUserById(int id);
}