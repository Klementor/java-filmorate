package com.yandex.practicum.filmorate.controller;

import com.yandex.practicum.filmorate.model.Mpa;
import com.yandex.practicum.filmorate.service.MpaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/mpa")
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class MpaController {
    private final MpaService mpaService;

    @GetMapping()
    public List<Mpa> getAllMpa() {
        return mpaService.getAllMpa();
    }

    @GetMapping("/{id}")
    public Mpa getMpaById(@PathVariable int id) {
        return mpaService.getMpaById(id);
    }
}