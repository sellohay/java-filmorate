package ru.yandex.practicum.filmorate.storage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.mapper.MpaRatingRowMapper;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class MpaRatingStorage {

    private static final String SELECT_ALL_RATINGS = "SELECT * FROM mpa_rating;";
    private static final String SELECT_RATING = "SELECT * FROM mpa_rating WHERE mpa_rating_id = ?;";
    private static final String SELECT_MPA_BY_IDS = "SELECT mpa_rating_id, rating_name " +
                    "FROM mpa_rating WHERE mpa_rating_id IN (%s)";

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

    public Map<Long, MpaRating> getMpaByIds(Set<Long> mpaIds) {
        if (mpaIds.isEmpty()) {
            return new HashMap<>();
        }
        String inClause = mpaIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        String query = String.format(SELECT_MPA_BY_IDS, inClause);

        Map<Long, MpaRating> mpaMap = new HashMap<>();
        jdbcTemplate.query(query, rs -> {
            MpaRating mpa = new MpaRating();
            mpa.setId(rs.getLong("mpa_rating_id"));
            mpa.setName(rs.getString("rating_name"));
            mpaMap.put(mpa.getId(), mpa);
        });
        return mpaMap;
    }
}
