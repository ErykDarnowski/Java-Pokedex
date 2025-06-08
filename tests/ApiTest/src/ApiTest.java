/*
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.MalformedURLException;

public class ApiTest {

    public static void main(String[] args) throws IOException {
        try {
            URL url = new URL("https://pokeapi.co/api/v2/pokemon?limit=100000&offset=0");
            
            URLConnection urlCon = url.openConnection();
            urlCon.connect();
            
            InputStream is = urlCon.getInputStream();
            
            int c;
            while ((c = is.read()) != -1)
                System.out.print((char)c);
            
            is.close();
            
            System.out.println("Koniec");
        } catch (MalformedURLException e) {
            System.out.println(e);
        } catch (IOException e) {
            System.out.println(e);
        }
    }
    
}
*/

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.MalformedURLException;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONObject;

public class ApiTest {

    public static void main(String[] args) {
        try {
            // Connect to API and get JSON
            URL url = new URL("https://pokeapi.co/api/v2/pokemon?limit=100000&offset=0");
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

            // Parse JSON
            JSONObject json = new JSONObject(jsonBuilder.toString());

            // Get data out + format and print it
            int count = json.getInt("count");
            System.out.println("Total Pokémon: " + count);

            JSONArray results = json.getJSONArray("results");
            for (int i = 0; i < results.length(); i++) {
                JSONObject pokemon = results.getJSONObject(i);
                String name = formatPokemonName(pokemon.getString("name"));
                String pokeUrl = pokemon.getString("url");
                System.out.println((i + 1) + ". " + name + " -> " + pokeUrl);
            }

            System.out.println("Done");

        } catch (MalformedURLException e) {
            System.out.println("Invalid URL: " + e);
        } catch (IOException e) {
            System.out.println("IO Error: " + e);
        } catch (Exception e) {
            System.out.println("Parsing Error: " + e);
        }
    }
    
    private static String formatPokemonName(String input) {
        String[] parts = input.split("-");  // split by hyphen
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