package gui.controllers;

import api.Api;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import java.io.IOException;

public class MainWindowController {

    @FXML
    MenuItem aboutTab;
    @FXML
    TabPane tabPane;

    //The following dark magic allows the trading and analysis tabs to use the same API object so as to save API requests



    @FXML private StockTabController stockTabController;
    @FXML private TradingTabController tradingTabController;

    public Api getApi(){
        return stockTabController.alphaVantage;
    }


    public void openWelcomeTab() throws IOException {
        if(!tabPane.getTabs().get(0).getText().equals("Welcome!")) {
            Tab welcomeTab = new Tab();
            welcomeTab.setContent(FXMLLoader.load(getClass().getClassLoader().getResource("WelcomeTab.fxml")));
            welcomeTab.setText("Welcome!");
            tabPane.getTabs().add(0, welcomeTab);
            SingleSelectionModel<Tab> selectionModel = tabPane.getSelectionModel();
            selectionModel.select(0);
        }
    }

    @FXML
    private void initialize() {
        //inject this main controller into the trading controller so that it may call this controller to get the API
        //From the stockTabController
        tradingTabController.injectAPIcache(getApi());
    }
}
