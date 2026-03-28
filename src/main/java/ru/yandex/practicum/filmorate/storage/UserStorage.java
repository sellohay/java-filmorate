package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserStorage {
    Collection<User> getUsers();

    User createUser(User user);

    User updateUser(User user);

    Optional<User> getUserById(long id);

    void addFriends(User user, User friend);

    void removeFriends(User user, User friend);

    List<User> findFriendsCommon(User user1, User user2);

    Collection<User> getFriends(User user);
}
