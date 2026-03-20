-- Fixture for Selenium E2E backlog tests.
-- Assumes test users are seeded by TestUserSeeder (profile "test").

-- Ensure the seeded library game/platform pair used by BacklogE2ETest exists.
INSERT INTO platforms (id, name)
SELECT 1, 'PC'
WHERE NOT EXISTS (SELECT 1 FROM platforms WHERE id = 1);

INSERT INTO games (id, title, cover_art_url, release_year)
SELECT 1, 'Elden Ring', 'https://example.com/elden.jpg', 2022
WHERE NOT EXISTS (SELECT 1 FROM games WHERE id = 1);

INSERT INTO games (id, title, cover_art_url, release_year)
SELECT 2, 'Balatro', 'https://example.com/balatro.jpg', 2025
WHERE NOT EXISTS (SELECT 1 FROM games WHERE id = 2);

INSERT INTO game_platforms (game_id, platform_id)
SELECT 1, 1
WHERE NOT EXISTS (
	SELECT 1 FROM game_platforms WHERE game_id = 1 AND platform_id = 1
);

-- Start each test from a deterministic backlog state.
DELETE FROM user_games;

-- Seed exactly one backlog item for the regular USER account.
INSERT INTO user_games (user_id, game_id, platform_id, status)
SELECT id, 1, 1, 'WANT_TO_PLAY'
FROM users
WHERE role = 'USER'
ORDER BY id
LIMIT 1;


