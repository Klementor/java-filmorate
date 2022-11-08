package com.yandex.practicum.filmorate.controller;

import com.yandex.practicum.filmorate.model.Film;
import com.yandex.practicum.filmorate.service.FilmService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    private final FilmService filmService;

    @Autowired
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping()
    public List<Film> getAll() {
        return filmService.getFilms();
    }

    @PostMapping()
    public Film create(@RequestBody Film film) {
        return filmService.createFilm(film);
    }

    @PutMapping()
    public Film update(@RequestBody Film film) {
        return filmService.updateFilm(film);
    }

    @GetMapping("/{id}")
    public Film getFilm(@PathVariable int id) {
        return filmService.getFilm(id);
    }

    @PutMapping("/{id}/like/{userId}")
    public void likeFilm(@PathVariable int id, @PathVariable int userId) {
        filmService.likeFilm(userId, id);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void unlikeFilm(@PathVariable int id, @PathVariable int userId) {
        filmService.unlikeFilm(userId, id);
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilms(@RequestParam(required = false) String count) {
        return filmService.getMostPopularFilms(count);
    }

    @GetMapping("/director/{directorId}")
    public List<Film> getSortedFilms(@PathVariable int directorId, @RequestParam String sortBy) {
        return filmService.getSortedFilms(directorId, sortBy);
    }
}
