package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class Film {

    private Long id;
    @NotBlank(message = "Название не должно быть пустым")
    private String name;

    @NotBlank
    @Size(max = 200, message = "Описание не должно быть длиннее 200 символов")
    private String description;

    @NotNull(message = "Дата выпуска обязательна")
    private LocalDate releaseDate;

    @Positive(message = "Длина фильма должна быть положительным числом")
    private int duration;

    @JsonProperty("mpa")
    @NotNull(message = "Рейтинг MPA обязателен")
    private MpaRating mpa;

    @JsonProperty("genres")
    private List<Genre> genres;

}
