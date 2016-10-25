package trainer;

import battle.effect.generic.Effect;
import battle.effect.generic.TeamEffect;
import namesies.EffectNamesies;
import pokemon.ActivePokemon;

import java.util.ArrayList;
import java.util.List;

public class WildPokemon implements Opponent {
	private ActivePokemon p;
	private List<TeamEffect> effects;
	
	public WildPokemon(ActivePokemon p) {
		this.p = p;
		effects = new ArrayList<>();
	}
	
	public ActivePokemon front() {
		return p;
	}
	
	public List<TeamEffect> getEffects() {
		return effects;
	}
	
	public void resetEffects() {
		effects = new ArrayList<>();
	}
	
	public void resetUsed() {
		p.getAttributes().setUsed(true);
	}
	
	public boolean hasEffect(EffectNamesies effect) {
		return Effect.hasEffect(effects, effect);
	}
	
	public void addEffect(TeamEffect e) {
		effects.add(e.newInstance());
	}
	
	public List<ActivePokemon> getTeam() {
		List<ActivePokemon> list = new ArrayList<>();
		list.add(p);
		return list;
	}
	
	public boolean blackout() {
		return !p.canFight();
	}
}
