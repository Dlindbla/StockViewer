package gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import utils.IniSettings;

import java.io.IOException;

public class Main extends Application {
    public static void main(String[] args) {
        // Load ini file
        try {
            IniSettings.read("StockViewer.ini");
        }
        catch (IOException ex) {
            System.out.println("Failed to read ini-file: " + ex.toString());
        }

        // Launch application
        launch(args);
        
        // Write ini file to preserve changes
        try {
            IniSettings.write("StockViewer.ini");
        }
        catch (IOException ex) {
            System.out.println("Failed to write ini-file: " + ex.toString());
        }
    }

    public void start(Stage FirstStage) throws IOException{

        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("MainWindow.fxml"));
        root.getStylesheets().add(getClass().getClassLoader().getResource("AppStyle.css").toString());
        Scene scene = new Scene(root);
        FirstStage.setTitle("StockViewer V.0.2.\uD83C\uDF5D");
        FirstStage.setScene(scene);
        FirstStage.show();

    }
}
