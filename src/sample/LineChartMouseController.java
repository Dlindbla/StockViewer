package sample;

import Main.XYSeriesGenerator;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import javax.sound.sampled.Line;
import java.util.ArrayList;

public class LineChartMouseController {

    Integer firstZoomCord;

    ArrayList<XYChart.Data<Number,Number>> startValues = new ArrayList<>();

    public void setMouseController(ActionEvent actionEvent, LineChartWithMarkers lineChart, NumberAxis xAxis, NumberAxis yAxis, XYSeriesGenerator gen) {
        final Node chartBackground = lineChart.lookup(".chart-plot-background");
        chartBackground.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                int xValue = xAxis.getValueForDisplay(mouseEvent.getX()).intValue();
                if(mouseEvent.getButton() == MouseButton.PRIMARY) {
                    startValues.clear();
                    startValues.addAll(lineChart.getMarkersForXValue(xValue));
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
                if(!(firstZoomCord == null)){
                    if(mouseEvent.isControlDown()){
                        int xValue = xAxis.getValueForDisplay(mouseEvent.getX()).intValue();
                        //zoomIn(firstZoomCord, xValue);
                        lineChart.removeAllVeritcalZoomMarkers();
                    }
                    firstZoomCord = null;
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
                System.out.print("AKSAKDSAKKDSAJDSAJ");
                lineChart.removeAllStackPanes();
                int xValue = xAxis.getValueForDisplay(mouseEvent.getX()).intValue();
                //If we are zooming in
                if(!(firstZoomCord == null)){
                    lineChart.removeAllRectangleMarkers();
                    lineChart.removeAllVeritcalZoomMarkers();
                    lineChart.removeAllStackPanes();
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

                ArrayList<XYChart.Data<Number,Number>> list = lineChart.getMarkersForXValue(xValue);

                lineChart.removeAllVerticalValueMarkers();


                for(var newValue : list){
                    //Find the corrsponding old stock value for each new value if applicable
                    for(var oldValue : startValues){
                        if((String) oldValue.getExtraValue() == (String) newValue.getExtraValue()){
                            //Calculate the difference in price and set a boolean depending on value
                            double priceDelta = newValue.getYValue().doubleValue() - oldValue.getYValue().doubleValue();
                            boolean priceDeltaBoolean = priceDelta > 0;
                            //Create A string to display the delta
                            double percentDiffDouble = ((newValue.getYValue().doubleValue()/oldValue.getYValue().doubleValue())-1)*100;
                            String priceDeltaString = String.format("%S : %s%.2f",oldValue.getExtraValue(),"%",percentDiffDouble);
                            //Create StackPanes for each one
                            lineChart.addStackPane(newValue,priceDeltaString,priceDeltaBoolean);
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

                lineChart.removeAllVerticalValueMarkers();
                lineChart.addVerticalValueMarker(new XYChart.Data<>(xValue, 0));
                lineChart.removeAllStackPanes();
                int yValue = yAxis.getValueForDisplay(xValue).intValue();

                ArrayList<XYChart.Data<Number, Number>> markers = lineChart.getMarkersForXValue(xValue);
                for(XYChart.Data<Number,Number> item : markers){
                    String dataString = String.format("%s : %.2f%s",item.getExtraValue(),item.getYValue(),"$");
                    lineChart.addStackPane(item, dataString,true);
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
