package map.triggers;

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

    protected Trigger(String suffix) {
        this(suffix, null);
    }

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

    public Trigger addData() {
        Game.getData().addTrigger(this);
        return this;
    }

    public static void createCommonTriggers() {
        // PC Start Up
        GroupTriggerMatcher loadPC = new GroupTriggerMatcher(
                "LoadPC",
                new DialogueTrigger("Starting up PC..."),
                new ChangeViewTrigger(ViewMode.PC_VIEW)
        );
        new GroupTrigger(loadPC, null).addData();

        // Mart Bro
        GroupTriggerMatcher loadMart = new GroupTriggerMatcher(
                "LoadMart",
                new DialogueTrigger("Welcome to the " + PokeString.POKE + "Mart!"),
                new ChangeViewTrigger(ViewMode.MART_VIEW)
        );
        new GroupTrigger(loadMart, null).addData();

        // PokeCenter Healing
        // NOTE: If this is changed in any way, please also change the RSA Town Pokecenter trigger manually
        GroupTriggerMatcher pokeCenterHeal = new GroupTriggerMatcher(
                "PokeCenterHeal",
                new DialogueTrigger("Welcome to the " + PokeString.POKEMON + " Center!"),
                new DialogueTrigger("Let me heal your " + PokeString.POKEMON + " for you!"),
                new SoundTrigger(SoundTitle.POKE_CENTER_HEAL),
                new DialogueTrigger("Dun Dun Dun-Dun Dun!"),
                new HealPartyTrigger(),
                new DialogueTrigger("Your " + PokeString.POKEMON + " have been healed!"),
                new DialogueTrigger("I hope to see you again soon!"),
                new MedalCountTrigger(MedalTheme.POKE_CENTER_HEALS)
        );
        new GroupTrigger(pokeCenterHeal, null).addData();

        // Egg hatching
        GroupTriggerMatcher eggHatching = new GroupTriggerMatcher(
                "EggHatching",
                new DialogueTrigger("Huh?"),
                new ChangeViewTrigger(ViewMode.EVOLUTION_VIEW)
        );
        new GroupTrigger(eggHatching, null).addData();
    }
}
