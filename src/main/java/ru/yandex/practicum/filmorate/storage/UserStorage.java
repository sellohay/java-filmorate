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

    void addFriends(long userId, long friendId);

    void removeFriends(long userId, long friendId);

    List<User> findFriendsCommon(Long id1, Long id2);

    Collection<User> getFriends(long id);
}
