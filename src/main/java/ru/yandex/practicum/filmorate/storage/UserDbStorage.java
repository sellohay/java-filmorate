package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.mapper.FriendshipRowMapper;
import ru.yandex.practicum.filmorate.storage.mapper.UserRowMapper;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component("UserDbStorage")
public class UserDbStorage implements UserStorage {

    private static final int STATUS_PENDING = 1;
    private static final int STATUS_CONFIRMED = 2;

    private static final String SELECT_ALL_USERS = "SELECT * FROM users;";
    private static final String INSERT_QUERY = "INSERT INTO users(user_email, user_login, user_name, birthday) " +
            "VALUES (?, ?, ?, ?);";
    private static final String UPDATE_USER = "UPDATE users SET user_email = ?, user_login = ?, " +
            "user_name = ?, birthday = ? WHERE user_id = ?;";
    private static final String GET_USER_BY_ID = "SELECT * FROM users WHERE user_id = ?;";
    private static final String INSERT_FRIENDSHIP_QUERY = "INSERT INTO friendships(user_id, friend_id, status_id) " +
            "VALUES (?, ?, ?);";
    private static final String UPDATE_FRIENDSHIP_QUERY = "UPDATE friendships SET status_id = ? WHERE friendship_id = ?;";
    private static final String CHECK_FRIENDSHIP_ONE_WAY = "SELECT * FROM friendships WHERE user_id = ? AND friend_id = ?";
    private static final String DELETE_FRIENDSHIP_QUERY = "DELETE FROM friendships WHERE friendship_id = ?;";
    private static final String GET_USER_FRIENDS = "SELECT u.* FROM users u " +
            "JOIN friendships f ON u.user_id = f.friend_id WHERE f.user_id = ?";

    private final JdbcTemplate jdbcTemplate;
    private final UserRowMapper userRowMapper;
    private final FriendshipRowMapper friendshipRowMapper;

    public UserDbStorage(JdbcTemplate jdbcTemplate, UserRowMapper userRowMapper, FriendshipRowMapper friendshipRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.userRowMapper = userRowMapper;
        this.friendshipRowMapper = friendshipRowMapper;
    }

    @Override
    public Collection<User> getUsers() {
        return jdbcTemplate.query(SELECT_ALL_USERS, userRowMapper);
    }

    @Override
    public User createUser(User user) {
        if (user.getName() == null || user.getName().isEmpty()) {
            log.info("Не было передано имя, будет использоваться логин");
            user.setName(user.getLogin());
        }
        Long id = insertTemplate(
                INSERT_QUERY,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                Date.valueOf(user.getBirthday())
        );
        user.setId(id);
        log.info("Создан новый пользователь с id={}", id);
        return user;
    }

    @Override
    public User updateUser(User user) {
        jdbcTemplate.update(UPDATE_USER,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                Date.valueOf(user.getBirthday()),
                user.getId());
        return user;
    }

    @Override
    public Optional<User> getUserById(long id) {
        try {
            User result = jdbcTemplate.queryForObject(GET_USER_BY_ID, userRowMapper, id);
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }

    @Override
    public void addFriends(User user, User friend) {
        Long userId = user.getId();
        Long friendId = friend.getId();
        try {
            //проверяем, существует ли уже дружба
            Friendship existingFriendship = jdbcTemplate.queryForObject(
                    CHECK_FRIENDSHIP_ONE_WAY, friendshipRowMapper, userId,friendId);
            log.warn("Пользователь id={} уже добавил пользователя id={} в друзья!", userId, friendId);
        } catch (EmptyResultDataAccessException ignored) {
            //проверяем обратную дружбу
            checkReverseFriendship(userId, friendId);
        }
    }

    @Override
    public void removeFriends(User user, User friend) {
        Long userId = user.getId();
        Long friendId = friend.getId();
        try {
            // Удаляем дружбу от user к friend
            Friendship friendship = jdbcTemplate.queryForObject(CHECK_FRIENDSHIP_ONE_WAY,
                    friendshipRowMapper,
                    userId,
                    friendId
            );
            jdbcTemplate.update(DELETE_FRIENDSHIP_QUERY, friendship.getId());
            log.info("Пользователь id={} удалил пользователя id={} из друзей", userId, friendId);

            turnToPendingFriendship(userId, friendId);
        } catch (EmptyResultDataAccessException e) {
            log.warn("Пользователь id={} не добавлял пользователя id={} в друзья", userId, friendId);
        }
    }

    @Override
    public List<User> findFriendsCommon(User user1, User user2) {
        Collection<User> frUser1 = getFriends(user1);
        Collection<User> frUser2 = getFriends(user2);
        return frUser1.stream()
                .filter(frUser2::contains)
                .toList();
    }

    @Override
    public Collection<User> getFriends(User user) {
        return jdbcTemplate.query(GET_USER_FRIENDS, userRowMapper, user.getId());
    }

    private Long insertTemplate(String query, Object... params) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            return ps;
        }, keyHolder);
        return keyHolder.getKeyAs(Long.class);
    }


    private void checkReverseFriendship(Long userId, Long friendId) {
        try {
            Friendship reverseFriendship = jdbcTemplate.queryForObject(
                    CHECK_FRIENDSHIP_ONE_WAY, friendshipRowMapper, friendId, userId);
            //если есть, то подтверждаем
            if (reverseFriendship.getStatusId() == STATUS_PENDING) {
                jdbcTemplate.update(UPDATE_FRIENDSHIP_QUERY, STATUS_CONFIRMED, reverseFriendship.getId());
                log.info("Пользователь id={} подтвердил дружбу с пользователем id={}!", userId, friendId);
            }
        } catch (EmptyResultDataAccessException ex) {
            //если нет с другой стороны, то создаем заявку
            insertTemplate(INSERT_FRIENDSHIP_QUERY,
                    userId,
                    friendId,
                    STATUS_PENDING
            );
            log.info("Пользователь id={} добавил пользователя id={} в друзья!", userId, friendId);
        }
    }


    private void turnToPendingFriendship(Long userId, Long friendId) {
        // Проверяем обратную дружбу и меняем её статус на PENDING, если она была CONFIRMED
        try {
            Friendship reverseFriendship = jdbcTemplate.queryForObject(
                    CHECK_FRIENDSHIP_ONE_WAY,
                    friendshipRowMapper,
                    friendId,
                    userId
            );
            if (reverseFriendship.getStatusId() == STATUS_CONFIRMED) {
                jdbcTemplate.update(UPDATE_FRIENDSHIP_QUERY, STATUS_PENDING, reverseFriendship.getId());
                log.info("Статус дружбы пользователя id={} с пользователем id={} изменен на PENDING",
                        friendId, userId);
            }
        } catch (EmptyResultDataAccessException e) {
            //обратной дружбы нет
        }
    }
}
