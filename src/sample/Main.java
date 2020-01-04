package sample;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.XYChart;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.controlsfx.control.textfield.TextFields;
import org.json.simple.parser.ParseException;
import java.io.IOException;
import java.util.ArrayList;

public class Main extends Application {


    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage FirstStage) throws IOException, ParseException {

        Parent root = FXMLLoader.load(getClass().getResource("new.fxml"));
        root.getStylesheets().add(getClass().getResource("AppStyle.css").toString());
        Scene scene = new Scene(root);
        FirstStage.setTitle("StockViewer V.0.0.1");
        FirstStage.setScene(scene);
        FirstStage.show();





        //FirstStage.setScene();
        //FirstStage.show();

    }

}
