package gui.controllers;

import api.Api;
import api.ApiException;
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
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import utils.LongPosition;
import utils.PlottableObject;
import utils.Portfolio;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.ResourceBundle;

public class TradingTabController implements Initializable {

    MainWindowController mainWindowController;
    Api api;
    ArrayList<Portfolio> portfolios = new ArrayList<>();
    private SearchFunction searchFunction = new SearchFunction();
    private updateCurrentPortfolio portfolioUpdater = new updateCurrentPortfolio();
    PlottableObject currentTicker;

    final String DEFAULT_INTERVAL = "Daily";
    final String DEFAULT_DATATYPE = "Close";

    @FXML
    TextField newPortfolioNameField;
    @FXML
    TextField newPortfolioLiquidityField;
    @FXML
    ComboBox portfolioComboBox;
    @FXML
    Button createPortfolioButton;
    @FXML
    Button deletePortfolioButton;
    @FXML
    ComboBox tradingComboBox;
    @FXML
    TextField leftTextField;
    @FXML
    Button searchButton;
    @FXML
    Button updatePortfolioButton;
    @FXML
    Button buyButton;
    @FXML
    Button sellButton;
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

    public void updateCurrentlySelectedPortfolio(){portfolioUpdater.restart();}

    public void updateCurrentTicker() throws ApiException {
        //gets the currently selected symbol, queries the API for it's daily interval plottable object and overwrites
        //currentticker with it
        //This should be threaded, it's not, too bad
        var searchResult = (ApiSearchResult) tradingComboBox.getSelectionModel().getSelectedItem();
        var ticker = searchResult.getSymbol();
        var plottableObject = api.query(ticker, DEFAULT_INTERVAL,DEFAULT_DATATYPE);
        currentTicker = plottableObject;
    }

    public Date getDate() throws ParseException {
        String dateString = buyDateField.getText();
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        Date inputDate = formatter.parse(dateString);

        // find closest date
        currentTicker.sortItems();
        var date = currentTicker.getItems().get(0).getKey();

        for (var item : currentTicker.getItems()) {
            if (item.getKey().equals(inputDate)) return item.getKey();

            if (date.compareTo(inputDate) < 0) {
                date = item.getKey();
                continue;
            }

            if (date.compareTo(inputDate) > 0) {
                break;
            }
        }

        return date;
    }

    //find the price for the given date in the data, finds the first date equal or greater than the given date
    public double getPriceByDate() throws ParseException {
        var date = getDate();
        for (var item : currentTicker.getItems()) {
            if (item.getKey().equals(date)) {
                return item.getValue().doubleValue();
            }
        }
        return 0.0;
    }

    public void selectCurrentPortfolio() {
        // get currently selected portfolio
        var currentPortfolio = (Portfolio) portfolioComboBox.getSelectionModel().getSelectedItem();

        // update table rows
        positionsTable.getItems().clear();
        if (currentPortfolio != null) {
            for (var position : currentPortfolio.longPositions) {
                // fetch latest price
                try {
                    var currentPrice = getCurrentTickerPrice(position.getTicker());
                    position.setCurrentPrice(currentPrice);
                }
                catch (ApiException ex) {}

                positionsTable.getItems().add(position);
            }
        }
    }

    public void buyPosition() throws ParseException {
        //these could be changed to be function that both retrieve and validate the data
        var searchResult = (ApiSearchResult) tradingComboBox.getSelectionModel().getSelectedItem();
        var ticker = searchResult.getSymbol();
        var quantity = Integer.parseInt(quantityField.getText());
        var buyDate = getDate();
        var buyPrice = getPriceByDate();
        var currentPortfolio = (Portfolio) portfolioComboBox.getSelectionModel().getSelectedItem();
        currentPortfolio.buyPosition(ticker,buyDate,buyPrice,quantity);

        savePortfolios();
        selectCurrentPortfolio();
    }

    public void sellPosition() throws ApiException {
        //get the selected position
        LongPosition longPosition = positionsTable.getSelectionModel().getSelectedItem();
        //get the current price for the positions ticker
        double sellPrice = getCurrentTickerPrice(longPosition.getTicker());
        //call the selected portfolios sellPosition() function
        var currentPortfolio = (Portfolio) portfolioComboBox.getSelectionModel().getSelectedItem();
        currentPortfolio.sellPosition(longPosition, sellPrice);

        var diff = (sellPrice - longPosition.getBuyPrice()) * longPosition.getQuantity();

        // lol
        var alert = new Alert(AlertType.INFORMATION);
        alert.setContentText(String.format("Sold %s with a %s of %f", longPosition.getTicker(), ((diff >= 0) ? "profit" : "loss"), diff));
        alert.show();

        savePortfolios();
        selectCurrentPortfolio();
    }

    public double getCurrentTickerPrice(String ticker) throws ApiException {
        var data = api.query(ticker, DEFAULT_INTERVAL, DEFAULT_DATATYPE);
        data.sortItems();
        var items = data.getItems();
        return items.get(items.size() - 1).getValue().doubleValue();
    }


    public void createPortfolio(){
        //get the new name from the namefield
        String name = newPortfolioNameField.getText();
        double initLiquidity = Double.parseDouble(newPortfolioLiquidityField.getText());
        Portfolio newPortfolio = new Portfolio(name,initLiquidity);
        portfolios.add(newPortfolio);
        updatePortfolioComboBox();

        // save to file
        savePortfolios();
    }
    public void deletePortfolio(){
        //get the current chosen portfolio
        Portfolio portfolioDelete = (Portfolio) portfolioComboBox.getSelectionModel().getSelectedItem();
        //delete it
        portfolios.remove(portfolioDelete);
        updatePortfolioComboBox();

        savePortfolios();
    }
    
    public void updatePortfolioComboBox(){
        portfolioComboBox.getItems().clear();
        portfolioComboBox.getItems().addAll(portfolios);
    }


    //for a given position update it's values from an arraylist of plottableobjects
    public void updatePosition(LongPosition position) throws ApiException {
        //get the ticker from the position
        String ticker = position.getTicker();
        //get the plottableobject from the api
        api.query(ticker,DEFAULT_INTERVAL,DEFAULT_INTERVAL);
        //get the latest price from the plottable object
        //update the positions value
    }



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

        //prevents user from inserting non integers into the liquidity and quantity fields
        quantityField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue,
                                String newValue) {
                if (!newValue.matches("\\d*")) {
                    quantityField.setText(newValue.replaceAll("[^\\d]", ""));
                }
            }
        });
        newPortfolioLiquidityField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue,
                                String newValue) {
                if (!newValue.matches("\\d*")) {
                    newPortfolioLiquidityField.setText(newValue.replaceAll("[^\\d]", ""));
                }
            }
        });

        //load all portfolios
        try {
            var fileStream = new FileInputStream("portfolios.bin");
            var objectStream = new ObjectInputStream(fileStream);
            var p = (ArrayList<Portfolio>)objectStream.readObject();
            objectStream.close();
            fileStream.close();

            portfolios.addAll(p);
            updatePortfolioComboBox();
        }
        catch (FileNotFoundException ex) {}
        catch (Exception ex) {
            System.out.println("Failed to load portfolios: " + ex.getMessage());
        }
    }

    private void savePortfolios() {
        try {
            var fileStream = new FileOutputStream("portfolios.bin");
            var objectStream = new ObjectOutputStream(fileStream);
            objectStream.writeObject(portfolios);
            objectStream.close();
            fileStream.close();
        }
        catch (Exception ex) {
            System.out.println("Failed to save portfolios to disk: " + ex.getMessage());
        }
    }


    //start to update the currently selected portfolio
    private class updateCurrentPortfolio extends Service<ArrayList<LongPosition>>{
        @Override
        protected Task<ArrayList<LongPosition>> createTask() {
            return new Task<>(){
                @Override
                public ArrayList<LongPosition> call() throws Exception {
                    // update table rows

                    //create a new list to contain the updated poistions
                    var currentPortfolio = (Portfolio) portfolioComboBox.getSelectionModel().getSelectedItem();

                    ArrayList<LongPosition> newList = new ArrayList<>();
                    //positionsTable.getItems().clear();

                    if (currentPortfolio != null) {
                        for (var position : currentPortfolio.longPositions) {
                            // fetch latest price
                            try {
                                var currentPrice = getCurrentTickerPrice(position.getTicker());
                                position.setCurrentPrice(currentPrice);
                            }
                            catch (ApiException ex) {}
                            //add all the new positions into the list
                            newList.add(position);
                        }

                    }
                    return newList;
                }
                @Override
                protected void succeeded() {
                    //replace the positions in the portfolio
                    positionsTable.getItems().clear();
                    for(var item : getValue()){
                        positionsTable.getItems().add(item);
                    }
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













