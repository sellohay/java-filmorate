package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.mapper.MpaRatingRowMapper;

import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({MpaRatingStorage.class, MpaRatingRowMapper.class})
@TestPropertySource(locations = "classpath:application-test.properties")
class MpaRatingStorageTest {

    private final MpaRatingStorage mpaRatingStorage;

    @Test
    void testGetAllRatings() {
        Collection<MpaRating> ratings = mpaRatingStorage.getAllRatings();
        assertThat(ratings).hasSize(5);
    }

    @Test
    void testGetRatingById() {
        Optional<MpaRating> ratingOptional = mpaRatingStorage.getRatingById(1L);
        assertThat(ratingOptional)
                .isPresent()
                .hasValueSatisfying(rating -> {
                    assertThat(rating.getId()).isEqualTo(1L);
                    assertThat(rating.getName()).isEqualTo("G");
                });
    }

    @Test
    void testGetRatingById_NotFound() {
        Optional<MpaRating> ratingOptional = mpaRatingStorage.getRatingById(999L);
        assertThat(ratingOptional).isEmpty();
    }
}