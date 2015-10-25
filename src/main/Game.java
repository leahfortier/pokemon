package main;
import gui.GameData;
import gui.view.BagView;
import gui.view.BattleView;
import gui.view.EvolutionView;
import gui.view.MainMenuView;
import gui.view.MapView;
import gui.view.MartView;
import gui.view.OptionsView;
import gui.view.PCView;
import gui.view.PartyView;
import gui.view.PokedexView;
import gui.view.StartView;
import gui.view.TrainerCardView;
import gui.view.View;
import item.Item;
import item.hold.HoldItem;

import java.awt.Graphics;
import java.util.EnumMap;

import pokemon.ActivePokemon;
import pokemon.PokemonInfo;
import trainer.CharacterData;


public class Game
{
	public static enum ViewMode
	{
		MAP_VIEW, POKEDEX_VIEW, BATTLE_VIEW, BAG_VIEW, 
		PARTY_VIEW, PC_VIEW, MAIN_MENU_VIEW, TRAINER_CARD_VIEW, 
		MART_VIEW, OPTIONS_VIEW, START_VIEW, EVOLUTION_VIEW
	}
	
	public final GameData data;
	public final EnumMap<ViewMode, View> viewMap;
	
	public CharacterData charData;
	private ViewMode currentViewMode;
	private View currentView;
	
	public Game()
	{
		data = new GameData();
		
		viewMap = new EnumMap<>(ViewMode.class);
		viewMap.put(ViewMode.MAIN_MENU_VIEW, new MainMenuView());
		
		currentViewMode = ViewMode.MAIN_MENU_VIEW;
		currentView = viewMap.get(ViewMode.MAIN_MENU_VIEW);
	}
	
	public void addCharStuff()
	{
		charData.addPokemon(null, new ActivePokemon(PokemonInfo.getPokemonInfo(Namesies.EEVEE_POKEMON), 1, false, true));
		charData.front().giveItem((HoldItem)Item.getItem(Namesies.ORAN_BERRY_ITEM));
	}
	
	private void checkViewSwitch(InputControl input)
	{
		if (!currentView.getViewModel().equals(currentViewMode))
		{
			input.resetKeys();
			currentView = viewMap.get(currentViewMode);
			currentView.movedToFront(this);
		}
	}

	public void update(int dt, InputControl input) 
	{
		checkViewSwitch(input);
		currentView.update(dt, input, this);
		checkViewSwitch(input);
	}

	public void draw(Graphics g) 
	{
		currentView.draw(g, data);
	}
	
	public void setViewMode(ViewMode mode)
	{
		currentViewMode = mode;
	}
	
	public ViewMode getCurrentViewMode() 
	{
		return currentViewMode;
	}

	public void loadSave(int index) 
	{
		charData = Save.load(index, this);
		setViews();
	}
	
	public void newSave(int index)
	{
		charData = new CharacterData(this);
		
		String startingMap = "PlayersHouseUp";
		String startingMapEntrance = "GameStartLocation";
		charData.setMap(startingMap, startingMapEntrance);
		data.getMap(startingMap).setCharacterToEntrance(charData, startingMapEntrance);
		
		charData.setFileNum(index);
		addCharStuff();
		setViews();
		
		// Testing things
//		EnemyTrainer rival = new EnemyTrainer("Blue", 500);
//		rival.addPokemon(null, new ActivePokemon(PokemonInfo.getPokemonInfo("Charmander"), 5, false, false));
//		Battle b = new Battle(charData, rival);
//		BattleView battleView = new BattleView();
//		battleView.setBattle(b);
//		viewMap.put(ViewMode.BATTLE_VIEW, battleView);
	}
	
	public void setViews()
	{
		viewMap.put(ViewMode.MAP_VIEW, new MapView());
		viewMap.put(ViewMode.BAG_VIEW, new BagView(charData));
		viewMap.put(ViewMode.PARTY_VIEW, new PartyView(charData));
		viewMap.put(ViewMode.PC_VIEW, new PCView(charData));
		viewMap.put(ViewMode.POKEDEX_VIEW, new PokedexView(charData.getPokedex()));
		viewMap.put(ViewMode.MART_VIEW, new MartView(charData));
		viewMap.put(ViewMode.TRAINER_CARD_VIEW, new TrainerCardView(charData));
		viewMap.put(ViewMode.BATTLE_VIEW, new BattleView());
		viewMap.put(ViewMode.OPTIONS_VIEW, new OptionsView());
		viewMap.put(ViewMode.START_VIEW, new StartView(charData));
		viewMap.put(ViewMode.EVOLUTION_VIEW, new EvolutionView(charData));
	}
}
