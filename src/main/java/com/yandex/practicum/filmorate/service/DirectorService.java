package com.yandex.practicum.filmorate.service;

import com.yandex.practicum.filmorate.exeption.NotFoundException;
import com.yandex.practicum.filmorate.model.Director;
import com.yandex.practicum.filmorate.storage.DirectorStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DirectorService {

    private final DirectorStorage directorStorage;

    public List<Director> getDirectors() {
        return directorStorage.getDirectors();
    }

    public Director getDirectorById(int id) {
        try {
            return directorStorage.getDirectorById(id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Режиссера под таким id не существует");
        }
    }

    public Director postDirector(Director director) {
        return directorStorage.addDirector(director);
    }

    public Director updateDirector(Director director) {
        getDirectorById(director.getId());
        return directorStorage.updateDirector(director);
    }

    public void deleteDirectorById(int id) {
        getDirectorById(id);
        directorStorage.deleteDirectorById(id);
    }
}