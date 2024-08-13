CREATE TABLE songs (
                       id INTEGER PRIMARY KEY AUTOINCREMENT,
                       title TEXT,
                       artist TEXT,
                       album TEXT,
                       genre TEXT,
                       file_path TEXT
);

CREATE TABLE playlists (
                           id INTEGER PRIMARY KEY AUTOINCREMENT,
                           name TEXT
);

CREATE TABLE playlist_songs (
                                playlist_id INTEGER,
                                song_id INTEGER,
                                FOREIGN KEY (playlist_id) REFERENCES playlists(id),
                                FOREIGN KEY (song_id) REFERENCES songs(id)
);
