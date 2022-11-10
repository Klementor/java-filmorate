package com.yandex.practicum.filmorate.storage.dao;

import com.yandex.practicum.filmorate.model.Review;
import com.yandex.practicum.filmorate.storage.ReviewsStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component("reviewStorage")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ReviewsDbStorage implements ReviewsStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Review create(Review review) {
        KeyHolder keyHolder = null;
        String insert = "INSERT INTO reviews (content, positive, film_id, user_id) VALUES (?,?,?,?)";
        keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(insert, new String[]{"id"});
            ps.setString(1, review.getContent());
            ps.setBoolean(2, review.getIsPositive());
            ps.setInt(3, review.getFilmId());
            ps.setInt(4, review.getUserId());
            return ps;
        }, keyHolder);

        return getReviewById(Objects.requireNonNull(keyHolder.getKey()).intValue()).get();
    }

    @Override
    public Review update(Review review) {
        String update = "UPDATE reviews SET content = ?, positive = ? WHERE id = ?";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(update, new String[]{"id"});
            ps.setString(1, review.getContent());
            ps.setBoolean(2, review.getIsPositive());
            ps.setInt(3, review.getReviewId());
            return ps;
        }, keyHolder);
        return getReviewById(Objects.requireNonNull(keyHolder.getKey()).intValue()).get();
    }

    @Override
    public Optional<Review> getReviewById(int id) {
        try {
            String select = "SELECT reviews.*, IFNULL(SUM(r.reaction), 0) as useful " +
                    "FROM reviews " +
                    "LEFT JOIN review_reactions AS r ON reviews.id = r.review_id " +
                    "WHERE reviews.id = ? " +
                    "GROUP BY reviews.id ";
            SqlRowSet reviewRow = jdbcTemplate.queryForRowSet(select, id);
            if (reviewRow.next()) {
                Review review = Review.builder()
                        .reviewId(reviewRow.getInt("id"))
                        .content(reviewRow.getString("content"))
                        .isPositive(reviewRow.getBoolean("positive"))
                        .userId(reviewRow.getInt("user_id"))
                        .filmId(reviewRow.getInt("film_id"))
                        .useful(reviewRow.getInt("useful"))
                        .build();
                return Optional.of(review);
            } else {
                return Optional.empty();
            }
        } catch (DataAccessException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    @Override
    public Review deleteReviewById(int id) {
        String delete = "DELETE FROM reviews WHERE id = ?";
        jdbcTemplate.update(delete, id);
        return null;
    }

    @Override
    public List<Review> getReviewsByFilm(int filmId, int count) {
        String whereFilm = filmId == -1 ? "" : String.format("WHERE reviews.film_id = (%s) ", filmId);
        String select = "SELECT reviews.*, IFNULL(SUM(r.reaction), 0) as useful " +
                "FROM reviews " +
                "LEFT JOIN review_reactions AS r ON reviews.id = r.review_id " + whereFilm +
                "GROUP BY reviews.id " +
                "ORDER BY useful DESC " +
                "LIMIT ? ";
        return jdbcTemplate.query(select, (rs, rowNum) -> makeReview(rs), count);
    }

    @Override
    public void userLikeReview(int id, int userId, boolean added) {
        if (added) {
            String update = "INSERT INTO review_reactions (user_id, review_id, reaction) VALUES (?,?,?)";
            jdbcTemplate.update(update, userId, id, 1);
        } else {
            String delete = String.format("DELETE FROM review_reactions WHERE user_id = (%s) AND review_id = (%s)", userId, id);
            jdbcTemplate.update(delete);
        }
    }

    @Override
    public void userDislikeReview(int id, int userId, boolean added) {
        if (added) {
            String update = "INSERT INTO review_reactions (user_id, review_id, reaction) VALUES (?,?,?)";
            jdbcTemplate.update(update, userId, id, -1);
        } else {
            String delete = String.format("DELETE FROM review_reactions WHERE user_id = (%s) AND review_id = (%s)", userId, id);
            jdbcTemplate.update(delete);
        }
    }

    private Review makeReview(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        return Review.builder()
                .reviewId(id)
                .content(rs.getString("content"))
                .isPositive(rs.getBoolean("positive"))
                .userId(rs.getInt("user_id"))
                .filmId(rs.getInt("film_id"))
                .useful(rs.getInt("useful"))
                .build();
    }
}
