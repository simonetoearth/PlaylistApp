package edu.sdccd.cisc191;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class MusicPlayerApp extends Application {

    private ListView<Song> listView;

    @Override
    public void start(Stage primaryStage) {
        listView = new ListView<>();
        SongListController songListController = new SongListController(listView);

        // loading songs
        List<Song> songs = loadSongsFromDatabase(); // Replace with actual song loading logic
        songListController.updateUIWithSongs(songs);

        VBox root = new VBox(listView);
        Scene scene = new Scene(root, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Music Player");
        primaryStage.show();
    }

    private List<Song> loadSongsFromDatabase() {
        return new ArrayList<>();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
