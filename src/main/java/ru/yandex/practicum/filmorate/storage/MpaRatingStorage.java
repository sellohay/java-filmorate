package ru.yandex.practicum.filmorate.storage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.mapper.MpaRatingRowMapper;

import java.util.Collection;
import java.util.Optional;

@Component
public class MpaRatingStorage {

    private static final String SELECT_ALL_RATINGS = "SELECT * FROM mpa_rating;";
    private static final String SELECT_RATING = "SELECT * FROM mpa_rating WHERE mpa_rating_id = ?;";

    private final JdbcTemplate jdbcTemplate;
    private final MpaRatingRowMapper rowMapper;

    public MpaRatingStorage(JdbcTemplate jdbcTemplate, MpaRatingRowMapper rowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.rowMapper = rowMapper;
    }

    public Collection<MpaRating> getAllRatings() {
        return jdbcTemplate.query(SELECT_ALL_RATINGS, rowMapper);
    }

    public Optional<MpaRating> getRatingById(Long id) {
        try {
            MpaRating rating = jdbcTemplate.queryForObject(SELECT_RATING, rowMapper, id);
            return Optional.ofNullable(rating);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
