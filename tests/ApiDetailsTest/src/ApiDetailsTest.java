import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ApiDetailsTest {

    public static void main(String[] args) {
        String apiUrl = "https://pokeapi.co/api/v2/pokemon/25"; // Example: pikachu

        try {
            // Connect and fetch JSON
            URL url = new URL(apiUrl);
            URLConnection connection = url.openConnection();
	    StringBuilder jsonBuilder;
	    try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
		jsonBuilder = new StringBuilder();
		String line;
		while ((line = reader.readLine()) != null) {
			jsonBuilder.append(line);
		}
	    }

            // Parse JSON
            JSONObject json = new JSONObject(jsonBuilder.toString());

            // Extract data
            String name = formatName(json.getString("name"));
            int id = json.getInt("id");
            int height = json.getInt("height");
            int weight = json.getInt("weight");
            String species = formatName(json.getJSONObject("species").getString("name"));

            // abilities
            JSONArray abilitiesArray = json.getJSONArray("abilities");
            String ability1 = "";
            String hiddenAbility = "";
            for (int i = 0; i < abilitiesArray.length(); i++) {
                JSONObject abilityObj = abilitiesArray.getJSONObject(i);
                String abilityName = formatName(abilityObj.getJSONObject("ability").getString("name"));
                if (abilityObj.getBoolean("is_hidden")) {
                    hiddenAbility = abilityName;
                } else {
                    ability1 = abilityName;
                }
            }

            // base stats
            JSONArray stats = json.getJSONArray("stats");
            int hp = 0, attack = 0, defense = 0, spAttack = 0, spDefense = 0, speed = 0;
            for (int i = 0; i < stats.length(); i++) {
                JSONObject statObj = stats.getJSONObject(i);
                String statName = statObj.getJSONObject("stat").getString("name");
                int value = statObj.getInt("base_stat");

                switch (statName) {
                    case "hp": hp = value; break;
                    case "attack": attack = value; break;
                    case "defense": defense = value; break;
                    case "special-attack": spAttack = value; break;
                    case "special-defense": spDefense = value; break;
                    case "speed": speed = value; break;
                }
            }

            // get image URL
            String imageUrl = "";
            try {
                JSONObject sprites = json.getJSONObject("sprites");
                JSONObject other = sprites.getJSONObject("other");

                // 1. official-artwork
                JSONObject official = other.getJSONObject("official-artwork");
                String officialUrl = official.optString("front_default", "");
                if (!officialUrl.isEmpty() && !officialUrl.endsWith(".png")) {
                    imageUrl = officialUrl;
                }

                // 2. home
                if (imageUrl.isEmpty()) {
                    JSONObject home = other.getJSONObject("home");
                    String homeUrl = home.optString("front_default", "");
                    if (!homeUrl.isEmpty() && !homeUrl.endsWith(".png")) {
                        imageUrl = homeUrl;
                    }
                }

                // 3. fallback sprite
                if (imageUrl.isEmpty()) {
                    String fallback = sprites.optString("front_default", "");
                    if (!fallback.isEmpty() && !fallback.endsWith(".png")) {
                        imageUrl = fallback;
                    }
                }
            } catch (JSONException imgEx) {
                System.out.println("Could not retrieve image: " + imgEx.getMessage());
            }

            // Format + print out data
            System.out.println(name + " #" + id);
            System.out.println("Gatunek: " + species);
            System.out.println("Wzrost: " + (height * 10) + " cm");
            System.out.println("Waga: " + (weight / 10.0) + " kg");
            System.out.println("Umiejetnosci:");
            System.out.println("  - " + ability1);
            System.out.println("  - " + hiddenAbility + " (ukryta)");
            System.out.println("Bazowe statystyki:");
            System.out.println("  - HP: " + hp);
            System.out.println("  - Atak: " + attack);
            System.out.println("  - Predkosc: " + speed);
            System.out.println("  - Atak specjalny: " + spAttack);
            System.out.println("  - Obrona specjalna: " + spDefense);
            System.out.println("\nIMG URL: " + imageUrl);

        } catch (IOException | JSONException e) {
            System.out.println("Error fetching or parsing data: " + e);
        }
    }

    // Converts 'pikachu' to 'Pikachu'
    private static String formatName(String raw) {
        String[] parts = raw.split("-");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                sb.append(Character.toUpperCase(part.charAt(0)))
                  .append(part.substring(1))
                  .append(' ');
            }
        }
        return sb.toString().trim();
    }
}