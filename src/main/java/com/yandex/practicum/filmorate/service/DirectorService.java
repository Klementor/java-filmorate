package com.yandex.practicum.filmorate.service;

import com.yandex.practicum.filmorate.exeption.NotFoundException;
import com.yandex.practicum.filmorate.model.Director;
import com.yandex.practicum.filmorate.storage.DirectorStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DirectorService {

    private final DirectorStorage directorStorage;

    public List<Director> getDirectors() {
        return directorStorage.getDirectors();
    }

    public Director getDirector(int id) {
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
        getDirector(director.getId());
        return directorStorage.updateDirector(director);
    }

    public void deleteDirector(int id) {
        getDirector(id);
        directorStorage.deleteDirector(id);
    }
}
