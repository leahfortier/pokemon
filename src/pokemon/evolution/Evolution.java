package pokemon.evolution;

import main.Global;
import item.ItemNamesies;
import pokemon.ActivePokemon;
import pokemon.PokemonNamesies;

import java.io.Serializable;
import java.util.Scanner;

public abstract class Evolution implements Serializable {
	private static final long serialVersionUID = 1L;

	public abstract Evolution getEvolution(EvolutionMethod type, ActivePokemon p, ItemNamesies use);
	public abstract PokemonNamesies[] getEvolutions();
	
	public boolean canEvolve() {
		return true;
	}

	public static Evolution readEvolution(Scanner in) {
		String type = in.next();
		
		// TODO: Enum
		switch (type) {
			case "None":
				return new NoEvolution();
			case "Gender":
				return new GenderEvolution(in.next(), readEvolution(in));
			case "Stat":
				return new StatEvolution(in.next(), in.next(), in.next(), readEvolution(in));
			case "Level":
				return new LevelUpEvolution(in.nextInt(), in.nextInt());
			case "Item":
				return new ItemEvolution(in.nextInt(), in.nextLine().trim());
			case "Move":
				return new MoveEvolution(in.nextInt(), in.nextLine().trim());
			case "Multi":
				Evolution[] evolutions = new Evolution[in.nextInt()];
				for (int i = 0; i < evolutions.length; i++) {
					evolutions[i] = readEvolution(in);
				}
				
				return new MultipleEvolution(evolutions);
			default:
				Global.error("Undefined Evolution Type " + type);
				return null; // THIS SHOULDN'T EVEN GET CALLED BECAUSE I JUST DID A SYSTEM.EXIT WOOOO JAVA
		}
	}
}
