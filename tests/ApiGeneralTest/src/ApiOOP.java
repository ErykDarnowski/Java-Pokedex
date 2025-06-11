import java.util.List;

public class ApiOOP {

	public static void main(String[] args) {
		PokeApiService service = new PokeApiService();

		try {
		    System.out.println("Fetching search data for first 10 Pokemons:");
		    List<Pokemon> pokemons = service.fetchAllPokemon();
		    for (int i = 0; i < 10; i++) {
				Pokemon p = pokemons.get(i);
				System.out.println((i + 1) + ". " + p);
		    }

		    System.out.println("\nFetching details for first Pokemon:");
		    PokemonDetails details = service.fetchPokemonDetails(pokemons.get(0).getUrl());
		    System.out.println(details);
		} catch (Exception e) {
		    System.out.println("Error: " + e.getMessage());
		}
    }
	
}
