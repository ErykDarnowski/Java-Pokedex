package pokedex.model;

import java.util.List;

public class PokemonDetails {
    private final String name;
    private final int id, height, weight;
    private final String species;
    private final List<String> abilities;
    private final int hp, attack, defense, spAttack, spDefense, speed;

    public PokemonDetails(String name, int id, int height, int weight, String species,
                          List<String> abilities, int hp, int attack, int defense,
                          int spAttack, int spDefense, int speed) {
        this.name = name;
        this.id = id;
        this.height = height;
        this.weight = weight;
        this.species = species;
        this.abilities = abilities;
        this.hp = hp;
        this.attack = attack;
        this.defense = defense;
        this.spAttack = spAttack;
        this.spDefense = spDefense;
        this.speed = speed;
    }

    public String getName() { return name; }
    public int getId() { return id; }
    public int getHeight() { return height; }
    public int getWeight() { return weight; }
    public String getSpecies() { return species; }
    public List<String> getAbilities() { return abilities; }
    public int getHp() { return hp; }
    public int getAttack() { return attack; }
    public int getDefense() { return defense; }
    public int getSpAttack() { return spAttack; }
    public int getSpDefense() { return spDefense; }
    public int getSpeed() { return speed; }
}