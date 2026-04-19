package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.service.GenreService;
import ru.yandex.practicum.filmorate.service.MpaRatingService;
import ru.yandex.practicum.filmorate.storage.mapper.FilmLikeRowMapper;
import ru.yandex.practicum.filmorate.storage.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.mapper.GenreRowMapper;
import ru.yandex.practicum.filmorate.storage.mapper.MpaRatingRowMapper;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({
        FilmDbStorage.class,
        FilmRowMapper.class,
        FilmLikeRowMapper.class,
        GenreStorage.class,
        GenreRowMapper.class,
        GenreService.class,
        MpaRatingStorage.class,
        MpaRatingRowMapper.class,
        MpaRatingService.class
})
@TestPropertySource(locations = "classpath:application-test.properties")
class FilmDbStorageTest {

    private final FilmDbStorage filmStorage;

    @Test
    void testGetAllFilms() {
        Collection<Film> films = filmStorage.getFilms();
        assertThat(films).hasSize(3);
    }

    @Test
    void testGetFilmById() {
        Optional<Film> filmOptional = filmStorage.getFilmById(1L);

        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(film -> {
                    assertThat(film.getId()).isEqualTo(1L);
                    assertThat(film.getName()).isEqualTo("Test Film 1");
                    assertThat(film.getDescription()).isEqualTo("Description 1");
                    assertThat(film.getReleaseDate()).isEqualTo(LocalDate.of(2020, 1, 1));
                    assertThat(film.getDuration()).isEqualTo(120);

                    assertThat(film.getMpa()).isNotNull();
                    assertThat(film.getMpa().getId()).isEqualTo(1L);
                    assertThat(film.getMpa().getName()).isEqualTo("G");
                });
    }

    @Test
    void testGetFilmById_NotFound() {
        Optional<Film> filmOptional = filmStorage.getFilmById(999L);
        assertThat(filmOptional).isEmpty();
    }

    @Test
    void testCreateFilm() {
        Film newFilm = new Film();
        newFilm.setName("New Film");
        newFilm.setDescription("New Description");
        newFilm.setReleaseDate(LocalDate.of(2023, 1, 1));
        newFilm.setDuration(100);

        MpaRating mpa = new MpaRating();
        mpa.setId(1L);
        newFilm.setMpa(mpa);

        Genre genre1 = new Genre();
        genre1.setId(1L);
        Genre genre2 = new Genre();
        genre2.setId(2L);
        newFilm.setGenres(List.of(genre1, genre2));

        Film created = filmStorage.createFilm(newFilm);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getName()).isEqualTo("New Film");
    }

    @Test
    void testUpdateFilm() {
        Film film = filmStorage.getFilmById(1L).orElseThrow();
        film.setName("Updated Film");
        film.setDuration(999);
        Film updated = filmStorage.updateFilm(film);
        assertThat(updated.getName()).isEqualTo("Updated Film");
        assertThat(updated.getDuration()).isEqualTo(999);
    }

    @Test
    void testAddLike() {
        filmStorage.addLike(3L, 1L);
        List<Film> popular = filmStorage.getPopularFilms(10);
        assertThat(popular.get(0).getId()).isEqualTo(1L);
        assertThat(popular.get(1).getId()).isIn(2L, 3L);
    }

    @Test
    void testRemoveLike() {
        filmStorage.removeLike(1L, 1L);
        List<Film> popular = filmStorage.getPopularFilms(10);
        assertThat(popular.getFirst().getId()).isIn(1L, 2L);
    }

    @Test
    void testGetPopularFilms() {
        List<Film> popular = filmStorage.getPopularFilms(2);
        assertThat(popular).hasSize(2);
        assertThat(popular.getFirst().getId()).isEqualTo(1L);
    }

    @Test
    void testGetPopularFilms_EmptyResult() {
        filmStorage.removeLike(1L, 1L);
        filmStorage.removeLike(1L, 2L);
        filmStorage.removeLike(2L, 1L);
        List<Film> popular = filmStorage.getPopularFilms(10);
        assertThat(popular).hasSize(3);
    }
}