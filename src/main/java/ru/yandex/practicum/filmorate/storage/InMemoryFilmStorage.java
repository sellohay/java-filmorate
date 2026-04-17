package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;

@Slf4j
@Component("InMemoryFilmStorage")
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Long, Film> films = new HashMap<>();
    private int currentMaxId = 0;

    @Override
    public Collection<Film> getFilms() {
        Collection<Film> foundFilms =  films.values();
        log.info("Найдено фильмов: {}", foundFilms.size());
        return foundFilms;
    }

    @Override
    public Film createFilm(Film film) {
        film.setId(getNextId());
        //film.setLikedUsers(new HashSet<>());
        log.info("Создан новый фильм с id={}", film.getId());
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        Film filmToUpdate = films.get(film.getId());
        log.info("Получен запрос на обновление фильма с id={}", film.getId());
        return updateFilmFields(film, filmToUpdate);
    }

    @Override
    public Optional<Film> getFilmById(long id) {
        return Optional.ofNullable(films.get(id));
    }

    @Override
    public void addLike(Long filmId, Long userId) {

    }

    @Override
    public void removeLike(Long filmId, Long userId) {

    }

    @Override
    public List<Film> getPopularFilms(int count) {
        return List.of();
    }

    private long getNextId() {
        return ++currentMaxId;
    }

    private Film updateFilmFields(Film film, Film filmToUpdate) {
        log.info("Новое имя фильма: {}", film.getName());
        filmToUpdate.setName(film.getName());
        log.info("Новое описание фильма: {}", film.getDescription());
        filmToUpdate.setDescription(film.getDescription());
        log.info("Новая дата релиза: {}", film.getReleaseDate());
        filmToUpdate.setReleaseDate(film.getReleaseDate());
        log.info("Новая длина фильма: {}", film.getDuration());
        filmToUpdate.setDuration(film.getDuration());
        return filmToUpdate;
    }

}
