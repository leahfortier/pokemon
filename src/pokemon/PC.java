package pokemon;

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import main.Global;
import trainer.CharacterData;
import trainer.Trainer;

public class PC implements Serializable {
	private static final long serialVersionUID = 1L;

	public static final int BOX_WIDTH = 6;
	public static final int BOX_HEIGHT = 6;
	
	private int currBox;
	private int numPokemon;
	private final List<Box> boxes;
	
	public PC() {
		currBox = numPokemon = 0;
		boxes = new ArrayList<>(10);
		addBoxes(10); // TODO: constant
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
			addBoxes(10);
			return true;
		}
		
		return false;
	}
	
	// Returns the box you're currently on
	public int getBoxNum() {
		return currBox;
	}
	
	public void changeBox(int box) {
		currBox = box;
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
		int[] loc = findPokemon(null);
		int boxNum = loc[0], x = loc[1], y = loc[2];
		
		// If we couldn't find a place, create a new box and put it in. This should never be called, but is here to be safe.
		if (!inBounds(x, y)) {
			addBox();
			boxNum = boxes.size() - 1;
			x = 0;
			y = 0;
		}
		
		insertIntoBox(boxNum, x, y, p);
	}
	
	public void depositPokemon(ActivePokemon p) {
		addPokemon(p);
	}

	// TODO: Should return an object instead
	// Returns the coordinates of the Pokemon in the pc as {boxNum, x, y}, returns {-1, -1, -1} if the Pokemon is not in the box
	private int[] findPokemon(ActivePokemon p) {
		for (int boxNum = currBox, n = 0; n < boxes.size(); boxNum++, n++) {
			ActivePokemon[][] box = getBoxPokemon(boxNum);
			for (int i = 0; i < BOX_HEIGHT; i++) {
				for (int j = 0; j < BOX_WIDTH; j++) {
					if (box[i][j] == p) {
						return new int[] { boxNum, i, j };
					}
				}
			}			
		}
		
		return new int[] { -1, -1, -1 };
	}
	
	public void withdrawPokemon(CharacterData player, ActivePokemon p) {
		int[] loc = findPokemon(p);
		int x = loc[1], y = loc[2];
		
		if (!inBounds(x, y)) {
			return;
		}
		
		// Make sure they have space
		if (player.getTeam().size() < Trainer.MAX_POKEMON) {
			ActivePokemon[][] box = getBoxPokemon();
			player.getTeam().add(box[x][y]);
			box[x][y] = null;
		}
	}
	
	// Removes the Pokemon from the trainer and adds it to the box
	public void depositPokemon(CharacterData player, ActivePokemon p) {
		if (!player.canDeposit(p)) {
			return;
		}
		
		addPokemon(p);
		player.getTeam().remove(p);
	}

	// TODO: when is boxNum not the current box?
	// Removes the Pokemon from the trainer and adds it to the box at a specific location, fails if another Pokemon is already in this location
	private void depositPokemon(CharacterData player, ActivePokemon p, int boxNum, int x, int y) {
		if (!player.canDeposit(p) || !inBounds(x, y) || getBoxPokemon()[x][y] != null) {
			return;
		}
	
		insertIntoBox(boxNum, x, y, p);
		player.getTeam().remove(p);
	}
	
	public void depositPokemon(CharacterData player, ActivePokemon p, int x, int y) {
		depositPokemon(player, p, currBox, x, y);
	}
	
	// Switching two Pokemon, where the second Pokemon is inside the box
	public void switchPokemon(CharacterData player, ActivePokemon p, int i, int j) {
		int index = player.getTeam().indexOf(p);
		
		// The first Pokemon to be switched is in the user's party
		if (index != -1) {
			boxPartySwap(currBox, i, j, player, index);
		}
		// Swapping two Pokemon in the box
		else {
			int[] loc = findPokemon(p);
			int boxNum = loc[0], x = loc[1], y = loc[2];
			
			if (!inBounds(x, y)) {
				return;
			}
			
			ActivePokemon[][] box = getBoxPokemon();
			ActivePokemon switchee = box[i][j];
			
			box[i][j] = p;
			getBoxPokemon(boxNum)[x][y] = switchee;
		}
	}
	
	// Switching two Pokemon, where the second Pokemon is in the player's party
	public void switchPokemon(CharacterData player, ActivePokemon p, int i) {
		int index = player.getTeam().indexOf(p);
		
		// The first Pokemon to be switched is also in the user's party
		if (index != -1) {
			player.swapPokemon(i, index);
		}
		// Swap box Pokemon with party Pokemon
		else {
			int[] loc = findPokemon(p);
			boxPartySwap(loc[0], loc[1], loc[2], player, i);
		}
	}
	
	private void boxPartySwap(int boxNum, int i, int j, CharacterData player, int partyIndex) {
		if (!inBounds(i, j)) {
			return;
		}
		
		ActivePokemon[][] box = getBoxPokemon(boxNum);
		ActivePokemon boxPokemon = box[i][j], partyPokemon = player.getTeam().get(partyIndex);
		
		// Swapping with an empty space -- same as depositing
		if (boxPokemon == null) {
			depositPokemon(player, partyPokemon, boxNum, i, j);
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
			
			box[i][j] = partyPokemon;
			player.getTeam().set(partyIndex, boxPokemon);
		}
	}
	
	// If you're adding a Pokemon to the PC, this should be called internally. All the error checking and such
	// should be handled by the publicly accessible versions.
	private void insertIntoBox(int boxNum, int i, int j, ActivePokemon p) {
		if (!inBounds(i, j)) {
			Global.error("Attempting to insert into box at invalid indices.");
		}
		
		currBox = boxNum;
		p.fullyHeal();
		boxes.get(boxNum).pokemon[i][j] = p;
		++numPokemon;
		expandBoxes();
	}
	
	// Removes a Pokemon forevers from the box 
	private void removePokemon(int boxNum, int i, int j) {
		getBoxPokemon(boxNum)[i][j] = null;
		--numPokemon;
	}
	
	// Release a Pokemon forevers
	public void releasePokemon(CharacterData player, ActivePokemon p) {
		if (player.getTeam().contains(p)) {
			player.getTeam().remove(p);
			return;
		}
		
		int[] loc = findPokemon(p);
		removePokemon(loc[0], loc[1], loc[2]);
	}
	
	private boolean inBounds(int i, int j) {
		return i >= 0 && i < BOX_HEIGHT && j >= 0 && j < BOX_WIDTH;
	}
	
	public void nextBox() {
		currBox += 1;
		currBox %= boxes.size();
	}

	// TODO: Should have a generic method for incrementing pages that takes in the max size
	public void prevBox() {
		currBox += boxes.size() - 1;
		currBox %= boxes.size();
	}
	
	private static class Box implements Serializable {
		private static final long serialVersionUID = 1L;
		
		private ActivePokemon[][] pokemon;
		private Color color;
		
		public Box() {
			pokemon = new ActivePokemon[BOX_HEIGHT][BOX_WIDTH];
			color = new Color((int)(Math.random()*255),(int)(Math.random()*255), (int)(Math.random()*255)); 
		}
	}
}
