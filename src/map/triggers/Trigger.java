package map.triggers;

import gui.view.ViewMode;
import main.Game;
import map.condition.Condition;
import pattern.GroupTriggerMatcher;
import sound.SoundTitle;
import trainer.player.medal.MedalTheme;
import util.PokeString;
import util.SerializationUtils;

import java.util.ArrayList;
import java.util.List;

public abstract class Trigger {

    protected final String name;
    protected final Condition condition;
    private final List<String> globals;

    protected Trigger(TriggerType type, String contents, String condition) {
        this(type, contents, condition, null);
    }

    protected Trigger(TriggerType type, String contents, String condition, List<String> globals) {
        this.name = type.getTriggerName(contents);

        this.condition = new Condition(condition);

        this.globals = new ArrayList<>();
        if (globals != null) {
            this.globals.addAll(globals);
        }
    }

    // Evaluate the function, Should only be triggered when a player moves
    // into a map square that is defined to trigger this event
    public boolean isTriggered() {
        return condition.isTrue();
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

    protected abstract void executeTrigger();

    public static void createCommonTriggers() {

        // PC Start Up
        GroupTriggerMatcher loadPC = new GroupTriggerMatcher(
                "LoadPC",
                TriggerType.DIALOGUE.createTrigger("Starting up PC...", null).getName(),
                TriggerType.CHANGE_VIEW.createTrigger(ViewMode.PC_VIEW.name(), null).getName()
        );
        TriggerType.GROUP.createTrigger(SerializationUtils.getJson(loadPC), null);

        // Mart Bro
        GroupTriggerMatcher loadMart = new GroupTriggerMatcher(
                "LoadMart",
                TriggerType.DIALOGUE.createTrigger("Welcome to the " + PokeString.POKE + "Mart!", null).getName(),
                TriggerType.CHANGE_VIEW.createTrigger(ViewMode.MART_VIEW.name(), null).getName()
        );
        TriggerType.GROUP.createTrigger(SerializationUtils.getJson(loadMart), null);

        // PokeCenter Healing
        // NOTE: If this is changed in any way, please also change the RSA Town Pokecenter trigger manually
        GroupTriggerMatcher pokeCenterHeal = new GroupTriggerMatcher(
                "PokeCenterHeal",
                TriggerType.DIALOGUE.createTrigger("Welcome to the " + PokeString.POKEMON + " Center!", null).getName(),
                TriggerType.DIALOGUE.createTrigger("Let me heal your " + PokeString.POKEMON + " for you!", null).getName(),
                TriggerType.SOUND.createTrigger(SoundTitle.POKE_CENTER_HEAL.name(), null).getName(),
                TriggerType.DIALOGUE.createTrigger("Dun Dun Dun-Dun Dun!", null).getName(),
                TriggerType.HEAL_PARTY.createTrigger(null, null).getName(),
                TriggerType.DIALOGUE.createTrigger("Your " + PokeString.POKEMON + " have been healed!", null).getName(),
                TriggerType.DIALOGUE.createTrigger("I hope to see you again soon!", null).getName(),
                TriggerType.MEDAL_COUNT.createTrigger(MedalTheme.POKE_CENTER_HEALS.name(), null).getName()
        );
        TriggerType.GROUP.createTrigger(SerializationUtils.getJson(pokeCenterHeal), null);

        // Egg hatching
        GroupTriggerMatcher eggHatching = new GroupTriggerMatcher(
                "EggHatching",
                TriggerType.DIALOGUE.createTrigger("Huh?", null).getName(),
                TriggerType.CHANGE_VIEW.createTrigger(ViewMode.EVOLUTION_VIEW.name(), null).getName()
        );
        TriggerType.GROUP.createTrigger(SerializationUtils.getJson(eggHatching), null);
    }
}
