-- Test fixture data for Karate API integration tests (H2)
INSERT INTO platforms (id, name) VALUES (1, 'PC');
INSERT INTO platforms (id, name) VALUES (2, 'PlayStation 5');
INSERT INTO platforms (id, name) VALUES (3, 'Nintendo Switch');

INSERT INTO games (id, title, cover_art_url, release_year) VALUES (1, 'Elden Ring', 'https://example.com/elden.jpg', 2022);
INSERT INTO games (id, title, cover_art_url, release_year) VALUES (2, 'Hades', 'https://example.com/hades.jpg', 2020);

INSERT INTO game_platforms (game_id, platform_id) VALUES (1, 1);
INSERT INTO game_platforms (game_id, platform_id) VALUES (1, 2);
INSERT INTO game_platforms (game_id, platform_id) VALUES (2, 1);
INSERT INTO game_platforms (game_id, platform_id) VALUES (2, 3);

