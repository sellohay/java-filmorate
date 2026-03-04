package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final Map<Long, User> users = new HashMap<>();

    //создание пользователя
    @PostMapping
    public User createUser(@Valid @RequestBody User user) {
        log.info("Получен запрос POST /users");
        if (user == null) {
            log.error("Был передан пустой пользователь");
            throw new ValidationException("Пользователь не должен быть пустым");
        }
        if (user.getLogin().contains(" ")) {
            log.error("Был передан некорректный логин: {}", user.getLogin());
            throw new ValidationException("Логин не может содержать пробелы");
        }
        if (user.getName() == null || user.getName().isEmpty()) {
            log.warn("Передано пустое имя - вместо него используется логин: {}", user.getLogin());
            user.setName(user.getLogin());
        }

        user.setId(getNextId());
        users.put(user.getId(), user);
        return user;
    }

    //обновление пользователя
    @PutMapping
    public User updateUser(@Valid @RequestBody User user) {
        log.info("Получен запрос PUT /users");
        if (user == null || user.getId() == null) {
            log.error("Не указан ID пользователя");
            throw new ValidationException("ID пользователя не был передан");
        }
        User userToUpdate = users.get(user.getId());
        if (userToUpdate == null) {
            log.error("Пользователь с id={} не найден", user.getId());
            throw new ValidationException("Пользователь не найден");
        }
        log.info("Получен запрос на обновление пользователя с id={}", user.getId());

        log.info("Новый email пользователя: {}", user.getEmail());
        userToUpdate.setEmail(user.getEmail());

        if (user.getLogin().contains(" ")) {
            log.error("Был передан некорректный логин: {}", user.getLogin());
            throw new ValidationException("Логин не может содержать пробелы");
        }

        log.info("Новый логин пользователя: {}", user.getLogin());
        userToUpdate.setLogin(user.getLogin());

        if (user.getName() != null && !user.getName().isEmpty()) {
            log.info("Новое имя пользователя: {}", user.getName());
            userToUpdate.setName(user.getName());
        }

        log.info("Новая дата рождения: {}", user.getBirthday());
        userToUpdate.setBirthday(user.getBirthday());

        return userToUpdate;
    }

    //получение списка пользователей
    @GetMapping
    public Collection<User> getUsers() {
        log.info("Получен запрос GET /users");
        Collection<User> foundUsers =  users.values();
        log.info("Найдено пользователей: {}", foundUsers.size());
        return foundUsers;
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

}
