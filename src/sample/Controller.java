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
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.util.StringConverter;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
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

    //Function for zooming in on the lineChart
    @FXML
    public void onClick(ActionEvent actionEvent) {
        final Node chartBackground = lineChart.lookup(".chart-plot-background");
        chartBackground.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if(mouseEvent.getButton() == MouseButton.PRIMARY) {
                    if (mouseEvent.isControlDown()) {
                        int xValue = xAxis.getValueForDisplay(mouseEvent.getX()).intValue();
                        //Get the date from the hiddenseries
                        int dateArraySize = gen.getAllDates().size();
                        if (xValue < dateArraySize) {
                            String date = gen.getAllDates().get(xValue);
                            System.out.println(date);
                            XYChart.Data item = new XYChart.Data();
                            item.setYValue(0);
                            item.setXValue(xValue);
                            if (firstZoomCord == null) {
                                //set first cord
                                firstZoomCord = xValue;
                            }
                        }
                    }
                }else if(mouseEvent.getButton() == MouseButton.SECONDARY){
                    //Cancel zoomIn if leftclick is pressed
                    lineChart.removeAllVeritcalZoomMarkers();
                    lineChart.removeAllRectangleMarkers();
                    firstZoomCord = null;
                }
            }
        });

        chartBackground.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if(mouseEvent.isControlDown()){
                    if(!(firstZoomCord == null)){
                        int xValue = xAxis.getValueForDisplay(mouseEvent.getX()).intValue();
                        zoomIn(firstZoomCord, xValue);
                        firstZoomCord = null;
                        lineChart.removeAllVeritcalZoomMarkers();
                    }
                }
            }
        });

        chartBackground.setOnMouseDragged(new EventHandler<MouseEvent>() {
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

        chartBackground.setOnMouseMoved(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                //USE THE xAXIS VALUE TO FIND A RANDOM SERIES CORRESPONDING YVALUE LOCATION
                //Get the X cord of the mouse and round to the nearest integer value
                //Get the Y values of each series with x cord value

                //We round the mouse cords to Integer so that the line wont flicker or disappear if the mouse if between
                //two value on the xAxis
                Number xMouseCord = mouseEvent.getX();
                int xValue = xAxis.getValueForDisplay(xMouseCord.intValue()).intValue();

                lineChart.removeAllVerticalValueMarkers();
                lineChart.addVerticalValueMarker(new XYChart.Data<>(xValue, 0));
                lineChart.removeAllSeriesLabels();
                int yValue = yAxis.getValueForDisplay(xValue).intValue();


                //TODO : IMPROVE THIS TO BE FASTER, THIS IS GARBAGE
                if(!lineChart.getData().isEmpty()) {
                    for (XYChart.Series series : lineChart.getSeries()) {
                        if (!(series.getName() == "HiddenSeries")) {
                            if (xValue < series.getData().size()) {
                                for (Object dataItem : series.getData()) {
                                    var item = (XYChart.Data<Number, Number>) dataItem;
                                    if (item.getXValue().intValue() == xValue) {
                                        lineChart.addInfoBox(new XYChart.Data<>(xValue, item.getYValue()));
                                        continue;
                                        //TODO : Create a label inside of the rectangle that displays the current price
                                    }
                                }
                            }
                        }
                    }
                }
            }
        });

        chartBackground.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                lineChart.removeAllVeritcalZoomMarkers();
                lineChart.removeAllVerticalValueMarkers();
                lineChart.removeAllRectangleMarkers();
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
        //Sets the hiddenSeries legend to invisible
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
                item.getNode().setMouseTransparent(true);
                drawnTickers.add(item.getName());
            }
        }
        hideHiddenSeriesLegend();
        System.out.println("ITEMS ADDED TO LINECHART");
        xAxis.setUpperBound(gen.getAllDates().size());
    }

    public void setZoom(){
        //Flips the zoom in boolean and clears zoom in cords
        zoomInSet = !zoomInSet;
        if(!zoomInSet){
            firstZoomCord = null;
        }
    }

    public void zoomIn(int firstValue, int secondValue){
        lineChart.removeAllRectangleMarkers();
        lineChart.setAnimated(true);
        xAxis.setAutoRanging(false);
        xAxis.setForceZeroInRange(false);

        //The amount of ticks displayed
        int rangeSize;

        //if the difference is less than the method will not zoom

        if(Math.abs((firstValue-secondValue))>=3) {
            if (firstValue > secondValue) {
                xAxis.setUpperBound(firstValue);
                xAxis.setLowerBound(secondValue);
                rangeSize = firstValue - secondValue;
            } else {
                xAxis.setLowerBound(firstValue);
                xAxis.setUpperBound(secondValue);
                rangeSize = secondValue - firstValue;
            }
            //Sets the amount of ticks displayed
            xAxis.setTickUnit(rangeSize / 10);
            if (rangeSize < 10) {
                xAxis.setTickUnit(1);
            }
        }
    }

    public void resetZoom(){
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

    //TODO: REPLACE THIS SO THAT THE STOCKDATA DOESNT NEED A KEY AND INSTEAD PEEKS INTO THE KEYSETS DATA TO FIND THE RIGHT KEY
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

    //TODO: MOVE THIS SOMEWHERE ELSE
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

    public void createPearsonValue(){
        PearsonCorrelation pCorr = new PearsonCorrelation();
        double pCorrelationDouble = pCorr.calculateCorrelation(lineChart.getSeries().get(1),lineChart.getSeries().get(2));
        System.out.println("The pearson Correlation between the items is : " + pCorrelationDouble);
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

                        //Check if the cache contains the stockdata already
                        ArrayList<StockData> stockDataArrayList = new ArrayList<>();
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
                        //Set up uncached items for caching
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
