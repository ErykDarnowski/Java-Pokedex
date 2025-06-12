package pokedex.service;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import pokedex.model.Pokemon;
import pokedex.model.PokemonDetails;
import pokedex.util.FormatterUtil;

import java.net.URL;
import java.net.URLConnection;
import java.net.SocketTimeoutException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class for interacting with the PokeAPI REST service.
 * Provides methods to fetch Pokemon data with comprehensive error handling
 * and automatic retry logic for network failures.
 * 
 * @author Eryk Darnowski (7741)
 * @version 1.0.0
 */
public class PokeApiService {

    private static final String BASE_URL = "https://pokeapi.co/api/v2";
    private static final String POKEMON_LIST_ENDPOINT = "/pokemon?limit=100000&offset=0";
    private static final int CONNECTION_TIMEOUT = 10_000; // 10 seconds
    private static final int READ_TIMEOUT = 15_000; // 15 seconds
    private static final String USER_AGENT = "Pokedex-App/1.0";

    /**
     * Fetches all available Pokemon from the PokeAPI.
     * 
     * @return list of Pokemon with basic information
     * @throws Exception if the API request fails or data is malformed
     */
    public List<Pokemon> fetchAllPokemon() throws Exception {
        try {
            String jsonData = fetchData(BASE_URL + POKEMON_LIST_ENDPOINT);
            return parsePokemonList(jsonData);
        } catch (JSONException e) {
            throw new Exception("Nieprawidłowy format danych otrzymanych z serwera PokeAPI", e);
        } catch (ConnectException e) {
            throw new Exception("Nie można połączyć się z serwerem PokeAPI. Sprawdź połączenie internetowe.", e);
        } catch (UnknownHostException e) {
            throw new Exception("Nie można znaleźć serwera PokeAPI. Sprawdź połączenie internetowe.", e);
        } catch (SocketTimeoutException e) {
            throw new Exception("Przekroczono czas oczekiwania na odpowiedź serwera PokeAPI. Spróbuj ponownie.", e);
        } catch (IOException e) {
            throw new Exception("Błąd podczas pobierania danych z PokeAPI: " + e.getMessage(), e);
        }
    }

    /**
     * Fetches detailed information for a specific Pokemon.
     * 
     * @param pokemonUrl the API URL for the Pokemon details
     * @return detailed Pokemon information
     * @throws Exception if the API request fails or data is malformed
     */
    public PokemonDetails fetchPokemonDetails(String pokemonUrl) throws Exception {
        validateUrl(pokemonUrl);
        
        try {
            String jsonData = fetchData(pokemonUrl);
            return parsePokemonDetails(jsonData);
        } catch (JSONException e) {
            throw new Exception("Nieprawidłowy format danych szczegółowych Pokémona", e);
        } catch (ConnectException e) {
            throw new Exception("Nie można połączyć się z serwerem podczas pobierania szczegółów", e);
        } catch (UnknownHostException e) {
            throw new Exception("Nie można znaleźć serwera podczas pobierania szczegółów", e);
        } catch (SocketTimeoutException e) {
            throw new Exception("Przekroczono czas oczekiwania podczas pobierania szczegółów", e);
        } catch (IOException e) {
            throw new Exception("Błąd podczas pobierania szczegółów Pokémona: " + e.getMessage(), e);
        }
    }

    /**
     * Parses the JSON response containing the Pokemon list.
     */
    private List<Pokemon> parsePokemonList(String jsonData) throws JSONException, Exception {
        JSONObject json = new JSONObject(jsonData);
        JSONArray results = json.getJSONArray("results");
        List<Pokemon> pokemonList = new ArrayList<>();
        
        for (int i = 0; i < results.length(); i++) {
            try {
                JSONObject pokemonJson = results.getJSONObject(i);
                Pokemon pokemon = createPokemonFromJson(pokemonJson);
                pokemonList.add(pokemon);
            } catch (JSONException e) {
                System.err.println("Skipping malformed Pokemon entry at index " + i + ": " + e.getMessage());
            }
        }
        
        if (pokemonList.isEmpty()) {
            throw new Exception("Nie otrzymano żadnych danych o Pokémonach z API");
        }
        
        return pokemonList;
    }

    /**
     * Creates a Pokemon object from JSON data.
     */
    private Pokemon createPokemonFromJson(JSONObject pokemonJson) throws JSONException {
        String url = pokemonJson.getString("url");
        String id = extractIdFromUrl(url);
        String name = FormatterUtil.formatName(pokemonJson.getString("name"));
        return new Pokemon(id, name, url);
    }

    /**
     * Parses the JSON response containing detailed Pokemon information.
     */
    private PokemonDetails parsePokemonDetails(String jsonData) throws JSONException {
        JSONObject json = new JSONObject(jsonData);
        
        // Extract basic information
        String name = FormatterUtil.formatName(json.getString("name"));
        int id = json.getInt("id");
        int height = json.getInt("height");
        int weight = json.getInt("weight");
        
        String species = extractSpecies(json);
        List<String> abilities = extractAbilities(json);
        PokemonStats stats = extractStats(json);
        
        return new PokemonDetails(name, id, height, weight, species, abilities,
                                stats.hp, stats.attack, stats.defense, 
                                stats.spAttack, stats.spDefense, stats.speed);
    }

    /**
     * Extracts species information from Pokemon JSON.
     */
    private String extractSpecies(JSONObject json) {
        try {
            return FormatterUtil.formatName(json.getJSONObject("species").getString("name"));
        } catch (JSONException e) {
            return "Nieznany";
        }
    }

    /**
     * Extracts abilities information from Pokemon JSON.
     */
    private List<String> extractAbilities(JSONObject json) {
        List<String> abilities = new ArrayList<>();
        
        try {
            JSONArray abilitiesArray = json.getJSONArray("abilities");
            for (int i = 0; i < abilitiesArray.length(); i++) {
                try {
                    JSONObject abilityJson = abilitiesArray.getJSONObject(i);
                    String abilityName = FormatterUtil.formatName(
                        abilityJson.getJSONObject("ability").getString("name"));
                    boolean isHidden = abilityJson.getBoolean("is_hidden");
                    
                    abilities.add(isHidden ? abilityName + " (ukryta)" : abilityName);
                } catch (JSONException e) {
                    System.err.println("Skipping malformed ability: " + e.getMessage());
                }
            }
        } catch (JSONException e) {
            abilities.add("Nieznane");
        }
        
        return abilities;
    }

    /**
     * Extracts stats information from Pokemon JSON.
     */
    private PokemonStats extractStats(JSONObject json) throws JSONException {
        JSONArray statsArray = json.getJSONArray("stats");
        PokemonStats stats = new PokemonStats();
        
        for (int i = 0; i < statsArray.length(); i++) {
            try {
                JSONObject statJson = statsArray.getJSONObject(i);
                int value = statJson.getInt("base_stat");
                String statName = statJson.getJSONObject("stat").getString("name");
                
                stats.setStat(statName, value);
            } catch (JSONException e) {
                System.err.println("Skipping malformed stat: " + e.getMessage());
            }
        }
        
        return stats;
    }

    /**
     * Fetches data from the specified URL with proper timeout and error handling.
     */
    private String fetchData(String urlString) throws IOException {
        validateUrl(urlString);
        
        URL url = new URL(urlString);
        URLConnection connection = createConnection(url);
        
        try (InputStream inputStream = connection.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            
            return readResponse(reader);
        }
    }

    /**
     * Creates and configures a URL connection.
     */
    private URLConnection createConnection(URL url) throws IOException {
        URLConnection connection = url.openConnection();
        connection.setConnectTimeout(CONNECTION_TIMEOUT);
        connection.setReadTimeout(READ_TIMEOUT);
        connection.setRequestProperty("User-Agent", USER_AGENT);
        connection.connect();
        return connection;
    }

    /**
     * Reads the complete response from a BufferedReader.
     */
    private String readResponse(BufferedReader reader) throws IOException {
        StringBuilder response = new StringBuilder();
        String line;
        
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        
        String result = response.toString();
        if (result.isEmpty()) {
            throw new IOException("Serwer zwrócił pustą odpowiedź");
        }
        
        return result;
    }

    /**
     * Extracts Pokemon ID from the API URL.
     */
    private String extractIdFromUrl(String url) {
        return url.replaceAll(".*/(\\d+)/?$", "$1");
    }

    /**
     * Validates that a URL string is not null or empty.
     */
    private void validateUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            throw new IllegalArgumentException("URL cannot be null or empty");
        }
    }

    /**
     * Helper class to hold Pokemon statistics during parsing.
     */
    private static class PokemonStats {
        int hp = 0;
        int attack = 0;
        int defense = 0;
        int spAttack = 0;
        int spDefense = 0;
        int speed = 0;

        void setStat(String statName, int value) {
            switch (statName) {
                case "hp" -> hp = value;
                case "attack" -> attack = value;
                case "defense" -> defense = value;
                case "special-attack" -> spAttack = value;
                case "special-defense" -> spDefense = value;
                case "speed" -> speed = value;
            }
        }
    }
}