package map.triggers;

import gui.view.ViewMode;
import pattern.action.ActionMatcher.ChoiceActionMatcher;
import pattern.action.ChoiceMatcher;
import pattern.action.EnumActionMatcher.ChangeViewActionMatcher;
import sound.SoundTitle;
import trainer.player.medal.MedalTheme;
import util.string.PokeString;

public enum CommonTrigger {
    LOAD_PC(new GroupTrigger(
            new DialogueTrigger("Starting up PC..."),
            new ChangeViewTrigger(ViewMode.PC_VIEW)
    )),
    LOAD_MART(new GroupTrigger(
            new DialogueTrigger("Welcome to the " + PokeString.POKE + "Mart!"),
            new ChoiceTrigger(new ChoiceActionMatcher(
                    "What would you like to do?",
                    new ChoiceMatcher[]{
                            new ChoiceMatcher("Buy", new ChangeViewActionMatcher(ViewMode.MART_VIEW)),
                            new ChoiceMatcher("Sell", new ChangeViewActionMatcher(ViewMode.BAG_VIEW)),
                    }
            ))
    )),
    POKE_CENTER(new GroupTrigger(
            // NOTE: If this is changed in any way, please also change the RSA Town Pokecenter trigger manually
            new DialogueTrigger("Welcome to the " + PokeString.POKEMON + " Center!"),
            new DialogueTrigger("Let me heal your " + PokeString.POKEMON + " for you!"),
            new SoundTrigger(SoundTitle.POKE_CENTER_HEAL),
            new DialogueTrigger("Dun Dun Dun-Dun Dun!"),
            new HealPartyTrigger(),
            new DialogueTrigger("Your " + PokeString.POKEMON + " have been healed!"),
            new DialogueTrigger("I hope to see you again soon!"),
            new MedalCountTrigger(MedalTheme.POKE_CENTER_HEALS)
    )),
    EGGY_HATCH(new GroupTrigger(
            new DialogueTrigger("Huh?"),
            new ChangeViewTrigger(ViewMode.EVOLUTION_VIEW)
    ));

    private final Trigger trigger;

    CommonTrigger(Trigger trigger) {
        this.trigger = trigger;
    }

    public Trigger getTrigger() {
        return this.trigger;
    }
}
