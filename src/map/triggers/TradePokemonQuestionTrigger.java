package map.triggers;

import main.Game;
import mapMaker.dialogs.action.trigger.TriggerActionType;
import message.MessageUpdate;
import message.Messages;
import pattern.TradePokemonMatcher;
import pattern.action.ActionMatcher;
import pattern.action.ChoiceActionMatcher;
import pattern.action.ChoiceActionMatcher.ChoiceMatcher;
import pattern.action.TriggerActionMatcher;
import pokemon.ActivePokemon;
import trainer.player.Player;
import util.PokeString;
import util.SerializationUtils;
import util.StringUtils;

import java.util.List;

public class TradePokemonQuestionTrigger extends Trigger {
    private final TradePokemonMatcher tradePokemonMatcher;

    TradePokemonQuestionTrigger(String contents, String condition) {
        super(TriggerType.TRADE_POKEMON_QUESTION, contents, condition);

        tradePokemonMatcher = SerializationUtils.deserializeJson(contents, TradePokemonMatcher.class);
    }

    @Override
    protected void executeTrigger() {
        Player player = Game.getPlayer();
        List<ActivePokemon> team = player.getTeam();

        // One option for each Pokemon plus cancel
        ChoiceMatcher[] choices = new ChoiceMatcher[team.size() + 1];
        for (int i = 0; i < team.size(); i++) {
            tradePokemonMatcher.setTeamIndex(i);

            ActionMatcher actionMatcher = new ActionMatcher();
            actionMatcher.setTrigger(new TriggerActionMatcher(
                    TriggerActionType.TRADE_POKEMON_ACTION,
                    SerializationUtils.getJson(tradePokemonMatcher))
            );

            choices[i] = new ChoiceMatcher(team.get(i).getName(), new ActionMatcher[] { actionMatcher });
        }

        ActionMatcher cancelAction = new ActionMatcher();
        cancelAction.setTrigger(new TriggerActionMatcher(TriggerActionType.DIALOGUE, StringUtils.empty()));
        choices[choices.length - 1] = new ChoiceMatcher("Cancel", new ActionMatcher[] { cancelAction });

        ChoiceActionMatcher choice = new ChoiceActionMatcher(
                "Which " + PokeString.POKEMON + " would you like to trade?",
                choices
        );

        Trigger choiceTrigger = TriggerType.CHOICE.createTrigger(SerializationUtils.getJson(choice));
        Messages.add(new MessageUpdate().withTrigger(choiceTrigger.getName()));
    }
}
