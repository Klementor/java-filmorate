package com.yandex.practicum.filmorate.service;

import com.yandex.practicum.filmorate.exeption.NotFoundException;
import com.yandex.practicum.filmorate.exeption.ValidationException;
import com.yandex.practicum.filmorate.model.Review;
import com.yandex.practicum.filmorate.storage.FilmStorage;
import com.yandex.practicum.filmorate.storage.ReviewsStorage;
import com.yandex.practicum.filmorate.storage.UserStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ReviewsService {
    private static final int DEFAULT_COUNT_REVIEWS = 10;
    private final ReviewsStorage reviewsStorage;
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public Review createReview(Review review) {
        validationReview(review);
        userStorage.getUserById(review.getUserId()).orElseThrow(() -> {
            throw new NotFoundException("Пользователя с id = " + review.getUserId() + " не существует.");
        });

        filmStorage.get(review.getFilmId()).orElseThrow(() -> {
            throw new NotFoundException("Фильм с id = " + review.getFilmId() + " не существует.");
        });

        Review reviewReturned = reviewsStorage.create(review);
        userStorage.addHistoryEvent(review.getUserId(), "REVIEW", "ADD", reviewReturned.getReviewId());
        return reviewReturned;
    }

    public Review updateReview(Review review) {
        validationReview(review);
        Review reviewReturned = reviewsStorage.update(review);
        userStorage.addHistoryEvent(reviewReturned.getUserId(), "REVIEW", "UPDATE", reviewReturned.getReviewId());
        return reviewReturned;
    }

    public Review getReview(int id) {
        return reviewsStorage.getReviewById(id).orElseThrow(() -> {
            throw new NotFoundException("Отзыва с id " + id + " не существует");
        });
    }

    public Review delete(int id) {
        Optional<Review> reviewReturned = reviewsStorage.getReviewById(id);
        reviewsStorage.deleteReviewById(id);
        userStorage.addHistoryEvent(reviewReturned.get().getUserId(), "REVIEW", "REMOVE", reviewReturned.get().getReviewId());
        return reviewReturned.get();
    }

    public List<Review> getReviewsByFilm(String filmIdStr, String countStr) {
        int count = DEFAULT_COUNT_REVIEWS;
        if (countStr != null) {
            count = Integer.parseInt(countStr);
        }
        int filmId = -1;
        if (filmIdStr != null) {
            filmId = Integer.parseInt(filmIdStr);
        }
        return reviewsStorage.getReviewsByFilm(filmId, count);
    }

    public void userLikeReview(int id, int userId, boolean added) {
        reviewsStorage.userLikeReview(id, userId, added);
    }

    public void userDislikeReview(int id, int userId, boolean added) {
        reviewsStorage.userDislikeReview(id, userId, added);

    }

    private void validationReview(Review review) {
        if (review.getContent() == null || review.getContent().isBlank()) {
            throw new ValidationException("Тело отзыва пустое.");
        }
        if (review.getUserId() == null) {
            throw new ValidationException("User не может быть пустым.");
        }

        if (review.getFilmId() == null) {
            throw new ValidationException("Film не может быть пустым.");
        }

        if (review.getIsPositive() == null) {
            throw new ValidationException("Отзыв должен быть позитивным или негативным.");
        }
    }
}
