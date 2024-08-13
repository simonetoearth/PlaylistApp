package edu.sdccd.cisc191;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private Connection connection;

    public DatabaseManager() {
        try {
            // Initialize the database connection
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:playlistapp.db");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void executeScript(String scriptPath) {
        try (Statement statement = connection.createStatement()) {
            // Read the SQL script from the file
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(scriptPath);
            if (inputStream == null) {
                throw new FileNotFoundException("Script file not found: " + scriptPath);
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder script = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                script.append(line).append("\n");
            }
            reader.close();

            // Execute the script
            statement.execute(script.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
