package com.yandex.practicum.filmorate.storage;

import com.yandex.practicum.filmorate.model.Review;

import java.util.List;
import java.util.Optional;

public interface ReviewsStorage {
    Review create(Review review);

    Review update(Review review);

    Optional<Review> getReviewById(int id);

    Review deleteReviewById(int id);

    List<Review> getReviewsByFilm(int filmId, int count);

    void userLikeReview(int id, int userId, boolean added);

    void userDislikeReview(int id, int userId, boolean added);
}
