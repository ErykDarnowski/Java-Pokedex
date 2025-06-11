import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class PokeApiService {

    public List<Pokemon> fetchAllPokemon() throws Exception {
        List<Pokemon> pokemons = new ArrayList<>();

        String apiUrl = "https://pokeapi.co/api/v2/pokemon?limit=100000&offset=0";
        JSONObject json = new JSONObject(readFromUrl(apiUrl));
        JSONArray results = json.getJSONArray("results");

        for (int i = 0; i < results.length(); i++) {
            JSONObject pokemon = results.getJSONObject(i);
            String urlStr = pokemon.getString("url");
            String id = urlStr.replaceAll(".*/(\\d+)/?$", "$1");
            String name = FormatterUtil.formatName(pokemon.getString("name"));
            pokemons.add(new Pokemon(id, name, urlStr));
        }

        return pokemons;
    }

    public PokemonDetails fetchPokemonDetails(String url) throws Exception {
        JSONObject json = new JSONObject(readFromUrl(url));

        String name = FormatterUtil.formatName(json.getString("name"));
        int id = json.getInt("id");
        int height = json.getInt("height");
        int weight = json.getInt("weight");
        String species = FormatterUtil.formatName(json.getJSONObject("species").getString("name"));

        List<String> abilities = new ArrayList<>();
        JSONArray abilitiesArray = json.getJSONArray("abilities");
        for (int i = 0; i < abilitiesArray.length(); i++) {
            JSONObject abilityObj = abilitiesArray.getJSONObject(i);
            String ability = FormatterUtil.formatName(abilityObj.getJSONObject("ability").getString("name"));
            if (abilityObj.getBoolean("is_hidden")) {
                abilities.add(ability + " (ukryta)");
            } else {
                abilities.add(ability);
            }
        }

        JSONArray stats = json.getJSONArray("stats");
        int hp = 0, attack = 0, defense = 0, spAttack = 0, spDefense = 0, speed = 0;
        for (int i = 0; i < stats.length(); i++) {
            JSONObject stat = stats.getJSONObject(i);
            int value = stat.getInt("base_stat");
            switch (stat.getJSONObject("stat").getString("name")) {
                case "hp": hp = value; break;
                case "attack": attack = value; break;
                case "defense": defense = value; break;
                case "special-attack": spAttack = value; break;
                case "special-defense": spDefense = value; break;
                case "speed": speed = value; break;
            }
        }

        String imageUrl = "";
        try {
            JSONObject sprites = json.getJSONObject("sprites");
            JSONObject other = sprites.getJSONObject("other");
            imageUrl = other.getJSONObject("official-artwork").optString("front_default", "");
            if (imageUrl.isEmpty()) {
                imageUrl = other.getJSONObject("home").optString("front_default", "");
            }
            if (imageUrl.isEmpty()) {
                imageUrl = sprites.optString("front_default", "");
            }
        } catch (JSONException e) {
            imageUrl = "Image not found";
        }

        return new PokemonDetails(name, id, height, weight, species, abilities, hp, attack, defense, spAttack, spDefense, speed, imageUrl);
    }

    private String readFromUrl(String apiUrl) throws Exception {
        URL url = new URL(apiUrl);
        URLConnection connection = url.openConnection();
        connection.connect();

        InputStream is = connection.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder jsonBuilder = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            jsonBuilder.append(line);
        }
        reader.close();

        return jsonBuilder.toString();
    }
}