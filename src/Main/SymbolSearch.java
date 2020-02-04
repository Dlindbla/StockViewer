package Main;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class SymbolSearch {

    ArrayList<searchResultObject> searchResults = new ArrayList<>();

    public void search(String searchString) throws IOException, ParseException {
        //generate url and get json
        String searchURL = URLBuilder.searchString(searchString);
        //get JSON
        JSONObject searchResultsJSON = GetJSONData.getJsonFromUrl(searchURL);
        //parse JSON and get Array
        JSONArray results = (JSONArray) searchResultsJSON.get("bestMatches");
        if (!(results.isEmpty())) {
            for (Object resultValue : results.toArray()) {
                JSONObject resultValueAsJSON = (JSONObject) resultValue;
                String symbol = resultValueAsJSON.get("1. symbol").toString();
                String name = resultValueAsJSON.get("2. name").toString();
                String type = resultValueAsJSON.get("3. type").toString();
                String region = resultValueAsJSON.get("4. region").toString();
                String marketOpen = resultValueAsJSON.get("5. marketOpen").toString();
                String marketClose = resultValueAsJSON.get("6. marketClose").toString();
                String timezone = resultValueAsJSON.get("7. timezone").toString();
                String currency = resultValueAsJSON.get("8. currency").toString();
                Float matchScore = Float.parseFloat((String) resultValueAsJSON.get("9. matchScore"));
                searchResultObject tempResult = new searchResultObject(symbol, name, type, region, marketOpen, marketClose, timezone, currency, matchScore);
                searchResults.add(tempResult);
            }
        } else {
            System.out.println("The results-list is empty!");
        }
        //sort objects according to matchscore
        Collections.sort(searchResults);
        //return ArrayList containing all the objects
    }

    public ObservableList<searchResultObject> getObservables() {
        ObservableList<searchResultObject> result = FXCollections.observableArrayList();
        result.addAll(searchResults);
        return result;
    }
}

