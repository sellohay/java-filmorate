package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Data
public class User {

    private Long id;

    @NotBlank(message = "Электронная почта обязательна")
    @Email(message = "Почта должна иметь корректный формат")
    private String email;

    @NotBlank(message = "Логин обязателен")
    private String login;

    private String name;

    @NotNull(message = "Дата рождения обязательна")
    @PastOrPresent(message = "Дата рождения должна быть в прошлом")
    private LocalDate birthday;

    private Set<Long> friends;

}
