package pokedex.model;

import java.util.Objects;

/**
 * Represents a basic Pokemon entity with essential identification information.
 * This class serves as a lightweight data transfer object for Pokemon list views.
 * 
 * @author Eryk Darnowski (7741)
 * @version 1.0.0
 */
public class Pokemon {
    
    private final String id;
    private final String name;
    private final String url;

    /**
     * Constructs a new Pokemon instance.
     * 
     * @param id   the unique Pokemon identifier
     * @param name the Pokemon's display name
     * @param url  the API endpoint URL for detailed Pokemon data
     * @throws IllegalArgumentException if any parameter is null or empty
     */
    public Pokemon(String id, String name, String url) {
        this.id = validateParameter(id, "id");
        this.name = validateParameter(name, "name");
        this.url = validateParameter(url, "url");
    }

    /**
     * @return the Pokemon's unique identifier
     */
    public String getId() { 
        return id; 
    }

    /**
     * @return the Pokemon's formatted display name
     */
    public String getName() { 
        return name; 
    }

    /**
     * @return the API endpoint URL for this Pokemon's detailed information
     */
    public String getUrl() { 
        return url; 
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Pokemon pokemon = (Pokemon) obj;
        return Objects.equals(id, pokemon.id) &&
               Objects.equals(name, pokemon.name) &&
               Objects.equals(url, pokemon.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, url);
    }

    @Override
    public String toString() {
        return String.format("Pokemon{id='%s', name='%s'}", id, name);
    }

    /**
     * Validates that a parameter is not null or empty.
     * 
     * @param value the parameter value to validate
     * @param paramName the parameter name for error messages
     * @return the validated parameter value
     * @throws IllegalArgumentException if the parameter is null or empty
     */
    private String validateParameter(String value, String paramName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(paramName + " cannot be null or empty");
        }
        return value;
    }
}