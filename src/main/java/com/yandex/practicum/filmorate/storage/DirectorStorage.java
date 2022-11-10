package com.yandex.practicum.filmorate.storage;

import com.yandex.practicum.filmorate.model.Director;

import java.util.List;
import java.util.Set;

public interface DirectorStorage {

    List<Director> getDirectors();

    Director getDirectorById(int id);

    Set<Director> getDirectorByFilmId(int id);

    Director addDirector(Director director);

    Director updateDirector(Director director);

    void deleteDirector(int id);

    void addDirectors(int id, Set<Integer> directorIds);
}
