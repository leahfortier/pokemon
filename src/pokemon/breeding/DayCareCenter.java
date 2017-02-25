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
import util.JsonUtils;
import util.PokeString;

import java.io.Serializable;
import java.util.List;

public class DayCareCenter implements Serializable {
    private ActivePokemon first;
    private ActivePokemon second;

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
        ChoiceMatcher[] choices = new ChoiceMatcher[team.size()];
        for (int i = 0; i < choices.length; i++) {
            ActionMatcher actionMatcher = new ActionMatcher();
            actionMatcher.setTrigger(new TriggerActionMatcher(TriggerActionType.BREEDING_DEPOSIT, i + ""));

            ActivePokemon pokemon = team.get(i);
            choices[i] = new ChoiceMatcher(
                    pokemon.getName() + " " + pokemon.getGenderString(),
                    new ActionMatcher[] { actionMatcher }
            );
        }

        ChoiceActionMatcher choice = new ChoiceActionMatcher(
                "Which " + PokeString.POKEMON + " would you like to deposit?",
                choices
        );

        return TriggerType.CHOICE.createTrigger(JsonUtils.getJson(choice));
    }

    public Trigger getWithdrawTrigger() {
        // Both are null -- display invalid message
        if (first == null && second == null) {
            return TriggerType.DIALOGUE.createTrigger("No " + PokeString.POKEMON + " to withdraw.");
        }

        // Only one pokemon here
        if (first == null) {
            return TriggerType.BREEDING_WITHDRAW.createTrigger("false");
        } else if (second == null) {
            return TriggerType.BREEDING_WITHDRAW.createTrigger("true");
        }

        // Two Pokemon in center -- choice option
        ChoiceMatcher chooseFirst = getChoice(true);
        ChoiceMatcher chooseSecond = getChoice(false);

        ChoiceActionMatcher choice = new ChoiceActionMatcher(
                "Which " + PokeString.POKEMON + " would you like to take back?",
                new ChoiceMatcher[] { chooseFirst, chooseSecond }
        );

        return TriggerType.CHOICE.createTrigger(JsonUtils.getJson(choice));
    }

    private ChoiceMatcher getChoice(boolean isFirst) {
        ActionMatcher actionMatcher = new ActionMatcher();
        actionMatcher.setTrigger(new TriggerActionMatcher(TriggerActionType.BREEDING_WITHDRAW, isFirst + ""));

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

        if (this.first == null) {
            this.first = toDeposit;
        }
        else if (second == null) {
            second = toDeposit;
        }
        else {
            Global.error("Cannot deposit a Pokemon into a full Breeding center.");
        }

        Messages.add("Okay, we'll look after your " + toDeposit.getName() + " for a while.");
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
    }

}
