package battle.effect.status;

import battle.Battle;
import pokemon.ActivePokemon;
import util.StringUtils;

class Fainted extends Status {
    private static final long serialVersionUID = 1L;

    public Fainted() {
        super(StatusCondition.FAINTED);
    }

    // Fainted status condition applies regardless of other status conditions
    @Override
    protected boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim) {
        return this.statusApplies(b, caster, victim);
    }

    @Override
    protected boolean statusApplies(Battle b, ActivePokemon caster, ActivePokemon victim) {
        return victim.getHP() == 0;
    }

    public String getCastMessage(ActivePokemon p) {
        return p.getName() + " fainted!";
    }

    public String getAbilityCastMessage(ActivePokemon abilify, ActivePokemon victim) {
        return abilify.getName() + "'s " + abilify.getAbility().getName() + " caused " + victim.getName() + " to faint!";
    }

    public String getGenericRemoveMessage(ActivePokemon victim) {
        return StringUtils.empty();
    }

    public String getSourceRemoveMessage(ActivePokemon victim, String sourceName) {
        return StringUtils.empty();
    }
}
