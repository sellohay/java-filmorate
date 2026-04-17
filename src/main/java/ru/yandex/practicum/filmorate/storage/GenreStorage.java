package ru.yandex.practicum.filmorate.storage;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.mapper.GenreRowMapper;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Component
public class GenreStorage {

    private static final String GET_ALL_GENRES_QUERY = "SELECT * FROM genres;";
    private static final String SELECT_GENRE_QUERY = "SELECT * FROM genres WHERE genre_id = ?;";
    private static final String SELECT_FILM_GENRES = "SELECT g.genre_id, g.genre_name " +
                    "FROM film_genres fg JOIN genres g ON fg.genre_id = g.genre_id " +
                    "WHERE fg.film_id = ? ORDER BY g.genre_id";

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

}
