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

    private int currentMaxId = 0;
    private final Map<Long, User> users = new HashMap<>();

    //создание пользователя
    @PostMapping
    public User createUser(@Valid @RequestBody User user) {
        log.info("Получен запрос POST /users");
        validateCreateUser(user);
        return initializeUser(user);

    }

    private User initializeUser(User user) {
        if (user.getName() == null || user.getName().isEmpty()) {
            log.warn("Передано пустое имя - вместо него используется логин: {}", user.getLogin());
            user.setName(user.getLogin());
        }

        user.setId(getNextId());
        users.put(user.getId(), user);
        log.info("Создан пользователь с id={}", user.getId());
        return user;
    }

    private void validateCreateUser(User user) {
        if (user == null) {
            log.error("Был передан пустой пользователь");
            throw new ValidationException("Пользователь не должен быть пустым");
        }
        if (user.getLogin().contains(" ")) {
            log.error("Был передан некорректный логин: {}", user.getLogin());
            throw new ValidationException("Логин не может содержать пробелы");
        }
    }

    //обновление пользователя
    @PutMapping
    public User updateUser(@Valid @RequestBody User user) {
        log.info("Получен запрос PUT /users");

        validateUpdateUser(user);
        User userToUpdate = users.get(user.getId());

        log.info("Получен запрос на обновление пользователя с id={}", user.getId());
        return updateUserFields(user, userToUpdate);
    }

    private User updateUserFields(User user, User userToUpdate) {
        log.info("Новый email пользователя: {}", user.getEmail());
        userToUpdate.setEmail(user.getEmail());
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

    private void validateUpdateUser(User user) {
        if (user == null || user.getId() == null) {
            log.error("Не указан ID пользователя");
            throw new ValidationException("ID пользователя не был передан");
        }
        User userToUpdate = users.get(user.getId());
        if (userToUpdate == null) {
            log.error("Пользователь с id={} не найден", user.getId());
            throw new ValidationException("Пользователь не найден");
        }
        if (user.getLogin().contains(" ")) {
            log.error("Был передан некорректный логин: {}", user.getLogin());
            throw new ValidationException("Логин не может содержать пробелы");
        }
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
        return ++currentMaxId;
    }

}
