package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private final FilmService filmService;

    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    //добавление фильма
    @PostMapping
    public Film createFilm(@Valid @RequestBody Film film) {
        log.info("Получен запрос POST /films");
        return filmService.createFilm(film);
    }

    //обновление фильма
    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        log.info("Получен запрос PUT /films");
        return filmService.updateFilm(film);
    }

    //получение всех фильмов
    @GetMapping
    public Collection<Film> getFilms() {
        log.info("Получен запрос на получение всех фильмов");
        return filmService.getFilms();
    }

    @GetMapping("/{id}")
    public Optional<Film> getUserById(@PathVariable long id) {
        return filmService.getFilmById(id);
    }

    @PutMapping("/{id}/like/{userId}")
    public void likeFilm(@PathVariable long id, @PathVariable long userId) {
        filmService.addLike(userId, id);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable long id, @PathVariable long userId) {
        filmService.removeLike(userId, id);
    }

    @GetMapping("/popular")
    public Collection<Film> getPopularFilms(@RequestParam Optional<Integer> count) {
        return filmService.getPopularFilms(count);
    }

}
