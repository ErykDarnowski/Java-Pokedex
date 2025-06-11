package pokedex.service;

import org.json.JSONArray;
import org.json.JSONObject;
import pokedex.model.Pokemon;
import pokedex.model.PokemonDetails;
import pokedex.util.FormatterUtil;

import java.net.URL;
import java.net.URLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class PokeApiService {

    public List<Pokemon> fetchAllPokemon() throws Exception {
        JSONObject json = new JSONObject(read("https://pokeapi.co/api/v2/pokemon?limit=100000&offset=0"));
        JSONArray results = json.getJSONArray("results");
        List<Pokemon> list = new ArrayList<>();
        for (int i=0;i<results.length();i++){
            JSONObject p = results.getJSONObject(i);
            String url = p.getString("url");
            String id = url.replaceAll(".*/(\\d+)/?$", "$1");
            String name = FormatterUtil.formatName(p.getString("name"));
            list.add(new Pokemon(id, name, url));
        }
        return list;
    }

    public PokemonDetails fetchPokemonDetails(String url) throws Exception {
        JSONObject json = new JSONObject(read(url));
        String name = FormatterUtil.formatName(json.getString("name"));
        int id = json.getInt("id");
        int height = json.getInt("height");
        int weight = json.getInt("weight");
        String species = FormatterUtil.formatName(json.getJSONObject("species").getString("name"));

        List<String> abilities = new ArrayList<>();
        JSONArray abilitiesArr = json.getJSONArray("abilities");
        for (int i=0;i<abilitiesArr.length();i++){
            JSONObject ab = abilitiesArr.getJSONObject(i);
            String n = FormatterUtil.formatName(ab.getJSONObject("ability").getString("name"));
            abilities.add(ab.getBoolean("is_hidden") ? n + " (ukryta)" : n);
        }

        JSONArray stats = json.getJSONArray("stats");
        int hp=0, attack=0, defense=0, spAttack=0, spDefense=0, speed=0;
        for (int i=0;i<stats.length();i++){
            JSONObject s = stats.getJSONObject(i);
            int val = s.getInt("base_stat");
            switch (s.getJSONObject("stat").getString("name")){
                case "hp": hp = val; break;
                case "attack": attack = val; break;
                case "defense": defense = val; break;
                case "special-attack": spAttack = val; break;
                case "special-defense": spDefense = val; break;
                case "speed": speed = val; break;
            }
        }
        return new PokemonDetails(name,id,height,weight,species,abilities,hp,attack,defense,spAttack,spDefense,speed);
    }

    private String read(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        URLConnection conn = url.openConnection();
        conn.connect();
        try (InputStream is = conn.getInputStream();
             BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line=br.readLine())!=null) sb.append(line);
            return sb.toString();
        }
    }
}