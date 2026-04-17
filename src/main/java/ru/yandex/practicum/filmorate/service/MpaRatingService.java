package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.MpaRatingStorage;

import java.util.Collection;
import java.util.Optional;

@Service
public class MpaRatingService {

    private final MpaRatingStorage storage;

    public MpaRatingService(MpaRatingStorage storage) {
        this.storage = storage;
    }

    public Collection<MpaRating> getRatings() {
        return storage.getAllRatings();
    }

    public MpaRating getRating(Long id) {
        Optional<MpaRating> ratingOpt = storage.getRatingById(id);
        if (ratingOpt.isEmpty()) {
            throw new NotFoundException("Рейтинг с id=" + id + " не найден");
        }
        return ratingOpt.get();
    }
}
