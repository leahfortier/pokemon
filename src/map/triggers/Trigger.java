package map.triggers;

import gui.GameData;
import gui.view.ViewMode;
import main.Game;
import map.condition.Condition;
import map.condition.ConditionSet;
import pattern.GroupTriggerMatcher;
import sound.SoundTitle;
import trainer.player.medal.MedalTheme;
import util.PokeString;
import util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public abstract class Trigger {
    private final String name;
    private final ConditionSet condition;
    private final List<String> globals;

    protected Trigger(String suffix, Condition condition) {
        this(suffix, condition, null);
    }

    protected Trigger(String suffix, Condition condition, List<String> globals) {
        this.name = createName(this.getClass(), suffix);

        this.condition = new ConditionSet(condition);

        this.globals = new ArrayList<>();
        if (globals != null) {
            this.globals.addAll(globals);
        }

        Game.getData().addTrigger(this);
    }

    public static String createName(Class<? extends Trigger> classy, String triggerSuffix) {
        return classy.getSimpleName() + (StringUtils.isNullOrEmpty(triggerSuffix) ? "" : "_" + triggerSuffix);
    }

    protected abstract void executeTrigger();

    // Evaluate the function, Should only be triggered when a player moves
    // into a map square that is defined to trigger this event
    public boolean isTriggered() {
        return condition.evaluate();
    }

    public String getName() {
        return this.name;
    }

    public final void execute() {
        for (String global : globals) {
            if (global.startsWith("!")) {
                Game.getPlayer().removeGlobal(global.substring(1));
            } else {
                Game.getPlayer().addGlobal(global);
            }
        }

        this.executeTrigger();
    }

    public static void createCommonTriggers() {
        // Explicitly add trigger even though it happens in the constructor since it looks weird and just in case it changes
        GameData data = Game.getData();

        // PC Start Up
        GroupTriggerMatcher loadPC = new GroupTriggerMatcher(
                "LoadPC",
                new DialogueTrigger("Starting up PC...", null),
                new ChangeViewTrigger(ViewMode.PC_VIEW, null)
        );
        data.addTrigger(new GroupTrigger(loadPC, null));

        // Mart Bro
        GroupTriggerMatcher loadMart = new GroupTriggerMatcher(
                "LoadMart",
                new DialogueTrigger("Welcome to the " + PokeString.POKE + "Mart!", null),
                new ChangeViewTrigger(ViewMode.MART_VIEW, null)
        );
        data.addTrigger(new GroupTrigger(loadMart, null));

        // PokeCenter Healing
        // NOTE: If this is changed in any way, please also change the RSA Town Pokecenter trigger manually
        GroupTriggerMatcher pokeCenterHeal = new GroupTriggerMatcher(
                "PokeCenterHeal",
                new DialogueTrigger("Welcome to the " + PokeString.POKEMON + " Center!", null),
                new DialogueTrigger("Let me heal your " + PokeString.POKEMON + " for you!", null),
                new SoundTrigger(SoundTitle.POKE_CENTER_HEAL, null),
                new DialogueTrigger("Dun Dun Dun-Dun Dun!", null),
                new HealPartyTrigger(null),
                new DialogueTrigger("Your " + PokeString.POKEMON + " have been healed!", null),
                new DialogueTrigger("I hope to see you again soon!", null),
                new MedalCountTrigger(MedalTheme.POKE_CENTER_HEALS.name(), null)
        );
        data.addTrigger(new GroupTrigger(pokeCenterHeal, null));

        // Egg hatching
        GroupTriggerMatcher eggHatching = new GroupTriggerMatcher(
                "EggHatching",
                new DialogueTrigger("Huh?", null),
                new ChangeViewTrigger(ViewMode.EVOLUTION_VIEW, null)
        );
        data.addTrigger(new GroupTrigger(eggHatching, null));
    }
}
