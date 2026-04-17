DELETE FROM film_genres;
DELETE FROM film_likes;
DELETE FROM friendships;
DELETE FROM films;
DELETE FROM users;

INSERT INTO users (user_email, user_login, user_name, birthday) VALUES
    ('user1@test.com', 'user1', 'Test User 1', '1990-01-01'),
    ('user2@test.com', 'user2', 'Test User 2', '1995-05-15'),
    ('user3@test.com', 'user3', 'Test User 3', '2000-12-31');

INSERT INTO films (name, description, release_date, duration, mpa) VALUES
    ('Test Film 1', 'Description 1', '2020-01-01', 120, 1),
    ('Test Film 2', 'Description 2', '2021-06-15', 90, 2),
    ('Test Film 3', 'Description 3', '2022-12-31', 150, 3);

INSERT INTO film_genres (film_id, genre_id) VALUES
     (1, 1),
     (1, 2),
     (2, 3),
     (3, 4);

INSERT INTO film_likes (film_id, user_id, liked_at) VALUES
     (1, 1, '2023-01-01'),
     (1, 2, '2023-01-02'),
     (2, 1, '2023-01-03');

INSERT INTO friendships (user_id, friend_id, status_id) VALUES
     (1, 2, 1),
    (2, 3, 2);