package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {

    private int currentMaxId = 0;
    private final Map<Long, User> users = new HashMap<>();

    @Override
    public Collection<User> getUsers() {
        Collection<User> foundUsers =  users.values();
        log.info("Найдено пользователей: {}", foundUsers.size());
        return foundUsers;
    }

    @Override
    public User createUser(User user) {
        if (user.getName() == null || user.getName().isEmpty()) {
            log.warn("Передано пустое имя - вместо него используется логин: {}", user.getLogin());
            user.setName(user.getLogin());
        }
        user.setId(getNextId());
        user.setFriends(new HashSet<>());
        users.put(user.getId(), user);
        log.info("Создан пользователь с id={}", user.getId());
        return user;
    }

    @Override
    public User updateUser(User user) {
        User userToUpdate = users.get(user.getId());
        log.info("Получен запрос на обновление пользователя с id={}", user.getId());
        return updateUserFields(user, userToUpdate);
    }

    @Override
    public Optional<User> getUserById(long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public void addFriends(User user, User friend) {
        user.getFriends().add(friend.getId());
        friend.getFriends().add(user.getId());
    }

    @Override
    public void removeFriends(User user, User friend) {
        user.getFriends().remove(friend.getId());
        friend.getFriends().remove(user.getId());
    }

    @Override
    public List<User> findFriendsCommon(User user1, User user2) {
        Set<Long> friendsUser2 = user2.getFriends();
        return user1.getFriends().stream()
                .filter(friendsUser2::contains)
                .map(users::get)
                .toList();
    }

    @Override
    public Collection<User> getFriends(User user) {
        return user.getFriends().stream()
                .map(users::get)
                .toList();
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

    private long getNextId() {
        return ++currentMaxId;
    }

}
