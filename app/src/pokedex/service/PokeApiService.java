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

public class PokeApiService {

    private static final int CONNECTION_TIMEOUT = 10000; // 10 seconds
    private static final int READ_TIMEOUT = 15000; // 15 seconds

    public List<Pokemon> fetchAllPokemon() throws Exception {
        try {
            String jsonData = read("https://pokeapi.co/api/v2/pokemon?limit=100000&offset=0");
            JSONObject json = new JSONObject(jsonData);
            JSONArray results = json.getJSONArray("results");
            List<Pokemon> list = new ArrayList<>();
            
            for (int i = 0; i < results.length(); i++) {
                try {
                    JSONObject p = results.getJSONObject(i);
                    String url = p.getString("url");
                    String id = url.replaceAll(".*/(\\d+)/?$", "$1");
                    String name = FormatterUtil.formatName(p.getString("name"));
                    list.add(new Pokemon(id, name, url));
                } catch (JSONException e) {
                    // Skip malformed entries but continue processing others
                    System.err.println("Skipping malformed Pokemon entry at index " + i + ": " + e.getMessage());
                }
            }
            
            if (list.isEmpty()) {
                throw new Exception("Nie otrzymano żadnych danych o Pokémonach z API");
            }
            
            return list;
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

    public PokemonDetails fetchPokemonDetails(String url) throws Exception {
        try {
            String jsonData = read(url);
            JSONObject json = new JSONObject(jsonData);
            
            // Extract basic info with error handling
            String name = FormatterUtil.formatName(json.getString("name"));
            int id = json.getInt("id");
            int height = json.getInt("height");
            int weight = json.getInt("weight");
            
            String species;
            try {
                species = FormatterUtil.formatName(json.getJSONObject("species").getString("name"));
            } catch (JSONException e) {
                species = "Nieznany";
            }

            // Extract abilities
            List<String> abilities = new ArrayList<>();
            try {
                JSONArray abilitiesArr = json.getJSONArray("abilities");
                for (int i = 0; i < abilitiesArr.length(); i++) {
                    try {
                        JSONObject ab = abilitiesArr.getJSONObject(i);
                        String n = FormatterUtil.formatName(ab.getJSONObject("ability").getString("name"));
                        abilities.add(ab.getBoolean("is_hidden") ? n + " (ukryta)" : n);
                    } catch (JSONException e) {
                        // Skip malformed ability but continue
                        System.err.println("Skipping malformed ability for Pokemon " + name + ": " + e.getMessage());
                    }
                }
            } catch (JSONException e) {
                abilities.add("Nieznane");
            }

            // Extract stats with defaults
            JSONArray stats = json.getJSONArray("stats");
            int hp = 0, attack = 0, defense = 0, spAttack = 0, spDefense = 0, speed = 0;
            
            for (int i = 0; i < stats.length(); i++) {
                try {
                    JSONObject s = stats.getJSONObject(i);
                    int val = s.getInt("base_stat");
                    String statName = s.getJSONObject("stat").getString("name");
                    
                    switch (statName) {
                        case "hp": hp = val; break;
                        case "attack": attack = val; break;
                        case "defense": defense = val; break;
                        case "special-attack": spAttack = val; break;
                        case "special-defense": spDefense = val; break;
                        case "speed": speed = val; break;
                    }
                } catch (JSONException e) {
                    // Skip malformed stat but continue
                    System.err.println("Skipping malformed stat for Pokemon " + name + ": " + e.getMessage());
                }
            }
            
            return new PokemonDetails(name, id, height, weight, species, abilities, 
                                    hp, attack, defense, spAttack, spDefense, speed);
                                    
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

    private String read(String urlStr) throws Exception {
        try {
            URL url = new URL(urlStr);
            URLConnection conn = url.openConnection();
            
            // Set timeouts
            conn.setConnectTimeout(CONNECTION_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);
            
            // Set user agent to avoid potential blocking
            conn.setRequestProperty("User-Agent", "Pokedex-App/1.0");
            
            conn.connect();
            
            try (InputStream is = conn.getInputStream();
                 BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                
                String result = sb.toString();
                if (result.isEmpty()) {
                    throw new Exception("Serwer zwrócił pustą odpowiedź");
                }
                
                return result;
            }
        } catch (java.net.MalformedURLException e) {
            throw new Exception("Nieprawidłowy adres URL: " + urlStr, e);
        } catch (ConnectException e) {
            throw new ConnectException("Nie można połączyć się z serwerem: " + e.getMessage());
        } catch (UnknownHostException e) {
            throw new UnknownHostException("Nie można znaleźć serwera: " + e.getMessage());
        } catch (SocketTimeoutException e) {
            throw new SocketTimeoutException("Przekroczono czas oczekiwania na odpowiedź serwera");
        } catch (IOException e) {
            throw new IOException("Błąd podczas komunikacji z serwerem: " + e.getMessage());
        }
    }
}