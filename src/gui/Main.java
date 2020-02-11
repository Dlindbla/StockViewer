package gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import java.io.IOException;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage FirstStage) throws IOException{
        Parent root = FXMLLoader.load(getClass().getResource("views/mainWindow.fxml"));
        root.getStylesheets().add(getClass().getResource("AppStyle.css").toString());
        Scene scene = new Scene(root);
        FirstStage.setTitle("StockViewer V.0.2.\uD83C\uDF5D");
        FirstStage.getIcons().add(new Image("resources/stonks.png"));
        FirstStage.setScene(scene);
        FirstStage.show();
    }
}
