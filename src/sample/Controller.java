package sample;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.chart.*;
import Main.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.util.StringConverter;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import javax.swing.*;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class Controller implements Initializable {
    @FXML
    Button searchButton;
    @FXML
    Button fillLineChartButton;
    @FXML
    LineChartWithMarkers<Number, Number> lineChart;
    @FXML
    NumberAxis xAxis;
    @FXML
    NumberAxis yAxis;
    @FXML
    TextField leftTextField;
    @FXML
    TableView<searchResultObject> tickerTable;
    @FXML
    ComboBox<searchResultObject> leftComboBox;
    @FXML
    TableColumn<searchResultObject, String> symbolColumn;
    @FXML
    TableColumn<searchResultObject, Double> priceColumn;
    @FXML
    ComboBox<String> intervalCombobox;
    @FXML
    Button flipbutton;

    //Searchbox press enter function
    @FXML
    public void onEnter(ActionEvent actionEvent){
        threadedSearchFunction();
    }



    Integer firstZoomCord = null;
    Integer secondZoomCord = null;

    //Function for zooming in on the lineChart
    @FXML
    public void onClick(ActionEvent actionEvent) {
        final Node chartBackground = lineChart.lookup(".chart-plot-background");
        chartBackground.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (zoomInSet) {
                    int xValue = xAxis.getValueForDisplay(mouseEvent.getX()).intValue();
                    //Get the date from the hiddenseries
                    int dateArraySize = gen.getAllDates().size();
                    if (xValue < dateArraySize) {
                        String date = gen.getAllDates().get(xValue);
                        System.out.println(date);
                        XYChart.Data item = new XYChart.Data();
                        item.setYValue(0);
                        item.setXValue(xValue);

                        if(secondZoomCord == null && firstZoomCord == null){
                            //set first cord
                            firstZoomCord = xValue;

                        }else if(secondZoomCord == null){
                            //set second cord
                            secondZoomCord = xValue;
                        }

                        //If both values are set then zoom in
                        if(!(secondZoomCord == null) && !(firstZoomCord == null)){
                            zoomIn(firstZoomCord,secondZoomCord);
                            lineChart.removeAllVeritcalZoomMarkers();
                            firstZoomCord = null;
                            secondZoomCord = null;
                        }
                    }
                }
            }
        });

        chartBackground.setOnMouseMoved(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                //IF the firstZoomInterger hsa been set then create a rectangle spanning from firstZoomintgercords
                //to the mouse position
                if(!(firstZoomCord == null)){
                    lineChart.removeAllRectangleMarkers();
                    lineChart.removeAllVeritcalZoomMarkers();
                    System.out.println(lineChart.rectangleMarkers.size());
                    int xValue = xAxis.getValueForDisplay(mouseEvent.getX()).intValue();
                    XYChart.Data item = new XYChart.Data();

                    if(xValue>firstZoomCord) {
                        item.setXValue(firstZoomCord);
                        item.setYValue(xValue);
                    }else{
                        item.setXValue(xValue);
                        item.setYValue(firstZoomCord);
                    }
                    lineChart.addRectangleMarker(item);
                    //ADDS A VERTICAL LINE TO THE END OF THE RECTANGLE
                    lineChart.addVerticalZoomMarker(new XYChart.Data<>(firstZoomCord, 0));
                    lineChart.addVerticalZoomMarker(new XYChart.Data<>(xValue,0));
                }
            }
        });
    }



    //Boolean to check if user intends to use zoom in function
    boolean zoomInSet = false;

    //cache downloaded stockdata objects during the session
    StockDataCache cache = new StockDataCache();

    //contains the symbol string of the currently displayed tickers
    ArrayList<String> drawnTickers = new ArrayList<>();

    XYSeriesGenerator gen = new XYSeriesGenerator();
    private GraphDrawer graphDrawer = new GraphDrawer();
    private SearchFunction searchFunction = new SearchFunction();

    String currentDrawnInterval = "15min";

    public void threadedDrawFunction(javafx.event.ActionEvent actionEvent) {
        graphDrawer.restart();
    }

    public void threadedSearchFunction() {
        searchFunction.restart();
    }

    public void hideHiddenSeriesLegend() {
        //THIS MIGHT RESULT IN A BUG IF USER INPUTS A MARKERLINE BEFORE LOADING ANY TICKERS
        //OR IF THE USER THEN LOADS A TICKER IT MIGHT CAUSE THE LINE TO BE OVERWRITTEN
        /*
        XYChart.Series item = (XYChart.Series) lineChart.getData().get(0);
        item.getNode().setVisible(false);
        */

        lineChart.getSeries().get(0).getNode().setVisible(false);
        Set<Node> legends = lineChart.lookupAll("Label.chart-legend-item");
        legends.stream().findFirst().get().setVisible(!legends.stream().findFirst().get().isVisible());

    }

    public void runLaterfillLineChart(ArrayList<XYChart.Series> series) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                fillLineChart(series);
            }
        });
    }

    public void fillLineChart(ArrayList<XYChart.Series> series) {
        boolean firstpass = true;
        for (XYChart.Series item : series) {
            //Attempts to update the hiddenSeries
            if (firstpass) {
                try {
                    //always update the hidden series
                    lineChart.getData().set(0, item);
                    firstpass = false;
                    continue;
                } catch (IndexOutOfBoundsException e) {
                    firstpass = false;
                }
            }
            if (!drawnTickers.contains(item.getName())) {
                lineChart.getData().add(item);
                drawnTickers.add(item.getName());
            }
        }
        hideHiddenSeriesLegend();
        System.out.println("ITEMS ADDED TO LINECHART");
        xAxis.setUpperBound(gen.getAllDates().size());
    }

    public void setZoom(){
        //SET THE STATE OF THE ZOOM LINES TO DEFAULT
        //IF USERS CLICKS THE LINECHART SETS FIRST LINE AND THEN SECOND LINE
        //ONCE BOTH LINES ARE SET WILL USE SET LOWER AND UPPER BOUND TO ZOOM IN
        zoomInSet = !zoomInSet;
        if(!zoomInSet){
            firstZoomCord = null;
            secondZoomCord = null;
        }
    }

    public void zoomIn(int firstValue, int secondValue){
        lineChart.removeAllRectangleMarkers();
        lineChart.setAnimated(true);
        xAxis.setAutoRanging(false);
        xAxis.setForceZeroInRange(false);

        //The amount of ticks displayed
        int rangeSize;

        if(firstValue>secondValue){
            xAxis.setUpperBound(firstValue);
            xAxis.setLowerBound(secondValue);
            rangeSize = firstValue - secondValue;
        }
        else{
            xAxis.setLowerBound(firstValue);
            xAxis.setUpperBound(secondValue);
            rangeSize = secondValue - firstValue;
        }
        //dynamically set the this to be the range of the displayed area divided by 10
        xAxis.setTickUnit(rangeSize/10);

    }
    public void resetZoom(){
        //.removeAllRectangleMarkers();
        lineChart.removeAllVeritcalZoomMarkers();
        xAxis.setAutoRanging(true);
    }

    public void testbutton() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                clearLineChart();
            }
        });
    }

    public void clearLineChart() {
        lineChart.getData().clear();
        lineChart.removeAllVerticalValueMarkers();
        drawnTickers.clear();
        yAxis.setUpperBound(100);
        yAxis.setLowerBound(0);
    }

    public void deleteTicker(javafx.event.ActionEvent actionEvent) {
        Object objectToRemove = tickerTable.getSelectionModel().getSelectedItem();
        tickerTable.getItems().remove(objectToRemove);
    }

    public void addTicker(javafx.event.ActionEvent actionEvent) {
        searchResultObject item = leftComboBox.getSelectionModel().getSelectedItem();
        System.out.println(item.getSymbol());
        tickerTable.getItems().add(item);
    }

    //Display a tooltip at the parent node of the caller
    public void displayTooltip(Parent parent, String message) {
        final Tooltip emptySearchWarning = new Tooltip();
        Bounds boundsInScene = parent.localToScreen(parent.getBoundsInLocal());
        emptySearchWarning.setAutoHide(true);
        emptySearchWarning.setText(message);
        emptySearchWarning.show(parent, boundsInScene.getMaxX(), boundsInScene.getMaxY());
    }

    //Find the right time interval key for the URLBuilder and JSONParser
    public String getTimeSerie() {
        String intervalboxString = intervalCombobox.getSelectionModel().getSelectedItem();
        if (intervalboxString == null) {
            intervalboxString = "15min";
        }
        // TODO: Move these lists to an .INI file
        ArrayList<String> series = new ArrayList<>(List.of("15min", "5min", "1min", "Monthly", "Weekly", "Daily"));
        ArrayList<String> timeSeries = new ArrayList<>(List.of("TIME_SERIES_INTRADAY", "TIME_SERIES_INTRADAY", "TIME_SERIES_INTRADAY", "TIME_SERIES_MONTHLY", "TIME_SERIES_WEEKLY", "TIME_SERIES_DAILY"));
        return timeSeries.get(series.indexOf(intervalboxString));
    }

    public ArrayList<StockData> generateStockdataArray(ArrayList<String> symbolStrings, String interval) throws IOException, ParseException, java.text.ParseException {
        ArrayList<StockData> stockDataArrayList = new ArrayList<StockData>();
        for (String symbol : symbolStrings) {
            String timeSeries = getTimeSerie();
            String url = URLBuilder.queryString(timeSeries, symbol, interval, true);
            System.out.println(url);
            //GET JSON object
            JSONObject jsonData = GetJSONData.getJsonFromUrl(url);
            //Parse to StockData object
            StockData stockDataObject = new StockData(jsonData, symbol, interval);
            //Feed into the Generator to create a XYChart.Series object
            stockDataArrayList.add(stockDataObject);
        }
        return stockDataArrayList;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //Start up values
        //TODO : Move as many lines as possible from here to new.FXML
        SymbolSearch searchResults = new SymbolSearch();
        ObservableList<searchResultObject> observables = searchResults.getObservables();
        lineChart.setCreateSymbols(false);
        leftComboBox.setItems(observables);
        tickerTable.setPlaceholder(new Label("NO TICKERS SELECTED"));
        symbolColumn.setCellValueFactory(new PropertyValueFactory<>("symbol"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        intervalCombobox.getItems().addAll("15min", "5min", "1min", "Monthly", "Weekly", "Daily");
        xAxis.setMinorTickVisible(false);
        yAxis.setForceZeroInRange(false);
        lineChart.layout();

        onClick(new ActionEvent());


        xAxis.setTickLabelFormatter(new StringConverter<>() {
            @Override
            public String toString(Number number) {
                //Check if the alldates has been generated
                //iterate over all the Dates and assign them
                if (!gen.getAllDates().isEmpty() && number.intValue() < gen.getAllDates().size()) {
                    String label = gen.getAllDates().get(number.intValue());
                    return label;
                }
                return null;
            }

            @Override
            public Number fromString(String s) {
                return null;
            }
        });

        //Prints the Date value of the coordinates the mouse is hovering

    }

    private class GraphDrawer extends Service<ArrayList<XYChart.Series>> {
        @Override
        protected Task<ArrayList<XYChart.Series>> createTask() {
            return new Task<ArrayList<XYChart.Series>>() {
                @Override
                protected ArrayList<XYChart.Series> call() throws Exception {
                    if (!tickerTable.getItems().isEmpty()) {
                        //if user doesn't set an interval time default to 15min
                        //TODO: possibly move this to another method or make one
                        //TODO: Create method do detect if the time interval has been changed
                        String interval = intervalCombobox.getSelectionModel().getSelectedItem();
                        if (interval == null) {
                            interval = "15min";
                        }

                        //check if the timeInterval has been changed and update it
                        if (!(currentDrawnInterval == interval)) {
                            currentDrawnInterval = interval;
                            testbutton();
                            //drawnTickers is also cleared in testbutton() but needs to be done here due to threading
                            drawnTickers.clear();
                        }

                        ArrayList<String> symbolStrings = new ArrayList<>();
                        //add all stock symbols from tableview to list, skip items already drawn
                        for (searchResultObject item : tickerTable.getItems()) {
                            if (!drawnTickers.contains(item.getSymbol())) {
                                symbolStrings.add(item.getSymbol());
                            }
                        }
                        //TODO : Cache the stockdata in an Arraylist so that it can be re-used when changing date spinners

                        ArrayList<StockData> stockDataArrayList = new ArrayList<>();
                        //Check if the cache contains the stockdata already
                        for (String item : symbolStrings) {
                            if (cache.contains(item.concat(interval))) {
                                int index = cache.indexOf(item.concat(interval));
                                stockDataArrayList.add((cache.get(index)));
                            }
                        }
                        //remove cached items from symbolStrings
                        for (StockData item : stockDataArrayList){
                            if(symbolStrings.contains(item.getStockSymbol())){
                                symbolStrings.remove(item.getStockSymbol());
                            }
                        }

                        ArrayList<StockData> uncachedData = new ArrayList<>();
                        if(!symbolStrings.isEmpty()) {
                            uncachedData.addAll(generateStockdataArray(symbolStrings, interval));
                        }
                        //cache here-to uncached items
                        for(var item : uncachedData){
                            cache.add(item);
                            stockDataArrayList.add(item);
                        }

                        gen.populateSeries(stockDataArrayList);
                        return gen.getSeries();
                    } else {
                        displayTooltip(fillLineChartButton, "Please add stonks to the list");
                        return null;
                    }
                }

                @Override
                protected void succeeded() {
                    var results = getValue();
                    runLaterfillLineChart(results);
                }

                @Override
                protected void failed() {
                    System.out.println(getValue());
                    Throwable error = getException();
                    System.out.println(error);
                }
            };
        }
    }

    private class SearchFunction extends Service<ObservableList<searchResultObject>> {
        @Override
        protected Task<ObservableList<searchResultObject>> createTask() {
            return new Task<ObservableList<searchResultObject>>() {
                @Override
                protected ObservableList<searchResultObject> call() throws Exception {
                    String searchString = leftTextField.getText();
                    if (!(searchString.isEmpty())) {
                        SymbolSearch searchResults = new SymbolSearch();
                        searchResults.search(searchString);
                        ObservableList<searchResultObject> observables = searchResults.getObservables();
                        return observables;
                    } else {
                        displayTooltip(searchButton, "Please input a search string!");
                        return null;
                    }
                }

                @Override
                protected void succeeded() {
                    leftComboBox.getItems().clear(); // clear the previous items from the box
                    leftComboBox.getItems().addAll(getValue()); //add the new once received from the task
                }

                @Override
                protected void failed() {
                    Throwable error = getException();
                }
            };
        }
    }
}
