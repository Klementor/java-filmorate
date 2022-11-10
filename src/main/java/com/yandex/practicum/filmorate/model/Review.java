package com.yandex.practicum.filmorate.model;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Review {
    private int reviewId;
    @NonNull
    private String content;
    @NonNull
    private Boolean isPositive;
    @NonNull
    private Integer userId;
    @NonNull
    private Integer filmId;
    private int useful;
}
