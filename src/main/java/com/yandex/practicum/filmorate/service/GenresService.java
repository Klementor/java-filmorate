package com.yandex.practicum.filmorate.service;

import com.yandex.practicum.filmorate.exeption.NotFoundException;
import com.yandex.practicum.filmorate.model.Genre;
import com.yandex.practicum.filmorate.storage.GenresStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class GenresService {
    private final GenresStorage genresStorage;

    public List<Genre> getAllGenres() {
        return genresStorage.getGenres();
    }

    public Genre getGenreById(int id) {
        return genresStorage.getGenreById(id).orElseThrow(() -> {
            throw new NotFoundException("Жанр с id = " + id + " не существует.");
        });
    }
}