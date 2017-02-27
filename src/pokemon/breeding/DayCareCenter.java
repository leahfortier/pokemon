package pokemon.breeding;

import main.Game;
import main.Global;
import map.triggers.Trigger;
import map.triggers.TriggerType;
import mapMaker.dialogs.action.trigger.TriggerActionType;
import message.Messages;
import pattern.action.ActionMatcher;
import pattern.action.ChoiceActionMatcher;
import pattern.action.ChoiceActionMatcher.ChoiceMatcher;
import pattern.action.TriggerActionMatcher;
import pokemon.ActivePokemon;
import trainer.Player;
import util.SerializationUtils;
import util.PokeString;
import util.StringUtils;

import java.io.Serializable;
import java.util.List;

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

    public Trigger getDepositTrigger() {
        // Center is full -- display invalid message
        if (first != null && second != null) {
            return TriggerType.DIALOGUE.createTrigger(
                    "Center is already full. Withdraw a " + PokeString.POKEMON + " first to deposit."
            );
        }

        Player player = Game.getPlayer();
        List<ActivePokemon> team = player.getTeam();

        // Two Pokemon in center -- choice option
        ChoiceMatcher[] choices = new ChoiceMatcher[team.size() + 1];
        for (int i = 0; i < team.size(); i++) {
            ActionMatcher actionMatcher = new ActionMatcher();
            actionMatcher.setTrigger(new TriggerActionMatcher(TriggerActionType.DAY_CARE_DEPOSIT, i + ""));

            ActivePokemon pokemon = team.get(i);
            choices[i] = new ChoiceMatcher(
                    pokemon.getName() + " " + pokemon.getGenderString(),
                    new ActionMatcher[] { actionMatcher }
            );
        }

        ActionMatcher cancelAction = new ActionMatcher();
        cancelAction.setTrigger(new TriggerActionMatcher(TriggerActionType.DIALOGUE, StringUtils.empty()));
        choices[team.size()] = new ChoiceMatcher("Cancel", new ActionMatcher[] { cancelAction });

        ChoiceActionMatcher choice = new ChoiceActionMatcher(
                "Which " + PokeString.POKEMON + " would you like to deposit?",
                choices
        );

        return TriggerType.CHOICE.createTrigger(SerializationUtils.getJson(choice));
    }


    public Trigger getWithdrawTrigger() {
        // Both are null -- display invalid message
        if (first == null && second == null) {
            return TriggerType.DIALOGUE.createTrigger("No " + PokeString.POKEMON + " to withdraw.");
        }

        // Only one pokemon here
        if (first == null) {
            return TriggerType.DAY_CARE_WITHDRAW.createTrigger("false");
        } else if (second == null) {
            return TriggerType.DAY_CARE_WITHDRAW.createTrigger("true");
        }

        // Two Pokemon in center -- choice option
        ChoiceMatcher chooseFirst = getChoice(true);
        ChoiceMatcher chooseSecond = getChoice(false);

        ActionMatcher cancelAction = new ActionMatcher();
        cancelAction.setTrigger(new TriggerActionMatcher(TriggerActionType.DIALOGUE, StringUtils.empty()));
        ChoiceMatcher chooseCancel = new ChoiceMatcher("Cancel", new ActionMatcher[] { cancelAction });

        ChoiceActionMatcher choice = new ChoiceActionMatcher(
                "Which " + PokeString.POKEMON + " would you like to take back?",
                new ChoiceMatcher[] { chooseFirst, chooseSecond, chooseCancel }
        );

        return TriggerType.CHOICE.createTrigger(SerializationUtils.getJson(choice));
    }

    private ChoiceMatcher getChoice(boolean isFirst) {
        ActionMatcher actionMatcher = new ActionMatcher();
        actionMatcher.setTrigger(new TriggerActionMatcher(TriggerActionType.DAY_CARE_WITHDRAW, isFirst + ""));

        ActivePokemon pokemon = isFirst ? first : second;
        return new ChoiceMatcher(
                pokemon.getName() + " " + pokemon.getGenderString(),
                new ActionMatcher[] { actionMatcher }
        );
    }

    public void deposit(ActivePokemon toDeposit) {
        Player player = Game.getPlayer();
        if (toDeposit.isEgg()) {
            Messages.add("We don't actually take eggs... Sorry.");
            return;
        }

        if (!player.canDeposit(toDeposit)) {
            Messages.add("Can't deposit " + toDeposit.getName() + " at this time.");
            return;
        }

        if (first == null) {
            first = toDeposit;
        }
        else if (second == null) {
            second = toDeposit;
        }
        else {
            Global.error("Cannot deposit a Pokemon into a full Breeding center.");
        }

        Messages.add("Okay, we'll look after your " + toDeposit.getName() + " for a while.");
        player.getTeam().remove(toDeposit);
        toDeposit.fullyHeal();

        this.reset();
    }

    private void reset() {
        compatibility = Compatibility.getCompatibility(first, second);
        steps = 0;
        eggy = null;
    }

    public void withdraw(boolean isFirstPokemon) {
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

        Messages.add("Took back " + withdrawPokemon.getName() + " back for 500 " + PokeString.POKEDOLLARS + ".");

        this.reset();
    }

}
