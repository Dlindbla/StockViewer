package gui.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class mainWindowController implements Initializable {

    @FXML
    MenuItem aboutTab;
    @FXML
    TabPane tabPane;


    public void openWelcomeTab() throws IOException {
        if(!tabPane.getTabs().get(0).getText().equals("Welcome!")) {
            Tab welcomeTab = new Tab();
            welcomeTab.setContent(FXMLLoader.load(getClass().getClassLoader().getResource("welcomeTab.fxml")));
            welcomeTab.setText("Welcome!");
            tabPane.getTabs().add(0, welcomeTab);
            SingleSelectionModel<Tab> selectionModel = tabPane.getSelectionModel();
            selectionModel.select(0);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }
}
