package com.yandex.practicum.filmorate.controller;

import com.yandex.practicum.filmorate.model.Director;
import com.yandex.practicum.filmorate.service.DirectorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/directors")
@RequiredArgsConstructor
public class DirectorController {
    private final DirectorService directorService;

    @GetMapping
    public List<Director> getDirectors() {
        return directorService.getDirectors();
    }

    @GetMapping("/{id}")
    public Director getDirectorById(@PathVariable int id) {
        return directorService.getDirectorById(id);
    }

    @PostMapping
    public Director postDirector(@Valid @RequestBody Director director) {
        return directorService.postDirector(director);
    }

    @PutMapping
    public Director updateDirector(@Valid @RequestBody Director director) {
        return directorService.updateDirector(director);
    }

    @DeleteMapping("/{id}")
    public void deleteDirectorById(@PathVariable int id) {
        directorService.deleteDirectorById(id);
    }
}
