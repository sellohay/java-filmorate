package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreStorage;

import java.util.*;

@Service
public class GenreService {

    private final GenreStorage genreStorage;

    public GenreService(GenreStorage genreStorage) {
        this.genreStorage = genreStorage;
    }

    public Collection<Genre> getGenres() {
        return genreStorage.getAllGenres();
    }

    public Genre getGenreById(Long id) {
        Optional<Genre> genreOpt = genreStorage.getGenreById(id);
        if (genreOpt.isEmpty()) {
            throw new NotFoundException("Жанр с id=" + id + " не найден");
        }
        return genreOpt.get();
    }

    public List<Genre> getFilmGenres(Long filmId) {
        return genreStorage.getFilmGenres(filmId);
    }

    public Map<Long, List<Genre>> getGenresForFilms(Set<Long> filmIds) {
        return genreStorage.getGenresForFilms(filmIds);
    }

    public void validateGenres(Set<Long> genreIds) {
        if (genreIds.isEmpty()) {
            return;
        }
        Set<Long> existingIds = genreStorage.countGenreIds(genreIds);
        if (existingIds.size() != genreIds.size()) {
            Set<Long> errorIds = new HashSet<>(genreIds);
            errorIds.removeAll(existingIds);
            throw new NotFoundException(String.format("Жанр с ID %s не найден", errorIds));
        }
    }
}
