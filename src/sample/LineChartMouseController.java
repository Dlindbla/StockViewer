package sample;

import Main.XYSeriesGenerator;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

public class LineChartMouseController {

    //A startvalue for selecting zoom
    Integer startValue;




    public void setMouseController(ActionEvent actionEvent, LineChartWithMarkers lineChart, NumberAxis xAxis, NumberAxis yAxis, XYSeriesGenerator gen) {
        final Node chartBackground = lineChart.lookup(".chart-plot-background");
        //Get the amount of values on the xAxis
        //if the amount is over a certain limit, employ a range to check if the values of data should be updated


        chartBackground.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                int xValue = xAxis.getValueForDisplay(mouseEvent.getX()).intValue();
                if(mouseEvent.getButton() == MouseButton.PRIMARY) {
                    startValue = xValue;
                    //Get the date from the hiddenseries
                    int dateArraySize = gen.getAllDates().size();
                    if (xValue < dateArraySize) {
                        String date = gen.getAllDates().get(xValue);
                        XYChart.Data item = new XYChart.Data();
                        item.setYValue(0);
                        item.setXValue(xValue);
                    }

                }else if(mouseEvent.getButton() == MouseButton.SECONDARY){
                    //Cancel zoomIn if leftclick is pressed
                    lineChart.removeAllVeritcalZoomMarkers();
                    lineChart.removeAllRectangleMarkers();
                }
            }
        });

        chartBackground.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if(!(startValue == null)){
                    if(mouseEvent.isControlDown()){
                        int xValue = xAxis.getValueForDisplay(mouseEvent.getX()).intValue();
                        lineChart.zoomIn(startValue, xValue);
                        lineChart.removeAllVeritcalZoomMarkers();
                    }
                    startValue = null;
                    lineChart.removeAllRectangleMarkers();
                    lineChart.removeAllVeritcalZoomMarkers();
                }
            }
        });

        chartBackground.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                //IF the firstZoomInterger hsa been set then create a rectangle spanning from firstZoomintgercords
                //to the mouse position
                lineChart.removeAllStackPanes();
                int xValue = xAxis.getValueForDisplay(mouseEvent.getX()).intValue();
                //If we are zooming in
                if(!(startValue == null)){
                    lineChart.removeAllRectangleMarkers();
                    lineChart.removeAllVeritcalZoomMarkers();
                    lineChart.removeAllStackPanes();
                    XYChart.Data item = new XYChart.Data();
                    if(xValue>startValue) {
                        item.setXValue(startValue);
                        item.setYValue(xValue);
                    }else{
                        item.setXValue(xValue);
                        item.setYValue(startValue);
                    }
                    lineChart.addRectangleMarker(item);


                    //ADDS A VERTICAL LINE TO THE END OF THE RECTANGLE
                    lineChart.addVerticalZoomMarker(new XYChart.Data<>(startValue, 0));
                    lineChart.addVerticalZoomMarker(new XYChart.Data<>(xValue,0));
                }
                lineChart.removeAllVerticalValueMarkers();

                for(var newValue : gen.hashMap.get(xValue)){
                    //Find the corrsponding old stock value for each new value if applicable
                    for(var oldValue : gen.hashMap.get(startValue)){
                        if((String) oldValue.getExtraValue() == (String) newValue.getExtraValue()){
                            //Calculate the difference in price and set a boolean depending on value
                            double priceDelta = newValue.getYValue().doubleValue() - oldValue.getYValue().doubleValue();
                            boolean priceDeltaBoolean = priceDelta > 0;
                            //Create A string to display the delta
                            double percentDiffDouble = ((newValue.getYValue().doubleValue()/oldValue.getYValue().doubleValue())-1)*100;
                            String priceDeltaString = String.format("%S : %s%.2f",oldValue.getExtraValue(),"%",percentDiffDouble);
                            //Create StackPanes for each one,
                            XYChart.Data data = new XYChart.Data(newValue.getXValue(),newValue.getYValue());
                            lineChart.addStackPane(data,priceDeltaString,priceDeltaBoolean);
                        }
                    }
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

                //Check if the current xCord has a new value in the range and update it, if it different, update the items.

                if (!gen.hashMap.get(xValue).isEmpty()) {
                    lineChart.removeAllVerticalValueMarkers();
                    lineChart.addVerticalValueMarker(new XYChart.Data<>(xValue, 0));
                    lineChart.removeAllStackPanes();
                    for (XYChart.Data<Number, Number> item : gen.hashMap.get(xValue)) {
                        String dataString = String.format("%s : %.2f%s", item.getExtraValue(), item.getYValue(), "$");
                        XYChart.Data<Number, Number> xyData = new XYChart.Data<>(item.getXValue(), item.getYValue());
                        lineChart.addStackPane(xyData, dataString, true);
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
                lineChart.removeAllStackPanes();
            }
        });

    }

}
