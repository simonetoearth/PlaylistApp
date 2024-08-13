package edu.sdccd.cisc191;

import javafx.application.Platform;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;

import java.io.*;
import java.util.ArrayList;

public class MusicPlayer extends PlaybackListener {
    private final DatabaseManager dbManager;
    private static final Object playSignal = new Object();
    private PlayerGUI musicPlayerGUI = null;

    private long currentPlaybackPosition = 0; // Playback position in milliseconds
    private Song currentSong;
    private ArrayList<Song> playlist;
    private int currentPlaylistIndex;
    private AdvancedPlayer advancedPlayer;
    private BufferedInputStream bufferedInputStream; // Stream for playing the song
    private boolean isPaused;
    private boolean songFinished;
    private boolean pressedNext, pressedPrev;
    private int currentFrame;
    private int currentTimeInMilli;

    public MusicPlayer(PlayerGUI musicPlayerGUI) {
        this.musicPlayerGUI = musicPlayerGUI;
        dbManager = new DatabaseManager();
        initializeDatabase();
    }


    void initializeDatabase() {
        dbManager.executeScript("db/schema.sql");
    }

    public void loadSong(Song song) {
        if (song == null || song.getFilePath() == null || song.getFilePath().isEmpty()) {
            System.err.println("Invalid song file path.");
            return;
        }

        currentSong = song;
        playlist = null;
        currentPlaybackPosition = 0; // Reset playback position for the new song

        stopSong(); // Ensure any previous song is stopped

        if (currentSong != null) {
            currentFrame = 0;
            currentTimeInMilli = 0;
            musicPlayerGUI.setPlaybackSliderValue(0);
            playCurrentSong();
        }
    }

    public void loadPlaylist(File playlistFile) {
        if (playlistFile == null || !playlistFile.exists()) {
            System.err.println("Playlist file does not exist.");
            return;
        }

        playlist = new ArrayList<>();

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(playlistFile))) {
            String songPath;
            while ((songPath = bufferedReader.readLine()) != null) {
                Song song = new Song(songPath);
                playlist.add(song);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!playlist.isEmpty()) {
            currentPlaylistIndex = 0;
            currentSong = playlist.get(currentPlaylistIndex);
            currentFrame = 0;
            currentTimeInMilli = 0;
            musicPlayerGUI.setPlaybackSliderValue(0);
            musicPlayerGUI.enablePauseButtonDisablePlayButton();
            musicPlayerGUI.updatePlaybackSlider(currentSong);
            playCurrentSong();
        }
    }

    public void pauseSong() {
        if (advancedPlayer != null) {
            currentPlaybackPosition = getCurrentPlaybackPosition(); // save current playback position
            stopSong(); // stop playback
            isPaused = true;
        }
    }

    public void stopSong() {
        if (advancedPlayer != null) {
            advancedPlayer.close();
            advancedPlayer = null;
        }
        if (bufferedInputStream != null) {
            try {
                bufferedInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            bufferedInputStream = null;
        }
    }


    public void nextSong() {
        if (playlist == null || currentPlaylistIndex + 1 >= playlist.size()) return;

        pressedNext = true;
        stopSong(); // Stop the current song if needed

        currentPlaylistIndex++;
        currentSong = playlist.get(currentPlaylistIndex);
        currentFrame = 0;
        currentTimeInMilli = 0;
        musicPlayerGUI.enablePauseButtonDisablePlayButton();
        musicPlayerGUI.updatePlaybackSlider(currentSong);
        playCurrentSong();
    }

    public void prevSong() {
        if (playlist == null || currentPlaylistIndex - 1 < 0) return;

        pressedPrev = true;
        stopSong(); // Stop the current song if needed

        currentPlaylistIndex--;
        currentSong = playlist.get(currentPlaylistIndex);
        currentFrame = 0;
        currentTimeInMilli = 0;
        musicPlayerGUI.enablePauseButtonDisablePlayButton();
        musicPlayerGUI.updatePlaybackSlider(currentSong);
        playCurrentSong();
    }

    public void playCurrentSong() {
        if (currentSong == null || currentSong.getFilePath() == null || currentSong.getFilePath().isEmpty()) {
            System.err.println("No valid song to play.");
            return;
        }

        System.out.println("Playing song: " + currentSong.getFilePath());

        try {
            if (bufferedInputStream != null) {
                bufferedInputStream.close();
            }
            bufferedInputStream = new BufferedInputStream(new FileInputStream(currentSong.getFilePath()));

            bufferedInputStream.skip(currentPlaybackPosition);

            advancedPlayer = new AdvancedPlayer(bufferedInputStream);
            advancedPlayer.setPlayBackListener(this);

            startMusicThread();
            startPlaybackSliderThread();

            isPaused = false;
        } catch (IOException | JavaLayerException e) {
            e.printStackTrace();
        }
    }


    private void startMusicThread() {
        new Thread(() -> {
            try {
                System.out.println("Music thread started.");
                if (isPaused) {
                    synchronized (playSignal) {
                        playSignal.wait();
                    }
                }
                advancedPlayer.play(currentFrame, Integer.MAX_VALUE);
            } catch (JavaLayerException | InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void startPlaybackSliderThread() {
        new Thread(() -> {
            while (!isPaused && !songFinished && !pressedNext && !pressedPrev) {
                try {
                    currentTimeInMilli++;
                    int calculatedFrame = (int) ((double) currentTimeInMilli * currentSong.getFrameRatePerMilliseconds());
                    Platform.runLater(() -> musicPlayerGUI.setPlaybackSliderValue(calculatedFrame));
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    @Override
    public void playbackStarted(PlaybackEvent evt) {
        System.out.println("Playback Started");
        songFinished = false;
        pressedNext = false;
        pressedPrev = false;
    }

    @Override
    public void playbackFinished(PlaybackEvent evt) {
        System.out.println("Playback Finished");
        if (isPaused) {
            currentFrame += (int) ((double) evt.getFrame() * currentSong.getFrameRatePerMilliseconds());
        } else {
            if (pressedNext || pressedPrev) return;

            songFinished = true;
            if (playlist == null) {
                musicPlayerGUI.enablePlayButtonDisablePauseButton();
            } else {
                if (currentPlaylistIndex == playlist.size() - 1) {
                    musicPlayerGUI.enablePlayButtonDisablePauseButton();
                } else {
                    nextSong();
                }
            }
        }
    }


    public void setCurrentTimeInMilli(int i) {
        if (advancedPlayer == null || currentSong == null) return;

        stopSong(); // stop the current song if it's playing

        currentPlaybackPosition = i; // update playback position
        currentFrame = (int) (i / (currentSong.getFrameRatePerMilliseconds() / 1000.0));
        currentTimeInMilli = i;

        playCurrentSong();
    }

    public void setCurrentFrame(int frame) {
        if (advancedPlayer == null) return;

        stopSong();

        currentFrame = frame;
        currentPlaybackPosition = (long) (frame * (1000.0 / currentSong.getFrameRatePerMilliseconds()));

        playCurrentSong();
    }

    public boolean isPlaying() {
        return advancedPlayer != null && !isPaused && !songFinished;
    }

    private long getCurrentPlaybackPosition() {
        return currentPlaybackPosition;
    }

    public void close() {
        dbManager.closeConnection();
    }
}
