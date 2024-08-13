package edu.sdccd.cisc191;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;

public class PlaylistDialog {

    private final ArrayList<String> songPaths;
    private final Stage primaryStage;

    public PlaylistDialog(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.songPaths = new ArrayList<>();
        show();
    }

    private void show() {
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Create Playlist");
        dialogStage.initOwner(primaryStage);
        dialogStage.initModality(Modality.APPLICATION_MODAL);

        Button addSongButton = new Button("Add");
        Button savePlaylistButton = new Button("Save");

        VBox vbox = new VBox(10, addSongButton, savePlaylistButton);
        vbox.setAlignment(Pos.CENTER);

        addSongButton.setOnAction(e -> addSong());
        savePlaylistButton.setOnAction(e -> savePlaylist());

        Scene scene = new Scene(vbox, 400, 200);
        dialogStage.setScene(scene);
        dialogStage.show();
    }

    private void addSong() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("MP3 Files", "*.mp3"));
        File file = fileChooser.showOpenDialog(primaryStage);
        if (file != null) {
            songPaths.add(file.getPath());
        }
    }

    private void savePlaylist() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        File file = fileChooser.showSaveDialog(primaryStage);
        if (file != null) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                for (String songPath : songPaths) {
                    writer.println(songPath);
                }
                showCustomDialog("Playlist saved successfully!");
            } catch (Exception ex) {
                showCustomDialog("An error occurred while saving the playlist.");
                ex.printStackTrace();
            }
        }
    }

    private void showCustomDialog(String message) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(primaryStage);
        dialog.setTitle("Information");

        Label messageLabel = new Label(message);
        Button okButton = new Button("OK");
        okButton.setOnAction(e -> dialog.close());

        VBox vbox = new VBox(10, messageLabel, okButton);
        vbox.setAlignment(Pos.CENTER);

        Scene dialogScene = new Scene(vbox, 300, 150);
        dialog.setScene(dialogScene);
        dialog.showAndWait();
    }
}
