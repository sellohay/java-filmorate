package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class FilmService {

    private static final int POPULAR_FILMS_DEFAULT = 10;
    private final FilmStorage filmStorage;
    private final UserService userService;
    private final GenreService genreService;
    private final MpaRatingService mpaRatingService;
    private static final LocalDate START_DATE = LocalDate.of(1895, 12, 28);

    public FilmService(@Qualifier("FilmDbStorage") FilmStorage filmStorage, UserService userService, GenreService genreService, MpaRatingService mpaRatingService) {
        this.filmStorage = filmStorage;
        this.userService = userService;
        this.genreService = genreService;
        this.mpaRatingService = mpaRatingService;
    }

    public Film createFilm(Film film) {
        validateCreateFilm(film);
        validateFields(film);
        return filmStorage.createFilm(film);
    }

    public Film updateFilm(Film film) {
        validateUpdateFilm(film);
        validateFields(film);
        return filmStorage.updateFilm(film);
    }

    public Collection<Film> getFilms() {
        return filmStorage.getFilms();
    }

    public Film getFilmById(long id) {
        Optional<Film> filmOpt = filmStorage.getFilmById(id);
        if (filmOpt.isEmpty()) {
            throw new NotFoundException("Фильм с id=" + id + " не найден");
        }
        return filmOpt.get();
    }

    public void addLike(Long userId, Long filmId) {
        userService.validateUser(userId);
        validateFilm(filmId);
        filmStorage.addLike(filmId, userId);
    }

    public void removeLike(Long userId, Long filmId) {
        userService.validateUser(userId);
        validateFilm(filmId);
        filmStorage.removeLike(filmId, userId);
    }

    public List<Film> getPopularFilms(Optional<Integer> countOpt) {
        int count = countOpt.orElse(POPULAR_FILMS_DEFAULT);
        if (count <= 0) {
            log.error("Введено неверное количество фильмов: {}", count);
            throw new ValidationException("Количество фильмов должно быть положительным");
        }
        return filmStorage.getPopularFilms(count);
    }

    private void validateFilm(Long id) {
        filmStorage.getFilmById(id)
                .orElseThrow(() ->
                        new NotFoundException("Фильм с id=" + id + " не найден")
                );
    }

    private void validateCreateFilm(Film film) {
        if (film == null) {
            log.error("Был передан пустой фильм");
            throw new ValidationException("Фильм не был передан");
        }

        if (film.getReleaseDate().isBefore(START_DATE)) {
            log.error("Дата релиза фильма была раньше дня рождения кино: {}", film.getReleaseDate());
            throw new ValidationException("Дата релиза должна быть позже 28.12.1895");
        }
    }

    private void validateUpdateFilm(Film film) {
        if (film == null || film.getId() == null) {
            log.error("Не указан ID фильма");
            throw new ValidationException("ID фильма не был передан");
        }
        Optional<Film> filmOpt = filmStorage.getFilmById(film.getId());
        if (filmOpt.isEmpty()) {
            log.error("Фильм с id={} не найден", film.getId());
            throw new NotFoundException("Фильм не найден");
        }
        if (film.getReleaseDate() != null && film.getReleaseDate().isBefore(START_DATE)) {
            log.error("Дата релиза фильма была раньше дня рождения кино: {}", film.getReleaseDate());
            throw new ValidationException("Дата релиза должна быть позже 28.12.1895");
        }
    }

    private void validateFields(Film film) {
        validateMpa(film);
        validateGenres(film);
    }

    private void validateMpa(Film film) {
        if (film.getMpa() == null || film.getMpa().getId() == null) {
            log.error("Не указан рейтинг MPA");
            throw new ValidationException("Рейтинг MPA обязателен");
        }
        mpaRatingService.getRating(film.getMpa().getId());
    }

    private void validateGenres(Film film) {
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            for (Genre genre : film.getGenres()) {
                genreService.getGenreById(genre.getId());
            }
        }
    }
}
