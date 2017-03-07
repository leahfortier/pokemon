package trainer.player;

import main.Game;
import pokemon.ActivePokemon;
import trainer.Trainer;
import util.GeneralUtils;
import util.RandomUtils;

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PC implements Serializable {
	private static final long serialVersionUID = 1L;

	public static final int BOX_WIDTH = 6;
	public static final int BOX_HEIGHT = 6;
	private static final int DEFAULT_BOXES = 10;

	private final List<Box> boxes;
	private int currBox;
	private int numPokemon;

	public PC() {
		boxes = new ArrayList<>(DEFAULT_BOXES);
		addBoxes(DEFAULT_BOXES);

		currBox = 0;
		numPokemon = 0;
	}
	
	private void addBox() {
		boxes.add(new Box());
	}
	
	private void addBoxes(int n) {
		for (int i = 0; i < n; i++) {
			addBox();
		}
	}
	
	private boolean expandBoxes() {
		if (numPokemon >= (BOX_HEIGHT*BOX_WIDTH*boxes.size())/2) {
			addBoxes(DEFAULT_BOXES);
			return true;
		}
		
		return false;
	}
	
	// Returns the box you're currently on
	public int getBoxNum() {
		return currBox;
	}
	
	// Returns the list of Pokemon for the current box
	public ActivePokemon[][] getBoxPokemon() {
		return getBoxPokemon(currBox);
	}
	
	// Returns the list of Pokemon for the specified box
	private ActivePokemon[][] getBoxPokemon(int box) {
		return boxes.get(box).pokemon;
	}
	
	public Color getBoxColor() {
		return boxes.get(currBox).color;
	}
	
	public int getNumBoxes() {
		return boxes.size();
	}
	
	// Adds a Pokemon to the next open box
	private void addPokemon(ActivePokemon p) {
		// Get the location of the next open spot
		BoxCoordinate loc = findPokemon(null);
		
		// If we couldn't find a place, create a new box and put it in. This should never be called, but is here to be safe.
		if (loc == null) {
			addBox();
			addPokemon(p);
		}
		
		insertIntoBox(loc, p);
	}
	
	public void depositPokemon(ActivePokemon p) {
		addPokemon(p);
	}

	// Returns the coordinates of the Pokemon in the pc as {boxNum, x, y}, returns {-1, -1, -1} if the Pokemon is not in the box
	private BoxCoordinate findPokemon(ActivePokemon p) {
		for (int boxNum = currBox, n = 0; n < boxes.size(); boxNum++, n++) {
			ActivePokemon[][] box = getBoxPokemon(boxNum);
			for (int i = 0; i < BOX_HEIGHT; i++) {
				for (int j = 0; j < BOX_WIDTH; j++) {
					if (box[i][j] == p) {
						return new BoxCoordinate(boxNum, i, j);
					}
				}
			}			
		}
		
		return null;
	}
	
	public void withdrawPokemon(ActivePokemon p) {
		BoxCoordinate coordinate = findPokemon(p);
		if (coordinate == null) {
			return;
		}
		
		// Make sure they have space
		Player player = Game.getPlayer();
		if (player.getTeam().size() < Trainer.MAX_POKEMON) {
			player.getTeam().add(coordinate.getPokemon());
			coordinate.setPokemon(null);
		}
	}

	// Removes the Pokemon from the trainer and adds it to the box
	public void depositPokemonFromPlayer(ActivePokemon p) {
		Player player = Game.getPlayer();
		if (!player.canDeposit(p)) {
			return;
		}
		
		addPokemon(p);
		player.getTeam().remove(p);
	}

	// Removes the Pokemon from the trainer and adds it to the box at a specific location, fails if another Pokemon is already in this location
	private void depositPokemonFromPlayer(ActivePokemon p, BoxCoordinate coordinate) {
		Player player = Game.getPlayer();
		if (!player.canDeposit(p) || coordinate == null || coordinate.getPokemon() != null) {
			return;
		}
	
		insertIntoBox(coordinate, p);
		player.getTeam().remove(p);
	}
	
	public void depositPokemonFromPlayer(ActivePokemon p, int x, int y) {
		depositPokemonFromPlayer(p, new BoxCoordinate(currBox, x, y));
	}
	
	// Switching two Pokemon, where the second Pokemon is inside the box
	public void switchPokemon(ActivePokemon p, int i, int j) {
		Player player = Game.getPlayer();
		int index = player.getTeam().indexOf(p);
		
		// The first Pokemon to be switched is in the user's party
		if (index != -1) {
			boxPartySwap(new BoxCoordinate(currBox, i, j), index);
		}
		// Swapping two Pokemon in the box
		else {
			BoxCoordinate coordinate = findPokemon(p);
			if (coordinate == null) {
				return;
			}
			
			ActivePokemon[][] box = getBoxPokemon();
			ActivePokemon switchee = box[i][j];
			
			box[i][j] = p;
			coordinate.setPokemon(switchee);
		}
	}
	
	// Switching two Pokemon, where the second Pokemon is in the player's party
	public void switchPokemon(ActivePokemon p, int i) {
		Player player = Game.getPlayer();
		int index = player.getTeam().indexOf(p);
		
		// The first Pokemon to be switched is also in the user's party
		if (index != -1) {
			player.swapPokemon(i, index);
		}
		// Swap box Pokemon with party Pokemon
		else {
			boxPartySwap(findPokemon(p), i);
		}
	}
	
	private void boxPartySwap(BoxCoordinate coordinate, int partyIndex) {
		if (coordinate == null) {
			return;
		}

		Player player = Game.getPlayer();

		ActivePokemon boxPokemon = coordinate.getPokemon();
		ActivePokemon partyPokemon = player.getTeam().get(partyIndex);
		
		// Swapping with an empty space -- same as depositing
		if (boxPokemon == null) {
			depositPokemonFromPlayer(partyPokemon, coordinate);
		}
		else {
			int eggs = player.totalEggs();
			if (boxPokemon.isEgg()) {
				eggs++;
			}
			if (partyPokemon.isEgg()) {
				eggs--;
			}
			
			// If swapping these Pokemon will make the player only have eggs, we can't do it.
			if (eggs == player.getTeam().size()) {
				return;
			}

			coordinate.setPokemon(partyPokemon);
			player.getTeam().set(partyIndex, boxPokemon);
		}
	}
	
	// If you're adding a Pokemon to the PC, this should be called internally. All the error checking and such
	// should be handled by the publicly accessible versions.
	private void insertIntoBox(BoxCoordinate coordinate, ActivePokemon p) {
		if (coordinate == null) {
			return;
		}
		
		currBox = coordinate.boxNum;
		p.fullyHeal();
		coordinate.setPokemon(p);
		++numPokemon;
		expandBoxes();
	}
	
	// Removes a Pokemon forevers from the box 
	private void removePokemon(BoxCoordinate coordinate) {
		coordinate.setPokemon(null);
		--numPokemon;
	}
	
	// Release a Pokemon forevers
	public void releasePokemon(ActivePokemon p) {
		Player player = Game.getPlayer();
		if (player.getTeam().contains(p)) {
			player.getTeam().remove(p);
			return;
		}
		
		BoxCoordinate coordinate = findPokemon(p);
		removePokemon(coordinate);
	}
	
	public void incrementBox(int delta) {
		this.currBox = GeneralUtils.wrapIncrement(this.currBox, delta, boxes.size());
	}

	private class BoxCoordinate {
		private int boxNum;
		private int x;
		private int y;

		BoxCoordinate(int boxNum, int x, int y) {
			this.boxNum = boxNum;
			this.x = x;
			this.y = y;
		}

		private ActivePokemon getPokemon() {
			return getBoxPokemon(boxNum)[x][y];
		}

		private void setPokemon(ActivePokemon pokemon) {
			boxes.get(boxNum).pokemon[x][y] = pokemon;
		}
	}

	private static class Box implements Serializable {
		private static final long serialVersionUID = 1L;
		
		private final ActivePokemon[][] pokemon;
		private final Color color;
		
		public Box() {
			pokemon = new ActivePokemon[BOX_HEIGHT][BOX_WIDTH];
			color = new Color(RandomUtils.getRandomInt(255), RandomUtils.getRandomInt(255), RandomUtils.getRandomInt(255));
		}
	}
}
