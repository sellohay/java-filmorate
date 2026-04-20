package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.mapper.FriendshipRowMapper;
import ru.yandex.practicum.filmorate.storage.mapper.UserRowMapper;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({UserDbStorage.class, UserRowMapper.class, FriendshipRowMapper.class})
@TestPropertySource(locations = "classpath:application-test.properties")
class UserDbStorageTest {

    private final UserDbStorage userStorage;

    @Test
    void testGetAllUsers() {
        Collection<User> users = userStorage.getUsers();
        assertThat(users).hasSize(3);
    }

    @Test
    void testGetUserById() {
        Optional<User> userOptional = userStorage.getUserById(1L);

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user -> {
                    assertThat(user).hasFieldOrPropertyWithValue("id", 1L);
                    assertThat(user.getEmail()).isEqualTo("user1@test.com");
                    assertThat(user.getLogin()).isEqualTo("user1");
                    assertThat(user.getName()).isEqualTo("Test User 1");
                    assertThat(user.getBirthday()).isEqualTo(LocalDate.of(1990, 1, 1));
                });
    }

    @Test
    void testGetUserById_NotFound() {
        Optional<User> userOptional = userStorage.getUserById(999L);

        assertThat(userOptional).isEmpty();
    }

    @Test
    void testCreateUser() {
        User newUser = new User();
        newUser.setEmail("new@test.com");
        newUser.setLogin("newuser");
        newUser.setName("New User");
        newUser.setBirthday(LocalDate.of(2000, 1, 1));

        User created = userStorage.createUser(newUser);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getEmail()).isEqualTo("new@test.com");
        assertThat(created.getLogin()).isEqualTo("newuser");
    }

    @Test
    void testCreateUser_WithoutName() {
        User newUser = new User();
        newUser.setEmail("noname@test.com");
        newUser.setLogin("noname");
        newUser.setBirthday(LocalDate.of(2000, 1, 1));

        User created = userStorage.createUser(newUser);

        assertThat(created.getName()).isEqualTo("noname");
    }

    @Test
    void testUpdateUser() {
        User user = userStorage.getUserById(1L).orElseThrow();
        user.setName("Updated Name");
        user.setEmail("updated@test.com");

        User updated = userStorage.updateUser(user);

        assertThat(updated.getName()).isEqualTo("Updated Name");
        assertThat(updated.getEmail()).isEqualTo("updated@test.com");
    }

    @Test
    void testAddFriends() {
        User user1 = userStorage.getUserById(1L).orElseThrow();
        User user3 = userStorage.getUserById(3L).orElseThrow();

        userStorage.addFriends(user1, user3);

        Collection<User> friends = userStorage.getFriends(user1);
        assertThat(friends).hasSize(2);
        assertThat(friends).extracting("id").contains(3L);
    }

    @Test
    void testGetFriends() {
        User user1 = userStorage.getUserById(1L).orElseThrow();

        Collection<User> friends = userStorage.getFriends(user1);

        assertThat(friends).hasSize(1);
        assertThat(friends).extracting("id").contains(2L);
    }

    @Test
    void testRemoveFriends() {
        User user1 = userStorage.getUserById(1L).orElseThrow();
        User user2 = userStorage.getUserById(2L).orElseThrow();

        userStorage.removeFriends(user1, user2);

        Collection<User> friends = userStorage.getFriends(user1);
        assertThat(friends).isEmpty();
    }

    @Test
    void testFindCommonFriends() {
        User user1 = userStorage.getUserById(1L).orElseThrow();
        User user2 = userStorage.getUserById(2L).orElseThrow();
        User user3 = userStorage.getUserById(3L).orElseThrow();

        userStorage.addFriends(user1, user3);

        Collection<User> commonFriends = userStorage.findFriendsCommon(user1, user2);

        assertThat(commonFriends).hasSize(1);
        assertThat(commonFriends).extracting("id").contains(3L);
    }
}