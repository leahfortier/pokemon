package trainer.player.medal;

import battle.attack.AttackNamesies;
import pokemon.ActivePokemon;
import type.Type;
import type.TypeAdvantage;

import java.io.Serializable;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/*
    TODO:
        Berry harvests
        Nicknames given
        Critical captures
        Hatch all babies
        Arceus plates
        Catch legendary trios
        trades
 */
public class MedalCase implements Serializable {
    private final Set<Medal> medalsEarned;

    private final Map<MedalTheme, Integer> themeCounters;

    private int totalPokemonCaught;
    private Map<Type, Integer> totalPokemonCaughtTypeMap;

    public MedalCase() {
        this.medalsEarned = EnumSet.noneOf(Medal.class);

        this.themeCounters = new EnumMap<>(MedalTheme.class);
        for (MedalTheme theme : MedalTheme.values()) {
            this.themeCounters.put(theme, 0);
        }

        this.totalPokemonCaughtTypeMap = new EnumMap<>(Type.class);
        for (Type type : Type.values()) {
            this.totalPokemonCaughtTypeMap.put(type, 0);
        }
    }

    private boolean hasMedal(Medal medal) {
        return medalsEarned.contains(medal);
    }

    public void earnMedal(Medal medal) {
        if (!this.hasMedal(medal)) {
            // TODO: Animation
            medalsEarned.add(medal);
            System.out.println("Medal Earned: " + medal.getMedalName() + "!");

            this.update(MedalTheme.MEDALS_COLLECTED, this.medalsEarned.size());
        }
    }

    public void catchNewPokemon(ActivePokemon caught) {
        totalPokemonCaught++;

        for (Type type : caught.getActualType()) {
            totalPokemonCaughtTypeMap.put(type, totalPokemonCaughtTypeMap.get(type) + 1);
        }
    }

    public void encounterPokemon(ActivePokemon encountered) {
        if (encountered.isShiny()) {
            this.increase(MedalTheme.SHINIES_FOUND);
        }
    }

    public void useMove(AttackNamesies attack, double advantage) {
        if (attack == AttackNamesies.SPLASH) {
            earnMedal(Medal.MAGIKARP_AWARD);
        }
        else if (attack == AttackNamesies.STRUGGLE) {
            earnMedal(Medal.NEVER_GIVE_UP);
        }

        if (TypeAdvantage.isSuperEffective(advantage)) {
            this.increase(MedalTheme.SUPER_EFFECTIVE_MOVES_USED);
        }
        else if (TypeAdvantage.isNotVeryEffective(advantage)) {
            earnMedal(Medal.NONEFFECTIVE_ARTIST);
        }
    }

    private void checkThreshold(MedalTheme theme) {
        theme.checkThreshold(themeCounters.get(theme));
    }

    public void update(MedalTheme theme, int count) {
        this.themeCounters.put(theme, count);
        this.checkThreshold(theme);
    }

    public void increase(MedalTheme theme) {
        this.increase(theme, 1);
    }

    public void increase(MedalTheme theme, int amount) {
        this.themeCounters.put(theme, this.themeCounters.get(theme) + amount);
        this.checkThreshold(theme);
    }
}
