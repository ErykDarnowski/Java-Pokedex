package pokedex.model;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Comprehensive Pokemon data model containing detailed statistics and attributes.
 * This immutable class represents complete Pokemon information retrieved from the API.
 * 
 * @author Eryk Darnowski (7741)
 * @version 1.0.0
 */
public class PokemonDetails {
    
    private final String name;
    private final int id;
    private final int height;
    private final int weight;
    private final String species;
    private final List<String> abilities;
    private final Stats stats;

    /**
     * Constructs a new PokemonDetails instance with complete Pokemon information.
     * 
     * @param name      the Pokemon's display name
     * @param id        the unique Pokemon identifier
     * @param height    the Pokemon's height in decimeters
     * @param weight    the Pokemon's weight in hectograms
     * @param species   the Pokemon's species classification
     * @param abilities list of Pokemon abilities (defensive copy is made)
     * @param hp        base HP stat
     * @param attack    base attack stat
     * @param defense   base defense stat
     * @param spAttack  base special attack stat
     * @param spDefense base special defense stat
     * @param speed     base speed stat
     * @throws IllegalArgumentException if any parameter is invalid
     */
    public PokemonDetails(String name, int id, int height, int weight, String species,
                          List<String> abilities, int hp, int attack, int defense,
                          int spAttack, int spDefense, int speed) {
        this.name = validateString(name, "name");
        this.id = validatePositive(id, "id");
        this.height = validateNonNegative(height, "height");
        this.weight = validateNonNegative(weight, "weight");
        this.species = validateString(species, "species");
        this.abilities = abilities != null ? List.copyOf(abilities) : Collections.emptyList();
        this.stats = new Stats(hp, attack, defense, spAttack, spDefense, speed);
    }

    /**
     * @return the Pokemon's formatted display name
     */
    public String getName() { 
        return name; 
    }

    /**
     * @return the Pokemon's unique identifier
     */
    public int getId() { 
        return id; 
    }

    /**
     * @return the Pokemon's height in decimeters
     */
    public int getHeight() { 
        return height; 
    }

    /**
     * @return the Pokemon's weight in hectograms
     */
    public int getWeight() { 
        return weight; 
    }

    /**
     * @return the Pokemon's species classification
     */
    public String getSpecies() { 
        return species; 
    }

    /**
     * @return an unmodifiable list of Pokemon abilities
     */
    public List<String> getAbilities() { 
        return abilities; 
    }

    /**
     * @return the Pokemon's base HP stat
     */
    public int getHp() { 
        return stats.hp; 
    }

    /**
     * @return the Pokemon's base attack stat
     */
    public int getAttack() { 
        return stats.attack; 
    }

    /**
     * @return the Pokemon's base defense stat
     */
    public int getDefense() { 
        return stats.defense; 
    }

    /**
     * @return the Pokemon's base special attack stat
     */
    public int getSpAttack() { 
        return stats.spAttack; 
    }

    /**
     * @return the Pokemon's base special defense stat
     */
    public int getSpDefense() { 
        return stats.spDefense; 
    }

    /**
     * @return the Pokemon's base speed stat
     */
    public int getSpeed() { 
        return stats.speed; 
    }

    /**
     * Calculates the total base stat points for this Pokemon.
     * 
     * @return the sum of all base stats
     */
    public int getTotalStats() {
        return stats.getTotal();
    }

    /**
     * @return the Pokemon's height in centimeters
     */
    public int getHeightInCm() {
        return height * 10;
    }

    /**
     * @return the Pokemon's weight in kilograms
     */
    public double getWeightInKg() {
        return weight / 10.0;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        PokemonDetails that = (PokemonDetails) obj;
        return id == that.id &&
               height == that.height &&
               weight == that.weight &&
               Objects.equals(name, that.name) &&
               Objects.equals(species, that.species) &&
               Objects.equals(abilities, that.abilities) &&
               Objects.equals(stats, that.stats);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, id, height, weight, species, abilities, stats);
    }

    @Override
    public String toString() {
        return String.format("PokemonDetails{id=%d, name='%s', species='%s', totalStats=%d}", 
                           id, name, species, getTotalStats());
    }

    /**
     * Immutable value object representing Pokemon battle statistics.
     */
    private static class Stats {
        private final int hp;
        private final int attack;
        private final int defense;
        private final int spAttack;
        private final int spDefense;
        private final int speed;

        private Stats(int hp, int attack, int defense, int spAttack, int spDefense, int speed) {
            this.hp = validateNonNegative(hp, "hp");
            this.attack = validateNonNegative(attack, "attack");
            this.defense = validateNonNegative(defense, "defense");
            this.spAttack = validateNonNegative(spAttack, "spAttack");
            this.spDefense = validateNonNegative(spDefense, "spDefense");
            this.speed = validateNonNegative(speed, "speed");
        }

        private int getTotal() {
            return hp + attack + defense + spAttack + spDefense + speed;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            
            Stats stats = (Stats) obj;
            return hp == stats.hp &&
                   attack == stats.attack &&
                   defense == stats.defense &&
                   spAttack == stats.spAttack &&
                   spDefense == stats.spDefense &&
                   speed == stats.speed;
        }

        @Override
        public int hashCode() {
            return Objects.hash(hp, attack, defense, spAttack, spDefense, speed);
        }
    }

    // Validation helper methods
    private static String validateString(String value, String paramName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(paramName + " cannot be null or empty");
        }
        return value;
    }

    private static int validatePositive(int value, String paramName) {
        if (value <= 0) {
            throw new IllegalArgumentException(paramName + " must be positive, got: " + value);
        }
        return value;
    }

    private static int validateNonNegative(int value, String paramName) {
        if (value < 0) {
            throw new IllegalArgumentException(paramName + " cannot be negative, got: " + value);
        }
        return value;
    }
}