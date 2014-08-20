package main;
import gui.GameData;
import gui.view.BagView;
import gui.view.BattleView;
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
import java.io.File;
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
		MART_VIEW, OPTIONS_VIEW, START_VIEW
	}
	
	public GameData data;
	public CharacterData charData;
	public EnumMap<ViewMode, View> viewMap;
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
		charData.addPokemon(null, new ActivePokemon(PokemonInfo.getPokemonInfo("Eevee"), 1, false, true));
		charData.front().giveItem((HoldItem)Item.getItem("Oran Berry"));
		
		//charData.addPokemon(null, new ActivePokemon(PokemonInfo.getRandomBaseEvolution()));
	}

	public void update(int dt, InputControl input) 
	{
		if (!currentView.getViewModel().equals(currentViewMode))
		{
			input.resetKeys();
			currentView = viewMap.get(currentViewMode);
			currentView.movedToFront();
		}
		currentView.update(dt, input, this);
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
		charData = new CharacterData();
		charData.setFileNum(index);
		charData = charData.load();
		setViews();
	}
	
	public void newSave(int index)
	{
		charData = new CharacterData();
		
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

	public void deleteSave(int index) 
	{
		File file = new File("saves"+Global.FILE_SLASH+"File"+(index+1)+".ser");
		File preview = new File("saves"+Global.FILE_SLASH+"Preview"+(index+1)+".out");
		if (file.exists())
			file.delete();
		if (preview.exists())
			preview.delete();
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
	}
}
