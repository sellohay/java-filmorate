package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@TestPropertySource(locations = "classpath:application-test.properties")
@Sql(scripts = "/test-cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class UserControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    //тесты создания
    @DisplayName("Успешное создание пользователя")
    @Test
    public void testCreateUser_Valid() {
        User user = new User();
        user.setEmail("qwe123@mail.ru");
        user.setLogin("login");
        user.setName("Пользователь 1");
        user.setBirthday(LocalDate.of(1990, 12, 1));

        ResponseEntity<User> response = restTemplate.postForEntity("/users", user, User.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        User createdUser = response.getBody();
        assertNotNull(createdUser);
        assertNotNull(createdUser.getId());
        assertEquals("login", createdUser.getLogin());
    }

    @DisplayName("Ошибка создания пользователя с некорректным имейлом")
    @Test
    public void testCreateUser_IncorrectEmail() {
        User user = new User();
        user.setEmail("qwe123- @");
        user.setLogin("login");
        user.setName("Пользователь 1");
        user.setBirthday(LocalDate.of(1990, 12, 1));

        ResponseEntity<User> response = restTemplate.postForEntity("/users", user, User.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @DisplayName("Ошибка создания пользователя с некорректным логином")
    @Test
    public void testCreateUser_IncorrectLogin() {
        User user = new User();
        user.setEmail("qwe123@mail.ru");
        user.setLogin("login 123");
        user.setName("Пользователь 1");
        user.setBirthday(LocalDate.of(1990, 12, 1));

        ResponseEntity<User> response = restTemplate.postForEntity("/users", user, User.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @DisplayName("Успешное создание пользователя с пустым именем")
    @Test
    public void testCreateUser_EmptyName() {
        User user = new User();
        user.setEmail("qwe123@mail.ru");
        user.setLogin("user2");
        user.setBirthday(LocalDate.of(2010, 10, 10));

        ResponseEntity<User> response = restTemplate.postForEntity("/users", user, User.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        User createdUser = response.getBody();
        assertNotNull(createdUser);
        assertNotNull(createdUser.getId());
        assertEquals("user2", createdUser.getName());
    }

    @DisplayName("Ошибка создания пользователя с датой рождения в будущем")
    @Test
    public void testCreateUser_BirthdayInFuture() {
        User user = new User();
        user.setEmail("qwe123@mail.ru");
        user.setLogin("login 456");
        user.setName("Пользователь 3");
        user.setBirthday(LocalDate.of(2027, 12, 1));

        ResponseEntity<User> response = restTemplate.postForEntity("/users", user, User.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // получение всех фильмов
    @DisplayName("Успешное получение всех пользователей")
    @Test
    public void testGetUsers() {
        User user = new User();
        user.setEmail("qwe123@mail.ru");
        user.setLogin("login123");
        user.setName("Пользователь 1");
        user.setBirthday(LocalDate.of(1990, 12, 1));
        restTemplate.postForEntity("/users", user, User.class);

        user = new User();
        user.setEmail("qwe456@mail.ru");
        user.setLogin("login456");
        user.setName("Пользователь 2");
        user.setBirthday(LocalDate.of(2003, 3, 3));
        restTemplate.postForEntity("/users", user, User.class);

        user = new User();
        user.setEmail("qwe789@mail.ru");
        user.setLogin("login789");
        user.setName("Пользователь 3");
        user.setBirthday(LocalDate.of(1950, 5, 5));
        restTemplate.postForEntity("/users", user, User.class);

        ResponseEntity<List<User>> response = restTemplate.exchange(
                "/users",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        List<User> films = response.getBody();
        assertEquals(3, films.size());
    }

    @DisplayName("Успешное получение пустого списка пользователей")
    @Test
    public void testGetUser_Empty() {
        ResponseEntity<String> response = restTemplate.getForEntity("/users", String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        String json = response.getBody();
        assertNotNull(json);
        assertEquals("[]", json);
    }

    //обновление
    private void createUserForUpdate() {
        User user = new User();
        user.setEmail("qwe123@mail.ru");
        user.setLogin("login123");
        user.setName("Пользователь 1");
        user.setBirthday(LocalDate.of(1990, 12, 1));
        restTemplate.postForEntity("/users", user, User.class);
    }

    @DisplayName("Успешное обновление пользователя")
    @Test
    public void testUpdateUser_Valid() {
        createUserForUpdate();
        User user = new User();
        user.setId(1L);
        user.setEmail("qwe123@mail.ru");
        user.setLogin("new_login");
        user.setName("Пользователь 1");
        user.setBirthday(LocalDate.of(1990, 12, 1));

        ResponseEntity<User> response = restTemplate.exchange(
                "/users",
                HttpMethod.PUT,
                new HttpEntity<>(user),
                User.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        User updated = response.getBody();
        assertNotNull(updated);
        assertEquals(user.getId(), updated.getId());
        assertEquals("new_login", updated.getLogin());
    }

    @DisplayName("Ошибка обновления пользователя с некорректными данными")
    @Test
    public void testUpdateUser_FailUpdate() {
        createUserForUpdate();
        User user = new User();
        user.setId(1L);
        user.setEmail("qwe123@mail.ru");
        user.setLogin("new login");
        user.setName("Пользователь 1");
        user.setBirthday(LocalDate.of(1990, 12, 1));

        ResponseEntity<User> response = restTemplate.exchange(
                "/users",
                HttpMethod.PUT,
                new HttpEntity<>(user),
                User.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @DisplayName("Ошибка обновления пользователя с некорректным идентификатором")
    @Test
    public void testUpdateUser_IncorrectId() {
        createUserForUpdate();
        User user = new User();
        user.setId(10L);
        user.setEmail("qwe123@mail.ru");
        user.setLogin("new_login");
        user.setName("Пользователь 1");
        user.setBirthday(LocalDate.of(1990, 12, 1));

        ResponseEntity<User> response = restTemplate.exchange(
                "/users",
                HttpMethod.PUT,
                new HttpEntity<>(user),
                User.class
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
