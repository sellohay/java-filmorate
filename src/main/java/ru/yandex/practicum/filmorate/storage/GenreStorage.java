package ru.yandex.practicum.filmorate.storage;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.mapper.GenreRowMapper;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class GenreStorage {

    private static final String GET_ALL_GENRES_QUERY = "SELECT * FROM genres;";
    private static final String SELECT_GENRE_QUERY = "SELECT * FROM genres WHERE genre_id = ?;";
    private static final String SELECT_FILM_GENRES = "SELECT g.genre_id, g.genre_name " +
                    "FROM film_genres fg JOIN genres g ON fg.genre_id = g.genre_id " +
                    "WHERE fg.film_id = ? ORDER BY g.genre_id";
    private static final String SELECT_GENRES_BY_FILM_IDS = "SELECT fg.film_id, g.genre_id, g.genre_name " +
                    "FROM film_genres fg JOIN genres g ON fg.genre_id = g.genre_id " +
                    "WHERE fg.film_id IN (%s) ORDER BY fg.film_id, g.genre_id";
    private static final String COUNT_GENRES_BY_IDS = "SELECT COUNT(*) FROM genres WHERE genre_id IN (%s)";

    private final JdbcTemplate jdbcTemplate;
    private final GenreRowMapper genreRowMapper;

    public GenreStorage(JdbcTemplate jdbcTemplate, GenreRowMapper genreRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.genreRowMapper = genreRowMapper;
    }

    public Collection<Genre> getAllGenres() {
        return jdbcTemplate.query(GET_ALL_GENRES_QUERY, genreRowMapper);
    }

    public Optional<Genre> getGenreById(Long id) {
        try {
            Genre genre = jdbcTemplate.queryForObject(SELECT_GENRE_QUERY, genreRowMapper, id);
            return Optional.ofNullable(genre);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<Genre> getFilmGenres(Long filmId) {
        return jdbcTemplate.query(SELECT_FILM_GENRES, genreRowMapper, filmId);
    }

    public Map<Long, List<Genre>> getGenresForFilms(Set<Long> filmIds) {
        if (filmIds.isEmpty()) {
            return new HashMap<>();
        }
        String inClause = filmIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        String query = String.format(SELECT_GENRES_BY_FILM_IDS, inClause);

        Map<Long, List<Genre>> genresByFilmId = new HashMap<>();
        jdbcTemplate.query(query, rs -> {
            Long filmId = rs.getLong("film_id");
            Genre genre = new Genre();
            genre.setId(rs.getLong("genre_id"));
            genre.setName(rs.getString("genre_name"));
            genresByFilmId.computeIfAbsent(filmId, k -> new ArrayList<>()).add(genre);
        });
        return genresByFilmId;
    }

    public Set<Long> countGenreIds(Set<Long> genreIds) {
        if (genreIds.isEmpty()) {
            return new HashSet<>();
        }
        String inClause = genreIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        String query = String.format("SELECT genre_id FROM genres WHERE genre_id IN (%s)", inClause);
        return new HashSet<>(jdbcTemplate.queryForList(query, Long.class));
    }

}
