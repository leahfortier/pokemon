package trainer;

import battle.effect.generic.Effect;
import battle.effect.generic.TeamEffect;
import battle.effect.generic.EffectNamesies;
import pokemon.ActivePokemon;

import java.util.ArrayList;
import java.util.List;

public class WildPokemon implements Opponent {
	private final ActivePokemon wildPokemon;
	private List<TeamEffect> effects;
	
	public WildPokemon(ActivePokemon wildPokemon) {
		this.wildPokemon = wildPokemon;
		this.effects = new ArrayList<>();
	}
	
	public ActivePokemon front() {
		return wildPokemon;
	}
	
	public List<TeamEffect> getEffects() {
		return effects;
	}
	
	public void resetEffects() {
		effects = new ArrayList<>();
	}
	
	public void resetUsed() {
		wildPokemon.getAttributes().setUsed(true);
	}
	
	public boolean hasEffect(EffectNamesies effect) {
		return Effect.hasEffect(effects, effect);
	}
	
	public void addEffect(TeamEffect e) {
		effects.add(e);
	}
	
	public List<ActivePokemon> getTeam() {
		List<ActivePokemon> list = new ArrayList<>();
		list.add(wildPokemon);
		return list;
	}
	
	public boolean blackout() {
		return !wildPokemon.canFight();
	}
}
