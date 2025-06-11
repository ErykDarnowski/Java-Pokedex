import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONObject;

public class ApiTest {

    public static void main(String[] args) {
        try {
            String[][] pokemonData = fetchPokemonData();

            // Print the 2D array
            for (int i = 0; i < pokemonData.length; i++) {
                System.out.printf("%d. #%s | %s | %s%n", 
                    i + 1, 
                    pokemonData[i][0], 
                    pokemonData[i][1], 
                    pokemonData[i][2]);
            }

            System.out.println("Done");
        } catch (Exception e) {
            System.out.println("Error: " + e);
        }
    }

    public static String[][] fetchPokemonData() throws IOException {
        URL url = new URL("https://pokeapi.co/api/v2/pokemon?limit=100000&offset=0"); // get all pokemon from API
        URLConnection urlCon = url.openConnection();
        urlCon.connect();

        InputStream is = urlCon.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder jsonBuilder = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            jsonBuilder.append(line);
        }
        reader.close();

        JSONObject json = new JSONObject(jsonBuilder.toString());
        JSONArray results = json.getJSONArray("results");

        String[][] data = new String[results.length()][3];

        for (int i = 0; i < results.length(); i++) {
            JSONObject pokemon = results.getJSONObject(i);
            String urlStr = pokemon.getString("url");

            // Extract ID
            String id = urlStr.replaceAll(".*/(\\d+)/?$", "$1");

	    	// Format name
            String formattedName = formatPokemonName(pokemon.getString("name"));

            data[i][0] = id;
            data[i][1] = formattedName;
            data[i][2] = urlStr;
        }

        return data;
    }

    private static String formatPokemonName(String input) {
        String[] parts = input.split("-");
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < parts.length; i++) {
            String word = parts[i];
            if (word.length() > 0) {
                result.append(Character.toUpperCase(word.charAt(0)));
                if (word.length() > 1) {
                    result.append(word.substring(1).toLowerCase());
                }
            }
            if (i < parts.length - 1) {
                result.append(" ");
            }
        }

        return result.toString();
    }
}