package com.yandex.practicum.filmorate.storage;

import com.yandex.practicum.filmorate.model.Review;

import java.util.List;
import java.util.Optional;

public interface ReviewsStorage {
    Review createReview(Review review);

    Review updateReview(Review review);

    Optional<Review> getReviewById(int id);

    void deleteReviewById(int id);

    List<Review> getReviewsByFilm(int filmId, int count);

    List<Review> getReviewsWithoutFilm(int count);

    void userLikeReview(int id, int userId, boolean added);

    void userDislikeReview(int id, int userId, boolean added);
}