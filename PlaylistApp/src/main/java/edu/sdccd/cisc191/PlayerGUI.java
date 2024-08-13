package edu.sdccd.cisc191;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class PlayerGUI extends Application {

    private MusicPlayer musicPlayer;
    private FileChooser fileChooser;
    private Slider playbackSlider;
    private Button playButton, pauseButton;

    @Override
    public void start(Stage primaryStage) {
        // Initialize components
        musicPlayer = new MusicPlayer(this);
        fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(System.getProperty("user.dir"))); // Use current directory as fallback
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("MP3 Files", "*.mp3"));

        // Set up the main layout
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: black;");

        // Menu bar
        MenuBar menuBar = new MenuBar();
        Menu songMenu = new Menu("Song");
        MenuItem loadSong = new MenuItem("Load Song");
        loadSong.setOnAction(event -> loadSong());
        songMenu.getItems().add(loadSong);

        Menu playlistMenu = new Menu("Playlist");
        MenuItem createPlaylist = new MenuItem("Create Playlist");
        createPlaylist.setOnAction(event -> new PlaylistDialog(primaryStage));
        MenuItem loadPlaylist = new MenuItem("Load Playlist");
        loadPlaylist.setOnAction(event -> loadPlaylist());
        playlistMenu.getItems().addAll(createPlaylist, loadPlaylist);

        menuBar.getMenus().addAll(songMenu, playlistMenu);
        root.setTop(menuBar);

        // Playback Slider
        playbackSlider = new Slider();
        playbackSlider.setMaxWidth(600);
        playbackSlider.setStyle("-fx-background-color: gray;");

        VBox playbackContainer = new VBox(10);
        playbackContainer.getChildren().addAll(playbackSlider, createPlaybackControls());
        VBox.setVgrow(playbackSlider, Priority.ALWAYS);

        root.setBottom(playbackContainer);

        // Set up the scene
        Scene scene = new Scene(root, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Music Player");
        primaryStage.show();
    }

    private void loadSong() {
        fileChooser.setInitialDirectory(new File("src/main/java/edu/sdccd/cisc191/assets"));
        fileChooser.getExtensionFilters().clear();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("MP3 Files", "*.mp3"));

        File file = fileChooser.showOpenDialog(null); // Use primaryStage if needed
        if (file != null) {
            try {
                Song song = new Song(file.getPath());
                musicPlayer.loadSong(song);
                updatePlaybackSlider(song);
                enablePauseButtonDisablePlayButton();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void loadPlaylist() {
        fileChooser.setInitialDirectory(new File("src/main/java/edu/sdccd/cisc191/assets"));
        fileChooser.getExtensionFilters().clear();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Playlist Files", "*.txt"));

        File file = fileChooser.showOpenDialog(null); // Use primaryStage if needed
        if (file != null) {
            try {
                musicPlayer.stopSong();
                musicPlayer.loadPlaylist(file);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private HBox createPlaybackControls() {
        HBox playbackControls = new HBox();
        playbackControls.setAlignment(Pos.CENTER);
        playbackControls.setSpacing(10);
        playbackControls.setPadding(new Insets(10));

        Button prevButton = new Button();
        prevButton.setGraphic(new ImageView(new Image("file:src/main/java/edu/sdccd/cisc191/assets/previous.png")));
        prevButton.setOnAction(event -> musicPlayer.prevSong());

        playButton = new Button();
        playButton.setGraphic(new ImageView(new Image("file:src/main/java/edu/sdccd/cisc191/assets/play.png")));
        playButton.setOnAction(event -> {
            enablePauseButtonDisablePlayButton();
            musicPlayer.playCurrentSong();
        });

        pauseButton = new Button();
        pauseButton.setGraphic(new ImageView(new Image("file:src/main/java/edu/sdccd/cisc191/assets/pause.png")));
        pauseButton.setVisible(false);
        pauseButton.setOnAction(event -> {
            enablePlayButtonDisablePauseButton();
            musicPlayer.pauseSong();
        });

        Button nextButton = new Button();
        nextButton.setGraphic(new ImageView(new Image("file:src/main/java/edu/sdccd/cisc191/assets/next.png")));
        nextButton.setOnAction(event -> musicPlayer.nextSong());

        playbackControls.getChildren().addAll(prevButton, playButton, pauseButton, nextButton);
        return playbackControls;
    }

    public void setPlaybackSliderValue(int value) {
        playbackSlider.setValue(value);
    }

    public void updatePlaybackSlider(Song song) {
        playbackSlider.setMax(song.getMp3File().getFrameCount());

        // Creating a label table for slider
        Map<Double, String> labelTable = new HashMap<>();
        labelTable.put(0.0, "00:00");
        labelTable.put((double) song.getMp3File().getFrameCount(), song.getSongLength());

        // Set the label formatter
        playbackSlider.setLabelFormatter(new StringConverter<Double>() {
            @Override
            public String toString(Double value) {
                return labelTable.getOrDefault(value, "");
            }

            @Override
            public Double fromString(String string) {
                // Not needed in this context
                return null;
            }
        });
    }

    public void enablePauseButtonDisablePlayButton() {
        playButton.setVisible(false);
        playButton.setDisable(true);
        pauseButton.setVisible(true);
        pauseButton.setDisable(false);
    }

    public void enablePlayButtonDisablePauseButton() {
        playButton.setVisible(true);
        playButton.setDisable(false);
        pauseButton.setVisible(false);
        pauseButton.setDisable(true);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
