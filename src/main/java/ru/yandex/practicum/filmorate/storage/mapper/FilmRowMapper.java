package ru.yandex.practicum.filmorate.storage.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class FilmRowMapper implements RowMapper<Film> {
    @Override
    public Film mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(resultSet.getLong("film_id"));
        film.setName(resultSet.getString("name"));
        film.setDescription(resultSet.getString("description"));
        film.setReleaseDate(resultSet.getDate("release_date").toLocalDate());
        film.setDuration(resultSet.getInt("duration"));
        Long mpaId = resultSet.getLong("mpa_rating_id");
        if (!resultSet.wasNull()) {
            MpaRating mpa = new MpaRating();
            mpa.setId(mpaId);
            try {
                mpa.setName(resultSet.getString("rating_name"));
            } catch (SQLException ignored) {
            }
            film.setMpa(mpa);
        }
        return film;
    }
}
