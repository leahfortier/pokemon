package pokemon.breeding;

import main.Game;
import main.Global;
import message.Messages;
import pokemon.ActivePokemon;
import trainer.player.Player;
import util.PokeString;
import util.StringUtils;

import java.io.Serializable;

public class DayCareCenter implements Serializable {
    private ActivePokemon first;
    private ActivePokemon second;

    private Compatibility compatibility;
    private ActivePokemon eggy;
    private int steps;

    public DayCareCenter() {
        this.reset();
    }

    public void giveEggy() {
        Messages.add("Eggy! You want? Yee. Here go.");
        Game.getPlayer().addPokemon(this.eggy);
        this.reset();
    }

    public boolean hasEggy() {
        return eggy != null;
    }

    public void step() {
        steps++;
        if (!hasEggy() && steps%256 == 0 && compatibility.eggChanceTest()) {
            eggy = Breeding.breed(first, second);
        }
    }

    public String getPokemonPresentMessage() {
        if (first == null && second == null) {
            return StringUtils.empty();
        }

        if (first != null && second != null) {
            return "Your " + first.getName() + " and your " + second.getName() + " are doing just fine.";
        }

        return "Your " + (first == null ? second : first).getName() + " is doing just fine.";
    }

    public String getCompatibilityMessage() {
        return compatibility.getMessage();
    }

    public String deposit(ActivePokemon toDeposit) {
        Player player = Game.getPlayer();
        if (!this.canDeposit(toDeposit)) {
            Global.error("Invalid deposit Pokemon.");
            return null;
        }

        if (first == null) {
            first = toDeposit;
        }
        else if (second == null) {
            second = toDeposit;
        }
        else {
            Global.error("Cannot deposit a Pokemon into a full Day Care center.");
        }

        player.getTeam().remove(toDeposit);
        toDeposit.fullyHeal();

        this.reset();

        return "Okay, we'll look after your " + toDeposit.getName() + " for a while.";
    }

    private void reset() {
        compatibility = Compatibility.getCompatibility(first, second);
        steps = 0;
        eggy = null;
    }

    public String withdraw(ActivePokemon pokemon) {
        if (pokemon == first) {
            return withdraw(true);
        }
        else if (pokemon == second) {
            return withdraw(false);
        }
        else {
            Global.error("Cannot withdraw a Pokemon that is not in the day care center...");
            return null;
        }
    }

    public String withdraw(boolean isFirstPokemon) {
        final ActivePokemon withdrawPokemon;
        if (isFirstPokemon) {
            withdrawPokemon = first;
            first = null;
        } else {
            withdrawPokemon = second;
            second = null;
        }

        Player player = Game.getPlayer();
        player.addPokemon(withdrawPokemon, false);
        player.sucksToSuck(500); // TODO: Would like this to be a function of the number of eggs

        this.reset();

        return "Took back " + withdrawPokemon.getName() + " back for 500 " + PokeString.POKEDOLLARS + ".";
    }

    public boolean canDeposit(ActivePokemon pokemon) {
        return (first == null || second == null) && !pokemon.isEgg() && Game.getPlayer().canDeposit(pokemon);
    }

    public ActivePokemon getFirstPokemon() {
        return this.first;
    }

    public ActivePokemon getSecondPokemon() {
        return this.second;
    }
}
