package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class UserService {

    private final UserStorage userStorage;

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User createUser(User user) {
        validateCreateUser(user);
        return userStorage.createUser(user);
    }

    public User updateUser(User user) {
        validateUpdateUser(user);
        return userStorage.updateUser(user);
    }

    public Collection<User> getUsers() {
        return userStorage.getUsers();
    }

    public Optional<User> getUserById(long id) {
        return userStorage.getUserById(id);
    }

    public Collection<User> getFriends(long id) {
        User user = validateUser(id);
        return userStorage.getFriends(user);
    }

    public void addFriend(Long id1, Long id2) {
        User user1 = validateUser(id1);
        User user2 = validateUser(id2);

        if (user1.getFriends().contains(id2)) {
            log.warn("Эти пользователи уже друзья!");
            return;
        }

        userStorage.addFriends(user1, user2);
        log.info("Пользователи с id={} и id={} теперь друзья!", id1, id2);
    }

    public void deleteFriend(Long id1, Long id2) {
        User user1 = validateUser(id1);
        User user2 = validateUser(id2);

        if (!user1.getFriends().contains(id2)) {
            log.warn("Пользователи и так не друзья :(");
            return;
        }

        userStorage.removeFriends(user1, user2);
        log.info("Пользователи с id={} и id={} больше не друзья :(", id1, id2);
    }

    public List<User> findFriendsCommon(Long id1, Long id2) {
        User user1 = validateUser(id1);
        User user2 = validateUser(id2);

        return userStorage.findFriendsCommon(user1, user2);
    }

    public User validateUser(Long id) {
        return userStorage.getUserById(id)
                .orElseThrow(() ->
                        new NotFoundException("Пользователь id=" + id + " не найден")
                );
    }

    private void validateCreateUser(User user) {
        if (user == null) {
            log.error("Был передан пустой пользователь");
            throw new RuntimeException("Пользователь не должен быть пустым");
        }
        if (user.getLogin().contains(" ")) {
            log.error("Был передан некорректный логин: {}", user.getLogin());
            throw new ValidationException("Логин не может содержать пробелы");
        }
    }

    private void validateUpdateUser(User user) {
        if (user == null || user.getId() == null) {
            log.error("Не указан ID пользователя");
            throw new RuntimeException("ID пользователя не был передан");
        }
        Optional<User> userOpt = userStorage.getUserById(user.getId());
        if (userOpt.isEmpty()) {
            log.error("Пользователь с id={} не найден", user.getId());
            throw new NotFoundException("Пользователь не найден");
        }
        if (user.getLogin().contains(" ")) {
            log.error("Был передан некорректный логин: {}", user.getLogin());
            throw new ValidationException("Логин не может содержать пробелы");
        }
    }

}
