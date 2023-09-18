package com.yandex.practicum.filmorate.controller;

import com.yandex.practicum.filmorate.model.Review;
import com.yandex.practicum.filmorate.service.ReviewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewsController {
    private final ReviewsService reviewsService;

    @PostMapping
    public Review createReview(@Valid @RequestBody Review review) {
        return reviewsService.createReview(review);
    }

    @PutMapping
    public Review updateReview(@Valid @RequestBody Review review) {
        return reviewsService.updateReview(review);
    }

    @GetMapping("/{id}")
    public Review getReviewById(@PathVariable int id) {
        return reviewsService.getReview(id);
    }

    @DeleteMapping("/{id}")
    public Review deleteReviewById(@PathVariable int id) {
        return reviewsService.delete(id);
    }

    @GetMapping
    public List<Review> getReviewsByFilmId(@RequestParam(required = false) String filmId, @RequestParam(required = false) String count) {
        return reviewsService.getReviewsByFilm(filmId, count);
    }

    @PutMapping("/{id}/like/{userId}")
    public void userAddLikeReview(@PathVariable int id, @PathVariable int userId) {
        reviewsService.userLikeReview(id, userId, true);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public void userAddDislikeReview(@PathVariable int id, @PathVariable int userId) {
        reviewsService.userDislikeReview(id, userId, true);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void userDeleteLikeReview(@PathVariable int id, @PathVariable int userId) {
        reviewsService.userLikeReview(id, userId, false);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public void userDeleteDislikeReview(@PathVariable int id, @PathVariable int userId) {
        reviewsService.userDislikeReview(id, userId, false);
    }
}