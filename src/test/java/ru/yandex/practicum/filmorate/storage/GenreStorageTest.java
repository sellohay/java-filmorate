package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.mapper.GenreRowMapper;

import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({GenreStorage.class, GenreRowMapper.class})
@TestPropertySource(locations = "classpath:application-test.properties")
class GenreStorageTest {

    private final GenreStorage genreStorage;

    @Test
    void testGetAllGenres() {
        Collection<Genre> genres = genreStorage.getAllGenres();
        assertThat(genres).hasSizeGreaterThanOrEqualTo(6);
    }

    @Test
    void testGetGenreById() {
        Optional<Genre> genreOptional = genreStorage.getGenreById(1L);
        assertThat(genreOptional)
                .isPresent()
                .hasValueSatisfying(genre -> {
                    assertThat(genre.getId()).isEqualTo(1L);
                    assertThat(genre.getName()).isEqualTo("Комедия");
                });
    }

    @Test
    void testGetGenreById_NotFound() {
        Optional<Genre> genreOptional = genreStorage.getGenreById(999L);
        assertThat(genreOptional).isEmpty();
    }

    @Test
    void testGetFilmGenres() {
        Collection<Genre> genres = genreStorage.getFilmGenres(1L);
        assertThat(genres).hasSize(2);
        assertThat(genres).extracting("id").containsExactlyInAnyOrder(1L, 2L);
    }
}