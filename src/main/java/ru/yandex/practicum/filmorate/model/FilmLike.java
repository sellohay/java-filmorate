package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import java.time.LocalDate;

@Data
public class FilmLike {

    private Long id;
    private Long filmId;
    private Long userId;
    private LocalDate likedAt;
}
