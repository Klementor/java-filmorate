package com.yandex.practicum.filmorate.model;


import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Film {
    private int id;
    @NonNull
    private String name;
    private String description;
    @NonNull
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate releaseDate;
    @NonNull
    private int duration;
    private Mpa mpa;
    private final Set<Director> directors = new HashSet<>();
    private final Set<Integer> likes = new HashSet<>();
    private final Set<Genre> genres = new HashSet<>();
}
