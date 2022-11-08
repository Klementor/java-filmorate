package com.yandex.practicum.filmorate.service;

import com.yandex.practicum.filmorate.exeption.NotFoundException;
import com.yandex.practicum.filmorate.exeption.ValidationException;
import com.yandex.practicum.filmorate.model.Film;
import com.yandex.practicum.filmorate.model.User;
import com.yandex.practicum.filmorate.storage.FilmStorage;
import com.yandex.practicum.filmorate.storage.UserStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserService {

    private final UserStorage userStorage;
    private final FilmStorage filmStorage;
    private final FilmService filmService;
    private int idGenerator = 0;

    public User createUser(User user) {
        if (user == null) {
            throw new ValidationException("Пользователь не может быть создан.");
        }
        validationUser(user);

        user.setId(generatedId());
        return userStorage.create(user);
    }

    public User updateUser(User user) {
        if (user == null) {
            throw new ValidationException("Пользователь не может быть обновлен.");
        }
        if (userStorage.getUserById(user.getId()).isEmpty()) {
            log.warn("Пользователь с id {} не существует.", user.getId());
            throw new NotFoundException("Пользователь не существует.");
        }
        validationUser(user);
        return userStorage.update(user).get();
    }

    public User getUser(Integer userId) {
        return userStorage.getUserById(userId).orElseThrow(() -> {
            throw new NotFoundException("Пользователя с id = " + userId + " не существует.");
        });
    }

    public List<User> getUsers() {
        return userStorage.getUsers();
    }

    public void addToFriends(int targetUserId, int friendId) {
        User targetUser = getUser(targetUserId);
        User friend = getUser(friendId);
        userStorage.addToFriend(targetUser, friend);
    }

    public void removeFromFriends(int targetUserId, int friendId) {
        User targetUser = getUser(targetUserId);
        User friend = getUser(friendId);
        userStorage.removeFromFriend(targetUser, friend);
    }

    public List<User> getFriends(int userId) {
        User user = getUser(userId);
        return user.getFriends().stream()
                .map(userStorage::getUserById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(int targetUserId, int otherUserId) {
        User targetUser = getUser(targetUserId);
        User otherUser = getUser(otherUserId);
        return targetUser.getFriends().stream().filter(id -> otherUser.getFriends().contains(id))
                .map(userStorage::getUserById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private void validationUser(User user) {
        if (user.getLogin().isBlank()) {
            log.warn("Логин не может быть пустым.");
            throw new ValidationException("Логин не может быть пустым.");
        }

        if (user.getLogin().contains(" ")) {
            log.warn("Некорректный логин {}.", user.getLogin());
            throw new ValidationException("Некорректный логин " + user.getLogin() + ".");

        }

        if (user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            log.warn("Некорректный адрес электронной почты {}.", user.getEmail());
            throw new ValidationException("Некорректный адрес электронной почты " + user.getEmail() + ".");

        }

        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.warn("Неверная дата рождения {}.", user.getBirthday());
            throw new ValidationException("Неверная дата рождения " + user.getBirthday() + ".");

        }

        if (user.getName().isBlank()) {
            log.warn("Имя пользователя пустое. Используем для имения значение логина {}.", user.getLogin());
            user.setName(user.getLogin());
        }
    }

    private int generatedId() {
        return ++idGenerator;
    }


    public Set<Film> getRecommendation(int userId) {
        Map<Integer, Set<Integer>> data = initializeData();
        Map<Integer, Integer> similarsMap = getSimilarMap(userId, data);
        Set<Integer> userFilmSet = data.get(userId);
        Set<Film> recomSet = new HashSet<>();
        AtomicLong count = new AtomicLong(0);
        similarsMap.keySet()
                .forEach(u -> {
                    if (similarsMap.get(u) >= count.get()) {
                        data.get(u).stream()
                                .filter(f -> !userFilmSet.contains(f))
                                .map(filmService::getFilm)
                                .forEach(recomSet::add);
                        count.set(similarsMap.get(u));
                    }
                });
        return recomSet;
    }

    private Map<Integer, Integer> getSimilarMap(int userId, Map<Integer, Set<Integer>> data) {
        Map<Integer, Integer> similarLikesMap = new HashMap<>();
        Set<Integer> userLikesSet = data.get(userId);
        data.keySet().stream()
                .filter(user -> !similarLikesMap.containsKey(user) && user != userId).forEach(user -> {
                    Integer similar = (int) data.get(user).stream()
                            .filter(userLikesSet::contains)
                            .count();
                    similarLikesMap.put(user, similar);
                });

        return similarLikesMap;
    }

    private Map<Integer, Set<Integer>> initializeData() {
        Map<Integer, Set<Integer>> data = new HashMap<>();
        filmStorage.getAllLikes().forEach(like -> {
            if (!data.containsKey(like.getUserId())) {
                data.put(like.getUserId(), new HashSet<>());
            }
            data.get(like.getUserId()).add(like.getFilmId());
        });
        return data;
    }



    public void deleteUser(int userId) {
        userStorage.getUserById(userId).orElseThrow(() -> {
            throw new NotFoundException("Пользователя с id = " + userId + " не существует.");
        });
        userStorage.deleteUser(userId);
    }
}
