package com.yandex.practicum.filmorate.service;

import com.yandex.practicum.filmorate.exeption.NotFoundException;
import com.yandex.practicum.filmorate.model.Genre;
import com.yandex.practicum.filmorate.storage.GenresStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
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