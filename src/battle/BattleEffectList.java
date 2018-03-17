package battle;

import battle.effect.Effect;
import battle.effect.EffectList;
import battle.effect.EffectNamesies.BattleEffectNamesies;
import battle.effect.battle.BattleEffect;
import battle.effect.battle.terrain.TerrainEffect;
import battle.effect.battle.weather.WeatherEffect;
import battle.effect.battle.weather.WeatherNamesies;
import battle.effect.interfaces.InvokeInterfaces.BattleEndTurnEffect;
import battle.effect.interfaces.InvokeInterfaces.EndTurnEffect;
import battle.effect.interfaces.InvokeInterfaces.SuperDuperEndTurnEffect;
import battle.effect.interfaces.InvokeInterfaces.TerrainCastEffect;
import battle.effect.interfaces.InvokeInterfaces.WeatherEliminatingEffect;
import main.Game;
import map.area.AreaData;
import map.overworld.TerrainType;
import map.weather.WeatherState;
import message.MessageUpdate;
import message.Messages;
import trainer.Opponent;
import trainer.PlayerTrainer;

import java.util.List;

public class BattleEffectList extends EffectList<BattleEffectNamesies, BattleEffect<? extends BattleEffectNamesies>> {
    private static final long serialVersionUID = 1L;

    private transient Battle battle;

    private WeatherState baseWeather;
    private WeatherEffect weather;

    private TerrainType baseTerrain;
    private TerrainEffect currentTerrain;

    void initialize(Battle battle) {
        this.setBattle(battle);

        // Would ideally want these in the constructor but that causes NPEs since battle will
        // reference this.effects before it is officially set at the end of the constructor
        AreaData area = Game.getPlayer().getArea();
        this.setBaseWeather(area.getWeather());
        this.setBaseTerrain(area.getBattleTerrain());
    }

    void setBattle(Battle battle) {
        this.battle = battle;
    }

    @Override
    public List<BattleEffect<? extends BattleEffectNamesies>> asList() {
        List<BattleEffect<? extends BattleEffectNamesies>> list = super.asList();
        list.add(weather);
        if (currentTerrain != null) {
            list.add(currentTerrain);
        }
        return list;
    }

    @Override
    public void reset() {
        super.reset();
        this.setBaseWeather(this.baseWeather);
        this.setBaseTerrain(this.baseTerrain);
    }

    @Override
    public void add(BattleEffect<? extends BattleEffectNamesies> effect) {
        if (effect instanceof WeatherEffect) {
            weather = (WeatherEffect)effect;
            Messages.add(new MessageUpdate().withWeather(weather));

            if (WeatherEliminatingEffect.shouldEliminateWeather(battle, battle.getPlayer().front(), weather)
                    || WeatherEliminatingEffect.shouldEliminateWeather(battle, battle.getOpponent().front(), weather)) {
                weather = WeatherNamesies.CLEAR_SKIES.getEffect();
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
    public void remove(BattleEffect<? extends BattleEffectNamesies> effect) {
        if (effect == weather) {
            this.setBaseWeather(this.baseWeather);
        } else if (effect == currentTerrain) {
            this.setBaseTerrain(this.baseTerrain);
        } else {
            super.remove(effect);
        }
    }

    @Override
    public boolean remove(BattleEffectNamesies effectToRemove) {
        if (weather.namesies() == effectToRemove) {
            this.remove(weather);
            return true;
        } else if (currentTerrain != null && currentTerrain.namesies() == effectToRemove) {
            this.remove(currentTerrain);
            return true;
        } else {
            return super.remove(effectToRemove);
        }
    }

    void printShit() {
        List<BattleEffect<? extends BattleEffectNamesies>> effects = super.asList();
        if (!effects.isEmpty()) {
            System.out.println("Battle:");
            for (Effect effect : effects) {
                System.out.println("\t" + effect);
            }
        }

        if (weather.namesies() != baseWeather.getWeatherEffect()) {
            System.out.println("Weather: " + this.weather);
        }

        if (currentTerrain != null) {
            System.out.println("Terrain: " + this.currentTerrain);
        }
    }

    public WeatherEffect getWeather() {
        return weather;
    }

    public TerrainType getTerrainType() {
        return this.currentTerrain == null ? this.baseTerrain : this.currentTerrain.getTerrainType();
    }

    private void setBaseWeather(WeatherState weatherState) {
        this.baseWeather = weatherState;
        this.add(weatherState.getWeatherEffect().getEffect());
    }

    private void setBaseTerrain(TerrainType terrainType) {
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
        BattleEndTurnEffect.invokeBattleEndTurnEffect(battle);

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
        me.endTurn();
    }
}
