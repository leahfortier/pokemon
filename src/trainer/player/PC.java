package trainer.player;

import main.Game;
import pokemon.PartyPokemon;
import trainer.Trainer;
import util.GeneralUtils;
import util.RandomUtils;
import util.serialization.Serializable;

import java.awt.Color;
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
    public PartyPokemon[][] getBoxPokemon() {
        return getBoxPokemon(currBox);
    }

    // Returns the list of Pokemon for the specified box
    private PartyPokemon[][] getBoxPokemon(int box) {
        return boxes.get(box).pokemon;
    }

    public Color getBoxColor() {
        return boxes.get(currBox).color;
    }

    public int getNumBoxes() {
        return boxes.size();
    }

    // Adds a Pokemon to the next open box
    private void addPokemon(PartyPokemon p) {
        // Get the location of the next open spot
        BoxCoordinate loc = findPokemon(null);

        // If we couldn't find a place, create a new box and put it in. This should never be called, but is here to be safe.
        if (loc == null) {
            addBox();
            addPokemon(p);
        } else {
            insertIntoBox(loc, p);
        }
    }

    public void depositPokemon(PartyPokemon p) {
        addPokemon(p);
    }

    // Returns the coordinates of the Pokemon in the pc as {boxNum, x, y}, returns null if the Pokemon is not in the box
    private BoxCoordinate findPokemon(PartyPokemon p) {
        for (int boxNum = currBox, n = 0; n < boxes.size(); boxNum++, n++) {
            PartyPokemon[][] box = getBoxPokemon(boxNum);
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

    public void withdrawPokemon(PartyPokemon p) {
        BoxCoordinate coordinate = findPokemon(p);
        if (coordinate == null) {
            return;
        }

        // Make sure they have space
        Player player = Game.getPlayer();
        if (player.getTeam().size() < Trainer.MAX_POKEMON) {
            player.getTeam().add(coordinate.getPokemon());
            coordinate.removePokemon();
        }
    }

    // Removes the Pokemon from the trainer and adds it to the box
    public void depositPokemonFromPlayer(PartyPokemon p) {
        Player player = Game.getPlayer();
        if (!player.canDeposit(p)) {
            return;
        }

        addPokemon(p);
        player.getTeam().remove(p);
    }

    // Removes the Pokemon from the trainer and adds it to the box at a specific location, fails if another Pokemon is already in this location
    private void depositPokemonFromPlayer(PartyPokemon p, BoxCoordinate coordinate) {
        Player player = Game.getPlayer();
        if (!player.canDeposit(p) || coordinate == null || coordinate.getPokemon() != null) {
            return;
        }

        insertIntoBox(coordinate, p);
        player.getTeam().remove(p);
    }

    public void depositPokemonFromPlayer(PartyPokemon p, int x, int y) {
        depositPokemonFromPlayer(p, new BoxCoordinate(currBox, x, y));
    }

    // Switching two Pokemon, where the second Pokemon is inside the box
    public void switchPokemon(PartyPokemon p, int i, int j) {
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

            PartyPokemon[][] box = getBoxPokemon();
            PartyPokemon switchee = box[i][j];

            box[i][j] = p;
            coordinate.setPokemon(switchee);
        }
    }

    // Switching two Pokemon, where the second Pokemon is in the player's party
    public void switchPokemon(PartyPokemon p, int i) {
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

        PartyPokemon boxPokemon = coordinate.getPokemon();
        PartyPokemon partyPokemon = player.getTeam().get(partyIndex);

        // Swapping with an empty space -- same as depositing
        if (boxPokemon == null) {
            depositPokemonFromPlayer(partyPokemon, coordinate);
        } else {
            // TODO: Should this also be checking fainted Pokemon?
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
    private void insertIntoBox(BoxCoordinate coordinate, PartyPokemon p) {
        if (coordinate == null) {
            return;
        }

        currBox = coordinate.boxNum;
        p.fullyHeal();
        coordinate.setPokemon(p);
        numPokemon++;
        expandBoxes();
    }

    // Release a Pokemon forevers
    public void releasePokemon(PartyPokemon p) {
        Player player = Game.getPlayer();
        if (player.getTeam().contains(p)) {
            player.getTeam().remove(p);
            return;
        }

        BoxCoordinate coordinate = findPokemon(p);
        coordinate.removePokemon();
    }

    public void incrementBox(int delta) {
        this.currBox = GeneralUtils.wrapIncrement(this.currBox, delta, boxes.size());
    }

    private static class Box implements Serializable {
        private static final long serialVersionUID = 1L;

        private final PartyPokemon[][] pokemon;
        private final Color color;

        public Box() {
            pokemon = new PartyPokemon[BOX_HEIGHT][BOX_WIDTH];
            color = new Color(RandomUtils.getRandomInt(255), RandomUtils.getRandomInt(255), RandomUtils.getRandomInt(255));
        }
    }

    private class BoxCoordinate {
        private final int boxNum;
        private final int x;
        private final int y;

        BoxCoordinate(int boxNum, int x, int y) {
            this.boxNum = boxNum;
            this.x = x;
            this.y = y;
        }

        private PartyPokemon getPokemon() {
            return getBoxPokemon(boxNum)[x][y];
        }

        private void setPokemon(PartyPokemon pokemon) {
            boxes.get(boxNum).pokemon[x][y] = pokemon;
        }

        // Removes a Pokemon forevers from the box
        private void removePokemon() {
            this.setPokemon(null);
            numPokemon--;
        }
    }
}
