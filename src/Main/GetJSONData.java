package Main;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;


public class GetJSONData {

    public static JSONObject getJsonFromUrl(String inputURL) throws IOException, ParseException {
        URL url = new URL(inputURL);
        BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
        String temp_string = "";
        String json_string = "";
        while (null != (temp_string = br.readLine())) {
            json_string += temp_string;
        }
        JSONParser parser = new JSONParser();
        JSONObject jsonObject = (JSONObject) parser.parse(json_string);
        return jsonObject;
    }

}
