package gui.controllers;

import api.Api;
import api.ApiSearchResult;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import utils.LongPosition;
import utils.PlottableObject;
import utils.Portfolio;
import java.io.Serializable;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.ResourceBundle;

public class TradingTabController implements Initializable {

    MainWindowController mainWindowController;
    Api api;
    ArrayList<Portfolio> portfolios;
    private SearchFunction searchFunction = new SearchFunction();
    PlottableObject currentTicker;

    @FXML
    ComboBox portfolioComboBox;
    @FXML
    Button updateButton;
    @FXML
    ComboBox tradingComboBox;
    @FXML
    TextField leftTextField;
    @FXML
    Button searchButton;
    @FXML
    Button buyButton;
    @FXML
    TextField quantityField;
    @FXML
    TextField buyDateField;
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

    public void search(){searchFunction.restart();}

    public void addPortfolio(String name, Double initialLiqudity){
        Portfolio newPortfolio = new Portfolio(name,initialLiqudity);
        portfolios.add(newPortfolio);
    }

    public Date getDate() throws ParseException {
        String dateString = buyDateField.getText();
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        Date inputDate = formatter.parse(dateString);
        for(var item: currentTicker.getItems()){
            if (item.getKey().compareTo(inputDate)<0){
                return item.getKey();
            }
        }
        return null;
    }

    //find the price for the given date in the data, finds the first date equal or greater than the given date
    public double getPrice() throws ParseException {
        var date = getDate();
        for(var item: currentTicker.getItems()){
            if(item.getKey().equals(date)){return item.getValue().doubleValue();}
        }
        return 0.0;
    }

    public void buyPosition() throws ParseException {
        //these could be changed to be function that both retrieve and validate the data
        var ticker = (String) tradingComboBox.getSelectionModel().getSelectedItem();
        var quantity = Integer.parseInt(quantityField.getText());
        var buyDate = getDate();
        var buyPrice = getPrice();
        var currentPortfolio = (Portfolio) portfolioComboBox.getSelectionModel().getSelectedItem();
        currentPortfolio.buyPosition(ticker,buyDate,buyPrice,quantity);
        //find the current selected portfolio
        //use the portfolios buyPosition method
    }

    public void sellPosition(){
        //get the selected position
        //compute the profit
        //call the selected portfolios sellPosition() function
        //????
        //Profit
    }






    public void deletePortfolio(Portfolio portfolio){
        portfolios.remove(portfolio);
    }

    public void savePortfolio(Serializable portfolio, String fileName){

    }

    public void loadPortfolio(String fileName){

    }

    public void updatePortfolio(){

    }

    //for a given position update it's values from an arraylist of plottableobjects
    public void updatePosition(){}



    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        positionsTable.setPlaceholder(new Label("NO PORTFOLIO SELECTED"));

        TickerColumn.setCellValueFactory(new PropertyValueFactory<>("ticker"));
        BuyDateColumn.setCellValueFactory(new PropertyValueFactory<>("buyDate"));
        BuyPriceColumn.setCellValueFactory(new PropertyValueFactory<>("buyPrice"));
        currentPriceColumn.setCellValueFactory(new PropertyValueFactory<>("currentPrice"));
        PriceDeltaColumn.setCellValueFactory(new PropertyValueFactory<>("priceDelta"));
        QuantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        TotalValueColumn.setCellValueFactory(new PropertyValueFactory<>("totalValue"));

        quantityField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue,
                                String newValue) {
                if (!newValue.matches("\\d*")) {
                    quantityField.setText(newValue.replaceAll("[^\\d]", ""));
                }
            }
        });

        //load all portfolios


    }




    private class getStockDate extends Service<ArrayList<XYChart.Series<Number, Number>>>{

        @Override
        protected Task<ArrayList<XYChart.Series<Number, Number>>> createTask() {
            return new Task<>(){
                @Override
                protected ArrayList<XYChart.Series<Number, Number>> call() throws Exception {
                    //TODO : Implement this method to populate the currentTicker() ArrayList with stockpoints
                    // Also sort the data afterwards pl0x;
                    return null;
                }
            };
        }
    }



    private class SearchFunction extends Service<ObservableList<ApiSearchResult>> {
        @Override
        protected Task<ObservableList<ApiSearchResult>> createTask() {
            return new Task<>() {
                @Override
                protected ObservableList<ApiSearchResult> call() throws Exception {
                    String searchString = leftTextField.getText();
                    if (!(searchString.isEmpty())) {
                        var res = api.search(searchString);
                        ObservableList<ApiSearchResult> list = FXCollections.observableArrayList();
                        list.addAll(res);

                        return list;
                    } else {
                        return null;
                    }
                }

                @Override
                protected void succeeded() {
                    tradingComboBox.getItems().clear(); // clear the previous items from the box
                    tradingComboBox.getItems().addAll(getValue()); // add the new once received from the task
                }

                @Override
                protected void failed() {
                    System.out.printf("failed to search: %s\n", getException().getMessage());
                }

            };
        }
    }





}













