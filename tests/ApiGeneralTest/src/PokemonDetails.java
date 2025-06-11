import java.util.List;

public class PokemonDetails {
    private final String name;
    private final int id, height, weight;
    private final String species;
    private final List<String> abilities;
    private final int hp, attack, defense, spAttack, spDefense, speed;
    private final String imageUrl;

    public PokemonDetails(String name, int id, int height, int weight, String species,
                          List<String> abilities, int hp, int attack, int defense,
                          int spAttack, int spDefense, int speed, String imageUrl) {
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
        this.imageUrl = imageUrl;
    }

    @Override
	public String toString() {
	    return String.format(
		"%s #%d\nGatunek: %s\nWzrost: %d cm\nWaga: %.1f kg\nUmiejetnosci: %s\nBazowe statystyki:\n" +
		"  - HP: %d\n  - Atak: %d\n  - Obrona: %d\n  - Szybkosc: %d\n  - Atak specjalny: %d\n  - Obrona specjalna: %d\nIMG URL: %s",
		name, id, species, height * 10, weight / 10.0, abilities,
		hp, attack, defense, speed, spAttack, spDefense, imageUrl
	    );
	}
}