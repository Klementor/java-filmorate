package com.yandex.practicum.filmorate.controller;

import com.yandex.practicum.filmorate.model.Film;
import com.yandex.practicum.filmorate.service.FilmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.Set;


@RestController
@RequestMapping("/films")
public class FilmController {
    private final FilmService filmService;

    @Autowired
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping
    public List<Film> getAllFilms() {
        return filmService.getFilms();
    }

    @PostMapping
    public Film createFilm(@RequestBody Film film) {
        return filmService.createFilm(film);
    }

    @GetMapping("/search")
    @ResponseBody
    public List<Film> searchByParameter(@RequestParam String query, @RequestParam Optional<String> by) {
        return filmService.search(query, by.orElse(null));
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        return filmService.updateFilm(film);
    }

    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable int id) {
        return filmService.getFilmById(id);
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
    public List<Film> getPopularFilms(@RequestParam(defaultValue = "10", required = false) Integer count,
                                      @RequestParam(required = false) Integer genreId,
                                      @RequestParam(required = false) Integer year) {
        return filmService.getMostPopularFilms(count, genreId, year);
    }

    @GetMapping("/common")
    public Set<Film> getCommonFilms(@RequestParam int userId, @RequestParam int friendId) {
        return filmService.getCommonFilms(userId, friendId);
    }

    @GetMapping("/director/{directorId}")
    public List<Film> getSortedFilmsByParameter(@PathVariable int directorId, @RequestParam String sortBy) {
        return filmService.getSortedFilmsByParameter(directorId, sortBy);
    }

    @DeleteMapping("/{id}")
    public void deleteFilmById(@PathVariable int id) {
        filmService.deleteFilmById(id);
    }
}
