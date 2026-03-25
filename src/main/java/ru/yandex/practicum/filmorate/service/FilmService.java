package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {

    private static final int POPULAR_FILMS_DEFAULT = 10;
    private final FilmStorage filmStorage;
    private final UserService userService;
    private static final LocalDate START_DATE = LocalDate.of(1895, 12, 28);

    public FilmService(FilmStorage filmStorage, UserService userService) {
        this.filmStorage = filmStorage;
        this.userService = userService;
    }

    public Film createFilm(Film film) {
        validateCreateFilm(film);
        return filmStorage.createFilm(film);
    }

    public Film updateFilm(Film film) {
        validateUpdateFilm(film);
        return filmStorage.updateFilm(film);
    }

    public Collection<Film> getFilms() {
        return filmStorage.getFilms();
    }

    public Optional<Film> getFilmById(long id) {
        return filmStorage.getFilmById(id);
    }

    public void addLike(Long userId, Long filmId) {
        userService.validateUser(userId);
        validateFilm(filmId);
        Film film = filmStorage.getFilmById(filmId).get();
        Set<Long> likes = film.getLikedUsers();
        if (likes.contains(userId)) {
            log.warn("Пользователь id={} уже лайкнул фильм с id={}", userId, filmId);
            return;
        }
        likes.add(userId);
    }

    public void removeLike(Long userId, Long filmId) {
        userService.validateUser(userId);
        validateFilm(filmId);
        Film film = filmStorage.getFilmById(filmId).get();
        Set<Long> likes = film.getLikedUsers();
        if (!likes.contains(userId)) {
            log.warn("Пользователь id={} не лайкал фильм с id={}", userId, filmId);
            return;
        }
        likes.remove(userId);
    }

    public List<Film> getPopularFilms(Optional<Integer> countOpt) {
        int count = countOpt.orElse(POPULAR_FILMS_DEFAULT);
        if (count <= 0) {
            log.error("Введено неверное количество фильмов: {}", count);
            throw new ValidationException("Количество фильмов должно быть положительным");
        }
        return filmStorage.getFilms().stream()
                .sorted((o1, o2) -> o2.getLikedUsers().size() - o1.getLikedUsers().size())
                .limit(count)
                .collect(Collectors.toList());
    }

    private void validateFilm(Long id) {
        if (filmStorage.getFilmById(id).isEmpty()) {
            log.error("Фильм с id={} не найден", id);
            throw new NotFoundException("Фильм с id=" + id + " не найден");
        }
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
        if (film.getReleaseDate() != null) {
            if (film.getReleaseDate().isBefore(START_DATE)) {
                log.error("Дата релиза фильма была раньше дня рождения кино: {}", film.getReleaseDate());
                throw new ValidationException("Дата релиза должна быть позже 28.12.1895");
            }
        }
    }
}
