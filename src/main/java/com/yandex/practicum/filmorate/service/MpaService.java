package com.yandex.practicum.filmorate.service;

import com.yandex.practicum.filmorate.exeption.NotFoundException;
import com.yandex.practicum.filmorate.model.Mpa;
import com.yandex.practicum.filmorate.storage.MpaStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MpaService {
    private final MpaStorage mpaStorage;

    public Mpa getMpaById(int id) {
        return mpaStorage.getMpaById(id).orElseThrow(() -> {
            throw new NotFoundException("Рейтинг с id = " + id + " не существует.");
        });
    }

    public List<Mpa> getAllMpa() {
        return mpaStorage.getMpa();
    }
}