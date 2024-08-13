package edu.sdccd.cisc191;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;

import java.util.List;

public class SongListController {

    private ListView<Song> songListView;

    // Constructor or initialization method
    public SongListController(ListView<Song> listView) {
        this.songListView = listView;
    }

    // Method to update the UI with the list of songs
    public void updateUIWithSongs(List<Song> songs) {
        Platform.runLater(() -> {
            ObservableList<Song> observableSongs = FXCollections.observableArrayList(songs);
            songListView.setItems(observableSongs);
        });
    }
}
