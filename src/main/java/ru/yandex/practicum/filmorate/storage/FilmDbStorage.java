package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmLike;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.service.GenreService;
import ru.yandex.practicum.filmorate.service.MpaRatingService;
import ru.yandex.practicum.filmorate.storage.mapper.FilmLikeRowMapper;
import ru.yandex.practicum.filmorate.storage.mapper.FilmRowMapper;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component("FilmDbStorage")
public class FilmDbStorage implements FilmStorage {

    private static final String SELECT_QUERY = "SELECT * FROM films;";
    private static final String GET_FILM_BY_ID = "SELECT * FROM films WHERE film_id = ?;";
    private static final String INSERT_FILM_QUERY = "INSERT INTO films(name, description, " +
            "release_date, duration, mpa) VALUES (?, ?, ?, ?, ?);";
    private static final String UPDATE_FILM_QUERY = "UPDATE films SET name = ?, description = ?, " +
            "release_date = ?, duration = ?, mpa = ? WHERE film_id = ?;";
    private static final String CHECK_LIKE_QUERY = "SELECT * FROM film_likes WHERE film_id = ? AND user_id = ?;";
    private static final String INSERT_FILM_LIKE = "INSERT INTO film_likes(film_id, user_id, liked_at) VALUES (?, ?, ?);";
    private static final String DELETE_LIKE_QUERY = "DELETE FROM film_likes WHERE film_likes_id = ?;";
    private static final String SELECT_POPULAR_FILMS = "SELECT f.* FROM films f " +
                    "LEFT JOIN film_likes fl ON f.film_id = fl.film_id " +
                    "GROUP BY f.film_id ORDER BY COUNT(fl.film_likes_id) DESC LIMIT ?";
    private static final String INSERT_FILM_GENRE = "INSERT INTO film_genres(film_id, genre_id) VALUES (?, ?);";
    private static final String DELETE_FILM_GENRES = "DELETE FROM film_genres WHERE film_id = ?;";
    private static final String SELECT_FILM_GENRES = "SELECT genre_id FROM film_genres " +
            "WHERE film_id = ? ORDER BY genre_id";

    private final JdbcTemplate jdbcTemplate;
    private final FilmRowMapper filmRowMapper;
    private final FilmLikeRowMapper filmLikeRowMapper;
    private final MpaRatingService mpaRatingService;
    private final GenreService genreService;

    public FilmDbStorage(JdbcTemplate jdbcTemplate, FilmRowMapper filmRowMapper,
                         FilmLikeRowMapper filmLikeRowMapper, MpaRatingService mpaRatingService, GenreService genreService) {
        this.jdbcTemplate = jdbcTemplate;
        this.filmRowMapper = filmRowMapper;
        this.filmLikeRowMapper = filmLikeRowMapper;
        this.mpaRatingService = mpaRatingService;
        this.genreService = genreService;
    }

    @Override
    public Collection<Film> getFilms() {
        Collection<Film> films = jdbcTemplate.query(SELECT_QUERY, filmRowMapper);
        for (Film film : films) {
            setFilmProperties(film);
        }
        return films;
    }

    @Override
    public Film createFilm(Film film) {
        Long id = insertTemplate(INSERT_FILM_QUERY,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa().getId()
        );
        film.setId(id);
        insertFilmGenres(film);
        setFilmProperties(film);
        log.info("Создан новый фильм с id={}", film.getId());
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        jdbcTemplate.update(UPDATE_FILM_QUERY,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId()
                );
        jdbcTemplate.update(DELETE_FILM_GENRES, film.getId());
        insertFilmGenres(film);
        setFilmProperties(film);
        return film;
    }

    @Override
    public Optional<Film> getFilmById(long id) {
        try {
            Film result = jdbcTemplate.queryForObject(GET_FILM_BY_ID, filmRowMapper, id);
            if (result != null) {
                setFilmProperties(result);
            }
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        try {
            FilmLike result = jdbcTemplate.queryForObject(CHECK_LIKE_QUERY, filmLikeRowMapper, filmId, userId);
            if (result != null) {
                log.warn("Пользователь с id={} уже поставил лайк фильму с id={}", userId, filmId);
            }
        } catch (EmptyResultDataAccessException ignored) {
            insertTemplate(INSERT_FILM_LIKE,
                    filmId,
                    userId,
                    Date.valueOf(LocalDate.now()));
        }
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        try {
            FilmLike result = jdbcTemplate.queryForObject(CHECK_LIKE_QUERY, filmLikeRowMapper, filmId, userId);
            jdbcTemplate.update(DELETE_LIKE_QUERY, result.getId());
        } catch (EmptyResultDataAccessException ignored) {
            log.warn("Пользователь с id={} не ставил лайк фильму с id={}", userId, filmId);
        }
    }

    @Override
    public List<Film> getPopularFilms(int count) {
        try {
            List<Film> films = jdbcTemplate.query(SELECT_POPULAR_FILMS, filmRowMapper, count);
            for (Film film : films) {
                setFilmProperties(film);
            }
            return films;
        } catch (EmptyResultDataAccessException ignored) {
            return List.of();
        }
    }

    private Long insertTemplate(String query, Object... params) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            return ps;
        }, keyHolder);
        return keyHolder.getKeyAs(Long.class);
    }

    private void insertFilmGenres(Film film) {
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            List<Long> genreIds = film.getGenres().stream()
                    .map(Genre::getId)
                    .distinct()
                    .toList();

            if (genreIds.isEmpty()) {
                return;
            }
            List<Object[]> batchArgs = genreIds.stream()
                    .map(genreId -> new Object[]{film.getId(), genreId})
                    .collect(Collectors.toList());
            jdbcTemplate.batchUpdate(INSERT_FILM_GENRE, batchArgs);
        }
    }

    private void setFilmProperties(Film film) {
        film.setMpa(mpaRatingService.getRating(film.getMpa().getId()));
        film.setGenres(getFilmGenres(film.getId()));
    }

    private List<Genre> getFilmGenres(Long filmId) {
        List<Long> genreIds = jdbcTemplate.query(SELECT_FILM_GENRES,
                (rs, rowNum) -> rs.getLong("genre_id"),
                filmId
        );
        return genreIds.stream()
                .map(genreService::getGenreById)
                .collect(Collectors.toList());
    }

}
