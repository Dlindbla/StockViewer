package gui.controllers;

import api.Api;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import utils.LongPosition;
import utils.Portfolio;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

public class TradingTabController {

    MainWindowController mainWindowController;
    Api api;
    ArrayList<Portfolio> portfolios;

    @FXML
    TableView<LongPosition> positionsTable;
    @FXML
    TableColumn<LongPosition, String> TickerColumn;
    @FXML
    TableColumn<LongPosition, Date> BuyDateColumn;
    @FXML
    TableColumn<LongPosition, Double> BuyPriceColumn;
    @FXML
    TableColumn<LongPosition, Double> currentPriceColumn;
    @FXML
    TableColumn<LongPosition, Double> PriceDeltaColumn;
    @FXML
    TableColumn<LongPosition, Integer> QuantityColumn;
    @FXML
    TableColumn<LongPosition, Double> TotalValueColumn;






    public void injectAPIcache(Api api){
        this.api = api;
    }

    public void addPortfolio(String name, Double initialLiqudity){
        Portfolio newPortfolio = new Portfolio(name,initialLiqudity);
        portfolios.add(newPortfolio);
    }




    public void deletePortfolio(){
    }
    public void savePortfolio(Serializable portfolio, String fileName){
    }
    public void loadPortfolio(String fileName){
    }



}













