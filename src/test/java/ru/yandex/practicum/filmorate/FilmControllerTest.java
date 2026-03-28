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
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class FilmControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    //тесты создания
    @DisplayName("Успешное создание фильма")
    @Test
    public void testCreateFilm_Valid() {
        Film film = new Film();
        film.setName("Фильм 1");
        film.setDescription("Описание 1");
        film.setReleaseDate(LocalDate.of(2006, 6, 2));
        film.setDuration(115);

        ResponseEntity<Film> response = restTemplate.postForEntity("/films", film, Film.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        Film createdFilm = response.getBody();
        assertNotNull(createdFilm);
        assertNotNull(createdFilm.getId());
        assertEquals("Фильм 1", createdFilm.getName());
    }

    @DisplayName("Ошибка создания фильма с пустым именем")
    @Test
    public void testCreateFilm_EmptyName() {
        Film film = new Film();
        film.setName("");
        film.setDescription("Описание 1");
        film.setReleaseDate(LocalDate.of(2006, 6, 2));
        film.setDuration(115);

        ResponseEntity<Film> response = restTemplate.postForEntity("/films", film, Film.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @DisplayName("Ошибка создания фильма с длинным описанием")
    @Test
    public void testCreateFilm_DescLooLong() {
        Film film = new Film();
        film.setName("Фильм 2");
        film.setDescription("Описание 2222222222222222222222222222222222222222222222222222222222222222222222" +
                "222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222" +
                "2222222222222222222222222222222222222222222222222222222222222222222222222222");
        film.setReleaseDate(LocalDate.of(2006, 6, 2));
        film.setDuration(115);

        ResponseEntity<Film> response = restTemplate.postForEntity("/films", film, Film.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @DisplayName("Ошибка создания фильма с датой раньше заданной (28.12.1895)")
    @Test
    public void testCreateFilm_ReleaseDateTooEarly() {
        Film film = new Film();
        film.setName("Фильм 3");
        film.setDescription("Описание 3");
        film.setReleaseDate(LocalDate.of(1895, 12, 27));
        film.setDuration(115);

        ResponseEntity<Film> response = restTemplate.postForEntity("/films", film, Film.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @DisplayName("Ошибка создания фильма с отрицательной длиной")
    @Test
    public void testCreateFilm_NegativeDuration() {
        Film film = new Film();
        film.setName("Фильм 4");
        film.setDescription("Описание 4");
        film.setReleaseDate(LocalDate.of(1895, 12, 28));
        film.setDuration(-115);

        ResponseEntity<Film> response = restTemplate.postForEntity("/films", film, Film.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // получение всех фильмов
    @DisplayName("Успешное получение всех фильмов")
    @Test
    public void testGetFilms() {
        Film film = new Film();
        film.setName("Фильм 1");
        film.setDescription("Описание 1");
        film.setReleaseDate(LocalDate.of(2006, 6, 2));
        film.setDuration(115);
        restTemplate.postForEntity("/films", film, Film.class);

        film = new Film();
        film.setName("Фильм 2");
        film.setDescription("Описание 2");
        film.setReleaseDate(LocalDate.of(1999, 7, 25));
        film.setDuration(87);
        restTemplate.postForEntity("/films", film, Film.class);

        film = new Film();
        film.setName("Фильм 3");
        film.setDescription("Описание 3");
        film.setReleaseDate(LocalDate.of(1956, 2, 13));
        film.setDuration(94);
        restTemplate.postForEntity("/films", film, Film.class);

        ResponseEntity<List<Film>> response = restTemplate.exchange(
                "/films",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        List<Film> films = response.getBody();
        assertEquals(3, films.size());
    }

    @DisplayName("Успешное получение пустого списка фильмов")
    @Test
    public void testGetFilm_Empty() {
        ResponseEntity<String> response = restTemplate.getForEntity("/films", String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        String json = response.getBody();
        assertNotNull(json);
        assertEquals("[]", json);
    }

    //обновление
    private void createFilmForUpdate() {
        Film film = new Film();
        film.setName("Фильм 1");
        film.setDescription("Описание 1");
        film.setReleaseDate(LocalDate.of(2000, 12, 12));
        film.setDuration(115);

        restTemplate.postForEntity("/films", film, Film.class);
    }

    @DisplayName("Успешное обновление фильма")
    @Test
    public void testUpdateFilm_Valid() {
        createFilmForUpdate();
        Film film = new Film();
        film.setId(1L);
        film.setName("Фильм 1");
        film.setDescription("Описание 1");
        LocalDate newDate = LocalDate.of(2000, 11, 11);
        film.setReleaseDate(newDate); //меняем дату
        film.setDuration(115);

        ResponseEntity<Film> response = restTemplate.exchange(
                "/films",
                HttpMethod.PUT,
                new HttpEntity<>(film),
                Film.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Film updated = response.getBody();
        assertNotNull(updated);
        assertEquals(film.getId(), updated.getId());
        assertEquals(newDate, updated.getReleaseDate());
    }

    @DisplayName("Ошибка обновления фильма с некорректными данными")
    @Test
    public void testUpdateFilm_FailUpdate() {
        createFilmForUpdate();
        Film film = new Film();
        film.setId(1L);
        film.setName("Фильм 1");
        film.setDescription("Описание 1");
        film.setReleaseDate(LocalDate.of(2000, 12, 12));
        film.setDuration(-115);

        ResponseEntity<Film> response = restTemplate.exchange(
                "/films",
                HttpMethod.PUT,
                new HttpEntity<>(film),
                Film.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @DisplayName("Ошибка обновления фильма с некорректным идентификатором")
    @Test
    public void testUpdateFilm_IncorrectId() {
        createFilmForUpdate();
        Film film = new Film();
        film.setId(2L);
        film.setName("Фильм 1");
        film.setDescription("Описание 1");
        film.setReleaseDate(LocalDate.of(2000, 12, 12));
        film.setDuration(120);

        ResponseEntity<Film> response = restTemplate.exchange(
                "/films",
                HttpMethod.PUT,
                new HttpEntity<>(film),
                Film.class
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
