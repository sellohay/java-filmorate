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
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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

    @Test
    void testGetMpaByIds() {
        Set<Long> mpaIds = Set.of(1L, 2L, 3L);
        Map<Long, MpaRating> mpaMap = mpaRatingStorage.getMpaByIds(mpaIds);
        assertThat(mpaMap).hasSize(3);
        assertThat(mpaMap.get(1L).getName()).isEqualTo("G");
        assertThat(mpaMap.get(2L).getName()).isEqualTo("PG");
        assertThat(mpaMap.get(3L).getName()).isEqualTo("PG-13");
    }
}