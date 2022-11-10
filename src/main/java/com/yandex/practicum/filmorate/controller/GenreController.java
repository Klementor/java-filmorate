package com.yandex.practicum.filmorate.controller;

import com.yandex.practicum.filmorate.model.Genre;
import com.yandex.practicum.filmorate.service.GenresService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/genres")
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class GenreController {
    private final GenresService genresService;

    @GetMapping()
    public List<Genre> getAllGenres() {
        return genresService.getAllGenres();
    }

    @GetMapping("/{id}")
    public Genre getGenreById(@PathVariable int id) {
        return genresService.getGenreById(id);
    }
}