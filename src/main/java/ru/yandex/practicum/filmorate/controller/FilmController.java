package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private final Map<Long, Film> films = new HashMap<>();

    //добавление фильма
    @PostMapping
    public Film createFilm(@Valid @RequestBody Film film) {
        log.info("Получен запрос POST /films");

        if (film == null) {
            log.error("Был передан пустой фильм");
            throw new ValidationException("Фильм не был передан");
        }

        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.error("Дата релиза фильма была раньше дня рождения кино: {}", film.getReleaseDate());
            throw new ValidationException("Дата релиза должна быть позже 28.12.1895");
        }

        film.setId(getNextId());
        log.info("Создан новый фильм с id={}", film.getId());
        films.put(film.getId(), film);
        return film;
    }

    //обновление фильма
    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        log.info("Получен запрос PUT /films");
        if (film == null || film.getId() == null) {
            log.error("Не указан ID фильма");
            throw new ValidationException("ID фильма не был передан");
        }
        Film filmToUpdate = films.get(film.getId());
        if (filmToUpdate == null) {
            log.error("Фильм с id={} не найден", film.getId());
            throw new ValidationException("Фильм не найден");
        }
        log.info("Получен запрос на обновление фильма с id={}", film.getId());

        log.info("Новое имя фильма: {}", film.getName());
        filmToUpdate.setName(film.getName());

        log.info("Новое описание фильма: {}", film.getDescription());
        filmToUpdate.setDescription(film.getDescription());

        if (film.getReleaseDate() != null) {
            if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
                log.error("Дата релиза фильма была раньше дня рождения кино: {}", film.getReleaseDate());
                throw new ValidationException("Дата релиза должна быть позже 28.12.1895");
            }
            log.info("Новая дата релиза: {}", film.getReleaseDate());
            filmToUpdate.setReleaseDate(film.getReleaseDate());
        }

        log.info("Новая длина фильма: {}", film.getDuration());
        filmToUpdate.setDuration(film.getDuration());

        return filmToUpdate;
    }

    //получение всех фильмов
    @GetMapping
    public Collection<Film> getFilms() {
        log.info("Получен запрос на получение всех фильмов");
        Collection<Film> foundFilms =  films.values();
        log.info("Найдено фильмов: {}", foundFilms.size());
        return foundFilms;
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

}
