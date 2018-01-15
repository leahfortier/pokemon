package battle.effect.status;

import battle.ActivePokemon;
import battle.Battle;
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

    @Override
    public String getCastMessage(ActivePokemon p) {
        return p.getName() + " fainted!";
    }

    @Override
    public String getAbilityCastMessage(ActivePokemon abilify, ActivePokemon victim) {
        return abilify.getName() + "'s " + abilify.getAbility().getName() + " caused " + victim.getName() + " to faint!";
    }

    @Override
    public String getGenericRemoveMessage(ActivePokemon victim) {
        return StringUtils.empty();
    }

    @Override
    public String getSourceRemoveMessage(ActivePokemon victim, String sourceName) {
        return StringUtils.empty();
    }
}
