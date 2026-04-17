package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreStorage;

import java.util.Collection;
import java.util.Optional;

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

}
