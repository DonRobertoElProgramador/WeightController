package rae.wc.wcontroller;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class WCController {
    private enum weightValidationResult{
        OK, NOT_A_NUMBER, BELOW_ZERO, MORE_THAN_TWO_DECIMALS
    }

    private enum graphDateInterval{
        WEEKLY, MONTHLY, YEAR
    }

    private WeightPersister weightPersister;

    @FXML
    private TextField currentDate;
    @FXML
    private Button sendButton;
    @FXML
    private Button weeklySelector;
    @FXML
    private Button monthlySelector;
    @FXML
    private Button yearlySelector;
    @FXML
    private TextField weightInput;
    @FXML
    private Label weightMessageResult;
    @FXML
    private Label minWeightLabel;
    @FXML
    private Label maxWeightLabel;
    @FXML
    private Label lastWeightLabel;
    @FXML
    private LineChart<String, Number> weightGraph;
    @FXML
    private BorderPane borderPane;

    private graphDateInterval intervalSelected = graphDateInterval.WEEKLY;

    @FXML
    private void initialize() {
        weightPersister = new WeightPersister();
        weightPersister.loadCsvToList();
        String formattedDate = getFormattedDate();
        prepareDate(formattedDate);
        prepareButton(formattedDate);
        prepareIntervalSelector();
        refreshShownData();
    }

    private String getFormattedDate(){
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        return today.format(formatter);
    }

    private void prepareDate(String formattedDate){
        currentDate.setAlignment(Pos.CENTER);
        currentDate.setEditable(false);
        currentDate.setFocusTraversable(false);
        currentDate.setText(formattedDate);
    }

    private void prepareButton(String formattedDate){
        sendButton.setDisable(isDateSubmitted(formattedDate));
        sendButton.setOnAction(event -> {
            String weight = weightInput.getText();
            weightValidationResult validateWeightResult=validateWeight(weight);
            if(validateWeightResult== weightValidationResult.OK){
                weightPersister.saveWeight(formattedDate, weight);
                sendButton.setDisable(true);
                weightPersister.loadCsvToList();
                refreshShownData();
            } else {
                String errorMessage = translateWeightValidationResult(validateWeightResult);
                weightMessageResult.setText(errorMessage);
            }
        });
    }

    private weightValidationResult validateWeight(String weight){

        boolean isANumber = weight.matches("-?\\d+(\\.\\d+)?") || weight.matches("-?\\d+(,\\d+)?");
        if (!isANumber) {
            return weightValidationResult.NOT_A_NUMBER;
        }
        boolean moreThanTwoDecimals = false;
        weight = weight.replace(",", ".");
        if (weight.contains(".")) {
            String[] parts = weight.split("\\.");
            moreThanTwoDecimals = parts[1].length() > 2;
        }
        if(moreThanTwoDecimals){
            return weightValidationResult.MORE_THAN_TWO_DECIMALS;
        }
        float weightAsNumber = Float.parseFloat(weight);
        if(weightAsNumber<0) {
            return weightValidationResult.BELOW_ZERO;
        }
        return weightValidationResult.OK;
    }

    private void prepareIntervalSelector(){

        weeklySelector.setOnAction(event-> {
                    intervalSelected= graphDateInterval.WEEKLY;
                    refreshGraph();
                }
        );
        monthlySelector.setOnAction(event-> {
                    intervalSelected= graphDateInterval.MONTHLY;
                    refreshGraph();
                }
        );
        yearlySelector.setOnAction(event-> {
            intervalSelected= graphDateInterval.YEAR;
            refreshGraph();
        });
    }

    private void refreshGraph(){

        weightGraph.getData().clear();
        recreateChart();

        List<Pair<String, Float>> weightList = new ArrayList<>();

        if (intervalSelected == graphDateInterval.WEEKLY) {
            weightList = weightPersister.getLastWeek();
        }
        if (intervalSelected == graphDateInterval.MONTHLY) {
            weightList= weightPersister.getLastMonth();
        }
        else if (intervalSelected == graphDateInterval.YEAR) {
            weightList= weightPersister.getLastYear();
        }

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Evolución del peso");

        weightList.forEach(
                weightMeasure->series.getData().add(new XYChart.Data<>(weightMeasure.key(), weightMeasure.value()))
        );
        weightGraph.getData().add(series);
    }

    private boolean isDateSubmitted(String date){
        List<Pair<String, Float>> weightList = weightPersister.getLastWeek();
        return (weightList.get(weightList.size()-1).key()).equals(date);
    }

    private void showMaxWeight(){
        List<Pair<String, Float>> weightList = weightPersister.getWholeSeries();
        float maxFloat=0;
        if (!weightList.isEmpty()) {
            maxFloat = weightList.get(0).value();
            for (Pair<String, Float> pair : weightList) {
                if (pair.value() > maxFloat) {
                    maxFloat = pair.value();
                }
            }
        }
        maxWeightLabel.setText("PESO MÁXIMO \n"+maxFloat);
    }

    private void showMinWeight(){
        List<Pair<String, Float>> weightList = weightPersister.getWholeSeries();
        float minFloat=9999;
        if (!weightList.isEmpty()) {
            minFloat = weightList.get(0).value();
            for (Pair<String, Float> pair : weightList) {
                if (pair.value() < minFloat) {
                    minFloat = pair.value();
                }
            }
        }
        minWeightLabel.setText("PESO MÍNIMO \n"+minFloat);
    }

    private void showLastWeight(){
        List<Pair<String, Float>> weightList = weightPersister.getLastWeek();
        float lastWeight = weightList.get(weightList.size()-1).value();
        lastWeightLabel.setText("ÚLTIMO PESO \n"+lastWeight);
    }

    private void refreshShownData(){
        showMaxWeight();
        showMinWeight();
        showLastWeight();
        refreshGraph();
    }

    private String translateWeightValidationResult(weightValidationResult result){
        switch (result){
            case NOT_A_NUMBER -> {
                return "EL NÚMERO NO ES VÁLIDO";
            }
            case BELOW_ZERO -> {
                return "EL NÚMERO TIENE QUE SER MAYOR QUE CERO";
            }
            case MORE_THAN_TWO_DECIMALS -> {
                return "EL NÚMERO NO PUEDE TENER MÁS DE DOS DECIMALES";
            }
        }

        return "INTRODUCE TU PESO";
    }

    private void recreateChart(){

        borderPane.setCenter(null);

        CategoryAxis newXAxis = new CategoryAxis();
        NumberAxis newYAxis = new NumberAxis();

        // Create a new LineChart with the new axes
        weightGraph = new LineChart<String, Number>(newXAxis, newYAxis);

        // Replace the old chart with the new one in the center of the BorderPane
        borderPane.setCenter(weightGraph);

    }

}
