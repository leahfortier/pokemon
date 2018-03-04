package battle;

import battle.effect.EffectList;
import battle.effect.generic.BattleEffect;
import battle.effect.generic.EffectInterfaces.BattleEndTurnEffect;
import battle.effect.generic.EffectInterfaces.EndTurnEffect;
import battle.effect.generic.EffectInterfaces.SuperDuperEndTurnEffect;
import battle.effect.generic.EffectInterfaces.TerrainCastEffect;
import battle.effect.generic.EffectInterfaces.TerrainEffect;
import battle.effect.generic.EffectInterfaces.WeatherEliminatingEffect;
import battle.effect.generic.EffectNamesies;
import battle.effect.generic.Weather;
import map.overworld.TerrainType;
import map.weather.WeatherState;
import message.MessageUpdate;
import message.Messages;
import trainer.Opponent;
import trainer.PlayerTrainer;

import java.io.Serializable;
import java.util.List;

class BattleEffectList extends EffectList<BattleEffect> implements Serializable {
    private static final long serialVersionUID = 1L;

    private transient Battle battle;

    private WeatherState baseWeather;
    private Weather weather;

    private TerrainType baseTerrain;
    private TerrainEffect currentTerrain;

    public BattleEffectList(Battle battle) {
        this.battle = battle;
    }

    void setBattle(Battle battle) {
        this.battle = battle;
    }

    @Override
    public List<BattleEffect> asList() {
        List<BattleEffect> list = super.asList();
        list.add(weather);
        if (currentTerrain != null) {
            list.add((BattleEffect)currentTerrain);
        }
        return list;
    }

    @Override
    public void add(BattleEffect effect) {
        if (effect instanceof Weather) {
            weather = (Weather)effect;
            Messages.add(new MessageUpdate().withWeather(weather));

            if (WeatherEliminatingEffect.shouldEliminateWeather(battle, battle.getPlayer().front(), weather)
                    || WeatherEliminatingEffect.shouldEliminateWeather(battle, battle.getOpponent().front(), weather)) {
                weather = (Weather)EffectNamesies.CLEAR_SKIES.getEffect();
                Messages.add(new MessageUpdate().withWeather(weather));
            }
        } else if (effect instanceof TerrainEffect) {
            currentTerrain = (TerrainEffect)effect;

            TerrainType terrainType = currentTerrain.getTerrainType();
            Messages.add(new MessageUpdate().withTerrain(terrainType));

            TerrainCastEffect.invokeTerrainCastEffect(battle, battle.getPlayer().front(), terrainType);
            TerrainCastEffect.invokeTerrainCastEffect(battle, battle.getOpponent().front(), terrainType);
        } else {
            super.add(effect);
        }
    }

    @Override
    public void remove(BattleEffect effect) {
        if (effect instanceof Weather) {
            this.setBaseWeather(this.baseWeather);
        } else if (effect instanceof TerrainEffect) {
            this.setBaseTerrain(this.baseTerrain);
        } else {
            super.remove(effect);
        }
    }

    public Weather getWeather() {
        return weather;
    }

    public WeatherState getBaseWeather() {
        return this.baseWeather;
    }

    public TerrainType getTerrainType() {
        return this.currentTerrain == null ? this.baseTerrain : this.currentTerrain.getTerrainType();
    }

    void setBaseWeather(WeatherState weatherState) {
        this.baseWeather = weatherState;
        this.add((Weather)weatherState.getWeatherEffect().getEffect());
    }

    void setBaseTerrain(TerrainType terrainType) {
        this.baseTerrain = terrainType;
        this.currentTerrain = null;
        Messages.add(new MessageUpdate().withTerrain(terrainType));
    }

    void endTurn() {
        PlayerTrainer player = battle.getPlayer();
        Opponent opponent = battle.getOpponent();

        // Apply End Turn Effects
        endTurnPokemonEffects(player.front());
        endTurnPokemonEffects(opponent.front());
        BattleEndTurnEffect.invokeBattleEndTurnEffect(this.asList(), battle);

        // Decrement Pokemon effects
        player.front().getEffects().decrement(battle, player.front());
        opponent.front().getEffects().decrement(battle, opponent.front());

        // Decrement Team effects
        player.getEffects().decrement(battle, player.front());
        opponent.getEffects().decrement(battle, opponent.front());

        // Decrement Battle effects
        this.decrement(battle, null);

        // The very, very end
        while (SuperDuperEndTurnEffect.checkSuperDuperEndTurnEffect(battle, player.front())
                || SuperDuperEndTurnEffect.checkSuperDuperEndTurnEffect(battle, opponent.front())) {}
    }

    private void endTurnPokemonEffects(ActivePokemon me) {
        EndTurnEffect.invokeEndTurnEffect(me, battle);

        me.isFainted(battle);

        // No longer the first turn anymore
        me.setFirstTurn(false);
    }
}
